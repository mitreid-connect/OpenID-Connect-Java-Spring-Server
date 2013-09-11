<%@ page import="org.springframework.security.core.AuthenticationException"%>
<%@ page import="org.springframework.security.oauth2.common.exceptions.UnapprovedClientAuthenticationException"%>
<%@ page import="org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter"%>
<%@ taglib prefix="authz" uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags"%>
<o:header title="Approve Access" />
<o:topbar />
<div class="container main">
	<% if (session.getAttribute(AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY) != null && !(session.getAttribute(AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY) instanceof UnapprovedClientAuthenticationException)) { %>
	<div class="alert-message error">
		<a href="#" class="close">&times;</a>

		<p><strong>Access could not be granted.</strong> 
			(<%= ((AuthenticationException) session.getAttribute(AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY)).getMessage() %>)
		</p>
	</div>
	<% } %>
	<c:remove scope="session" var="SPRING_SECURITY_LAST_EXCEPTION" />

	<div class="well" style="text-align: center">
		<h1>Approve New Site</h1>

		<form name="confirmationForm"
			action="<%=request.getContextPath()%>/authorize" method="post">

			<div class="row">
				<div class="span4 offset2 well-small" style="text-align: left">

					<%-- TODO: wire up to stats engine and customize display of this block --%>
					<c:if test="${ client.dynamicallyRegistered }">
						<div class="alert alert-block alert-info">
							<h4>
								<i class="icon-globe"></i> Caution:
							</h4>
							This client was dynamically registered and has very few other
							users on this system.
						</div>
					</c:if>

					<c:if test="${ not empty client.logoUri }">
						<ul class="thumbnails">
							<li class="span4">
								<div class="thumbnail">
									<img src="${client.logoUri }" />
								</div>
							</li>
						</ul>
					</c:if>
					Do you authorize 
					"<c:choose>
						<c:when test="${empty client.clientName}">
							<c:out value="${client.clientId}" />
						</c:when>
						<c:otherwise>
							<c:out value="${client.clientName}" />
						</c:otherwise>
					</c:choose>"
					to sign you into their site using your identity?
					<c:if test="${not empty client.clientDescription}">
						<div>
							<a class="small" href="#"onclick="$('#description').toggle('fast'); return false;"><i class="icon-chevron-right"></i> more information</a>
						</div>
						<p>
							<blockquote id="description" style="display: none">
								${client.clientDescription}
							</blockquote>
						</p>
					</c:if>
					<div>
						<small> 
							<strong>Redirect URI: </strong><c:out value="${redirect_uri}" />
						</small>
					</div>

					<c:if test="${ client.subjectType == 'PAIRWISE' }">
						<div class="alert alert-success">
							This client uses a <b>pairwise</b> identifier, which makes it more difficult to correlate your identity between sites.
						</div>
					</c:if>

				</div>
				<div class="span4">
					<fieldset style="text-align: left" class="well">
						<legend style="margin-bottom: 0;">Access to:</legend>

						<c:forEach var="scope" items="${ scopes }">

							<label for="scope_${ scope.value }" class="checkbox"> 
								<input type="checkbox" name="scope_${ scope.value }" id="scope_${ scope.value }" value="${ scope.value }" checked="checked"> 
								<c:if test="${ not empty scope.icon }">
									<i class="icon-${ scope.icon }"></i>
								</c:if> 
								<c:choose>
									<c:when test="${ not empty scope.description }">
										${ scope.description }
									</c:when>
									<c:otherwise>
										${ scope.value }
									</c:otherwise>
								</c:choose>
								
							</label>
								<c:if test="${ scope.structured }">
									<input name="scopeparam_${ scope.value }" type="text" value="${ scope.structuredValue }" placeholder="${scope.structuredParamDescription}">
								</c:if>

						</c:forEach>

					</fieldset>

					<fieldset style="text-align: left" class="well">
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
				<input id="user_oauth_approval" name="user_oauth_approval" value="true" type="hidden" /> 
					<input name="authorize" value="Authorize" type="submit"
					onclick="$('#user_oauth_approval').attr('value',true)" class="btn btn-success btn-large" /> 
					&nbsp; 
					<input name="deny" value="Deny" type="submit" onclick="$('#user_oauth_approval').attr('value',false)"
					class="btn btn-secondary btn-large" />
			</div>

		</form>

	</div>
</div>
<o:footer/>
