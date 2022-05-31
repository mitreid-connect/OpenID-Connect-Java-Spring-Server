<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ls" tagdir="/WEB-INF/tags/lsaai" %>

<ls:header />

<div class="error_message" style="word-wrap: break-word;">
    <h1><spring:message code="${outHeader}"/></h1>
    <p><spring:message code="${outMessage}"/></p>
    <c:if test="${hasTarget}">
        <form method="POST" action="" class="mb-4">
            <button class="btn btn-primary btn-block"><spring:message code="${outButton}"/></button>
        </form>
    </c:if>
    <p><spring:message code="${outContactP}"/>${" "}<a href="mailto:${contactMail}">${contactMail}</a></p>
</div>

<ls:footer/>
