<%@attribute name="pageName" required="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags"%>
<c:choose>
	<c:when test="${ not empty userInfo.preferredUsername }">
		<c:set var="shortName" value="${ userInfo.preferredUsername }" />
	</c:when>
	<c:otherwise>
		<c:set var="shortName" value="${ userInfo.userId }" />
	</c:otherwise>
</c:choose>
<c:choose>
	<c:when test="${ not empty userInfo.name }">
		<c:set var="longName" value="${ userInfo.name }" />
	</c:when>
	<c:otherwise>
		<c:choose>
			<c:when test="${ not empty userInfo.givenName || not empty userInfo.familyName }">
				<c:set var="longName" value="${ userInfo.givenName } {$ userInfo.familyName }" />
			</c:when>
			<c:otherwise>
				<c:set var="longName" value="${ shortName }" />
			</c:otherwise>
		</c:choose>
	</c:otherwise>
</c:choose>
<div class="navbar navbar-fixed-top">
	<div class="navbar-inner">
		<div class="container">
			<button class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
				<span class="icon-bar"></span> 
				<span class="icon-bar"></span> 
				<span class="icon-bar"></span>
			</button>
			<a class="brand" href="">${config.topbarTitle}</a>
			<div class="nav-collapse collapse">
				<ul class="nav">
					<c:choose>
						<c:when test="${pageName == 'Home' || empty pageName}">
							<li class="active"><a href="">Home</a></li>
						</c:when>
						<c:otherwise>
							<li><a href="">Home</a></li>
						</c:otherwise>
					</c:choose>
					<c:choose>
						<c:when test="${pageName == 'About'}">
							<li class="active"><a href="">About</a></li>
						</c:when>
						<c:otherwise>
							<li><a href="about">About</a></li>
						</c:otherwise>
					</c:choose>
					<c:choose>
						<c:when test="${pageName == 'Statistics'}">
							<li class="active"><a href="">Statistics</a></li>
						</c:when>
						<c:otherwise>
							<li><a href="stats">Statistics</a></li>
						</c:otherwise>
					</c:choose>
					<c:choose>
						<c:when test="${pageName == 'Contact'}">
							<li class="active"><a href="">Contact</a></li>
						</c:when>
						<c:otherwise>
							<li><a href="contact">Contact</a></li>
						</c:otherwise>
					</c:choose>


					<security:authorize access="hasRole('ROLE_USER')">

						<li class="dropdown hidden-desktop"><a href="#"
							class="dropdown-toggle" data-toggle="dropdown">Action <b
								class="caret"></b></a>
							<ul class="dropdown-menu">
								<o:actionmenu />
							</ul></li>

					</security:authorize>

				</ul>
				<ul class="nav pull-right">
                    <security:authorize access="hasRole('ROLE_USER')">
					<div class="btn-group">
						<a class="btn btn-primary btn-small dropdown-toggle" data-toggle="dropdown" href=""><i class="icon-user icon-white"></i> ${ shortName } <span class="caret"></span></a>
						<ul class="dropdown-menu">
							<li><a>${ longName }</a></li>
							<li class="divider"></li>
							<li><a href="logout"><i class="icon-remove"></i> Log out</a></li>
						</ul>
					</div>
                    </security:authorize>
                    <security:authorize access="!hasRole('ROLE_USER')">
                    	<a class="btn btn-primary btn-small" href="login"><i class="icon-user icon-white"></i> Log in</a>
                    </security:authorize>
                </ul>
                    
            </div><!--/.nav-collapse -->
        </div>
    </div>
</div>
