<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags" %>

<div class="row">
    <div class="col-sm-6">
        <div id="yesform">
            <button id="yesbutton" name="yes" type="submit" class="btn btn-success btn-lg btn-block btn-primary"
                    onclick="$('#user_oauth_approval').attr('value', true);">
                <span>${langProps['yes']}</span>
            </button>
        </div>
    </div>
    <div class="col-sm-6">
        <div>
            <button id="nobutton" name="no" type="submit" class="btn btn-lg btn-default btn-block btn-no"
                    onclick="$('#user_oauth_approval').attr('value', false);">
                <span>${langProps['no']}</span>
            </button>
        </div>
    </div>
</div>