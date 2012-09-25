<%@ taglib prefix="authz" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags" %>
<o:header title="Log In"/>
<script type="text/javascript">
<!--

$(document).ready(function() {
	$('#j_username').focus();
});

//-->
</script>
<o:topbar/>
<div class="container">

<h1>Login with Username and Password</h1>

<!-- TODO: this is an ugly form but it gets the job done -->

<form action="<%=request.getContextPath()%>/j_spring_security_check" method="POST" class="well">
 <fieldset>
    <div class="input-prepend"><span class="add-on">Username:</span><input name="j_username" id="j_username" value="" type="text"></div>
    <div class="input-prepend"><span class="add-on">Password:</span><input name="j_password" id="j_password" type="password"></div>
    <div class="form-actions"><input name="submit" value="Login" class="btn" type="submit"></div>
  </fieldset>
</form>

<o:copyright/>
</div>
<o:footer/>