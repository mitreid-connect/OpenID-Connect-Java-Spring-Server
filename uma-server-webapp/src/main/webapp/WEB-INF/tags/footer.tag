<%@ attribute name="js" required="false"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags"%>
<div id="push"></div>
</div>
<!-- end #wrap -->
<div id="footer">
	<div class="container">
		<p class="muted credit">
			<o:copyright />
		</p>
	</div>
</div>
<!-- javascript
================================================== -->
<!-- Placed at the end of the document so the pages load faster -->
<script type="text/javascript" src="resources/bootstrap2/js/bootstrap.js"></script>
<script type="text/javascript" src="resources/js/lib/underscore.js"></script>
<script type="text/javascript" src="resources/js/lib/backbone.js"></script>
<script type="text/javascript" src="resources/js/lib/purl.js"></script>
<script type="text/javascript" src="resources/js/lib/bootstrapx-clickover.js"></script>
<script type="text/javascript" src="resources/js/lib/bootstrap-sheet.js"></script>
<script type="text/javascript" src="resources/js/lib/bootpag.js"></script>
<c:if test="${js != null && js != ''}">
	<script type="text/javascript" src="resources/js/client.js"></script>
	<script type="text/javascript" src="resources/js/grant.js"></script>
	<script type="text/javascript" src="resources/js/scope.js"></script>
	<script type="text/javascript" src="resources/js/whitelist.js"></script>
	<script type="text/javascript" src="resources/js/dynreg.js"></script>
	<script type="text/javascript" src="resources/js/rsreg.js"></script>
	<script type="text/javascript" src="resources/js/token.js"></script>
	<script type="text/javascript" src="resources/js/blacklist.js"></script>
	<script type="text/javascript" src="resources/js/policy.js"></script>
	<script type="text/javascript" src="resources/js/admin.js"></script>
</c:if>
<script type="text/javascript" src="resources/js/lib/retina.js"></script>
</body>
</html>
