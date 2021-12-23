<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/common" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:set var="baseURL" value="${baseURL}"/>
<c:set var="samlResourcesURL" value="${samlResourcesURL}"/>

<%

	List<String> cssLinks = new ArrayList<>();
	pageContext.setAttribute("cssLinks", cssLinks);

%>

<t:header title="Logout" reqURL="${reqURL}" baseURL="${baseURL}" cssLinks="${cssLinks}" theme="${theme}"/>

<h1><spring:message code="logout.confirmation.header"/></h1>
</div> <%-- header --%>

<div id="content">
    <form action="${config.issuer}${config.issuer.endsWith('/') ? '' : '/'}endsession" method="POST">
        <p><spring:message code="logout.confirmation.explanation"/></p>
        <div class="row">
            <div class="col-md-6 mb-4">
                <spring:message code="logout.confirmation.submit" var="submit_value_approve"/>
                <input name="approve" value="${submit_value_approve}"
                       type="submit" class="btn btn-lg btn-block btn-primary" />
            </div>
            <div class="col-md-6 mb-4">
                <spring:message code="logout.confirmation.deny" var="submit_value_deny"/>
                <input name="deny" value="${submit_value_deny}"
                       type="submit" class="btn btn-lg btn-block" />
            </div>
        </div>
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
    </form>
</div>

</div><!-- wrap -->

<t:footer baseURL="${baseURL}" theme="${theme}"/>
