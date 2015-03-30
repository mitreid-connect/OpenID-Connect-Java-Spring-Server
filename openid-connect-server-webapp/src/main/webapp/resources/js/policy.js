/*******************************************************************************
 * Copyright 2015 The MITRE Corporation
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
 *******************************************************************************/

var ResourceSetModel = Backbone.Model.extend({
	
});

var ResourceSetCollection = Backbone.Collection.extend({
	model: ResourceSetModel,
	url: 'api/claims'
});

var ClaimModel = Backbone.Model.extend({
	
});

var ClaimCollection = Backbone.Collection.extend({
	model: ClaimModel
});

var ResourceSetListView = Backbone.View.extend({
	tagName: 'span',
	
	initialize:function (options) {
		this.options = options;
	},
	
	load:function(callback) {
    	if (this.model.isFetched &&
    			this.options.clientList.isFetched &&
    			this.options.systemScopeList.isFetched) {
    		callback();
    		return;
    	}

    	$('#loadingbox').sheet('show');
    	$('#loading').html(
                '<span class="label" id="loading-resourcesets">' + $.t('policy.resource-sets') + '</span> ' +
                '<span class="label" id="loading-clients">' + $.t('common.clients') + '</span> ' +
                '<span class="label" id="loading-scopes">' + $.t('common.scopes') + '</span> '
    			);

    	$.when(this.model.fetchIfNeeded({success:function(e) {$('#loading-resourcesets').addClass('label-success');}}),
    			this.options.clientList.fetchIfNeeded({success:function(e) {$('#loading-clients').addClass('label-success');}}),
    			this.options.systemScopeList.fetchIfNeeded({success:function(e) {$('#loading-scopes').addClass('label-success');}}))
    			.done(function() {
    	    		$('#loadingbox').sheet('hide');
    	    		callback();
    			});    	
    },
	
    events: {
        "click .refresh-table":"refreshTable"
    },
    
	render:function (eventName) {
		$(this.el).html($('#tmpl-resource-set-table').html());
		
		var _self = this;
		
		_.each(this.model.models, function (resourceSet) {
			
			// look up client
			var client = this.options.clientList.getByClientId(resourceSet.get('clientId'));
			
			// if there's no client ID, this is an error!
			if (client != null) {
				var view = new ResourceSetView({model: resourceSet, client: client, systemScopeList: _self.options.systemScopeList});
				view.parentView = _self;
				$('#resource-set-table', this.el).append(view.render().el);
			}
			
		}, this);

		this.togglePlaceholder();
        $(this.el).i18n();
		return this;
	},

	togglePlaceholder:function() {
		if (this.model.length > 0) {
			$('#resource-set-table', this.el).show();
			$('#resource-set-table-empty', this.el).hide();
		} else {
			$('#resource-set-table', this.el).hide();
			$('#resource-set-table-empty', this.el).show();
		}
	},
	
    refreshTable:function(e) {
    	e.preventDefault();
    	var _self = this;
    	$('#loadingbox').sheet('show');
    	$('#loading').html(
    	        '<span class="label" id="loading-resourcesets">' + $.t('policy.resource-sets') + '</span> ' +
                '<span class="label" id="loading-clients">' + $.t('common.clients') + '</span> ' +
                '<span class="label" id="loading-scopes">' + $.t('common.scopes') + '</span> '
    			);

    	$.when(this.model.fetch({success:function(e) {$('#loading-resourcesets').addClass('label-success');}}),
    			this.options.clientList.fetch({success:function(e) {$('#loading-clients').addClass('label-success');}}),
    			this.options.systemScopeList.fetch({success:function(e) {$('#loading-scopes').addClass('label-success');}}))
    			.done(function() {
    	    		$('#loadingbox').sheet('hide');
    	    		_self.render();
    			});    	
    }

	
});


