<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ls" tagdir="/WEB-INF/tags/lsaai" %>

<ls:header />

    <div id="head">
        <h1><spring:message code="login_failure_header"/></h1>
    </div>
    <div class="msg"><spring:message code="login_failure_msg"/></div>
    <c:if test="${not empty('error_msg')}">
        <div class="mgs">
            <spring:message code="${error_msg}"/>
        </div>
    </c:if>
    <div class="msg"><spring:message code="login_failure_contact_us"/>${" "}
        <a href="mailto:${contactMail}">${contactMail}</a>.
    </div>

<ls:footer />
