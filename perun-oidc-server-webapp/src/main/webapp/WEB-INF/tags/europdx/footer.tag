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
    <div class="row" style="margin: 0 auto; max-width: 1000px;">
        <div class="col-md-6" style="float: left">
            <img src="${samlResourcesURL}/module.php/europdx/res/img/eu_flag_128.png">
            <p>The EDIReX project has received funding from the European Union’s Horizon 2020 research and innovation programme, grant agreement no. #731105</p>
        </div>

        <div class="col-md-6" style="float: right;">
            <ul>
                <li>
                    <a href="http://www.twitter.com/EurOPDX"> Follow @EUROPDX</a>
                </li>
                <li>
                    <a href="https://europdx.eu/#"> TERMS OF USE</a>
                </li>
            </ul>
        </div>
    </div>

    <div class="row" style="text-align: center">
        <div class="col-md-12 copyright">
            <p> © 1991– 2019 | EuroPDX - <a href="mailto:contact@europdx.eu"> contact@europdx.eu </a></p>
        </div>
    </div>
</div>
