<%@ tag pageEncoding="UTF-8" import="cz.muni.ics.oidc.server.configurations.PerunOidcConfig" trimDirectiveWhitespaces="true" %>
<%@ tag import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:if test="${ config.heartMode }"><span class="pull-left"><img src="resources/images/heart_mode.png" alt="HEART Mode" title="This server is running in HEART Compliance Mode" /></span> </c:if>
<%
    PerunOidcConfig perunOidcConfig = WebApplicationContextUtils.getWebApplicationContext(application).getBean("perunOidcConfig", PerunOidcConfig.class);
%>
Powered by
<a href="https://github.com/CESNET/perun-mitreid">Perun MITREid</a> <span class="label"><%=perunOidcConfig.getPerunOIDCVersion()%></span>
<span class="pull-right">&copy; 2017 The MIT Internet Trust Consortium.</span>.
