package cz.muni.ics.oauth2.web.endpoint;

import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@Slf4j
public class AuthorizationEndpoint {

    public static final String ENDPOINT_INIT_URL = "/authorize";
    public static final String ENDPOINT_URL = "/auth/authorize";

    @RequestMapping(value = ENDPOINT_INIT_URL)
    public RedirectView authorize(HttpServletRequest req) {
        String redirect = ENDPOINT_URL + '?' + req.getQueryString();
        RedirectView view = new RedirectView(redirect);
        view.setContextRelative(true);
        log.debug("Authorization endpoint - {}: user is being redirected to to: {}", ENDPOINT_INIT_URL, redirect);
        return view;
    }

}
