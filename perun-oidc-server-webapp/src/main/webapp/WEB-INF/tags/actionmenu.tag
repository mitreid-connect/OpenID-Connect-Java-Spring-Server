<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="security"
	uri="http://www.springframework.org/security/tags"%>
<security:authorize access="hasRole('ROLE_ADMIN')">
	<li class="nav-header"><spring:message code="sidebar.administrative.title"/></li>
	<li><a href="manage/#admin/clients" data-toggle="collapse" data-target=".nav-collapse"><spring:message code="sidebar.administrative.manage_clients"/></a></li>
	<li><a href="manage/#admin/whitelists" data-toggle="collapse" data-target=".nav-collapse"><spring:message code="sidebar.administrative.whitelisted_clients"/></a></li>
	<li><a href="manage/#admin/blacklist" data-toggle="collapse" data-target=".nav-collapse"><spring:message code="sidebar.administrative.blacklisted_clients"/></a></li>
	<li><a href="manage/#admin/scope" data-toggle="collapse" data-target=".nav-collapse"><spring:message code="sidebar.administrative.system_scopes"/></a></li>
	<li class="divider"></li>
</security:authorize>
<li class="nav-header"><spring:message code="sidebar.personal.title"/></li>
<li><a href="manage/#user/approved" data-toggle="collapse" data-target=".nav-collapse"><spring:message code="sidebar.personal.approved_sites"/></a></li>
<li><a href="manage/#user/tokens" data-toggle="collapse" data-target=".nav-collapse"><spring:message code="sidebar.personal.active_tokens"/></a></li>
