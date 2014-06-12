<h2>Current Statistics</h2>

<p id="statsloader" class="muted">Loading statistics...</p>

<p id="stats">There have been 
	<span class="label label-info" id="userCount">?</span> <span id="userCountLabel">user</span> 
	of this system who have authorized
	<span class="label label-info" id="clientCount">?</span> <span id="clientCountLabel">application</span>, 
	with a total of 
	<span class="label label-info" id="approvalCount">?</span> site <span id="approvalCountLabel">approval</span>.</p>

<script type="text/javascript">

$(document).ready(function() {

		$('#stats').hide();
	
	    var base = $('base').attr('href');
        $.getJSON(base + 'api/stats/summary', function(data) {
        	var stats = data;
        	
        	$('#userCount').html(stats.userCount);
        	if (stats.userCount != 1) {
        		$('#userCountLabel').append('s');
        	}
        	$('#clientCount').html(stats.clientCount);
        	if (stats.clientCount != 1) {
        		$('#clientCountLabel').append('s');
        	}
        	$('#approvalCount').html(stats.approvalCount);
        	if (stats.approvalCount != 1) {
        		$('#approvalCountLabel').append('s');
        	}
        	
        	
        	$('#statsloader').hide();
        	$('#stats').show();
        	
        });


});

</script>