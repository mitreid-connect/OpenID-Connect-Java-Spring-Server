<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ls" tagdir="/WEB-INF/tags/lsaai" %>

<ls:header />

    <div class="error_message" style="word-wrap: break-word;">
        <c:forEach var="contactIter" items="${client.contacts}" end="0">
            <c:set var="contact" value="${contactIter}" />
        </c:forEach>
        <c:if test="${empty contact}">
            <c:set var="contact" value="${contactMail}"/>
        </c:if>
        <h1><spring:message code="403_header"/></h1>
        <p><spring:message code="403_text"/>${' '}${fn:escapeXml(client.clientName)}
            <c:if test="${not empty client.clientUri}">
                <br/>
                <spring:message code="403_informationPage"/>${' '}
                <a href="${fn:escapeXml(client.clientUri)}">
                    ${fn:escapeXml(client.clientUri)}
                </a>
            </c:if>
        </p>

        <spring:message code="403_subject" var="subject"/>
        <p><spring:message code="403_contactSupport"/>${' '}
           <a href="mailto:${contact}?subject=${subject} ${fn:escapeXml(client.clientName)}">
               ${fn:escapeXml(contact)}
           </a>
        </p>
    </div>

<ls:footer />
