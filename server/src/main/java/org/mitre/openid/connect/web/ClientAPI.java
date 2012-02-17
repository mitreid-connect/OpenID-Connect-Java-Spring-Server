package org.mitre.openid.connect.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Michael Jett <mjett@mitre.org>
 */

@Controller
@RequestMapping("/manager/clients/api")
public class ClientAPI {

    /**
     * constructor
     */
    public ClientAPI() {

    }

    /**
     *
     * @param modelAndView
     * @param clientId
     * @param clientSecret
     * @param scope
     * @param grantTypes
     * @param redirectUri
     * @param authorities
     * @param name
     * @param description
     * @param allowRefresh
     * @param accessTokenTimeout
     * @param refreshTokenTimeout
     * @param owner
     * @return
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping("/add")
    public ModelAndView apiAddClient(ModelAndView modelAndView,
                                     @RequestParam String clientId, @RequestParam String clientSecret,
                                     @RequestParam String scope, // space delimited
                                     @RequestParam String grantTypes, // space delimited
                                     @RequestParam(required = false) String redirectUri,
                                     @RequestParam String authorities, // space delimited
                                     @RequestParam(required = false) String name,
                                     @RequestParam(required = false) String description,
                                     @RequestParam(required = false, defaultValue = "false") boolean allowRefresh,
                                     @RequestParam(required = false) Long accessTokenTimeout,
                                     @RequestParam(required = false) Long refreshTokenTimeout,
                                     @RequestParam(required = false) String owner
    ) {
        return null;
    }

    /**
     *
     * @param modelAndView
     * @param clientId
     * @return
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping("/delete")
    public ModelAndView apiDeleteClient(ModelAndView modelAndView,
                                        @RequestParam String clientId) {
        return null;
    }

    /**
     *
     * @param modelAndView
     * @return
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping("/getAll")
    public ModelAndView apiGetAllClients(ModelAndView modelAndView) {
        return null;
    }

    /**
     *
     * @param modelAndView
     * @param clientId
     * @param clientSecret
     * @param scope
     * @param grantTypes
     * @param redirectUri
     * @param authorities
     * @param name
     * @param description
     * @param allowRefresh
     * @param accessTokenTimeout
     * @param refreshTokenTimeout
     * @param owner
     * @return
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping("/update")
    public ModelAndView apiUpdateClient(ModelAndView modelAndView,
                                        @RequestParam String clientId, @RequestParam String clientSecret,
                                        @RequestParam String scope, // space delimited
                                        @RequestParam String grantTypes, // space delimited
                                        @RequestParam(required = false) String redirectUri,
                                        @RequestParam String authorities, // space delimited
                                        @RequestParam(required = false) String name,
                                        @RequestParam(required = false) String description,
                                        @RequestParam(required = false, defaultValue = "false") boolean allowRefresh,
                                        @RequestParam(required = false) Long accessTokenTimeout,
                                        @RequestParam(required = false) Long refreshTokenTimeout,
                                        @RequestParam(required = false) String owner
    ) {
        return null;
    }
}
