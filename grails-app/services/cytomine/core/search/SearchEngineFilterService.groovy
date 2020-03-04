package cytomine.core.search

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

import cytomine.core.Exception.CytomineException
import cytomine.core.Exception.ForbiddenException
import cytomine.core.Exception.WrongArgumentException
import cytomine.core.command.AddCommand
import cytomine.core.command.Command
import cytomine.core.command.DeleteCommand
import cytomine.core.command.Transaction
import cytomine.core.security.SecUser
import cytomine.core.security.User
import cytomine.core.utils.JSONUtils
import cytomine.core.utils.ModelService
import cytomine.core.utils.Task
import grails.converters.JSON
import grails.transaction.Transactional

@Transactional
class SearchEngineFilterService extends ModelService {

    static transactional = true
    boolean saveOnUndoRedoStack = true

    def springSecurityService
    def transactionService
    def securityACLService
    def projectService
    def secUserService

    def currentDomain() {
        return SearchEngineFilter
    }

    SearchEngineFilter read(def id) {
        def filter = SearchEngineFilter.read(id)
        if (filter) {
            securityACLService.checkIsSameUser(filter.getUser(), cytomineService.getCurrentUser())
        }
        filter
    }

    /**
     * Get all filters of the current user
     * @return SearchEngineFilter list
     */
    def list() {
        SecUser currentUser = cytomineService.getCurrentUser()
        try {
            securityACLService.checkAdmin(currentUser)
            return SearchEngineFilter.list()
        } catch (ForbiddenException e) {
            securityACLService.checkUser(currentUser)
            return SearchEngineFilter.findAllByUser(currentUser)
        }
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        securityACLService.checkUser(currentUser)
        json.user = currentUser.id
        checkJsonConsistency(json)
        return executeCommand(new AddCommand(user: currentUser), null,json)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(SearchEngineFilter domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        securityACLService.checkIsSameUser(domain.getUser(), cytomineService.getCurrentUser())
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null, task)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.name]
    }

    /**
     * Check if the params json are ok
     * I.e. if selected projects exists and if some words are filled for the search
     * @param json SearchEngineFilter json
     */
    private void checkJsonConsistency(def json) {

        def filters
        if(json.filters instanceof String) {
            filters = JSON.parse(json.filters)
        } else {
            filters = json.filters
        }

        if (!JSONUtils.getJSONList(filters.projects).equals([])) {
            def projects = projectService.list(User.findById(json.user))
            for (def projectId in JSONUtils.getJSONList(filters.projects)) {
                def project = projects.find {
                    it.id == projectId
                };
                if (project == null) {
                    throw new WrongArgumentException("A search filter cannot have non-existing projects")
                }
            }
        }

        if (JSONUtils.getJSONList(filters.words).equals([])) {
            throw new WrongArgumentException("A search filter cannot search with no words")
        }

        if (json.name == null || json.name.equals("")) {
            throw new WrongArgumentException("A search filter cannot have a blanck name")
        }
    }

}
