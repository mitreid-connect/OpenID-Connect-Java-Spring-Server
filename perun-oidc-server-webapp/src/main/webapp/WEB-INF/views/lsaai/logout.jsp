<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ls" tagdir="/WEB-INF/tags/lsaai" %>

<ls:header />

    <h1><spring:message code="logout.confirmation.header"/></h1>
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

<ls:footer/>
