<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags/common" %>
<%@ attribute name="logoURL" required="true" %>

<body>

    <div id="wrap">
        <c:if test="${ langsMap.size() > 1 }">
            <o:langbar lang="${lang}" langsMap="${langsMap}" reqURL="${reqURL}"/>
        </c:if>
        <div id="header">
            <img src="${logoURL}" alt="logo">
