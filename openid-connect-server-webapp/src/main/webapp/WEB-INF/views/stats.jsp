<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="security"
	uri="http://www.springframework.org/security/tags"%>

<!-- TODO: highlight proper section of topbar; what is the right way to do this? -->

<o:header title="Statistics"/>
<o:topbar pageName="Statistics"/>
<div class="container-fluid main">
    <div class="row-fluid">
        <o:sidebar/>
        <div class="span10">
            <div class="hero-unit">
				<o:statsContent/>
            </div>
        </div>
    </div>
</div>
<o:footer/>
