<%@ page import="org.springframework.security.core.AuthenticationException" %>
<%@ page import="org.springframework.security.oauth2.provider.verification.BasicUserApprovalFilter" %>
<%@ page import="org.springframework.security.oauth2.provider.verification.VerificationCodeFilter" %>
<%@ page import="org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter" %>
<%@ taglib prefix="authz" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags" %>

<o:header title="approve access"/>
<o:topbar/>
<div class="container">
    <div class="content">
        <c:if test="${!empty sessionScope.SPRING_SECURITY_LAST_EXCEPTION}">
            <div class="alert-message error">
                <a href="#" class="close">&times;</a>

                <p><strong>Access could not be granted.</strong>
                    (<%= ((AuthenticationException) session.getAttribute(AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY)).getMessage() %>
                    )</p>
            </div>
        </c:if>
        <c:remove scope="session" var="SPRING_SECURITY_LAST_EXCEPTION"/>

        <authz:authorize ifAllGranted="ROLE_USER">

            <div class="hero-unit" style="text-align:center">
                <h1>Please Confirm!</h1>

                <p>I hereby authorize "<c:out value="${client.clientId}"/>" to access my protected resources.</p>

                <p>

                <form id="confirmationForm" name="confirmationForm"
                      action="<%=request.getContextPath() + VerificationCodeFilter.DEFAULT_PROCESSING_URL%>"
                      method="post">
                    <input name="<%=BasicUserApprovalFilter.DEFAULT_APPROVAL_REQUEST_PARAMETER%>"
                           value="<%=BasicUserApprovalFilter.DEFAULT_APPROVAL_PARAMETER_VALUE%>" type="hidden"/>
                    <input name="authorize" value="Authorize" type="submit" class="btn success large"/>
                </form>
                &nbsp;
                <form id="denialForm" name="denialForm"
                      action="<%=request.getContextPath() + VerificationCodeFilter.DEFAULT_PROCESSING_URL%>"
                      method="post">
                    <input name="<%=BasicUserApprovalFilter.DEFAULT_APPROVAL_REQUEST_PARAMETER%>"
                           value="not_<%=BasicUserApprovalFilter.DEFAULT_APPROVAL_PARAMETER_VALUE%>" type="hidden"/>
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