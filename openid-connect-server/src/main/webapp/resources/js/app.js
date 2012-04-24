$(function () {

    var ExampleOpenIdClient = {
        name:"A name",
        redirectURL:"http://myURL.domain",
        grantType:["my grant type 1", "my grant type 2"],
        scope:["scope 1", "scope 2"],
        authority:"my authority",
        description:"my description",
        refreshTokens:false
    };

    console.log(tmpl('client_tmpl', ExampleOpenIdClient));

    $('#client-table').append(tmpl('client_tmpl', ExampleOpenIdClient));

});


var Client = Backbone.Model.extend({

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

    urlRoot:"../api/clients"

});

var ClientCollection = Backbone.Collection.extend({
    model:Client,
    url:"../api/clients"
});


var ClientView = Backbone.View.extend({


    initialize:function () {

        var self = this;

        if (!($.isFunction(self.template))) {
            $.get('resources/template/client.html', function (templates) {
                $('body').append(templates);
                self.template = $('#tmpl-client').template();
            });
        }

        this.model.bind("change", this.render, this);
    },

    render:function (eventName) {
        $(this.el).html(this.template(this.model.toJSON()));
        return this;
    },

    events:{
        "change input":"change",
        "click .save":"saveClient",
        "click .delete":"deleteClient"
    },

    change:function (event) {
    },

    saveClient:function () {

    },

    deleteClient:function () {

    },

    close:function () {
        $(this.el).unbind();
        $(this.el).empty();
    }
});