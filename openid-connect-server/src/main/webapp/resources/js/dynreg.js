var DynRegClient = Backbone.Model.extend({
    idAttribute: "client_id",

    defaults:{
        client_id:null,
        client_secret:null,
        redirect_uris:[],
        client_name:null,
        client_uri:null,
        logo_uri:null,
        contacts:[],
        tos_uri:null,
        token_endpoint_auth_method:null,
        scope:null,
        grant_types:[],
        response_types:[],
        policy_uri:null,
        jwks_uri:null,
        
        application_type:null,
        sector_identifier_uri:null,
        subject_type:null,
        
        request_object_signing_alg:null,
        
        userinfo_signed_response_alg:null,
        userinfo_encrypted_response_alg:null,
        userinfo_encrypted_response_enc:null,
        
        id_token_signed_response_alg:null,
        id_token_encrypted_response_alg:null,
        id_token_encrypted_response_enc:null,
        
        default_max_age:null,
        require_auth_time:false,
        default_acr_values:null,
        
        initiate_login_uri:null,
        post_logout_redirect_uri:null,
        
        request_uris:[],
        
        client_description:null,
        
        registration_access_token:null,
        registration_client_uri:null
    },
    
    sync: function(method, model, options){
    	console.log('Sync! ' + method);
    	console.log(model);
    	console.log(options);
    	if (model.get('registration_access_token')) {
    		var headers = options.headers ? options.headers : {};
    		headers['Authorization'] = 'Bearer ' + model.get('registration_access_token');
    		options.headers = headers;
    		console.log('Added token to request');
    		console.log(options);
    	}
    	
    	return this.constructor.__super__.sync(method, model, options);
    },

    urlRoot:'register'
    
});

var DynRegRootView = Backbone.View.extend({
	
	tagName: 'span',
	
	initialize:function() {
		
	},
	
	events:{
		"click #newreg":"newReg",
		"click #editreg":"editReg"
	},
	
	render:function() {
    	$(this.el).html($('#tmpl-dynreg').html());
    	return this;
	},
	
	newReg:function() {
        this.remove();
        app.navigate('dev/dynreg/new', {trigger: true});		
	},
	
	editReg:function() {
		var clientId = $('#clientId').val();
		var token = $('#regtoken').val();
		
		var client = new DynRegClient({
			client_id: clientId,
			registration_access_token: token
		});
		
		console.log(client.get('registration_access_token'));

		client.fetch({success: function() {
			console.log(client);
			
			var dynRegEditView = new DynRegEditView({model: client});
			
			$('#content').html(dynRegEditView.render().el);
		}});
		app.navigate('dev/dynreg/edit', {trigger: true});
		this.remove();
	}
	
});

var DynRegEditView = Backbone.View.extend({
	
	tagName: 'span',
	
	initialize:function() {
        if (!this.template) {
            this.template = _.template($('#tmpl-dynreg-client-form').html());
        }

        this.redirectUrisCollection = new Backbone.Collection();
        this.scopeCollection = new Backbone.Collection();
        this.contactsCollection = new Backbone.Collection();
        this.defaultAcrValuesCollection = new Backbone.Collection();
        this.requestUrisCollection = new Backbone.Collection();
	},
	
    previewLogo:function(event) {
    	if ($('#logoUri input', this.el).val()) {
    		$('#logoPreview', this.el).empty();
    		$('#logoPreview', this.el).attr('src', $('#logoUri input').val());
    	} else {
    		$('#logoBlock', this.el).hide();
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

    // returns "null" if given the value "default" as a string, otherwise returns input value. useful for parsing the JOSE algorithm dropdowns
    defaultToNull:function(value) {
    	if (value == 'default') {
    		return null;
    	} else {
    		return value;
    	}
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

    render:function() {
		console.log(this.model.toJSON());
		$(this.el).html(this.template({client: this.model.toJSON()}));
		
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
        var scopeSet = this.model.get("scope").split(" ");
        _.each(scopeSet, function (scope) {
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

        this.previewLogo();
        
        // disable unsupported JOSE algorithms
        this.disableUnsupportedJOSEItems(app.serverConfiguration.request_object_signing_alg_values_supported, '#requestObjectSigningAlg option');
        this.disableUnsupportedJOSEItems(app.serverConfiguration.userinfo_signing_alg_values_supported, '#userInfoSignedResponseAlg option');
        this.disableUnsupportedJOSEItems(app.serverConfiguration.userinfo_encryption_alg_values_supported, '#userInfoEncryptedResponseAlg option');
        this.disableUnsupportedJOSEItems(app.serverConfiguration.userinfo_encryption_enc_values_supported, '#userInfoEncryptedResponseEnc option');
        this.disableUnsupportedJOSEItems(app.serverConfiguration.id_token_signing_alg_values_supported, '#idTokenSignedResponseAlg option');
        this.disableUnsupportedJOSEItems(app.serverConfiguration.id_token_encryption_alg_values_supported, '#idTokenEncryptedResponseAlg option');
        this.disableUnsupportedJOSEItems(app.serverConfiguration.id_token_encryption_enc_values_supported, '#idTokenEncryptedResponseEnc option');
        
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