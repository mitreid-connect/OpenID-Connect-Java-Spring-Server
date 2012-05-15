

    var ClientModel = Backbone.Model.extend({

        // We can pass it default values.
        defaults:{
            name:null,
            redirectURL:"http://myURL.domain",
            grantType:["my grant type 1", "my grant type 2"],
            scope:["scope 1", "scope 2"],
            authority:"my authority",
            description:"my description",
            refreshTokens:false
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
            alert('edit');
        },

        deleteClient:function () {
            alert('delete');
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
            document.location.hash = 'new_client';
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

        saveClient:function () {
            this.model.set({
                name:$('#name').val(),
                redirectURL:$('#redirectURL').val(),
                description:$('#description').val()
            });
            if (this.model.isNew()) {
                var self = this;
                app.clientList.create(this.model, {
                    success:function () {
                        alert('bravo!');
                    },
                    error: function () {
                        alert('boo!');
                    }
                });

            } else {
                this.model.save();
            }

            return false;
        },

        render:function (eventName) {

            var action = "Edit";

            if (!this.model) {
                 action = "New";
            }

            $(this.el).html(this.template({action: action}));
            return this;
        }
    });

    // Router
    var AppRouter = Backbone.Router.extend({

        routes:{
            "":"list",
            "new_client":"newClient"
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


