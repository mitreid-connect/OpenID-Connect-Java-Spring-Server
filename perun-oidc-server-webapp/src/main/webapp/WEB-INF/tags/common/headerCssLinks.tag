<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="cssLinks" required="true" type="java.util.ArrayList<java.lang.String>" %>

<c:forEach var="link" items="${cssLinks}">
    <link rel="stylesheet" type="text/css" href="${link}" />
</c:forEach>
