<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>

<o:header title="welcome"/>
<o:topbar/>
<div class="container<security:authorize ifAnyGranted="ROLE_USER">-fluid</security:authorize>">
    <div class="row<security:authorize ifAnyGranted="ROLE_USER">-fluid</security:authorize>">
        <security:authorize ifAnyGranted="ROLE_USER">
            <o:sidebar/>
        </security:authorize>
        <div<security:authorize ifAnyGranted="ROLE_USER"> class="span10"</security:authorize>>
            <!-- Main hero unit for a primary marketing message or call to action -->
            <div class="hero-unit">
                <h1>Welcome!</h1>

                <p>Can't remember your passwords? Tired of filling out registration forms?
                    OpenID is a <strong>safe</strong>, <strong>faster</strong>, and <strong>easier</strong> way to log
                    in to
                    web sites.</p>

                <p><a class="btn btn-primary btn-large" href="http://openid.net/connect/">Learn more &raquo;</a></p>
            </div>
            <!-- Example row of columns -->
            <div class="row<security:authorize ifAnyGranted="ROLE_USER">-fluid</security:authorize>">
                <div class="span6">
                    <h2>About</h2>

                    <p>Donec id elit non mi porta gravida at eget metus. Fusce dapibus, tellus ac cursus commodo,
                        tortor
                        mauris condimentum nibh, ut fermentum massa justo sit amet risus. Etiam porta sem malesuada
                        magna
                        mollis euismod. Donec sed odio dui. </p>

                    <p><a class="btn" href="#">More &raquo;</a></p>
                </div>
                <div class="span6">
                    <h2>Contact</h2>

                    <p>Donec id elit non mi porta gravida at eget metus. Fusce dapibus, tellus ac cursus commodo,
                        tortor
                        mauris condimentum nibh, ut fermentum massa justo sit amet risus. Etiam porta sem malesuada
                        magna
                        mollis euismod. Donec sed odio dui. </p>

                    <p><a class="btn" href="#">Email &raquo;</a></p>
                </div>

            </div>
            <hr>
            <!-- Example row of columns -->
            <div class="row<security:authorize ifAnyGranted="ROLE_USER">-fluid</security:authorize>">
                <div class="span12">
                    <h2>Current Statistics</h2>

                    <p>You'll be keen to know that there have been <span class="label label-info">4720</span> users of this
                        system who have logged in to
                        <span class="label label-info">203</span>
                        total sites, for a total of <span class="label label-info">6224</span> site approvals.</p>

                </div>
            </div>

        </div>
        <o:copyright/>
    </div>
</div>
<o:footer/>
