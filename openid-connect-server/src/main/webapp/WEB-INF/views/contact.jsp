<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>

<!-- TODO: highlight proper section of topbar; what is the right way to do this? -->

<o:header title="welcome"/>
<o:topbar title="${topbarTitle}" pageName="Contact"/>
<div class="container-fluid">
    <div class="row-fluid">
        <o:sidebar/>
        <div class="span10">
            <!-- Main hero unit for a primary marketing message or call to action -->
            <div class="hero-unit">

				<h2>Contact</h2>    
                <p>    
                For general assistance, email Bob at <a href="mailto:email@address.com?Subject=OIDC%20Server%20Assistance">email@address.com</a>.
                To offer feedback, email Sue at <a href="mailto:email@address.com?Subject=OIDC%20Server%20Feedback">email@address.com</a>.  
                To report a system failure or bug report, email Joe at <a href="mailto:email@address.com?Subject=OIDC%20Server%20Failure">email@address.com</a>. 
                </p>

            </div>


        </div>
    </div>
</div>
<o:copyright/>
<o:footer/>