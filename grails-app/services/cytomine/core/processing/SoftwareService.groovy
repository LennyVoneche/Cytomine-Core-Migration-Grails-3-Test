package cytomine.core.processing

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
import cytomine.core.command.*
import cytomine.core.middleware.AmqpQueue
import cytomine.core.middleware.MessageBrokerServer
import cytomine.core.project.Project
import cytomine.core.security.SecUser
import cytomine.core.utils.ModelService
import cytomine.core.utils.Task
import groovy.json.JsonBuilder
import org.springframework.security.acls.domain.BasePermission

import static org.springframework.security.acls.domain.BasePermission.*

class SoftwareService extends ModelService {

    static transactional = true

    boolean saveOnUndoRedoStack = false

    def cytomineService
    def transactionService
    def aclUtilService
    def softwareParameterService
    def jobService
    def softwareProjectService
    def securityACLService
    def amqpQueueService

    def currentDomain() {
        Software
    }

    Software read(def id) {
        securityACLService.checkGuest(cytomineService.currentUser)
        Software.read(id)
    }

    def readMany(def ids) {
        securityACLService.checkGuest(cytomineService.currentUser)
        Software.findAllByIdInList(ids)
    }

    def list(Boolean executableOnly = false, def sort = 'id', def order_ = 'desc') {
        securityACLService.checkGuest(cytomineService.currentUser)
        def results
        if (sort == 'fullName') {
            def criteria = Software.createCriteria()
            results = criteria.list {
                and  {
                    order('name', order_)
                    order('softwareVersion', order_)
                }
            }
        }
        else {
            results = Software.list([sort: sort, order: order_])
        }

        if (executableOnly) {
            return results.findAll {
                it.executable()
            }
        }
        return results
    }

    def list(Project project) {
        securityACLService.check(project.container(), BasePermission.READ)
        SoftwareProject.findAllByProject(project).collect {it.software}
    }

    def list(SoftwareUserRepository softwareUserRepository) {
        securityACLService.checkGuest(cytomineService.currentUser)
        Software.findAllBySoftwareUserRepositoryAndDeprecated(softwareUserRepository, false)
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        securityACLService.checkGuest(currentUser)
        json.user = currentUser.id
        return executeCommand(new AddCommand(user: currentUser),null,json)
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(Software software, def jsonNewData) {
        securityACLService.check(software.container(), BasePermission.WRITE)
        SecUser currentUser = cytomineService.getCurrentUser()
        return executeCommand(new EditCommand(user: currentUser),software, jsonNewData)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(Software domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        log.info "delete software"
        SecUser currentUser = cytomineService.getCurrentUser()
        securityACLService.check(domain.container(), BasePermission.DELETE)
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }


    def afterAdd(def domain, def response) {
        aclUtilService.addPermission(domain, cytomineService.currentUser.username, BasePermission.ADMINISTRATION)

        // Add this software in all projects that have the previous version
        if (domain.softwareVersion) {
            List<Project> projects = Project.executeQuery("select distinct p from SoftwareProject as sp " +
                    "inner join sp.project as p " +
                    "inner join sp.software as s " +
                    "where s.name = ? and s.softwareVersion != ?", [domain.name, domain.softwareVersion])
            projects.each {
                SoftwareProject sp = new SoftwareProject(software: domain, project: it)
                sp.save(failOnError: true)
            }
        }


    }

    def afterDelete(def domain, def response) {

    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.name]
    }

    def deleteDependentSoftwareParameter(Software software, Transaction transaction, Task task = null) {
        log.info "deleteDependentSoftwareParameter ${SoftwareParameter.findAllBySoftware(software).size()}"
        SoftwareParameter.findAllBySoftware(software).each {
            softwareParameterService.delete(it,transaction,null, false)
        }
    }

    def deleteDependentJob(Software software, Transaction transaction, Task task = null) {
        Job.findAllBySoftware(software).each {
            jobService.delete(it,transaction,null, false)
        }
    }

    def deleteDependentSoftwareProject(Software software, Transaction transaction, Task task = null) {
        SoftwareProject.findAllBySoftware(software).each {
            softwareProjectService.delete(it,transaction,null, false)
        }
    }
}
