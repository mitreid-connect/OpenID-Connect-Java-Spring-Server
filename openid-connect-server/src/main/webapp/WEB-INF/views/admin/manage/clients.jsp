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
            <button data-controls-modal="modal-from-dom" data-backdrop="true" data-keyboard="true"
                    class="btn">edit
            </button>
        </td>
        <td>
            <button class="btn danger">delete</button>
        </td>
    </tr>
</script>


<div style="display: none;">

    <form>
        <fieldset>
            <legend>OpenID Client</legend>
            <div class="clearfix">
                <label for="xlInput">Client Name</label>

                <div class="input">
                    <input type="text" size="30" name="xlInput" id="xlInput" class="xlarge">
                </div>
            </div>

            <div class="clearfix">
                <label for="prependedInput">Redirect Url</label>

                <div class="input">
                    <div class="input-prepend">
                        <span class="add-on">http://</span>
                        <input type="text" size="16" name="prependedInput" id="prependedInput" class="medium">
                    </div>
                    <span class="help-block">Url to be redirected</span>
                </div>
            </div>

            <div class="clearfix">
                <label id="optionsCheckboxes">Grant Types:</label>

                <div class="input">
                    <ul class="inputs-list">
                        <li>
                            <label>
                                <input type="checkbox" value="option1" name="optionsCheckboxes">
                                <span>Grant Type Blah</span>
                            </label>
                        </li>
                        <li>
                            <label>
                                <input type="checkbox" value="option2" name="optionsCheckboxes">
                                <span>Grant Type Blah</span>
                            </label>
                        </li>
                        <li>
                            <label>
                                <input type="checkbox" value="option2" name="optionsCheckboxes">
                                <span>Grant Type Blah</span>
                            </label>
                        </li>
                    </ul>
              <span class="help-block">
                <strong>Note:</strong> Grant type help text.
              </span>
                </div>
            </div>

            <div class="clearfix">
                <label for="textarea2">Scope</label>

                <div class="input">
                    <textarea rows="3" name="textarea2" id="textarea2" class="xlarge">email,first name</textarea>
              <span class="help-block">
                Please enter scopes separated by commas
              </span>
                </div>
            </div>

            <div class="clearfix">
                <label for="normalSelect">Authority</label>

                <div class="input">
                    <select id="normalSelect" name="normalSelect">
                        <option>My Authority Option 1</option>
                        <option>My Authority Option 2</option>
                    </select>
                </div>
            </div>

        </fieldset>

        <div class="clearfix">
            <label for="form-description">Description</label>

            <div class="input">
                <input type="text" size="30" name="form-description" id="form-description" class="xlarge">
            </div>
        </div>

        <div class="clearfix">
            <label id="form-allow-tokens">Allow refresh tokens?</label>

            <div class="input">
                <ul class="inputs-list">
                    <li>
                        <label>
                            <input type="checkbox" value="option1" name="form-allow-tokens">
                            <span>&nbsp;</span>
                        </label>
                    </li>
                </ul>
              <span class="help-block">
                <strong>Note:</strong> Labels surround all the options for much larger click areas and a more usable form.
              </span>
            </div>
        </div>

    </form>
</div>


<o:topbar/>
<div class="container-fluid">
    <o:sidebar/>

    <div class="content">
        <o:breadcrumbs crumb="Manage Clients"/>

        <div class="well">
            <a class="btn small primary" href="#">New Client</a>
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
            <a class="btn small primary" href="#">New Client</a>
        </div>
        <o:copyright/>
    </div>
</div>
<o:footer/>
