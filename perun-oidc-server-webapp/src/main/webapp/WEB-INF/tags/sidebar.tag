<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags"%>


<security:authorize access="hasRole('ROLE_USER')">
	<div class="span2 visible-desktop">
	    <div class="well sidebar-nav">
	        <ul class="nav nav-list">
	        	<o:actionmenu />
	        </ul>
	    </div><!--/.well -->
	</div><!--/span-->
</security:authorize>
