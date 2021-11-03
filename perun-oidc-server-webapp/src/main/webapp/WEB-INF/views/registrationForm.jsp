<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" trimDirectiveWhitespaces="true" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/common"%>

<c:set var="baseURL" value="${baseURL}"/>
<c:set var="samlResourcesURL" value="${samlResourcesURL}"/>
<%

String samlCssUrl = (String) pageContext.getAttribute("samlResourcesURL");
List<String> cssLinks = new ArrayList<>();

cssLinks.add(samlCssUrl + "/module.php/perun/res/css/perun_identity_choose_vo_and_group.css");

pageContext.setAttribute("cssLinks", cssLinks);

%>

<t:header title="${langProps['registration_title']}" reqURL="${reqURL}" baseURL="${baseURL}" cssLinks="${cssLinks}" theme="${theme}"/>

</div> <%-- header --%>

<div id="content">
    <div id="head">
        <h1>${fn:escapeXml(langProps['registration_header1'])}
            <c:choose>
                <c:when test="${not empty client.clientName and not empty client.clientUri}">
                    &#32;<a href="${fn:escapeXml(client.clientUri)}">${fn:escapeXml(client.clientName)}</a>
                </c:when>
                <c:when test="${not empty client.clientName}">
                    &#32;${fn:escapeXml(client.clientName)}
                </c:when>
            </c:choose>
            &#32;${langProps['registration_header2']}
        </h1>
    </div>
    <div class="msg">${langProps['registration_message']}</div>

    <div class="list-group">
        <form action="${action}" method="get">
            <h4>${langProps['registration_select_vo']}</h4>
            <select id="selectVo" class="form-control" name="selectedVo" onchange="filter()" required>
                <c:forEach var="voGroupPair" items="${groupsForRegistration}">
                    <option value="${fn:escapeXml(voGroupPair.key.shortName)}">
                            ${fn:escapeXml(voGroupPair.key.name)}
                    </option>
                </c:forEach>
            </select>

            <h4 class="selectGroup" style="display: none">${langProps['registration_select_group']}</h4>
            <select  class="selectGroup form-control" name="selectedGroup" class="form-control" style="display: none" required>
                <c:forEach var="voGroupPair" items="${groupsForRegistration}">
                    <c:forEach var="group" items="${voGroupPair.value}">
                        <option class="groupOption" value="${fn:escapeXml(voGroupPair.key.shortName)}:${fn:escapeXml(group.name)}">
                                ${fn:escapeXml(group.description)}
                        </option>
                    </c:forEach>
                </c:forEach>
            </select>

            <input type="submit" value="${langProps['registration_continue']}" class="btn btn-lg btn-primary btn-block">
        </form>
    </div>
</div>
</div><!-- ENDWRAP -->

<t:footer baseURL="${baseURL}" theme="${theme}"/>

<script type="text/javascript" src="resources/js/reg_form_select.js"></script>
