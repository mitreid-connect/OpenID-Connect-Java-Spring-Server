

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

        validate:{
            clientName:{
               /* required:true,
                pattern:/^[\w ]+$/,
                minlength:3,*/
                maxlength:100
            },
            clientDescription:{
                /*required:true,
                pattern:/^[\w ]+$/,
                minlength:3,*/
                maxlength:200
            },
            accessTokenValiditySeconds: {
                required: true,
                type:"number"
            },
            refreshTokenValiditySeconds: {
                required: true,
                type:"number"
            },
            registeredRedirectUri: {
                custom: 'validateURI'
            }
        },

        validateURI: function(attributeName, attributeValue) {

            var expression = /^(?:([a-z0-9+.-]+:\/\/)((?:(?:[a-z0-9-._~!$&'()*+,;=:]|%[0-9A-F]{2})*)@)?((?:[a-z0-9-._~!$&'()*+,;=]|%[0-9A-F]{2})*)(:(?:\d*))?(\/(?:[a-z0-9-._~!$&'()*+,;=:@\/]|%[0-9A-F]{2})*)?|([a-z0-9+.-]+:)(\/?(?:[a-z0-9-._~!$&'()*+,;=:@]|%[0-9A-F]{2})+(?:[a-z0-9-._~!$&'()*+,;=:@\/]|%[0-9A-F]{2})*)?)(\?(?:[a-z0-9-._~!$&'()*+,;=:\/?@]|%[0-9A-F]{2})*)?(#(?:[a-z0-9-._~!$&'()*+,;=:\/?@]|%[0-9A-F]{2})*)?$/i;
            var regex = new RegExp(expression);


            for (var i in attributeValue) {
                if (!attributeValue[i].match(regex)) {
                    return "Invalid URI";
                }
            }


        },

        // We can pass it default values.
        defaults:{
            id:null,
            idTokenValiditySeconds: 0,
            applicationName:"",
            clientSecret:"",
            registeredRedirectUri:[],
            authorizedGrantTypes:["authorization_code"],
            scope:["openid"],
            authorities:[],
            clientDescription:"",
            clientId:"",
            allowRefresh:false,
            accessTokenValiditySeconds: 0,
            refreshTokenValiditySeconds: 0,
            displayClientSecret: false,
            generateClientSecret: false,
            requireClientSecret: true
        },

        urlRoot:"api/clients"

    });

    var ClientCollection = Backbone.Collection.extend({

        initialize: function() {
            this.fetch();
        },

        model:ClientModel,
        url:"api/clients"
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

            this.model.bind('change', this.render, this);
        },

        render:function (eventName) {
            this.$el.html(this.template(this.model.toJSON()));
            return this;
        },

        events:{
            "click .btn-edit":"editClient",
            "click .btn-delete":"deleteClient"
        },

        editClient:function () {
            app.navigate('client/' + this.model.id, {trigger: true});
        },

        deleteClient:function () {

            if (confirm("Are you sure sure you would like to delete this client?")) {
                var self = this;

                this.model.destroy({
                    success:function () {
                        self.$el.fadeTo("fast", 0.00, function () { //fade
                            $(this).slideUp("fast", function () { //slide up
                                $(this).remove(); //then remove from the DOM
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
            this.model.bind("reset", this.render, this);
        },

        events:{
            "click .btn-primary":"newClient"
        },

        newClient:function () {
            this.remove();
            app.navigate('client/new', {trigger: true});
        },

        render:function (eventName) {

            // append and render table structure
            $(this.el).html($('#tmpl-client-table').html());

            _.each(this.model.models, function (client) {
                $("#client-table",this.el).append(new ClientView({model:client}).render().el);
            }, this);

            return this;
        }
    });
    // this is an innocuous change
    var ClientFormView = Backbone.View.extend({

        tagName:"span",

        initialize:function () {

            if (!this.template) {
                this.template = _.template($('#tmpl-client-form').html());
            }
        },

        events:{
            "click .btn-primary":"saveClient",
            "click .btn-cancel": function() { window.history.back(); return false; },
            "change #requireClientSecret":"toggleRequireClientSecret",
            "change #displayClientSecret":"toggleDisplayClientSecret",
            "change #generateClientSecret":"toggleGenerateClientSecret"
        },

        /**
         * Set up the form based on the current state of the requireClientSecret checkbox parameter
         * @param event
         */
        toggleRequireClientSecret:function(event) {
        	
        	if ($('#requireClientSecret input').is(':checked')) {
        		// client secret is required, show all the bits
        		$('#clientSecretPanel').show();
        		// this function sets up the display portions
        		this.toggleGenerateClientSecret();
        	} else {
        		// no client secret, hide all the bits
        		$('#clientSecretPanel').hide();        		
        	}
        },
        
        /**
         * Set up the form based on the "Generate" checkbox
         * @param event
         */
        toggleGenerateClientSecret:function(event) {
        	
        	if ($('#generateClientSecret input').is(':checked')) {
        		// show the "generated" block, hide the "display" checkbox
        		$('#displayClientSecret').hide();
        		$('#clientSecret').hide();
        		$('#clientSecretGenerated').show();
        		$('#clientSecretHidden').hide();
        	} else {
        		// show the display checkbox, fall back to the "display" logic
        		$('#displayClientSecret').show();
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
        		$('#clientSecret').show();
        		$('#clientSecretHidden').hide();
        		$('#clientSecretGenerated').hide();
        	} else {
        		// want to hide it
        		$('#clientSecret').hide();
        		$('#clientSecretHidden').show();
        		$('#clientSecretGenerated').hide();
        	}
        },
        
        saveClient:function (event) {

            $('.control-group').removeClass('error');

            // do some trimming to the redirect URI to allow null value
            var registeredRedirectUri = $.trim($('#registeredRedirectUri textarea').val()).replace(/ /g,'').split("\n");
            if (registeredRedirectUri.length == 1 && registeredRedirectUri[0] == "") {
                registeredRedirectUri = [];
            }

            // build the grant type object
            var authorizedGrantTypes = [];
            $.each(["authorization_code","client_credentials","password","implicit"],function(index,type) {
                if ($('#authorizedGrantTypes-' + type).is(':checked')) {
                    authorizedGrantTypes.push(type);
                }
            });

            var requireClientSecret = $('#requireClientSecret input').is(':checked');
            var generateClientSecret = $('#generateClientSecret input').is(':checked');
            var clientSecret = null;
            
            if (requireClientSecret && !generateClientSecret) {
            	// if it's required but we're not generating it, send the value
            	clientSecret = $('#clientSecret').val();
            }

            var valid = this.model.set({
                applicationName:$('#applicationName input').val(),
                clientId:$('#clientId input').val(),
                clientSecret: clientSecret,
                generateClientSecret:generateClientSecret,
                registeredRedirectUri:registeredRedirectUri,
                clientDescription:$('#clientDescription textarea').val(),
                allowRefresh:$('#allowRefresh').is(':checked'),
                authorizedGrantTypes: authorizedGrantTypes,
                accessTokenValiditySeconds: $('#accessTokenValiditySeconds input').val(),
                refreshTokenValiditySeconds: $('#refreshTokenValiditySeconds input').val(),
                idTokenValiditySeconds: $('#idTokenValiditySeconds input').val(),
                scope:$.map($('#scope textarea').val().replace(/,$/,'').replace(/\s/g,' ').split(","), $.trim)
            });

            if (valid) {

                var _self = this;
                this.model.save(this.model, {
                    success:function () {
                        app.clientList.add(_self.model);
                        app.navigate('clients', {trigger:true});
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
            
            return this;
        },
        
        postRender:function() {
            this.toggleRequireClientSecret();
        }
    });

    var URLListView = Backbone.View.extend({

        tagName: 'span',

        initialize:function () {
        },

        events:{
            "click .btn-primary":"save"
        },

        save:function () {

        },

        render:function (eventName) {

            // append and render
            $(this.el).html($('#tmpl-url-list').html());
            return this;
        }
    });


    // Router
    var AppRouter = Backbone.Router.extend({

        routes:{
            "clients":"listClients",
            "client/new":"newClient",
            "client/:id":"editClient",
            "white_list":"whiteList"
        },

        initialize:function () {

            this.clientList = new ClientCollection();
            this.clientListView = new ClientListView({model:this.clientList});

            this.whiteListView = new URLListView();
            this.blackListView = new URLListView();


            this.breadCrumbView = new BreadCrumbView({
                collection:new Backbone.Collection()
            });

            this.breadCrumbView.render();

            this.startAfter([this.clientList]);

        },

        startAfter:function (collections) {
            // Start history when required collections are loaded
            var start = _.after(collections.length, _.once(function () {
                Backbone.history.start()
            }));
            _.each(collections, function (collection) {
                collection.bind('reset', start, Backbone.history)
            });
        },

        listClients:function () {

            this.breadCrumbView.collection.reset();
            this.breadCrumbView.collection.add([
                {text:"Home", href:"/"},
                {text:"Manage Clients", href:"admin/manage/#clients"}
            ]);

            $('#content').html(this.clientListView.render().el);
            this.clientListView.delegateEvents();
        },

        newClient:function() {

            this.breadCrumbView.collection.reset();
            this.breadCrumbView.collection.add([
                {text:"Home", href:"/"},
                {text:"Manage Clients", href:"admin/manage/#clients"},
                {text:"New", href:"#"}
            ]);

        	var client = new ClientModel();
        	
        	// set up this new client to require a secret and have us autogenerate one
        	client.set({
        		requireClientSecret:true, 
        		generateClientSecret:true,
        		displayClientSecret:false
        	}, { silent: true });
        	
            this.clientFormView = new ClientFormView({model:client});
            $('#content').html(this.clientFormView.render().el);
            this.clientFormView.postRender(); // set up the form for the given model data
        },

        editClient:function(id) {

            this.breadCrumbView.collection.reset();
            this.breadCrumbView.collection.add([
                {text:"Home", href:"/"},
                {text:"Manage Clients", href:"admin/manage/#clients"},
                {text:"Edit", href:"#"}
            ]);

            var client = this.clientList.get(id);

            if (client.get("clientSecret") == null) {
            	client.set({
            		requireClientSecret:false
            	}, { silent: true });
            }
            
        	client.set({
        		generateClientSecret:false,
        		displayClientSecret:false
        	}, { silent: true });
            
            this.clientFormView = new ClientFormView({model:client});
            $('#content').html(this.clientFormView.render().el);
            this.clientFormView.postRender(); // set up the form for the given model data
        },

        whiteList:function () {
            $('#content').html(this.whiteListView.render().el);
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
        $.get('resources/template/client.html', _load);
        $.get('resources/template/list.html', _load);

        jQuery.ajaxSetup({async:true});
        app = new AppRouter();


    });


