<%@attribute name="js" required="false" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!-- Le javascript
================================================== -->
<!-- Placed at the end of the document so the pages load faster -->
<script type="text/javascript" src="resources/bootstrap2/js/bootstrap.js"></script>
<script type="text/javascript" src="resources/js/lib/underscore.js"></script>
<script type="text/javascript" src="resources/js/lib/backbone.js"></script>
<script type="text/javascript" src="resources/js/lib/purl.js"></script>
<c:if test="${js != null && js != ''}">
<script type="text/javascript" src="resources/js/client.js"></script>
<script type="text/javascript" src="resources/js/grant.js"></script>
<script type="text/javascript" src="resources/js/scope.js"></script>
<script type="text/javascript" src="resources/js/whitelist.js"></script>
<script type="text/javascript" src="resources/js/admin.js"></script>
</c:if>
</body>
</html>