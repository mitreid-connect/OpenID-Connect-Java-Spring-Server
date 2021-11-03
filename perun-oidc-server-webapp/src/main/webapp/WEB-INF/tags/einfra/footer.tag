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
                    <img src="${samlResourcesURL}/module.php/cesnet/res/img/footer_logo.png">
                </div>
                <div class="col-md-8">
                    <div class="row">
                        <div class="col col-sm-6">
                            <h2>${langProps['footer_other_links']}</h2>
                            <ul>
                                <li>
                                    <a target="_blank" href="${langProps['einfra_link']}">${langProps['einfra_name']}</a>
                                </li>
                                <li>
                                    <a target="_blank" href="${langProps['cesnet_link']}">${langProps['cesnet_name']}</a>
                                </li>
                                <li>
                                    <a target="_blank" href="${langProps['cerit_link']}">${langProps['cerit_name']}</a>
                                </li>
                                <li>
                                    <a target="_blank" href="${langProps['it4i_link']}">${langProps['it4i_name']}</a>
                                </li>
                                <li>
                                    <a target="_blank" href="${langProps['data_processing_link']}">${langProps['data_processing_name']}</a>
                                </li>
                            </ul>
                        </div>
                        <div class="col col-sm-6">
                            <h2>${langProps['footer_helpdesk']}</h2>
                            TEL: +420 234 680 222<br>
                            GSM: +420 602 252 531<br>
                            <a href="mailto:support@e-infra.cz">support@e-infra.cz</a>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col col-sm-12 copyright">
                    &copy; 2021 | <a target="_blank" href="${langProps['einfra_link']}">${langProps['einfra_name']}</a>
                    <%-- USABLE FROM 2022 --%>
                    <%--&copy; 2021-<fmt:formatDate value="${date}" pattern="yyyy" /> | <a target="_blank" href="${langProps['einfra_link']}">${langProps['einfra_name']}</a>--%>
                </div>
            </div>
        </div>
    </footer>
</div>
