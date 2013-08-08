<%@ taglib prefix="authz" uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags"%>
<o:header title="Log In" />
<script type="text/javascript">
<!--

$(document).ready(function() {
	$('#j_username').focus();
});

//-->
</script>
<o:topbar />
<div class="container main">

	<h1>Login with Username and Password</h1>

	<c:if test="${ param.error != null }">
		<div class="alert alert-error">The system was unable to log you in. Please try again.</div>
	</c:if>


<div class="row-fluid">
   <form action="<%=request.getContextPath()%>/j_spring_security_check" method="POST">
      <div class="span4 offset1 well">
         <div class="input-prepend"><span class="add-on"><i class="icon-user"></i></span><input type="text" placeholder="Username" autocorrect="off" autocapitalize="off" autocomplete="off" spellcheck="false" value="" id="j_username" name="j_username" class="span11"></div>
         <div class="input-prepend"><span class="add-on"><i class="icon-lock"></i></span><input type="password" placeholder="Password" autocorrect="off" autocapitalize="off" autocomplete="off" spellcheck="false" id="j_password" name="j_password" class="span11"></div>
         <div class="form-actions"><input type="submit" class="btn" value="Login" name="submit"></div>
      </div>
   </form>
</div>

</div>
<o:footer/>
