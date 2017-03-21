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
var ApprovedSiteModel = Backbone.Model.extend({
	idAttribute: 'id',

	initialize: function() {
	},

	urlRoot: 'api/approved'

});

var ApprovedSiteCollection = Backbone.Collection.extend({
	initialize: function() {
	},

	model: ApprovedSiteModel,
	url: 'api/approved'
});

var ApprovedSiteListView = Backbone.View.extend({
	tagName: 'span',

	initialize: function(options) {
		this.options = options;
	},

	load: function(callback) {
		if (this.model.isFetched && this.options.clientList.isFetched && this.options.systemScopeList.isFetched) {
			callback();
			return;
		}

		$('#loadingbox').sheet('show');
		$('#loading').html('<span class="label" id="loading-grants">' + $.t('grant.grant-table.approved-sites') + '</span> ' + '<span class="label" id="loading-clients">' + $.t('common.clients') + '</span> ' + '<span class="label" id="loading-scopes">' + $.t('common.scopes') + '</span> ');

		$.when(this.model.fetchIfNeeded({
			success: function(e) {
				$('#loading-grants').addClass('label-success');
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

	events: {
		"click .refresh-table": "refreshTable"
	},

	render: function(eventName) {
		$(this.el).html($('#tmpl-grant-table').html());

		var approvedSiteCount = 0;

		var _self = this;

		_.each(this.model.models, function(approvedSite) {
			// look up client
			var client = this.options.clientList.getByClientId(approvedSite.get('clientId'));

			if (client != null) {

				var view = new ApprovedSiteView({
					model: approvedSite,
					client: client,
					systemScopeList: this.options.systemScopeList
				});
				view.parentView = _self;
				$('#grant-table', this.el).append(view.render().el);
				approvedSiteCount = approvedSiteCount + 1;

			}

		}, this);

		this.togglePlaceholder();
		$(this.el).i18n();
		return this;
	},

	togglePlaceholder: function() {
		// count entries
		if (this.model.length > 0) {
			$('#grant-table', this.el).show();
			$('#grant-table-empty', this.el).hide();
		} else {
			$('#grant-table', this.el).hide();
			$('#grant-table-empty', this.el).show();
		}

	},

	refreshTable: function(e) {
		e.preventDefault();
		var _self = this;
		$('#loadingbox').sheet('show');
		$('#loading').html('<span class="label" id="loading-grants">' + $.t('grant.grant-table.approved-sites') + '</span> ' + '<span class="label" id="loading-clients">' + $.t('common.clients') + '</span> ' + '<span class="label" id="loading-scopes">' + $.t('common.scopes') + '</span> ');

		$.when(this.model.fetch({
			success: function(e) {
				$('#loading-grants').addClass('label-success');
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
			$('#loadingbox').sheet('hide');
			_self.render();
		});
	}

});

var ApprovedSiteView = Backbone.View.extend({
	tagName: 'tr',

	initialize: function(options) {
		this.options = options;
		if (!this.template) {
			this.template = _.template($('#tmpl-grant').html());
		}
		if (!this.scopeTemplate) {
			this.scopeTemplate = _.template($('#tmpl-scope-list').html());
		}

		if (!this.moreInfoTemplate) {
			this.moreInfoTemplate = _.template($('#tmpl-client-more-info-block').html());
		}

	},

	render: function() {

		var creationDate = this.model.get("creationDate");
		var accessDate = this.model.get("accessDate");
		var timeoutDate = this.model.get("timeoutDate");

		var displayCreationDate = $.t('grant.grant-table.unknown');
		var hoverCreationDate = "";
		if ((creationDate != null) && moment(creationDate).isValid()) {
			creationDate = moment(creationDate);
			if (moment().diff(creationDate, 'months') < 6) {
				displayCreationDate = creationDate.fromNow();
			} else {
				displayCreationDate = creationDate.format("LL");
			}
			hoverCreationDate = creationDate.format("LLL");
		}

		var displayAccessDate = $.t('grant.grant-table.unknown');
		var hoverAccessDate = "";
		if ((accessDate != null) && moment(accessDate).isValid()) {
			accessDate = moment(accessDate);
			if (moment().diff(accessDate, 'months') < 6) {
				displayAccessDate = accessDate.fromNow();
			} else {
				displayAccessDate = accessDate.format("LL");
			}
			hoverAccessDate = accessDate.format("LLL");
		}

		var displayTimeoutDate = $.t('grant.grant-table.unknown');
		var hoverTimeoutDate = "";
		if (timeoutDate == null) {
			displayTimeoutDate = $.t('grant.grant-table.never');
		} else if (moment(timeoutDate).isValid()) {
			timeoutDate = moment(timeoutDate);
			if (moment().diff(timeoutDate, 'months') < 6) {
				displayTimeoutDate = timeoutDate.fromNow();
			} else {
				displayTimeoutDate = timeoutDate.format("LL");
			}
			hoverTimeoutDate = timeoutDate.format("LLL");
		}

		var formattedDate = {
			displayCreationDate: displayCreationDate,
			hoverCreationDate: hoverCreationDate,
			displayAccessDate: displayAccessDate,
			hoverAccessDate: hoverAccessDate,
			displayTimeoutDate: displayTimeoutDate,
			hoverTimeoutDate: hoverTimeoutDate
		};

		var json = {
			grant: this.model.toJSON(),
			client: this.options.client.toJSON(),
			formattedDate: formattedDate
		};

		this.$el.html(this.template(json));

		$('.scope-list', this.el).html(this.scopeTemplate({
			scopes: this.model.get('allowedScopes'),
			systemScopes: this.options.systemScopeList
		}));

		$('.client-more-info-block', this.el).html(this.moreInfoTemplate({
			client: this.options.client.toJSON()
		}));

		this.$('.dynamically-registered').tooltip({
			title: $.t('grant.grant-table.dynamically-registered')
		});
		this.$('.tokens').tooltip({
			title: $.t('grant.grant-table.active-tokens')
		});
		$(this.el).i18n();
		return this;
	},

	events: {
		'click .btn-delete': 'deleteApprovedSite',
		'click .toggleMoreInformation': 'toggleMoreInformation'
	},

	deleteApprovedSite: function(e) {
		e.preventDefault();
		if (confirm("Are you sure you want to revoke access to this site?")) {
			var self = this;

			this.model.destroy({
				dataType: false,
				processData: false,
				success: function() {
					self.$el.fadeTo("fast", 0.00, function() { // fade
						$(this).slideUp("fast", function() { // slide up
							$(this).remove(); // then remove from the DOM
							self.parentView.togglePlaceholder();
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
	}
});

ui.routes.push({
	path: "user/approved",
	name: "approvedSites",
	callback:

	function() {
		this.breadCrumbView.collection.reset();
		this.breadCrumbView.collection.add([{
			text: $.t('admin.home'),
			href: ""
		}, {
			text: $.t('grant.manage-approved-sites'),
			href: "manage/#user/approve"
		}]);

		this.updateSidebar('user/approved');

		var view = new ApprovedSiteListView({
			model: this.approvedSiteList,
			clientList: this.clientList,
			systemScopeList: this.systemScopeList
		});
		view.load(function(collection, response, options) {
			$('#content').html(view.render().el);
			setPageTitle($.t('grant.manage-approved-sites'));
		});
	}
});

ui.templates.push('resources/template/grant.html');

ui.init.push(function(app) {
	app.approvedSiteList = new ApprovedSiteCollection();
});
