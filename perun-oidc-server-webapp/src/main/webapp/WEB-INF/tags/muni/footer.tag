<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>

        </div>
    </div>
</main>

<footer class="footer">
    <div class="row-main">
        <p class="footer__copyrights">
            <spring:message code="masaryk_university"/><br />
            <spring:message code="service"/>${" "}<a href="https://it.muni.cz/sluzby/jednotne-prihlaseni-na-muni" target="_blank"><spring:message code="unified_login"/></a>${" "}<spring:message code="provided"/>${" "}<a href="https://www.ics.muni.cz" target="_blank"><spring:message code="ics"/></a>
        </p>
    </div>
</footer>

</body>
</html>
