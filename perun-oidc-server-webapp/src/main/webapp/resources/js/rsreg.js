/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
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
 *******************************************************************************/
var ResRegClient = Backbone.Model.extend({
	idAttribute: "client_id",

	defaults: {
		client_id: null,
		client_secret: null,
		client_name: null,
		client_uri: null,
		contacts: [],
		tos_uri: null,
		token_endpoint_auth_method: null,
		scope: null,
		policy_uri: null,

		jwks_uri: null,
		jwks: null,
		jwksType: 'URI',

		application_type: null,
		registration_access_token: null,
		registration_client_uri: null
	},

	sync: function(method, model, options) {
		if (model.get('registration_access_token')) {
			var headers = options.headers ? options.headers : {};
			headers['Authorization'] = 'Bearer ' + model.get('registration_access_token');
			options.headers = headers;
		}

		return this.constructor.__super__.sync(method, model, options);
	},

	urlRoot: 'resource'

});

var ResRegRootView = Backbone.View.extend({

	tagName: 'span',

	initialize: function(options) {
		this.options = options;

	},

	events: {
		"click #newreg": "newReg",
		"click #editreg": "editReg"
	},

	load: function(callback) {
		if (this.options.systemScopeList.isFetched) {
			callback();
			return;
		}

		$('#loadingbox').sheet('show');
		$('#loading').html('<span class="label" id="loading-scopes">' + $.t('common.scopes') + '</span> ');

		$.when(this.options.systemScopeList.fetchIfNeeded({
			success: function(e) {
				$('#loading-scopes').addClass('label-success');
			},
			error: app.errorHandlerView.handleError()
		})).done(function() {
			$('#loadingbox').sheet('hide');
			callback();
		});
	},

	render: function() {
		$(this.el).html($('#tmpl-rsreg').html());
		$(this.el).i18n();
		return this;
	},

	newReg: function(e) {
		e.preventDefault();
		this.remove();
		app.navigate('dev/resource/new', {
			trigger: true
		});
	},

	editReg: function(e) {
		e.preventDefault();
		var clientId = $('#clientId').val();
		var token = $('#regtoken').val();

		var client = new ResRegClient({
			client_id: clientId,
			registration_access_token: token
		});

		var self = this;

		client.fetch({
			success: function() {

				if (client.get("jwks")) {
					client.set({
						jwksType: "VAL"
					}, {
						silent: true
					});
				} else {
					client.set({
						jwksType: "URI"
					}, {
						silent: true
					});
				}

				var view = new ResRegEditView({
					model: client,
					systemScopeList: app.systemScopeList
				});

				view.load(function() {
					$('#content').html(view.render().el);
					view.delegateEvents();
					setPageTitle($.t('rsreg.new'));
					app.navigate('dev/resource/edit', {
						trigger: true
					});
					self.remove();
				});
			},
			error: app.errorHandlerView.handleError({
				message: $.t('dynreg.invalid-access-token')
			})
		});
	}

});

