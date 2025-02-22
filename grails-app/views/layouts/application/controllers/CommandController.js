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

var CommandController = Backbone.Router.extend({
    initialize: function () {
        this.commandInProgess = false;
    },
    undo: function () {
        var self = this;
        if (self.commandInProgess) {
            window.app.view.message("Oh wait :-)", "Operation already in progress", "error");
            return
        }
        self.commandInProgess = true;
        $.post('command/undo.json', {}, function (data) {
            _.each(data, function (undoElem) {
                self.dispatch(undoElem.callback, undoElem.message, "Undo");
                if (undoElem.printMessage) {
                    window.app.view.message("Undo", undoElem.message, "info");
                }
                self.commandInProgess = false;
            });

        }, "json");

    },

    redo: function () {
        var self = this;
        if (self.commandInProgess) {
            window.app.view.message("Oh wait :-)", "Operation already in progress", "error");
            return
        }
        self.commandInProgess = true;
        $.post('command/redo.json', {}, function (data) {
            _.each(data, function (redoElem) {
                self.dispatch(redoElem.callback, redoElem.message, "Redo");
                if (redoElem.printMessage) {
                    window.app.view.message("Redo", redoElem.message, "info");
                }
            });
            self.commandInProgess = false;
        }, "json");

    },

    dispatch: function (callback, message, operation) {

        if (!callback) {
            return;
        } //nothing to do

        /**
         * ANNOTATION
         */
        if (callback.method == "cytomine.core.AddUserAnnotationCommand") {

            var tab = _.detect(window.app.controllers.browse.tabs.tabs, function (object) {

                return object.idImage == callback.imageID;
            });

            if (tab == undefined) {
                return;
            } //tab is closed

            var image = tab.view;
            image.getUserLayer().annotationAdded(callback.annotationID);
            if (window.app.controllers.dashboard.view != null) {
                window.app.controllers.dashboard.view.refreshAnnotationsView();
            }
        } else if (callback.method == "cytomine.core.EditUserAnnotationCommand") {

            var tab = _.detect(window.app.controllers.browse.tabs.tabs, function (object) {
                return object.idImage == callback.imageID;
            });
            if (tab == undefined) {
                return;
            } //tab is closed

            var image = tab.view;
            image.getUserLayer().annotationUpdated(callback.annotationID);
            if (window.app.controllers.dashboard.view != null) {
                window.app.controllers.dashboard.view.refreshAnnotationsView();
            }

        } else if (callback.method == "cytomine.core.DeleteUserAnnotationCommand") {
            console.log("delete annotation");
            var tab = _.detect(window.app.controllers.browse.tabs.tabs, function (object) {
                return object.idImage == callback.imageID;
            });
            if (tab == undefined) {
                return;
            } //tab is closed

            var image = tab.view;

            image.getUserLayer().annotationRemoved(callback.annotationID);
            if (window.app.controllers.dashboard.view != null) {
                window.app.controllers.dashboard.view.refreshAnnotationsView();
            }
            /**
             * ANNOTATION TERM
             */
        } else if (callback.method == "cytomine.core.AddAnnotationTermCommand") {

            var tab = _.detect(window.app.controllers.browse.tabs.tabs, function (object) {
                return object.idImage == callback.imageID;
            });
            if (tab == undefined) {
                return;
            } //tab is closed

            var image = tab.view;
            image.getUserLayer().termAdded(callback.annotationID, callback.termID);
            if (window.app.controllers.dashboard.view != null) {
                window.app.controllers.dashboard.view.refreshAnnotationsView();
            }
        } else if (callback.method == "cytomine.core.DeleteAnnotationTermCommand") {

            var tab = _.detect(window.app.controllers.browse.tabs.tabs, function (object) {
                return object.idImage == callback.imageID;
            });
            if (tab == undefined) {
                return;
            } //tab is closed

            var image = tab.view;
            image.getUserLayer().termRemoved(callback.annotationID, callback.termID);
            if (window.app.controllers.dashboard.view != null) {
                window.app.controllers.dashboard.view.refreshAnnotationsView();
            }
        }

        /**
         * ONTOLOGY
         */
        else if (callback.method == "cytomine.core.AddOntologyCommand") {

            window.app.controllers.ontology.view.refresh(callback.ontologyID);
        } else if (callback.method == "cytomine.core.DeleteOntologyCommand") {

            window.app.controllers.ontology.view.refresh();
        } else if (callback.method == "cytomine.core.EditOntologyCommand") {

            window.app.controllers.ontology.view.refresh(callback.ontologyID);
        }
        /**
         * PROJECT
         */
        else if (callback.method == "cytomine.core.AddProjectCommand") {

            window.app.controllers.project.view.refresh();
        } else if (callback.method == "cytomine.core.DeleteProjectCommand") {

            window.app.controllers.project.view.refresh();
        } else if (callback.method == "cytomine.core.EditProjectCommand") {

            window.app.controllers.project.view.refresh();
        }
        /**
         * TERM
         */
        else if (callback.method == "cytomine.core.AddTermCommand") {

            window.app.controllers.ontology.view.refresh(callback.ontologyID);
        } else if (callback.method == "cytomine.core.DeleteTermCommand") {

            window.app.controllers.ontology.view.refresh(callback.ontologyID);
        } else if (callback.method == "cytomine.core.EditTermCommand") {

            window.app.controllers.ontology.view.refresh(callback.ontologyID);
        }

        else if (callback.method == "cytomine.core.AddImageInstanceCommand") {
            if (window.app.controllers.project.view != null) {
                window.app.controllers.project.view.refresh();
            }
            if (window.app.controllers.dashboard.view != null) {
                window.app.controllers.dashboard.view.refresh();
            }
        } else if (callback.method == "cytomine.core.DeleteImageInstanceCommand") {

            if (window.app.controllers.project.view != null) {
                window.app.controllers.project.view.refresh();
            }
            if (window.app.controllers.dashboard.view != null) {
                window.app.controllers.dashboard.view.refresh();
            }
        } else if (callback.method == "cytomine.core.EditImageInstanceCommand") {
            if (window.app.controllers.project.view != null) {
                window.app.controllers.project.view.refresh();
            }
            if (window.app.controllers.dashboard.view != null) {
                window.app.controllers.dashboard.view.refresh();
            }
        }

    }
});