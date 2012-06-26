<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>

<div class="span2">
    <div class="well sidebar-nav">
        <ul class="nav nav-list">
            <security:authorize ifAnyGranted="ROLE_ADMIN">
                <li class="nav-header">Administrative</li>
                <li><a href="admin/manage/#clients">Manage Clients</a></li>
                <li><a href="admin/manage/#white_list">White Lists</a></li>
                <li><a href="#">Black Lists</a></li>
            </security:authorize>
            <li class="nav-header">Personal</li>
            <li><a href="#">Manage Sites</a></li>
            <li><a href="#">Manage Active Tokens</a></li>
            <li><a href="#">Manage Profiles</a></li>
        </ul>
    </div><!--/.well -->
</div><!--/span-->