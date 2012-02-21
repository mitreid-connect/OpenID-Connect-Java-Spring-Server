package org.mitre.openid.connect.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Michael Jett <mjett@mitre.org>
 */

@Controller
public class ManagerController {

    @RequestMapping({"/", "/home", "/index"})
    public String showHomePage() {
        return "home";
    }
}
