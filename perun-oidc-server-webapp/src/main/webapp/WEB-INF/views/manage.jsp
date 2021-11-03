<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="o" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:message code="manage.title" var="title"/>
<o:header title="${title}"/>
<o:topbar pageName="Home" />

<!-- Modal dialogue for management UI -->
<div id="modalAlert" class="modal hide fade" tabindex="-1" role="dialog">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal">&times;</button>
		<h3 id="modalAlertLabel"></h3>
	</div>
	<div class="modal-body"></div>
	<div class="modal-footer">
		<button class="btn btn-primary" data-dismiss="modal"><spring:message code="manage.ok"/></button>
	</div>
</div>

<div class="container-fluid main">
    <div class="row-fluid">
        <o:sidebar/>
        <div class="span10">
            <div class="content span12">
				<div id="breadcrumbs"></div>
				<div id="loadingbox" class="sheet hide fade" data-sheet-parent="#breadcrumbs">
					<div class="sheet-body">
			                <p><spring:message code="manage.loading"/>:</p>
			                <p><span id="loading"></span></p>
	                </div>
	            </div>
                <div id="content">
                	<div class="well">
	                	<div><h3><spring:message code="manage.loading"/>...</h3></div>
	               	    <div class="progress progress-striped active">
							<div class="bar" style="width: 100%;"></div>
						</div>
					</div>
                </div>
            </div>
        </div>
    </div>
</div>

<o:footer js="resources/js/admin.js" />