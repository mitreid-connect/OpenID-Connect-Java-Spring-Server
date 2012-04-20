<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags" %>

<o:header title="welcome"/>

<script type="text/html" id="client_tmpl">
    <tr>
        <td>
            <#=name#>
        </td>
        <td>
            <#=redirectURL#>
        </td>
        <td>
            <ul>
                <# for (var i in grantType) { #>
                    <li>
                        <#=grantType[i]#>
                    </li>
                    <# } #>
            </ul>
        </td>
        <td>
            <ul>
                <# for (var i in scope) { #>
                    <li>
                        <#=scope[i]#>
                    </li>
                    <# } #>
            </ul>
        </td>
        <td>
            <#=authority#>
        </td>
        <td>
            <#=description#>
        </td>
        <td><input type="checkbox" "
            <#=(refreshTokens == 1 ? 'checked' : '')#> value="" id="" name="" disabled>
        </td>
        <td>
            <button class="btn">edit</button>
        </td>
        <td>
            <button class="btn btn-danger">delete</button>
        </td>
    </tr>
</script>


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

                <table id="client-table">
                    <thead>
                    <tr>
                        <th>Name</th>
                        <th>Redirect URL</th>
                        <th>Grant Types</th>
                        <th>Scope</th>
                        <th>Authority</th>
                        <th class="span8">Description</th>
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
