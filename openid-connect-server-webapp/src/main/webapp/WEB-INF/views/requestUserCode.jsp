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

<spring:message code="device.request_code.title" var="title"/>
<o:header title="${title}"/>
<o:topbar pageName="Approve" />
<div class="container main">

	<div class="well" style="text-align: center">
		<h1><spring:message code="device.request_code.header"/>&nbsp;
			<c:choose>
				<c:when test="${empty client.clientName}">
					<em><c:out value="${client.clientId}" /></em>
				</c:when>
				<c:otherwise>
					<em><c:out value="${client.clientName}" /></em>
				</c:otherwise>
			</c:choose>

		</h1>

		<form action="${ config.issuer }${ config.issuer.endsWith('/') ? '' : '/' }device-user/verify" method="POST">

			<div class="row">
				<div class="span12">
	                <spring:message code="approve.label.authorize" var="authorize_label"/>
	                <spring:message code="approve.label.deny" var="deny_label"/>
	                <input type="text" name="user_code" />
					<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
					<input name="approve" value="${authorize_label}" type="submit" class="btn btn-success btn-large" /> 
					&nbsp; 
					<input name="deny" value="${deny_label}" type="submit" class="btn btn-secondary btn-large" />
				</div>
			</div>

		</form>

	</div>
</div>
<o:footer/>
