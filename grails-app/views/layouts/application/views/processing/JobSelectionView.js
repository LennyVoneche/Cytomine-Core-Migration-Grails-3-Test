/*
 * Copyright (c) 2009-2017. Authors: see NOTICE file.
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

var JobSelectionView = Backbone.View.extend({
    width: null,
    software: null,
    project: null,
    jobs: null,
    parent: null,
    availableDate: null,
    table: null,
    disableDateChangeEvent: false,
    currentDate: undefined,
    comparator: false,
    selectedJob: null,
    initialize: function (options) {
        var self = this;
        this.software = options.software;
        this.project = options.project;
        this.parent = options.parent;
        this.jobs = options.jobs;
        this.comparator = options.comparator;
        this.loadDateArray();
        //this.initDataTableSelectFiltering();
    },
    render: function () {
        var self = this;
        require([
            "text!application/templates/processing/JobSelection.tpl.html"
        ],
            function (jobSelectionViewTpl) {
                self.loadResult(jobSelectionViewTpl);
            });
        return this;
    },
    loadResult: function (jobSelectionViewTpl) {
        var self = this;
        var content = _.template(jobSelectionViewTpl, {});

        $(self.el).empty();
        $(self.el).append(content);
        self.printDatatables(self.jobs.models);
        self.printDataPicker(undefined);

        $(self.el).find("#seeAllButton").click(function () {
            self.currentDate = undefined;
            self.disableDateChangeEvent = true;
            $(self.el).find("#datepicker").datepicker('setDate', null);
            self.disableDateChangeEvent = false;
            self.refresh();
        });

        $(self.el).find("#refreshButton").click(function () {
            self.refreshWithDate(self.currentDate);
            $(self.el).find("#datepicker").datepicker('setDate', self.currentDate);
        });
    },
    refresh: function () {
        this.refreshWithDate(undefined)
    },
    refreshWithDate: function (date) {
        var self = this;
        console.log("refreshWithDate")
        new JobCollection({ project: self.project.id, software: self.software.id, light: true}).fetch({
            success: function (collection, response) {
                self.jobs = collection;
                self.loadDateArray();
                self.printDatatables(self.jobs.models, date);
                self.printDataPicker(date);
                $(self.el).find("#datepicker").datepicker('setDate', date);
                self.refreshJobs();

            }
        });
    },
    loadDateArray: function () {
        var self = this;
        self.availableDate = [];
        self.jobs.each(function (job) {
            //fill availableDate array with (yyyy/mm/dd) timestamp (without hour/min/sec)
            var createdDate = new Date();
            createdDate.setTime(job.get('created'));
            createdDate = new Date(createdDate.getFullYear(), createdDate.getMonth(), createdDate.getDate());
            self.availableDate.push(createdDate.getTime());
        });
    },
    printDataPicker: function (date) {
        var self = this;
        $(self.el).find("#datepicker").datepicker({
            beforeShowDay: function (date) {
                if (_.indexOf(self.availableDate, date.getTime()) != -1) {
                    return [true, "", ""];
                } else {
                    return [false, "", "No job was run at this date!"];
                }
            },
            onSelect: function (dateStr) {
                self.refreshJobs();
            }
        });
        if (date != undefined) {
            self.disableDateChangeEvent = true;
            $(self.el).find("#datepicker").datepicker('setDate', date);
            self.disableDateChangeEvent = false;
        }

        $(self.el).find("#onlyDeletedJob").change(function() {
           self.refreshJobs();
        });

    },
    refreshJobs: function () {
        var self = this;

        if (!self.disableDateChangeEvent) {

            var filteredJobs = [];

            self.jobs.each(function(job) {
                filteredJobs.push(job);
            });

            var date = $(self.el).find("#datepicker").datepicker("getDate");
            if (date != null) {
                self.currentDate = date;
                var indx = self.findJobIndiceBetweenDateTime(date.getTime(), date.getTime() + 86400000); //60*60*24*1000 = 86400000ms in a day
                filteredJobs = _.filter(filteredJobs, function(job,index){
                    return  $.inArray(index,indx)!=-1;
                });
            }

            var noDeletedData = $(self.el).find("#onlyDeletedJob").is(':checked');
            if(noDeletedData) {
                filteredJobs = _.filter(filteredJobs, function(job,index){ return !job.get('dataDeleted')});
            }

            self.printDatatables(filteredJobs, date);
        }
    },
    findJobIndiceBetweenDateTime: function (min, max) {
        var self = this;
        var correctDateArray = _.map(self.availableDate, function (date, indx) {
            if (date >= min && date < max) {
                return indx;
            }
            else {
                return -1;
            }
        });
        correctDateArray = _.without(correctDateArray, -1);
        return correctDateArray;
    },
    findJobByIndice: function (indiceArray) {
        var self = this;
        var jobArray = [];
        _.each(indiceArray, function (indx) {
            jobArray.push(self.jobs.at(indx));
        });
        return jobArray;
    },
    printDatatables: function (jobs, date) {
        var self = this;

        var data = [];
        _.each(jobs, function (job) {
            data.push(["<i class='glyphicon glyphicon-plus'></i>", job.id, job.get('number'),
                window.app.convertLongToDate(job.get('created')),
                self.getStateElement(job),
                // self.comparator ?
                //     '<a id="select' + job.id + '">Compare</a>' :
                '<div class="btn-group">' +
                '<button class="btn btn-info btn-xs" id="job-details-'+job.id+'">Details</button> ' +
                '<button class="btn btn-info btn-xs dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">' +
                '<span class="caret"></span><span class="sr-only">Toggle Dropdown</span>' +
                '</button>' +
                '<ul class="dropdown-menu">' +
                ((job.isInQueue() || job.isRunning()) ?'<li><a href="#" id="job-kill-'+job.id+'"><span class="glyphicon glyphicon-ban-circle" aria-hidden="true"></span> Kill job</a></li>': '') +
                '<li>'+(job.get('dataDeleted') ? '<span class="glyphicon glyphicon-trash" aria-hidden="true"></span> All job data are deleted ' : '<a href="#" id="job-delete-data-' + job.id + '"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span> Delete data</a>') + '</li>' +
                '</ul>' +
                '</div>'
            ]);
        });

        //rebuilt table
        var selectRunParamElem = $(self.el).find('#selectJobTable').find('tbody').empty();
        self.table = $(self.el).find('#selectJobTable').DataTable({
            searching: false,
            dom: '<"toolbar">frtip',
            data: data,
            displayLength: 10,
            lengthChange: false,
            destroy: true,
            columnDefs: [
                { width: "10%", targets: [ 0 ] },
                { width: "15%", targets: [ 1 ] },
                { width: "15%", targets: [ 2 ] },
                { width: "25%", targets: [ 3 ] },
                { width: "20%", targets: [ 4 ] },
                { width: "15%", targets: [ 5 ] }
                // { width: "15%", targets: [ 6 ] }
            ]
        });

          _.each(jobs, function (job) {
            $(self.el).find("#job-details-" + job.id).click(function () {
              window.location = '#tabs-algos-' + self.project.id + "-" + self.software.id + "-" + job.id + '';
            });

            $(self.el).find("#job-delete-data-" + job.id).click(function () {
              new JobModel({ id: job.id}).fetch({
                success: function (model, response) {
                  new JobDeleteAllDataView({
                    model: model,
                    project: self.project,
                    container: self
                  }).render();
                }
              });
              return false;
            });

            $(self.el).find("#job-kill-" + job.id).click(function () {
              $.post('/api/job/' + job.id + '/kill.json').done(function() {
                window.app.view.message("Job", "Job killed !", "success");
                self.refresh();
              }).fail(function() {
                window.app.view.message("Job", "Error during job killing !", "error");
              });
              return false;
            });
          });

        self.initSubGridDatatables();
    },
    getStateElement: function (job) {
      if (job.isNotLaunch()) {
        return '<span class="label label-default">Not Launched</span> ';
      }
      else if (job.isInQueue()) {
        return '<span class="label label-info">In queue</span> ';
      }
      else if (job.isRunning()) {
        return '<span class="label label-primary">Running</span> ';
      }
      else if (job.isSuccess()) {
        return '<span class="label label-success">Success</span> ';
      }
      else if (job.isFailed()) {
        return '<span class="label label-danger">Failed</span> ';
      }
      else if (job.isIndeterminate()) {
        return '<span class="label label-default">Indetereminate</span> ';
      }
      else if (job.isWait()) {
        return '<span class="label label-warning">Waiting</span> ';
      }
      else if (job.isPreviewed()) {
        return '<span class="label label-info">Previewed</span> ';
      }
      else if (job.isKilled()) {
        return '<span class="label" style="background: black;">Killed</span> ';
      }
      else {
        return "no supported";
      }
    },
    initSubGridDatatables: function () {
        var self = this;

        $(self.el).find("#selectJobTable tbody td i").on('click', function () {
            var nTr = $(this).parents('tr');
            var row = self.table.row( nTr );
            if (row.child.isShown()) {
                /* This row is already open - close it */
                $(this).removeClass("glyphicon-minus");
                $(this).addClass("glyphicon-plus");
                row.child.hide();
            }
            else {
                /* Open this row */
                $(this).removeClass("glyphicon-plus");
                $(this).addClass("glyphicon-minus");
                row.child(self.seeDetails(nTr)).show();
                var aData =  self.table.row( nTr).data();
                new JobModel({ id: aData[1]}).fetch({
                    success: function (model, response) {
                        var tableParam = $(self.el).find('#selectJobTable').find('table[id=' + aData[1] + ']');
                        _.each(model.get('jobParameters'), function (param) {
                            var value = param.value
                            if (value.length > 50) {
                                value = value.substring(0, 50) + "..."
                            }

                            tableParam.append('<tr><td>' + param.humanName + '</td><td>' + value + '</td><td>' + param.type + '</td></tr>');
                        });
                    }
                });

            }

        });
    },
    /* Formating function for row details */
    seeDetails: function (nTr) {
        var self = this;
        var row = self.table.row( nTr );
        var aData = self.table.row( nTr).data();

        var sOut = '<table cellpadding="5" cellspacing="0" border="0" style="padding-left:50px;" id="' + aData[1] + '">';
        sOut += '</table>';

        return sOut;
    },

    initDataTableSelectFiltering: function () {
        /*
         * Function: fnGetColumnData
         * Purpose:  Return an array of table values from a particular column.
         * Returns:  array string: 1d data array
         * Inputs:   object:oSettings - dataTable settings object. This is always the last argument past to the function
         *           int:iColumn - the id of the column to extract the data from
         *           bool:bUnique - optional - if set to false duplicated values are not filtered out
         *           bool:bFiltered - optional - if set to false all the table data is used (not only the filtered)
         *           bool:bIgnoreEmpty - optional - if set to false empty values are not filtered from the result array
         * Author:   Benedikt Forchhammer <b.forchhammer /AT\ mind2.de>
         */
        $.fn.dataTableExt.oApi.fnGetColumnData = function (oSettings, iColumn, bUnique, bFiltered, bIgnoreEmpty) {
            // check that we have a column id
            if (typeof iColumn == "undefined") {
                return [];
            }

            // by default we only wany unique data
            if (typeof bUnique == "undefined") {
                bUnique = true;
            }

            // by default we do want to only look at filtered data
            if (typeof bFiltered == "undefined") {
                bFiltered = true;
            }

            // by default we do not wany to include empty values
            if (typeof bIgnoreEmpty == "undefined") {
                bIgnoreEmpty = true;
            }

            // list of rows which we're going to loop through
            var aiRows;

            // use only filtered rows
            if (bFiltered == true) {
                aiRows = oSettings.aiDisplay;
            }
            // use all rows
            else {
                aiRows = oSettings.aiDisplayMaster;
            } // all row numbers

            // set up data array
            var asResultData = [];

            for (var i = 0, c = aiRows.length; i < c; i++) {
                iRow = aiRows[i];
                var aData = this.fnGetData(iRow);
                var sValue = aData[iColumn];

                // ignore empty values?
                if (bIgnoreEmpty == true && sValue.length == 0) {
                    continue;
                }

                // ignore unique values?
                else if (bUnique == true && jQuery.inArray(sValue, asResultData) > -1) {
                    continue;
                }

                // else push the value onto the result data array
                else {
                    asResultData.push(sValue);
                }
            }

            return asResultData;
        }
    }

});