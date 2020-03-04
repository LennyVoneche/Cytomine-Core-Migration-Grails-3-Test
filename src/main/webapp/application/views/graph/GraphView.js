var GraphView = Backbone.View.extend({
    title : null,
    initialize: function (options) {
        if(!window.app.isUndefined(options.title)){
            this.title = options.title;
        }
        if(!window.app.isUndefined(options.resizeBtn)){
            this.resizeBtn = options.resizeBtn;
        } else {
            this.resizeBtn = true;
        }
    },
    render: function () {
        var self = this;
        if(!window.app.isUndefined(this.title)){
            var titleHtml = "<div class='header_h'>";
            titleHtml += "<h4 class='header-graph'><i class='glyphicon glyphicon-stats'></i> "+this.title+"</h4>";
            if(this.resizeBtn) titleHtml += "<span class='resize-button' aria-hidden='true'><i class='glyphicon glyphicon-resize-full visible-md visible-lg'></i></span>";
            titleHtml += "</div>";
            $(self.el).append(titleHtml);
        }
        $(self.el).on("click",".resize-button",function(){
            $(this).closest(".graph").toggleClass('graph-enlarge');
            if (typeof self.resize == 'function') {
                setTimeout(function(){
                    self.resize();
                }, 1000);
            }
            $(this).children('i').toggleClass("glyphicon-resize-full");
            $(this).children('i').toggleClass("glyphicon-resize-small");
        })
    }
});
