<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<div class="navbar navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container-fluid">
            <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </a>
            <a class="brand" style="padding-left: 35px" href="#">OpenID Connect Server</a>
            <div class="nav-collapse">
                <ul class="nav">
                    <li class="active"><a href="#">Home</a></li>
                    <li><a href="#about">About</a></li>
                    <li><a href="#contact">Statistics</a></li>
                    <li><a href="#contact">Contact</a></li>
                </ul>
                <p class="navbar-text pull-right">
                    <security:authorize ifAllGranted="ROLE_USER">
                        Logged in as <a href="#"><%= request.getUserPrincipal().getName() %></a>
                    </security:authorize>
                </p>
            </div><!--/.nav-collapse -->
        </div>
    </div>
</div>