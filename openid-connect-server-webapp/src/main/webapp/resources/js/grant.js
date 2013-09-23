/*******************************************************************************
 * Copyright 2013 The MITRE Corporation 
 *   and the MIT Kerberos and Internet Trust Consortium
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
var ApprovedSiteModel = Backbone.Model.extend({
	idAttribute: 'id',
	
	initialize: function() { },
	
	urlRoot: 'api/approved'
	
});

var ApprovedSiteCollection = Backbone.Collection.extend({
	initialize: function() { },

	model: ApprovedSiteModel,
	url: 'api/approved'
});


var ApprovedSiteListView = Backbone.View.extend({
	tagName: 'span',
	
	initialize:function() { },
	
	events: {
		"click .refresh-table":"refreshTable"
	},
	
	render:function (eventName) {
		$(this.el).html($('#tmpl-grant-table').html());
		
		_.each(this.model.models, function(approvedSite) {
			// look up client
			var client = app.clientList.getByClientId(approvedSite.get('clientId'));
			
			if (client != null) {
				
				if (approvedSite.get('whitelistedSite') != null) {
					$('#grant-whitelist-table', this.el).append(new ApprovedSiteView({model: approvedSite, client: client}).render().el);
				} else {
					$('#grant-table', this.el).append(new ApprovedSiteView({model: approvedSite, client: client}).render().el);
				}
				
			}
			
		}, this);
		
		this.togglePlaceholder();
		
		return this;
	},
	
	togglePlaceholder:function() {
		// count the whitelisted and non-whitelisted entries
		var wl = 0;
		var gr = 0;
		for (var i = 0; i < this.model.length; i++) {
			if (this.model.at(i).get('whitelistedSite') != null) {
				wl += 1;
			} else {
				gr += 1;
			}
		}
		
		if (wl > 0) {
			$('#grant-whitelist-table', this.el).show();
			$('#grant-whitelist-table-empty', this.el).hide();
		} else {
			$('#grant-whitelist-table', this.el).hide();
			$('#grant-whitelist-table-empty', this.el).show();
		}
		if (gr > 0) {
			$('#grant-table', this.el).show();
			$('#grant-table-empty', this.el).hide();
		} else {
			$('#grant-table', this.el).hide();
			$('#grant-table-empty', this.el).show();
		}
	},
	
    refreshTable:function() {
    	var _self = this;
    	this.model.fetch({
    		success: function() {
    			_self.render();
    		}
    	});
    }

});

var ApprovedSiteView = Backbone.View.extend({
	tagName: 'tr',
	
	initialize: function() {
		if (!this.template) {
			this.template = _.template($('#tmpl-grant').html());
		}
        if (!this.scopeTemplate) {
        	this.scopeTemplate = _.template($('#tmpl-scope-list').html());
        }

	},

	render: function() {
		
		var creationDate = this.model.get("creationDate");
		var accessDate = this.model.get("accessDate");
		var timeoutDate = this.model.get("timeoutDate");
		
		if (creationDate == null || !moment(creationDate).isValid()) {
			creationDate = "Unknown";
		} else {
			creationDate = moment(creationDate);
			if (moment().diff(creationDate, 'months') < 6) {
				creationDate = creationDate.fromNow();
			} else {
				creationDate = creationDate.format("MMMM Do, YYYY");
			}
		}
		
		if (accessDate == null || !moment(accessDate).isValid()) {
			accessDate = "Unknown";
		} else {
			accessDate = moment(accessDate);
			if (moment().diff(accessDate, 'months') < 6) {
				accessDate = accessDate.fromNow();
			} else {
				accessDate = accessDate.format("MMMM Do, YYYY");
			}
		}
		
		if (timeoutDate == null) {
			timeoutDate = "Never";
		} else if (!moment(timeoutDate).isValid()) {
			timeoutDate = "Unknown";
		} else {
			timeoutDate = moment(timeoutDate).calendar();
		}
		
		var formattedDate = {creationDate: creationDate, accessDate: accessDate, timeoutDate: timeoutDate};
		
		var json = {grant: this.model.toJSON(), client: this.options.client.toJSON(), formattedDate: formattedDate};
		
		this.$el.html(this.template(json));

        $('.scope-list', this.el).html(this.scopeTemplate({scopes: this.options.client.get('scope'), systemScopes: app.systemScopeList}));
        
        this.$('.dynamically-registered').tooltip({title: 'This client was dynamically registered'});
        this.$('.whitelisted-site').tooltip({title: 'This site was whitelisted by an adminstrator'});
        
		return this;
	},
	
	events: {
		'click .btn-delete': 'deleteApprovedSite'
	},
	
	deleteApprovedSite:function() {
		if (confirm("Are you sure you want to revoke access to this site?")) {
			var self = this;
			
            this.model.destroy({
                success:function () {
                    self.$el.fadeTo("fast", 0.00, function () { //fade
                        $(this).slideUp("fast", function () { //slide up
                            $(this).remove(); //then remove from the DOM
                            app.approvedSiteListView.togglePlaceholder();
                        });
                    });
                },
                error:function (error, response) {
            		
					//Pull out the response text.
					var responseJson = JSON.parse(response.responseText);
            		
            		//Display an alert with an error message
            		$('#modalAlert div.modal-body').html(responseJson.errorMessage);
            		
        			 $("#modalAlert").modal({ // wire up the actual modal functionality and show the dialog
        				 "backdrop" : "static",
        				 "keyboard" : true,
        				 "show" : true // ensure the modal is shown immediately
        			 });
            	}
            
            });
            
            app.approvedSiteListView.delegateEvents();
		}
		
		return false;
	},
	
	close:function() {
		$(this.el).unbind();
		$(this.el).empty();
	}
});

