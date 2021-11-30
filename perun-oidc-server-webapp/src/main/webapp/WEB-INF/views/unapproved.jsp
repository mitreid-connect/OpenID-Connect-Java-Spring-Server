<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" trimDirectiveWhitespaces="true" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/common"%>
<%@ taglib prefix="spring" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%

List<String> cssLinks = new ArrayList<>();

pageContext.setAttribute("cssLinks", cssLinks);

%>

<t:header title="${title}" reqURL="${reqURL}" baseURL="${baseURL}" cssLinks="${cssLinks}" theme="${theme}"/>

</div> <%-- header --%>

<div id="content">
    <div class="error_message" style="word-wrap: break-word;">
        <c:forEach var="contactIter" items="${client.contacts}" end="0">
            <c:set var="contact" value="${contactIter}" />
        </c:forEach>
        <c:if test="${empty contact}">
            <c:set var="contact" value="${contactMail}"/>
        </c:if>
        <h1><spring:message key="403_header"/></h1>
        <p><spring:message key="403_text"/>${' '}${fn:escapeXml(client.clientName)}
            <c:if test="${not empty client.clientUri}">
                <br/>
                <spring:message key="403_informationPage"/>${' '}
                <a href="${fn:escapeXml(client.clientUri)}">
                    ${fn:escapeXml(client.clientUri)}
                </a>
            </c:if>
        </p>

        <spring:message key="403_subject" var="subject"/>
        <p><spring:message key="403_contactSupport"/>${' '}
           <a href="mailto:${contact}?subject=${subject} ${fn:escapeXml(client.clientName)}">
               ${fn:escapeXml(contact)}
           </a>
        </p>
    </div>
</div>
</div><!-- ENDWRAP -->

<t:footer baseURL="${baseURL}" theme="${theme}"/>
