var UserActivityModel = Backbone.Model.extend({
    url: function () {
        return 'api/user/' + this.user + '/activities.json';
    },
    initialize: function (options) {
        this.user = options.user;
    }
});

var UserActivitiesCollection = PaginatedCollection.extend({
    url: function () {
        var url;
        if (!window.app.isUndefined(this.project)) {
            url= "api/project/" + this.project;
            if (!window.app.isUndefined(this.last) && this.last) {
                url+= "/lastConnection/"+this.user+".json"
            } else if (!window.app.isUndefined(this.frequency) && this.frequency) {
                url+= "/connectionFrequency/"+this.user+".json"
            } else if (!window.app.isUndefined(this.resumeActivity) && this.resumeActivity) {
                url+= "/resumeActivity/"+this.user+".json"
            } else {
                url+= "/connectionHistory/"+this.user+".json"
            }
        }
        return url;
    },
    initialize: function (options) {
        this.initPaginator(options);
        this.project = options.project;
        this.user = options.user;
        this.last = options.last;
        this.frequency = options.frequency;
        this.resumeActivity = options.resumeActivity;
    }
});
var UserActivityDetailsCollection = PaginatedCollection.extend({
    url: function () {
        return "/api/projectConnection/"+this.activity+".json";
    },
    initialize: function (options) {
        this.initPaginator(options);
        this.activity = options.activity;
    }
});
var UserPositionModel = Backbone.Model.extend({
    url: function () {
        if (this.user == undefined) {
            return 'api/imageinstance/' + this.image + '/position.json';
        } else {
            return 'api/imageinstance/' + this.image + '/position/' + this.user + ".json";
        }
    },
    initialize: function (options) {
        this.image = options.image;
        this.user = options.user;
    }
});

var UserOnlineModel = Backbone.Model.extend({
    url: function () {
        return 'api/imageinstance/' + this.image + '/online.json';
    },
    initialize: function (options) {
        this.image = options.image;
    }
});

var ImageConsultationModel = Backbone.Model.extend({
    url: function () {
        return '/api/imageinstance/'+this.image +'/consultation.json';
    },
    initialize: function (options) {
        this.image = options.image;
        this.mode = options.mode;
    }
});
var ImageConsultationCollection = PaginatedCollection.extend({
    url: function () {
        var url;
        url= "api/imageconsultation/resume.json";
        if (!window.app.isUndefined(this.user)) {
            if (!window.app.isUndefined(this.project)) {
                url+= "?project="+this.project+"&user="+this.user
            } else {
                url+= "?user="+this.user
            }
        }
        return url;
    },
    initialize: function (options) {
        this.initPaginator(options);
        this.project = options.project;
        this.user = options.user;
    }
});