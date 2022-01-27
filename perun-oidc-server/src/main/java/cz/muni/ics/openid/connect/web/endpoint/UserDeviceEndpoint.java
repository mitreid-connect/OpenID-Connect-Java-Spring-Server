package cz.muni.ics.openid.connect.web.endpoint;

import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@Slf4j
public class UserDeviceEndpoint {

    @RequestMapping(value = "/device")
    public RedirectView authorize(HttpServletRequest req) {
        String redirect = "/auth/device" + (StringUtils.hasText(req.getQueryString()) ? '?' + req.getQueryString() : "");
        RedirectView view = new RedirectView(redirect);
        view.setContextRelative(true);
        log.debug("DEVICE_ENDPOINT: Redirecting to: {}", view);
        return view;
    }
}
