package org.mitre.xyz;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.servlet.http.HttpServletRequest;

import org.mitre.oauth2.model.AuthenticationHolderEntity;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.OAuth2TokenEntityService;
import org.mitre.oauth2.service.SystemScopeService;
import org.mitre.openid.connect.config.ConfigurationPropertiesBean;
import org.mitre.openid.connect.view.HttpCodeView;
import org.mitre.openid.connect.view.JsonEntityView;
import org.mitre.openid.connect.view.JsonErrorView;
import org.mitre.xyz.Hash.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObject;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Base64URL;

/**
 * @author jricher
 *
 */
@Controller
public class TxEndpoint {

	public enum Status {

		NEW,		// newly created transaction, nothing's been done to it yet
		ISSUED,		// an access token has been issued
		AUTHORIZED,	// the user has authorized but a token has not been issued yet
		WAITING,	// we are waiting for the user
		DENIED; 	// the user denied the transaction
	}

	@Autowired
	private ClientDetailsEntityService clientService;

	@Autowired
	private TxService txService;

	@Autowired
	private SystemScopeService scopeService;

	@Autowired
	private OAuth2TokenEntityService tokenService;

	@Autowired
	private ConfigurationPropertiesBean config;

	@RequestMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
		produces = MediaType.APPLICATION_JSON_VALUE,
		path = "/transaction",
		method = RequestMethod.POST)
	public String transaction(@RequestBody String incoming,
		@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String auth,
		@RequestHeader(name = "Signature", required = false) String signature,
		@RequestHeader(name = "Digest", required = false) String digest,
		@RequestHeader(name = "Detached-JWS", required = false) String jwsd,
		@RequestHeader(name = "DPoP", required = false) String dpop,
		@RequestHeader(name = "PoP", required = false) String oauthPop,
		Model m,
		HttpServletRequest req) {

		JsonObject json = JsonParser.parseString(incoming).getAsJsonObject();

		TxEntity tx;

		if (json.has("handle")) {
			// it's a handle to an existing transaction, load it up and wire things in
			tx = txService.loadByHandle(json.get("handle").getAsString());
		} else {
			// otherwise we build one from the parts

			tx = new TxEntity();
			ClientDetailsEntity client = loadOrRegisterClient(json);

			if (client == null) {
				m.addAttribute(JsonErrorView.ERROR, "unknown_key");
				m.addAttribute(JsonErrorView.ERROR_MESSAGE, "The key handle presented does not match a client.");
				m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
				return JsonErrorView.VIEWNAME;
			}


			tx.setClient(client);

			// scopes are passed in as handles for the resources
			JsonArray resources = json.get("resources").getAsJsonArray();
			Set<String> scopes = StreamSupport.stream(resources.spliterator(), false)
				.filter( e -> e.isJsonPrimitive() ) // filter out anything that's not a handle
				.map( e -> e.getAsString() )
				.collect(Collectors.toSet());
			tx.setScope(scopes);

			tx.setStatus(Status.NEW);
		}

		// process transaction

		// check signatures

		// get the only key
		if (tx.getClient().getJwks() == null || tx.getClient().getJwks().getKeys().size() == 0) {
			m.addAttribute(JsonErrorView.ERROR, "unknown_key");
			m.addAttribute(JsonErrorView.ERROR_MESSAGE, "The key handle presented does not map to a key.");
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			return JsonErrorView.VIEWNAME;
		}

		JWKSet clientJwks = tx.getClient().getJwks();

		if (clientJwks.getKeys().size() != 1) {
			m.addAttribute(JsonErrorView.ERROR, "unknown_key");
			m.addAttribute(JsonErrorView.ERROR_MESSAGE, "The key handle presented maps to multiple keys.");
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			return JsonErrorView.VIEWNAME;
		}

		// TODO: this doesn't allow for multiple keys to be selected, this could be a client property
		// TODO: this doesn't allow for jwks_uri loaded keys
		JWK clientJwk = clientJwks.getKeys().get(0);

		// check the signature on the incoming request
		// TODO: make this configurable on clients, for now assume JWSD
		checkDetachedJws(jwsd, incoming, clientJwk);

		// process the transaction based on its current state
		switch (tx.getStatus()) {
			case NEW:
				// now make sure the client is asking for scopes that it's allowed to
				// note: if the client has no scopes registered, it can ask for anything
				if (!tx.getClient().getScope().isEmpty()) {
					if (!scopeService.scopesMatch(tx.getClient().getScope(), tx.getScope())) {
						m.addAttribute(JsonErrorView.ERROR, "resource_not_allowed");
						m.addAttribute(JsonErrorView.ERROR_MESSAGE, "The client requested resources it does not have access to.");
						m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
						return JsonErrorView.VIEWNAME;
					}
				}

				OAuth2Request o2r = new OAuth2Request(
					Collections.emptyMap(), tx.getClient().getClientId(),
					tx.getClient().getAuthorities(), tx.getStatus().equals(Status.AUTHORIZED),
					tx.getScope(), null, tx.getCallbackUri(), null, null);

				OAuth2Authentication o2a = new OAuth2Authentication(o2r, null);

				AuthenticationHolderEntity ah = new AuthenticationHolderEntity();
				ah.setAuthentication(o2a);
				ah.setApproved(false);

				tx.setAuthenticationHolder(ah);

				// look back at the request to process the interaction parameters
				JsonObject interact = json.get("interact").getAsJsonObject();
				if (interact == null) {
					if (tx.getClient().getGrantTypes().contains("client_credentials")) {
						// TODO client can do credentials-only grant, issue a token

					}
				} else {
					Map<String, Object> map = new HashMap<>();

					// we support "redirect" and "callback" here
					if (interact.has("redirect")) {
						// generate an interaction URL
						String interactPage = UUID.randomUUID().toString();
						tx.setInteraction(interactPage);

						String interactUrl = config.getIssuer() + "interact/" + interactPage;
						map.put("interaction_url", interactUrl);
					}

					if (interact.has("callback")) {
						JsonObject callback = interact.get("callback").getAsJsonObject();

						String callbackString = callback.get("uri").getAsString();
						Path callbackPath = Paths.get(URI.create(callbackString).getPath());

						if (!tx.getClient().getRedirectUris().isEmpty()) {
							// we do sub-path matching for the callback
							// FIXME: this is a really simplistic filter that definitely has holes in it
							boolean callbackMatches = tx.getClient().getRedirectUris().stream()
								.filter(s -> callbackString.startsWith(s))
								.map(URI::create)
								.map(URI::getPath)
								.map(Paths::get)
								.anyMatch(path ->
									callbackPath.startsWith(path)
								);

							if (!callbackMatches) {
								m.addAttribute(JsonErrorView.ERROR, "invalid_callback_uri");
								m.addAttribute(JsonErrorView.ERROR_MESSAGE, "The client presented a callback URI that did not match one registered.");
								m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
								return JsonErrorView.VIEWNAME;
							}
						}

						tx.setCallbackUri(callbackString);

						tx.setClientNonce(callback.get("nonce").getAsString());

						if (callback.has("hash_method")) {
							tx.setHashMethod(Hash.Method.fromJson(callback.get("hash_method").getAsString()));
						} else {
							tx.setHashMethod(Method.SHA3);
						}

						String serverNonce = UUID.randomUUID().toString();
						tx.setServerNonce(serverNonce);

						map.put("server_nonce", serverNonce);
					}

					// rotate the handle
					String handle = UUID.randomUUID().toString();
					tx.setHandle(handle);

					Map<String, String> h = new HashMap<>();
					h.put("value", handle);
					h.put("presentation", "bearer");
					map.put("handle", h);

					if (tx.getClient().isDynamicallyRegistered()) {
						Map<String, String> kh = ImmutableMap.of(
							"value", tx.getClient().getClientId(),
							"presentation", "bearer"
							);
						map.put("key_handle", kh);
					}

					txService.save(tx);

					m.addAttribute(JsonEntityView.ENTITY, map);
					return JsonEntityView.VIEWNAME;
				}

				break;
			case AUTHORIZED:

				Map<String, Object> map = new HashMap<>();

				OAuth2Authentication storedAuth = tx.getAuthenticationHolder().getAuthentication();

				OAuth2AccessToken accessToken = tokenService.createAccessToken(storedAuth);

				Map<String, String> at = new HashMap<>();
				at.put("value", accessToken.getValue());
				at.put("presentation", accessToken.getTokenType().toLowerCase());
				at.put("expiration", accessToken.getExpiration().toInstant().toString());
				map.put("access_token", at);

				tx.setStatus(Status.ISSUED);

				if (accessToken.getAdditionalInformation().containsKey("id_token")) {
					// add in the ID token if it's included
					Map<String, String> c = new HashMap<>();
					c.put("oidc_id_token", (String) accessToken.getAdditionalInformation().get("id_token"));
					map.put("claims", c);

					// TODO: save the claims request and translate that directly
				}

				// rotate the handle
				String handle = UUID.randomUUID().toString();
				tx.setHandle(handle);

				Map<String, String> h = new HashMap<>();
				h.put("value", handle);
				h.put("presentation", "bearer");
				map.put("handle", h);

				txService.save(tx);
				m.addAttribute(JsonEntityView.ENTITY, map);
				return JsonEntityView.VIEWNAME;
			case DENIED:
				break;
			case ISSUED:
				break;
			case WAITING:
				break;
			default:
				break;

		}

		m.addAttribute(JsonErrorView.ERROR, "transaction_error");
		m.addAttribute(JsonErrorView.ERROR_MESSAGE, "There was an error processing the transaction.");
		m.addAttribute(HttpCodeView.CODE, HttpStatus.INTERNAL_SERVER_ERROR);
		return JsonErrorView.VIEWNAME;
	}


	private ClientDetailsEntity loadOrRegisterClient(JsonObject json) {

		if (json.get("keys").isJsonObject()) {
			// dynamically register the client

			if (!json.get("keys").getAsJsonObject().has("jwk")) {
				// we can only do a JWKS-based key proof
				return null;
			}

			if (json.get("keys").getAsJsonObject().has("proof")
				&& json.get("keys").getAsJsonObject().get("proof").getAsString().equals("jwsd")) {
				try {

					String jwkString = json.get("keys").getAsJsonObject().get("jwk").toString();
					JWK jwk = JWK.parse(jwkString); // we have to round-trip this to get into the native object format for Nimbus

					// TODO: see if we can figure out how to look up the client by its key value
					//ClientDetailsEntity client = clientService.findClientByPublicKey(jwk);

					// we create a new client and register it with the given key
					ClientDetailsEntity client = new ClientDetailsEntity();
					client.setDynamicallyRegistered(true);
					client.setJwks(new JWKSet(jwk));

					if (json.has("display") && json.get("display").isJsonObject()) {
						JsonObject display = json.get("display").getAsJsonObject();

						if (display.has("name")) {
							client.setClientName(display.get("name").getAsString());
						}

						if (display.has("uri")) {
							client.setClientUri(display.get("uri").getAsString());
						}

						if (display.has("logo_uri")) {
							client.setLogoUri(display.get("logo_uri").getAsString());
						}
					}

					ClientDetailsEntity saved = clientService.saveNewClient(client);

					return saved;
				} catch (ParseException e) {
					return null;
				}
			} else {
				// unsupported proof type
				return null;
			}
		} else {
			// client ID is passed in as the handle for the key object
			String clientId = json.get("keys").getAsString();

			// first, load the client
			ClientDetailsEntity client = clientService.loadClientByClientId(clientId);
			return client;
		}
	}


	private void checkDetachedJws(String jwsd, String requestBody, JWK clientKey) {
		try {

			Base64URL[] parts = JOSEObject.split(jwsd);
			Payload payload = new Payload(requestBody.getBytes());

			JWSObject jwsObject = new JWSObject(parts[0], payload, parts[2]);

			JWSVerifier verifier = new DefaultJWSVerifierFactory().createJWSVerifier(jwsObject.getHeader(),
				((RSAKey)clientKey).toRSAPublicKey());

			if (!jwsObject.verify(verifier)) {
				throw new RuntimeException("Unable to verify JWS");
			}

		} catch (ParseException | JOSEException e) {
			throw new RuntimeException("Bad JWS", e);
		}
	}

}
