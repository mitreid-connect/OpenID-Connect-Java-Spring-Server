<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/common" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="baseURL" value="${baseURL}"/>
<c:set var="samlResourcesURL" value="${samlResourcesURL}"/>

<%

    List<String> cssLinks = new ArrayList<>();
    pageContext.setAttribute("cssLinks", cssLinks);

%>

    <t:header title="${langProps['request_code_title']}" reqURL="${reqURL}" baseURL="${baseURL}"
              cssLinks="${cssLinks}" theme="${theme}"/>

</div> <%-- header --%>

<div id="content" class="text-center">
    <h1>${langProps['request_code_header']}</h1>
    <c:choose>
        <c:when test="${ not empty error }">
            <p class="alert alert-danger mt-2">
            <c:choose>
                <c:when test="${ error == 'noUserCode' }">${langProps['user_code_empty_or_not_found']}</c:when>
                <c:when test="${ error == 'expiredUserCode' }">${langProps['user_code_expired']}</c:when>
                <c:when test="${ error == 'userCodeAlreadyApproved' }">${langProps['user_code_already_approved']}</c:when>
                <c:when test="${ error == 'userCodeMismatch' }">${langProps['user_code_mismatch']}</c:when>
                <c:otherwise>${langProps['user_code_error']}</c:otherwise>
            </c:choose>
            </p>
        </c:when>
        <c:otherwise>
            <p class="mt-2">
                ${langProps['user_code_info']}
            </p>
        </c:otherwise>
    </c:choose>

    <form name="confirmationForm" class="mt-2"
          action="${ config.issuer }${ config.issuer.endsWith('/') ? '' : '/' }device/verify" method="post">
        <div class="row-fluid">
            <div class="span12">
                <div>
                    <div class="input-block-level input-xlarge">
                            <input type="text" name="user_code" placeholder="${langProps['code']}"
                                   autocorrect="off" autocapitalize="off" autocomplete="off" spellcheck="false"
                                   value="" />
                    </div>
                </div>
            </div>
        </div>
        <div class="row-fluid mt-2">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
            <input type="hidden" name="acr" value="${acr}">
            <input name="approve" value="${langProps['user_code_submit']}" type="submit"
                   class="btn btn-success btn-block btn-large" />
        </div>

    </form>
</div>

</div><!-- ENDWRAP -->

<t:footer baseURL="${baseURL}" theme="${theme}"/>