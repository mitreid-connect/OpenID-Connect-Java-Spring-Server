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
		<h1>Approval Required for 
			<c:choose>
				<c:when test="${empty client.clientName}">
					<em><c:out value="${client.clientId}" /></em>
				</c:when>
				<c:otherwise>
					<em><c:out value="${client.clientName}" /></em>
				</c:otherwise>
			</c:choose>
		</h1>

		<form name="confirmationForm"
			action="<%=request.getContextPath()%>/authorize" method="post">

			<div class="row">
				<div class="span5 offset1 well-small" style="text-align: left">

					<%-- TODO: wire up to stats engine and customize display of this block --%>
					<c:if test="${ client.dynamicallyRegistered }">
						<div class="alert alert-block alert-info">
							<h4>
								<i class="icon-globe"></i> Caution:
							</h4>
							This software was dynamically registered and has been used by
							<span class="label"><c:out value="${ count }" /></span>
							users.
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
					<div>
						${client.clientDescription}
					</div>
					<c:if test="${ (not empty client.clientUri) || (not empty client.policyUri) || (not empty client.tosUri) }">
						<div>
							<a id="toggleMoreInformation" class="small" href="#"><i class="icon-chevron-right"></i> more information</a>
						</div>
						<div id="moreInformation" class="hide">
							<ul>
								<c:if test="${ not empty client.clientUri }">
									<li>Home page: <a href="<c:out value="${ client.clientUri }" />"><c:out value="${ client.clientUri }" /></a>
								</c:if>
							</ul>
						</div>
					</c:if>
					<div>
						<c:choose>
							<c:when test="${ empty client.redirectUris }">
								<div class="alert alert-block alert-error">
									<h4>
										<i class="icon-info-sign"></i> Warning:
									</h4>
									This client does not have any redirect URIs registered and could be using a 
									malicious URI. You will be redirected to the following page if you click Approve:
									<code><c:out value="${redirect_uri}" /></code>
								</div>
							</c:when>
							<c:otherwise>
								<small> 
									<strong>You will be redirected to the following page
									if you click Approve: </strong><code><c:out value="${redirect_uri}" /></code>
								</small>							
							</c:otherwise>
						</c:choose>
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

						<c:if test="${ empty client.scope }">
								<div class="alert alert-block alert-error">
									<h4>
										<i class="icon-info-sign"></i> Warning:
									</h4>
									This client does not have any scopes registered and is therefore allowed to
									request <em>any</em> scopes available on the system. Proceed with caution.
								</div>
						</c:if>

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
								
								<c:if test="${ not empty claims[scope.value] }">
									<span class="claim-tooltip" data-toggle="popover"
										data-html="true"
										data-placement="right"
										data-trigger="hover"
										data-title="Claim values:"
										data-content="<div style=&quot;text-align: left;&quot;>
											<ul>
											<c:forEach var="claim" items="${ claims[scope.value] }">
												<li>
												<b><c:out value="${ claim.key }" /></b>: 
												<c:out value="${ claim.value }" />
												</li>
											</c:forEach>
											</ul>
											</div>
										"
									>
										<i class="icon-question-sign"></i>
										
									</span>
								</c:if>
								
								<c:if test="${ scope.structured }">
									<input name="scopeparam_${ scope.value }" type="text" value="${ scope.structuredValue }" placeholder="${scope.structuredParamDescription}">
								</c:if>
								
							</label>

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
			<h3>
					Do you authorize 
					"<c:choose>
						<c:when test="${empty client.clientName}">
							<c:out value="${client.clientId}" />
						</c:when>
						<c:otherwise>
							<c:out value="${client.clientName}" />
						</c:otherwise>
					</c:choose>"?
			</h3>
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
<script type="text/javascript">
<!--

$(document).ready(function() {
		$('.claim-tooltip').popover();
		
		$('#toggleMoreInformation').on('click', function(event) {
			event.preventDefault();
			if ($('#moreInformation').is(':visible')) {
				// hide it
				$('#moreInformation').hide('fast');
				$('#toggleMoreInformation i').attr('class', 'icon-chevron-right');
			} else {
				// show it
				$('#moreInformation').show('fast');
				$('#toggleMoreInformation i').attr('class', 'icon-chevron-down');
			}
		});
		
});

//-->
</script>
<o:footer/>
