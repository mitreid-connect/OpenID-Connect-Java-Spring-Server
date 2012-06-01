<%@ page import="org.springframework.security.core.AuthenticationException" %>
<%@ page import="org.springframework.security.oauth2.common.exceptions.UnapprovedClientAuthenticationException" %>
<%@ page import="org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter" %>
<%@ taglib prefix="authz" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags" %>

<o:header title="approve access"/>
<o:topbar/>
<div class="container">
    <% if (session.getAttribute(AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY) != null && !(session.getAttribute(AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY) instanceof UnapprovedClientAuthenticationException)) { %>
    <div class="alert-message error">
        <a href="#" class="close">&times;</a>

        <p><strong>Access could not be granted.</strong>
            (<%= ((AuthenticationException) session.getAttribute(AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY)).getMessage() %>
            )</p>
    </div>
    <% } %>
    <c:remove scope="session" var="SPRING_SECURITY_LAST_EXCEPTION"/>

    <authz:authorize ifAnyGranted="ROLE_USER">

    <div class="hero-unit" style="text-align:center">
        <h1>Please Confirm!</h1>

        <p>I hereby authorize "<c:out value="${client.clientId}"/>" to access my protected resources.</p>



        <form name="confirmationForm" style="display:inline" action="<%=request.getContextPath()%>/oauth/authorize" method="post">

            <div class="row">
                <input id="user_oauth_approval" name="user_oauth_approval" value="true" type="hidden"/>
                <input name="authorize" value="Authorize" type="submit"
                       onclick="$('#user_oauth_approval').attr('value',true)" class="btn btn-success btn-large"/>

                &nbsp;
                <input name="deny" value="Deny" type="submit" onclick="$('#user_oauth_approval').attr('value',false)"
                       class="btn btn-secondary btn-large"/>
            </div>
            <div class="row control-group">
                <label for="option1"></label>
                <input name="option1" id="option1" type="checkbox"> Check me out

                <label for="option2"></label>
                <input name="option2" id="option2" type="checkbox"> Check me out

            </div>
        </form>
        <div>
        <a href="#" class="small">learn more</a>
        </div>

        </authz:authorize>

        <o:copyright/>
    </div>
</div>
<o:footer/>