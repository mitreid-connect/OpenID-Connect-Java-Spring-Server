<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true"
        import="cz.muni.ics.oidc.server.ga4gh.Ga4ghPassportAndVisaClaimSource" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<c:if test="${empty scopes}">
    <p><spring:message code="no_scopes"/></p>
</c:if>
<c:if test="${not empty scopes}">
    <ul id="perun-table_with_attributes" class="perun-attributes">
        <c:forEach var="scope" items="${scopes}">
            <spring:message code="${scope.value}" var="scope_value_txt"/>
            <c:set var="singleClaim" value="${fn:length(claims[scope.value]) eq 1}" />
            <li class="scope-item scope_${fn:escapeXml(scope.value)} ${' '} ${fn:length(claims[scope.value]) eq 0 ? 'hidden' : ''}">
                <div class="row">
                    <div class="col-sm-5">
                        <div class="checkbox-wrapper">
                            <input class="mt-0 mr-half" type="checkbox" name="scope_${ fn:escapeXml(scope.value) }" checked="checked"
                                   id="scope_${fn:escapeXml(scope.value)}" value="${fn:escapeXml(scope.value)}">
                        </div>
                        <h2 class="perun-attrname ${classes['perun-attrname.h2.class']}">
                            <label for="scope_${fn:escapeXml(scope.value)}"
                                   class="${classes['perun-attrname.h2.class']}">${scope_value_txt}</label>
                        </h2>
                    </div>
                    <div class="perun-attrcontainer col-sm-7">
                        <span class="perun-attrvalue">
                            <ul class="perun-attrlist ${classes['perun-attrcontainer.ul.class']}/>">
                                <c:forEach var="claim" items="${claims[scope.value]}">
                                    <c:choose>
                                        <c:when test="${not singleClaim}">
                                            <li class="subclaim subclaim_${fn:escapeXml(claim.key)}">
                                                <spring:message code="${claim.key}" var="claimKey"/>
                                                <h3 class="visible-xs-block visible-sm-inline-block visible-md-inline-block
                                                    visible-lg-inline-block ${classes['perun-attrlist.h3.class']}">
                                                    ${claimKey}:
                                                </h3>
                                                <c:if test="${claim.value.getClass().name eq 'java.util.ArrayList'}">
                                                    <ul class="subclaim-value">visible-md-inline-block
                                                        <c:forEach var="subValue" items="${claim.value}">
                                                            <li>${subValue}</li>
                                                        </c:forEach>
                                                    </ul>
                                                </c:if>
                                                <c:if test="${not(claim.value.getClass().name eq 'java.util.ArrayList')}">
                                                    <span class="subclaim-value">${claim.value}</span>
                                                </c:if>
                                            </li>
                                        </c:when>
                                        <c:when test="${claim.value.getClass().name eq 'java.util.ArrayList'}">
                                            <c:forEach var="subValue" items="${claim.value}">
                                                <li>${subValue}</li>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <li>${claim.value}</li>
                                        </c:otherwise>
                                    </c:choose>
                                </c:forEach>
                            </ul>
                        </span>
                    </div>
                </div>
            </li>
        </c:forEach>
    </ul>
</c:if>