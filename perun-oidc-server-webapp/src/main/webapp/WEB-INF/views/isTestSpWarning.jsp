<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" trimDirectiveWhitespaces="true" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/common"%>


<c:set var="baseURL" value="${baseURL}"/>
<c:set var="samlResourcesURL" value="${samlResourcesURL}"/>

<%
    List<String> cssLinks = new ArrayList<>();
    pageContext.setAttribute("cssLinks", cssLinks);
%>

<t:header title="${langProps['is_test_sp_warning_title']}" reqURL="${reqURL}"
          baseURL="${baseURL}" cssLinks="${cssLinks}" theme="${theme}"/>

</div> <%-- header --%>

<div id="content">
    <div id="head">
        <h1>${langProps['is_test_sp_warning_header']}</h1>
    </div>
    <p>${langProps['is_test_sp_warning_text']}</p>

    <form method="GET" action="${action}">
        <hr/>
        <br/>
        <input type="hidden" name="target" value="${fn:escapeXml(target)}">
        <input type="hidden" name="accepted" value="true">
        <input type="submit" name="continue" value="${langProps['is_test_sp_warning_continue']}"
               class="btn btn-lg btn-primary btn-block">
    </form>
</div>
</div><!-- ENDWRAP -->

<t:footer baseURL="${baseURL}" theme="${theme}"/>