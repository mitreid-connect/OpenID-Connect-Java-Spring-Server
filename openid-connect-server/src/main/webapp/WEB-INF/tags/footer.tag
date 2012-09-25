<%@attribute name="js" required="false" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!-- Le javascript
================================================== -->
<!-- Placed at the end of the document so the pages load faster -->
<script type="text/javascript" src="resources/bootstrap2/js/bootstrap.min.js"></script>
<script type="text/javascript" src="resources/js/underscore-min.js"></script>
<script type="text/javascript" src="resources/js/backbone-min.js"></script>
<script type="text/javascript" src="resources/js/backbone.validations.js"></script>
<c:if test="${js != null || js != ''}">
<script type="text/javascript" src="${js}"></script>
</c:if>
</body>
</html>