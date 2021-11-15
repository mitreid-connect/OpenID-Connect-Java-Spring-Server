package cz.muni.ics.oidc.saml;

import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_ACR_VALUES;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_FORCE_AUTHN;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PARAM_PROMPT;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PROMPT_LOGIN;
import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.PROMPT_SELECT_ACCOUNT;

import cz.muni.ics.oidc.server.filters.PerunFilterConstants;
import javax.servlet.ServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
public class PerunSamlUtils {

    public static boolean needsReAuthByPrompt(ServletRequest request) {
        String prompt = request.getParameter(PARAM_PROMPT);
        boolean res = (StringUtils.hasText(prompt) && (PROMPT_LOGIN.equalsIgnoreCase(prompt)
            || PROMPT_SELECT_ACCOUNT.equalsIgnoreCase(prompt)));
        log.debug("requires reAuth by prompt - {}", res);
        return res;
    }

    public static boolean needsReAuthByForceAuthn(ServletRequest request) {
        String forceAuthn = request.getParameter(PARAM_FORCE_AUTHN);
        boolean res = (StringUtils.hasText(forceAuthn) && Boolean.parseBoolean(forceAuthn));
        log.debug("requires reAuth by forceAuthn - {}", res);
        return res;
    }

    public static boolean needsReAuthByMfa(ServletRequest request) {
        String acrValues = request.getParameter(PARAM_ACR_VALUES);
        boolean res = StringUtils.hasText(acrValues)
            && acrValues.contains(PerunFilterConstants.REFEDS_MFA);
        log.debug("requires reAuth by MFA acr - {}", res);
        return res;
    }

}
