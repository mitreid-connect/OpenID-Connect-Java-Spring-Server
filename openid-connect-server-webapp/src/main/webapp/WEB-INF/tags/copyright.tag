<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:if test="${ config.heartMode }"><span class="pull-left"><img src="resources/images/heart_mode.png" alt="HEART Mode" title="This server is running in HEART Compliance Mode" /></span> </c:if>
<spring:message code="copyright" arguments="${project.version}"/>
