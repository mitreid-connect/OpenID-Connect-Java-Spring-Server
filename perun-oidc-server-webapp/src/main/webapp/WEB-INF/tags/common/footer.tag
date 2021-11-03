<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="elixir" tagdir="/WEB-INF/tags/elixir" %>
<%@ taglib prefix="cesnet" tagdir="/WEB-INF/tags/cesnet" %>
<%@ taglib prefix="bbmri" tagdir="/WEB-INF/tags/bbmri" %>
<%@ taglib prefix="ceitec" tagdir="/WEB-INF/tags/ceitec" %>
<%@ taglib prefix="europdx" tagdir="/WEB-INF/tags/europdx" %>
<%@ taglib prefix="muni" tagdir="/WEB-INF/tags/muni" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/common" %>
<%@ attribute name="baseURL" required="true" %>
<%@ attribute name="theme" required="true" %>

<c:choose>
    <c:when test="${theme eq 'elixir'}">
        <elixir:footer baseURL="${baseURL}" samlResourcesURL="${samlResourcesURL}"/>
    </c:when>
    <c:when test="${theme eq 'cesnet'}">
        <cesnet:footer baseURL="${baseURL}" samlResourcesURL="${samlResourcesURL}"/>
    </c:when>
    <c:when test="${theme eq 'bbmri'}">
        <bbmri:footer baseURL="${baseURL}" samlResourcesURL="${samlResourcesURL}"/>
    </c:when>
    <c:when test="${theme eq 'ceitec'}">
        <ceitec:footer baseURL="${baseURL}" samlResourcesURL="${samlResourcesURL}"/>
    </c:when>
    <c:when test="${theme eq 'europdx'}">
        <europdx:footer baseURL="${baseURL}" samlResourcesURL="${samlResourcesURL}"/>
    </c:when>
    <c:when test="${theme eq 'muni'}">
        <muni:footer/>
    </c:when>
    <c:otherwise>
        <o:footer />
    </c:otherwise>
</c:choose>

<script type="text/javascript" src="resources/js/jquery-3-3-1.min.js"></script>
