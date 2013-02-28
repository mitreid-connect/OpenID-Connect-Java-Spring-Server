<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>

<o:header title="welcome"/>
<o:topbar title="${topbarTitle}"/>
<div class="container-fluid">
    <div class="row-fluid">
        <o:sidebar/>
        <div class="span10">
            <!-- Main hero unit for a primary marketing message or call to action -->
            <div class="hero-unit">
                <o:landingPageText/>
            </div>
            <!-- Example row of columns -->
            <div class="row-fluid">
                <div class="span6">
                    <o:aboutContent short="true"/>
                </div>
                <div class="span6">
                	<o:contactContent short="true"/>
                </div>

            </div>
            <hr>
            <!-- Example row of columns -->
            <div class="row-fluid">
                <div class="span12">
                    <o:statsContent short="true"/>
                </div>
            </div>

        </div>
    </div>
</div>
<o:copyright/>
<o:footer/>
