<%@ page import="org.springframework.security.core.AuthenticationException" %>
<%@ page import="org.springframework.security.oauth2.common.exceptions.UnapprovedClientAuthenticationException" %>
<%@ page import="org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter" %>
<%@ taglib prefix="authz" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags" %>

<o:header title="approve access"/>
<o:topbar/>
<div class="container">
    <div class="content">
        <% if (session.getAttribute(AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY) != null && !(session.getAttribute(AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY) instanceof UnapprovedClientAuthenticationException)) { %>
        <div class="alert-message error">
            <a href="#" class="close">&times;</a>

            <p><strong>Access could not be granted.</strong>
                (<%= ((AuthenticationException) session.getAttribute(AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY)).getMessage() %>
                )</p>
        </div>
        <% } %>
        <c:remove scope="session" var="SPRING_SECURITY_LAST_EXCEPTION"/>

        <authz:authorize ifAllGranted="ROLE_USER">

            <div class="hero-unit" style="text-align:center">
                <h1>Please Confirm!</h1>

                <p>I hereby authorize "<c:out value="${client.clientId}"/>" to access my protected resources.</p>

                <p>

                <form id="confirmationForm" name="confirmationForm"
                      action="<%=request.getContextPath()%>/oauth/authorize" method="post">
                    <input name="user_oauth_approval" value="true" type="hidden"/>
                    <input name="authorize" value="Authorize" type="submit" class="btn success large"/>
                </form>
                &nbsp;
                <form id="denialForm" name="denialForm" action="<%=request.getContextPath()%>/oauth/authorize"
                      method="post">
                    <input name="user_oauth_approval" value="false" type="hidden"/>
                    <input name="deny" value="Deny" type="submit" class="btn secondary large"/>
                </form>
                </p>
                <a href="#" class="small">learn more</a>

            </div>

        </authz:authorize>

        <o:copyright/>
    </div>
</div>
<o:footer/>