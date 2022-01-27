package cz.muni.ics.openid.connect.web.endpoint;

import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@Slf4j
public class AuthorizationEndpoint {

    @RequestMapping(value = "/authorize")
    public RedirectView authorize(HttpServletRequest req) {
        log.debug("Handling authorize in endpoint");
        RedirectView view = new RedirectView("/auth/authorize?" + req.getQueryString());
        view.setContextRelative(true);
        view.setAttributesMap(req.getParameterMap());
        log.debug("AUTH_ENDPOINT: Redirecting to: {}", view);
        return view;
    }

}