var ResRegEditView = Backbone.View.extend({

	tagName: 'span',

	initialize: function(options) {
		this.options = options;
		if (!this.template) {
			this.template = _.template($('#tmpl-rsreg-resource-form').html());
		}

		this.redirectUrisCollection = new Backbone.Collection();
		this.scopeCollection = new Backbone.Collection();
		this.contactsCollection = new Backbone.Collection();
		this.defaultAcrValuesCollection = new Backbone.Collection();
		this.requestUrisCollection = new Backbone.Collection();

		this.listWidgetViews = [];
	},

	load: function(callback) {
		if (this.options.systemScopeList.isFetched) {
			callback();
			return;
		}

		$('#loadingbox').sheet('show');
		$('#loading').html('<span class="label" id="loading-scopes">' + $.t('common.scopes') + '</span> ');

		$.when(this.options.systemScopeList.fetchIfNeeded({
			success: function(e) {
				$('#loading-scopes').addClass('label-success');
			},
			error: app.errorHandlerView.handleError()
		})).done(function() {
			$('#loadingbox').sheet('hide');
			callback();
		});
	},

	events: {
		"click .btn-save": "saveClient",
		"click .btn-cancel": "cancel",
		"click .btn-delete": "deleteClient",
		"change #tokenEndpointAuthMethod input:radio": "toggleClientCredentials",
		"change #jwkSelector input:radio": "toggleJWKSetType"
	},

	cancel: function(e) {
		e.preventDefault();
		app.navigate('dev/resource', {
			trigger: true
		});
	},

	deleteClient: function(e) {
		e.preventDefault();

		if (confirm($.t('client.client-table.confirm'))) {
			var self = this;

			this.model.destroy({
				dataType: false,
				processData: false,
				success: function() {
					self.remove();
					app.navigate('dev/resource', {
						trigger: true
					});
				},
				error: app.errorHandlerView.handleError()
			});

		}

		return false;
	},

	/**
	 * Set up the form based on the current state of the tokenEndpointAuthMethod
	 * parameter
	 * 
	 * @param event
	 */
	toggleClientCredentials: function() {

		var tokenEndpointAuthMethod = $('#tokenEndpointAuthMethod input', this.el).filter(':checked').val();

		// show or hide the signing algorithm method depending on what's
		// selected
		if (tokenEndpointAuthMethod == 'private_key_jwt' || tokenEndpointAuthMethod == 'client_secret_jwt') {
			$('#tokenEndpointAuthSigningAlg', this.el).show();
		} else {
			$('#tokenEndpointAuthSigningAlg', this.el).hide();
		}
	},

	/**
	 * Set up the form based on the JWK Set selector
	 */
	toggleJWKSetType: function() {
		var jwkSelector = $('#jwkSelector input:radio', this.el).filter(':checked').val();

		if (jwkSelector == 'URI') {
			$('#jwksUri', this.el).show();
			$('#jwks', this.el).hide();
		} else if (jwkSelector == 'VAL') {
			$('#jwksUri', this.el).hide();
			$('#jwks', this.el).show();
		} else {
			$('#jwksUri', this.el).hide();
			$('#jwks', this.el).hide();
		}

	},

	disableUnsupportedJOSEItems: function(serverSupported, query) {
		var supported = ['default'];
		if (serverSupported) {
			supported = _.union(supported, serverSupported);
		}
		$(query, this.$el).each(function(idx) {
			if (_.contains(supported, $(this).val())) {
				$(this).prop('disabled', false);
			} else {
				$(this).prop('disabled', true);
			}
		});

	},

	// returns "null" if given the value "default" as a string,
	// otherwise returns input value. useful for parsing the JOSE
	// algorithm dropdowns
	defaultToNull: function(value) {
		if (value == 'default') {
			return null;
		} else {
			return value;
		}
	},

	saveClient: function(e) {
		e.preventDefault();

		$('.control-group').removeClass('error');

		// sync any leftover collection items
		_.each(this.listWidgetViews, function(v) {
			v.addItem($.Event('click'));
		});

		// build the scope object
		var scopes = this.scopeCollection.pluck("item").join(" ");

		var contacts = this.contactsCollection.pluck('item');
		var userInfo = getUserInfo();
		if (userInfo && userInfo.email) {
			if (!_.contains(contacts, userInfo.email)) {
				contacts.push(userInfo.email);
			}
		}

		// process the JWKS
		var jwksUri = null;
		var jwks = null;
		var jwkSelector = $('#jwkSelector input:radio', this.el).filter(':checked').val();

		if (jwkSelector == 'URI') {
			jwksUri = $('#jwksUri input').val();
			jwks = null;
		} else if (jwkSelector == 'VAL') {
			jwksUri = null;
			try {
				jwks = JSON.parse($('#jwks textarea').val());
			} catch (e) {
				console.log("An error occurred when parsing the JWK Set");

				// Display an alert with an error message
				app.errorHandlerView.showErrorMessage($.t("client.client-form.error.jwk-set"), $.t("client.client-form.error.jwk-set-parse"));
				return false;
			}
		} else {
			jwksUri = null;
			jwks = null;
		}

		var attrs = {
			client_name: $('#clientName input').val(),
			scope: scopes,
			client_secret: null, // never send a client secret
			tos_uri: $('#tosUri input').val(),
			policy_uri: $('#policyUri input').val(),
			client_uri: $('#clientUri input').val(),
			application_type: $('#applicationType input').filter(':checked').val(),
			jwks_uri: jwksUri,
			jwks: jwks,
			token_endpoint_auth_method: $('#tokenEndpointAuthMethod input').filter(':checked').val(),
			contacts: contacts,
			token_endpoint_auth_signing_alg: this.defaultToNull($('#tokenEndpointAuthSigningAlg select').val())
		};

		// set all empty strings to nulls
		for ( var key in attrs) {
			if (attrs[key] === "") {
				attrs[key] = null;
			}
		}

		var _self = this;
		this.model.save(attrs, {
			success: function() {
				// switch to an "edit" view
				app.navigate('dev/resource/edit', {
					trigger: true
				});
				_self.remove();

				if (_self.model.get("jwks")) {
					_self.model.set({
						jwksType: "VAL"
					}, {
						silent: true
					});
				} else {
					_self.model.set({
						jwksType: "URI"
					}, {
						silent: true
					});
				}

				var view = new ResRegEditView({
					model: _self.model,
					systemScopeList: _self.options.systemScopeList
				});

				view.load(function() {
					// reload
					$('#content').html(view.render().el);
					view.delegateEvents();
				});
			},
			error: app.errorHandlerView.handleError()
		});

		return false;
	},

	render: function() {
		$(this.el).html(this.template({
			client: this.model.toJSON(),
			userInfo: getUserInfo()
		}));

		this.listWidgetViews = [];

		var _self = this;

		// build and bind scopes
		var scopes = this.model.get("scope");
		var scopeSet = scopes ? scopes.split(" ") : [];
		_.each(scopeSet, function(scope) {
			_self.scopeCollection.add(new Backbone.Model({
				item: scope
			}));
		});

		var scopeView = new ListWidgetView({
			placeholder: $.t('client.client-form.scope-placeholder'),
			autocomplete: _.uniq(_.flatten(this.options.systemScopeList.unrestrictedScopes().pluck("value"))),
			helpBlockText: $.t('rsreg.client-form.scope-help'),
			collection: this.scopeCollection
		});
		$("#scope .controls", this.el).html(scopeView.render().el);
		this.listWidgetViews.push(scopeView);

		// build and bind contacts
		_.each(this.model.get('contacts'), function(contact) {
			_self.contactsCollection.add(new Backbone.Model({
				item: contact
			}));
		});

		var contactView = new ListWidgetView({
			placeholder: $.t('client.client-form.contacts-placeholder'),
			helpBlockText: $.t('client.client-form.contacts-help'),
			collection: this.contactsCollection
		});
		$("#contacts .controls", this.el).html(contactView.render().el);
		this.listWidgetViews.push(contactView);

		this.toggleClientCredentials();
		this.toggleJWKSetType();

		// disable unsupported JOSE algorithms
		this.disableUnsupportedJOSEItems(app.serverConfiguration.token_endpoint_auth_signing_alg_values_supported, '#tokenEndpointAuthSigningAlg option');

		this.$('.nyi').clickover({
			placement: 'right',
			title: $.t('common.not-yet-implemented'),
			content: $.t('common.not-yet-implemented-content')
		});

		$(this.el).i18n();
		return this;
	}

});

