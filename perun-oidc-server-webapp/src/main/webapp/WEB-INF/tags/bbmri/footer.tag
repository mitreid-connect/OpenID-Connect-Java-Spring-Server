<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ attribute name="js" required="false"%>
<%@ attribute name="baseURL" required="true"%>
<%@ attribute name="samlResourcesURL" required="true"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/common" %>
<jsp:useBean id="date" class="java.util.Date" />

<div id="footer">
    <div style="margin: 0px auto; max-width: 1000px;">
        <div style="float: left;">
            <img src="${samlResourcesURL}/module.php/bbmri/res/img/BBMRI-ERIC-gateway-for-health_216.png" alt="BBMRI-ERIC Logo">
        </div>

        <div style="float: left;">
            <p>BBMRI-ERIC, Neue Stiftingtalstrasse 2/B/6, 8010 Graz, Austria
                &nbsp; &nbsp; +43 316 34 99 17-0 &nbsp;
                <a href="mailto:contact@bbmri-eric.eu">contact@bbmri-eric.eu</a>
            </p>
            <p>Copyright &copy; BBMRI-ERIC <fmt:formatDate value="${date}" pattern="yyyy" /></p>
        </div>
    </div>
</div><!-- #footer -->
