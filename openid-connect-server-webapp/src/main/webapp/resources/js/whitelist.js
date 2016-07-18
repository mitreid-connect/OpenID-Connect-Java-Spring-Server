/*******************************************************************************
 * Copyright 2016 The MITRE Corporation
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
                '<span class="label" id="loading-whitelist">' + $.t('whitelist.whitelist') + '</span> ' +
                '<span class="label" id="loading-clients">' + $.t('common.clients') + '</span> ' +
                '<span class="label" id="loading-scopes">' + $.t('common.scopes') + '</span> '
    			);

    	$.when(this.model.fetchIfNeeded({success:function(e) {$('#loading-whitelist').addClass('label-success');}, error: app.errorHandlerView.handleError()}),
    			this.options.clientList.fetchIfNeeded({success:function(e) {$('#loading-clients').addClass('label-success');}, error: app.errorHandlerView.handleError()}),
    			this.options.systemScopeList.fetchIfNeeded({success:function(e) {$('#loading-scopes').addClass('label-success');}, error: app.errorHandlerView.handleError()}))
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
		
		var _self = this;
		
		_.each(this.model.models, function (whiteList) {
			
			// look up client
			var client = _self.options.clientList.getByClientId(whiteList.get('clientId'));
			
			// if there's no client ID, this is an error!
			if (client != null) {
				var view = new WhiteListView({model: whiteList, client: client, systemScopeList: _self.options.systemScopeList});
				view.parentView = _self;
				$('#whitelist-table', _self.el).append(view.render().el);
			}
			
		}, this);

		this.togglePlaceholder();
        $(this.el).i18n();
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
    	$('#loading').html(
    	        '<span class="label" id="loading-whitelist">' + $.t('whitelist.whitelist') + '</span> ' +
                '<span class="label" id="loading-clients">' + $.t('common.clients') + '</span> ' +
                '<span class="label" id="loading-scopes">' + $.t('common.scopes') + '</span> '
    			);

    	$.when(this.model.fetch({success:function(e) {$('#loading-whitelist').addClass('label-success');}, error: app.errorHandlerView.handleError()}),
    			this.options.clientList.fetch({success:function(e) {$('#loading-clients').addClass('label-success');}, error: app.errorHandlerView.handleError()}),
    			this.options.systemScopeList.fetch({success:function(e) {$('#loading-scopes').addClass('label-success');}, error: app.errorHandlerView.handleError()}))
    			.done(function() {
    	    		$('#loadingbox').sheet('hide');
    	    		_self.render();
    			});    	
    }
});

var WhiteListView = Backbone.View.extend({
	tagName: 'tr',
	
	initialize:function(options) {
    	this.options = options;
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
        
		this.$('.dynamically-registered').tooltip({title: $.t('common.dynamically-registered')});

        $(this.el).i18n();
        return this;
	},
	
	events:{
		'click .btn-edit': 'editWhitelist',
		'click .btn-delete': 'deleteWhitelist',
		'click .toggleMoreInformation': 'toggleMoreInformation'
	},
	
	editWhitelist:function(e) {
    	e.preventDefault();
		app.navigate('admin/whitelist/' + this.model.get('id'), {trigger: true});
	},
	
	deleteWhitelist:function(e) {
    	e.preventDefault();
		
		if (confirm($.t('whitelist.confirm'))) {
			var _self = this;
			
            this.model.destroy({
            	dataType: false, processData: false,
                success:function () {
                    _self.$el.fadeTo("fast", 0.00, function () { //fade
                        $(this).slideUp("fast", function () { //slide up
                            $(this).remove(); //then remove from the DOM
                            // check the placeholder in case it's empty now
                            _self.parentView.togglePlaceholder();
                        });
                    });
                },
                error:app.errorHandlerView.handleError()
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

	close:function() {
		$(this.el).unbind();
		$(this.el).empty();
	}
});

var WhiteListFormView = Backbone.View.extend({
	tagName: 'span',
	
	initialize:function (options) {
    	this.options = options;
		if (!this.template) {
			this.template = _.template($('#tmpl-whitelist-form').html());
		}
		
		this.scopeCollection = new Backbone.Collection();

		this.listWidgetViews = [];
		
	},

	load:function(callback) {

		if (this.options.client) {
			// we know what client we're dealing with already
	    	if (this.model.isFetched &&
	    			this.options.client.isFetched) {
	    		callback();
	    		return;
	    	}
			
	    	$('#loadingbox').sheet('show');
	    	$('#loading').html(
	                '<span class="label" id="loading-whitelist">' + $.t('whitelist.whitelist') + '</span> ' +
	                '<span class="label" id="loading-clients">' + $.t('common.clients') + '</span> ' +
	                '<span class="label" id="loading-scopes">' + $.t('common.scopes') + '</span> '
	    			);

	    	$.when(this.model.fetchIfNeeded({success:function(e) {$('#loading-whitelist').addClass('label-success');}, error: app.errorHandlerView.handleError()}),
	    			this.options.client.fetchIfNeeded({success:function(e) {$('#loading-clients').addClass('label-success');}, error: app.errorHandlerView.handleError()}),
	    			this.options.systemScopeList.fetchIfNeeded({success:function(e) {$('#loading-scopes').addClass('label-success');}, error: app.errorHandlerView.handleError()}))
	    			.done(function() {
	    				$('#loadingbox').sheet('hide');
	    	    		callback();
	    			});    	
			
		} else {
			// we need to get the client information from the list
			
	    	if (this.model.isFetched &&
	    			this.options.clientList.isFetched &&
	    			this.options.systemScopeList.isFetched) {
	    		
				var client = this.options.clientList.getByClientId(this.model.get('clientId'));
				this.options.client = client;
	    			
	    		callback();
	    		return;
	    	}

	    	$('#loadingbox').sheet('show');
	    	$('#loading').html(
	                '<span class="label" id="loading-whitelist">' + $.t('whitelist.whitelist') + '</span> ' +
	                '<span class="label" id="loading-clients">' + $.t('common.clients') + '</span> ' +
	                '<span class="label" id="loading-scopes">' + $.t('common.scopes') + '</span> '
	    			);

	    	var _self = this;
	    	
	    	$.when(this.model.fetchIfNeeded({success:function(e) {$('#loading-whitelist').addClass('label-success');}, error: app.errorHandlerView.handleError()}),
	    			this.options.clientList.fetchIfNeeded({success:function(e) {$('#loading-clients').addClass('label-success');}, error: app.errorHandlerView.handleError()}),
	    			this.options.systemScopeList.fetchIfNeeded({success:function(e) {$('#loading-scopes').addClass('label-success');}, error: app.errorHandlerView.handleError()}))
	    			.done(function() {
	    				
	    				var client = _self.options.clientList.getByClientId(_self.model.get('clientId'));
	    				_self.options.client = client;
	    				
	    				$('#loadingbox').sheet('hide');
	    	    		callback();
	    			});    	
			
		}
		
    	

    	
	},
	
	events:{
		'click .btn-save':'saveWhiteList',
		'click .btn-cancel':'cancelWhiteList',
		
	},
	
	saveWhiteList:function (e) {
    	e.preventDefault();
		$('.control-group').removeClass('error');
		
        // sync any leftover collection items
        _.each(this.listWidgetViews, function(v) {
        	v.addItem($.Event('click'));
        });
        
		// process allowed scopes
        var allowedScopes = this.scopeCollection.pluck("item");
		
        this.model.set({clientId: this.options.client.get('clientId')}, {silent: true});
        
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
                error:app.errorHandlerView.handleError()
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
		
		this.listWidgetViews = [];
		
        var _self = this;
        // build and bind scopes
        _.each(this.model.get("allowedScopes"), function (scope) {
            _self.scopeCollection.add(new Backbone.Model({item:scope}));
        });

        var scopeView = new ListWidgetView({
        	placeholder: $.t('whitelist.whitelist-form.scope-placeholder'),        	
        	autocomplete: this.options.client.get("scope"), 
        	helpBlockText: $.t('whitelist.whitelist-form.scope-help'),
        	collection: this.scopeCollection});
        $("#scope .controls",this.el).html(scopeView.render().el);
        this.listWidgetViews.push(scopeView);
		
        $(this.el).i18n();
		return this;

	}

});

