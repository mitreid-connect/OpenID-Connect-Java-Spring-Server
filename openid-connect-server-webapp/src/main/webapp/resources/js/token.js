/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
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

//----------------  ClientModel  ---------------------------------------------

var ClientModelView = Backbone.View.extend({

    tagName: 'tr',

    initialize: function (options) {
        this.options = options;

        if (!this.template) {
            this.template = _.template($('#tmpl-twitter-table-tokens').html());
        }

        //this.render();
    },

    events: {
        'click .btn-revokeAll': 'revokeAll',
        'click .btn-revokeAccessTokens': 'revokeAccessTokens',
        'click .btn-revokeRefreshTokens': 'revokeRefreshTokens'
    },

    render: function (eventName) {

        var json = {
            client: this.options.client.toJSON(),
            access: this.options.access,
            refresh: this.options.refresh
        };

        //alert("Entered render method!!!");

        this.$el.html(this.template(json));

        $(this.el).i18n();
        return this;
    },

    revokeAccessTokens: function (e) {
        e.preventDefault();

        if (confirm("Are you soore you want to delete all ACCESS TOKENS for this Client?")) {

            // THIS METHOD IS CPU BARBARIAN NEEDS TO BE IMPROVED FOR REAL , DO NOT USE IN PRODUCTION
            // CAUSE IT WILL CAUSE PROBLEMS WHEN SERVER HAS MANY CLIENTS AND TOKENS

            var _self = this;
            var clientId = this.options.client.get('clientId');


            //Clear all the access Tokens for this Client
            _.each(_.clone(this.options.access.models), function (model) {
                //alert("Model ClientId is : " + model.get('clientId') + " , ClientId is : " + clientId);
                if (model.get('clientId') == clientId) {
                    model.destroy();
                    //_self.parentView.delegateEvents();
                }
            });

            _self.parentView.delegateEvents();
            this.parentView.refreshTable();

            //------------------Pass one more time to be sure that tokens are removed---------------

            //Clear all the access Tokens for this Client
            _.each(_.clone(this.options.access.models), function (model) {
                if (model.get('clientId') == clientId) {
                    model.destroy();
                    //_self.parentView.delegateEvents();
                }
            });

            _self.parentView.delegateEvents();
            this.parentView.refreshTable();
        }

        return false;
    },

    revokeRefreshTokens: function (e) {
        e.preventDefault();

        if (confirm("Are you soore you want to delete all REFRESH TOKENS for this Client?")) {

            // THIS METHOD IS CPU BARBARIAN NEEDS TO BE IMPROVED FOR REAL , DO NOT USE IN PRODUCTION
            // CAUSE IT WILL CAUSE PROBLEMS WHEN SERVER HAS MANY CLIENTS AND TOKENS

            var _self = this;
            var clientId = this.options.client.get('clientId');


            //Clear all the refresh Tokens for this Client
            _.each(_.clone(this.options.refresh.models), function (model) {
                //alert("Model ClientId is : " + model.get('clientId') + " , ClientId is : " + clientId);
                if (model.get('clientId') == clientId) {
                    model.destroy();
                    //_self.parentView.delegateEvents();
                }
            });

            _self.parentView.delegateEvents();
            this.parentView.refreshTable();

            //------------------Pass one more time to be sure that tokens are removed---------------

            //Clear all the refresh Tokens for this Client
            _.each(_.clone(this.options.refresh.models), function (model) {
                if (model.get('clientId') == clientId) {
                    model.destroy();
                    //_self.parentView.delegateEvents();
                }
            });

            _self.parentView.delegateEvents();
            this.parentView.refreshTable();
        }

        return false;
    },

    revokeAll: function (e) {
        e.preventDefault();


        if (confirm("Are you soore you want to delete all the tokens for this client?")) {

            //-------------REVOKE ALL TOKENS OF THIS CLIENT ONLY --------------------------------------
            // THIS METHOD IS CPU BARBARIAN NEEDS TO BE IMPROVED FOR REAL , DO NOT USE IN PRODUCTION
            // CAUSE IT WILL CAUSE PROBLEMS WHEN SERVER HAS MANY CLIENTS AND TOKENS

            var _self = this;
            var clientId = this.options.client.get('clientId');


            //Clear all the access Tokens for this Client
            _.each(_.clone(this.options.access.models), function (model) {
                //alert("Model ClientId is : " + model.get('clientId') + " , ClientId is : " + clientId);
                if (model.get('clientId') == clientId) {
                    model.destroy({
                        dataType: false,
                        processData: false
                    });
                    //_self.parentView.delegateEvents();
                }
            });

//            _self.parentView.delegateEvents(); //not exactly soore about this method what is extacly is doing yet , i think it calls JQuery remove 
//            this.parentView.refreshTable();

            //Clear all the refresh Tokens for this Client
            _.each(_.clone(this.options.refresh.models), function (model) {
                //alert("Model ClientId is : " + model.get('clientId') + " , ClientId is : " + clientId);
                if (model.get('clientId') == clientId) {
                    model.destroy({
                        dataType: false,
                        processData: false
                    });
                    //_self.parentView.delegateEvents();
                }
            });


//            _self.parentView.delegateEvents();
            this.parentView.refreshTable();


            //-------------REVOKE ALL TOKENS OF THIS CLIENT ONLY --------------------------------------

        }
        return false;
    },
});


