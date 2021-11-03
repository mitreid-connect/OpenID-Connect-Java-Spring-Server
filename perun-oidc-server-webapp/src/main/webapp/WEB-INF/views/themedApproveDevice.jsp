<%@ page import="cz.muni.ics.oidc.server.elixir.GA4GHClaimSource" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/common" %>

<c:set var="baseURL" value="${baseURL}"/>
<c:set var="samlResourcesURL" value="${samlResourcesURL}"/>

<%

	String samlCssUrl = (String) pageContext.getAttribute("samlResourcesURL");
	List<String> cssLinks = new ArrayList<>();

	cssLinks.add(samlCssUrl + "/module.php/consent/assets/css/consent.css");
	cssLinks.add(samlCssUrl + "/module.php/perun/res/css/consent.css");

	pageContext.setAttribute("cssLinks", cssLinks);

%>

    <t:header title="${langProps['device_approve_title']}" reqURL="${reqURL}" baseURL="${baseURL}"
              cssLinks="${cssLinks}" theme="${theme}"/>

    <h1 class="h3">${langProps['device_approve_header']} ${" "} ${fn:escapeXml(client.clientName)}</h1>

</div> <%-- header --%>

<div id="content">
    <c:remove scope="session" var="SPRING_SECURITY_LAST_EXCEPTION" />
        <form name="confirmationForm"
              action="${ config.issuer }${ config.issuer.endsWith('/') ? '' : '/' }device/approve" method="post">
            <p>
                <c:if test="${not empty client.policyUri}">
                    ${langProps['device_approve_privacy']}
                    &#32;<a target='_blank' href='${fn:escapeXml(client.policyUri)}'><em>${fn:escapeXml(client.clientName)}</em></a>
                </c:if>
            </p>
            <t:attributesConsent/>
            <input id="user_oauth_approval" name="user_oauth_approval" value="true" type="hidden" />
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
            <input type="hidden" name="user_code" value="${ dc.userCode }" />
            <t:consentButtons/>
        </form>
    </div>
</div><!-- wrap -->

<t:footer baseURL="${baseURL}" theme="${theme}"/>
