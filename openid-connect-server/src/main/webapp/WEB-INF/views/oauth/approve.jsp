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

    <div class="well" style="text-align:center">
        <h1>Approve New Site</h1>

        <form name="confirmationForm" style="display:inline" action="<%=request.getContextPath()%>/oauth/authorize"
              method="post">
            <div class="row">
                <div class="span4 offset2 well-small" style="text-align:left">Do you authorize
                    "<c:choose>
                        <c:when test="${empty client.clientName}">
                            <c:out value="${client.clientId}"/>
                        </c:when>
                        <c:otherwise>
                            <c:out value="${client.clientName}"/>
                        </c:otherwise>
                    </c:choose>" to sign you into their site
                    using your identity?
                    <a class="small" href="#" onclick="$('#description').toggle('fast')">more information</a>

                    <p>
                    <blockquote id="description" style="display: none">
                        <c:choose>
                            <c:when test="${empty client.clientDescription}">
                                No additional information available.
                            </c:when>
                            <c:otherwise>
                                <c:out value="${client.clientDescription}"/>
                            </c:otherwise>
                        </c:choose>

                    </blockquote>
                    </p>
                </div>
                <div class="span4">
                    <fieldset style="text-align:left" class="well">
                        <legend style="margin-bottom: 0;">Access to:</legend>
                        <label for="option1"></label>
                        <input type="checkbox" name="option1" id="option1" checked="checked"> basic profile information
                        <label for="option2"></label>
                        <input type="checkbox" name="option1" id="option2" checked="checked"> email address
                        <label for="option3"></label>
                        <input type="checkbox" name="option3" id="option3" checked="checked"> address
                        <label for="option4"></label>
                        <input type="checkbox" name="option4" id="option4" checked="checked"> phone number
                        <label for="option5"></label>
                        <input type="checkbox" name="option5" id="option5" checked="checked"> offline access
                    </fieldset>
                </div>

            </div>


            <div class="row">
                <input id="user_oauth_approval" name="user_oauth_approval" value="true" type="hidden"/>
                <input name="authorize" value="Authorize" type="submit"
                       onclick="$('#user_oauth_approval').attr('value',true)" class="btn btn-success btn-large"/>

                &nbsp;
                <input name="deny" value="Deny" type="submit" onclick="$('#user_oauth_approval').attr('value',false)"
                       class="btn btn-secondary btn-large"/>
            </div>

        </form>

        </authz:authorize>

        <o:copyright/>
    </div>
</div>
<o:footer/>