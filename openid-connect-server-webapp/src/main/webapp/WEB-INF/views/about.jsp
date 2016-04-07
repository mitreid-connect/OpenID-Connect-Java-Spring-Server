<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<!-- TODO: highlight proper section of topbar; what is the right way to do this? -->
<spring:message code="about.title" var="title"/>
<o:header title="${title}"/>
<o:topbar pageName="About"/>
<div class="container-fluid main">
	<div class="row-fluid">
		<o:sidebar />
		<div class="span10">
			<!-- Main hero unit for a primary marketing message or call to action -->
			<div class="hero-unit">

				<o:aboutContent />

			</div>


		</div>
	</div>
</div>
<o:footer/>
