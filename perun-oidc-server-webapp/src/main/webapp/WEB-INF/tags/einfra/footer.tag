<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ attribute name="js" required="false"%>
<%@ attribute name="baseURL" required="true"%>
<%@ attribute name="samlResourcesURL" required="true"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/common" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<jsp:useBean id="date" class="java.util.Date" />

<div id="footer">
    <footer>
        <div class="container">
            <div class="row">
                <div class="col-md-8 col-md-push-4 col-xs-12">
                    <div class="row">
                        <div class="col col-sm-6 col-xs-12">
                            <h2><spring:message code="footer_other_links"/></h2>
                            <ul>
                                <li>
                                    <spring:message code="einfra_link" var="einfra_link"/>
                                    <a target="_blank" href="${einfra_link}"><spring:message code="einfra_name"/></a>
                                </li>
                                <li>
                                    <spring:message code="einfra_link" var="cesnet_link"/>
                                    <a target="_blank" href="${cesnet_link}"><spring:message code="cesnet_name"/></a>
                                </li>
                                <li>
                                    <spring:message code="cerit_link" var="cerit_link"/>
                                    <a target="_blank" href="${cerit_link}"><spring:message code="cerit_name"/></a>
                                </li>
                                <li>
                                    <spring:message code="it4i_link" var="it4i_link"/>
                                    <a target="_blank" href="${it4i_link}"><spring:message code="it4i_name"/></a>
                                </li>
                            </ul>
                        </div>
                        <div class="col col-sm-6 col-xs-12">
                            <h2><spring:message code="footer_helpdesk"/></h2>
                            TEL: +420 234 680 222<br>
                            GSM: +420 602 252 531<br>
                            <a href="mailto:support@e-infra.cz">support@e-infra.cz</a>
                        </div>
                    </div>
                </div>
                <div class="col-md-4 col-md-pull-8 col-xs-12">
                    <div class="row">
                        <div id="footer-logo-cesnet" class="col-md-12 col-sm-4 col-xs-12 footer-logo-wrapper">
                            <img alt="CESNET logo" src="${samlResourcesURL}/module.php/cesnet/res/img/cesnet.svg"/>
                        </div>
                        <div id="footer-logo-cerit" class="col-md-12 col-sm-4 col-xs-12 footer-logo-wrapper">
                            <img alt="CERIT-SC logo" src="${samlResourcesURL}/module.php/cesnet/res/img/cerit.svg"/>
                        </div>
                        <div id="footer-logo-it4i" class="col-md-12 col-sm-4 col-xs-12 footer-logo-wrapper">
                            <img alt="IT4I logo" src="${samlResourcesURL}/module.php/cesnet/res/img/it4i.svg"/>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col col-sm-12 copyright">
                    &copy; 2021 | <a target="_blank" href="${einfra_link}"><spring:message code="einfra_name"/></a>
                    <%-- USABLE FROM 2022 --%>
                    <%--&copy; 2021-<fmt:formatDate value="${date}" pattern="yyyy" /> | <a target="_blank" href="<spring:message code="einfra_link"/>"><spring:message code="einfra_name"/></a>--%>
                </div>
            </div>
        </div>
    </footer>
</div>
