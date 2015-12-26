<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags"%>
<o:header title="Login"/>
<o:topbar />
<div class="container-fluid main">
	<div class="row-fluid">
		<div class="span10 offset1">

			<h2>Log In</h2>
			
			<p>Enter your email address to log in</p> 
			
			<div class="well">
				<div class="row-fluid">
	
					<div class="span8">
						<form action="openid_connect_login" method="get">
							<input type="text" class="input-xxlarge" name="identifier" id="identifier" />
							<input type="submit" value="Log In" />
						</form>
					</div>
	
				</div>

		</div>
	</div>
</div>

<script type="text/javascript">
	$(document).ready(function () {
		$('#localhost').on('click', function() {
			$('#identifier').val('http://localhost:8080/openid-connect-server-webapp/');
		});
		$('#mitreidorg').on('click', function() {
			$('#identifier').val('user@mitreid.org');
		});
		
	});
</script>
<o:footer />