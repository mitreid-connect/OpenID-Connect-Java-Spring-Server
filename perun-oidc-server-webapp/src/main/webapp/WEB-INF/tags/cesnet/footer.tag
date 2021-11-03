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
    <footer>
        <div class="container">
            <div class="row">
                <div class="col-md-4 logo">
                    <a href="http://www.cesnet.cz/">
                        <img src="${samlResourcesURL}/module.php/cesnet/res/img/logo-cesnet.png" alt="CESNET logo" style="width: 250px;">
                    </a>
                </div>
                <div class="col-md-8">
                    <div class="row">
                        <div class="col col-sm-6">
                            <h2>${langProps['footer_other_projects']}</h2>
                            <ul>
                                <li><a href="http://www.cesnet.cz/wp-content/uploads/2014/04/CzechLight-family_Posp%C3%ADchal.pdf">CzechLight</a></li>
                                <li><a href="http://www.ultragrid.cz/en">UltraGrid</a></li>
                                <li><a href="http://www.4kgateway.com/">4k Gateway</a></li>
                                <li><a href="http://shongo.cesnet.cz/">Shongo</a></li>
                                <li><a href="http://www.cesnet.cz/sluzby/sledovani-provozu-site/sledovani-infrastruktury/">FTAS a G3</a></li>
                                <li><a href="https://www.liberouter.org/">Librerouter</a></li>
                            </ul>
                        </div>
                        <div class="col col-sm-6">
                            <h2>${langProps['footer_helpdesk']}</h2>
                            TEL: +420 224 352 994<br>
                            GSM: +420 602 252 531<br>
                            FAX: +420 224 313 211<br>
                            <a href="mailto:perun@cesnet.cz">perun@cesnet.cz</a>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col col-sm-12 copyright">
                    &copy; 1991–<fmt:formatDate value="${date}" pattern="yyyy" /> | CESNET, z. s. p. o.
                </div>
            </div>
        </div>
    </footer>
</div>
