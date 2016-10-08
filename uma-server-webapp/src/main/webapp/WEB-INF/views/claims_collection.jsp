<%@page import="org.springframework.http.HttpStatus"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>
<%@page import="org.springframework.security.oauth2.common.exceptions.OAuth2Exception"%>
<spring:message code="claims.title" var="title"/>
<o:header title="${title}" />
<o:topbar pageName="Claims" />
<div class="container-fluid main">
	<div class="row-fluid">
		<div class="offset1 span10">
			<div class="hero-unit">

			<h1>The client
			<c:choose>
				<c:when test="${empty client.clientName}">
					<em><c:out value="${client.clientId}" /></em>
				</c:when>
				<c:otherwise>
					<em><c:out value="${client.clientName}" /></em>
				</c:otherwise>
			</c:choose>
			is requesting access to the resource set
			<c:choose>
				<c:when test="${empty resourceSet.name}">
					<em><c:out value="${resourceSet.id}" /></em>
				</c:when>
				<c:otherwise>
					<em><c:out value="${resourceSet.name}" /></em>
				</c:otherwise>
			</c:choose>.					
			</h1>
			
			<p>This system requires that you identify yourself before the process can continue.</p>
				
			<div>So far, you have provided the following claims:
				<ul>			
				<c:if test="${empty claims}">
					<li><b>NONE</b></li>
				</c:if>
				<c:forEach items="${ claims }" var="claim">
					<li>
					<b><c:out value="${claim.name}" /></b>: <i><c:out value="${claim.value}" /></i>
					<small>(<c:out value="${claim.issuer}" />)</small>
					</li>
				</c:forEach>
				</ul>
			</div>

			<p>Enter your email address to log in with OpenID Connect</p> 
			
			<div class="well">
				<div class="row-fluid">
	
					<div class="span8">
						<form action="openid_connect_login" method="get">
							<input type="text" class="input-xxlarge" name="identifier" id="identifier" />
							<input type="hidden" name="target_link_uri" value="rqp_claims/collect" />
							<input type="submit" value="Log In" />
						</form>
					</div>
				</div>
			</div>
			
			<form action="rqp_claims" method="POST">
				<input type="submit" value="Return to Client" class="btn" />
			</form>

			</div>

		</div>
	</div>
</div>
<o:footer />
