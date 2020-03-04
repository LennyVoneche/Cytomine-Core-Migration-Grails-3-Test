var LastConnexionsGraphsView = GraphView.extend({
    graphs : [],
    initialize: function (options) {
        this.constructor.__super__.initialize.apply(this,[options]);
        var url;
        if(window.app.isUndefined(options.project)){
            url = "api/connectionFrequency.json?period=";
        } else {
            url = "api/project/"+options.project+"/connectionFrequency.json?period=";
        }

        var lastDay = new Date();
        var firstDay = new Date(lastDay.getTime());

        this.graphs = [];
        firstDay.setDate(lastDay.getDate()-1);
        firstDay.setHours(lastDay.getHours()+1,0,0);
        this.graphs.push({name : "Day", period : "hour", url: url+"hour&afterThan="+firstDay.getTime(), panel : null});

        firstDay = new Date(lastDay.getTime());
        firstDay.setDate(lastDay.getDate()-7);
        firstDay.setHours(0,0,0);
        this.graphs.push({name : "Week", period : "day", url: url+"day&afterThan="+firstDay.getTime(), panel : null});

        firstDay = new Date(lastDay.getTime());
        firstDay.setYear(lastDay.getFullYear()-1);
        firstDay.setMonth(lastDay.getMonth()+1);
        firstDay.setDate(1);
        firstDay.setHours(0,0,0);
        this.graphs.push({name : "Year", period : "week", url: url+"week&afterThan="+firstDay.getTime(), panel : null});
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

        var tpl = 'Last <select id="lastConnectionsSelection">';

        _.each(self.graphs, function(it){
            tpl+= '<option>'+it.name+'</option>';
        });

        tpl += '</select>';

        _.each(self.graphs, function(it){
            tpl+= '<div class="col-md-offset-1 graph-last" id="LastConnectionsBy'+it.name+'" style="text-align: center;"></div>';
        });

        $(self.el).append(tpl);

        $(self.el).find("#lastConnectionsSelection").on("change", function(e){

            $(self.el).find(".graph-last").hide();

            var chosen = $(this).val();
            chosen = _.find(self.graphs, function(it) {
                return it.name === chosen;
            });

            var panel = $(self.el).find("#LastConnectionsBy"+chosen.name);
            // create objet if not null
            if(chosen.panel === null){
                chosen.panel = new HistogramActivitiesChart({
                    el: panel,
                    period : chosen.period,
                    url : chosen.url,
                    enableTooltip : true
                });
                chosen.panel.render();
            }
            panel.show();
        });

        $(self.el).find("#lastConnectionsSelection").trigger("change");

        return this;
    }
});
