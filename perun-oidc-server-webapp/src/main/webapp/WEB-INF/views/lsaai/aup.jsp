<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ls" tagdir="/WEB-INF/tags/lsaai" %>

<ls:header />

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

<ls:footer/>
