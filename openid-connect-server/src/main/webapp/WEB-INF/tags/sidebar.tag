<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>

<div class="span2">

<security:authorize access="hasRole('ROLE_USER')">
    <div class="well sidebar-nav">
        <ul class="nav nav-list">
            <security:authorize access="hasRole('ROLE_ADMIN')">
                <li class="nav-header">Administrative</li>
                <li><a href="manage/#admin/clients">Manage Clients</a></li>
                <li><a href="manage/#admin/whitelists">Whitelisted Clients</a></li>
                <li><a href="manage/#admin/blacklist">Blacklisted Clients</a></li>
                <li><a href="manage/#admin/scope">System Scopes</a></li>
            </security:authorize>
	            <li class="nav-header">Personal</li>
	            <li><a href="manage/#user/approved">Manage Sites</a></li>
	            <li><a href="manage/#user/tokens">Manage Active Tokens</a></li>
	            <li><a href="manage/#user/profile">Manage Profiles</a></li>
        </ul>
    </div><!--/.well -->
</security:authorize>
<security:authorize access="!hasRole('ROLE_USER')">
	<div class="well">
		<div>You are not logged in.</div>
		<hr />
		<div class="row-fluid"><a class="btn btn-primary span12" href="j_spring_security_check"><i class="icon-user icon-white"></i> Log in</a></div>
	</div>
</security:authorize>
</div><!--/span-->