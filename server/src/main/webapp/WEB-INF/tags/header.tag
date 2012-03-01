<%@attribute name="title" required="false" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>

    <c:set var="url">${pageContext.request.requestURL}</c:set>
    <base href="${fn:substring(url, 0, fn:length(url) - fn:length(pageContext.request.requestURI))}${pageContext.request.contextPath}/" />

    <meta charset="utf-8">
    <title>OpenID Connect - ${title}</title>
    <meta name="description" content="">
    <meta name="author" content="">

    <!-- Le javascript -->
    <script src="http://code.jquery.com/jquery-1.7.min.js"></script>
    <script src="/resources/bootstrap/js/bootstrap-modal.js"></script>
    <script src="/resources/bootstrap/js/bootstrap-alerts.js"></script>
    <script src="/resources/bootstrap/js/bootstrap-twipsy.js"></script>
    <script src="/resources/bootstrap/js/bootstrap-popover.js"></script>
    <script src="/resources/bootstrap/js/bootstrap-dropdown.js"></script>
    <script src="/resources/bootstrap/js/bootstrap-scrollspy.js"></script>
    <script src="/resources/bootstrap/js/bootstrap-tabs.js"></script>
    <script src="/resources/bootstrap/js/bootstrap-buttons.js"></script>

    <script>$(function () {

    })</script>

    <!-- Le HTML5 shim, for IE6-8 support of HTML elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

    <!-- Le styles -->
    <link href="/resources/bootstrap/bootstrap.css" rel="stylesheet">
    <style type="text/css">
        body {
            padding-top: 60px;
        }

        .logo {
            background: url("/resources/images/openid_small.png") no-repeat left center;
            padding-left: 30px;
        }
    </style>

    <!-- Le fav and touch icons -->
    <link rel="shortcut icon" href="images/favicon.ico">
    <link rel="apple-touch-icon" href="images/apple-touch-icon.png">
    <link rel="apple-touch-icon" sizes="72x72" href="images/apple-touch-icon-72x72.png">
    <link rel="apple-touch-icon" sizes="114x114" href="images/apple-touch-icon-114x114.png">
</head>

<body>