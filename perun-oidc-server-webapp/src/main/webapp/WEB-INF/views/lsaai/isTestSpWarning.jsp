<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ls" tagdir="/WEB-INF/tags/lsaai" %>

<ls:header />

    <div id="head">
        <h1><spring:message code="is_test_sp_warning_header"/></h1>
    </div>
    <p><spring:message code="is_test_sp_warning_text"/></p>

    <form method="GET" action="${action}">
        <hr/>
        <br/>
        <input type="hidden" name="target" value="${fn:escapeXml(target)}">
        <input type="hidden" name="accepted" value="true">
        <spring:message code="is_test_sp_warning_continue" var="submit_value"/>
        <input type="submit" name="continue" value="${submit_value}" class="btn btn-lg btn-primary btn-block">
    </form>
<ls:footer />
