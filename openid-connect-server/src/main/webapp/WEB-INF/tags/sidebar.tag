<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>

<div class="span2">
    <div class="well sidebar-nav">
        <ul class="nav nav-list">
            <security:authorize ifAnyGranted="ROLE_ADMIN">
                <li class="nav-header">Administrative</li>
                <li><a href="manage/#admin/clients">Manage Clients</a></li>
                <li><a href="manage/#admin/whitelists">White Lists</a></li>
                <li><a href="manage/#admin/blacklists">Black Lists</a></li>
            </security:authorize>
            <li class="nav-header">Personal</li>
            <li><a href="manage/#user/grants">Manage Sites</a></li>
            <li><a href="manage/#user/tokens">Manage Active Tokens</a></li>
            <li><a href="manage/#user/profile">Manage Profiles</a></li>
        </ul>
    </div><!--/.well -->
</div><!--/span-->