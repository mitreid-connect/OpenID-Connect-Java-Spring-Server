<%@attribute name="pageName"%>
<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="security"
	uri="http://www.springframework.org/security/tags"%>

<c:choose>
	<c:when test="${pageName == 'Home'}">
		<li class="active"><a href="" data-toggle="collapse" data-target=".nav-collapse"><spring:message code="topbar.home"/></a></li>
	</c:when>
	<c:otherwise>
		<li><a href="" data-toggle="collapse" data-target=".nav-collapse"><spring:message code="topbar.home"/></a></li>
	</c:otherwise>
</c:choose>
<c:choose>
	<c:when test="${pageName == 'About'}">
		<li class="active" data-toggle="collapse" data-target=".nav-collapse"><a href=""><spring:message code="topbar.about"/></a></li>
	</c:when>
	<c:otherwise>
		<li><a href="about" data-toggle="collapse" data-target=".nav-collapse"><spring:message code="topbar.about"/></a></li>
	</c:otherwise>
</c:choose>
<c:choose>
	<c:when test="${pageName == 'Statistics'}">
		<li class="active" data-toggle="collapse" data-target=".nav-collapse"><a href=""><spring:message code="topbar.statistics"/></a></li>
	</c:when>
	<c:otherwise>
		<li><a href="stats" data-toggle="collapse" data-target=".nav-collapse"><spring:message code="topbar.statistics"/></a></li>
	</c:otherwise>
</c:choose>
<c:choose>
	<c:when test="${pageName == 'Contact'}">
		<li class="active" data-toggle="collapse" data-target=".nav-collapse"><a href=""><spring:message code="topbar.contact"/></a></li>
	</c:when>
	<c:otherwise>
		<li><a href="contact" data-toggle="collapse" data-target=".nav-collapse"><spring:message code="topbar.contact"/></a></li>
	</c:otherwise>
</c:choose>
