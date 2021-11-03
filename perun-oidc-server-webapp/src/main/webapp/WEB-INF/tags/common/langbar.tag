<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="lang" required="true" %>
<%@ attribute name="langsMap" required="true" type="java.util.Map" %>
<%@ attribute name="reqURL" required="true" %>

<c:set var="i" value="0"/>
<div id="languagebar_line">
    <div id="languagebar">
    <c:choose>
        <c:when test="${fn:contains(reqURL, '?')}">
            <c:set var="requestURL" value="${reqURL}${'&lang='}"/>
        </c:when>
        <c:otherwise>
            <c:set var="requestURL" value="${reqURL}${'?lang='}"/>
        </c:otherwise>
    </c:choose>
        <c:forEach var="langEntry" items="${langsMap}">
            <c:choose>
                <c:when test="${ langEntry.key.equalsIgnoreCase(lang)}">
                    <c:out value="${langEntry.value}" />
                </c:when>
                <c:otherwise>
                    <a href="${requestURL}${langEntry.key}">${langEntry.value}</a>
                </c:otherwise>
            </c:choose>
            <c:if test="${ i < (langsMap.size() - 1) }">
                <c:out value=" | "/>
            </c:if>
            <c:set var="i" value="${ i + 1 }"/>
        </c:forEach>
    </div>
</div>