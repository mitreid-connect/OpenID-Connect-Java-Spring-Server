<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags" %>

<o:header title="welcome"/>



<o:topbar/>
<div class="container-fluid">
    <div class="row-fluid">
        <o:sidebar/>
        <div class="span10">
            <div class="content span12">
                <o:breadcrumbs crumb="Manage Clients"/>
                <span id="content">
                    loading...
                </span>
                <o:copyright/>
            </div>
        </div>
    </div>
</div>
<o:footer js="resources/js/admin.js"/>
