<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags"%>

<div class="span2 visible-desktop">

<security:authorize access="hasRole('ROLE_USER')">
    <div class="well sidebar-nav">
        <ul class="nav nav-list">
        	<o:actionmenu />
        </ul>
    </div><!--/.well -->
</security:authorize>
<security:authorize access="!hasRole('ROLE_USER')">
	<div class="well">
		<div>You are not logged in.</div>
		<hr />
		<div class="row-fluid"><a class="btn btn-primary span12" href="login"><i class="icon-user icon-white"></i> Log in</a></div>
	</div>
</security:authorize>
</div><!--/span-->
