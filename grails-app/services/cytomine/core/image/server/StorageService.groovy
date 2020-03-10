package cytomine.core.image.server

import cytomine.core.command.*

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

import cytomine.core.security.SecUser
import cytomine.core.utils.ModelService
import cytomine.core.utils.Task
import org.springframework.security.acls.domain.BasePermission
import grails.util.Metadata
class StorageService extends ModelService {

    def cytomineService
    def transactionService
    def permissionService
    def securityACLService
    def springSecurityService

    static transactional = true

    def currentDomain() {
        return Storage
    }

    def list() {
        def list = securityACLService.getStorageList(cytomineService.currentUser)
        list.sort{it.name}
    }

    def read(def id) {
        def storage =  Storage.read((Long) id)
        if(storage) {
            securityACLService.check(storage, BasePermission.READ)
        }
        storage
    }

    /**
     * Add the new domain with JSON data
     * @param json New domain data)
     * @return Response structure (created domain data,..)
     */
    def add(def json) {
        SecUser currentUser = cytomineService.getCurrentUser()
        securityACLService.checkUser(currentUser)
        Command c = new AddCommand(user: currentUser)
        executeCommand(c,null,json)
    }

    /**
     * Update this domain with new data from json
     * @param domain Domain to update
     * @param jsonNewData New domain datas
     * @return  Response structure (new domain data, old domain data..)
     */
    def update(Storage storage, def jsonNewData) {
        securityACLService.check(storage, BasePermission.WRITE)
        SecUser currentUser = cytomineService.getCurrentUser()
        Command c = new EditCommand(user: currentUser)
        executeCommand(c,storage,jsonNewData)
    }

    /**
     * Delete domain in argument
     * @param json JSON that was passed in request parameter
     * @param security Security service object (user for right check)
     * @return Response structure (created domain data,..)
     */
    def delete(Storage storage, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        securityACLService.check(storage.container(), BasePermission.WRITE)
        SecUser currentUser = cytomineService.getCurrentUser()
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,storage,null)
    }

    def afterAdd(Storage domain, def response) {
        log.info("Add permission on " + domain + " to " + springSecurityService.authentication.name)
        if(!domain.hasACLPermission(BasePermission.READ)) {
            log.info("force to put it in list")
            permissionService.addPermission(domain, cytomineService.currentUser.username, BasePermission.READ)
        }
        if(!domain.hasACLPermission(BasePermission.ADMINISTRATION)) {
            log.info("force to put it in list")
            permissionService.addPermission(domain, cytomineService.currentUser.username, BasePermission.ADMINISTRATION)
        }
        for (imageServer in ImageServer.findAll()) {
            imageServer.save(failOnError: true)
            ImageServerStorage imageServerStorage = new ImageServerStorage(imageServer : imageServer, storage : domain)
            imageServerStorage.save(flush : true)
        }
    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.name]
    }

    def imageServerStorageService
    void deleteDependentImageServerStorage(Storage storage, Transaction transaction, Task task = null){
        ImageServerStorage.findAllByStorage(storage).each {
            imageServerStorageService.delete(it,transaction,task)
        }
    }
    def storageAbstractImageService
    void  deleteDependentStorageAbstractImage(Storage storage, Transaction transaction, Task task = null) {
        StorageAbstractImage.findAllByStorage(storage).each {
            storageAbstractImageService.delete(it, transaction, task)
        }
    }


    def initUserStorage(SecUser user) {  //:to do => use command instead of domains
//        Migration
//        String storage_base_path = grailsApplication.config.storage_path
        String storage_base_path = Metadata.current.'cytomine.storage_path'.toString()
        print getClass().getName() + ' initUserStorage : ' + '001' + '\n'
        print getClass().getName() + ' initUserStorage : ' + storage_base_path + ' !\n'

        String remotePath;
        if(storage_base_path.charAt(storage_base_path.length()-1) == File.separator) {
            remotePath = [storage_base_path, user.id.toString()].join("")
        } else {
            remotePath = [storage_base_path, user.id.toString()].join(File.separator)
        }

        log.info ("create storage for $user.username")
        Storage storage = new Storage(
                name: "$user.username storage",
                basePath: remotePath,
                user: user
        )

        if (storage.validate()) {
            storage.save()
            permissionService.addPermission(storage,user.username, BasePermission.ADMINISTRATION)

            for (imageServer in ImageServer.findAll()) {
                imageServer.save(failOnError: true)
                ImageServerStorage imageServerStorage = new ImageServerStorage(imageServer : imageServer, storage : storage)
                imageServerStorage.save(flush : true)
            }
        } else {
            storage.errors.each {
                log.error it
            }
        }

    }

}
