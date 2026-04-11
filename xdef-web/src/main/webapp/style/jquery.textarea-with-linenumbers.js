(function($){
    $.fn.linenumbers = function(in_opts){
        // Settings and Defaults
        var opt = $.extend({
            gutter_width: '30px',
            start: 1,
            digits: 3.,
            lineValidatorCallback: function(){return 0;}
        }, in_opts);
        // Function run
        return this.each(function(idx){
            if( $(this).prop('nodeName') != 'TEXTAREA' ) {
                return;
            }
            var dataname = 'linenumbers-gutter-id'+idx;
            // Remove existing div and the textarea from previous run
            $("[id='"+dataname+"']").remove();
            // Get some numbers sorted out for the CSS changes
            var textarea_height = parseFloat($(this).css("height"));
            // Create the new textarea and style it
            var lnbox = $(this).before('<div class="linenumbers-gutter" id="'+dataname+'"></div>');
            // Edit the existing textarea's styles
            $(this).css({
                'position': 'relative',
                'left': parseFloat(opt.gutter_width)+'px',
                'width': parseFloat($(this).css("width"))-parseFloat(opt.gutter_width)+'px',
                'white-space': 'pre',
                'overflow-wrap': 'normal',
                'overflow-x': 'scroll',
            });
            // Add a clearing div.
            $(this).after('<div style="clear:both;"></div>');
            // Define a simple variable for the line-numbers box
            var lnbox = $('#'+dataname);
            $(lnbox).css({
                'top': (textarea_height+parseFloat($(this).css('border-top'))+parseFloat($(this).css('border-bottom'))+parseFloat($(this).css('padding-top'))+parseFloat($(this).css('padding-bottom')))+'px',
                'margin-top': -(textarea_height+parseFloat($(this).css('border-top'))+parseFloat($(this).css('border-bottom'))+parseFloat($(this).css('padding-top'))+parseFloat($(this).css('padding-bottom')))+'px',
                'padding-top': parseFloat($(this).css('padding-top')) + 'px',
                'padding-bottom': parseFloat($(this).css('padding-bottom')) + 'px',
                'width': parseFloat(opt.gutter_width)+'px',
                'height': textarea_height+'px',

                // misc css stuff
                'resize': 'none',
                'white-space': 'pre',
                'text-align': 'right',
                'overflow':'hidden',
                'position': 'relative',
                // copy the border styles from the textarea
                'border-style': $(this).css('border-style'),
                'border-width': $(this).css('border-width'),
                // copy the font styles from the textarea
                'font-style': $(this).css('font-style'),
                'font-size': $(this).css('font-size'),
                'font-variant': $(this).css('font-variant'),
                'font-family': $(this).css('font-family'),
                'line-height': $(this).css('line-height'),
            });
            // Bind some actions to all sorts of events that may change it's contents
            $(this).bind('blur focus change keyup keydown click',function(){

                // current text
                var text = $(this).val();

                // get cursor position
                var cursorPosition = this.selectionStart;
                var textBeforeCursor = text.substring(0, cursorPosition);
                var currentLineNumber = textBeforeCursor.split('\n').length - 1;

                // Break apart and regex the lines, everything to spaces sans linebreaks
                var lines = "\n"+$(this).val();
                lines = text.split("\n");
                // declare output var
                var line_number_output='';
                // declare spacers and max_spacers vars, and set defaults
                var max_spacers = ''; var spacers = '';
                for(i=0;i<opt.digits;i++){
                    max_spacers += ' ';
                }
                var obj = this;
                // Loop through and process each line
                $.each(lines,function(k,v){
                    var validatorClasses = opt.lineValidatorCallback(obj, k, v, k == currentLineNumber);
                    if(k == currentLineNumber) {
                        validatorClasses += ' current-line';
                    }
                    // Determine the appropriate number of leading spaces
                    lencheck = k+opt.start+'!';
                    spacers = max_spacers.substr(lencheck.length-1);
                    // Add the line with out line number, to the output variable
                    line_number_output += '<span class="'+validatorClasses+'">'+spacers+(k+opt.start)+' '+'</span>';
                });
                // Give the text area out modified content.
                $(lnbox).html(line_number_output);
                // Change scroll position as they type, makes sure they stay in sync
                $(lnbox).scrollTop($(this).scrollTop());
            });
            // Lock scrolling together, for mouse-wheel scrolling
            $(this).scroll(function(){
                $(lnbox).scrollTop($(this).scrollTop());
            });
            // Fire it off once to get things started
            $(this).trigger('keyup');
        });
    };
})(jQuery);
