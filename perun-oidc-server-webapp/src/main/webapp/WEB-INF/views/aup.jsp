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

<t:header title="${langProps['aup_header']}" reqURL="${reqURL}" baseURL="${baseURL}" cssLinks="${cssLinks}" theme="${theme}"/>

<h1>${langProps['aup_header']}</h1>

</div> <%-- header --%>

<div id="content">
    <h3>${langProps['must_agree_aup']}</h3>
    <form method="POST" action="">
        <c:forEach var="aup" items="${newAups}">
            <div>
                <p style="font-size: 16px; padding: 0; margin: 0;">${langProps['org_vo']} ${" "}<strong><c:out value="${aup.key}"/></strong></p>
                <p>${langProps['see_aup']}${" "}${aup.value.version}${" "}<a href="<c:out value="${aup.value.link}"/>">${langProps['here']}</a></p>
            </div>
        </c:forEach>
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <div class="form-group">
            <input type="submit" value="${langProps['agree_aup']}" class="btn btn-lg btn-primary btn-block">
        </div>
    </form>
</div>
</div><!-- wrap -->

<t:footer baseURL="${baseURL}" theme="${theme}"/>
