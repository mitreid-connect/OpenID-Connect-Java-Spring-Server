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
<o:topbar title="${topbarTitle}"/>
<div class="container">

<h1>Login with Username and Password</h1>

<c:if test="${ param.error != null }">
<div class="alert alert-error">The system was unable to log you in. Please try again.</div>
</c:if>

<!-- TODO: this is an ugly form but it gets the job done -->

<form action="<%=request.getContextPath()%>/j_spring_security_check" method="POST" class="well">
 <fieldset>
    <div class="input-prepend"><span class="add-on"><i class="icon-user"></i></span><input type="text" name="j_username" id="j_username" value="" spellcheck="false" autocomplete="off" autocapitalize="off" autocorrect="off" placeholder="Username"></div>
    <div class="input-prepend"><span class="add-on"><i class="icon-lock"></i></span><input type="password" name="j_password" id="j_password" spellcheck="false" autocomplete="off" autocapitalize="off" autocorrect="off" placeholder="Password"></div>
    <div class="form-actions"><input type="submit" name="submit" value="Login" class="btn"></div>
  </fieldset>
</form>

<o:copyright/>
</div>
<o:footer/>