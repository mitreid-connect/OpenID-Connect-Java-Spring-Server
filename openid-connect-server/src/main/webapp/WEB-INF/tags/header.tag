<%@attribute name="title" required="false"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ tag import="com.google.gson.Gson" %>
<!DOCTYPE html>
<html lang="en">
<head>

    <base href="${config.issuer}">

    <meta charset="utf-8">
    <title>${config.topbarTitle} - ${title}</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <!-- Le styles -->
    <link href="resources/bootstrap2/css/bootstrap.min.css" rel="stylesheet">
    <style type="text/css">

      html,
      body {
        height: 100%;
        /* The html and body elements cannot have any padding or margin. */
      }

        .sidebar-nav {
            padding: 9px 0;
        }

        h1,label {
            text-shadow: 1px 1px 1px #FFFFFF;
        }

        .brand {
            padding-left: 35px !important;
        }

      /* Wrapper for page content to push down footer */
      #wrap {
        min-height: 100%;
        height: auto !important;
        height: 100%;
        /* Negative indent footer by it's height */
        margin: 0 auto -60px;
      }

      /* Set the fixed height of the footer here */
      #push,
      #footer {
        min-height: 60px;
      }
      #footer {
        background-color: #f5f5f5;
      }

      .main {
        padding-top: 60px;
      }

      .credit {
        margin: 20px 0;
      }
	
	  .inputError {
	  	border: 1px solid #b94a48 !important;
	  }
	
	  a.brand {
	  	background: url('${config.logoImageUrl}') no-repeat scroll 7px 7px transparent;
	  }
    </style>
    <link href="resources/bootstrap2/css/bootstrap-responsive.css" rel="stylesheet">
	<style type="text/css">
		@media (min-width: 768px) and (max-width: 979px) {
	        .main {
	        	padding-top: 0px;
	        }
	
	    }
	    
	    @media (max-width: 767px) {
	        #footer {
	          margin-left: -20px;
	          margin-right: -20px;
	          padding-left: 20px;
	          padding-right: 20px;
	        }
	    }
	
	
	</style>

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

    <!-- Le fav and touch icons -->
    <link rel="shortcut icon" href="../bootstrap2/ico/favicon.ico">
    <link rel="apple-touch-icon-precomposed" sizes="114x114" href="../bootstrap2/ico/apple-touch-icon-114-precomposed.png">
    <link rel="apple-touch-icon-precomposed" sizes="72x72" href="../bootstrap2/ico/apple-touch-icon-72-precomposed.png">
    <link rel="apple-touch-icon-precomposed" href="../bootstrap2/ico/apple-touch-icon-57-precomposed.png">

    <!-- Load jQuery up here so that we can use in-page functions -->
    <script type="text/javascript" src="resources/js/lib/jquery.js"></script>
    <script type="text/javascript">
    	// safely set the title of the application
    	function setPageTitle(title) {
    		document.title = "${config.topbarTitle} - " + title;
    	}
    	
		// get the info of the current user, if available (null otherwise)
    	function getUserInfo() {
    		return ${userInfoJson};
    	}
		
		// get the authorities of the current user, if available (null otherwise)
		function getUserAuthorities() {
			return ${userAuthorities};
		}
		
		// is the current user an admin?
		function isAdmin() {
			var auth = getUserAuthorities();
			if (auth && _.contains(auth, "ROLE_ADMIN")) {
				return true;
			} else {
				return false;
			}
		}
    </script>    
</head>

<body>

	<div id="modalAlert" class="modal hide fade">
		<div class="alert alert-error">
			<strong>Warning!</strong>
			<div class="modal-body"></div>
		</div>
		<div class="modal-footer">
			<button class="btn primary" type="button"
				onclick="$('#modalAlert').modal('hide');">OK</button>
		</div>
	</div>

<div id="wrap">
