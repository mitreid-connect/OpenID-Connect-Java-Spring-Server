<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/common" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="baseURL" value="${baseURL}"/>
<c:set var="samlResourcesURL" value="${samlResourcesURL}"/>

<%

List<String> cssLinks = new ArrayList<>();
pageContext.setAttribute("cssLinks", cssLinks);

%>

<spring:message code="device_approved_title" var="title"/>
<t:header title="${title}" reqURL="${reqURL}" baseURL="${baseURL}" cssLinks="${cssLinks}" theme="${theme}"/>

</div> <%-- header --%>

<div id="content" class="text-center">
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
    </p>q
</div>

</div> <%-- wrap --%>

<t:footer baseURL="${baseURL}" theme="${theme}"/>
