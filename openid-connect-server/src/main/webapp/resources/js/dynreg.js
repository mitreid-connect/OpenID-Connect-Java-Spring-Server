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
	
	render:function() {
		$(this.el).html(this.template(this.model.toJSON()));
		
		return this;
	}
	
});