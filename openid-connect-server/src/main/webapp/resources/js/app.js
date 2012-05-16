

    var ClientModel = Backbone.Model.extend({

        idAttribute: "clientId",

        initialize: function () {

            this.bind('error:clientName', function(model, errs) {
                $('#clientName').addClass('error');
            });

            this.bind('error:clientDescription', function(model, errs) {
                $('#clientDescription').addClass('error');
            });

            this.bind('error:registeredRedirectUri', function(model, errs) {
                $('#registeredRedirectUri').addClass('error');
            });
        },

        validate:{
            clientName:{
                required:true,
                pattern:/^[a-zA-Z ]+$/,
                minlength:3,
                maxlength:100
            },
            clientDescription:{
                required:true,
                pattern:/^[a-zA-Z ]+$/,
                minlength:3,
                maxlength:100
            },
            registeredRedirectUri: {
                custom: 'validateURI'
            }
        },

        validateURI: function(attributeName, attributeValue) {

            var expression = /[-a-zA-Z0-9@:%_\+.~#?&//=]{2,256}\.[a-z]{2,4}\b(\/[-a-zA-Z0-9@:%_\+.~#?&//=]*)?/gi;
            var regex = new RegExp(expression);

            return attributeValue.every(function (url) {
                if (!url.match(regex)) {
                    return false;
                }
            });
        },

        // We can pass it default values.
        defaults:{
            clientName:"",
            registeredRedirectUri:[],
            authorizedGrantTypes:[],
            scope:[],
            authorities:[],
            clientDescription:"",
            clientId:null,
            allowRefresh:false
        },

        urlRoot:"/api/clients"

    });

    var ClientCollection = Backbone.Collection.extend({
        model:ClientModel,
        url:"/api/clients"
    });


    var ClientView = Backbone.View.extend({

        tagName: 'tr',

        initialize:function () {

            if (!this.template) {
                this.template = _.template($('#tmpl-client').html());
            }

            this.model.bind('change', this.render, this);
            //this.model.on('change', this.render)
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
            document.location.hash = 'client/' + this.model.id;
        },

        deleteClient:function () {

            var self = this;

            this.model.destroy({
                success:function () {
                    self.$el.fadeTo("slow", 0.00, function(){ //fade
                        $(this).slideUp("slow", function() { //slide up
                            $(this).remove(); //then remove from the DOM
                        });
                    });
                }
            });
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
            document.location.hash = 'client/new';
        },

        render:function (eventName) {

            // append and render table structure
            $(this.el).html($('#tmpl-client-table').html());

            _.each(this.model.models, function (client) {
                $("#client-table").append(new ClientView({model:client}).render().el);
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

            event.preventDefault();

            this.model.set({
                clientName:$('#clientName input').val(),
                registeredRedirectUri:[$('#registeredRedirectUri input').val()],
                clientDescription:$('#clientDescription textarea').val(),
                allowRefresh:$('#allowRefresh').is(':checked')
            });
            if (this.model.isNew()) {
                var self = this;
                app.clientList.create(this.model, {
                    success:function () {
                        document.location.hash = '';
                    },
                    error: function () {
                        //alert('boo!');
                    }
                });

            } else {
                this.model.save(this.model, {
                    success:function () {
                        document.location.hash = '';
                    },
                    error: function () {
                        //alert('boo!');
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
            "":"list",
            "client/new":"newClient",
            "client/:id":"editClient"
        },

        initialize:function () {

        },

        list:function () {

            this.clientList = new ClientCollection();
            this.clientListView = new ClientListView({model:this.clientList});
            this.clientList.fetch();

            $('#content').html(this.clientListView.render().el);
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
            Backbone.history.start();
        });


    });


