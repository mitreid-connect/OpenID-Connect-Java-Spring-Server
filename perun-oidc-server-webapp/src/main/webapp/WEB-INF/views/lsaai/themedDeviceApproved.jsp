<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ls" tagdir="/WEB-INF/tags/lsaai" %>

<ls:header />

    <h1>
        <c:if test="${ approved }"><p>&#x2714; <spring:message code="device_approved_approved"/></p></c:if>
        <c:if test="${ not approved }"><p>&#x2717; <spring:message code="device_approved_rejected"/></p></c:if>
    </h1>
    <p class="mt-2">
        <c:if test="${ approved }">
            <spring:message code="device_approved_text_approved_start"/>${" "}
            <c:if test="${empty client.clientName}"><em><c:out value="${client.clientId}" /></em></c:if>
            <c:if test="${not empty client.clientName}"><em><c:out value="${client.clientName}" /></em></c:if>
            ${" "}<spring:message code="device_approved_text_approved_end"/>
        </c:if>
        <c:if test="${not approved}">
            <spring:message code="device_approved_text_rejected_start"/>
            <c:if test="${empty client.clientName}">
                <em>${" "}<c:out value="${client.clientId}"/></em>
            </c:if>
            <c:if test="${not empty client.clientName}">
                <em>${" "}<c:out value="${client.clientName}"/></em>
            </c:if>
            ${". "}<spring:message code="device_approved_text_rejected_end"/>
        </c:if>
    </p>

<ls:footer />
