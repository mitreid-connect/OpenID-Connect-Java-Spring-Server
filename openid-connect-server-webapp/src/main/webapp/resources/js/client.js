/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
 *
 * Portions copyright 2011-2013 The MITRE Corporation
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
var ClientModel = Backbone.Model.extend({

	idAttribute: "id",

	initialize: function() {

		// bind validation errors to dom elements
		// this will display form elements in red if they are not valid
		this.bind('error', function(model, errs) {
			_.map(errs, function(val, elID) {
				$('#' + elID).addClass('error');
			});
		});

	},

	// We can pass it default values.
	defaults: {
		id: null,

		clientId: null,
		clientSecret: null,
		redirectUris: [],
		clientName: null,
		clientUri: null,
		logoUri: null,
		contacts: [],
		tosUri: null,
		tokenEndpointAuthMethod: null,
		scope: [],
		grantTypes: [],
		responseTypes: [],
		policyUri: null,

		jwksUri: null,
		jwks: null,
		jwksType: "URI",

		applicationType: null,
		sectorIdentifierUri: null,
		subjectType: null,

		requestObjectSigningAlg: null,

		userInfoSignedResponseAlg: null,
		userInfoEncryptedResponseAlg: null,
		userInfoEncryptedResponseEnc: null,

		idTokenSignedResponseAlg: null,
		idTokenEncryptedResponseAlg: null,
		idTokenEncryptedResponseEnc: null,

		tokenEndpointAuthSigningAlg: null,

		defaultMaxAge: null,
		requireAuthTime: false,
		defaultACRvalues: null,

		initiateLoginUri: null,
		postLogoutRedirectUris: [],

		requestUris: [],

		softwareStatement: null,
		softwareId: null,
		softwareVersion: null,

		codeChallengeMethod: null,

		authorities: [],
		accessTokenValiditySeconds: null,
		refreshTokenValiditySeconds: null,
		resourceIds: [],
		// additionalInformation?

		claimsRedirectUris: [],

		clientDescription: null,
		reuseRefreshToken: true,
		clearAccessTokensOnRefresh: true,
		dynamicallyRegistered: false,
		allowIntrospection: false,
		idTokenValiditySeconds: null,
		deviceCodeValiditySeconds: null,
		createdAt: null,

		allowRefresh: false,
		displayClientSecret: false,
		generateClientSecret: false,
	},

	urlRoot: "api/clients",

	matches: function(term) {

		var matches = [];

		if (term) {
			if (this.get('clientId').toLowerCase().indexOf(term.toLowerCase()) != -1) {
				matches.push($.t('client.client-table.match.id'));
			}
			if (this.get('clientName') != null && this.get('clientName').toLowerCase().indexOf(term.toLowerCase()) != -1) {
				matches.push($.t('client.client-table.match.name'));
			}
			if (this.get('clientDescription') != null && this.get('clientDescription').toLowerCase().indexOf(term.toLowerCase()) != -1) {
				matches.push($.t('client.client-table.match.description'));
			}
			if (this.get('clientUri') != null && this.get('clientUri').toLowerCase().indexOf(term.toLowerCase()) != -1) {
				matches.push($.t('client.client-table.match.homepage'));
			}
			if (this.get('policyUri') != null && this.get('policyUri').toLowerCase().indexOf(term.toLowerCase()) != -1) {
				matches.push($.t('client.client-table.match.policy'));
			}
			if (this.get('tosUri') != null && this.get('tosUri').toLowerCase().indexOf(term.toLowerCase()) != -1) {
				matches.push($.t('client.client-table.match.terms'));
			}
			if (this.get('logoUri') != null && this.get('logoUri').toLowerCase().indexOf(term.toLowerCase()) != -1) {
				matches.push($.t('client.client-table.match.logo'));
			}
			if (this.get('contacts') != null) {
				var f = _.filter(this.get('contacts'), function(item) {
					return item.toLowerCase().indexOf(term.toLowerCase()) != -1;
				});
				if (f.length > 0) {
					matches.push($.t('client.client-table.match.contacts'));
				}
			}
			if (this.get('redirectUris') != null) {
				var f = _.filter(this.get('redirectUris'), function(item) {
					return item.toLowerCase().indexOf(term.toLowerCase()) != -1;
				});
				if (f.length > 0) {
					matches.push($.t('client.client-table.match.redirect'));
				}
			}
			if (this.get('scope') != null) {
				var f = _.filter(this.get('scope'), function(item) {
					return item.toLowerCase().indexOf(term.toLowerCase()) != -1;
				});
				if (f.length > 0) {
					matches.push($.t('client.client-table.match.scope'));
				}
			}
			if (this.get('softwareId') != null && this.get('softwareId').toLowerCase().indexOf(term.toLowerCase()) != -1) {
				matches.push($.t('client.client-table.match.software-id'));
			}
			if (this.get('softwareVersion') != null && this.get('softwareVersion').toLowerCase().indexOf(term.toLowerCase()) != -1) {
				matches.push($.t('client.client-table.match.software-version'));
			}
		} else {
			// there's no search term, we always match

			this.unset('matches', {
				silent: true
			});
			// console.log('no term');
			return true;
		}

		var matchString = matches.join(' | ');

		if (matches.length > 0) {
			this.set({
				matches: matchString
			}, {
				silent: true
			});

			return true;
		} else {
			this.unset('matches', {
				silent: true
			});

			return false;
		}
	}

});

