package cytomine.core.utils

import cytomine.core.command.Command

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

import cytomine.core.command.DeleteCommand
import cytomine.core.command.Transaction
import cytomine.core.security.SecUser
import org.springframework.security.acls.domain.BasePermission

class AttachedFileService extends ModelService {

    static transactional = true
    def springSecurityService
    def transactionService
    def commandService
    def cytomineService
    def securityACLService

    def currentDomain() {
        return AttachedFile
    }

    /**
     * List all description, Only for admin
     */
    def list() {
        securityACLService.checkAdmin(cytomineService.currentUser)
        return AttachedFile.list()
    }

    def list(Long domainIdent,String domainClassName) {
        if(domainClassName.contains("AbstractImage")) {
//            securityACLService.checkAtLeastOne(domainIdent,domainClassName,"containers",READ)
        } else {
            securityACLService.check(domainIdent,domainClassName,"container", BasePermission.READ)
        }
        return AttachedFile.findAllByDomainIdentAndDomainClassName(domainIdent,domainClassName)
    }


    def read(def id) {
        AttachedFile file = AttachedFile.read(id)
        if(file) {
            if(file.domainClassName.contains("AbstractImage")) {
//                securityACLService.checkAtLeastOne(file.domainIdent, file.domainClassName, "containers", READ)
            } else if (file.domainClassName.equals("cytomine.core.processing.Software") || file.domainClassName.equals("cytomine.core.processing.SoftwareParameter")) {
                securityACLService.checkGuest(cytomineService.currentUser)
            }
            else {
                securityACLService.check(file.domainIdent,file.domainClassName,"container", BasePermission.READ)
            }
        }
        file
    }

    def add(String filename,byte[] data,Long domainIdent,String domainClassName) {
        //securityACLService.checkAtLeastOne(domainIdent,domainClassName,"containers",READ)
        if(domainClassName.contains("AbstractImage")) {
//            securityACLService.checkAtLeastOne(domainIdent, domainClassName, "containers", READ)
        } else {
            securityACLService.check(domainIdent,domainClassName,"container", BasePermission.READ)
        }
        AttachedFile file = new AttachedFile()
        file.domainIdent =  domainIdent
        file.domainClassName = domainClassName
        file.filename = filename
        file.data = data
        saveDomain(file)
        file
    }

    /**
     * Delete this domain
     * @param domain Domain to delete
     * @param transaction Transaction link with this command
     * @param task Task for this command
     * @param printMessage Flag if client will print or not confirm message
     * @return Response structure (code, old domain,..)
     */
    def delete(AttachedFile domain, Transaction transaction = null, Task task = null, boolean printMessage = true) {
        //securityACLService.checkAtLeastOne(domain.domainIdent, domain.domainClassName, "containers", WRITE)
        if(domain.domainClassName.contains("AbstractImage")) {
            securityACLService.checkAtLeastOne(domain.domainIdent, domain.domainClassName, "containers", BasePermission.READ)
        } else {
            securityACLService.check(domain.domainIdent,domain.domainClassName,"container", BasePermission.READ)
        }
        SecUser currentUser = cytomineService.getCurrentUser()
        Command c = new DeleteCommand(user: currentUser,transaction:transaction)
        return executeCommand(c,domain,null)
    }

    def getStringParamsI18n(def domain) {
        return [domain.id, domain.domainClassName]
    }

}
