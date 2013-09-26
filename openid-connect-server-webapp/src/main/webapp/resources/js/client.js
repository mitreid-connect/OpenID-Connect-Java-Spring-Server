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
        
        clientId:"",
        clientSecret:"",
        redirectUris:[],
        clientName:null,
        clientUri:"",
        logoUri:"",
        contacts:[],
        tosUri:"",
        tokenEndpointAuthMethod:null,
        scope:[],
        grantTypes:["authorization_code"],
        responseTypes:[],
        policyUri:"",
        jwksUri:"",
        
        applicationType:null,
        sectorIdentifierUri:"",
        subjectType:null,
        
        requestObjectSigningAlg:null,
        
        userInfoSignedResponseAlg:null,
        userInfoEncryptedResponseAlg:null,
        userInfoEncryptedResponseEnc:null,
        
        idTokenSignedResponseAlg:null,
        idTokenEncryptedResponseAlg:null,
        idTokenEncryptedResponseEnc:null,
        
        tokenEndpointAuthSigningAlg:null,
        
        defaultMaxAge:60000,
        requireAuthTime:false,
        defaultACRvalues:null,
        
        initiateLoginUri:"",
        postLogoutRedirectUri:"",
        
        requestUris:[],
        
        authorities:[],
        accessTokenValiditySeconds: null,
        refreshTokenValiditySeconds: null,
        resourceIds:[],
        //additionalInformation?
        
        clientDescription:"",
        reuseRefreshToken:true,
        dynamicallyRegistered:false,
        allowIntrospection:false,
        idTokenValiditySeconds: null,
        createdAt:null,     

        allowRefresh:false,
        displayClientSecret: false,
        generateClientSecret: false,
        requireClientSecret: true,
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
    	var json = {client: this.model.toJSON(), count: this.options.count};
        this.$el.html(this.template(json));

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
                },
                error:function (error, response) {
            		console.log("An error occurred when deleting a client");
    
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
            $("#client-table",this.el).append(
            		new ClientView({
            				model:client, 
            				count:this.options.stats.get(client.get('id'))
            			}).render().el);
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
    	_self.model.fetch({
    		success: function() {
    			_self.options.stats.fetch({
    				success: function () {
    					_self.render();
    				}
    			});
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
        this.contactsCollection = new Backbone.Collection();
        this.defaultAcrValuesCollection = new Backbone.Collection();
        this.requestUrisCollection = new Backbone.Collection();
        // TODO: add Spring authorities collection and resource IDs collection?
    },

    events:{
        "click .btn-save":"saveClient",
        "click #allowRefresh" : "toggleRefreshTokenTimeout",
        "click #disableAccessTokenTimeout" : function() { 
        	$("#access-token-timeout-time", this.$el).prop('disabled',!$("#access-token-timeout-time", this.$el).prop('disabled')); 
        	$("#access-token-timeout-unit", this.$el).prop('disabled',!$("#access-token-timeout-unit", this.$el).prop('disabled')); 
        	document.getElementById("access-token-timeout-time").value = '';
        	},
        "click #disableRefreshTokenTimeout" : function() { 
        	$("#refresh-token-timeout-time", this.$el).prop('disabled',!$("#refresh-token-timeout-time", this.$el).prop('disabled'));
        	$("#refresh-token-timeout-unit", this.$el).prop('disabled',!$("#refresh-token-timeout-unit", this.$el).prop('disabled')); 
        	document.getElementById("refresh-token-timeout-time").value = ''; 	
        	},
        "click .btn-cancel": function() { window.history.back(); return false; },
        "change #requireClientSecret":"toggleRequireClientSecret",
        "change #displayClientSecret":"toggleDisplayClientSecret",
        "change #generateClientSecret":"toggleGenerateClientSecret",
        "change #logoUri input":"previewLogo"
    },

    toggleRefreshTokenTimeout:function () {
        $("#refreshTokenValidityTime", this.$el).toggle();
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

    // rounds down to the nearest integer value in seconds.
    getFormTokenNumberValue:function(value, timeUnit) {
        if (value == "") {
        	return null;
        } else if (timeUnit == 'hours') {
        	return parseInt(parseFloat(value) * 3600);
        } else if (timeUnit == 'minutes') {
        	return parseInt(parseFloat(value) * 60);
        } else { // seconds
        	return parseInt(value);
        }
    },
    
    // returns "null" if given the value "default" as a string, otherwise returns input value. useful for parsing the JOSE algorithm dropdowns
    defaultToNull:function(value) {
    	if (value == 'default') {
    		return null;
    	} else {
    		return value;
    	}
    },
    
    disableUnsupportedJOSEItems:function(serverSupported, query) {
        var supported = ['default'];
        if (serverSupported) {
        	supported = _.union(supported, serverSupported);
        }
        $(query, this.$el).each(function(idx) {
        	if(_.contains(supported, $(this).val())) {
        		$(this).prop('disabled', false);
        	} else {
        		$(this).prop('disabled', true);
        	}
        });
    	
    },

    // maps from a form-friendly name to the real grant parameter name
    grantMap:{
    	'authorization_code': 'authorization_code',
    	'password': 'password',
    	'implicit': 'implicit',
    	'client_credentials': 'client_credentials',
    	'redelegate': 'urn:ietf:params:oauth:grant_type:redelegate',
    	'refresh_token': 'refresh_token'
    },
    
    // maps from a form-friendly name to the real response type parameter name
    responseMap:{
    	'code': 'code',
    	'token': 'token',
    	'idtoken': 'id_token',
    	'token-idtoken': 'token id_token',
    	'code-idtoken': 'code id_token',
    	'code-token': 'code token',
    	'code-token-idtoken': 'code token id_token'
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
        
        // build the response type object
        var responseTypes = [];
        $.each(this.responseMap, function(index,type) {
        	if ($('#responseTypes-' + index).is(':checked')) {
        		responseTypes.push(type);
        	}
        });

        var requireClientSecret = $('#requireClientSecret input').is(':checked');
        var generateClientSecret = $('#generateClientSecret input').is(':checked');
        var clientSecret = null;
        
        if (requireClientSecret && !generateClientSecret) {
        	// if it's required but we're not generating it, send the value to preserve it
        	clientSecret = $('#clientSecret input').val();
        }

        var accessTokenValiditySeconds = null;
        if (!$('disableAccessTokenTimeout').is(':checked')) {
        	accessTokenValiditySeconds = this.getFormTokenNumberValue($('#accessTokenValidityTime input[type=text]').val(), $('#accessTokenValidityTime select').val()); 
        }
        
        var idTokenValiditySeconds = this.getFormTokenNumberValue($('#idTokenValidityTime input[type=text]').val(), $('#idTokenValidityTime select').val()); 
        
        var refreshTokenValiditySeconds = null;
        if ($('#allowRefresh').is(':checked')) {

        	if ($.inArray('refresh_token', grantTypes) == -1) {
        		grantTypes.push('refresh_token');
        	}

        	if ($.inArray('offline_access', scopes) == -1) {
            	scopes.push("offline_access");            		
        	}

        	if (!$('disableRefreshTokenTimeout').is(':checked')) {
        		refreshTokenValiditySeconds = this.getFormTokenNumberValue($('#refreshTokenValidityTime input[type=text]').val(), $('#refreshTokenValidityTime select').val());
        	}
        }
        
        var attrs = {
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
            allowRefresh: $('#allowRefresh').is(':checked'),
            allowIntrospection: $('#allowIntrospection input').is(':checked'), // <-- And here? --^
            scope: scopes,
            
            // TODO: items below this line are untested
            tosUri: $('#tosUri input').val(),
            policyUri: $('#policyUri input').val(),
            clientUri: $('#clientUri input').val(),
            applicationType: $('#applicationType input').filter(':checked').val(),
            jwksUri: $('#jwksUri input').val(),
            subjectType: $('#applicationType input').filter(':checked').val(),
            tokenEndpointAuthMethod: $('#tokenEndpointAuthMethod input').filter(':checked').val(),
            responseTypes: responseTypes,
            sectorIdentifierUri: $('#sectorIdentifierUri input').val(),
            initiateLoginUri: $('#initiateLoginUri input').val(),
            postLogoutRedirectUri: $('#postLogoutRedirectUri input').val(),
            reuseRefreshToken: $('#reuseRefreshToken').is(':checked'),
            requireAuthTime: $('#requireAuthTime input').is(':checked'),
            defaultMaxAge: parseInt($('#defaultMaxAge input').val()),
            contacts: this.contactsCollection.pluck('item'),
            requestUris: this.requestUrisCollection.pluck('item'),
            defaultAcrValues: this.defaultAcrValuesCollection.pluck('item'),
            requestObjectSigningAlg: this.defaultToNull($('#requestObjectSigningAlg select').val()),
            userInfoSignedResponseAlg: this.defaultToNull($('#userInfoSignedResponseAlg select').val()),
            userInfoEncryptedResponseAlg: this.defaultToNull($('#userInfoEncryptedResponseAlg select').val()),
            userInfoEncryptedResponseEnc: this.defaultToNull($('#userInfoEncryptedResponseEnc select').val()),
            idTokenSignedResponseAlg: this.defaultToNull($('#idTokenSignedResponseAlg select').val()),
            idTokenEncryptedResponseAlg: this.defaultToNull($('#idTokenEncryptedResponseAlg select').val()),
            idTokenEncryptedResponseEnc: this.defaultToNull($('#idTokenEncryptedResponseEnc select').val()),
            tokenEndpointAuthSigningAlg: this.defaultToNull($('#tokenEndpointAuthSigningAlg select').val())
        };

        // post-validate
        if (attrs["allowRefresh"] == false) {
            attrs["refreshTokenValiditySeconds"] = null;
        }

        if ($('#disableAccessTokenTimeout').is(':checked')) {
            attrs["accessTokenValiditySeconds"] = null;
        }

        if ($('#disableRefreshTokenTimeout').is(':checked')) {
            attrs["refreshTokenValiditySeconds"] = null;
        }

        // set all empty strings to nulls
        for (var key in attrs) {
        	if (attrs[key] === "") {
        		attrs[key] = null;
        	}
        }
        
        var _self = this;
        this.model.save(attrs, {
            success:function () {
                app.clientList.add(_self.model);
                app.navigate('admin/clients', {trigger:true});
            },
            error:function (error, response) {
        		console.log("An error occurred when deleting from a list widget");

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

        return false;
    },

    render:function (eventName) {

        $(this.el).html(this.template(this.model.toJSON()));

        
        var _self = this;

        // build and bind registered redirect URI collection and view
        _.each(this.model.get("redirectUris"), function (redirectUri) {
            _self.redirectUrisCollection.add(new URIModel({item:redirectUri}));
        });

        $("#redirectUris .controls",this.el).html(new ListWidgetView({
        	type:'uri', 
        	placeholder: 'http://',
        	collection: this.redirectUrisCollection}).render().el);
        
        // build and bind scopes
        _.each(this.model.get("scope"), function (scope) {
            _self.scopeCollection.add(new Backbone.Model({item:scope}));
        });

        $("#scope .controls",this.el).html(new ListWidgetView({
        	placeholder: 'new scope', 
        	autocomplete: _.uniq(_.flatten(app.systemScopeList.pluck("value"))), 
            collection: this.scopeCollection}).render().el);

        // build and bind contacts
        _.each(this.model.get('contacts'), function (contact) {
        	_self.contactsCollection.add(new Backbone.Model({item:contact}));
        });
        
        $("#contacts .controls", this.el).html(new ListWidgetView({
        	placeholder: 'new contact',
        	collection: this.contactsCollection}).render().el);
        
        
        // build and bind request URIs
        _.each(this.model.get('requestUris'), function (requestUri) {
        	_self.requestUrisCollection.add(new URIModel({item:requestUri}));
        });
        
        $('#requestUris .controls', this.el).html(new ListWidgetView({
        	type: 'uri',
        	placeholder: 'http://',
        	collection: this.requestUrisCollection}).render().el);
        
        // build and bind default ACR values
        _.each(this.model.get('defaultAcrValues'), function (defaultAcrValue) {
        	_self.defaultAcrValuesCollection.add(new Backbone.Model({item:defaultAcrValue}));
        });
        
        $('#defaultAcrValues .controls', this.el).html(new ListWidgetView({
        	placeholder: 'new ACR value',
        	// TODO: autocomplete from spec
        	collection: this.defaultAcrValuesCollection}).render().el);
        
        // build and bind 
        
        // set up token  fields
        if (!this.model.get("allowRefresh")) {
            $("#refreshTokenValidityTime", this.$el).hide();
        }

        if (this.model.get("accessTokenValiditySeconds") == null) {
            $("#access-token-timeout-time", this.$el).prop('disabled',true);
            $("#access-token-timeout-unit", this.$el).prop('disabled',true);
        }

        if (this.model.get("refreshTokenValiditySeconds") == null) {
            $("#refresh-token-timeout-time", this.$el).prop('disabled',true);
            $("#refresh-token-timeout-unit", this.$el).prop('disabled',true);
        }

        // toggle other dynamic fields
        this.toggleRequireClientSecret();
        this.previewLogo();
        
        // disable unsupported JOSE algorithms
        this.disableUnsupportedJOSEItems(app.serverConfiguration.request_object_signing_alg_values_supported, '#requestObjectSigningAlg option');
        this.disableUnsupportedJOSEItems(app.serverConfiguration.userinfo_signing_alg_values_supported, '#userInfoSignedResponseAlg option');
        this.disableUnsupportedJOSEItems(app.serverConfiguration.userinfo_encryption_alg_values_supported, '#userInfoEncryptedResponseAlg option');
        this.disableUnsupportedJOSEItems(app.serverConfiguration.userinfo_encryption_enc_values_supported, '#userInfoEncryptedResponseEnc option');
        this.disableUnsupportedJOSEItems(app.serverConfiguration.id_token_signing_alg_values_supported, '#idTokenSignedResponseAlg option');
        this.disableUnsupportedJOSEItems(app.serverConfiguration.id_token_encryption_alg_values_supported, '#idTokenEncryptedResponseAlg option');
        this.disableUnsupportedJOSEItems(app.serverConfiguration.id_token_encryption_enc_values_supported, '#idTokenEncryptedResponseEnc option');
        this.disableUnsupportedJOSEItems(app.serverConfiguration.token_endpoint_auth_signing_alg_values_supported, '#tokenEndpointAuthSigningAlg option');
        
        this.$('.nyi').clickover({
        	placement: 'right', 
        	title: 'Not Yet Implemented', 
        	content: 'The value of this field will be saved with the client, '
        		+'but the server does not currently process anything with it. '
        		+'Future versions of the server library will make use of this.'
        	});
        
       return this;
    }
});


