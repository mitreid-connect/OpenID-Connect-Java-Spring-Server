<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<div class="navbar navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container-fluid">
            <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </a>
            <a class="brand" style="padding-left: 35px" href="">OpenID Connect Server</a>
            <div class="nav-collapse">
                <ul class="nav">
                    <li class="active"><a href="">Home</a></li>
                    <li><a href="about">About</a></li>
                    <li><a href="stats">Statistics</a></li>
                    <li><a href="contact">Contact</a></li>
                </ul>
				<ul class="nav pull-right">
                    <security:authorize access="hasRole('ROLE_USER')">
					<div class="btn-group">
						<a class="btn btn-primary btn-mini" href=""><i class="icon-user icon-white"></i> ${ userInfo.preferredUsername }</a>
						<a class="btn btn-primary btn-mini dropdown-toggle" data-toggle="dropdown" href=""><span class="caret"></span></a>
						<ul class="dropdown-menu">
							<li><a href="j_spring_security_logout"><i class="icon-remove"></i> Log out</a></li>
						</ul>
					</div>
                    </security:authorize>
                    <security:authorize access="!hasRole('ROLE_USER')">
                    	<a class="btn btn-primary btn-mini" href="j_spring_security_check"><i class="icon-user icon-white"></i> Log in</a>
                    </security:authorize>
                </ul>
                    
            </div><!--/.nav-collapse -->
        </div>
    </div>
</div>