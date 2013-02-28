<%@attribute name="short" required="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:choose>
	<c:when test="${short == 'true'}">
		<!--  Display short version of Statistics page -->
		<h2>Current Statistics</h2>

        <p>There have been <span class="label label-info">${statsSummary["userCount"]}</span> users of this
            system who have logged in to <span class="label label-info">${statsSummary["clientCount"]}</span>
            total sites, for a total of <span class="label label-info">${statsSummary["approvalCount"]}</span> site approvals.</p>
    </c:when>
    <c:when test="${short == 'false' || empty short }">
    	<!-- Display long version of Statistics page -->
		<h2>Current Statistics</h2>

        <p>There have been <span class="label label-info">${statsSummary["userCount"]}</span> users of this
           system who have logged in to <span class="label label-info">${statsSummary["clientCount"]}</span>
           total sites, for a total of <span class="label label-info">${statsSummary["approvalCount"]}</span> site approvals.</p>
    </c:when>
</c:choose> 