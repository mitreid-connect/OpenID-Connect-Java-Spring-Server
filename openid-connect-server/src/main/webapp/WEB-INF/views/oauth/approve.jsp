<%@ page import="org.springframework.security.core.AuthenticationException" %>
<%@ page import="org.springframework.security.oauth2.common.exceptions.UnapprovedClientAuthenticationException" %>
<%@ page import="org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter" %>
<%@ taglib prefix="authz" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags" %>

<o:header title="Approve Access"/>
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
                        <c:when test="${empty client.applicationName}">
                            <c:out value="${client.clientId}"/>
                        </c:when>
                        <c:otherwise>
                            <c:out value="${client.applicationName}"/>
                        </c:otherwise>
                    </c:choose>" to sign you into their site
                    using your identity?
                    <div>
                    	<a class="small" href="#" onclick="$('#description').toggle('fast'); return false;">more information</a>
					</div>
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
                    <div>
			            <small>
			            	<strong>Redirect URI: </strong><c:out value="${redirect_uri}"/>
			            </small>
					</div>
                </div>
                <div class="span4">
                    <fieldset style="text-align:left" class="well">
                        <legend style="margin-bottom: 0;">Access to:</legend>
                        
                        <input type="hidden" name="scope_openid" id="scope_openid" value="openid"/>
                        <input type="checkbox" name="scope_profile" id="scope_profile" value="profile" checked="checked"><label for="scope_profile">basic profile information</label>
                        
                        <input type="checkbox" name="scope_email" id="scope_email" value="email" checked="checked"><label for="scope_email">email address</label>
                        
                        <input type="checkbox" name="scope_address" id="scope_address" value="address" checked="checked"><label for="scope_address">address</label>
                        
                        <input type="checkbox" name="scope_phone" id="scope_phone" value="phone" checked="checked"><label for="scope_phone">phone number</label>
                        
                        <input type="checkbox" name="scope_offline" id="scope_offline" value="offline" checked="checked"><label for="scope_offline">offline access</label>
                    
                    	<input type="checkbox" name="remember" id="remember" value="true" checked="checked"><label for="remember">remember this decision</label>
                    
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
<o:footer loadapp="false"/>