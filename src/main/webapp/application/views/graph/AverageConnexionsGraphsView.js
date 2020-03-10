var AverageConnexionsGraphsView = GraphView.extend({
    graphs : [],
    initialize: function (options) {
        this.graphs = [];
        this.constructor.__super__.initialize.apply(this,[options]);
        var url = "api/averageConnections.json?";
        if(!window.app.isUndefined(options.project)){
            url += "project="+options.project+"&";
        }
        if(!window.app.isUndefined(options.user)){
            url += "user="+options.user+"&";
        }
        url += "period=";

        this.graphs.push({name : "Hour", period : "hour", url: url+"hour", panel : null});
        this.graphs.push({name : "Day", period : "day", url: url+"day", panel : null});
        this.graphs.push({name : "Week", period : "week", url: url+"week", panel : null});
    },
    resize: function() {
        var self = this
        _.each(self.graphs, function(it){
            if(it.panel !== null) it.panel.resize();
        });
    },
    render: function () {
        this.constructor.__super__.render.apply(this);
        var self = this;

        var tpl = 'By <select id="avgConnectionsSelection">';

        _.each(self.graphs, function(it){
            tpl+= '<option>'+it.name+'</option>';
        });

        tpl += '</select>';
        tpl += '<span class="col-md-offset-1">Since  <input type="text" id="avgConnectionDatepickerSince" class="avgConnectionDatepicker" placeholder="date"><span class="add-on"><i class="icon-calendar"></i> </span></span>';
        tpl += '<span class="col-md-offset-1">Until  <input type="text" id="avgConnectionDatepickerUntil" class="avgConnectionDatepicker" placeholder="date"><span class="add-on"><i class="icon-calendar"></i> </span></span>';

        _.each(self.graphs, function(it){
            tpl+= '<div class="col-md-offset-1 graph-avg" id="AvgConnectionsBy'+it.name+'" style="text-align: center;"></div>';
        });

        $(self.el).append(tpl);

        $(self.el).find(".avgConnectionDatepicker").datepicker({
            beforeShowDay: function (date) {
                var tomorrow = new Date();
                tomorrow.setDate(tomorrow.getDate()+1);
                if (date <= tomorrow) {
                    return [true, "", ""];
                } else {
                    return [false, "", "Cannot select into the future."];
                }
            },
            onSelect: function (dateStr) {
                // every avg-graph is deleted.
                $(self.el).find(".graph-avg").empty();
                _.each(self.graphs, function(it){
                    it.panel = null;
                });
                // force refresh
                $(self.el).find("#avgConnectionsSelection").trigger("change");
            }
        });

        var lastDay = new Date();
        var firstDay = new Date(lastDay.getTime());
        firstDay.setMonth(lastDay.getMonth()-12);
        lastDay.setDate(lastDay.getDate()+1);
        $(self.el).find("#avgConnectionDatepickerSince").datepicker('setDate', firstDay);
        $(self.el).find("#avgConnectionDatepickerUntil").datepicker('setDate', lastDay);


        $(self.el).find("#avgConnectionsSelection").on("change", function(e){

            $(self.el).find(".graph-avg").hide();

            var chosen = $(this).val();
            chosen = _.find(self.graphs, function(it) {
                return it.name === chosen;
            });

            var panel = $(self.el).find("#AvgConnectionsBy"+chosen.name);
            // create objet if not null
            if(chosen.panel === null){
                var afterThan = $(self.el).find("#avgConnectionDatepickerSince").datepicker('getDate');
                var beforeThan = $(self.el).find("#avgConnectionDatepickerUntil").datepicker('getDate');
                chosen.panel = new HistogramActivitiesChart({
                    el: panel,
                    period : chosen.period,
                    url : chosen.url+"&afterThan="+afterThan.getTime()+"&beforeThan="+beforeThan.getTime(),
                    format: '%'
                });
                chosen.panel.render();
            }
            panel.show();
        });

        $(self.el).find("#avgConnectionsSelection").trigger("change");

        return this;
    }
});
