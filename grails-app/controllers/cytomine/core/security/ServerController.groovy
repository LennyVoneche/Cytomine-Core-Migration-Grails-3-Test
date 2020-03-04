package cytomine.core.security

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

import cytomine.core.project.Project
import cytomine.core.social.LastConnection
import cytomine.core.social.PersistentConnection
import grails.converters.JSON
import grails.converters.XML
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Metadata
import org.springframework.dao.NonTransientDataAccessException
import org.springframework.web.context.request.RequestContextHolder

class ServerController {

    def imageRetrievalService

    def springSecurityService
//    def grailsApplication
    def dataSource

    @Secured(['IS_AUTHENTICATED_REMEMBERED'])
    def ping () {
        def jsonContent = request.JSON
        def data = [:]
        data.alive = true
        data.authenticated = springSecurityService.isLoggedIn()
        data.version = grailsApplication.metadata['app.version']
//        data.serverURL = grailsApplication.config.grails.serverURL
//        data.serverID = grailsApplication.config.grails.serverID
        data.serverURL = Metadata.current.'grails.serverURL'.toString()
        data.serverID = Metadata.current.'grails.serverID'.toString()
        if (data.authenticated)  {
            data.user = springSecurityService.currentUser.id

            if(!springSecurityService.currentUser.enabled){
                log.info "Disabled user. Invalidation of its sessions"
                session.invalidate()
            }

            def idProject = null
            def idUser = data.user
            if(!jsonContent.project.toString().equals("null")) {
                idProject = Long.parseLong(jsonContent.project+"")
            }
            addLastConnection(idUser,idProject)
        }
        withFormat {
            json { render data as JSON }
            xml { render data as XML}
        }
    }

    def status() {
        def data = [:]
        data.alive = true
        data.version = grailsApplication.metadata['app.version']
//        data.serverURL = grailsApplication.config.grails.serverURL
        data.serverURL = Metadata.current.'grails.serverURL'.toString()
        withFormat {
            json { render data as JSON }
            xml { render data as XML}
        }
    }

    def addLastConnection(def idUser, def idProject) {
        try {
            LastConnection connection = new LastConnection()
            connection.user = SecUser.read(idUser)
            connection.date = new Date()
            connection.project = Project.read(idProject)
            connection.insert(flush:true) //don't use save (stateless collection)

            PersistentConnection connectionPersist = new PersistentConnection()
            connectionPersist.user = SecUser.read(idUser)
            connectionPersist.project = Project.read(idProject)
            connectionPersist.session = RequestContextHolder.currentRequestAttributes().getSessionId()
            connectionPersist.insert(flush:true) //don't use save (stateless collection)
        } catch (NonTransientDataAccessException e) {
            log.error e.message
        }
    }

    @Secured(['ROLE_USER','ROLE_ADMIN','ROLE_SUPER_ADMIN'])
    def missing() {
        return imageRetrievalService.indexMissingAnnotation()
    }

}