//----------------  AccessTokenModel  ---------------------------------------------
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

    initialize: function (options) {
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
        this.listenTo(this.model, 'destroy', this.remove);
    },

    events: {
        'click .btn-delete': 'deleteToken',
        'click .token-substring': 'showTokenValue',
        'click .toggleMoreInformation': 'toggleMoreInformation',
        'click .btn-copy': 'copyToken'
    },

    render: function (eventName) {

        var expirationDate = this.model.get("expiration");

        if (expirationDate == null) {
            expirationDate = "Never";
        } else if (!moment(expirationDate).isValid()) {
            expirationDate = "Unknown";
        } else {
            expirationDate = moment(expirationDate).calendar();
        }

        var token = this.model.get("value");

        var decoded = jwt_decode(token);

        var issuedDate = decoded.iat;

        issuedDate = new Date(issuedDate * 1000);
        issuedDate = moment(issuedDate).calendar();

        var json = {
            token: this.model.toJSON(),
            client: this.options.client.toJSON(),
            formattedExpiration: expirationDate,
            formattedIssuedDate: issuedDate
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

    remove: function () {

        var _self = this;

//        this.model.destroy({
//            dataType: false,
//            processData: false,
//            success: function () {
//
//                _self.$el.fadeTo("fast", 0.00, function () { // fade
//                    $(this).slideUp("fast", function () { // slide up
        $(this).remove(); // then remove from the DOM
//                        // refresh the table in case we removed an id token,
//                        // too
//        _self.parentView.refreshTable();
//                    });
//                });
//            },
//            error: app.errorHandlerView.handleError()
//        });

        this.parentView.delegateEvents();

        return false;
    },

    deleteToken: function (e) {
        e.preventDefault();

        if (confirm($.t("token.token-table.confirm"))) {

            var _self = this;

            this.model.destroy({
                dataType: false,
                processData: false,
                success: function () {

                    _self.$el.fadeTo("fast", 0.00, function () { // fade
                        $(this).slideUp("fast", function () { // slide up
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

            this.parentView.delegateEvents();

        }

        return false;
    },

    toggleMoreInformation: function (e) {
        e.preventDefault();
        if ($('.moreInformation', this.el).is(':visible')) {
            // hide it
            //$('.moreInformation', this.el).hide('fast');
            $('.toggleMoreInformation i', this.el).attr('class', 'icon-chevron-right');
            $('.moreInformationContainer', this.el).removeClass('alert').removeClass('alert-info').addClass('muted');

        } else {
            // show it
            $('.moreInformation', this.el).show('fast');
            $('.toggleMoreInformation i', this.el).attr('class', 'icon-chevron-down');
            $('.moreInformationContainer', this.el).addClass('alert').addClass('alert-info').removeClass('muted');
        }
    },

    close: function () {
        $(this.el).unbind();
        $(this.el).empty();
    },

    copyToken: function (e) {
        var _self = this;
        var val = this.model.get("value");
        var dummy = document.createElement("input");
        document.body.appendChild(dummy);
        dummy.setAttribute("id", "dummy_id");
        $('#dummy_id').val(val);
        try {
            dummy.focus();
            dummy.select();
            document.execCommand("copy");

            //Show a Success Notification on the User
            $.notify({
                // options
                message: 'Successfully copied the token value'
            }, {
                // settings
                type: 'success',
                placement: {
                    from: "top",
                    align: "center"
                },
            }, {
                delay: 700,
                timer: 1000,
            });

        } catch (e) {
            //Show error trying to copy the value of text field
            $.notify({
                // options
                message: 'Please select the text area and then press Ctrl / Cmd + C to copy',
                icon: 'fa fa-paw'
            }, {
                // settings
                type: 'danger',
                placement: {
                    from: "bottom",
                    align: "left"
                },
            }, {
                delay: 1200,
                timer: 1000,
            });
        }
        document.body.removeChild(dummy);

        return false;
    },

    showTokenValue: function (e) {
        e.preventDefault();
        $('.token-substring ' + (this.model.id) + (this.model.refreshTokenId), this.el).hide();
        $('.token-full ' + (this.model.id) + (this.model.refreshTokenId), this.el).show();
    }
});


//------------------- RefreshTokenModel -------------------------
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

    initialize: function (options) {
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
        this.listenTo(this.model, 'destroy', this.remove);
    },

    events: {
        'click .btn-delete': 'deleteToken',
        'click .token-substring': 'showTokenValue',
        'click .toggleMoreInformation': 'toggleMoreInformation',
        'click .btn-copy': 'copyToken'
    },

    render: function (eventName) {

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
            accessTokenCount: this.options.accessTokenCount,
            access: this.options.access,
            refresh: this.options.refresh,
            clientList: this.options.clientList
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

    remove: function () {
        var _self = this;

//        this.model.destroy({
//            dataType: false,
//            processData: false,
//            success: function () {
//
//                _self.$el.fadeTo("fast", 0.00, function () { // fade
//                    $(this).slideUp("fast", function () { // slide up
        $(this).remove(); // then remove from the DOM
//                        // refresh the table in case we removed an id token,
//                        // too
//        _self.parentView.refreshTable();
//                    });
//                });
//            },
//            error: app.errorHandlerView.handleError()
//        });

        this.parentView.delegateEvents();

        return false;
    },

    deleteToken: function (e) {
        e.preventDefault();

        if (confirm($.t("token.token-table.confirm"))) {

            var _self = this;

            this.model.destroy({
                dataType: false,
                processData: false,
                success: function () {

                    _self.$el.fadeTo("fast", 0.00, function () { // fade
                        $(this).slideUp("fast", function () { // slide up
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

            this.parentView.delegateEvents();

        }

        return false;
    },

    toggleMoreInformation: function (e) {
        e.preventDefault();
        if ($('.moreInformation', this.el).is(':visible')) {
            // hide it
            // $('.moreInformation', this.el).hide('fast');
            $('.toggleMoreInformation i', this.el).attr('class', 'icon-chevron-right');
            $('.moreInformationContainer', this.el).removeClass('alert').removeClass('alert-info').addClass('muted');
        } else {
            // show it
            $('.moreInformation', this.el).show('fast');
            $('.toggleMoreInformation i', this.el).attr('class', 'icon-chevron-down');
            $('.moreInformationContainer', this.el).addClass('alert').addClass('alert-info').removeClass('muted');
        }

    },

    close: function () {
        $(this.el).unbind();
        $(this.el).empty();
    },

    copyToken: function (e) {
        var _self = this;
        var val = this.model.get("value");
        var dummy = document.createElement("input");
        document.body.appendChild(dummy);
        dummy.setAttribute("id", "dummy_id");
        $('#dummy_id').val(val);
        try {
            dummy.focus();
            dummy.select();
            document.execCommand("copy");
            alert('Successfully copied the text.');
        } catch (e) {
            alert('Please select the text area and then press Ctrl/Cmd+C to copy');
        }
        document.body.removeChild(dummy);

        return false;
    },

    showTokenValue: function (e) {
        e.preventDefault();
        $('.token-substring ' + (this.model.id), this.el).hide();
        $('.token-full ' + (this.model.id), this.el).show();
    }
});

var TokenListView = Backbone.View.extend({
    tagName: 'span',

    initialize: function (options) {
        this.options = options;
    },

    events: {
        "click .refresh-table": "refreshTable"//,
                // 'page .paginator-access': 'changePageAccess',
                //'page .paginator-refresh': 'changePageRefresh'
    },

    load: function (callback) {
        if (this.model.access.isFetched && this.model.refresh.isFetched && this.options.clientList.isFetched && this.options.systemScopeList.isFetched) {
            callback();
            return;
        }

        $('#loadingbox').sheet('show');
        $('#loading').html(
                '<span class="label" id="loading-access">' + $.t('token.token-table.access-tokens') + '</span> ' + '<span class="label" id="loading-refresh">' + $.t('token.token-table.refresh-tokens') + '</span> ' + '<span class="label" id="loading-clients">' + $.t('common.clients') + '</span> ' + '<span class="label" id="loading-scopes">'
                + $.t('common.scopes') + '</span> ');

        $.when(this.model.access.fetchIfNeeded({
            success: function (e) {
                $('#loading-access').addClass('label-success');
            },
            error: app.errorHandlerView.handleError()
        }), this.model.refresh.fetchIfNeeded({
            success: function (e) {
                $('#loading-refresh').addClass('label-success');
            },
            error: app.errorHandlerView.handleError()
        }), this.options.clientList.fetchIfNeeded({
            success: function (e) {
                $('#loading-clients').addClass('label-success');
            },
            error: app.errorHandlerView.handleError()
        }), this.options.systemScopeList.fetchIfNeeded({
            success: function (e) {
                $('#loading-scopes').addClass('label-success');
            },
            error: app.errorHandlerView.handleError()
        })).done(function () {
            $('#loadingbox').sheet('hide');
            callback();
        });

    },

    /*changePageAccess: function (event, num) {
     $('.paginator-access', this.el).bootpag({
     page: num
     });
     $('#access-token-table tbody tr', this.el).each(function (index, element) {
     if (Math.ceil((index + 1) / 10) != num) {
     $(element).hide();
     } else {
     $(element).show();
     }
     });
     },
     
     changePageRefresh: function (event, num) {
     $('.paginator-refresh', this.el).bootpag({
     page: num
     });
     $('#refresh-token-table tbody tr', this.el).each(function (index, element) {
     if (Math.ceil((index + 1) / 10) != num) {
     $(element).hide();
     } else {
     $(element).show();
     }
     });
     },*/

    refreshTable: function (e) {
        $('#loadingbox').sheet('show');
        $('#loading').html(
                '<span class="label" id="loading-access">' + $.t('token.token-table.access-tokens') + '</span> ' + '<span class="label" id="loading-refresh">' + $.t('token.token-table.refresh-tokens') + '</span> ' + '<span class="label" id="loading-clients">' + $.t('common.clients') + '</span> ' + '<span class="label" id="loading-scopes">'
                + $.t('common.scopes') + '</span> ');
        var _self = this;
        $.when(this.model.access.fetch({
            success: function (e) {
                $('#loading-access').addClass('label-success');
            },
            error: app.errorHandlerView.handleError()
        }), this.model.refresh.fetch({
            success: function (e) {
                $('#loading-refresh').addClass('label-success');
            },
            error: app.errorHandlerView.handleError()
        }), this.options.clientList.fetch({
            success: function (e) {
                $('#loading-clients').addClass('label-success');
            },
            error: app.errorHandlerView.handleError()
        }), this.options.systemScopeList.fetch({
            success: function (e) {
                $('#loading-scopes').addClass('label-success');
            },
            error: app.errorHandlerView.handleError()
        })).done(function () {
            _self.render();
            $('#loadingbox').sheet('hide');
        });
    },

    togglePlaceholder: function () {
        if (this.model.access.length > 0) {
            //  $('#access-token-table', this.el).show();
            $('#access-token-table-empty', this.el).hide();
        } else {
            //  $('#access-token-table', this.el).hide();
            $('#access-token-table-empty', this.el).show();
        }
        if (this.model.refresh.length > 0) {
            // $('#refresh-token-table', this.el).show();
            $('#refresh-token-table-empty', this.el).hide();
        } else {
            //  $('#refresh-token-table', this.el).hide();
            $('#refresh-token-table-empty', this.el).show();
        }


        //Ok we want the twitter-table ( That one which contains all the clients to be shown 
        //when we have tokens generally . Either they are access tokens or they are refresh tokens
        if (this.model.access.length > 0 || this.model.refresh.length > 0) {
            $('#twitter-table', this.el).show();
        } else {
            $('#twitter-table', this.el).hide();
        }

        //Some extra , i want the access token and refresh token tables to remain hiden 
        $('#access-token-table', this.el).hide();
        $('#refresh-token-table', this.el).hide();

        //Who knows wtf this is
        $('#access-token-count', this.el).html(this.model.access.length);
        $('#refresh-token-count', this.el).html(this.model.refresh.length);
    },

    render: function (eventName) {

        // append and render the table structure
        $(this.el).html($('#tmpl-token-table').html());

        var _self = this;

//        // set up pagination
//        var numPagesAccess = Math.ceil(this.model.access.length / 10);
//        if (numPagesAccess > 1) {
//            $('.paginator-access', this.el).show();
//            $('.paginator-access', this.el).bootpag({
//                total: numPagesAccess,
//                page: 1
//            });
//        } else {
//            $('.paginator-access', this.el).hide();
//        }

        // count up refresh tokens
        var refreshCount = {};



        //Keep an instance of the lists here
        var accessTokensList = this.model.access;
        var refreshTokensList = this.model.refresh;
        var clientList = _self.options.clientList;


        //=============Count access tokens for each refresh token===================
        _.each(accessTokensList.models, function (token, index) {
            var refId = token.get('refreshTokenId');
            if (refId != null) {
                if (refreshCount[refId]) {
                    refreshCount[refId] += 1;
                } else {
                    refreshCount[refId] = 1;
                }
            }
        });

        //================Run a loop testing registered clients========================
        _.each(clientList.models, function (client, index) {
            var view = new ClientModelView({
                client: client,
                access: accessTokensList,
                refresh: refreshTokensList
            });
            view.parentView = _self;
            var element = view.render().el;
            $('#twitter-table', _self.el).append(element);
            // alert("Client name name is :[ " + client.get('clientName') + " ]");

            //Keep the client id to a variable
            var clientId = client.get('clientId');

            //================Add the RefreshTokenViews on the Client===================
            _.each(refreshTokensList.models, function (token, index) {

                //Check the token clientId
                if (token.get('clientId') === clientId) {

                    // look up client
                    //var client = _self.options.clientList.getByClientId(token.get('clientId'));
                    var view = new RefreshTokenView({
                        model: token,
                        client: client,
                        systemScopeList: _self.options.systemScopeList,
                        accessTokenCount: refreshCount[token.get('id')],
                        access: accessTokensList,
                        refresh: refreshTokensList,
                        clientList: clientList
                    });
                    view.parentView = _self;
                    var element = view.render().el;
                    $('#client' + clientId + '-token-table', _self.el).append(element);

                }
            });


            //=================Add the AccessTokenViews on the Client=========================
            //
            //1. If they have refresh token add them below refresh tokens
            //                            or
            //2. If they have not refresh token add them to an individual row
            _.each(accessTokensList.models, function (token, index) {

                //Check the token clientId
                if (token.get('clientId') === clientId) {

                    // look up client
                    // var client = clientList.getByClientId(token.get('clientId'));
                    var view = new AccessTokenView({
                        model: token,
                        client: client,
                        systemScopeList: _self.options.systemScopeList,
                        access: accessTokensList,
                        refresh: refreshTokensList,
                        clientList: clientList
                    });
                    view.parentView = _self;
                    var element = view.render().el;

                    //Let's check if access token has any existing refresh token
                    var refTokenId = token.get('refreshTokenId');
                    if (refTokenId != null)
                        $('#refresh' + clientId + refTokenId + '-token-table', _self.el).append(element);
                    else
                        $('#client' + clientId + '-token-table', _self.el).append(element);


                }
            });

        });



        //Create the AccessTokenViews
//        _.each(this.model.access.models, function (token, index) {
//            // look up client
//            var client = _self.options.clientList.getByClientId(token.get('clientId'));
//            var view = new AccessTokenView({
//                model: token,
//                client: client,
//                systemScopeList: _self.options.systemScopeList,
//                access: accessTokensList,
//                refresh: refreshTokensList,
//                clientList: clientList
//            });
//            view.parentView = _self;
//            var element = view.render().el;
//            $('#access-token-table', _self.el).append(element);
//            if (Math.ceil((index + 1) / 10) != 1) {
//                $(element).hide();
//            }
//
//            // console.log(token.get('refreshTokenId'));
//            var refId = token.get('refreshTokenId');
//            if (refId != null) {
//                if (refreshCount[refId]) {
//                    refreshCount[refId] += 1;
//                } else {
//                    refreshCount[refId] = 1;
//                }
//
//            }
//
//        });

        // console.log(refreshCount);

        // set up pagination
//        var numPagesRefresh = Math.ceil(this.model.refresh.length / 10);
//        if (numPagesRefresh > 1) {
//            $('.paginator-refresh', this.el).show();
//            $('.paginator-refresh', this.el).bootpag({
//                total: numPagesRefresh,
//                page: 1
//            });
//        } else {
//            $('.paginator-refresh', this.el).hide();
//        }

//
//        _.each(this.model.refresh.models, function (token, index) {
//
//            // look up client
//            var client = _self.options.clientList.getByClientId(token.get('clientId'));
//            var view = new RefreshTokenView({
//                model: token,
//                client: client,
//                systemScopeList: _self.options.systemScopeList,
//                accessTokenCount: refreshCount[token.get('id')],
//                access: accessTokensList,
//                refresh: refreshTokensList,
//                clientList: clientList
//            });
//            view.parentView = _self;
//            var element = view.render().el;
//            $('#refresh-token-table', _self.el).append(element);
//            if (Math.ceil((index + 1) / 10) != 1) {
//                $(element).hide();
//            }
//
//        });

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
    callback: function () {
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

        view.load(function (collection, response, options) {
            $('#content').html(view.render().el);
            setPageTitle($.t('token.manage'));
        });

    }
});

ui.templates.push('resources/template/token.html');

ui.init.push(function (app) {
    app.accessTokensList = new AccessTokenCollection();
    app.refreshTokensList = new RefreshTokenCollection();
});
