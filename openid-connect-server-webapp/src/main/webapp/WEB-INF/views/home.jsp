<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>

<spring:message code="home.title" var="title"/>
<o:header title="${title}" />
<o:topbar pageName="Home" />
<div class="container-fluid main">
	<div class="row-fluid">
		<o:sidebar />
		<div class="span10">
			<div class="hero-unit">
				<o:landingPageWelcome />
			</div>
			<!-- Example row of columns -->
			<div class="row-fluid">
				<div class="span6">
					<o:landingPageAbout />
				</div>
				<div class="span6">
					<o:landingPageContact />
				</div>

			</div>
			<hr>
			<!-- Example row of columns -->
			<div class="row-fluid">
				<div class="span12">
					<o:landingPageStats />
				</div>
			</div>

		</div>
	</div>
</div>
<o:footer />
