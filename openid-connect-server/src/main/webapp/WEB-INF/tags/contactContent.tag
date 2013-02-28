<%@attribute name="short" required="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:choose>
	<c:when test="${short == 'true'}">
		<!--  Display short version of Contact page -->
		<h2>Contact</h2>

        <p>For more information or support, contact the administrators of this system.</p>

        <p><a class="btn" href="mailto:idp@example.com?Subject=OpenID Connect">Email &raquo;</a></p>
    </c:when>
    <c:when test="${short == 'false' || empty short }">
    	<!-- Display long version of Contact page -->
		<h2>Contact</h2>    
        <p>    
        For general assistance, email Bob at <a href="mailto:email@address.com?Subject=OIDC%20Server%20Assistance">email@address.com</a>.
        To offer feedback, email Sue at <a href="mailto:email@address.com?Subject=OIDC%20Server%20Feedback">email@address.com</a>.  
        To report a system failure or bug report, email Joe at <a href="mailto:email@address.com?Subject=OIDC%20Server%20Failure">email@address.com</a>. 
        </p>
    </c:when>
</c:choose>  