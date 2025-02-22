package cytomine.core.processing

/*
 * Copyright (c) 2009-2018. Authors: see NOTICE file.
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
import cytomine.core.command.AddCommand
import cytomine.core.command.Command
import cytomine.core.command.DeleteCommand
import cytomine.core.command.EditCommand
import cytomine.core.command.Transaction
import cytomine.core.middleware.AmqpQueue
import cytomine.core.security.SecUser
import cytomine.core.utils.ModelService
import cytomine.core.utils.Task
import groovy.json.JsonBuilder

class SoftwareUserRepositoryService extends ModelService {

    static transactional = true

    def transactionService
    def securityACLService
    def amqpQueueService

    @Override
    def currentDomain() {
        return SoftwareUserRepository
    }

    SoftwareUserRepository get(def id) {
        securityACLService.checkGuest(cytomineService.currentUser)
        return SoftwareUserRepository.get(id)
    }

    SoftwareUserRepository read(def id) {
        securityACLService.checkGuest(cytomineService.currentUser)
        return SoftwareUserRepository.read(id)
    }

    def list() {
        securityACLService.checkGuest(cytomineService.currentUser)
        return SoftwareUserRepository.list()
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data
     * @return Response structure (created domain data,..)
     */
    def add(def json) throws CytomineException {
        SecUser currentUser = cytomineService.getCurrentUser()
        securityACLService.checkAdmin(currentUser)
        return executeCommand(new AddCommand(user: currentUser), null, json)
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return Response structure (new domain data, old domain data..)
     */
    def update(SoftwareUserRepository domain, def jsonNewData) {
        SecUser currentUser = cytomineService.getCurrentUser()
        securityACLService.checkAdmin(currentUser)
        return executeCommand(new EditCommand(user: currentUser), domain, jsonNewData)
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(SoftwareUserRepository domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        SecUser currentUser = cytomineService.getCurrentUser()
        securityACLService.checkAdmin(currentUser)
        Command c = new DeleteCommand(user: currentUser, transaction: transaction)
        return executeCommand(c, domain, null)
    }

//    def deleteDependentSoftware(SoftwareUserRepository domain, Transaction transaction, Task task = null) {
//
//
//        log.info("delteDependantSoftware ${Software.findAllBySoftwareRepository(domain).size()}")
//        Software.findAllBySoftwareRepository(domain).each {
//            softwareService.delete(it, transaction, null, false)
//        }
//    }

    @Override
    def getStringParamsI18n(def domain) {
        return [domain.username, domain.dockerUsername, domain.prefix]
    }

    @Override
    def afterAdd(Object domain, Object response) {
        SoftwareUserRepository softwareUserRepository = domain as SoftwareUserRepository

        def message = [requestType: "addSoftwareUserRepository",
                       id: softwareUserRepository.id,
                       provider: softwareUserRepository.provider,
                       username: softwareUserRepository.username,
                       dockerUsername: softwareUserRepository.dockerUsername,
                       prefix: softwareUserRepository.prefix]

        JsonBuilder jsonBuilder = new JsonBuilder()
        jsonBuilder(message)

        amqpQueueService.publishMessage(AmqpQueue.findByName("queueCommunication"), jsonBuilder.toString())
    }

    def refreshRepositories() {
        def message = [requestType: "refreshRepositories"]

        JsonBuilder jsonBuilder = new JsonBuilder()
        jsonBuilder(message)

        amqpQueueService.publishMessage(AmqpQueue.findByName("queueCommunication"), jsonBuilder.toString())
    }

}
