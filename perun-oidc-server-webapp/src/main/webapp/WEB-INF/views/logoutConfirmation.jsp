<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="org.springframework.security.core.AuthenticationException"%>
<%@ page import="org.springframework.security.oauth2.common.exceptions.UnapprovedClientAuthenticationException"%>
<%@ page import="org.springframework.security.web.WebAttributes"%>
<%@ taglib prefix="authz" uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:message code="logout.confirmation.title" var="title"/>
<o:header title="${title}"/>
<o:topbar />
<div class="container main">

	<div class="well" style="text-align: center">

		<h1><spring:message code="logout.confirmation.header"/></h1>

		<form action="${ config.issuer }${ config.issuer.endsWith('/') ? '' : '/' }endsession" method="POST">

			<div class="row-fluid">
				<div class="span12">
	                <spring:message code="logout.confirmation.submit" var="authorize_label"/>
	                <spring:message code="logout.confirmation.deny" var="deny_label"/>
	                <div>
						<c:if test="${ not empty client }">
							<!-- display some client information -->
							<spring:message code="logout.confirmation.requested"/>&nbsp;
							<c:choose>
								<c:when test="${empty client.clientName}">
									<em><c:out value="${client.clientId}" /></em>
								</c:when>
								<c:otherwise>
									<em><c:out value="${client.clientName}" /></em>
								</c:otherwise>
							</c:choose>
						</c:if>
	                </div>
	                <div>
	                	<spring:message code="logout.confirmation.explanation" />
	                </div>
					<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
					<input name="approve" value="${authorize_label}" type="submit" class="btn btn-info btn-large" /> 
					&nbsp; 
					<input name="deny" value="${deny_label}" type="submit" class="btn btn-large" />
				</div>
			</div>

		</form>

	</div>
</div>
<o:footer/>
