var HistogramActivitiesChart = Backbone.View.extend({
    data : null,
    chart : null,
    initialize: function () {
        this.format = this.options.format;
        this.period = this.options.period;
        this.url = this.options.url;
        this.enableTooltip = this.options.enableTooltip;
    },
    resize: function() {
        this.chart.update()
    },
    render: function () {
        var self = this;
        self.getValues(function() {

            nv.addGraph(function () {

                //TODO BrowserSupport.addMessage($("#userNbAnnotationsChart"),BrowserSupport.CHARTS);

                if(!self.format) self.format = 'f';

                if(self.enableTooltip && self.period === "week") {
                    self.enableTooltip = true;
                } else {
                    // tooltip not implemented for other than week
                    self.enableTooltip = false;
                }

                self.chart = nv.models.discreteBarChart()
                        .x(function (d) {
                            return d.label;
                        })    //Specify the data accessors.
                        .y(function (d) {
                            return d.value;
                        })
                        .staggerLabels(true)    //Too many bars and not enough room? Try staggering labels.
                        .valueFormat(d3.format(self.format))
                        .tooltips(self.enableTooltip)        //Don't show tooltips
                        .tooltipContent(function(key) {
                            var now = new Date();
                            now.setHours(0,0,0);
                            var d = new Date(now.getFullYear(), 0, 1);
                            // set to first sunday of the year
                            d.setDate(d.getDate()+(7-d.getDay())%7);
                            var result;
                            if(now.getWeekNumber() >= key.data.label-1) {
                                d.setDate(d.getDate()+(7*(key.data.label-1)));
                            } else {
                                d.setDate(d.getDate()-(7*(53-key.data.label)));
                            }
                            result = '<p>From '+(d.getMonth()+1)+"/"+d.getDate()+"/"+d.getFullYear()+' to ';
                            d.setDate(d.getDate()+6);
                            result += (d.getMonth()+1)+"/"+d.getDate()+"/"+d.getFullYear()+'</p>';
                            return result;
                        })
                        .showValues(true)       //...instead, show the bar value right on top of each bar.
                    ;

                d3.select(self.el)
                    .append("div")
                    .append("svg")
                    .datum(parseData())
                    .call(self.chart);


                nv.utils.windowResize(self.chart.update);

                return self.chart;
            });
            //Each bar represents a single discrete quantity.
            function parseData() {

                var result = {};
                result.key = "Evolution of Cytomine connections";
                result.values = [];

                var labels = [];
                var getPeriod;
                if(self.period === "week") {
                    for(var i=1;i<=52;i++) labels.push(""+i);
                    getPeriod = function(date){
                        return date.getWeekNumber();
                    };
                } else if(self.period === "day"){
                    labels = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
                    getPeriod = function(date){
                        return date.getDay();
                    };
                } else if(self.period === "hour") {
                    for(var i=0;i<24;i++) labels.push(""+i);
                    getPeriod = function(date){
                        return date.getHours();
                    };
                }

                for (var i = 0; i < labels.length; i++) {
                    result.values.push({label: labels[i], value: 0});
                }


                for (var i = 0; i < self.data.length; i++) {
                    var date = new Date(parseInt(self.data[i].time));
                    result.values[getPeriod(date)].value = self.data[i].frequency;
                }

                var pivot = getPeriod(new Date())+1;
                if(pivot>0) {
                    var toRotate = result.values.slice(pivot);
                    result.values = result.values.slice(0,pivot);
                    for(var i = toRotate.length-1; i>=0 ;i--) {
                        result.values.unshift(toRotate[i]);
                    }
                }
                return [result];
            }
        });
    },
    getValues : function(callback) {
        var self = this;

        $.get(self.url, function(data){
            self.data = data.collection;
            self.data.sort(function(a, b){
                return a.time-b.time;
            });
            callback();
        });
    }
});