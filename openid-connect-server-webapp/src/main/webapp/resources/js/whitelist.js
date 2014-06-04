/*******************************************************************************
 * Copyright 2014 The MITRE Corporation 
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
var WhiteListModel = Backbone.Model.extend({
	
	idAttribute: "id",
	
	initialize: function () { },
	
	urlRoot: "api/whitelist"
	
});

var WhiteListCollection = Backbone.Collection.extend({
	initialize: function() {
		//this.fetch();
	},
	
    getByClientId: function(clientId) {
		var clients = this.where({clientId: clientId});
		if (clients.length == 1) {
			return clients[0];
		} else {
			return null;
		}
    },
	
	model: WhiteListModel,
	url: "api/whitelist"
	
});

var WhiteListListView = Backbone.View.extend({
	tagName: 'span',
	
	initialize:function () {
		//this.model.bind("reset", this.render, this);
	},

	load:function(callback) {
    	if (this.model.isFetched &&
    			this.options.clientList.isFetched &&
    			this.options.systemScopeList.isFetched) {
    		callback();
    		return;
    	}

    	$('#loadingbox').sheet('show');
    	$('#loading').html('<span class="label" id="loading-whitelist">Whitelist</span>' +
    			'<span class="label" id="loading-clients">Clients</span>' + 
    			'<span class="label" id="loading-scopes">Scopes</span>'
    			);

    	$.when(this.model.fetchIfNeeded({success:function(e) {$('#loading-whitelist').addClass('label-success');}}),
    			this.options.clientList.fetchIfNeeded({success:function(e) {$('#loading-clients').addClass('label-success');}}),
    			this.options.systemScopeList.fetchIfNeeded({success:function(e) {$('#loading-scopes').addClass('label-success');}}))
    			.done(function() {
    	    		$('#loadingbox').sheet('hide');
    	    		callback();
    			});    	
    },

    events:{
        "click .refresh-table":"refreshTable"
	},
	
	render:function (eventName) {
		$(this.el).html($('#tmpl-whitelist-table').html());
		
		_.each(this.model.models, function (whiteList) {
			
			// look up client
			var client = this.options.clientList.getByClientId(whiteList.get('clientId'));
			
			// if there's no client ID, this is an error!
			if (client != null) {
				$('#whitelist-table', this.el).append(new WhiteListView({model: whiteList, client: client, systemScopeList: this.options.systemScopeList}).render().el);
			}
			
		}, this);

		this.togglePlaceholder();
		
		return this;
	},

	togglePlaceholder:function() {
		if (this.model.length > 0) {
			$('#whitelist-table', this.el).show();
			$('#whitelist-table-empty', this.el).hide();
		} else {
			$('#whitelist-table', this.el).hide();
			$('#whitelist-table-empty', this.el).show();
		}
	},
	
    refreshTable:function(e) {
    	e.preventDefault();
    	var _self = this;
    	$('#loadingbox').sheet('show');
    	$('#loading').html('<span class="label" id="loading-whitelist">Whitelist</span> ' +
    			'<span class="label" id="loading-clients">Clients</span> ' + 
    			'<span class="label" id="loading-scopes">Scopes</span> '
    			);

    	$.when(this.model.fetch({success:function(e) {$('#loading-whitelist').addClass('label-success');}}),
    			this.options.clientList.fetch({success:function(e) {$('#loading-clients').addClass('label-success');}}),
    			this.options.systemScopeList.fetch({success:function(e) {$('#loading-scopes').addClass('label-success');}}))
    			.done(function() {
    	    		$('#loadingbox').sheet('hide');
    	    		_self.render();
    			});    	
    }
});

var WhiteListView = Backbone.View.extend({
	tagName: 'tr',
	
	initialize:function() {
		if (!this.template) {
			this.template = _.template($('#tmpl-whitelist').html());
		}
		
        if (!this.scopeTemplate) {
        	this.scopeTemplate = _.template($('#tmpl-scope-list').html());
        }

        if (!this.moreInfoTemplate) {
        	this.moreInfoTemplate = _.template($('#tmpl-client-more-info-block').html());
        }

		this.model.bind('change', this.render, this);
	},
	
	render:function(eventName) {
		
		var json = {whiteList: this.model.toJSON(), client: this.options.client.toJSON()};
		
		this.$el.html(this.template(json));

        $('.scope-list', this.el).html(this.scopeTemplate({scopes: this.model.get('allowedScopes'), systemScopes: this.options.systemScopeList}));
        
        $('.client-more-info-block', this.el).html(this.moreInfoTemplate({client: this.options.client.toJSON()}));
        
		this.$('.dynamically-registered').tooltip({title: 'This client was dynamically registered'});

        return this;
	},
	
	events:{
		'click .btn-edit': 'editWhitelist',
		'click .btn-delete': 'deleteWhitelist',
		'click .toggleMoreInformation': 'toggleMoreInformation'
	},
	
	editWhitelist:function(e) {
    	e.preventDefault();
		app.navigate('admin/whitelist/' + this.model.id, {trigger: true});
	},
	
	deleteWhitelist:function(e) {
    	e.preventDefault();
		
		if (confirm("Are you sure you want to delete this whitelist entry?")) {
			var self = this;
			
            this.model.destroy({
                success:function () {
                    self.$el.fadeTo("fast", 0.00, function () { //fade
                        $(this).slideUp("fast", function () { //slide up
                            $(this).remove(); //then remove from the DOM
                            // check the placeholder in case it's empty now
                            app.whiteListListView.togglePlaceholder();
                        });
                    });
                },
                error:function (error, response) {
            		console.log("An error occurred when deleting a whitelist entry");

    				//Pull out the response text.
    				var responseJson = JSON.parse(response.responseText);
            		
            		//Display an alert with an error message
    				$('#modalAlert div.modal-header').html(responseJson.error);
            		$('#modalAlert div.modal-body').html(responseJson.error_description);
            		
        			 $("#modalAlert").modal({ // wire up the actual modal functionality and show the dialog
        				 "backdrop" : "static",
        				 "keyboard" : true,
        				 "show" : true // ensure the modal is shown immediately
        			 });
            	}
            });
            
            app.whiteListListView.delegateEvents();
		}
		
		return false;
	},
	
	toggleMoreInformation:function(e) {
		e.preventDefault();
		if ($('.moreInformation', this.el).is(':visible')) {
			// hide it
			$('.moreInformation', this.el).hide('fast');
			$('.toggleMoreInformation i', this.el).attr('class', 'icon-chevron-right');
			$('.moreInformationContainer', this.el).removeClass('alert').removeClass('alert-info').addClass('muted');
		
		} else {
			// show it
			$('.moreInformation', this.el).show('fast');
			$('.toggleMoreInformation i', this.el).attr('class', 'icon-chevron-down');
			$('.moreInformationContainer', this.el).addClass('alert').addClass('alert-info').removeClass('muted');
		}
	},

	close:function() {
		$(this.el).unbind();
		$(this.el).empty();
	}
});

var WhiteListFormView = Backbone.View.extend({
	tagName: 'span',
	
	initialize:function () {
		if (!this.template) {
			this.template = _.template($('#tmpl-whitelist-form').html());
		}
		
		this.scopeCollection = new Backbone.Collection();
	},

	events:{
		'click .btn-save':'saveWhiteList',
		'click .btn-cancel':'cancelWhiteList',
		
	},
	
	saveWhiteList:function (e) {
    	e.preventDefault();
		$('.control-group').removeClass('error');
		
		// process allowed scopes
        var allowedScopes = this.scopeCollection.pluck("item");
		
        if (this.model.get('id') == null) {
			this.model.set({clientId:$('#clientId input').val()});
        }
        
		var valid = this.model.set({
			allowedScopes: allowedScopes
		});
		
        if (valid) {
            var _self = this;
            this.model.save({}, {
                success:function () {
                    app.whiteListList.add(_self.model);
                    app.navigate('admin/whitelists', {trigger:true});
                },
                error:function (error, response) {
            		console.log("An error occurred when deleting from a list widget");

    				//Pull out the response text.
    				var responseJson = JSON.parse(response.responseText);
            		
            		//Display an alert with an error message
    				$('#modalAlert div.modal-header').html(responseJson.error);
            		$('#modalAlert div.modal-body').html(responseJson.error_description);
            		
        			 $("#modalAlert").modal({ // wire up the actual modal functionality and show the dialog
        				 "backdrop" : "static",
        				 "keyboard" : true,
        				 "show" : true // ensure the modal is shown immediately
        			 });
            	}
            });
        }

        return false;
		
	},
	
	cancelWhiteList:function(e) {
    	e.preventDefault();
    	// TODO: figure out where we came from and go back there instead
    	if (this.model.get('id') == null) {
    		// if it's a new whitelist entry, go back to the client listing page
    		app.navigate('admin/clients', {trigger:true});    		
    	} else {
    		// if we're editing a whitelist, go back to the whitelists page
    		app.navigate('admin/whitelists', {trigger:true});    		
    	}
	},
	
	render:function (eventName) {
		
		var json = {whiteList: this.model.toJSON(), client: this.options.client.toJSON()};
		
		this.$el.html(this.template(json));
		
		
        var _self = this;
        // build and bind scopes
        _.each(this.model.get("allowedScopes"), function (scope) {
            _self.scopeCollection.add(new Backbone.Model({item:scope}));
        });

        $("#scope .controls",this.el).html(new ListWidgetView({
        	placeholder: 'new scope here', 
        	autocomplete: this.options.client.scope, 
        	collection: this.scopeCollection}).render().el);
		
		
		return this;

	}

});

