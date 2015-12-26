/*******************************************************************************
 * Copyright 2015 The MITRE Corporation
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
var BlackListModel = Backbone.Model.extend({
	idAttribute: 'id',
	
	urlRoot: 'api/blacklist'
});

var BlackListCollection = Backbone.Collection.extend({
	initialize: function() { },

	url: "api/blacklist"
});

var BlackListListView = Backbone.View.extend({
	tagName: 'span',
	
	initialize:function(options) {
    	this.options = options;
	},

	load:function(callback) {
    	if (this.collection.isFetched) {
    		callback();
    		return;
    	}

    	$('#loadingbox').sheet('show');
    	$('#loading').html(
                '<span class="label" id="loading-blacklist">' + $.t('admin.blacklist') + '</span> '
    	        );

    	$.when(this.collection.fetchIfNeeded({success:function(e) {$('#loading-blacklist').addClass('label-success');}}))
    		.done(function() {
    			$('#loadingbox').sheet('hide');
    			callback();
    	});    	
    },
	
	events: {
        "click .refresh-table":"refreshTable",
        "click .btn-add":"addItem",
        "submit #add-blacklist form":"addItem"
	},

    refreshTable:function(e) {
    	e.preventDefault();

    	var _self = this;
    	$('#loadingbox').sheet('show');
        $('#loading').html(
                '<span class="label" id="loading-blacklist">' + $.t('admin.blacklist') + '</span> '
                );

    	$.when(this.collection.fetch()).done(function() {
    	    		$('#loadingbox').sheet('hide');
    	    		_self.render();
    			});    	
    },	
	
	togglePlaceholder:function() {
		if (this.collection.length > 0) {
			$('#blacklist-table', this.el).show();
			$('#blacklist-table-empty', this.el).hide();
		} else {
			$('#blacklist-table', this.el).hide();
			$('#blacklist-table-empty', this.el).show();
		}
	},
	
	render:function (eventName) {
		
		$(this.el).html($('#tmpl-blacklist-table').html());
		
		var _self = this;
		_.each(this.collection.models, function(blacklist) {
			var view = new BlackListWidgetView({model: blacklist});
			view.parentView = _self;
			$("#blacklist-table", _self.el).append(view.render().el);
		}, this);
		
		this.togglePlaceholder();
		
        $(this.el).i18n();
		return this;
	},
	
	addItem:function(e) {
    	e.preventDefault();

    	var input_value = $("#blacklist-uri", this.el).val().trim();
    	
    	if (input_value === "") {
    		return;
    	}
    	
    	// TODO: URI/pattern validation, check against existing clients
    	
    	var item = new BlackListModel({
    		uri: input_value
    	});
    	
    	var _self = this; // closures...
    	
    	item.save({}, {
    		success:function() {
    			_self.collection.add(item);
    			_self.render();
    		},
    		error:function(error, response) {
    			//Pull out the response text.
				var responseJson = JSON.parse(response.responseText);
        		
        		//Display an alert with an error message
				$('#modalAlert div.modal-header').html(responseJson.error);
        		$('#modalAlert div.modal-body').html(responseJson.error_description);
        		
    			 $("#modalAlert").modal({ // wire up the actual modal functionality and show the dialog
    				 "backdrop" : "static",
    				 "keyboard" : true,
    				 "show" : true // ensure the modal is shown immediately
    			 });
    		}
    	});

	}

});

var BlackListWidgetView = Backbone.View.extend({
	
	tagName: 'tr',
	
	initialize:function(options) {
		this.options = options;
		
		if (!this.template) {
			this.template = _.template($('#tmpl-blacklist-item').html());
		}
	},
	
	render:function() {

		this.$el.html(this.template(this.model.toJSON()));

        return this;
		
	},
	
	events:{
		'click .btn-delete':'deleteBlacklist'
	},
	
    deleteBlacklist:function (e) {
    	e.preventDefault();

        if (confirm($.t("blacklist.confirm"))) {
            var _self = this;

            this.model.destroy({
            	dataType: false, processData: false,
                success:function () {
                	
                    _self.$el.fadeTo("fast", 0.00, function () { //fade
                        $(this).slideUp("fast", function () { //slide up
                            $(this).remove(); //then remove from the DOM
                            _self.parentView.togglePlaceholder();
                        });
                    });
                },
            	error:function (model, response) {
					//Pull out the response text.
            		var responseJson = {error: 'Error', error_description: 'Error.'};
            		if (response) {
    					responseJson = JSON.parse(response.responseText);
            		}
            		
            		//Display an alert with an error message
					$('#modalAlert div.modal-header').html(responseJson.error);
	        		$('#modalAlert div.modal-body').html(responseJson.error_description);
            		
        			 $("#modalAlert").modal({ // wire up the actual modal functionality and show the dialog
        				 "backdrop" : "static",
        				 "keyboard" : true,
        				 "show" : true // ensure the modal is shown immediately
        			 });
            	}
            });

            _self.parentView.delegateEvents();
        }

        return false;
    }

});

