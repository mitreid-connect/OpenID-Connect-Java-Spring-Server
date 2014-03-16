/**
 * Bootstrap modal sheet
 * 
 * Author: Michaël Perrin
 * https://github.com/michaelperrin/bootstrap-modal-sheet
 */
 
(function () {
  "use strict";

  /*
   * Sheet class definition
   */

  var Sheet = function (element, options) {
    this.options = options;
    this.$element = $(element)
      .delegate('[data-dismiss="sheet"]', 'click.dismiss.sheet', $.proxy(this.hide, this));
    this.options.remote && this.$element.find('.sheet-body').load(this.options.remote);
  };

  Sheet.prototype = {
    constructor: Sheet,

    toggle: function () {
      return this[!this.isShown ? 'show' : 'hide']();
    },

    show: function () {
      var e = $.Event('show');
      var that = this;

      this.$element.trigger(e);

      if (this.isShown || e.isDefaultPrevented()) {
        return;
      }

      this.isShown = true;

      this.escape();

      var transition = this.$element.hasClass('fade');

      if (!this.$element.parent().length) {
        this.$element.appendTo(document.body); //don't move modals dom position
      }

      this.placeBelowParent();

      if (this.options.backdrop) {
        this.$backdrop = $('<div class="sheet-backdrop" />')
          .appendTo(document.body)
        ;

        this.$backdrop.click(
          this.options.backdrop == 'static' ?
            $.proxy(this.$element[0].focus, this.$element[0])
          : $.proxy(this.hide, this)
        );

        this.$backdrop.addClass('in');
      }

      transition ?
        this.$element.slideDown('fast', function() { that.$element.focus().trigger('shown'); }) :
        this.$element.show().focus().trigger('shown')
      ;

      this.$element
        .addClass('in')
        .attr('aria-hidden', false)
        .focus()
      ;
    },

    placeBelowParent: function() {
      if ( ! this.options.sheetParent) {
        return;
      }

      var $parent = $(this.options.sheetParent);

      if ( ! $parent) {
        return;
      }

      // Compute vertical position
      var sheetPosition = $parent.offset().top + $parent.height();
      this.$element.css('top', sheetPosition + 'px');

      // Compute horizontal position (make the sheet centered)
      var margin = ($parent.width() - this.$element.width()) / 2;
      var leftPosition = $parent.offset().left + margin;

      this.$element.css('left', leftPosition + 'px');
    },

    hide: function (e) {
      e && e.preventDefault();

      var that = this;

      e = $.Event('hide');

      this.$element.trigger(e);

      if (!this.isShown || e.isDefaultPrevented()) {
        return;
      }

      this.isShown = false;

      this.escape();

      $(document).off('focusin.sheet');

      this.$element
        .removeClass('in')
        .attr('aria-hidden', true)
      ;

      this.hideSheet();
    },

    hideSheet: function () {
      var transition = this.$element.hasClass('fade');

      transition ? this.$element.slideUp('fast') : this.$element.hide();
      this.removeBackdrop();
      this.$element.trigger('hidden');
    },

    removeBackdrop: function () {
      if ( ! this.options.backdrop) {
        return;
      }

      this.$backdrop.remove();
      this.$backdrop = null;
    },

    escape: function () {
      var that = this;

      if (this.isShown && this.options.keyboard) {
        this.$element.on('keyup.dismiss.sheet', function (e) {
          e.which == 27 && that.hide();
        });
      } else if (!this.isShown) {
        this.$element.off('keyup.dismiss.sheet');
      }
    }
  };

  /*
   * jQuery Sheet plugin definition
   */

  $.fn.sheet = function (option) {
    return this.each(function () {
      var $this = $(this);

      var data = $this.data('sheet');

      var options = $.extend({}, $.fn.sheet.defaults, $this.data(), typeof option == 'object' && option);

      if (!data) {
        $this.data('sheet', (data = new Sheet(this, options)));
      }

      if (typeof option == 'string') {
        data[option]();
      } else if (options.show) {
        data.show();
      }
    });
  };

  $.fn.sheet.defaults = {
    keyboard: true,
    show: true,
    backdrop: true
  };

  $.fn.sheet.Constructor = Sheet;

 /* SHEET DATA-API
  * ============== */

  $(document).on('click.sheet.data-api', '[data-toggle="sheet"]', function (e) {
    var $this = $(this);
    var href = $this.attr('href');
    var $target = $($this.attr('data-target') || (href && href.replace(/.*(?=#[^\s]+$)/, ''))); //strip for ie7
    var option = $target.data('sheet') ? 'toggle' : $.extend({ remote:!/#/.test(href) && href }, $target.data(), $this.data());

    e.preventDefault();

    $target
      .sheet(option)
      .one('hide', function () {
        $this.focus();
      });
  });
}).call(this);
