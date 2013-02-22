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
                <h1>Welcome!</h1>

                <p>OpenID Connect is a next-generation protocol built on top of the OAuth2 authorization framework.
                   OpenID Connect lets you log into a remote site using your identity without exposing your 
                   credentials, like a username and password.                
                </p>

                <p><a class="btn btn-primary btn-large" href="http://openid.net/connect/">Learn more &raquo;</a></p>
            </div>
            <!-- Example row of columns -->
            <div class="row-fluid">
                <div class="span6">
                    <h2>About</h2>

                    <p>This OpenID Connect service is built from the MITREid Connect Open Source project started by The MITRE Corporation.</p>

                    <p><a class="btn" href="http://github.com/mitreid-connect/">More &raquo;</a></p>
                </div>
                <div class="span6">
                    <h2>Contact</h2>

                    <p>For more information or support, contact the administrators of this system.</p>

                    <p><a class="btn" href="mailto:idp@example.com?Subject=OpenID Connect">Email &raquo;</a></p>
                </div>

            </div>
            <hr>
            <!-- Example row of columns -->
            <div class="row-fluid">
                <div class="span12">
                    <h2>Current Statistics</h2>

                    <p>There have been <span class="label label-info">${statsSummary["userCount"]}</span> users of this
                        system who have logged in to <span class="label label-info">${statsSummary["clientCount"]}</span>
                        total sites, for a total of <span class="label label-info">${statsSummary["approvalCount"]}</span> site approvals.</p>

                </div>
            </div>

        </div>
    </div>
</div>
<o:copyright/>
<o:footer/>
