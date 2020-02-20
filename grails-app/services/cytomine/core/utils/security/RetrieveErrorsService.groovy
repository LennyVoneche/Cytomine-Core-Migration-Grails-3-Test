package cytomine.core.utils.security

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

class RetrieveErrorsService {

    def grailsApplication
    def messageSource

    def initMethods() {
        log.info "RetrieveErrorsService method"
        grailsApplication.domainClasses.each {domainClass ->//iterate over the domainClasses
//            if (domainClass.clazz.name.contains("be.cytomine")) {//only add it to the domains in my plugin
            if (domainClass.clazz.name.contains("cytomine.core")) {//only add it to the domains in my plugin

                domainClass.metaClass.retrieveErrors = {
                    def list = delegate?.errors?.allErrors?.collect {messageSource.getMessage(it, null)}
                    return list?.join('\n')
                }
            }
        }
    }
}
