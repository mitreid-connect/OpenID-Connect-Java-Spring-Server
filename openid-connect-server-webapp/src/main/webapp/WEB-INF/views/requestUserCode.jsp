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

		<h1><spring:message code="device.request_code.header"/></h1>

	<c:if test="${ error != null }">
		<c:choose>
			<c:when test="${ error == 'noUserCode' }">
				<div class="alert alert-error"><spring:message code="device.error.noUserCode"/></div>
			</c:when>
			<c:when test="${ error == 'expiredUserCode' }">
				<div class="alert alert-error"><spring:message code="device.error.expiredUserCode"/></div>
			</c:when>
			<c:when test="${ error == 'userCodeAlreadyApproved' }">
				<div class="alert alert-error"><spring:message code="device.error.userCodeAlreadyApproved"/></div>
			</c:when>
			<c:when test="${ error == 'userCodeMismatch' }">
				<div class="alert alert-error"><spring:message code="device.error.userCodeMismatch"/></div>
			</c:when>
			<c:otherwise>
				<div class="alert alert-error"><spring:message code="device.error.error"/></div>	
			</c:otherwise>
		</c:choose>				
	</c:if>


		<form action="${ config.issuer }${ config.issuer.endsWith('/') ? '' : '/' }device/verify" method="POST">

			<div class="row-fluid">
				<div class="span12">
	                <spring:message code="device.request_code.submit" var="authorize_label"/>
	                <div>
		                <div class="input-block-level input-xlarge">
			                <input type="text" name="user_code" placeholder="code" autocorrect="off" autocapitalize="off" autocomplete="off" spellcheck="false" value="" />
		                </div>
	                </div>
					<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
					<input name="approve" value="${authorize_label}" type="submit" class="btn btn-info btn-large" /> 
				</div>
			</div>

		</form>

	</div>
</div>
<o:footer/>
