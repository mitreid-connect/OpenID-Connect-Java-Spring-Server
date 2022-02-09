package cz.muni.ics.oidc.saml;

import static cz.muni.ics.oidc.web.controllers.LoginController.ATTR_EXCEPTION;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

public class PerunAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException
    {
        request.setAttribute(ATTR_EXCEPTION, exception);
        super.onAuthenticationFailure(request, response, exception);
    }
}
