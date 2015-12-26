<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<h2><spring:message code="statistics.title"/></h2>

<p>
    <spring:message code="statistics.number_users" arguments='${statsSummary["userCount"]}'/>
    <spring:message code="statistics.number_clients" arguments='${statsSummary["clientCount"]}'/>
    <spring:message code="statistics.number_approvals" arguments='${statsSummary["approvalCount"]}'/>
</p>
