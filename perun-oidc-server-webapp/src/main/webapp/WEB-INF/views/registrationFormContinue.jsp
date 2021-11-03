<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" trimDirectiveWhitespaces="true" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/common"%>

<c:set var="baseURL" value="${baseURL}"/>
<c:set var="samlResourcesURL" value="${samlResourcesURL}"/>
<%

String samlCssUrl = (String) pageContext.getAttribute("samlResourcesURL");
List<String> cssLinks = new ArrayList<>();

cssLinks.add(samlCssUrl + "/module.php/perun/res/css/perun_identity_go_to_registration.css");

pageContext.setAttribute("cssLinks", cssLinks);

%>

<t:header title="${langProps['go_to_registration_title']}" reqURL="${reqURL}" baseURL="${baseURL}" cssLinks="${cssLinks}" theme="${theme}"/>

</div> <%-- header --%>

<div id="content">
    <div id="head">
        <h1>${langProps['go_to_registration_header1']}
            <c:choose>
                <c:when test="${not empty client.clientName and not empty client.clientUri}">
                    &#32;<a href="${fn:escapeXml(client.uri)}">${fn:escapeXml(client.clientName)}</a>
                </c:when>
                <c:when test="${not empty client.clientName}">
                    &#32;${fn:escapeXml(client.clientName)}
                </c:when>
            </c:choose>
            &#32;${langProps['go_to_registration_header2']}
        </h1>
    </div>
    <form method="GET" action="${action}">
        <hr/>
        <br/>
        <input type="hidden" name="client_id" value="${fn:escapeXml(client_id)}" />
        <input type="hidden" name="facility_id" value="${fn:escapeXml(facility_id)}" />
        <input type="hidden" name="user_id" value="${fn:escapeXml(user_id)}" />
        <input type="submit" name="continueToRegistration" value="${langProps['go_to_registration_continue']}"
               class="btn btn-lg btn-primary btn-block">
    </form>
</div>
</div><!-- ENDWRAP -->

<t:footer baseURL="${baseURL}" theme="${theme}"/>