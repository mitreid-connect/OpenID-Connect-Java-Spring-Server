<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags" %>

<o:header title="welcome"/>



<o:topbar/>
<div class="container-fluid">
    <div class="row-fluid">
        <o:sidebar/>
        <div class="span10">
            <div class="content">
                <o:breadcrumbs crumb="Manage Clients"/>

                <div class="well">
                    <a class="btn btn-small btn-primary" href="#">New Client</a>
                </div>

                <table id="client-table" class="table">
                    <thead>
                    <tr>
                        <th>Name</th>
                        <th>Redirect URL</th>
                        <th>Grant Types</th>
                        <th>Scope</th>
                        <th>Authority</th>
                        <th>Description</th>
                        <th>Refresh Tokens</th>
                        <th class="span1"></th>
                        <th class="span1"></th>
                    </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>

                <div class="well">
                    <a class="btn btn-small btn-primary" href="#">New Client</a>
                </div>
                <o:copyright/>
            </div>
        </div>
    </div>
</div>
<o:footer/>
