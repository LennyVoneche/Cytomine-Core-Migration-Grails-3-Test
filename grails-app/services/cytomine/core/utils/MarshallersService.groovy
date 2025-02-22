package cytomine.core.utils

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

import cytomine.core.image.NestedImageInstance
import cytomine.core.processing.JobTemplate
import cytomine.core.security.User
import cytomine.core.security.UserJob
import cytomine.core.social.LastUserPosition
import cytomine.core.social.PersistentUserPosition
import grails.converters.JSON

/**
 * Service to manage marshaller
 * Marshaller provide json/xml/... response structure for web request
 */
class MarshallersService {

    def grailsApplication
    static transactional = false

    /**
     * Init marshaller for all cytomine domain
     */
    def initMarshallers() {
        log.info "initMarshallers method"
        JSON.registerObjectMarshaller(Date) {
            return it?.time?.toString()
        }
        grailsApplication.getDomainClasses().each { domain ->
            domain.metaClass.methods.each { method ->
                if (method.name.equals("getDataFromDomain")) {
                    def domainFullName = domain.packageName + "." + domain.name
                    log.debug "Init Marshaller for domain class : " + domainFullName
                    def domainInstance = grailsApplication.getDomainClass(domainFullName).newInstance()
                    log.debug("Register custom JSON renderer for " + this.class)
                    JSON.registerObjectMarshaller(domain.clazz) { it ->
                        return domainInstance.getDataFromDomain(it)
                    }
                }
            }
        }
        //if ImageInstance.registerMarshaller is call after NestedImageInstance..registerMarshaller, it override it
        JSON.registerObjectMarshaller(NestedImageInstance) { it ->
            return NestedImageInstance.getDataFromDomain(it)
        }
        JSON.registerObjectMarshaller(User) { it ->
            return User.getDataFromDomain(it)
        }
        JSON.registerObjectMarshaller(UserJob) { it ->
            return UserJob.getDataFromDomain(it)
        }
        JSON.registerObjectMarshaller(JobTemplate) { it ->
            return JobTemplate.getDataFromDomain(it)
        }
        JSON.registerObjectMarshaller(PersistentUserPosition) { it ->
            return PersistentUserPosition.getDataFromDomain(it)
        }
        JSON.registerObjectMarshaller(LastUserPosition) { it ->
            return LastUserPosition.getDataFromDomain(it)
        }

    }
}
