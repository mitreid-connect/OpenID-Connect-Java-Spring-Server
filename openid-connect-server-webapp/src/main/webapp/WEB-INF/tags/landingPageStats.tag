<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<h2><spring:message code="home.statistics.title"/></h2>

<p id="statsloader" class="muted"><spring:message code="home.statistics.loading"/></p>

<p id="stats">
    <spring:message code="home.statistics.number_users" arguments="?"/>
    <spring:message code="home.statistics.number_clients" arguments="?"/>
    <spring:message code="home.statistics.number_approvals" arguments="?"/>
</p>

<script type="text/javascript">
$(document).ready(function() {
		$('#stats').hide();
	    var base = $('base').attr('href');

        $.getJSON(base + 'api/stats/summary', function(data) {
        	var stats = data;
        	$('#userCount').html(stats.userCount);
        	$('#clientCount').html(stats.clientCount);
        	$('#approvalCount').html(stats.approvalCount);
        	$('#statsloader').hide();
        	$('#stats').show();
        	
        });
});
</script>