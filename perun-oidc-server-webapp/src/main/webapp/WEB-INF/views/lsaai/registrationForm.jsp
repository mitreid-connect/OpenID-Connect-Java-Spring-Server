<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ls" tagdir="/WEB-INF/tags/lsaai" %>

<ls:header />

    <div id="head">
        <h1><spring:message code="registration_header1"/>
            <c:choose>
                <c:when test="${not empty client.clientName and not empty client.clientUri}">
                    &#32;<a href="${fn:escapeXml(client.clientUri)}">${fn:escapeXml(client.clientName)}</a>
                </c:when>
                <c:when test="${not empty client.clientName}">
                    &#32;${fn:escapeXml(client.clientName)}
                </c:when>
            </c:choose>
            ${" "}<spring:message code="registration_header2"/>
        </h1>
    </div>
    <div class="msg"><spring:message code="registration_message"/></div>

    <div class="list-group">
        <form action="${action}" method="get">
            <h4><spring:message code="registration_select_vo"/></h4>
            <select id="selectVo" class="form-control" name="selectedVo" onchange="filter()" required>
                <c:forEach var="voGroupPair" items="${groupsForRegistration}">
                    <option value="${fn:escapeXml(voGroupPair.key.shortName)}">
                            ${fn:escapeXml(voGroupPair.key.name)}
                    </option>
                </c:forEach>
            </select>

            <h4 class="selectGroup" style="display: none"><spring:message code="registration_select_group"/></h4>
            <select  class="selectGroup form-control" name="selectedGroup" class="form-control" style="display: none" required>
                <c:forEach var="voGroupPair" items="${groupsForRegistration}">
                    <c:forEach var="group" items="${voGroupPair.value}">
                        <option class="groupOption" value="${fn:escapeXml(voGroupPair.key.shortName)}:${fn:escapeXml(group.name)}">
                                ${fn:escapeXml(group.description)}
                        </option>
                    </c:forEach>
                </c:forEach>
            </select>

            <spring:message code="registration_continue" var="submit_value"/>
            <input type="submit" value="${submit_value}" class="btn btn-lg btn-primary btn-block">
        </form>
    </div>

<script type="text/javascript" src="resources/js/reg_form_select.js"></script>

<ls:footer/>
