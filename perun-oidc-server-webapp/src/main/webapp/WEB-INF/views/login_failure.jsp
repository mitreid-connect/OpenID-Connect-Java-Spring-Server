<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" trimDirectiveWhitespaces="true" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/common"%>

<c:set var="baseURL" value="${baseURL}"/>
<c:set var="samlResourcesURL" value="${samlResourcesURL}"/>
<%

List<String> cssLinks = new ArrayList<>();
pageContext.setAttribute("cssLinks", cssLinks);

%>

<t:header title="${langProps['login_failure_title']}" reqURL="${reqURL}" baseURL="${baseURL}"
          cssLinks="${cssLinks}" theme="${theme}"/>

</div> <%-- header --%>

<div id="content">
    <div id="head">
        <h1>${fn:escapeXml(langProps['login_failure_header'])}</h1>
    </div>
    <div class="msg">${langProps['login_failure_msg']}</div>
    <div class="msg">${langProps['login_failure_contact_us']}<a href="mailto:${contactMail}">${contactMail}</a>.</div>
</div>
</div><!-- ENDWRAP -->

<t:footer baseURL="${baseURL}" theme="${theme}"/>
