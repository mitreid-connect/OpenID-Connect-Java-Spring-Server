
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
            
            this.model.destroy({         
            	error:function (error, response) {
            		console.log("An error occurred when deleting from a list widget");
    
					//Pull out the response text.
					var responseText = JSON.parse(response.responseText);
            		
            		//Display an alert with an error message
            		$('#modalAlert div.modal-body').html("<div class='alert alert-error'><strong>Warning!</strong>" + responseText + "</div>");
            		
        			 $("#modalAlert").modal({ // wire up the actual modal functionality and show the dialog
        				 "backdrop" : "static",
        				 "keyboard" : true,
        				 "show" : true // ensure the modal is shown immediately
        			 });
            	}
            });
            
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
    		},
    		error:function(error, response) {
    			//Pull out the response text.
				var responseText = JSON.parse(response.responseText);
        		
        		//Display an alert with an error message
        		$('#modalAlert div.modal-body').html("<div class='alert alert-error'><strong>Warning!</strong>" + responseText + "</div>");
        		
    			 $("#modalAlert").modal({ // wire up the actual modal functionality and show the dialog
    				 "backdrop" : "static",
    				 "keyboard" : true,
    				 "show" : true // ensure the modal is shown immediately
    			 });
    		}
    	});

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

    // load up our model and collection classes
    /*
    $.get('resources/js/client.js', _load);
    $.get('resources/js/grant.js', _load);
    $.get('resources/js/scope.js', _load);
    $.get('resources/js/whitelist.js', _load);
    */
    
    var _load = function (templates) {
        $('body').append(templates);
    };

    // load templates and append them to the body
    $.get('resources/template/admin.html', _load);
    $.get('resources/template/client.html', _load);
    $.get('resources/template/grant.html', _load);
    $.get('resources/template/scope.html', _load);
    $.get('resources/template/whitelist.html', _load);

    jQuery.ajaxSetup({async:true});
    app = new AppRouter();

    // grab all hashed URLs and send them through the app router instead
    $('a[href*="#"]').on('click', function(event) {
    	event.preventDefault();
    	app.navigate(this.hash.slice(1), {trigger: true});
    });
    
});


