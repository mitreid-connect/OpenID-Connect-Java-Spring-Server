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

        <form name="confirmationForm" style="display:inline" action="<%=request.getContextPath()%>/authorize" method="post">

            <div class="row">
                <div class="span4 offset2 well-small" style="text-align:left">
                <c:choose>
            		<c:when test="${empty client.logoUrl }">
            		</c:when>
            		<c:otherwise>
            			<ul class="thumbnails">
            				<li class="span4">
            					<div class="thumbnail"><img src="${client.logoUrl }"/></div>
            				</li>
            			</ul>
            		</c:otherwise>
            	</c:choose>
                Do you authorize
                    "<c:choose>
                        <c:when test="${empty client.clientName}">
                            <c:out value="${client.clientId}"/>
                        </c:when>
                        <c:otherwise>
                            <c:out value="${client.clientName}"/>
                        </c:otherwise>
                    </c:choose>" 
                to sign you into their site using your identity?
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
                        
						<c:if test="${not empty scopes['profile']}">
							<label for="scope_profile" class="checkbox">
	                        	<input type="checkbox" name="scope_profile" id="scope_profile" value="profile" checked="checked"> 
	                        	<i class="icon-list-alt"></i> basic profile information
                        	</label>
						</c:if>
						                       
						<c:if test="${not empty scopes['email']}">
							<label for="scope_email" class="checkbox">
	                        	<input type="checkbox" name="scope_email" id="scope_email" value="email" checked="checked"> 
	                        	<i class="icon-envelope"></i> email address
                        	</label>
						</c:if>
                        
						<c:if test="${not empty scopes['address']}">
							<label for="scope_address" class="checkbox">
	                        	<input type="checkbox" name="scope_address" id="scope_address" value="address" checked="checked"> 
	                        	<i class="icon-home"></i> address
                        	</label>
						</c:if>
                        
						<c:if test="${not empty scopes['phone']}">
                        	<label for="scope_phone" class="checkbox">
	                        	<input type="checkbox" name="scope_phone" id="scope_phone" value="phone" checked="checked">
	                        	<i class="icon-bell"></i> phone number
                        	</label>
						</c:if>
                        
						<c:if test="${not empty scopes['offline']}">
                        	<label for="scope_offline" class="checkbox">
	                        	<input type="checkbox" name="scope_offline" id="scope_offline" value="offline" checked="checked"> 
	                        	<i class="icon-time"></i> offline access
                        	</label>
						</c:if>

                        </fieldset>

                   <fieldset style="text-align:left" class="well">
                        <legend style="margin-bottom: 0;">Remember this decision:</legend>
                    	<label for="remember-forever" class="radio">
                    		<input type="radio" name="remember" id="remember-forever" value="until-revoked" checked="checked">
                    		remember this decision until I revoke it
                    	</label>
                    	<label for="remember-hour" class="radio">
                    		<input type="radio" name="remember" id="remember-hour" value="one-hour">
                    		remember this decision for one hour
                    	</label>
                    	<label for="remember-not" class="radio">
                    		<input type="radio" name="remember" id="remember-not" value="none">
                    		prompt me again next time
                    	</label>
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