var RegistrationTokenModel = Backbone.Model.extend({
	idAttribute: 'clientId',
	urlRoot: 'api/tokens/registration'
});

var ClientStatsModel = Backbone.Model.extend({
	urlRoot: 'api/stats/byclientid'
});

var ClientCollection = Backbone.Collection.extend({

	initialize: function() {
		// this.fetch();
	},

	model: ClientModel,
	url: "api/clients",

	getByClientId: function(clientId) {
		var clients = this.where({
			clientId: clientId
		});
		if (clients.length == 1) {
			return clients[0];
		} else {
			return null;
		}
	}
});

var ClientView = Backbone.View.extend({

	tagName: 'tr',

	isRendered: false,

	initialize: function(options) {
		this.options = options;

		if (!this.template) {
			this.template = _.template($('#tmpl-client-table-item').html());
		}

		if (!this.scopeTemplate) {
			this.scopeTemplate = _.template($('#tmpl-scope-list').html());
		}

		if (!this.moreInfoTemplate) {
			this.moreInfoTemplate = _.template($('#tmpl-client-more-info-block').html());
		}

		if (!this.registrationTokenTemplate) {
			this.registrationTokenTemplate = _.template($('#tmpl-client-registration-token').html());
		}

		if (!this.countTemplate) {
			this.countTemplate = _.template($('#tmpl-client-count').html());
		}

		this.model.bind('change', this.render, this);

	},

	render: function(eventName) {

		var creationDate = this.model.get('createdAt');
		var displayCreationDate = $.t('client.client-table.unknown');
		var hoverCreationDate = "";
		if (creationDate == null || !moment(creationDate).isValid()) {
			displayCreationDate = $.t('client.client-table.unknown');
			hoverCreationDate = "";
		} else {
			creationDate = moment(creationDate);
			if (moment().diff(creationDate, 'months') < 6) {
				displayCreationDate = creationDate.fromNow();
			} else {
				displayCreationDate = "on " + creationDate.format("LL");
			}
			hoverCreationDate = creationDate.format("LLL");
		}

		var json = {
			client: this.model.toJSON(),
			whiteList: this.options.whiteList,
			displayCreationDate: displayCreationDate,
			hoverCreationDate: hoverCreationDate
		};
		this.$el.html(this.template(json));

		$('.scope-list', this.el).html(this.scopeTemplate({
			scopes: this.model.get('scope'),
			systemScopes: this.options.systemScopeList
		}));

		$('.client-more-info-block', this.el).html(this.moreInfoTemplate({
			client: this.model.toJSON()
		}));

		$('.clientid-full', this.el).hide();

		this.$('.dynamically-registered').tooltip({
			title: $.t('client.client-table.dynamically-registered-tooltip')
		});
		this.$('.allow-introspection').tooltip({
			title: $.t('client.client-table.allow-introspection-tooltip')
		});

		this.updateMatched();
		this.updateStats();

		$(this.el).i18n();

		this.isRendered = true;

		return this;
	},

	updateStats: function(eventName) {
		$('.count', this.el).html(this.countTemplate({
			count: this.options.clientStat.get('approvedSiteCount')
		}));
	},

	showRegistrationToken: function(e) {
		e.preventDefault();

		$('#modalAlertLabel').html($.t('client.client-form.registration-access-token'));

		var token = new RegistrationTokenModel({
			clientId: this.model.get('clientId')
		});

		var _self = this;
		token.fetch({
			success: function() {
				var savedModel = {
					clientId: _self.model.get('clientId'),
					registrationToken: token.get('value')
				};

				$('#modalAlert .modal-body').html(_self.registrationTokenTemplate(savedModel));

				$('#modalAlert .modal-body #rotate-token').click(function(e) {
					if (confirm($.t('client.client-form.rotate-registration-token-confirm'))) {
						token.save(null, {
							success: function() {
								console.log('token:' + token.get('value'));
								$('#modalAlert .modal-body #registrationToken').val(token.get('value'));
							},
							error: app.errorHandlerView.handleError({
								message: $.t('client.client-form.rotate-registration-token-error')
							})
						});
					}
				});

				$('#modalAlert').i18n();
				$('#modalAlert').modal({
					'backdrop': 'static',
					'keyboard': true,
					'show': true
				});

			},
			error: app.errorHandlerView.handleError({
				log: "An error occurred when fetching the registration token",
				message: $.t('client.client-form.registration-token-error')
			})
		});

	},

	updateMatched: function() {

		// console.log(this.model.get('matches'));

		if (this.model.get('matches')) {
			$('.matched', this.el).show();
			$('.matched span', this.el).html(this.model.get('matches'));
		} else {
			$('.matched', this.el).hide();
		}
	},

	events: {
		"click .btn-edit": "editClient",
		"click .btn-delete": "deleteClient",
		"click .btn-whitelist": "whiteListClient",
		'click .toggleMoreInformation': 'toggleMoreInformation',
		"click .clientid-substring": "showClientId",
		"click .dynamically-registered": 'showRegistrationToken'
	},

	editClient: function(e) {
		e.preventDefault();
		app.navigate('admin/client/' + this.model.id, {
			trigger: true
		});
	},

	whiteListClient: function(e) {
		e.preventDefault();
		if (this.options.whiteList == null) {
			// create a new one
			app.navigate('admin/whitelist/new/' + this.model.get('id'), {
				trigger: true
			});
		} else {
			// edit the existing one
			app.navigate('admin/whitelist/' + this.options.whiteList.get('id'), {
				trigger: true
			});
		}
	},

	deleteClient: function(e) {
		e.preventDefault();

		if (confirm($.t('client.client-table.confirm'))) {
			var _self = this;

			this.model.destroy({
				dataType: false,
				processData: false,
				success: function() {
					_self.$el.fadeTo("fast", 0.00, function() { // fade
						$(this).slideUp("fast", function() { // slide up
							$(this).remove(); // then remove from the DOM
							_self.parentView.togglePlaceholder();
						});
					});
				},
				error: app.errorHandlerView.handleError({
					log: "An error occurred when deleting a client"
				})
			});

			_self.parentView.delegateEvents();
		}

		return false;
	},

	toggleMoreInformation: function(e) {
		e.preventDefault();
		if ($('.moreInformation', this.el).is(':visible')) {
			// hide it
			$('.moreInformationContainer', this.el).removeClass('alert').removeClass('alert-info').addClass('muted');
			$('.moreInformation', this.el).hide('fast');
			$('.toggleMoreInformation i', this.el).attr('class', 'icon-chevron-right');

		} else {
			// show it
			$('.moreInformationContainer', this.el).addClass('alert').addClass('alert-info').removeClass('muted');
			$('.moreInformation', this.el).show('fast');
			$('.toggleMoreInformation i', this.el).attr('class', 'icon-chevron-down');
		}
	},

	showClientId: function(e) {
		e.preventDefault();

		$('.clientid-full', this.el).show();

	},

	close: function() {
		$(this.el).unbind();
		$(this.el).empty();
	}
});

