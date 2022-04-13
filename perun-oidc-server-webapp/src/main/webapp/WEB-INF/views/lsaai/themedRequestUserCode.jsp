<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ls" tagdir="/WEB-INF/tags/lsaai" %>

<ls:header/>

    <h1><spring:message code="request_code_header"/></h1>
    <c:choose>
        <c:when test="${ not empty error }">
            <p class="alert alert-danger mt-2">
            <c:choose>
                <c:when test="${ error == 'noUserCode' }">
                    <spring:message code="user_code_empty_or_not_found"/>
                </c:when>
                <c:when test="${ error == 'expiredUserCode' }">
                    <spring:message code="user_code_expired"/>
                </c:when>
                <c:when test="${ error == 'userCodeAlreadyApproved' }">
                    <spring:message code="user_code_already_approved"/>
                </c:when>
                <c:when test="${ error == 'userCodeMismatch' }">
                    <spring:message code="user_code_mismatch"/>
                </c:when>
                <c:otherwise>
                    <spring:message code="user_code_error"/>
                </c:otherwise>
            </c:choose>
            </p>
        </c:when>
        <c:otherwise>
            <p class="mt-2"><spring:message code="user_code_info"/></p>
        </c:otherwise>
    </c:choose>

    <form name="confirmationForm" class="mt-2"  method="POST"
          action="${ config.issuer }${ config.issuer.endsWith('/') ? '' : '/' }auth/device">
        <div class="row-fluid">
            <div class="span12">
                <div>
                    <div class="input-block-level input-xlarge">
                        <spring:message code="code" var="code_placeholder"/>
                        <input type="text" name="user_code" placeholder="${code_placeholder}"
                               autocapitalize="off" autocomplete="off" spellcheck="false" value="${user_code}" />
                    </div>
                </div>
            </div>
        </div>
        <div class="row-fluid mt-2">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
            <input type="hidden" name="acr" value="${acr}">
            <spring:message code="user_code_submit" var="submit_value"/>
            <input name="approve" value="${submit_value}" type="submit"
                   class="btn btn-success btn-block btn-large" />
        </div>

    </form>

<ls:footer/>