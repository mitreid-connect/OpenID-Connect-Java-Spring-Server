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

        if (confirm($.t('policy.policy-table.confirm'))) {
            var _self = this;

            this.model.destroy({
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
	
	render:function (eventName) {
		$(this.el).html($('#tmpl-policy-table').html());
		
		var _self = this;
		
		_.each(this.model.models, function (policy) {
			
			var view = new PolicyView({model: policy, systemScopeList: _self.options.systemScopeList, rs: _self.options.rs});
			view.parentView = _self;
			$('#policy-table', this.el).append(view.render().el);
			
		}, this);

		this.togglePlaceholder();
       // $(this.el).i18n();
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
		
		if (confirm($.t('policy.policy-table.policy-confirm'))) {
            var _self = this;
	        this.model.destroy({
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

        //$(this.el).i18n();
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
		
        this.scopeCollection = new Backbone.Collection();
	},
	
	events:{
		'click .btn-share': 'addClaim',
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

    addClaim:function(e) {
    	e.preventDefault();
    	
    	// post to the webfinger helper and get the response back
    	
    	var _self = this;
    	
    	var email = $('#email', this.el).val();
    	
        var base = $('base').attr('href');
    	$.getJSON(base + '/api/emailsearch?' + $.param({'identifier': email}), function(data) {
    		
    		_self.model.set({
    			claimsRequired: data
    		}, {trigger: false});

    		_self.render();
    		
    	}).error(function(jqXHR, textStatus, errorThrown) {
    		console.log("An error occurred when doing a webfinger lookup", errorThrown);
    	    
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
					app.navigate('user/policy/' + _self.options.rs.get('id'), {trigger: true});
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
		
		return this;
	}
});