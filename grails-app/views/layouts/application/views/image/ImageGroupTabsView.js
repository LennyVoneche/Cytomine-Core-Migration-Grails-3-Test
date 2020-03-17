/**
 * Created by laurent
 * Date : 02.03.17.
 */
/*
 * Copyright (c) 2009-2016. Authors: see NOTICE file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var ImageGroupTabsView = Backbone.View.extend({
    tagName: "div",
    groups: null,
    idProject: null,
    project : null,
    initialize: function (options) {
        this.idProject = options.idProject;
        this.project = options.project;
    },

    refresh: function () {
        this.doLayout();
        return this;
    },

    render : function() {
        var self = this;
        this.doLayout();
        $("#groupAdd"+self.idProject).click(function() {
            new AddImageGroupToProjectDialog({el: "#dialogs", model: self.project, backView: self}).render();
        });
        $("#groupRefresh"+self.idProject).click(function() {
            self.refresh();
        });

        return this;
    },

    doLayout: function () {
        var self = this;
        self.groups = [];
        var isAdmin = window.app.status.currentProjectModel.isAdmin(window.app.models.projectAdmin);
        var table = $(this.el).find("#imageGroupProjectTable" + self.idProject);
        var body = $(this.el).find("#imageGroupProjectArray" + self.idProject);
        var columns = [
            { className: 'center', data: "id", targets: [0]},
            { data: "name", defaultContent: "", searchable: true, orderable: true, render : function( data, type, row ) {
                self.groups.push(row);
                return data;
            }, targets: [1]},
            { data: "channel", defaultContent: "", render : function( data, type, row ) {
                return'<div id="channel-'+row["id"]+'"></div>';
            }, targets: [2]},
            { data: "zstack", defaultContent: "", render : function( data, type, row ) {
                return'<div id="zstack-'+row["id"] + '"></div>';
            }, targets: [3]},
            { data: "slice", defaultContent: "", render : function( data, type, row ) {
                return'<div id="slice-'+row["id"] + '"></div>';
            }, targets: [4]},
            { data: "time", defaultContent: "", render : function( data, type, row ) {
                return'<div id="time-'+row["id"] + '"></div>';
            }, targets: [5]},
            { data: "hdf5", defaultContent: "", orderable: true, render : function ( data, type, row ) {
                var grouphdf5 = new ImageGroupHDF5Model({group: row["id"], id: 666});
                var toRet = '<div id="con-'+row["id"] + '"></div>';
                grouphdf5.fetch({
                    success: function (data) {
                        toRet =  "" + data.get("filenames");
                        $(self.el).find("#con-"+o.aData.id).append(toRet);

                    },
                    error: function () {
                       var tt =  '<div id="convert-allow-<%= id %>"></div>';
                        toRet = _.template(tt, row);
                        $(self.el).find("#con-"+row["id"]).append(toRet);
                    }
                });
                return toRet;
            }, targets: [6]},
            { data: "add", defaultContent: "", orderable: true, render : function ( data, type, row ) {
                var html =    ' <button class="btn btn-info btn-xs" id="add-button-<%=  id  %>">Add Images</button>';
                return _.template(html, row);
            }, targets: [7]},
            { data: "delete", sDefaultContent: "", orderable: true, render : function ( data, type, row ) {
                var html =    ' <button class="btn btn-info btn-xs" id="delete-button-<%=  id  %>">Delete</button>';
                return _.template(html, row);
            }, targets: [8]},
            { searchable: false, orderable: false, targets: "_all" }
        ];
        self.imagesdDataTables = table.dataTable({
            destroy: true,
            processing: true,
            serverSide: true,
            ajax: {
                url: new ImageGroupCollection({project: this.idProject}).url(),
                data: {
                    "datatables": "true"
                }
            },
            drawCallback: function() {
                _.each(self.groups, function(aData) {
                    var imageGroup = new ImageGroupModel({});
                    imageGroup.set(aData);
                    $(self.el).find("#delete-button-"+aData.id).click(function () {
                        window.app.controllers.imagegroup.deleteGroup(aData.id, self.refresh);
                    });
                    $(self.el).find("#add-button-"+aData.id).click(function () {
                        new AddImageSequenceDialog({el: "#dialogs", model: imageGroup, backView: self}).render();
                    });
                    var cb = function (){
                        $(self.el).find("#channel-"+aData.id).append(imageGroup.channelPretty.toString());
                        $(self.el).find("#zstack-"+aData.id).append(imageGroup.zstack.toString());
                        $(self.el).find("#slice-"+aData.id).append(imageGroup.slice.toString());
                        $(self.el).find("#time-"+aData.id).append(imageGroup.time.toString());

                        if(imageGroup.channel.length != 0){
                            var link = ' <a href="#imagegroup/convert-<%= id %>">Convert</a>';
                            link = _.template(link, aData);
                            $(self.el).find("#convert-allow-"+aData.id).append(link)
                        }

                    };

                    imageGroup.feed(cb);

                });
                self.groups = [];
            },
            columnDefs : columns,
            order: [[ 0, "desc" ]]

        });

    }


});
