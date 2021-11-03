<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/common" %>

<%

List<String> cssLinks = new ArrayList<>();

pageContext.setAttribute("cssLinks", cssLinks);

%>

<t:header title="${langProps['continue_direct_title']}" reqURL="${reqURL}" baseURL="${baseURL}"
          cssLinks="${cssLinks}" theme="${theme}"/>

<h1>${langProps['continue_direct_header']}</h1>

</div> <%-- header --%>

<div id="content">
    <div id="head">
        <h1>${langProps['continue_direct_heading']}</h1>
    </div>
    <p>${langProps['continue_direct_text']}</p>
    <hr/>
    <br/>
    <a href="${fn:escapeXml(target)} "class="btn btn-lg btn-primary btn-block">${langProps['continue_direct_btn']}</a>
</div>
</div><!-- wrap -->

<t:footer baseURL="${baseURL}" theme="${theme}"/>
