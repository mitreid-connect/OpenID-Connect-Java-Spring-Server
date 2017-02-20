/*******************************************************************************
 * Copyright 2017 The MITRE Corporation
 *   and the MIT Internet Trust Consortium
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
	urlRoot: 'api/resourceset'
});

var ResourceSetCollection = Backbone.Collection.extend({
	model: ResourceSetModel,
	url: 'api/resourceset'
});

var PolicyModel = Backbone.Model.extend({
	urlRoot: function() {
		return 'api/resourceset/' + this.options.rsid + '/policy/';
	},
	initialize: function(model, options) {
		this.options = options;
	}
});

var PolicyCollection = Backbone.Collection.extend({
	model: PolicyModel,
	url: function() {
		return 'api/resourceset/' + this.options.rsid + '/policy/';
	},
	initialize: function(models, options) {
		this.options = options;
	}
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
		'click .btn-delete': 'deleteResourceSet',
		'click .toggleMoreInformation': 'toggleMoreInformation'
	},
	
	editPolicies:function(e) {
		e.preventDefault();
		app.navigate('user/policy/' + this.model.get('id'), {trigger: true});
	},
	
	deleteResourceSet:function(e) {
    	e.preventDefault();

        if (confirm($.t('policy.rs-table.confirm'))) {
            var _self = this;

            this.model.destroy({
            	dataType: false, processData: false,
                success:function () {
                    _self.$el.fadeTo("fast", 0.00, function () { //fade
                        $(this).slideUp("fast", function () { //slide up
                            $(this).remove(); //then remove from the DOM
                            _self.parentView.togglePlaceholder();
                        });
                    });
                },
                error:function (error, response) {
            		console.log("An error occurred when deleting a resource set");
    
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

            _self.parentView.delegateEvents();
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

});

var PolicyListView = Backbone.View.extend({
	tagName: 'span',
	
	initialize:function(options) {
		this.options = options;
	},
	
	load:function(callback) {
    	if (this.model.isFetched &&
    			this.options.rs.isFetched &&
    			this.options.systemScopeList.isFetched) {
    		callback();
    		return;
    	}

    	$('#loadingbox').sheet('show');
    	$('#loading').html(
                '<span class="label" id="loading-policies">' + $.t('policy.loading-policies') + '</span> ' + 
                '<span class="label" id="loading-rs">' + $.t('policy.loading-rs') + '</span> ' + 
                '<span class="label" id="loading-scopes">' + $.t("common.scopes") + '</span> '
    			);

    	$.when(this.model.fetchIfNeeded({success:function(e) {$('#loading-policies').addClass('label-success');}}),
    			this.options.rs.fetchIfNeeded({success:function(e) {$('#loading-rs').addClass('label-success');}}),
    			this.options.systemScopeList.fetchIfNeeded({success:function(e) {$('#loading-scopes').addClass('label-success');}}))
    			.done(function() {
    	    		$('#loadingbox').sheet('hide');
    	    		callback();
    			});    	
    },
	
    events:{
    	'click .btn-add':'addPolicy',
		'click .btn-cancel':'cancel'
    },

    cancel:function(e) {
    	e.preventDefault();
    	app.navigate('user/policy', {trigger: true});
    },
    
    togglePlaceholder:function() {
		if (this.model.length > 0) {
			$('#policy-info', this.el).show();
			$('#policy-table', this.el).show();
			$('#policy-table-empty', this.el).hide();
		} else {
			$('#policy-info', this.el).hide();
			$('#policy-table', this.el).hide();
			$('#policy-table-empty', this.el).show();
		}
	},
	
	addPolicy:function(e) {
		e.preventDefault();
    	app.navigate('user/policy/' + this.options.rs.get('id') +'/new', {trigger: true});
	},
	
	render:function (eventName) {
		$(this.el).html($('#tmpl-policy-table').html());
		
		var _self = this;
		
		_.each(this.model.models, function (policy) {
			
			var view = new PolicyView({model: policy, systemScopeList: _self.options.systemScopeList, rs: _self.options.rs});
			view.parentView = _self;
			$('#policy-table', this.el).append(view.render().el);
			
		}, this);

		this.togglePlaceholder();
       $(this.el).i18n();
		return this;
	}
});


var PolicyView = Backbone.View.extend({
	tagName: 'tr',
	
	initialize:function(options) {
		this.options = options;
		
		if (!this.template) {
			this.template = _.template($('#tmpl-policy').html());
		}

		if (!this.scopeTemplate) {
        	this.scopeTemplate = _.template($('#tmpl-scope-list').html());
        }


	},
	
	events:{
		'click .btn-edit':'editPolicy',
		'click .btn-remove':'removePolicy'
	},
	
	editPolicy:function(e) {
		e.preventDefault();
		app.navigate('user/policy/' + this.options.rs.get("id") + '/' + this.model.get('id'), {trigger: true});
	},
	
	removePolicy:function(e) {
		e.preventDefault();
		
		if (confirm($.t('policy.policy-table.confirm'))) {
            var _self = this;
	        this.model.destroy({
            	dataType: false, processData: false,
	            success:function () {
	                _self.$el.fadeTo("fast", 0.00, function () { //fade
	                    $(this).slideUp("fast", function () { //slide up
	                        $(this).remove(); //then remove from the DOM
	                        _self.parentView.togglePlaceholder();
	                    });
	                });
	            },
	            error:function (error, response) {
	        		console.log("An error occurred when deleting a client");
	
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
	
	        _self.parentView.delegateEvents();
		}
	},
	
	render:function (eventName) {
		var json = this.model.toJSON();
		
		this.$el.html(this.template(json));
		
        $('.scope-list', this.el).html(this.scopeTemplate({scopes: this.model.get('scopes'), systemScopes: this.options.systemScopeList}));

        $(this.el).i18n();
		return this;		
	}
	
	
});


var PolicyFormView = Backbone.View.extend({
	tagName: 'div',
	
	initialize:function(options) {
		this.options = options;
		
		if (!this.template) {
			this.template = _.template($('#tmpl-policy-form').html());
		}
		
        this.issuerCollection = new Backbone.Collection();

	},
	
	events:{
		'click .btn-share': 'addWebfingerClaim',
		'click .btn-share-advanced': 'addAdvancedClaim',
		'click .btn-clear': 'clearAllClaims',
		'click .btn-save': 'savePolicy',
		'click .btn-cancel': 'cancel'
	},
	
	load:function(callback) {
    	if (this.model.isFetched &&
    			this.options.rs.isFetched &&
    			this.options.systemScopeList.isFetched) {
    		callback();
    		return;
    	}

    	$('#loadingbox').sheet('show');
    	$('#loading').html(
                '<span class="label" id="loading-policies">' + $.t('policy.loading-policy') + '</span> ' + 
                '<span class="label" id="loading-rs">' + $.t('policy.loading-rs') + '</span> ' + 
                '<span class="label" id="loading-scopes">' + $.t("common.scopes") + '</span> '
    			);

    	$.when(this.model.fetchIfNeeded({success:function(e) {$('#loading-policies').addClass('label-success');}}),
    			this.options.rs.fetchIfNeeded({success:function(e) {$('#loading-rs').addClass('label-success');}}),
    			this.options.systemScopeList.fetchIfNeeded({success:function(e) {$('#loading-scopes').addClass('label-success');}}))
    			.done(function() {
    	    		$('#loadingbox').sheet('hide');
    	    		callback();
    			});    	
    },

    addWebfingerClaim:function(e) {
    	e.preventDefault();
    	
    	// post to the webfinger helper and get the response back
    	
    	var _self = this;
    	
    	var email = $('#email', this.el).val();
    	
    	$('#loadingbox').sheet('show');
    	$('#loading').html(
                'Looking up identity provider...'
    			);

    	var base = $('base').attr('href');
    	$.getJSON(base + '/api/emailsearch?' + $.param({'identifier': email}), function(data) {

    		// grab the current state of the scopes checkboxes just in case
        	var scopes = $('#scopes input[type="checkbox"]:checked').map(function(idx, elem) { return $(elem).val(); }).get();
        	
        	_self.model.set({
        		scopes: scopes,
    			claimsRequired: data
    		}, {trigger: false});

    		_self.render();

    		$('#loadingbox').sheet('hide');
    		
    	}).error(function(jqXHR, textStatus, errorThrown) {
    		console.log("An error occurred when doing a webfinger lookup", errorThrown);
    	    
    		$('#loadingbox').sheet('hide');

    		//Display an alert with an error message
			$('#modalAlert div.modal-header').html($.t('policy.webfinger-error'));
    		$('#modalAlert div.modal-body').html($.t('policy.webfinger-error-description', {email: email}));
    		
			 $("#modalAlert").modal({ // wire up the actual modal functionality and show the dialog
				 "backdrop" : "static",
				 "keyboard" : true,
				 "show" : true // ensure the modal is shown immediately
			 });
    	});
    	
    },
    
    addAdvancedClaim:function(e) {
    	e.preventDefault();
    	
    	var name = $('#name', this.el).val();
    	var friendly = $('#friendly-name', this.el).val();
    	var rawValue = $('#value', this.el).val();
    	var valueType = $('#value-type', this.el).val();
    	var value = null;
    	if (valueType == 'number') {
    		value = Number(rawValue);
    	} else if (valueType == 'boolean') {
    		value = (rawValue.toLowerCase() == 'true');
    	} else if (valueType == 'json') {
    		value = JSON.parse(rawValue);
    	} else {
    		// treat it as a string, the default
    		value = rawValue;
    	}

    	var issuers = this.issuerCollection.pluck('item');
    	
    	console.log(name, friendly, rawValue, valueType, value, issuers);
    	
    	if (!_.isEmpty(issuers) 
    			&& name
    			&& value) {
    		// we've got a valid claim, add it to our set
    		// grab the current state of the scopes checkboxes just in case
        	var scopes = $('#scopes input[type="checkbox"]:checked').map(function(idx, elem) { return $(elem).val(); }).get();
        
        	var claimsRequired = this.model.get('claimsRequired');
        	if (!claimsRequired) {
        		claimsRequired = [];
        	}
        	claimsRequired.push({
        		name: name,
        		friendlyName: friendly,
        		value: value,
        		issuer: issuers
        	});
        	
        	this.model.set({
        		scopes: scopes,
    			claimsRequired: claimsRequired
    		}, {trigger: false});

        	$('#name', this.el).val('');
        	$('#friendly-name', this.el).val('');
        	$('#value', this.el).val('');
        	$('#value-type', this.el).val('text');

        	this.render();
        	
        	// re-select the advanced tab
        	$('a[data-target="#policy-advanced-tab"]', this.el).tab('show')
        	
    	} else {
    		// something is missing
    		$('#loadingbox').sheet('hide');

    		//Display an alert with an error message
			$('#modalAlert div.modal-header').html($.t('policy.advanced-error'));
    		$('#modalAlert div.modal-body').html($.t('policy.advanced-error-description'));
    		
			 $("#modalAlert").modal({ // wire up the actual modal functionality and show the dialog
				 "backdrop" : "static",
				 "keyboard" : true,
				 "show" : true // ensure the modal is shown immediately
			 });
    	}
    },
    
    clearAllClaims:function(e) {
    	e.preventDefault();

    	if (confirm($.t('policy.policy-form.clear-all-confirm'))) {
    	
	    	var scopes = $('#scopes input[type="checkbox"]:checked').map(function(idx, elem) { return $(elem).val(); }).get();
	        
	    	var claimsRequired = [];
	    	
	    	this.model.set({
	    		scopes: scopes,
				claimsRequired: claimsRequired
			}, {trigger: false});
	
	    	this.render();
    	}  	
    },
    
    savePolicy:function(e) {
    	e.preventDefault();
    	
    	// get all the scopes that are checked
    	var scopes = $('#scopes input[type="checkbox"]:checked').map(function(idx, elem) { return $(elem).val(); }).get();
    	
    	var valid = this.model.set({
    		scopes: scopes
    	});
    	
		if (valid) {
			
			var _self = this;
			this.model.save({}, {
				success:function() {
					app.systemScopeList.add(_self.model);
					
					// refresh the associated RS
					_self.options.rs.fetch({success: function() {
						app.navigate('user/policy/' + _self.options.rs.get('id'), {trigger: true});
					}});
					
				},
				error:function(error, response) {
					
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
    
    cancel:function(e) {
    	e.preventDefault();
		app.navigate('user/policy/' + this.options.rs.get('id'), {trigger: true});
    },
    
    render:function (eventName) {
		var json = this.model.toJSON();
		var rs = this.options.rs.toJSON();
		
		this.$el.html(this.template({policy: json, rs: rs}));
		
        // build and bind issuer view
        var issuerView = new ListWidgetView({
        	placeholder: $.t('policy.policy-form.issuer-placeholder'), 
        	helpBlockText: $.t('policy.policy-form.issuer-help'),
            collection: this.issuerCollection});
        $("#issuers .controls",this.el).html(issuerView.render().el);

        $(this.el).i18n();

		return this;
	}
});


ui.routes.push({path: "user/policy", name: "policy", callback:
	function() {

		this.breadCrumbView.collection.reset();
		this.breadCrumbView.collection.add([
	        {text:$.t('admin.home'), href:""},
	        {text:$.t('policy.resource-sets'), href:"manage/#user/policy"}
		]);
		
		this.updateSidebar('user/policy');
		
		var view = new ResourceSetListView({model: this.resourceSetList, clientList: this.clientList, systemScopeList: this.systemScopeList});
	
		view.load(function() {
			$('#content').html(view.render().el);
			setPageTitle($.t('policy.resource-sets'));
		});
		
	}
});

ui.routes.push({path: "user/policy/:rsid", name: "editPolicies", callback:
	function(rsid) {

		this.breadCrumbView.collection.reset();
		this.breadCrumbView.collection.add([
	        {text:$.t('admin.home'), href:""},
	        {text:$.t('policy.resource-sets'), href:"manage/#user/policy"},
	        {text:$.t('policy.edit-policies'), href:"manage/#user/policy/" + rsid}
		]);
		
		this.updateSidebar('user/policy');
		
		var rs = this.resourceSetList.get(rsid);
		var policies = null;
		if (rs == null) {
			// need to load it directly
			policies = new PolicyCollection([], {rsid: rsid});
			rs = new ResourceSetModel({id: rsid});
			this.resourceSetList.add(rs); // it will be loaded below, don't need to load it again in the future
		} else {
			// the resource set is loaded, preload the claims
			policies = new PolicyCollection(rs.get('policies'), {rsid: rsid});
			policies.isFetched = true;
		}
		
		var view = new PolicyListView({model: policies, rs: rs, systemScopeList: this.systemScopeList});
		
		view.load(function() {
			$('#content').html(view.render().el);
			setPageTitle($.t('policy.edit-policy'));
		});
		
	}
});

ui.routes.push({path: "user/policy/:rsid/new", name: "newPolicy", callback:
	function(rsid) {

		this.breadCrumbView.collection.reset();
		this.breadCrumbView.collection.add([
	        {text:$.t('admin.home'), href:""},
	        {text:$.t('policy.resource-sets'), href:"manage/#user/policy"},
	        {text:$.t('policy.edit-policies'), href:"manage/#user/policy/" + rsid},
	        {text:$.t('policy.new-policy'), href:"manage/#user/policy/" + rsid + "/new"}
		]);
		
		this.updateSidebar('user/policy');
		
		var policy = policy = new PolicyModel({}, {rsid: rsid});
	
		var rs = this.resourceSetList.get(rsid);
		if (rs == null) {
			// need to load it directly
			rs = new ResourceSetModel({id: rsid});
			this.resourceSetList.add(rs); // it will be loaded below, don't need to load it again in the future
		}
		
		var view = new PolicyFormView({model: policy, rs: rs, systemScopeList: this.systemScopeList});
		
		view.load(function() {
			$('#content').html(view.render().el);
			setPageTitle($.t('policy.edit-policy'));
		});
	}
});

ui.routes.push({path: "user/policy/:rsid/:pid", name: "editPolicy", callback:
	function(rsid, pid) {
		this.breadCrumbView.collection.reset();
		this.breadCrumbView.collection.add([
	        {text:$.t('admin.home'), href:""},
	        {text:$.t('policy.resource-sets'), href:"manage/#user/policy"},
	        {text:$.t('policy.edit-policies'), href:"manage/#user/policy/" + rsid},
	        {text:$.t('policy.edit-policy'), href:"manage/#user/policy/" + rsid + "/" + pid}
		]);
		
		this.updateSidebar('user/policy');
		
		var rs = this.resourceSetList.get(rsid);
		var policy = null;
		if (rs == null) {
			// need to load it directly
			policy = new PolicyModel({id: pid}, {rsid: rsid});
			rs = new ResourceSetModel({id: rsid});
			this.resourceSetList.add(rs); // it will be loaded below, don't need to load it again in the future
		} else {
			// the resource set is loaded, preload the claims
			_.each(rs.get('policies'), function(p) {
				if (p.id == pid) {
					policy = new PolicyModel(p, {rsid: rsid});
					policy.isFetched = true;
				}
			});
			if (policy == null) {
	    		// need to load it directly
	    		policy = new PolicyModel({id: pid}, {rsid: rsid});
			}
		}
		
		var view = new PolicyFormView({model: policy, rs: rs, systemScopeList: this.systemScopeList});
		
		view.load(function() {
			$('#content').html(view.render().el);
			setPageTitle($.t('policy.edit-policy'));
		});
		
		
	}
});

ui.templates.push('resources/template/policy.html');

ui.init.push(function(app) {
	app.resourceSetList = new ResourceSetCollection();
});
