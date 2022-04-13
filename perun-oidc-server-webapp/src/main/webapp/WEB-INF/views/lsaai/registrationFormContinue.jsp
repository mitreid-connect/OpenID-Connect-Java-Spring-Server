<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ls" tagdir="/WEB-INF/tags/lsaai" %>

<ls:header />

    <div id="head">
        <h1><spring:message code="go_to_registration_header1"/>
            <c:choose>
                <c:when test="${not empty client.clientName and not empty client.clientUri}">
                    ${" "}<a href="${fn:escapeXml(client.uri)}">${fn:escapeXml(client.clientName)}</a>
                </c:when>
                <c:when test="${not empty client.clientName}">
                    ${" "}${fn:escapeXml(client.clientName)}
                </c:when>
            </c:choose>
            ${" "}<spring:message code="go_to_registration_header2"/>
        </h1>
    </div>
    <form method="GET" action="${action}">
        <hr/>
        <br/>
        <input type="hidden" name="client_id" value="${fn:escapeXml(client_id)}" />
        <input type="hidden" name="facility_id" value="${fn:escapeXml(facility_id)}" />
        <input type="hidden" name="user_id" value="${fn:escapeXml(user_id)}" />
        <spring:message code="go_to_registration_continue" var="submit_value"/>
        <input type="submit" name="continueToRegistration" value="${submit_value}"
               class="btn btn-lg btn-primary btn-block">
    </form>


<ls:footer/>