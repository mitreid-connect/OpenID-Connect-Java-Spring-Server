<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" trimDirectiveWhitespaces="true" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/common"%>

<%

List<String> cssLinks = new ArrayList<>();

pageContext.setAttribute("cssLinks", cssLinks);

%>

<t:header title="${title}" reqURL="${reqURL}" baseURL="${baseURL}" cssLinks="${cssLinks}" theme="${theme}"/>

</div> <%-- header --%>

<div id="content">
    <div class="error_message" style="word-wrap: break-word;">
        <h1><c:out value="${outHeader}" escapeXml="false"/></h1>
        <p><c:out value="${outMessage}" escapeXml="false"/></p>
        <p>${langProps['contact_p']}${" "}<a href="mailto:${contactMail}">${contactMail}</a></p>
    </div>
</div>
</div><!-- ENDWRAP -->

<t:footer baseURL="${baseURL}" theme="${theme}"/>
