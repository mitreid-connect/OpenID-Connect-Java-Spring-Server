<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="authz" uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>

<spring:message code="logout.post.title" var="title"/>
<o:header title="${title}"/>
<o:topbar />
<div class="container main">

	<div class="well" style="text-align: center">
		<h1><spring:message code="logout.post.header" /></h1>

		<security:authorize access="hasRole('ROLE_USER')">
			<div class=""><spring:message code="logout.post.notLoggedOut" /></div>
		</security:authorize>
		<security:authorize access="!hasRole('ROLE_USER')">
			<div class=""><spring:message code="logout.post.loggedOut" /></div>
		</security:authorize>
	</div>
</div>
<o:footer/>
