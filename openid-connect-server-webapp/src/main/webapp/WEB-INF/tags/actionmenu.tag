<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="security"
	uri="http://www.springframework.org/security/tags"%>
<security:authorize access="hasRole('ROLE_ADMIN')">
	<li class="nav-header">Administrative</li>
	<li><a href="manage/#admin/clients" data-toggle="collapse" data-target=".nav-collapse">Manage Clients</a></li>
	<li><a href="manage/#admin/whitelists" data-toggle="collapse" data-target=".nav-collapse">Whitelisted Clients</a></li>
	<li><a href="manage/#admin/blacklist" data-toggle="collapse" data-target=".nav-collapse">Blacklisted Clients</a></li>
	<li><a href="manage/#admin/scope" data-toggle="collapse" data-target=".nav-collapse">System Scopes</a></li>
	<li class="divider"></li>
</security:authorize>
<li class="nav-header">Personal</li>
<li><a href="manage/#user/approved" data-toggle="collapse" data-target=".nav-collapse">Manage Approved Sites</a></li>
<li><a href="manage/#user/tokens" data-toggle="collapse" data-target=".nav-collapse">Manage Active Tokens</a></li>
<li><a href="manage/#user/profile" data-toggle="collapse" data-target=".nav-collapse">View Profile Information</a></li>
<li class="divider"></li>
<li class="nav-header">Developer</li>
<li><a href="manage/#dev/dynreg" data-toggle="collapse" data-target=".nav-collapse">Self-service client registration</a><li>
<li><a href="manage/#dev/resource" data-toggle="collapse" data-target=".nav-collapse">Self-service protected resource registration</a><li>