var ClientListView = Backbone.View.extend({

	tagName: 'span',

	stats: {},

	initialize: function(options) {
		this.options = options;
		this.filteredModel = this.model;
	},

	load: function(callback) {
		if (this.model.isFetched && this.options.whiteListList.isFetched && this.options.systemScopeList.isFetched) {
			callback();
			return;
		}

		$('#loadingbox').sheet('show');
		$('#loading').html('<span class="label" id="loading-clients">' + $.t("common.clients") + '</span> ' + '<span class="label" id="loading-whitelist">' + $.t("whitelist.whitelist") + '</span> ' + '<span class="label" id="loading-scopes">' + $.t("common.scopes") + '</span> ');

		$.when(this.model.fetchIfNeeded({
			success: function(e) {
				$('#loading-clients').addClass('label-success');
			},
			error: app.errorHandlerView.handleError()
		}), this.options.whiteListList.fetchIfNeeded({
			success: function(e) {
				$('#loading-whitelist').addClass('label-success');
			},
			error: app.errorHandlerView.handleError()
		}), this.options.systemScopeList.fetchIfNeeded({
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
		"click .new-client": "newClient",
		"click .refresh-table": "refreshTable",
		'keyup .search-query': 'searchTable',
		'click .form-search button': 'clearSearch',
		'page .paginator': 'changePage'
	},

	newClient: function(e) {
		e.preventDefault();
		this.remove();
		app.navigate('admin/client/new', {
			trigger: true
		});
	},

	render: function(eventName) {

		// append and render table structure
		$(this.el).html($('#tmpl-client-table').html());

		this.renderInner();
		$(this.el).i18n();
		return this;
	},

	renderInner: function(eventName) {

		// set up the rows to render
		// (note that this doesn't render until visibility is determined in
		// togglePlaceholder)

		_.each(this.filteredModel.models, function(client, index) {
			var clientStat = this.getStat(client.get('clientId'));
			var view = new ClientView({
				model: client,
				clientStat: clientStat,
				systemScopeList: this.options.systemScopeList,
				whiteList: this.options.whiteListList.getByClientId(client.get('clientId'))
			});
			view.parentView = this;
			// var element = view.render().el;
			var element = view.el;
			$("#client-table", this.el).append(element);
			this.addView(client.get('id'), view);
		}, this);

		this.togglePlaceholder();
	},

	views: {},

	addView: function(index, view) {
		this.views[index] = view;
	},

	getView: function(index) {
		return this.views[index];
	},

	getStat: function(index) {
		if (!this.stats[index]) {
			this.stats[index] = new ClientStatsModel({
				id: index
			});
		}
		return this.stats[index];
	},

	togglePlaceholder: function() {
		// set up pagination
		var numPages = Math.ceil(this.filteredModel.length / 10);
		if (numPages > 1) {
			$('.paginator', this.el).show();
			$('.paginator', this.el).bootpag({
				total: numPages,
				maxVisible: 10,
				leaps: false,
				page: 1
			});
		} else {
			$('.paginator', this.el).hide();
		}

		if (this.filteredModel.length > 0) {
			this.changePage(undefined, 1);
			$('#client-table', this.el).show();
			$('#client-table-empty', this.el).hide();
			$('#client-table-search-empty', this.el).hide();
		} else {
			if (this.model.length > 0) {
				// there's stuff in the model but it's been filtered out
				$('#client-table', this.el).hide();
				$('#client-table-empty', this.el).hide();
				$('#client-table-search-empty', this.el).show();
			} else {
				// we're empty
				$('#client-table', this.el).hide();
				$('#client-table-empty', this.el).show();
				$('#client-table-search-empty', this.el).hide();
			}
		}
	},

	changePage: function(event, num) {
		console.log('Page changed: ' + num);

		$('.paginator', this.el).bootpag({
			page: num
		});
		var _self = this;

		_.each(this.filteredModel.models, function(client, index) {
			var view = _self.getView(client.get('id'));
			if (!view) {
				console.log('Error: no view for client ' + client.get('id'));
				return;
			}

			// only show/render clients on the current page

			console.log(':: ' + index + ' ' + num + ' ' + Math.ceil((index + 1) / 10) != num);

			if (Math.ceil((index + 1) / 10) != num) {
				$(view.el).hide();
			} else {
				if (!view.isRendered) {
					view.render();
					var clientStat = view.options.clientStat;

					// load and display the stats
					$.when(clientStat.fetchIfNeeded({
						success: function(e) {

						},
						error: app.errorHandlerView.handleError()
					})).done(function(e) {
						view.updateStats();
					});
				}
				$(view.el).show();
			}
		});

		/*
		 * $('#client-table tbody tr', this.el).each(function(index, element) {
		 * if (Math.ceil((index + 1) / 10) != num) { // hide the element
		 * $(element).hide(); } else { // show the element $(element).show(); }
		 * });
		 */
	},

	refreshTable: function(e) {
		e.preventDefault();
		$('#loadingbox').sheet('show');
		$('#loading').html('<span class="label" id="loading-clients">' + $.t("common.clients") + '</span> ' + '<span class="label" id="loading-whitelist">' + $.t("whitelist.whitelist") + '</span> ' + '<span class="label" id="loading-scopes">' + $.t("common.scopes") + '</span> ');

		var _self = this;
		$.when(this.model.fetch({
			success: function(e) {
				$('#loading-clients').addClass('label-success');
			},
			error: app.errorHandlerView.handleError()
		}), this.options.whiteListList.fetch({
			success: function(e) {
				$('#loading-whitelist').addClass('label-success');
			},
			error: app.errorHandlerView.handleError()
		}), this.options.systemScopeList.fetch({
			success: function(e) {
				$('#loading-scopes').addClass('label-success');
			},
			error: app.errorHandlerView.handleError()
		})).done(function() {
			$('#loadingbox').sheet('hide');
			_self.render();
		});
	},

	searchTable: function(e) {
		var term = $('.search-query', this.el).val();

		this.filteredModel = new ClientCollection(this.model.filter(function(client) {
			return client.matches(term);
		}));

		// clear out the table
		$('tbody', this.el).html('');

		// re-render the table
		this.renderInner();

	},

	clearSearch: function(e) {
		$('.search-query', this.el).val('');
		this.searchTable();
	}

});

var ClientFormView = Backbone.View.extend({

	tagName: "span",

	initialize: function(options) {
		this.options = options;

		if (!this.template) {
			this.template = _.template($('#tmpl-client-form').html());
		}

		if (!this.clientSavedTemplate) {
			this.clientSavedTemplate = _.template($('#tmpl-client-saved').html());
		}

		this.redirectUrisCollection = new Backbone.Collection();
		this.scopeCollection = new Backbone.Collection();
		this.contactsCollection = new Backbone.Collection();
		this.defaultACRvaluesCollection = new Backbone.Collection();
		this.requestUrisCollection = new Backbone.Collection();
		this.postLogoutRedirectUrisCollection = new Backbone.Collection();
		this.claimsRedirectUrisCollection = new Backbone.Collection();
		// TODO: add Spring authorities collection and resource IDs collection?

		// collection of sub-views that need to be sync'd on save
		this.listWidgetViews = [];
	},

	events: {
		"click .btn-save": "saveClient",
		"click #allowRefresh": "toggleRefreshTokenTimeout",
		"click #disableAccessTokenTimeout": function() {
			$("#access-token-timeout-time", this.$el).prop('disabled', !$("#access-token-timeout-time", this.$el).prop('disabled'));
			$("#access-token-timeout-unit", this.$el).prop('disabled', !$("#access-token-timeout-unit", this.$el).prop('disabled'));
			document.getElementById("access-token-timeout-time").value = '';
		},
		"click #disableRefreshTokenTimeout": function() {
			$("#refresh-token-timeout-time", this.$el).prop('disabled', !$("#refresh-token-timeout-time", this.$el).prop('disabled'));
			$("#refresh-token-timeout-unit", this.$el).prop('disabled', !$("#refresh-token-timeout-unit", this.$el).prop('disabled'));
			document.getElementById("refresh-token-timeout-time").value = '';
		},
		"click .btn-cancel": "cancel",
		"change #tokenEndpointAuthMethod input:radio": "toggleClientCredentials",
		"change #displayClientSecret": "toggleDisplayClientSecret",
		"change #generateClientSecret": "toggleGenerateClientSecret",
		"change #logoUri input": "previewLogo",
		"change #jwkSelector input:radio": "toggleJWKSetType"
	},

	cancel: function(e) {
		e.preventDefault();
		app.navigate('admin/clients', {
			trigger: true
		});
	},

	load: function(callback) {
		if (this.model.isFetched && this.options.systemScopeList.isFetched) {
			callback();
			return;
		}

		$('#loadingbox').sheet('show');
		$('#loading').html('<span class="label" id="loading-clients">' + $.t('common.clients') + '</span> ' + '<span class="label" id="loading-scopes">' + $.t("common.scopes") + '</span> ');

		$.when(this.options.systemScopeList.fetchIfNeeded({
			success: function(e) {
				$('#loading-scopes').addClass('label-success');
			},
			error: app.errorHandlerView.handleError()
		}), this.model.fetchIfNeeded({
			success: function(e) {
				$('#loading-clients').addClass('label-success');
			},
			error: app.errorHandlerView.handleError()
		})).done(function() {
			$('#loadingbox').sheet('hide');
			callback();
		});
	},

	toggleRefreshTokenTimeout: function() {
		$("#refreshTokenValidityTime", this.$el).toggle();
	},

	previewLogo: function() {
		if ($('#logoUri input', this.el).val()) {
			$('#logoPreview', this.el).empty();
			$('#logoPreview', this.el).attr('src', $('#logoUri input', this.el).val());
		} else {
			// $('#logoBlock', this.el).hide();
			$('#logoPreview', this.el).attr('src', 'resources/images/logo_placeholder.gif');
		}
	},

	/**
	 * Set up the form based on the current state of the tokenEndpointAuthMethod
	 * parameter
	 * 
	 * @param event
	 */
	toggleClientCredentials: function() {

		var tokenEndpointAuthMethod = $('#tokenEndpointAuthMethod input', this.el).filter(':checked').val();

		if (tokenEndpointAuthMethod == 'SECRET_BASIC' || tokenEndpointAuthMethod == 'SECRET_POST' || tokenEndpointAuthMethod == 'SECRET_JWT') {

			// client secret is required, show all the bits
			$('#clientSecretPanel', this.el).show();
			// this function sets up the display portions
			this.toggleGenerateClientSecret();
		} else {
			// no client secret, hide all the bits
			$('#clientSecretPanel', this.el).hide();
		}

		// show or hide the signing algorithm method depending on what's
		// selected
		if (tokenEndpointAuthMethod == 'PRIVATE_KEY' || tokenEndpointAuthMethod == 'SECRET_JWT') {
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

	/**
	 * Set up the form based on the "Generate" checkbox
	 * 
	 * @param event
	 */
	toggleGenerateClientSecret: function() {

		if ($('#generateClientSecret input', this.el).is(':checked')) {
			// show the "generated" block, hide the "display" checkbox
			$('#displayClientSecret', this.el).hide();
			$('#clientSecret', this.el).hide();
			$('#clientSecretGenerated', this.el).show();
			$('#clientSecretHidden', this.el).hide();
		} else {
			// show the display checkbox, fall back to the "display" logic
			$('#displayClientSecret', this.el).show();
			this.toggleDisplayClientSecret();
		}
	},

	/**
	 * Handle whether or not to display the client secret
	 * 
	 * @param event
	 */
	toggleDisplayClientSecret: function() {

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
	getFormTokenNumberValue: function(value, timeUnit) {
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

	// returns "null" if given the value "default" as a string, otherwise
	// returns input value. useful for parsing the JOSE algorithm dropdowns
	defaultToNull: function(value) {
		if (value == 'default') {
			return null;
		} else {
			return value;
		}
	},

	// returns "null" if the given value is falsy
	emptyToNull: function(value) {
		if (value) {
			return value;
		} else {
			return null;
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

	// maps from a form-friendly name to the real grant parameter name
	grantMap: {
		'authorization_code': 'authorization_code',
		'password': 'password',
		'implicit': 'implicit',
		'client_credentials': 'client_credentials',
		'redelegate': 'urn:ietf:params:oauth:grant_type:redelegate',
		'refresh_token': 'refresh_token',
		'device': 'urn:ietf:params:oauth:grant-type:device_code'
	},

	// maps from a form-friendly name to the real response type parameter name
	responseMap: {
		'code': 'code',
		'token': 'token',
		'idtoken': 'id_token',
		'token-idtoken': 'token id_token',
		'code-idtoken': 'code id_token',
		'code-token': 'code token',
		'code-token-idtoken': 'code token id_token'
	},

	saveClient: function(event) {

		$('.control-group').removeClass('error');

		// sync any leftover collection items
		_.each(this.listWidgetViews, function(v) {
			v.addItem($.Event('click'));
		});

		// build the scope object
		var scopes = this.scopeCollection.pluck("item");

		// build the grant type object
		var grantTypes = [];
		$.each(this.grantMap, function(index, type) {
			if ($('#grantTypes-' + index).is(':checked')) {
				grantTypes.push(type);
			}
		});

		// build the response type object
		var responseTypes = [];
		$.each(this.responseMap, function(index, type) {
			if ($('#responseTypes-' + index).is(':checked')) {
				responseTypes.push(type);
			}
		});

		var generateClientSecret = $('#generateClientSecret input').is(':checked');
		var clientSecret = null;

		var tokenEndpointAuthMethod = $('#tokenEndpointAuthMethod input').filter(':checked').val();

		// whether or not the client secret changed
		var secretChanged = false;

		if (tokenEndpointAuthMethod == 'SECRET_BASIC' || tokenEndpointAuthMethod == 'SECRET_POST' || tokenEndpointAuthMethod == 'SECRET_JWT') {

			if (!generateClientSecret) {
				// if it's required but we're not generating it, send the value
				// to preserve it
				clientSecret = $('#clientSecret input').val();

				// if it's not the same as before, offer to display it
				if (clientSecret != this.model.get('clientSecret')) {
					secretChanged = true;
				}
			} else {
				// it's being generated anew
				secretChanged = true;
			}
		}

		var accessTokenValiditySeconds = null;
		if (!$('disableAccessTokenTimeout').is(':checked')) {
			accessTokenValiditySeconds = this.getFormTokenNumberValue($('#accessTokenValidityTime input[type=text]').val(), $('#accessTokenValidityTime select').val());
		}

		var idTokenValiditySeconds = this.getFormTokenNumberValue($('#idTokenValidityTime input[type=text]').val(), $('#idTokenValidityTime select').val());

		var deviceCodeValiditySeconds = this.getFormTokenNumberValue($('#deviceCodeValidityTime input[type=text]').val(), $('#deviceCodeValidityTime select').val());

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

		// make sure that the subject identifier is consistent with the redirect
		// URIs
		var subjectType = $('#subjectType input').filter(':checked').val();
		var redirectUris = this.redirectUrisCollection.pluck("item");
		var sectorIdentifierUri = $('#sectorIdentifierUri input').val();
		if (subjectType == 'PAIRWISE' && redirectUris.length > 1 && sectorIdentifierUri == '') {
			// Display an alert with an error message
			app.errorHandlerView.showErrorMessage($.t("client.client-form.error.consistency"), $.t("client.client-form.error.pairwise-sector"));
			return false;

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
				app.errorHandlerView.showErrorMessage($.t("client.client-form.error.jwk-set"), $.t("client.client-form.error.jwk-set-parse"));
				return false;
			}
		} else {
			jwksUri = null;
			jwks = null;
		}

		var attrs = {
			clientName: this.emptyToNull($('#clientName input').val()),
			clientId: this.emptyToNull($('#clientId input').val()),
			clientSecret: clientSecret,
			generateClientSecret: generateClientSecret,
			redirectUris: redirectUris,
			clientDescription: this.emptyToNull($('#clientDescription textarea').val()),
			logoUri: this.emptyToNull($('#logoUri input').val()),
			grantTypes: grantTypes,
			accessTokenValiditySeconds: accessTokenValiditySeconds,
			refreshTokenValiditySeconds: refreshTokenValiditySeconds,
			idTokenValiditySeconds: idTokenValiditySeconds,
			deviceCodeValiditySeconds: deviceCodeValiditySeconds,
			allowRefresh: $('#allowRefresh').is(':checked'),
			allowIntrospection: $('#allowIntrospection input').is(':checked'),
			scope: scopes,
			tosUri: this.emptyToNull($('#tosUri input').val()),
			policyUri: this.emptyToNull($('#policyUri input').val()),
			clientUri: this.emptyToNull($('#clientUri input').val()),
			applicationType: $('#applicationType input').filter(':checked').val(),
			jwksUri: jwksUri,
			jwks: jwks,
			subjectType: subjectType,
			softwareStatement: this.emptyToNull($('#softwareStatement textarea').val()),
			softwareId: this.emptyToNull($('#softwareId input').val()),
			softwareVersion: this.emptyToNull($('#softwareVersion input').val()),
			tokenEndpointAuthMethod: tokenEndpointAuthMethod,
			responseTypes: responseTypes,
			sectorIdentifierUri: sectorIdentifierUri,
			initiateLoginUri: this.emptyToNull($('#initiateLoginUri input').val()),
			postLogoutRedirectUris: this.postLogoutRedirectUrisCollection.pluck('item'),
			claimsRedirectUris: this.claimsRedirectUrisCollection.pluck('item'),
			reuseRefreshToken: $('#reuseRefreshToken').is(':checked'),
			clearAccessTokensOnRefresh: $('#clearAccessTokensOnRefresh').is(':checked'),
			requireAuthTime: $('#requireAuthTime input').is(':checked'),
			defaultMaxAge: parseInt($('#defaultMaxAge input').val()),
			contacts: this.contactsCollection.pluck('item'),
			requestUris: this.requestUrisCollection.pluck('item'),
			defaultACRvalues: this.defaultACRvaluesCollection.pluck('item'),
			requestObjectSigningAlg: this.defaultToNull($('#requestObjectSigningAlg select').val()),
			userInfoSignedResponseAlg: this.defaultToNull($('#userInfoSignedResponseAlg select').val()),
			userInfoEncryptedResponseAlg: this.defaultToNull($('#userInfoEncryptedResponseAlg select').val()),
			userInfoEncryptedResponseEnc: this.defaultToNull($('#userInfoEncryptedResponseEnc select').val()),
			idTokenSignedResponseAlg: this.defaultToNull($('#idTokenSignedResponseAlg select').val()),
			idTokenEncryptedResponseAlg: this.defaultToNull($('#idTokenEncryptedResponseAlg select').val()),
			idTokenEncryptedResponseEnc: this.defaultToNull($('#idTokenEncryptedResponseEnc select').val()),
			tokenEndpointAuthSigningAlg: this.defaultToNull($('#tokenEndpointAuthSigningAlg select').val()),
			codeChallengeMethod: this.defaultToNull($('#codeChallengeMethod select').val())
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
		for ( var key in attrs) {
			if (attrs[key] === "") {
				attrs[key] = null;
			}
		}

		var _self = this;
		this.model.save(attrs, {
			success: function() {

				$('#modalAlertLabel').html($.t('client.client-form.saved.saved'));

				var savedModel = {
					clientId: _self.model.get('clientId'),
					clientSecret: _self.model.get('clientSecret'),
					secretChanged: secretChanged
				};

				$('#modalAlert div.modal-header').html($.t('client.client-form.saved.saved'));

				$('#modalAlert .modal-body').html(_self.clientSavedTemplate(savedModel));

				$('#modalAlert .modal-body #savedClientSecret').hide();

				$('#modalAlert').on('click', '#clientSaveShow', function(event) {
					event.preventDefault();
					$('#clientSaveShow').hide();
					$('#savedClientSecret').show();
				});

				$('#modalAlert').i18n();
				$('#modalAlert').modal({
					'backdrop': 'static',
					'keyboard': true,
					'show': true
				});

				app.clientList.add(_self.model);
				app.navigate('admin/clients', {
					trigger: true
				});
			},
			error: app.errorHandlerView.handleError({
				log: "An error occurred when saving a client"
			})
		});

		return false;
	},

	render: function(eventName) {

		var data = {
			client: this.model.toJSON(),
			heartMode: heartMode
		};
		$(this.el).html(this.template(data));

		var _self = this;

		// clear the sub-view collection
		this.listWidgetViews = [];

		// build and bind registered redirect URI collection and view
		_.each(this.model.get("redirectUris"), function(redirectUri) {
			_self.redirectUrisCollection.add(new URIModel({
				item: redirectUri
			}));
		});

		var redirUriView = new ListWidgetView({
			type: 'uri',
			placeholder: 'https://',
			helpBlockText: $.t('client.client-form.redirect-uris-help'),
			collection: this.redirectUrisCollection
		});
		$("#redirectUris .controls", this.el).html(redirUriView.render().el);
		this.listWidgetViews.push(redirUriView);

		// build and bind scopes
		_.each(this.model.get("scope"), function(scope) {
			_self.scopeCollection.add(new Backbone.Model({
				item: scope
			}));
		});

		var scopeView = new ListWidgetView({
			placeholder: $.t('client.client-form.scope-placeholder'),
			autocomplete: _.uniq(_.flatten(this.options.systemScopeList.pluck("value"))),
			helpBlockText: $.t('client.client-form.scope-help'),
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

		var contactsView = new ListWidgetView({
			placeholder: $.t("client.client-form.contacts-placeholder"),
			helpBlockText: $.t("client.client-form.contacts-help"),
			collection: this.contactsCollection
		});
		$("#contacts .controls", this.el).html(contactsView.render().el);
		this.listWidgetViews.push(contactsView);

		// build and bind post-logout redirect URIs
		_.each(this.model.get('postLogoutRedirectUris'), function(postLogoutRedirectUri) {
			_self.postLogoutRedirectUrisCollection.add(new URIModel({
				item: postLogoutRedirectUri
			}));
		});

		var postLogoutRedirectUrisView = new ListWidgetView({
			type: 'uri',
			placeholder: 'https://',
			helpBlockText: $.t('client.client-form.post-logout-help'),
			collection: this.postLogoutRedirectUrisCollection
		});
		$('#postLogoutRedirectUris .controls', this.el).html(postLogoutRedirectUrisView.render().el);
		this.listWidgetViews.push(postLogoutRedirectUrisView);

		// build and bind claims redirect URIs
		_.each(this.model.get('claimsRedirectUris'), function(claimsRedirectUri) {
			_self.claimsRedirectUrisCollection.add(new URIModel({
				item: claimsRedirectUri
			}));
		});

		var claimsRedirectUrisView = new ListWidgetView({
			type: 'uri',
			placeholder: 'https://',
			helpBlockText: $.t('client.client-form.claims-redirect-uris-help'),
			collection: this.claimsRedirectUrisCollection
		});
		$('#claimsRedirectUris .controls', this.el).html(claimsRedirectUrisView.render().el);
		this.listWidgetViews.push(claimsRedirectUrisView);

		// build and bind request URIs
		_.each(this.model.get('requestUris'), function(requestUri) {
			_self.requestUrisCollection.add(new URIModel({
				item: requestUri
			}));
		});

		var requestUriView = new ListWidgetView({
			type: 'uri',
			placeholder: 'https://',
			helpBlockText: $.t('client.client-form.request-uri-help'),
			collection: this.requestUrisCollection
		});
		$('#requestUris .controls', this.el).html(requestUriView.render().el);
		this.listWidgetViews.push(requestUriView);

		// build and bind default ACR values
		_.each(this.model.get('defaultACRvalues'), function(defaultACRvalue) {
			_self.defaultACRvaluesCollection.add(new Backbone.Model({
				item: defaultACRvalue
			}));
		});

		var defaultAcrView = new ListWidgetView({
			placeholder: $.t('client.client-form.acr-values-placeholder'),
			// TODO: autocomplete from spec
			helpBlockText: $.t('client.client-form.acr-values-help'),
			collection: this.defaultACRvaluesCollection
		});
		$('#defaultAcrValues .controls', this.el).html(defaultAcrView.render().el);
		this.listWidgetViews.push(defaultAcrView);

		// build and bind

		// set up token fields
		if (!this.model.get("allowRefresh")) {
			$("#refreshTokenValidityTime", this.$el).hide();
		}

		if (this.model.get("accessTokenValiditySeconds") == null) {
			$("#access-token-timeout-time", this.$el).prop('disabled', true);
			$("#access-token-timeout-unit", this.$el).prop('disabled', true);
		}

		if (this.model.get("refreshTokenValiditySeconds") == null) {
			$("#refresh-token-timeout-time", this.$el).prop('disabled', true);
			$("#refresh-token-timeout-unit", this.$el).prop('disabled', true);
		}

		// toggle other dynamic fields
		this.toggleClientCredentials();
		this.previewLogo();
		this.toggleJWKSetType();

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
			title: $.t('common.not-yet-implemented'),
			content: $.t('common.not-yet-implemented-content')
		});

		$(this.el).i18n();
		return this;
	}
});

ui.routes.push({
	path: "admin/clients",
	name: "listClients",
	callback: function() {

		if (!isAdmin()) {
			this.root();
			return;
		}

		this.breadCrumbView.collection.reset();
		this.breadCrumbView.collection.add([{
			text: $.t('admin.home'),
			href: ""
		}, {
			text: $.t('client.manage'),
			href: "manage/#admin/clients"
		}]);

		this.updateSidebar('admin/clients');

		var view = new ClientListView({
			model: this.clientList,
			systemScopeList: this.systemScopeList,
			whiteListList: this.whiteListList
		});
		view.load(function() {
			$('#content').html(view.render().el);
			view.delegateEvents();
			setPageTitle($.t('client.manage'));
		});

	}

});

ui.routes.push({
	path: "admin/client/new",
	name: "newClient",
	callback: function() {
		console.log("newClient");
		if (!isAdmin()) {
			this.root();
			return;
		}

		this.breadCrumbView.collection.reset();
		this.breadCrumbView.collection.add([{
			text: $.t('admin.home'),
			href: ""
		}, {
			text: $.t('client.manage'),
			href: "manage/#admin/clients"
		}, {
			text: $.t('client.client-form.new'),
			href: ""
		}]);

		this.updateSidebar('admin/clients');

		var client = new ClientModel();

		var view = new ClientFormView({
			model: client,
			systemScopeList: this.systemScopeList
		});
		view.load(function() {
			var userInfo = getUserInfo();
			var contacts = [];
			if (userInfo != null && userInfo.email != null) {
				contacts.push(userInfo.email);
			}

			// use a different set of defaults based on heart mode flag
			if (heartMode) {
				client.set({
					tokenEndpointAuthMethod: "PRIVATE_KEY",
					generateClientSecret: false,
					displayClientSecret: false,
					requireAuthTime: true,
					defaultMaxAge: 60000,
					scope: _.uniq(_.flatten(app.systemScopeList.defaultScopes().pluck("value"))),
					accessTokenValiditySeconds: 3600,
					refreshTokenValiditySeconds: 24 * 3600,
					idTokenValiditySeconds: 300,
					deviceCodeValiditySeconds: 30 * 60,
					grantTypes: ["authorization_code"],
					responseTypes: ["code"],
					subjectType: "PUBLIC",
					jwksType: "URI",
					contacts: contacts
				}, {
					silent: true
				});
			} else {
				// set up this new client to require a secret and have us
				// autogenerate one
				client.set({
					tokenEndpointAuthMethod: "SECRET_BASIC",
					generateClientSecret: true,
					displayClientSecret: false,
					requireAuthTime: true,
					defaultMaxAge: 60000,
					scope: _.uniq(_.flatten(app.systemScopeList.defaultScopes().pluck("value"))),
					accessTokenValiditySeconds: 3600,
					idTokenValiditySeconds: 600,
					deviceCodeValiditySeconds: 30 * 60,
					grantTypes: ["authorization_code"],
					responseTypes: ["code"],
					subjectType: "PUBLIC",
					jwksType: "URI",
					contacts: contacts
				}, {
					silent: true
				});
			}

			$('#content').html(view.render().el);
			setPageTitle($.t('client.client-form.new'));
		});
	}
});

ui.routes.push({
	path: "admin/client/:id",
	name: "editClient",
	callback: function(id) {
		console.log("editClient " + id);
		if (!isAdmin()) {
			this.root();
			return;
		}

		this.breadCrumbView.collection.reset();
		this.breadCrumbView.collection.add([{
			text: $.t('admin.home'),
			href: ""
		}, {
			text: $.t('client.manage'),
			href: "manage/#admin/clients"
		}, {
			text: $.t('client.client-form.edit'),
			href: "manage/#admin/client/" + id
		}]);

		this.updateSidebar('admin/clients');

		var client = this.clientList.get(id);
		if (!client) {
			client = new ClientModel({
				id: id
			});
		}

		var view = new ClientFormView({
			model: client,
			systemScopeList: app.systemScopeList
		});
		view.load(function() {
			if ($.inArray("refresh_token", client.get("grantTypes")) != -1) {
				client.set({
					allowRefresh: true
				}, {
					silent: true
				});
			}

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

			client.set({
				generateClientSecret: false,
				displayClientSecret: false
			}, {
				silent: true
			});

			$('#content').html(view.render().el);
			setPageTitle($.t('client.client-form.edit'));
		});

	}
});

ui.templates.push('resources/template/client.html');

ui.init.push(function(app) {
	app.clientList = new ClientCollection();
});
