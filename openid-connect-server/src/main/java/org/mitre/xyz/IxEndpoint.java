package org.mitre.xyz;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.http.client.utils.URIBuilder;
import org.mitre.oauth2.model.AuthenticationHolderEntity;
import org.mitre.oauth2.model.SystemScope;
import org.mitre.oauth2.service.ClientDetailsEntityService;
import org.mitre.oauth2.service.SystemScopeService;
import org.mitre.openid.connect.model.UserInfo;
import org.mitre.openid.connect.service.ScopeClaimTranslationService;
import org.mitre.openid.connect.service.StatsService;
import org.mitre.openid.connect.service.UserInfoService;
import org.mitre.openid.connect.token.TofuUserApprovalHandler;
import org.mitre.openid.connect.view.HttpCodeView;
import org.mitre.xyz.TxEndpoint.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.endpoint.RedirectResolver;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;

/**
 * @author jricher
 *
 */
@Controller
@RequestMapping("/interact")
public class IxEndpoint {

	@Autowired
	private ClientDetailsEntityService clientService;

	@Autowired
	private SystemScopeService scopeService;

	@Autowired
	private ScopeClaimTranslationService scopeClaimTranslationService;

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private StatsService statsService;

	@Autowired
	private RedirectResolver redirectResolver;

	@Autowired
	private TxService txService;

	@Autowired
	private OAuth2RequestFactory oAuth2RequestFactory;

	@Autowired
	private TofuUserApprovalHandler userApprovalHandler;


	@PreAuthorize("hasRole('ROLE_USER')")
	@RequestMapping(method = RequestMethod.GET, path = "/{id}")
	public String interact(@PathVariable("id") String id, Model m, Authentication auth) {

		TxEntity tx = txService.loadByInteractUrl(id);

		if (tx == null) {
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			return HttpCodeView.VIEWNAME;
		}

		m.addAttribute("client", tx.getClient());

		m.addAttribute("redirect_uri", tx.getCallbackUri());

		Set<SystemScope> scopes = scopeService.fromStrings(tx.getScope());

		Set<SystemScope> sortedScopes = new LinkedHashSet<>(scopes.size());
		Set<SystemScope> systemScopes = scopeService.getAll();

		// sort scopes for display based on the inherent order of system scopes
		for (SystemScope s : systemScopes) {
			if (scopes.contains(s)) {
				sortedScopes.add(s);
			}
		}

		// add in any scopes that aren't system scopes to the end of the list
		sortedScopes.addAll(Sets.difference(scopes, systemScopes));

		m.addAttribute("scopes", sortedScopes);

		// get the userinfo claims for each scope
		UserInfo user = userInfoService.getByUsername(auth.getName());
		Map<String, Map<String, String>> claimsForScopes = new HashMap<>();
		if (user != null) {
			JsonObject userJson = user.toJson();

			for (SystemScope systemScope : sortedScopes) {
				Map<String, String> claimValues = new HashMap<>();

				Set<String> claims = scopeClaimTranslationService.getClaimsForScope(systemScope.getValue());
				for (String claim : claims) {
					if (userJson.has(claim) && userJson.get(claim).isJsonPrimitive()) {
						// TODO: this skips the address claim
						claimValues.put(claim, userJson.get(claim).getAsString());
					}
				}

				claimsForScopes.put(systemScope.getValue(), claimValues);
			}
		}

		m.addAttribute("claims", claimsForScopes);

		// client stats
		Integer count = statsService.getCountForClientId(tx.getClient().getClientId()).getApprovedSiteCount();
		m.addAttribute("count", count);


		// contacts
		if (tx.getClient().getContacts() != null) {
			String contacts = Joiner.on(", ").join(tx.getClient().getContacts());
			m.addAttribute("contacts", contacts);
		}

		// if the client is over a week old and has more than one registration, don't give such a big warning
		// instead, tag as "Generally Recognized As Safe" (gras)
		Date lastWeek = new Date(System.currentTimeMillis() - (60 * 60 * 24 * 7 * 1000));
		if (count > 1 && tx.getClient().getCreatedAt() != null && tx.getClient().getCreatedAt().before(lastWeek)) {
			m.addAttribute("gras", true);
		} else {
			m.addAttribute("gras", false);
		}

		m.addAttribute("form_target", "interact/" + id);

		return "approve";

	}

	@PreAuthorize("hasRole('ROLE_USER')")
	@RequestMapping(method = RequestMethod.POST, path = "/{id}", params = OAuth2Utils.USER_OAUTH_APPROVAL)
	public ModelAndView approveOrDeny(@PathVariable("id") String id,
		@RequestParam Map<String, String> approvalParameters,
		Model m, Authentication auth) {

		TxEntity tx = txService.loadByInteractUrl(id);

		if (tx == null) {
			m.addAttribute(HttpCodeView.CODE, HttpStatus.BAD_REQUEST);
			new ModelAndView(HttpCodeView.VIEWNAME);
		}

		// FIXME: this is using a simplified constructor
		AuthorizationRequest ar = new AuthorizationRequest(tx.getClient().getClientId(), tx.getScope());
		ar.setRedirectUri(tx.getCallbackUri());

		ar.setApprovalParameters(approvalParameters);

		userApprovalHandler.updateAfterApproval(ar, auth);
		boolean approved = userApprovalHandler.isApproved(ar, auth);
		ar.setApproved(approved);

		AuthenticationHolderEntity ah = tx.getAuthenticationHolder();

		OAuth2Authentication o2a = new OAuth2Authentication(ah.getAuthentication().getOAuth2Request(), auth);

		ah.setAuthentication(o2a);
		ah.setApproved(approved);

		tx.setAuthenticationHolder(ah);

		tx.setStatus(approved ? Status.AUTHORIZED : Status.DENIED);

		if (!Strings.isNullOrEmpty(tx.getCallbackUri())) {

			String interactRef = UUID.randomUUID().toString();

			String hash = Hash.CalculateInteractHash(tx.getClientNonce(), tx.getServerNonce(), interactRef, tx.getHashMethod());

			try {
				String redirectTo = new URIBuilder(tx.getCallbackUri())
					.addParameter("interact", interactRef)
					.addParameter("hash", hash).build().toString();

				tx.setInteractionRef(interactRef);

				txService.save(tx);

				return new ModelAndView(new RedirectView(redirectTo));
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}

		} else {
			// no callback, show completion page
			// pre-process the scopes
			Set<SystemScope> scopes = scopeService.fromStrings(tx.getScope());

			Set<SystemScope> sortedScopes = new LinkedHashSet<>(scopes.size());
			Set<SystemScope> systemScopes = scopeService.getAll();

			// sort scopes for display based on the inherent order of system scopes
			for (SystemScope s : systemScopes) {
				if (scopes.contains(s)) {
					sortedScopes.add(s);
				}
			}

			// add in any scopes that aren't system scopes to the end of the list
			sortedScopes.addAll(Sets.difference(scopes, systemScopes));

			m.addAttribute("scopes", sortedScopes);
			m.addAttribute("approved", true);

			txService.save(tx);

			// TODO: we are re-using the device approval page here
			return new ModelAndView("deviceApproved");

		}
	}


}
