<%@attribute name="short" required="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:choose>
	<c:when test="${short == 'true'}">
		<!--  Display short version of About page -->
		<h2>About</h2>

        <p>This OpenID Connect service is built from the MITREid Connect Open Source project started by The MITRE Corporation.</p>

        <p><a class="btn" href="http://github.com/mitreid-connect/">More &raquo;</a></p>
    </c:when>
    <c:when test="${short == 'false' || empty short }">
    	<!-- Display long version of about page -->
    	<h2>About</h2>
    	<p> 
        This OpenID Connect service is built from the MITREid Connect Open Source project started by The MITRE Corporation.           
        </p>
        <p>
        More information about the project can be found on our GitHub page: <a href="http://github.com/mitreid-connect/">MTIREid Connect on GitHub</a>
        There, you can submit bug reports, give feedback, or even contribute code patches for additional features you'd like to see.
        </p>
    </c:when>
</c:choose>    
        