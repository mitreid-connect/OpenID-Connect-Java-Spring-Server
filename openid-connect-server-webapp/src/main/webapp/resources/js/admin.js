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

Backbone.Model.prototype.fetchIfNeeded = function(options) {
	var _self = this;
	if (!options) {
		options = {};
	}
	var success = options.success;
	options.success = function(c, r) {
		_self.isFetched = true;
		if (success) {
			success(c, r);
		}
	};
	if (!this.isFetched) {
		return this.fetch(options);
	} else {
		return options.success(this, null);
	}
};

Backbone.Collection.prototype.fetchIfNeeded = function(options) {
	var _self = this;
	if (!options) {
		options = {};
	}
	var success = options.success;
	options.success = function(c, r) {
		_self.isFetched = true;
		if (success) {
			success(c, r);
		}
	};
	if (!this.isFetched) {
		return this.fetch(options);
	} else {
		return options.success(this, null);
	}
};

var URIModel = Backbone.Model
		.extend({

			validate: function(attrs) {

				var expression = /^(?:([a-z0-9+.-]+:\/\/)((?:(?:[a-z0-9-._~!$&'()*+,;=:]|%[0-9A-F]{2})*)@)?((?:[a-z0-9-._~!$&'()*+,;=]|%[0-9A-F]{2})*)(:(?:\d*))?(\/(?:[a-z0-9-._~!$&'()*+,;=:@\/]|%[0-9A-F]{2})*)?|([a-z0-9+.-]+:)(\/?(?:[a-z0-9-._~!$&'()*+,;=:@]|%[0-9A-F]{2})+(?:[a-z0-9-._~!$&'()*+,;=:@\/]|%[0-9A-F]{2})*)?)(\?(?:[a-z0-9-._~!$&'()*+,;=:\/?@]|%[0-9A-F]{2})*)?(#(?:[a-z0-9-._~!$&'()*+,;=:\/?@]|%[0-9A-F]{2})*)?$/i;
				var regex = new RegExp(expression);

				if (attrs.item == null || !attrs.item.match(regex)) {
					return "Invalid URI";
				}
			}

		});

/*
 * Backbone JS Reusable ListWidget Options { collection: Backbone JS Collection
 * type: ('uri'|'default') autocomplete: ['item1','item2'] List of auto complete
 * items }
 * 
 */
var ListWidgetChildView = Backbone.View.extend({

	tagName: 'tr',

	events: {
		"click .btn-delete-list-item": 'deleteItem',
		"change .checkbox-list-item": 'toggleCheckbox'
	},

	deleteItem: function(e) {
		e.preventDefault();
		e.stopImmediatePropagation();
		// this.$el.tooltip('delete');

		this.model.destroy({
			dataType: false,
			processData: false,
			error: app.errorHandlerView.handleError()
		});

	},

	toggleCheckbox: function(e) {
		e.preventDefault();
		e.stopImmediatePropagation();
		if ($(e.target).is(':checked')) {
			this.options.collection.add(this.model);
		} else {
			this.options.collection.remove(this.model);
		}

	},

	initialize: function(options) {
		this.options = {
			toggle: false,
			checked: false
		};
		_.extend(this.options, options);
		if (!this.template) {
			this.template = _.template($('#tmpl-list-widget-child').html());
		}
	},

	render: function() {

		var data = {
			model: this.model.toJSON(),
			opt: this.options
		};

		this.$el.html(this.template(data));

		$('.item-full', this.el).hide();

		if (this.model.get('item').length > 30) {
			this.$el.tooltip({
				title: $.t('admin.list-widget.tooltip')
			});

			var _self = this;

			$(this.el).click(function(event) {
				event.preventDefault();
				$('.item-short', _self.el).hide();
				$('.item-full', _self.el).show();
				_self.$el.tooltip('destroy');
			});
		}

		$(this.el).i18n();
		return this;
	}
});

var ListWidgetView = Backbone.View.extend({

	tagName: "div",

	events: {
		"click .btn-add-list-item": "addItem",
		"keypress": function(e) {
			// trap the enter key
			if (e.which == 13) {
				e.preventDefault();
				this.addItem(e);
				$("input", this.$el).focus();
			}
		}
	},

	initialize: function(options) {
		this.options = options;

		if (!this.template) {
			this.template = _.template($('#tmpl-list-widget').html());
		}

		this.collection.bind('add', this.render, this);
		this.collection.bind('remove', this.render, this);
	},

	addItem: function(e) {
		e.preventDefault();

		var input_value = $("input", this.el).val().trim();

		if (input_value === "") {
			return;
		}

		var model;

		if (this.options.type == 'uri') {
			model = new URIModel({
				item: input_value
			});
		} else {
			model = new Backbone.Model({
				item: input_value
			});
			model.validate = function(attrs) {
				if (!attrs.item) {
					return "value can't be null";
				}
			};
		}

		// if it's valid and doesn't already exist
		if (model.get("item") != null && this.collection.where({
			item: input_value
		}).length < 1) {
			this.collection.add(model);
		} else {
			// else add a visual error indicator
			$(".control-group", this.el).addClass('error');
		}
	},

	render: function(eventName) {

		this.$el.html(this.template({
			placeholder: this.options.placeholder,
			helpBlockText: this.options.helpBlockText
		}));

		var _self = this;

		if (_.size(this.collection.models) == 0 && _.size(this.options.autocomplete) == 0) {
			$("tbody", _self.el).html($('#tmpl-list-widget-child-empty').html());
		} else {

			// make a copy of our collection to work from
			var values = this.collection.clone();

			// look through our autocomplete values (if we have them) and render
			// them all as checkboxes
			if (this.options.autocomplete) {
				_.each(this.options.autocomplete, function(option) {
					var found = _.find(values.models, function(element) {
						return element.get('item') == option;
					});

					var model = null;
					var checked = false;

					if (found) {
						// if we found the element, check the box
						model = found;
						checked = true;
						// and remove it from the list of items to be rendered
						// later
						values.remove(found, {
							silent: true
						});
					} else {
						model = new Backbone.Model({
							item: option
						});
						checked = false;
					}

					var el = new ListWidgetChildView({
						model: model,
						toggle: true,
						checked: checked,
						collection: _self.collection
					}).render().el;
					$("tbody", _self.el).append(el);

				}, this);
			}

			// now render everything not in the autocomplete list
			_.each(values.models, function(model) {
				var el = new ListWidgetChildView({
					model: model,
					collection: _self.collection
				}).render().el;
				$("tbody", _self.el).append(el);
			}, this);
		}

		$(this.el).i18n();
		return this;
	}

});

var BreadCrumbView = Backbone.View.extend({

	tagName: 'ul',

	initialize: function(options) {
		this.options = options;

		if (!this.template) {
			this.template = _.template($('#tmpl-breadcrumbs').html());
		}

		this.$el.addClass('breadcrumb');

		this.collection.bind('add', this.render, this);
	},

	render: function() {

		this.$el.empty();
		var parent = this;

		// go through each of the breadcrumb models
		_.each(this.collection.models, function(crumb, index) {

			// if it's the last index in the crumbs then render the link
			// inactive
			if (index == parent.collection.size() - 1) {
				crumb.set({
					active: true
				}, {
					silent: true
				});
			} else {
				crumb.set({
					active: false
				}, {
					silent: true
				});
			}

			this.$el.append(this.template(crumb.toJSON()));
		}, this);

		$('#breadcrumbs').html(this.el);
		$(this.el).i18n();
	}
});

// User Profile

var UserProfileView = Backbone.View.extend({
	tagName: 'span',

	initialize: function(options) {
		this.options = options;
		if (!this.template) {
			this.template = _.template($('#tmpl-user-profile-element').html());
		}
	},

	render: function() {

		$(this.el).html($('#tmpl-user-profile').html());

		var t = this.template;

		_.each(this.model, function(value, key) {
			if (key && value) {

				if (typeof (value) === 'object') {

					var el = this.el;
					var k = key;

					_.each(value, function(value, key) {
						$('dl', el).append(t({
							key: key,
							value: value,
							category: k
						}));
					});
				} else if (typeof (value) === 'array') {
					// TODO: handle array types
				} else {
					$('dl', this.el).append(t({
						key: key,
						value: value
					}));
				}
			}
		}, this);

		$(this.el).i18n();
		return this;
	}
});

// error handler
var ErrorHandlerView = Backbone.View.extend({

	initialize: function(options) {
		this.options = options;
		if (!this.template) {
			this.template = _.template($('#tmpl-error-box').html());
		}
		if (!this.headerTemplate) {
			this.headerTemplate = _.template($('#tmpl-error-header').html());
		}
	},

	reloadPage: function(event) {
		event.preventDefault();
		window.location.reload(true);
	},

	handleError: function(message) {

		if (!message) {
			message = {};
		}

		var _self = this;

		return function(model, response, options) {

			if (message.log) {
				console.log(message.log);
			}

			_self.showErrorMessage(_self.headerTemplate({
				message: message,
				model: model,
				response: response,
				options: options
			}), _self.template({
				message: message,
				model: model,
				response: response,
				options: options
			}));

			$('#modalAlert .modal-body .page-reload').on('click', _self.reloadPage);

		}
	},

	showErrorMessage: function(header, message) {
		// hide the sheet if it's visible
		$('#loadingbox').sheet('hide');

		$('#modalAlert').i18n();
		$('#modalAlert div.modal-header').html(header);
		$('#modalAlert .modal-body').html(message);

		$('#modalAlert').modal({
			'backdrop': 'static',
			'keyboard': true,
			'show': true
		});

	}
});

// Router
var AppRouter = Backbone.Router.extend({

	routes: {

		"": "root"

	},

	root: function() {
		if (isAdmin()) {
			this.navigate('admin/clients', {
				trigger: true
			});
		} else {
			this.navigate('user/approved', {
				trigger: true
			});
		}
	},

	initialize: function() {

		this.breadCrumbView = new BreadCrumbView({
			collection: new Backbone.Collection()
		});

		this.breadCrumbView.render();

		this.errorHandlerView = new ErrorHandlerView();

		// call all the extra initialization functions
		var app = this;
		_.each(ui.init, function(fn) {
			fn(app);
		});

	},

	notImplemented: function() {
		this.breadCrumbView.collection.reset();
		this.breadCrumbView.collection.add([{
			text: $.t('admin.home'),
			href: ""
		}]);

		this.updateSidebar('none');

		$('#content').html("<h2>Not implemented yet.</h2>");
	},

	updateSidebar: function(item) {
		$('.sidebar-nav li.active').removeClass('active');

		$('.sidebar-nav li a[href^="manage/#' + item + '"]').parent().addClass('active');
	}
});

// holds the global app.
// this gets init after the templates load
var app = null;

// main
$(function() {

	var loader = function(source) {
		return $.get(source, function(templates) {
			console.log('Loading file: ' + source);
			$('#templates').append(templates);
		});
	};

	// load templates and append them to the body
	$.when.apply(null, ui.templates.map(loader)).done(function() {
		console.log('done');
		$.ajaxSetup({
			cache: false
		});
		app = new AppRouter();

		_.each(ui.routes.reverse(), function(route) {
			console.log("Adding route: " + route.name);
			app.route(route.path, route.name, route.callback);
		});

		app.on('route', function(name, args) {
			// scroll to top of page on new route selection
			$("html, body").animate({
				scrollTop: 0
			}, "slow");
		});

		// grab all hashed URLs and send them through the app router instead
		$(document).on('click', 'a[href^="manage/#"]', function(event) {
			event.preventDefault();
			app.navigate(this.hash.slice(1), {
				trigger: true
			});
		});

		var base = $('base').attr('href');
		$.getJSON(base + '.well-known/openid-configuration', function(data) {
			app.serverConfiguration = data;
			var baseUrl = $.url(app.serverConfiguration.issuer);
			Backbone.history.start({
				pushState: true,
				root: baseUrl.attr('relative') + 'manage/'
			});
		});
	});

	window.onerror = function(message, filename, lineno, colno, error) {
		console.log(message);
		// Display an alert with an error message
		$('#modalAlert div.modal-header').html($.t('error.title'));
		$('#modalAlert div.modal-body').html($.t('error.message') + message + ' <br /> ' + [filename, lineno, colno, error]);

		$("#modalAlert").modal({ // wire up the actual modal functionality
									// and show the dialog
			"backdrop": "static",
			"keyboard": true,
			"show": true
		// ensure the modal is shown immediately
		});

	}
});
