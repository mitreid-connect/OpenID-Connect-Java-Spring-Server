
    var URIModel = Backbone.Model.extend({

        validate: function(attrs){

            var expression = /^(?:([a-z0-9+.-]+:\/\/)((?:(?:[a-z0-9-._~!$&'()*+,;=:]|%[0-9A-F]{2})*)@)?((?:[a-z0-9-._~!$&'()*+,;=]|%[0-9A-F]{2})*)(:(?:\d*))?(\/(?:[a-z0-9-._~!$&'()*+,;=:@\/]|%[0-9A-F]{2})*)?|([a-z0-9+.-]+:)(\/?(?:[a-z0-9-._~!$&'()*+,;=:@]|%[0-9A-F]{2})+(?:[a-z0-9-._~!$&'()*+,;=:@\/]|%[0-9A-F]{2})*)?)(\?(?:[a-z0-9-._~!$&'()*+,;=:\/?@]|%[0-9A-F]{2})*)?(#(?:[a-z0-9-._~!$&'()*+,;=:\/?@]|%[0-9A-F]{2})*)?$/i;
            var regex = new RegExp(expression);

            if (attrs.item == null || !attrs.item.match(regex)) {
                return "Invalid URI";
            }
        }

    });


    /*
    * Backbone JS Reusable ListWidget
    *  Options
    * {
    *   collection: Backbone JS Collection
    *   type: ('uri'|'default')
    *   autocomplete: ['item1','item2'] List of auto complete items
    * }
    *
     */
    var ListWidgetChildView = Backbone.View.extend({

        tagName: 'tr',

        events:{
            "click .btn-delete":function (e) {
            	e.preventDefault();
                //this.$el.tooltip('delete');
                this.model.destroy();
            }
        },

        initialize:function () {

            if (!this.template) {
                this.template = _.template($('#tmpl-list-widget-child').html());
            }

            this.model.bind('destroy', this.remove, this);

        },

        render:function () {
            this.$el.html(this.template(this.model.toJSON()));

            if (this.model.get('item').length > 30) {
                this.$el.tooltip({title:this.model.get('item')});
            }
            return this;
        }
    });

    var ListWidgetView = Backbone.View.extend({

        tagName: "table",

        childView:ListWidgetChildView,

        events:{
            "click .btn-add":"addItem",
            "keypress input":function (e) {
            	// trap the enter key
                if (e.which == 13) {
                    this.addItem();
                    e.preventDefault();
                    $("input", this.$el).focus();
                }
            }
        },

        initialize:function () {

            if (!this.template) {
                this.template = _.template($('#tmpl-list-widget').html());
            }

            this.$el.addClass("table table-condensed table-hover table-striped span4");
            this.collection.bind('add', this.render, this);

        },

        addItem:function(e) {
        	e.preventDefault();

        	var input_value = $("input", this.el).val().trim();

            var model;

            if (this.options.type == 'uri') {
                model = new URIModel({item:input_value});
            } else {
                model = new Backbone.Model({item:input_value});
                model.validate = function(attrs) { 
                	if(!attrs.item) {
                		return "value can't be null";
                	}
                };
            }

            // if it's valid and doesn't already exist
            if (model.get("item") != null && this.collection.where({item: input_value}).length < 1) {
                this.collection.add(model);
            } else {
                // else add a visual error indicator
                $(".control-group", this.el).addClass('error');
            }
        },

        render:function (eventName) {

            this.$el.html(this.template({placeholder:this.options.placeholder}));

            // bind autocomplete options
            if (this.options.autocomplete) {
                $('input', this.$el).typeahead({source:this.options.autocomplete});
            }

            _self = this;

            _.each(this.collection.models, function (model) {
                var el = new this.childView({model:model}).render().el;
                $("tbody", _self.el).append(el);
            }, this);

            return this;
        }
        
    });

    var BlackListModel = Backbone.Model.extend({
    	idAttribute: 'id',
    	
    	urlRoot: 'api/blacklist'
    });
    
    var BlackListCollection = Backbone.Collection.extend({
    	initialize: function() { },

    	url: "api/blacklist"
    });

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
    

    var SystemScopeModel = Backbone.Model.extend({
    	idAttribute: 'id',

    	defaults:{
    		id:null,
		description:null,
		icon:null,
		value:null,
    		defaultScope:false,
    		allowDynReg:false
    	},
    	
    	urlRoot: 'api/scopes'
    });
    
    var SystemScopeCollection = Backbone.Collection.extend({
    	idAttribute: 'id',
    	
    	model: SystemScopeModel,
    	
    	url: 'api/scopes',

    	defaultScopes: function() {
    		filtered = this.filter(function(scope) {
    			return scope.get("defaultScope") === true;
    		});
    		return new SystemScopeCollection(filtered);
    	},
    	
    	getByValue: function(value) {
    		var scopes = this.where({value: value});
    		if (scopes.length == 1) {
    			return scopes[0];
    		} else {
    			return null;
    		}
    	}
    	
    });
    
    var ClientModel = Backbone.Model.extend({

        idAttribute: "id",

        initialize: function () {

            // bind validation errors to dom elements
            // this will display form elements in red if they are not valid
            this.bind('error', function(model, errs) {
                _.map(errs, function (val, elID) {
                    $('#' + elID).addClass('error');
                });
            });

        },

        // We can pass it default values.
        defaults:{
            id:null,
            idTokenValiditySeconds: 600,
            clientName:"",
            clientSecret:"",
            registeredRedirectUri:[],
            authorizedGrantTypes:["authorization_code"],
            scope:[],
            authorities:[],
            clientDescription:"",
            logoUrl:"",
            clientId:"",
            allowRefresh:false,
            accessTokenValiditySeconds: 3600,
            refreshTokenValiditySeconds: 604800,
            displayClientSecret: false,
            generateClientSecret: false,
            requireClientSecret: true,
            allowIntrospection: false
        },

        urlRoot:"api/clients"

    });

    var ClientCollection = Backbone.Collection.extend({

        initialize: function() {
            //this.fetch();
        },

        model:ClientModel,
        url:"api/clients",
        
        getByClientId: function(clientId) {
			var clients = this.where({clientId: clientId});
			if (clients.length == 1) {
				return clients[0];
			} else {
				return null;
			}
        }
    });

    var BreadCrumbView = Backbone.View.extend({

        tagName: 'ul',

        initialize:function () {

            if (!this.template) {
                this.template = _.template($('#tmpl-breadcrumbs').html());
            }

            this.$el.addClass('breadcrumb');

            this.collection.bind('add', this.render, this);
        },

        render:function () {

            this.$el.empty();
            var parent = this;

            // go through each of the breadcrumb models
            _.each(this.collection.models, function (crumb, index) {

                // if it's the last index in the crumbs then render the link inactive
                if (index == parent.collection.size() - 1) {
                    crumb.set({active:true}, {silent:true});
                } else {
                    crumb.set({active:false}, {silent:true});
                }

                this.$el.append(this.template(crumb.toJSON()));
            }, this);

            $('#breadcrumbs').html(this.el);
        }
    });


    var ClientView = Backbone.View.extend({

        tagName: 'tr',

        initialize:function () {

            if (!this.template) {
                this.template = _.template($('#tmpl-client').html());
            }

            if (!this.scopeTemplate) {
            	this.scopeTemplate = _.template($('#tmpl-scope-list').html());
            }

            this.model.bind('change', this.render, this);
            
        },

        render:function (eventName) {
            this.$el.html(this.template(this.model.toJSON()));

            $('.scope-list', this.el).html(this.scopeTemplate({scopes: this.model.get('scope'), systemScopes: app.systemScopeList}));
            
            this.$('.dynamically-registered').tooltip({title: 'This client was dynamically registered'});
            
            return this;
        },

        events:{
            "click .btn-edit":"editClient",
            "click .btn-delete":"deleteClient",
            "click .btn-whitelist":"whiteListClient"
        },

        editClient:function () {
            app.navigate('admin/client/' + this.model.id, {trigger: true});
        },

        whiteListClient:function() {
        	var whiteList = app.whiteListList.getByClientId(this.model.get('clientId'));
        	if (whiteList == null) {
        		// create a new one
        		app.navigate('admin/whitelist/new/' + this.model.id, {trigger: true});
        	} else {
        		// edit the existing one
        		app.navigate('admin/whitelist/' + whiteList.id, {trigger: true});
        	}
        },
        
        deleteClient:function () {

            if (confirm("Are you sure sure you would like to delete this client?")) {
                var self = this;

                this.model.destroy({
                    success:function () {
                        self.$el.fadeTo("fast", 0.00, function () { //fade
                            $(this).slideUp("fast", function () { //slide up
                                $(this).remove(); //then remove from the DOM
                                app.clientListView.togglePlaceholder();
                            });
                        });
                    }
                });

                app.clientListView.delegateEvents();
            }

            return false;
        },

        close:function () {
            $(this.el).unbind();
            $(this.el).empty();
        }
    });

    var ClientListView = Backbone.View.extend({

        tagName: 'span',

        initialize:function () {
            //this.model.bind("reset", this.render, this);
        },

        events:{
            "click .new-client":"newClient",
            "click .refresh-table":"refreshTable"
        },

        newClient:function () {
            this.remove();
            app.navigate('admin/client/new', {trigger: true});
        },

        render:function (eventName) {

            // append and render table structure
            $(this.el).html($('#tmpl-client-table').html());

            _.each(this.model.models, function (client) {
                $("#client-table",this.el).append(new ClientView({model:client}).render().el);
            }, this);

            this.togglePlaceholder();
            
            return this;
        },
        
    	togglePlaceholder:function() {
    		if (this.model.length > 0) {
    			$('#client-table', this.el).show();
    			$('#client-table-empty', this.el).hide();
    		} else {
    			$('#client-table', this.el).hide();
    			$('#client-table-empty', this.el).show();
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

    var ClientFormView = Backbone.View.extend({

        tagName:"span",

        initialize:function () {

            if (!this.template) {
                this.template = _.template($('#tmpl-client-form').html());
            }

            this.registeredRedirectUriCollection = new Backbone.Collection();
            this.scopeCollection = new Backbone.Collection();
        },

        events:{
            "click .btn-save":"saveClient",
            "click #allowRefresh" : "toggleRefreshTokenTimeout",
            "click #disableAccessTokenTimeout" : function(){ $("#access-token-timeout-seconds", this.$el).prop('disabled',!$("#access-token-timeout-seconds", this.$el).prop('disabled')); },
            "click #disableIDTokenTimeout" : function(){ $("#id-token-timeout-seconds", this.$el).prop('disabled',!$("#id-token-timeout-seconds", this.$el).prop('disabled')); },
            "click #disableRefreshTokenTimeout" : function(){ $("#refresh-token-timeout-seconds", this.$el).prop('disabled',!$("#refresh-token-timeout-seconds", this.$el).prop('disabled')); },
            "click .btn-cancel": function() { window.history.back(); return false; },
            "change #requireClientSecret":"toggleRequireClientSecret",
            "change #displayClientSecret":"toggleDisplayClientSecret",
            "change #generateClientSecret":"toggleGenerateClientSecret",
            "change #logoUrl input":"previewLogo"
        },

        toggleRefreshTokenTimeout:function () {
            $("#refreshTokenValiditySeconds", this.$el).toggle();
        },
        
        previewLogo:function(event) {
        	if ($('#logoUrl input', this.el).val()) {
        		$('#logoPreview', this.el).empty();
        		$('#logoPreview', this.el).attr('src', $('#logoUrl input').val());
        	} else {
        		$('#logoBlock', this.el).hide();
        	}
        },

        /**
         * Set up the form based on the current state of the requireClientSecret checkbox parameter
         * @param event
         */
        toggleRequireClientSecret:function(event) {
        	
        	if ($('#requireClientSecret input', this.el).is(':checked')) {
        		// client secret is required, show all the bits
        		$('#clientSecretPanel', this.el).show();
        		// this function sets up the display portions
        		this.toggleGenerateClientSecret();
        	} else {
        		// no client secret, hide all the bits
        		$('#clientSecretPanel', this.el).hide();        		
        	}
        },
        
        /**
         * Set up the form based on the "Generate" checkbox
         * @param event
         */
        toggleGenerateClientSecret:function(event) {

        	if ($('#generateClientSecret input', this.el).is(':checked')) {
        		// show the "generated" block, hide the "display" checkbox
        		$('#displayClientSecret', this.el).hide();
        		$('#clientSecret', this.el).hide();
        		$('#clientSecretGenerated', this.el).show();
        		$('#clientSecretHidden', this.el).hide();
        	} else {
        		// show the display checkbox, fall back to the "display" logic
        		$('#displayClientSecret', this.el).show();
        		this.toggleDisplayClientSecret(event);
        	}
        },
        
        /**
         * Handle whether or not to display the client secret
         * @param event
         */
        toggleDisplayClientSecret:function(event) {
        	
        	if ($('#displayClientSecret input').is(':checked')) {
        		// want to display it
        		$('#clientSecret', this.el).show();
        		$('#clientSecretHidden', this.el).hide();
        		$('#clientSecretGenerated', this.el).hide();
        	} else {
        		// want to hide it
        		$('#clientSecret', this.el).hide();
        		$('#clientSecretHidden', this.el).show();
        		$('#clientSecretGenerated', this.el).hide();
        	}
        },

        getFormTokenValue:function(value) {
            if (value == "") return null;
            else return value;
        },

        // maps from a form-friendly name to the real grant parameter name
        authorizedGrantMap:{
        	"authorization_code": "authorization_code",
        	"password": "password",
        	"implicit": "implicit",
        	"client_credentials": "client_credentials",
        	"redelegate": "urn:ietf:params:oauth:grant_type:redelegate",
        	"refresh_token": "refresh_token"
        },
        
        saveClient:function (event) {

            $('.control-group').removeClass('error');

            // build the scope object
            var scopes = this.scopeCollection.pluck("item");
            
            // build the grant type object
            var authorizedGrantTypes = [];
            $.each(this.authorizedGrantMap, function(index,type) {
                if ($('#authorizedGrantTypes-' + index).is(':checked')) {
                    authorizedGrantTypes.push(type);
                }
            });

            var requireClientSecret = $('#requireClientSecret input').is(':checked');
            var generateClientSecret = $('#generateClientSecret input').is(':checked');
            var clientSecret = null;
            
            if (requireClientSecret && !generateClientSecret) {
            	// if it's required but we're not generating it, send the value
            	clientSecret = $('#clientSecret input').val();
            }

            var accessTokenValiditySeconds = null;
            if (!$('disableAccessTokenTimeout').is(':checked')) {
            	accessTokenValiditySeconds = this.getFormTokenValue($('#accessTokenValiditySeconds input[type=text]').val()); 
            }
            
            var idTokenValiditySeconds = null;
            if (!$('disableIDTokenTimeout').is(':checked')) {
            	idTokenValiditySeconds = this.getFormTokenValue($('#idTokenValiditySeconds input[type=text]').val()); 
            }
            
            var refreshTokenValiditySeconds = null;
            if ($('#allowRefresh').is(':checked')) {

            	if ($.inArray('refresh_token', authorizedGrantTypes) == -1) {
            		authorizedGrantTypes.push('refresh_token');
            	}

            	if ($.inArray('offline_access', scopes) == -1) {
                	scopes.push("offline_access");            		
            	}

            	if (!$('disableRefreshTokenTimeout').is(':checked')) {
            		refreshTokenValiditySeconds = this.getFormTokenValue($('#refreshTokenValiditySeconds input[type=text]').val()); 
            	}
            }
            
            var valid = this.model.set({
                clientName:$('#clientName input').val(),
                clientId:$('#clientId input').val(),
                clientSecret: clientSecret,
                generateClientSecret:generateClientSecret,
                registeredRedirectUri: this.registeredRedirectUriCollection.pluck("item"),
                clientDescription:$('#clientDescription textarea').val(),
                logoUrl:$('#logoUrl input').val(),
                authorizedGrantTypes: authorizedGrantTypes,
                accessTokenValiditySeconds: accessTokenValiditySeconds,
                refreshTokenValiditySeconds: refreshTokenValiditySeconds,
                idTokenValiditySeconds: idTokenValiditySeconds,
                allowRefresh: $('#allowRefresh').is(':checked'),
                allowIntrospection: $('#allowIntrospection input').is(':checked'),
                scope: scopes
            });

            // post-validate
            // TODO: move these into the validation function somehow?
            if (this.model.get("allowRefresh") == false) {
                this.model.set("refreshTokenValiditySeconds",null);
            }

            if ($('#disableIDTokenTimeout').is(':checked')) {
                 this.model.set("idTokenValiditySeconds",null);
            }

            if ($('#disableAccessTokenTimeout').is(':checked')) {
                this.model.set("accessTokenValiditySeconds",null);
            }

            if ($('#disableRefreshTokenTimeout').is(':checked')) {
                this.model.set("refreshTokenValiditySeconds",null);
            }

            if (valid) {

                var _self = this;
                this.model.save({}, {
                    success:function () {
                        app.clientList.add(_self.model);
                        app.navigate('admin/clients', {trigger:true});
                    },
                    error:function (model,resp) {
                        console.error("Oops! The object didn't save correctly.",resp);
                    }
                });
            }

            return false;
        },

        render:function (eventName) {

            $(this.el).html(this.template(this.model.toJSON()));

            
            var _self = this;

            // build and bind registered redirect URI collection and view
            _.each(this.model.get("registeredRedirectUri"), function (registeredRedirectUri) {
                _self.registeredRedirectUriCollection.add(new URIModel({item:registeredRedirectUri}));
            });

            $("#registeredRedirectUri .controls",this.el).html(new ListWidgetView({type:'uri', placeholder: 'http://',
                                                                                    collection: this.registeredRedirectUriCollection}).render().el);

            _self = this;
            // build and bind scopes
            _.each(this.model.get("scope"), function (scope) {
                _self.scopeCollection.add(new Backbone.Model({item:scope}));
            });

            $("#scope .controls",this.el).html(new ListWidgetView({placeholder: 'new scope here'
                , autocomplete: _.uniq(_.flatten(app.systemScopeList.pluck("value"))) // TODO: load from default scopes
                , collection: this.scopeCollection}).render().el);

            if (!this.model.get("allowRefresh")) {
                $("#refreshTokenValiditySeconds", this.$el).hide();
            }

            if (this.model.get("accessTokenValiditySeconds") == null) {
                $("#access-token-timeout-seconds", this.$el).prop('disabled',true);
            }

            if (this.model.get("refreshTokenValiditySeconds") == null) {
                $("#refresh-token-timeout-seconds", this.$el).prop('disabled',true);
            }

            if (this.model.get("idTokenValiditySeconds") == null) {
                $("#id-token-timeout-seconds", this.$el).prop('disabled',true);
            }

            this.toggleRequireClientSecret();
            this.previewLogo();
            
            return this;
        }
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
    		var json = {grant: this.model.toJSON(), client: this.options.client.toJSON()};
    		
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
    
    var WhiteListListView = Backbone.View.extend({
    	tagName: 'span',
    	
    	initialize:function () {
    		//this.model.bind("reset", this.render, this);
    	},
    
    	events:{
            "click .refresh-table":"refreshTable"
    	},
    	
    	render:function (eventName) {
    		$(this.el).html($('#tmpl-whitelist-table').html());
    		
    		_.each(this.model.models, function (whiteList) {
    			
    			// look up client
    			var client = app.clientList.getByClientId(whiteList.get('clientId'));
    			
    			// if there's no client ID, this is an error!
    			if (client != null) {
    				$('#whitelist-table', this.el).append(new WhiteListView({model: whiteList, client: client}).render().el);
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
    	
        refreshTable:function() {
        	var _self = this;
        	this.model.fetch({
        		success: function() {
        			_self.render();
        		}
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

    		this.model.bind('change', this.render, this);
    	},
    	
    	render:function(eventName) {
    		
    		var json = {whiteList: this.model.toJSON(), client: this.options.client.toJSON()};
    		
    		this.$el.html(this.template(json));

            $('.scope-list', this.el).html(this.scopeTemplate({scopes: this.model.get('allowedScopes'), systemScopes: app.systemScopeList}));
            
    		this.$('.dynamically-registered').tooltip({title: 'This client was dynamically registered'});

            return this;
    	},
    	
    	events:{
    		'click .btn-edit': 'editWhitelist',
    		'click .btn-delete': 'deleteWhitelist'
    	},
    	
    	editWhitelist:function() {
    		app.navigate('admin/whitelist/' + this.model.id, {trigger: true});
    	},
    	
    	deleteWhitelist:function() {
    		
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
                    }
                });
                
                app.whiteListListView.delegateEvents();
    		}
    		
    		return false;
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
    	
    	saveWhiteList:function (event) {
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
                    error:function (model,resp) {
                        console.error("Oops! The object didn't save correctly.",resp);
                    }
                });
            }

            return false;
    		
    	},
    	
    	cancelWhiteList:function(event) {
    		app.navigate('admin/whitelists', {trigger:true});
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
    
    var BlackListListView = Backbone.View.extend({
    	tagName: 'span',
    	
    	initialize:function() {
    		if (!this.template) {
    			this.template = _.template($('#tmpl-blacklist-form').html());
    		}
    	},
    	
    	events: {
            "click .refresh-table":"refreshTable"    		
    	},
    	
        refreshTable:function() {
        	var _self = this;
        	this.model.fetch({
        		success: function() {
        			_self.render();
        		}
        	});
        },	
    	
    	render:function (eventName) {
    		
    		$(this.el).html(this.template(this.model.toJSON()));
    		
			$('#blacklist .controls', this.el).html(new BlackListWidgetView({
				placeholder: 'http://',
				collection: this.model
			}).render().el);
    		
			return this;
    	}
    });

    var BlackListWidgetView = ListWidgetView.extend({
    	
    	childView: ListWidgetChildView.extend({
    		render:function() {
    			var uri = this.model.get('uri');
    			
    			this.$el.html(this.template({item: uri}));

                if (uri.length > 30) {
                    this.$el.tooltip({title:uri});
                }
                return this;
    			
    		}
    	}),
    	
    	addItem:function(e) {
        	e.preventDefault();

        	var input_value = $("input", this.el).val().trim();
        	
        	// TODO: URI/pattern validation, check against existing clients
        	
        	var item = new BlackListModel({
        		uri: input_value
        	});
        	
        	var _self = this; // closures...
        	
        	item.save({}, {
        		success:function() {
        			_self.collection.add(item);
        		}
        	});

    	}
    	
    });
    
    var SystemScopeView = Backbone.View.extend({
    	
    	tagName: 'tr',
    	
        initialize:function () {

            if (!this.template) {
                this.template = _.template($('#tmpl-system-scope').html());
            }

            this.model.bind('change', this.render, this);
            
        },
        
        events: {
    		'click .btn-edit':'editScope',
    		'click .btn-delete':'deleteScope'
    	},
    	
    	editScope:function() {
    		app.navigate('admin/scope/' + this.model.id, {trigger: true});
    	},
    	
        render:function (eventName) {
            this.$el.html(this.template(this.model.toJSON()));

            this.$('.dynamically-registered').tooltip({title: 'This client was dynamically registered'});
            
            return this;
        },
        
        deleteScope:function () {

            if (confirm("Are you sure sure you would like to delete this scope? Clients that have this scope will still be able to ask for it.")) {
                var self = this;

                this.model.destroy({
                    success:function () {
                        self.$el.fadeTo("fast", 0.00, function () { //fade
                            $(this).slideUp("fast", function () { //slide up
                                $(this).remove(); //then remove from the DOM
                                app.systemScopeListView.togglePlaceholder();
                            });
                        });
                    }
                });

                app.systemScopeListView.delegateEvents();
            }

            return false;
        },
        
        close:function () {
            $(this.el).unbind();
            $(this.el).empty();
        }
    });
    
    var SystemScopeListView = Backbone.View.extend({
    	tagName: 'span',
    	
    	events:{
    		"click .new-scope":"newScope",
    		"click .refresh-table":"refreshTable"
    	},
    
    	newScope:function() {
    		this.remove();
    		app.navigate('admin/scope/new', {trigger: true});
    	},
    	
    	refreshTable:function() {
    		var _self = this;
    		this.model.fetch({
    			success: function() {
    				_self.render();
    			}
    		});
    	},
    	
    	togglePlaceholder:function() {
    		if (this.model.length > 0) {
    			$('#scope-table', this.el).show();
    			$('#scope-table-empty', this.el).hide();
    		} else {
    			$('#scope-table', this.el).hide();
    			$('#scope-table-empty', this.el).show();
    		}
    	},
    	
    	render: function (eventName) {
    		
    		// append and render the table structure
    		$(this.el).html($('#tmpl-system-scope-table').html());
    		
    		_.each(this.model.models, function (scope) {
    			$("#scope-table", this.el).append(new SystemScopeView({model: scope}).render().el);
    		}, this);
    		
    		this.togglePlaceholder();
    		
    		return this;
    	}
    });
    
    var SystemScopeFormView = Backbone.View.extend({
    	tagName: 'span',
    	
    	initialize:function() {
    		if (!this.template) {
                	this.template = _.template($('#tmpl-system-scope-form').html());
    		}
    		if (!this.iconTemplate) {
    			this.iconTemplate = _.template($('#tmpl-system-scope-icon').html());
    		}
    		
    		// initialize our icon set into slices for the selector
    		if (!this.bootstrapIcons) {
    			this.bootstrapIcons = [];
    			
	    		var iconList = ['glass', 'music', 'search', 'envelope', 'heart', 'star',
	    		                'star-empty', 'user', 'film', 'th-large', 'th', 'th-list', 'ok',
	    		                'remove', 'zoom-in', 'zoom-out', 'off', 'signal', 'cog', 'trash',
	    		                'home', 'file', 'time', 'road', 'download-alt', 'download',
	    		                'upload', 'inbox', 'play-circle', 'repeat', 'refresh', 'list-alt',
	    		                'lock', 'flag', 'headphones', 'volume-off', 'volume-down',
	    		                'volume-up', 'qrcode', 'barcode', 'tag', 'tags', 'book',
	                			'bookmark', 'print', 'camera', 'font', 'bold', 'italic',
	                			'text-height', 'text-width', 'align-left', 'align-center',
	                			'align-right', 'align-justify', 'list', 'indent-left',
	                			'indent-right', 'facetime-video', 'picture', 'pencil',
	                			'map-marker', 'tint', 'share', 'move', 'fast-backward', 'backward',
	                			'pause', 'stop', 'forward', 'step-forward', 'eject',
	                			'chevron-right', 'plus-sign', 'minus-sign', 'remove-sign',
	                			'ok-sign', 'question-sign', 'info-sign', 'screenshot',
	                			'remove-circle', 'ok-circle', 'ban-circle', 'arrow-left',
	                			'arrow-right', 'arrow-down', 'share-alt', 'resize-full',
	                			'resize-small', 'plus', 'asterisk', 'exclamation-sign', 'gift',
	                			'leaf', 'fire', 'eye-close', 'plane', 'random', 'magnet',
	                			'chevron-up', 'chevron-down', 'retweet', 'shopping-cart',
	                			'folder-close', 'folder-open', 'resize-vertical',
	                			'resize-horizontal', 'hdd', 'bell', 'thumbs-up', 'hand-right',
	                			'hand-left', 'hand-down', 'circle-arrow-left', 'circle-arrow-up',
	                			'circle-arrow-down', 'globe', 'tasks', 'briefcase' ];
	    		
	    		var size = 3;
	    		while (iconList.length > 0) {
	    			this.bootstrapIcons.push(iconList.splice(0, size));
	    		}

    		}
    	},
    	
    	events:{
    		'click .btn-save':'saveScope',
    		'click .btn-cancel': function() {app.navigate('admin/scope', {trigger: true}); },
    		'click .btn-icon':'selectIcon'
    	},
    	
    	saveScope:function(event) {
    		
    		var value = $('#value input').val()
    		
    		if (value == null || value.trim() == "") {
    			// error: can't have a blank scope
    			return false
    		}
    		
    		var valid = this.model.set({
    			value:value,
    			description:$('#description textarea').val(),
    			icon:$('#iconDisplay input').val(),
    			defaultScope:$('#defaultScope input').is(':checked'),
    			allowDynReg:$('#allowDynReg input').is(':checked')
    		});
    		
    		if (valid) {
    			var _self = this;
    			this.model.save({}, {
    				success:function() {
    					app.systemScopeList.add(_self.model);
    					app.navigate('admin/scope', {trigger: true});
    				},
    				error:function(model,resp) {
    					console.error("The scope didn't save correctly.", resp);
    				}
    			});
    		}

    		return false;
    	},
    	
    	selectIcon:function(event) {
    		
    		var icon = event.target.value;
    		
    		$('#iconDisplay input').val(icon);
    		$('#iconDisplay span').html(icon);
    		$('#iconDisplay i').removeClass();
    		$('#iconDisplay i').addClass('icon-' + icon);
    		
    		$('#iconSelector').modal('hide');
    		
    		return false;
    	},
    	
    	render: function(eventName) {
    		this.$el.html(this.template(this.model.toJSON()));
    		
    		_.each(this.bootstrapIcons, function (items) {
    			$(".modal-body", this.el).append(this.iconTemplate({items:items}));
    		}, this);
    		
    		
    		return this;
    	}
    });
    
    // Router
    var AppRouter = Backbone.Router.extend({

        routes:{
            "admin/clients":"listClients",
            "admin/client/new":"newClient",
            "admin/client/:id":"editClient",

            "admin/whitelists":"whiteList",
            "admin/whitelist/new/:cid":"newWhitelist",
            "admin/whitelist/:id":"editWhitelist",
            
            "admin/blacklist":"blackList",
            
            "admin/scope":"siteScope",
            "admin/scope/new":"newScope",
            "admin/scope/:id":"editScope",
            
            "user/approved":"approvedSites",
            
            "": "root"
            	
        },
        
        root:function() {
        	this.navigate('user/approved', {trigger: true});
        },
        
        initialize:function () {

            this.clientList = new ClientCollection();
            this.whiteListList = new WhiteListCollection();
            this.blackListList = new BlackListCollection();
            this.approvedSiteList = new ApprovedSiteCollection();
            this.systemScopeList = new SystemScopeCollection();

            this.clientListView = new ClientListView({model:this.clientList});
            this.whiteListListView = new WhiteListListView({model:this.whiteListList});
            this.approvedSiteListView = new ApprovedSiteListView({model:this.approvedSiteList});
            this.blackListListView = new BlackListListView({model:this.blackListList});
            this.systemScopeListView = new SystemScopeListView({model:this.systemScopeList});
            
            this.breadCrumbView = new BreadCrumbView({
                collection:new Backbone.Collection()
            });

            this.breadCrumbView.render();

            //
            // Several items depend on the clients and whitelists being loaded, so we're going to pre-fetch them here
            // and not start the app router until they're loaded.
            //
            
            // load things in the right order:
            this.systemScopeList.fetch({
            	success: function(collection, response) {
            		app.clientList.fetch({
            			success: function(collection, response) {
            				app.whiteListList.fetch({
            					success: function(collection, response) {
            						var baseUrl = $.url($('base').attr('href'));                
            						Backbone.history.start({pushState: true, root: baseUrl.attr('relative') + 'manage/'});
            					}
            				});
            			}
            		});
            	}
            });

        },

        listClients:function () {

            this.breadCrumbView.collection.reset();
            this.breadCrumbView.collection.add([
                {text:"Home", href:""},
                {text:"Manage Clients", href:"manage/#admin/clients"}
            ]);

            $('#content').html(this.clientListView.render().el);
            this.clientListView.delegateEvents();

        },

        newClient:function() {

            this.breadCrumbView.collection.reset();
            this.breadCrumbView.collection.add([
                {text:"Home", href:""},
                {text:"Manage Clients", href:"manage/#admin/clients"},
                {text:"New", href:""}
            ]);

        	var client = new ClientModel();
        	
        	// set up this new client to require a secret and have us autogenerate one
        	client.set({
        		requireClientSecret:true, 
        		generateClientSecret:true,
        		displayClientSecret:false,
        		scope: _.uniq(_.flatten(this.systemScopeList.defaultScopes().pluck("value"))),
        	}, { silent: true });
        	
            this.clientFormView = new ClientFormView({model:client});
            $('#content').html(this.clientFormView.render().el);
        },

        editClient:function(id) {

            this.breadCrumbView.collection.reset();
            this.breadCrumbView.collection.add([
                {text:"Home", href:""},
                {text:"Manage Clients", href:"manage/#admin/clients"},
                {text:"Edit", href:"manage/#admin/client/" + id}
            ]);

            var client = this.clientList.get(id);

            if (client.get("clientSecret") == null) {
            	client.set({
            		requireClientSecret:false
            	}, { silent: true });
            }
            
            if ($.inArray("refresh_token", client.get("authorizedGrantTypes")) != -1) {
            	client.set({
            		allowRefresh: true
            	}, { silent: true });
            }
            
        	client.set({
        		generateClientSecret:false,
        		displayClientSecret:false
        	}, { silent: true });
            
            this.clientFormView = new ClientFormView({model:client});
            $('#content').html(this.clientFormView.render().el);
        },

        whiteList:function () {
            this.breadCrumbView.collection.reset();
            this.breadCrumbView.collection.add([
                {text:"Home", href:""},
                {text:"Manage Whitelisted Sites", href:"manage/#admin/whitelists"}
            ]);
            
            $('#content').html(this.whiteListListView.render().el);
            this.whiteListListView.delegateEvents();
        },
        
        newWhitelist:function(cid) {
            var client = this.clientList.get(cid);

            // if there's no client this is an error
            if (client != null) {

            	this.breadCrumbView.collection.reset();
                this.breadCrumbView.collection.add([
                    {text:"Home", href:""},
                    {text:"Manage Whitelisted Sites", href:"manage/#admin/whitelists"},
                    {text:"Manage Whitelisted Sites", href:"manage/#admin/whitelist/new/" + cid}
                ]);
                
                var whiteList = new WhiteListModel();
                whiteList.set({
                	clientId: client.get('clientId'),
                	allowedScopes: client.get('scope')
                }, { silent: true });
                
            	this.whiteListFormView = new WhiteListFormView({model: whiteList, client: client});
            	$('#content').html(this.whiteListFormView.render().el);
            } else {
            	console.log('ERROR: no client found for ' + cid);
            }
            
        	
        },
        
        editWhitelist:function(id) {
            this.breadCrumbView.collection.reset();
            this.breadCrumbView.collection.add([
                {text:"Home", href:""},
                {text:"Manage Whitelisted Sites", href:"manage/#admin/whitelists"},
                {text:"Manage Whitelisted Sites", href:"manage/#admin/whitelist/" + id}
            ]);
            
            var whiteList = this.whiteListList.get(id);
            if (whiteList != null) {
	            var client = app.clientList.getByClientId(whiteList.get('clientId'));
	            
	            // if there's no client, this is an error
	            if (client != null) {
	            	this.whiteListFormView = new WhiteListFormView({model: whiteList, client: client});
	            	$('#content').html(this.whiteListFormView.render().el);
	            } else {
	            	console.log('ERROR: no client found for ' + whiteList.get('clientId'));
	            }
            } else {
            	console.error('ERROR: no whitelist found for id ' + id);
            }
        },
        
        approvedSites:function() {
        
            this.breadCrumbView.collection.reset();
            this.breadCrumbView.collection.add([
                {text:"Home", href:""},
                {text:"Manage Approved Sites", href:"manage/#user/approve"}
            ]);

        	var view = this.approvedSiteListView;
        	
        	this.approvedSiteList.fetch({success: 
        		function(collection, response, options) {
        			$('#content').html(view.render().el);
        		}
        	});
        	
        },
        
        blackList:function() {
            this.breadCrumbView.collection.reset();
            this.breadCrumbView.collection.add([
                {text:"Home", href:""},
                {text:"Manage Blacklisted Sites", href:"manage/#admin/blacklist"}
            ]);
            
            var view = this.blackListListView;
            
            this.blackListList.fetch({success:
            	function(collection, response, options) {
            		$('#content').html(view.render().el);
            	}
            });
        },
        
        siteScope:function() {
        	this.breadCrumbView.collection.reset();
        	this.breadCrumbView.collection.add([
                 {text:"Home", href:""},
                 {text:"Mange System Scopes", href:"manage/#admin/scope"}
	        ]);
        	
        	$('#content').html(this.systemScopeListView.render().el);
            this.systemScopeListView.delegateEvents();
        },
        
        newScope:function() {
        	this.breadCrumbView.collection.reset();
        	this.breadCrumbView.collection.add([
                 {text:"Home", href:""},
                 {text:"Mange System Scopes", href:"manage/#admin/scope"},
                 {text:"New", href:"manage/#admin/scope/new"}
	        ]);
        	
        	var scope = new SystemScopeModel();
        	
        	this.systemScopeFormView = new SystemScopeFormView({model:scope});
        	$('#content').html(this.systemScopeFormView.render().el);
        },
        
        editScope:function(sid) {
        	this.breadCrumbView.collection.reset();
        	this.breadCrumbView.collection.add([
                 {text:"Home", href:""},
                 {text:"Mange System Scopes", href:"manage/#admin/scope"},
                 {text:"Edit", href:"manage/#admin/scope/" + sid}
	        ]);

        	var scope = this.systemScopeList.get(sid);
        	
        	this.systemScopeFormView = new SystemScopeFormView({model:scope});
        	$('#content').html(this.systemScopeFormView.render().el);
        	
        }


    });

    // holds the global app.
    // this gets init after the templates load
    var app = null;

    // main
    $(function () {

        jQuery.ajaxSetup({async:false});

        var _load = function (templates) {
            $('body').append(templates);
        };

        // load templates and append them to the body
        $.get('resources/template/admin.html', _load);

        jQuery.ajaxSetup({async:true});
        app = new AppRouter();

        // grab all hashed URLs and send them through the app router instead
        $('a[href*="#"]').on('click', function(event) {
        	event.preventDefault();
        	app.navigate(this.hash.slice(1), {trigger: true});
        });
        
    });


