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
        redirectUris:[],
        grantTypes:["authorization_code"],
        scope:[],
        authorities:[],
        clientDescription:"",
        logoUri:"",
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

        this.redirectUrisCollection = new Backbone.Collection();
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
        "change #logoUri input":"previewLogo"
    },

    toggleRefreshTokenTimeout:function () {
        $("#refreshTokenValiditySeconds", this.$el).toggle();
    },
    
    previewLogo:function(event) {
    	if ($('#logoUri input', this.el).val()) {
    		$('#logoPreview', this.el).empty();
    		$('#logoPreview', this.el).attr('src', $('#logoUri input').val());
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
    grantMap:{
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
        var grantTypes = [];
        $.each(this.grantMap, function(index,type) {
            if ($('#grantTypes-' + index).is(':checked')) {
                grantTypes.push(type);
            }
        });

        var requireClientSecret = $('#requireClientSecret input').is(':checked');
        var generateClientSecret = $('#generateClientSecret input').is(':checked');
        var clientSecret = null;
        
        if (requireClientSecret && !generateClientSecret) {
        	// if it's required but we're not generating it, send the value to preserve it
        	clientSecret = $('#clientSecret input').val();
        }

        // TODO: validate  these as integers
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

        	if ($.inArray('refresh_token', grantTypes) == -1) {
        		grantTypes.push('refresh_token');
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
            redirectUris: this.redirectUrisCollection.pluck("item"),
            clientDescription:$('#clientDescription textarea').val(),
            logoUri:$('#logoUri input').val(),
            grantTypes: grantTypes,
            accessTokenValiditySeconds: accessTokenValiditySeconds,
            refreshTokenValiditySeconds: refreshTokenValiditySeconds,
            idTokenValiditySeconds: idTokenValiditySeconds,
            allowRefresh: $('#allowRefresh').is(':checked'), // TODO: why are these two checkboxes different?
            allowIntrospection: $('#allowIntrospection input').is(':checked'), // <-- And here? --^
            scope: scopes,
            
            // TODO: items below this line are untested
            tosUri: $('#tosUri input').val(),
            policyUri: $('#policyUri input').val(),
            clientUri: $('#clientUri input').val(),
            applicationType: $('#applicationType input').filter(':checked').val(),
            
            
            
            // TODO: everything below this line isn't implemented yet
            contacts: this.contactsCollection.pluck('item'),
            tokenEndpointAuthMethod: $('#tokenEndpointAuthMethod input').val(), // TODO: this might need to be something different for a single-select?
            responseTypes: responseTypes, // TODO: need a preprocessor?
            jwksUri: $('#jwksUri input').val(),
            sectorIdentifierUri: $('#sectorIdentifierUri input').val(),
            subjectType: subjectType, // TODO: need a preprocessor?
            requestObjectSigningAlg: requestObjectSigningAlg,           // TODO: need a preprocessor for all the JOSE stuff:
            userInfoEncryptedResponseAlg: userInfoEncryptedResponseAlg, // "
            userInfoEncryptedResponseEnc: userInfoEncryptedResponseEnc, // "
            idTokenSignedResponseAlg: idTokenSignedResponseAlg,         // "
            idTokenEncryptedResponseAlg: idTokenEncryptedResponseAlg,   // "
            idTokenEncryptedResponseEnc: idTokenEncryptedResponseEnc,   // "
            defaultMaxAge: $('#defaultMaxAge input').val(), // TODO: validate integer
            requireAuthTime: $('#requireAuthTime input').is(':checked'),
            defaultAcrValues: this.defaultAcrValuesCollection.pluck('item'),
            initiateLoginUri: $('#initiateLoginUri input').val(),
            postLogoutRedirectUri: $('#postLogoutRedirectUri input').val(),
            requestUris: this.requestUrisCollection.pluck('item'),
            resourceIds: this.resourceIdsCollection.pluck('item'),
            reuseRefreshToken: $('#reuseRefreshToken input').is(':checked')
            
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
        _.each(this.model.get("redirectUris"), function (redirectUri) {
            _self.redirectUrisCollection.add(new URIModel({item:redirectUri}));
        });

        $("#redirectUris .controls",this.el).html(new ListWidgetView({type:'uri', placeholder: 'http://',
                                                                                collection: this.redirectUrisCollection}).render().el);

        _self = this;
        // build and bind scopes
        _.each(this.model.get("scope"), function (scope) {
            _self.scopeCollection.add(new Backbone.Model({item:scope}));
        });

        $("#scope .controls",this.el).html(new ListWidgetView({placeholder: 'new scope here'
            , autocomplete: _.uniq(_.flatten(app.systemScopeList.pluck("value")))
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


