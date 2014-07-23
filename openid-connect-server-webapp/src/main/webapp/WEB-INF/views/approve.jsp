<%@ page import="org.springframework.security.core.AuthenticationException"%>
<%@ page import="org.springframework.security.oauth2.common.exceptions.UnapprovedClientAuthenticationException"%>
<%@ page import="org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter"%>
<%@ taglib prefix="authz" uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<o:header title="Approve Access" />
<o:topbar pageName="Approve" />
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

					<c:if test="${ client.dynamicallyRegistered }">
						<c:choose>
							<c:when test="${ gras }">
								<!-- client is "generally recognized as safe, display a more muted block -->
								<div><p class="alert alert-info"><i class="icon-globe"></i> This client was dynamically registered <span id="registrationTime"></span>.</p></div>
							</c:when>
							<c:otherwise>
								<!-- client is dynamically registered -->
								<div class="alert alert-block <c:out value="${ count eq 0 ? 'alert-error' : 'alert-warn' }" />">
									<h4>
										<i class="icon-globe"></i> Caution:
									</h4>
									This software was dynamically registered <span id="registrationTime" class="label"></span> 
									and it has been approved
									<span class="label"><c:out value="${ count }" /></span>
									time<c:out value="${ count == 1 ? '' : 's' }"/> previously.
								</div>
							</c:otherwise>
						</c:choose>
					</c:if>

					<c:if test="${ not empty client.logoUri }">
						<ul class="thumbnails">
							<li class="span5">
								<a class="thumbnail" data-toggle="modal" data-target="#logoModal"><img src="${ fn:escapeXml(client.logoUri) }" /></a>
							</li>
						</ul>
						<!-- Modal -->
						<div id="logoModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="logoModalLabel" aria-hidden="true">
							<div class="modal-header">
								<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
								<h3 id="logoModalLabel">
									<c:choose>
										<c:when test="${empty client.clientName}">
											<em><c:out value="${client.clientId}" /></em>
										</c:when>
										<c:otherwise>
											<em><c:out value="${client.clientName}" /></em>
										</c:otherwise>
									</c:choose>
								</h3>
							</div>
							<div class="modal-body">
								<img src="${ fn:escapeXml(client.logoUri) }" />
								<c:if test="${ not empty client.clientUri }">
									<a href="<c:out value="${ client.clientUri }" />"><c:out value="${ client.clientUri }" /></a>
								</c:if>
							</div>
							<div class="modal-footer">
								<button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
							</div>
						</div>
					</c:if>
					<c:if test="${ (not empty client.clientDescription) || (not empty client.clientUri) || (not empty client.policyUri) || (not empty client.tosUri) || (not empty contacts) }">
						<div class="muted moreInformationContainer">
							<c:out value="${client.clientDescription}" />
							<c:if test="${ (not empty client.clientUri) || (not empty client.policyUri) || (not empty client.tosUri)  || (not empty contacts) }">
								<div id="toggleMoreInformation" style="cursor: pointer;">
									<small><i class="icon-chevron-right"></i> more information</small>
								</div>
								<div id="moreInformation" class="hide">
									<ul>
										<c:if test="${ not empty client.clientUri }">
											<li>Home page: <a href="<c:out value="${ client.clientUri }" />"><c:out value="${ client.clientUri }" /></a></li>
										</c:if>
										<c:if test="${ not empty client.policyUri }">
											<li>Policy: <a href="<c:out value="${ client.policyUri }" />"><c:out value="${ client.policyUri }" /></a></li>
										</c:if>
										<c:if test="${ not empty client.tosUri }">
											<li>Terms of Service: <a href="<c:out value="${ client.tosUri }" />"><c:out value="${ client.tosUri }" /></a></li>
										</c:if>
										<c:if test="${ (not empty contacts) }">
											<li>Administrative Contacts: <c:out value="${ contacts }" /></li>
										</c:if>
									</ul>
								</div>
							</c:if>
						</div>
					</c:if>
					<div>
						<c:choose>
							<c:when test="${ empty client.redirectUris }">
								<div class="alert alert-block alert-error">
									<h4>
										<i class="icon-info-sign"></i> Warning:
									</h4>
									This client does not have any redirect URIs registered and someone could be using a 
									malicious URI here. You will be redirected to the following page if you click Approve:
									<code><c:out value="${redirect_uri}" /></code>
								</div>
							</c:when>
							<c:otherwise>
								You will be redirected to the following page
								if you click Approve: <code><c:out value="${redirect_uri}" /></code>
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

							<label for="scope_${ fn:escapeXml(scope.value) }" class="checkbox"> 
								<input type="checkbox" name="scope_${ fn:escapeXml(scope.value) }" id="scope_${ fn:escapeXml(scope.value) }" value="${ fn:escapeXml(scope.value) }" checked="checked"> 
								<c:if test="${ not empty scope.icon }">
									<i class="icon-${ fn:escapeXml(scope.icon) }"></i>
								</c:if> 
								<c:choose>
									<c:when test="${ not empty scope.description }">
										<c:out value="${ scope.description }" />
									</c:when>
									<c:otherwise>
										<c:out value="${ scope.value }" />
									</c:otherwise>
								</c:choose>
								
								<c:if test="${ not empty claims[scope.value] }">
									<span class="claim-tooltip" data-toggle="popover"
										data-html="true"
										data-placement="right"
										data-trigger="hover"
										data-title="These values will be sent:"
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
									<input name="scopeparam_${ fn:escapeXml(scope.value) }" type="text" value="${ fn:escapeXml(scope.structuredValue) }" placeholder="${ fn:escapeXml(scope.structuredParamDescription) }">
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
				<input name="csrf" value="${ csrf }" type="hidden" />
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
		
		$(document).on('click', '#toggleMoreInformation', function(event) {
			event.preventDefault();
			if ($('#moreInformation').is(':visible')) {
				// hide it
				$('.moreInformationContainer', this.el).removeClass('alert').removeClass('alert-info').addClass('muted');
				$('#moreInformation').hide('fast');
				$('#toggleMoreInformation i').attr('class', 'icon-chevron-right');
			} else {
				// show it
				$('.moreInformationContainer', this.el).addClass('alert').addClass('alert-info').removeClass('muted');
				$('#moreInformation').show('fast');
				$('#toggleMoreInformation i').attr('class', 'icon-chevron-down');
			}
		});
		
		var creationDate = "<c:out value="${ client.createdAt }" />";
		var displayCreationDate = "Unknown";
		var hoverCreationDate = "";
		if (creationDate == null || !moment(creationDate).isValid()) {
			displayCreationDate = "Unknown";
			hoverCreationDate = "";
		} else {
			creationDate = moment(creationDate);
			if (moment().diff(creationDate, 'months') < 6) {
				displayCreationDate = creationDate.fromNow();
			} else {
				displayCreationDate = "on " + creationDate.format("MMMM Do, YYYY");
			}
			hoverCreationDate = creationDate.format("MMMM Do, YYYY [at] h:mmA")
		}
		
		$('#registrationTime').html(displayCreationDate);
		$('#registrationTime').attr('title', hoverCreationDate);
});

//-->
</script>
<o:footer/>
