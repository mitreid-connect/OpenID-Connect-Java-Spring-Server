<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags" %>

<o:header title="welcome" />

<script type="text/html" id="client_tmpl">
    <tr>
        <td><#=name#></td>
        <td><#=redirectURL#></td>
        <td>
            <ul>
                <# for (var i in grantType) { #>
                    <li><#=grantType[i]#></li>
                    <# } #>
            </ul>
        </td>
        <td>
            <ul>
                <# for (var i in scope) { #>
                    <li><#=scope[i]#></li>
                    <# } #>
            </ul>
        </td>
        <td><#=authority#></td>
        <td><#=description#>
        </td>
        <td><input type="checkbox" "<#=(refreshTokens == 1 ? 'checked' : '')#> value="" id="" name="" disabled></td>
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

<div id="modal-from-dom" class="modal hide fade" style="width: 577px; max-height: none; top: 35%">
    <div class="modal-header">
        <a href="#" class="close">&times;</a>

        <h3>Edit Client</h3>
    </div>
    <div class="modal-body">

        <form>
            <fieldset>
                <!--<legend>OpenID Client</legend>-->
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
    <div class="modal-footer">
        <a href="#" class="btn primary">Save</a>
        <a href="#" class="btn secondary">Cancel</a>
    </div>
</div>

<o:topbar/>
<div class="container-fluid">
    <o:sidebar/>
    <div class="content">
        <!-- Main hero unit for a primary marketing message or call to action -->
        <div class="hero-unit">
            <h1>Welcome, User Name!</h1>

            <p>Can't remember your passwords? Tired of filling out registration forms?
                OpenID is a <strong>safe</strong>, <strong>faster</strong>, and <strong>easier</strong> way to log in to
                web sites.</p>

            <p><a class="btn primary large" href="http://openid.net/connect/">Learn more &raquo;</a></p>
        </div>
        <!-- Example row of columns -->
        <div class="row">
            <div class="span8">
                <h2>About</h2>

                <p>Donec id elit non mi porta gravida at eget metus. Fusce dapibus, tellus ac cursus commodo,
                    tortor
                    mauris condimentum nibh, ut fermentum massa justo sit amet risus. Etiam porta sem malesuada
                    magna
                    mollis euismod. Donec sed odio dui. </p>

                <p><a class="btn" href="#">More &raquo;</a></p>
            </div>
            <div class="span8">
                <h2>Contact</h2>

                <p>Donec id elit non mi porta gravida at eget metus. Fusce dapibus, tellus ac cursus commodo,
                    tortor
                    mauris condimentum nibh, ut fermentum massa justo sit amet risus. Etiam porta sem malesuada
                    magna
                    mollis euismod. Donec sed odio dui. </p>

                <p><a class="btn" href="#">Email &raquo;</a></p>
            </div>

        </div>
        <hr>
        <!-- Example row of columns -->
        <div class="row">
            <div class="span16">
                <h2>Current Statistics</h2>

                <p>You'll be keen to know that there have been <span class="label notice">4720</span> users of this
                    system who have logged in to
                    <span class="label notice">203</span>
                    total sites, for a total of <span class="label notice">6224</span> site approvals.</p>

            </div>
        </div>

        <o:copyright/>
    </div>
</div>
<o:footer/>
