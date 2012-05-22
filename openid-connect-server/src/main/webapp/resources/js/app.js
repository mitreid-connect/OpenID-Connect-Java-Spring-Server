

    var ClientModel = Backbone.Model.extend({

        idAttribute: "clientId",

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
            accessTokenTimeout: {
                required: true,
                type:"number"
            },
            refreshTokenTimeout: {
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

            if (!attributeValue.every(function (url) {
                if (url.match(regex)) {
                    return true;
                }
            })) return "Invalid URI";


        },

        // We can pass it default values.
        defaults:{
            clientName:"",
            registeredRedirectUri:[""],
            authorizedGrantTypes:[],
            scope:["openid"],
            authorities:[],
            clientDescription:"",
            clientId:null,
            allowRefresh:false,
            accessTokenTimeout: 0,
            refreshTokenTimeout: 0
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

    var ClientFormView = Backbone.View.extend({

        tagName:"span",

        initialize:function () {

            if (!this.template) {
                this.template = _.template($('#tmpl-client-form').html());
            }
        },

        events:{
            "click .btn-primary":"saveClient"
        },

        saveClient:function (event) {

            $('.control-group').removeClass('error');

            this.model.set({
                clientName:$('#clientName input').val(),
                registeredRedirectUri:$.trim($('#registeredRedirectUri textarea').val()).replace(/ /g,'').split("\n"),
                clientDescription:$('#clientDescription textarea').val(),
                allowRefresh:$('#allowRefresh').is(':checked'),
                accessTokenTimeout: $('#accessTokenTimeout input').val(),
                refreshTokenTimeout: $('#refreshTokenTimeout input').val(),
                scope:$.map($('#scope textarea').val().replace(/,$/,'').replace(/\s/g,' ').split(","), $.trim)
            });

            this.model.save(this.model, {
                success:function () {
                    app.navigate('clients', {trigger: true});
                },
                error:function() {

                }
            });

            if (this.model.isNew() && this.model.isValid()) {
                var self = this;
                app.clientList.create(this.model, {
                    success:function () {
                        app.navigate('clients', {trigger: true});
                    },
                    error:function() {

                    }
                });

            }

            return false;
        },

        render:function (eventName) {

            $(this.el).html(this.template(this.model.toJSON()));
            return this;
        }
    });

    // Router
    var AppRouter = Backbone.Router.extend({

        routes:{
            "clients":"list",
            "client/new":"newClient",
            "client/:id":"editClient"
        },

        initialize:function () {

            this.clientList = new ClientCollection();
            this.clientListView = new ClientListView({model:this.clientList});

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

        list:function () {

            $('#content').html(this.clientListView.render().el);
            this.clientListView.delegateEvents();
        },

        newClient:function() {
            this.clientFormView = new ClientFormView({model:new ClientModel()});
            $('#content').html(this.clientFormView.render().el);
        },

        editClient:function(id) {
            var client = this.clientList.get(id);
            this.clientFormView = new ClientFormView({model:client});
            $('#content').html(this.clientFormView.render().el);
        }

    });

    // holds the global app.
    // this gets init after the templates load
    var app = null;

    // main
    $(function () {

        // load templates and append them to the body
        $.get('resources/template/client.html', function (templates) {
            $('body').append(templates);

            app = new AppRouter();
        });


    });