ui.routes.push({
	path: "dev/resource",
	name: "resReg",
	callback: function() {

		this.breadCrumbView.collection.reset();
		this.breadCrumbView.collection.add([{
			text: $.t('admin.home'),
			href: ""
		}, {
			text: $.t('admin.self-service-resource'),
			href: "manage/#dev/resource"
		}]);

		this.updateSidebar('dev/resource');

		var view = new ResRegRootView({
			systemScopeList: this.systemScopeList
		});
		view.load(function() {
			$('#content').html(view.render().el);

			setPageTitle($.t('admin.self-service-resource'));
		});

	}
});

ui.routes.push({
	path: "dev/resource/new",
	name: "newResReg",
	callback: function() {

		this.breadCrumbView.collection.reset();
		this.breadCrumbView.collection.add([{
			text: $.t('admin.home'),
			href: ""
		}, {
			text: $.t('admin.self-service-resource'),
			href: "manage/#dev/resource"
		}, {
			text: $.t('rsreg.new'),
			href: "manage/#dev/resource/new"
		}]);

		this.updateSidebar('dev/resource');

		var client = new ResRegClient();
		var view = new ResRegEditView({
			model: client,
			systemScopeList: this.systemScopeList
		});

		view.load(function() {

			var userInfo = getUserInfo();
			var contacts = [];
			if (userInfo != null && userInfo.email != null) {
				contacts.push(userInfo.email);
			}

			client.set({
				scope: _.uniq(_.flatten(app.systemScopeList.defaultUnrestrictedScopes().pluck("value"))).join(" "),
				token_endpoint_auth_method: 'client_secret_basic',
				contacts: contacts
			}, {
				silent: true
			});

			$('#content').html(view.render().el);
			view.delegateEvents();
			setPageTitle($.t('rsreg.new'));

		});

	}
});

ui.routes.push({
	path: "dev/resource/edit",
	name: "editResReg",
	callback: function() {

		this.breadCrumbView.collection.reset();
		this.breadCrumbView.collection.add([{
			text: $.t('admin.home'),
			href: ""
		}, {
			text: $.t('admin.self-service-resource'),
			href: "manage/#dev/resource"
		}, {
			text: $.t('rsreg.edit'),
			href: "manage/#dev/resource/edit"
		}]);

		this.updateSidebar('dev/resource');

		setPageTitle($.t('rsreg.edit'));
		// note that this doesn't actually load the client, that's supposed to
		// happen elsewhere...
	}
});

ui.templates.push('resources/template/rsreg.html');
