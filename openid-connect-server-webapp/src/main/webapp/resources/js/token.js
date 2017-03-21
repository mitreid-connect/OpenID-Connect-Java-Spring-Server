/*******************************************************************************
 * Copyright 2017 The MITRE Corporation
 *   and the MIT Internet Trust Consortium
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

var AccessTokenModel = Backbone.Model.extend({
	idAttribute: 'id',

	defaults: {
		id: null,
		value: null,
		refreshTokenId: null,
		scopes: [],
		clientId: null,
		userId: null,
		expiration: null
	},

	urlRoot: 'api/tokens/access'
});

var AccessTokenCollection = Backbone.Collection.extend({
	idAttribute: 'id',

	model: AccessTokenModel,

	url: 'api/tokens/access'

});

var AccessTokenView = Backbone.View.extend({

	tagName: 'tr',

	initialize: function(options) {
		this.options = options;

		if (!this.template) {
			this.template = _.template($('#tmpl-access-token').html());
		}

		if (!this.scopeTemplate) {
			this.scopeTemplate = _.template($('#tmpl-scope-list').html());
		}

		if (!this.moreInfoTemplate) {
			this.moreInfoTemplate = _.template($('#tmpl-client-more-info-block').html());
		}

		this.model.bind('change', this.render, this);

	},

	events: {
		'click .btn-delete': 'deleteToken',
		'click .token-substring': 'showTokenValue',
		'click .toggleMoreInformation': 'toggleMoreInformation'
	},

	render: function(eventName) {

		var expirationDate = this.model.get("expiration");

		if (expirationDate == null) {
			expirationDate = "Never";
		} else if (!moment(expirationDate).isValid()) {
			expirationDate = "Unknown";
		} else {
			expirationDate = moment(expirationDate).calendar();
		}

		var json = {
			token: this.model.toJSON(),
			client: this.options.client.toJSON(),
			formattedExpiration: expirationDate
		};

		this.$el.html(this.template(json));

		// hide full value
		$('.token-full', this.el).hide();

		// show scopes
		$('.scope-list', this.el).html(this.scopeTemplate({
			scopes: this.model.get('scopes'),
			systemScopes: this.options.systemScopeList
		}));

		$('.client-more-info-block', this.el).html(this.moreInfoTemplate({
			client: this.options.client.toJSON()
		}));

		$(this.el).i18n();
		return this;
	},

	deleteToken: function(e) {
		e.preventDefault();

		if (confirm($.t("token.token-table.confirm"))) {

			var _self = this;

			this.model.destroy({
				dataType: false,
				processData: false,
				success: function() {

					_self.$el.fadeTo("fast", 0.00, function() { // fade
						$(this).slideUp("fast", function() { // slide up
							$(this).remove(); // then remove from the DOM
							// refresh the table in case we removed an id token,
							// too
							_self.parentView.refreshTable();
						});
					});
				},
				error: app.errorHandlerView.handleError()
			});

			this.parentView.delegateEvents();
		}

		return false;
	},

	toggleMoreInformation: function(e) {
		e.preventDefault();
		if ($('.moreInformation', this.el).is(':visible')) {
			// hide it
			$('.moreInformation', this.el).hide('fast');
			$('.toggleMoreInformation i', this.el).attr('class', 'icon-chevron-right');
			$('.moreInformationContainer', this.el).removeClass('alert').removeClass('alert-info').addClass('muted');

		} else {
			// show it
			$('.moreInformation', this.el).show('fast');
			$('.toggleMoreInformation i', this.el).attr('class', 'icon-chevron-down');
			$('.moreInformationContainer', this.el).addClass('alert').addClass('alert-info').removeClass('muted');
		}
	},

	close: function() {
		$(this.el).unbind();
		$(this.el).empty();
	},

	showTokenValue: function(e) {
		e.preventDefault();
		$('.token-substring', this.el).hide();
		$('.token-full', this.el).show();
	}
});

var RefreshTokenModel = Backbone.Model.extend({
	idAttribute: 'id',

	defaults: {
		id: null,
		value: null,
		scopes: [],
		clientId: null,
		userId: null,
		expiration: null
	},

	urlRoot: 'api/tokens/refresh'
});

var RefreshTokenCollection = Backbone.Collection.extend({
	idAttribute: 'id',

	model: RefreshTokenModel,

	url: 'api/tokens/refresh'

});

var RefreshTokenView = Backbone.View.extend({

	tagName: 'tr',

	initialize: function(options) {
		this.options = options;

		if (!this.template) {
			this.template = _.template($('#tmpl-refresh-token').html());
		}

		if (!this.scopeTemplate) {
			this.scopeTemplate = _.template($('#tmpl-scope-list').html());
		}

		if (!this.moreInfoTemplate) {
			this.moreInfoTemplate = _.template($('#tmpl-client-more-info-block').html());
		}

		this.model.bind('change', this.render, this);

	},

	events: {
		'click .btn-delete': 'deleteToken',
		'click .token-substring': 'showTokenValue',
		'click .toggleMoreInformation': 'toggleMoreInformation'
	},

	render: function(eventName) {

		var expirationDate = this.model.get("expiration");

		if (expirationDate == null) {
			expirationDate = "Never";
		} else if (!moment(expirationDate).isValid()) {
			expirationDate = "Unknown";
		} else {
			expirationDate = moment(expirationDate).calendar();
		}

		var json = {
			token: this.model.toJSON(),
			client: this.options.client.toJSON(),
			formattedExpiration: expirationDate,
			accessTokenCount: this.options.accessTokenCount
		};

		this.$el.html(this.template(json));

		// hide full value
		$('.token-full', this.el).hide();

		// show scopes
		$('.scope-list', this.el).html(this.scopeTemplate({
			scopes: this.model.get('scopes'),
			systemScopes: this.options.systemScopeList
		}));

		$('.client-more-info-block', this.el).html(this.moreInfoTemplate({
			client: this.options.client.toJSON()
		}));

		$(this.el).i18n();
		return this;

	},

	deleteToken: function(e) {
		e.preventDefault();

		if (confirm($.t('token.token-table.confirm-refresh'))) {

			var _self = this;

			this.model.destroy({
				dataType: false,
				processData: false,
				success: function() {

					_self.$el.fadeTo("fast", 0.00, function() { // fade
						$(this).slideUp("fast", function() { // slide up
							$(this).remove(); // then remove from the DOM
							// refresh the table in case the access tokens have
							// changed, too
							_self.parentView.refreshTable();
						});
					});
				},
				error: app.errorHandlerView.handleError()
			});

			_self.parentView.delegateEvents();
		}

		return false;
	},

	toggleMoreInformation: function(e) {
		e.preventDefault();
		if ($('.moreInformation', this.el).is(':visible')) {
			// hide it
			$('.moreInformation', this.el).hide('fast');
			$('.toggleMoreInformation i', this.el).attr('class', 'icon-chevron-right');
			$('.moreInformationContainer', this.el).removeClass('alert').removeClass('alert-info').addClass('muted');
		} else {
			// show it
			$('.moreInformation', this.el).show('fast');
			$('.toggleMoreInformation i', this.el).attr('class', 'icon-chevron-down');
			$('.moreInformationContainer', this.el).addClass('alert').addClass('alert-info').removeClass('muted');
		}

	},

	close: function() {
		$(this.el).unbind();
		$(this.el).empty();
	},

	showTokenValue: function(e) {
		e.preventDefault();
		$('.token-substring', this.el).hide();
		$('.token-full', this.el).show();
	}
});

var TokenListView = Backbone.View.extend({
	tagName: 'span',

	initialize: function(options) {
		this.options = options;
	},

	events: {
		"click .refresh-table": "refreshTable",
		'page .paginator-access': 'changePageAccess',
		'page .paginator-refresh': 'changePageRefresh'
	},

	load: function(callback) {
		if (this.model.access.isFetched && this.model.refresh.isFetched && this.options.clientList.isFetched && this.options.systemScopeList.isFetched) {
			callback();
			return;
		}

		$('#loadingbox').sheet('show');
		$('#loading').html(
				'<span class="label" id="loading-access">' + $.t('token.token-table.access-tokens') + '</span> ' + '<span class="label" id="loading-refresh">' + $.t('token.token-table.refresh-tokens') + '</span> ' + '<span class="label" id="loading-clients">' + $.t('common.clients') + '</span> ' + '<span class="label" id="loading-scopes">'
						+ $.t('common.scopes') + '</span> ');

		$.when(this.model.access.fetchIfNeeded({
			success: function(e) {
				$('#loading-access').addClass('label-success');
			},
			error: app.errorHandlerView.handleError()
		}), this.model.refresh.fetchIfNeeded({
			success: function(e) {
				$('#loading-refresh').addClass('label-success');
			},
			error: app.errorHandlerView.handleError()
		}), this.options.clientList.fetchIfNeeded({
			success: function(e) {
				$('#loading-clients').addClass('label-success');
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

	changePageAccess: function(event, num) {
		$('.paginator-access', this.el).bootpag({
			page: num
		});
		$('#access-token-table tbody tr', this.el).each(function(index, element) {
			if (Math.ceil((index + 1) / 10) != num) {
				$(element).hide();
			} else {
				$(element).show();
			}
		});
	},

	changePageRefresh: function(event, num) {
		$('.paginator-refresh', this.el).bootpag({
			page: num
		});
		$('#refresh-token-table tbody tr', this.el).each(function(index, element) {
			if (Math.ceil((index + 1) / 10) != num) {
				$(element).hide();
			} else {
				$(element).show();
			}
		});
	},

	refreshTable: function(e) {
		$('#loadingbox').sheet('show');
		$('#loading').html(
				'<span class="label" id="loading-access">' + $.t('token.token-table.access-tokens') + '</span> ' + '<span class="label" id="loading-refresh">' + $.t('token.token-table.refresh-tokens') + '</span> ' + '<span class="label" id="loading-clients">' + $.t('common.clients') + '</span> ' + '<span class="label" id="loading-scopes">'
						+ $.t('common.scopes') + '</span> ');
		var _self = this;
		$.when(this.model.access.fetch({
			success: function(e) {
				$('#loading-access').addClass('label-success');
			},
			error: app.errorHandlerView.handleError()
		}), this.model.refresh.fetch({
			success: function(e) {
				$('#loading-refresh').addClass('label-success');
			},
			error: app.errorHandlerView.handleError()
		}), this.options.clientList.fetch({
			success: function(e) {
				$('#loading-clients').addClass('label-success');
			},
			error: app.errorHandlerView.handleError()
		}), this.options.systemScopeList.fetch({
			success: function(e) {
				$('#loading-scopes').addClass('label-success');
			},
			error: app.errorHandlerView.handleError()
		})).done(function() {
			_self.render();
			$('#loadingbox').sheet('hide');
		});
	},

	togglePlaceholder: function() {
		if (this.model.access.length > 0) {
			$('#access-token-table', this.el).show();
			$('#access-token-table-empty', this.el).hide();
		} else {
			$('#access-token-table', this.el).hide();
			$('#access-token-table-empty', this.el).show();
		}
		if (this.model.refresh.length > 0) {
			$('#refresh-token-table', this.el).show();
			$('#refresh-token-table-empty', this.el).hide();
		} else {
			$('#refresh-token-table', this.el).hide();
			$('#refresh-token-table-empty', this.el).show();
		}

		$('#access-token-count', this.el).html(this.model.access.length);
		$('#refresh-token-count', this.el).html(this.model.refresh.length);
	},

	render: function(eventName) {

		// append and render the table structure
		$(this.el).html($('#tmpl-token-table').html());

		var _self = this;

		// set up pagination
		var numPagesAccess = Math.ceil(this.model.access.length / 10);
		if (numPagesAccess > 1) {
			$('.paginator-access', this.el).show();
			$('.paginator-access', this.el).bootpag({
				total: numPagesAccess,
				page: 1
			});
		} else {
			$('.paginator-access', this.el).hide();
		}

		// count up refresh tokens
		var refreshCount = {};

		_.each(this.model.access.models, function(token, index) {
			// look up client
			var client = _self.options.clientList.getByClientId(token.get('clientId'));
			var view = new AccessTokenView({
				model: token,
				client: client,
				systemScopeList: _self.options.systemScopeList
			});
			view.parentView = _self;
			var element = view.render().el;
			$('#access-token-table', _self.el).append(element);
			if (Math.ceil((index + 1) / 10) != 1) {
				$(element).hide();
			}

			// console.log(token.get('refreshTokenId'));
			var refId = token.get('refreshTokenId');
			if (refId != null) {
				if (refreshCount[refId]) {
					refreshCount[refId] += 1;
				} else {
					refreshCount[refId] = 1;
				}

			}

		});

		// console.log(refreshCount);

		// set up pagination
		var numPagesRefresh = Math.ceil(this.model.refresh.length / 10);
		if (numPagesRefresh > 1) {
			$('.paginator-refresh', this.el).show();
			$('.paginator-refresh', this.el).bootpag({
				total: numPagesRefresh,
				page: 1
			});
		} else {
			$('.paginator-refresh', this.el).hide();
		}

		_.each(this.model.refresh.models, function(token, index) {
			// look up client
			var client = _self.options.clientList.getByClientId(token.get('clientId'));
			var view = new RefreshTokenView({
				model: token,
				client: client,
				systemScopeList: _self.options.systemScopeList,
				accessTokenCount: refreshCount[token.get('id')]
			});
			view.parentView = _self;
			var element = view.render().el;
			$('#refresh-token-table', _self.el).append(element);
			if (Math.ceil((index + 1) / 10) != 1) {
				$(element).hide();
			}

		});

		/*
		 * _.each(this.model.models, function (scope) { $("#scope-table",
		 * this.el).append(new SystemScopeView({model: scope}).render().el); },
		 * this);
		 */

		this.togglePlaceholder();
		$(this.el).i18n();
		return this;
	}
});

ui.routes.push({
	path: "user/tokens",
	name: "tokens",
	callback: function() {
		this.breadCrumbView.collection.reset();
		this.breadCrumbView.collection.add([{
			text: $.t('admin.home'),
			href: ""
		}, {
			text: $.t('token.manage'),
			href: "manage/#user/tokens"
		}]);

		this.updateSidebar('user/tokens');

		var view = new TokenListView({
			model: {
				access: this.accessTokensList,
				refresh: this.refreshTokensList
			},
			clientList: this.clientList,
			systemScopeList: this.systemScopeList
		});

		view.load(function(collection, response, options) {
			$('#content').html(view.render().el);
			setPageTitle($.t('token.manage'));
		});

	}
});

ui.templates.push('resources/template/token.html');

ui.init.push(function(app) {
	app.accessTokensList = new AccessTokenCollection();
	app.refreshTokensList = new RefreshTokenCollection();
});
