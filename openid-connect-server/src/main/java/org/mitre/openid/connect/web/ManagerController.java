package org.mitre.openid.connect.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Michael Jett <mjett@mitre.org>
 */

@Controller
@RequestMapping("/")
public class ManagerController {

    @RequestMapping({"", "/home", "/index"})
    public String showHomePage() {
        return "home";
    }

    @RequestMapping("/admin/manage/clients")
    public String showClientManager() {
        return "admin/manage/clients";
    }

}
