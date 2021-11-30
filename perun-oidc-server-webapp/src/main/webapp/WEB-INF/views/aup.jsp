<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/common" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%

List<String> cssLinks = new ArrayList<>();

pageContext.setAttribute("cssLinks", cssLinks);

%>

<spring:message code="aup_header" var="title"/>
<t:header title="${title}" reqURL="${reqURL}" baseURL="${baseURL}" cssLinks="${cssLinks}" theme="${theme}"/>

<h1><spring:message code="aup_header"/></h1>

</div> <%-- header --%>

<div id="content">
    <h3><spring:message code="must_agree_aup"/></h3>
    <form method="POST" action="">
        <c:forEach var="aup" items="${newAups}">
            <div>
                <p style="font-size: 16px; padding: 0; margin: 0;"><spring:message code="org_vo"/>${" "}<strong>${aup.key}</strong></p>
                <p><spring:message code="see_aup"/>${" "}${aup.value.version}${" "}
                    <a href="${aup.value.link}"><spring:message code="here"/></a></p>
            </div>
        </c:forEach>
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <div class="form-group">
            <spring:message code="agree_aup" var="submit_value"/>
            <input type="submit" value="${submit_value}" class="btn btn-lg btn-primary btn-block">
        </div>
    </form>
</div>
</div><!-- wrap -->

<t:footer baseURL="${baseURL}" theme="${theme}"/>
