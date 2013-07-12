/**
 * 
 */
package org.mitre.openid.connect.view;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * 
 * Provides a minimal representation of a client's registration information, to be shown from the dynamic registration endpoint
 * on the client_register and rotate_secret operations.
 * 
 * @author jricher
 *
 */
@Component("clientInformationResponseView")
public class ClientInformationResponseView extends AbstractView {

	// note that this won't serialize nulls by default
	private Gson gson = new Gson();

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.view.AbstractView#renderMergedOutputModel(java.util.Map, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {

		response.setContentType("application/json");

		ClientDetailsEntity c = (ClientDetailsEntity) model.get("client");
		OAuth2AccessTokenEntity token = (OAuth2AccessTokenEntity) model.get("token");
		HttpStatus code = (HttpStatus) model.get("code");
		if (code == null) {
			code = HttpStatus.OK;
		}

		JsonObject o = new JsonObject();

		o.addProperty("client_id", c.getClientId());
		if (c.getClientSecret() != null) {
			o.addProperty("client_secret", c.getClientSecret());
			o.addProperty("expires_at", 0); // TODO: do we want to let secrets expire?
		}

		if (c.getCreatedAt() != null) {
			o.addProperty("issued_at", c.getCreatedAt().getTime());
		}

		o.addProperty("registration_access_token", token.getValue());

		// TODO: urlencode the client id for safety?
		String uri = request.getRequestURL() + "/" + c.getClientId();
		o.addProperty("registration_client_uri", uri);


		// add in all other client properties

		// OAuth DynReg
		o.add("redirect_uris", getAsArray(c.getRedirectUris()));
		o.addProperty("client_name", c.getClientName());
		o.addProperty("client_uri", c.getClientUri());
		o.addProperty("logo_uri", c.getLogoUri());
		o.add("contacts", getAsArray(c.getContacts()));
		o.addProperty("tos_uri", c.getTosUri());
		o.addProperty("token_endpoint_auth_method", c.getTokenEndpointAuthMethod() != null ? c.getTokenEndpointAuthMethod().getValue() : null);
		o.addProperty("scope", c.getScope() != null ? Joiner.on(" ").join(c.getScope()) : null);
		o.add("grant_types", getAsArray(c.getGrantTypes()));
		o.addProperty("policy_uri", c.getPolicyUri());
		o.addProperty("jwks_uri", c.getJwksUri());

		// OIDC Registration
		o.addProperty("application_type", c.getApplicationType() != null ? c.getApplicationType().getValue() : null);
		o.addProperty("sector_identifier_uri", c.getSectorIdentifierUri());
		o.addProperty("subject_type", c.getSubjectType() != null ? c.getSubjectType().getValue() : null);
		o.addProperty("request_object_signing_alg", c.getRequestObjectSigningAlg() != null ? c.getRequestObjectSigningAlg().getAlgorithmName() : null);
		o.addProperty("userinfo_signed_response_alg", c.getUserInfoSignedResponseAlg() != null ? c.getUserInfoSignedResponseAlg().getAlgorithmName() : null);
		o.addProperty("userinfo_encrypted_response_alg", c.getUserInfoEncryptedResponseAlg() != null ? c.getUserInfoEncryptedResponseAlg().getAlgorithmName() : null);
		o.addProperty("userinfo_encrypted_response_enc", c.getUserInfoEncryptedResponseEnc() != null ? c.getUserInfoEncryptedResponseEnc().getAlgorithmName() : null);
		o.addProperty("id_token_signed_response_alg", c.getIdTokenSignedResponseAlg() != null ? c.getIdTokenSignedResponseAlg().getAlgorithmName() : null);
		o.addProperty("id_token_encrypted_response_alg", c.getIdTokenEncryptedResponseAlg() != null ? c.getIdTokenEncryptedResponseAlg().getAlgorithmName() : null);
		o.addProperty("id_token_encrypted_response_enc", c.getIdTokenEncryptedResponseEnc() != null ? c.getIdTokenEncryptedResponseEnc().getAlgorithmName() : null);
		o.addProperty("default_max_age", c.getDefaultMaxAge());
		o.addProperty("require_auth_time", c.getRequireAuthTime());
		o.add("default_acr_values", getAsArray(c.getDefaultACRvalues()));
		o.addProperty("initiate_login_uri", c.getInitiateLoginUri());
		o.addProperty("post_logout_redirect_uri", c.getPostLogoutRedirectUri());
		o.add("request_uris", getAsArray(c.getRequestUris()));

		try {
			Writer out = response.getWriter();
			gson.toJson(o, out);
		} catch (JsonIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private JsonElement getAsArray(Set<String> value) {
		return gson.toJsonTree(value, new TypeToken<Set<String>>(){}.getType());
	}

}
