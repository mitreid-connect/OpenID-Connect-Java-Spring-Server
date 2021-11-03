<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="elixir" tagdir="/WEB-INF/tags/elixir" %>
<%@ taglib prefix="cesnet" tagdir="/WEB-INF/tags/cesnet" %>
<%@ taglib prefix="bbmri" tagdir="/WEB-INF/tags/bbmri" %>
<%@ taglib prefix="ceitec" tagdir="/WEB-INF/tags/ceitec" %>
<%@ taglib prefix="europdx" tagdir="/WEB-INF/tags/europdx" %>
<%@ taglib prefix="muni" tagdir="/WEB-INF/tags/muni" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="reqURL" required="true" %>
<%@ attribute name="baseURL" required="true" %>
<%@ attribute name="theme" required="true" %>
<%@ attribute name="cssLinks" required="true" type="java.util.ArrayList<java.lang.String>" %>

<c:choose>
    <c:when test="${theme eq 'elixir'}">
        <elixir:header title="${title}" reqURL="${reqURL}" cssLinks="${cssLinks}" baseURL="${baseURL}" samlResourcesURL="${samlResourcesURL}"/>
    </c:when>
    <c:when test="${theme eq 'cesnet'}">
        <cesnet:header title="${title}" reqURL="${reqURL}" cssLinks="${cssLinks}" baseURL="${baseURL}" samlResourcesURL="${samlResourcesURL}"/>
    </c:when>
    <c:when test="${theme eq 'bbmri'}">
        <bbmri:header title="${title}" reqURL="${reqURL}" cssLinks="${cssLinks}" baseURL="${baseURL}" samlResourcesURL="${samlResourcesURL}"/>
    </c:when>
    <c:when test="${theme eq 'ceitec'}">
        <ceitec:header title="${title}" reqURL="${reqURL}" cssLinks="${cssLinks}" baseURL="${baseURL}" samlResourcesURL="${samlResourcesURL}"/>
    </c:when>
    <c:when test="${theme eq 'europdx'}">
        <europdx:header title="${title}" reqURL="${reqURL}" cssLinks="${cssLinks}" baseURL="${baseURL}" samlResourcesURL="${samlResourcesURL}"/>
    </c:when>
    <c:when test="${theme eq 'muni'}">
        <muni:header title="${title}" reqURL="${reqURL}" cssLinks="${cssLinks}" baseURL="${baseURL}" samlResourcesURL="${samlResourcesURL}"/>
    </c:when>
    <c:otherwise>
        <o:header title="${title}"/>
    </c:otherwise>
</c:choose>