var ResourceSetView = Backbone.View.extend({
	tagName: 'tr',
	
	initialize:function(options) {
    	this.options = options;
		if (!this.template) {
			this.template = _.template($('#tmpl-resource-set').html());
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
		
		var json = {rs: this.model.toJSON(), client: this.options.client.toJSON()};
		
		this.$el.html(this.template(json));
		
        $('.scope-list', this.el).html(this.scopeTemplate({scopes: this.model.get('scopes'), systemScopes: this.options.systemScopeList}));
        
        $('.client-more-info-block', this.el).html(this.moreInfoTemplate({client: this.options.client.toJSON()}));
		
		$(this.el).i18n();
		return this;
	},

	events:{
		'click .btn-edit': 'editPolicies',
		'click .toggleMoreInformation': 'toggleMoreInformation'
	},
	
	editPolicies:function(e) {
		e.preventDefault();
		app.navigate('user/policy/' + this.model.get('id'), {trigger: true});
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

});

var ClaimListView = Backbone.View.extend({
	tagName: 'span',
	
	initialize:function(options) {
		this.options = options;
	},
	
	load:function(callback) {
    	if (this.model.isFetched) {
    		callback();
    		return;
    	}

    	$('#loadingbox').sheet('show');
    	$('#loading').html(
                '<span class="label" id="loading-claims">' + $.t('policy.required-claims') + '</span> '
    			);

    	$.when(this.model.fetchIfNeeded({success:function(e) {$('#loading-claims').addClass('label-success');}}))
    			.done(function() {
    	    		$('#loadingbox').sheet('hide');
    	    		callback();
    			});    	
    },
	
    events:{
		'click .btn-save':'savePolicy',
		'click .btn-cancel':'cancel',
		'click #add-email':'addClaim'
    },

    cancel:function(e) {
    	e.preventDefault();
    	app.navigate('user/policy', {trigger: true});
    },
    
    savePolicy:function(e) {
    	e.preventDefault();

    	var _self = this;

    	console.log(this);
    	
    	this.model.sync('update', this.model, {
    		success:function() {
    			// update our copy of the resource set object (if we have it)
    			if (_self.options.rs != null) {
    				rs.set({claimsRequired: _self.model.toJSON()}, {trigger: false});
    			}
    			
    	    	app.navigate('user/policy', {trigger: true});
    		},
            error:function (error, response) {
        		console.log("An error occurred when saving a policy set");

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
    },
    
    addClaim:function(e) {
    	e.preventDefault();
    	
    	// post to the webfinger helper and get the response back
    	
    	var _self = this;
    	
    	var email = $('#email', this.el).val();
    	
        var base = $('base').attr('href');
    	$.getJSON(base + '/api/emailsearch?' + $.param({'identifier': email}), function(data) {
    		
    		var claim = new ClaimModel(data);
    		_self.model.add(claim, {'trigger': false});
    		_self.render();
    		
    	}).error(function(jqXHR, textStatus, errorThrown) {
    		console.log(errorThrown);
    	});
    	
    },
    
    togglePlaceholder:function() {
		if (this.model.length > 0) {
			$('#required-claim-table', this.el).show();
			$('#required-claim-table-empty', this.el).hide();
		} else {
			$('#required-claim-table', this.el).hide();
			$('#required-claim-table-empty', this.el).show();
		}
	},
	
	render:function (eventName) {
		$(this.el).html($('#tmpl-required-claim-table').html());
		
		var _self = this;
		
		_.each(this.model.models, function (claim) {
			
			var view = new ClaimView({model: claim});
			view.parentView = _self;
			$('#required-claim-table', this.el).append(view.render().el);
			
		}, this);

		this.togglePlaceholder();
        $(this.el).i18n();
		return this;
	}
});


var ClaimView = Backbone.View.extend({
	tagName: 'tr',
	
	initialize:function(options) {
		this.options = options;
		
		if (!this.template) {
			this.template = _.template($('#tmpl-required-claim').html());
		}
	},
	
	events:{
		'click .btn-remove':'removeClaim'
	},
	
	removeClaim:function(e) {
		e.preventDefault();
		
		var _self = this;
		
		this.model.collection.remove(this.model);
        _self.$el.fadeTo("fast", 0.00, function () { //fade
            $(this).slideUp("fast", function () { //slide up
                $(this).remove(); //then remove from the DOM
                _self.parentView.togglePlaceholder();
            });
        });
		
		
	},
	
	render:function (eventName) {
		var json = this.model.toJSON();
		
		this.$el.html(this.template(json));
		
		$(this.el).i18n();
		return this;		
	}
	
	
});