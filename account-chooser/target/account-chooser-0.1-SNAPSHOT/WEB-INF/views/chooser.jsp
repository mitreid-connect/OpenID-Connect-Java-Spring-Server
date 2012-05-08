<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@page import="org.mitre.account_chooser.OIDCServer"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Iterator"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:useBean id="servers" type="org.mitre.account_chooser.OIDCServers"
	scope="request" />

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>Account Chooser</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="Account Chooser GUI">
<meta name="author" content="nemonik">
<link href="resources/bootstrap/css/bootstrap.css" rel="stylesheet">
<link href="resources/bootstrap/css/bootstrap-responsive.css"
	rel="stylesheet">
<link href="resources/bootstrap/css/docs.css" rel="stylesheet">

<style>
body {
	padding-top: 60px;
	/* 60px to make the container go all the way to the bottom of the topbar */
}
</style>
<!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
<!--[if lt IE 9]>
<script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
<![endif]-->
</head>
<body>
	<div class="container">
		<div class="row">
			<div class="span12">
				<form class="form-horizontal" action="selected" method="get">
					<div class="control-group">
						<label class="control-label" for="select01">Account:</label>
						<div class="controls">
							<select name="alias">
								<%
									Map map = servers.getServers();
									Iterator entries = map.entrySet().iterator();

									while (entries.hasNext()) {
										Map.Entry entry = (Map.Entry) entries.next();

										String alias = (String) entry.getKey();
										OIDCServer server = (OIDCServer) entry.getValue();
								%>
								<option value="<%= alias %>"><%= server.getName() %></option>
								<%
									}
								%>
							</select>
							<p class="help-block">Select the Account you'd like to authenticate with.</p>
						</div>
					</div>
					<div class="control-group">
						<div class="controls">
							<input name="redirect_uri" type="hidden" value="<c:out value="${redirect_uri}"/>">
						</div>
					</div>
					<div class="form-actions">
						<button class="btn btn-primary" type="submit">Submit</button>
						<button class="btn">Cancel</button>
					</div>
				</form>
			</div>
		</div>
	</div>
	<!--/container-->

	<!-- Placed at the end of the document so the pages load faster -->
	<script src="resources/bootstrap/js/jquery.js"></script>
	<script src="resources/bootstrap/js/bootstrap-transition.js"></script>
	<script src="resources/bootstrap/js/bootstrap-alert.js"></script>

	<script src="resources/bootstrap/js/bootstrap-modal.js"></script>
	<script src="resources/bootstrap/js/bootstrap-dropdown.js"></script>
	<script src="resources/bootstrap/js/bootstrap-scrollspy.js"></script>
	<script src="resources/bootstrap/js/bootstrap-tab.js"></script>
	<script src="resources/bootstrap/js/bootstrap-tooltip.js"></script>
	<script src="resources/bootstrap/js/bootstrap-popover.js"></script>

	<script src="resources/bootstrap/js/bootstrap-button.js"></script>
	<script src="resources/bootstrap/js/bootstrap-collapse.js"></script>
	<script src="resources/bootstrap/js/bootstrap-carousel.js"></script>
	<script src="resources/bootstrap/js/bootstrap-typeahead.js"></script>

	<script type="text/javascript" language="JavaScript">
		// <![CDATA [
		  
		// javascript secific to the page would go here, if I had any...
		  
		// ]]>
	</script>

</body>
</html>
