package cytomine.core.utils.security

import cytomine.core.CytomineDomain

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

import cytomine.core.Exception.ForbiddenException
import cytomine.core.Exception.ObjectNotFoundException
import cytomine.core.image.server.Storage
import cytomine.core.ontology.Ontology
import cytomine.core.processing.Software
import cytomine.core.processing.SoftwareProject
import cytomine.core.project.Project
import cytomine.core.security.Group
import cytomine.core.security.SecUser
import cytomine.core.security.UserGroup
import cytomine.core.security.UserJob
import org.springframework.security.acls.domain.BasePermission
import org.springframework.security.acls.model.Permission

class SecurityACLService {

    def cytomineService
    static transactional = false
    def currentRoleServiceProxy

    void check(def id, Class classObj, Permission permission) {
        check(id,classObj.getName(),permission)
    }

    void check(def id, Class classObj, String method, Permission permission) {
        check(id, classObj.getName(), method, permission)
    }

    void checkAtLeastOne(CytomineDomain domain, Permission permission) {
        checkAtLeastOne(domain.id,domain.class.name,"containers",permission)
    }

    void checkAtLeastOne(def id, String className, String method, Permission permission) {
        def simpleObject =  Class.forName(className, false, Thread.currentThread().contextClassLoader).read(id)
        if (simpleObject) {
            def containerObjects = simpleObject."$method"()
            def atLeastOne = containerObjects.find {
                it.checkPermission(permission,currentRoleServiceProxy.isAdminByNow(cytomineService.currentUser))
            }
            if (!atLeastOne) throw new ForbiddenException("You don't have the right to read or modity this resource! ${className} ${id}")

        } else {
            throw new ObjectNotFoundException("ACL error: ${className} with id ${id} was not found! Unable to process auth checking")
        }
    }

    void checkAll(CytomineDomain domain, Permission permission) {
        checkAll(domain.id,domain.class.name,"containers",permission)
    }

    void checkAll(def id, String className, String method, Permission permission) {
        def simpleObject =  Class.forName(className, false, Thread.currentThread().contextClassLoader).read(id)
        if (simpleObject) {
            def containerObjects = simpleObject."$method"()
            def atLeastOne = containerObjects.find {
                !it.checkPermission(permission,currentRoleServiceProxy.isAdminByNow(cytomineService.currentUser))
            }
            if (atLeastOne) throw new ForbiddenException("You don't have the right to read or modity this resource! ${className} ${id}")
        } else {
            throw new ObjectNotFoundException("ACL error: ${className} with id ${id} was not found! Unable to process auth checking")
        }
    }

    void check(def id, String className, String method, Permission permission) {
        log.info "check:" + id + " className=$className method=$method"
        def simpleObject =  Class.forName(className, false, Thread.currentThread().contextClassLoader).read(id)
        if (simpleObject) {
            def containerObject = simpleObject."$method"()
            check(containerObject,permission)
        } else {
            throw new ObjectNotFoundException("ACL error: ${className} with id ${id} was not found! Unable to process auth checking")
        }
    }

    void check(def id, String className, Permission permission) {
        try {
            def domain = Class.forName(className, false, Thread.currentThread().contextClassLoader).read(id)
            if (domain) {
                check(domain,permission)
            } else {
                throw new ObjectNotFoundException("ACL error: ${className} with id ${id} was not found! Unable to process auth checking")
            }
        } catch(IllegalArgumentException ex) {
            throw new ObjectNotFoundException("ACL error: ${className} with id ${id} was not found! Unable to process auth checking")
        }

    }

    void check(CytomineDomain domain, Permission permission) {
        if (domain) {
            if (!domain.container().checkPermission(permission,currentRoleServiceProxy.isAdminByNow(cytomineService.currentUser))) {
                throw new ForbiddenException("You don't have the right to read or modity this resource! ${domain.class.getName()} ${domain.id}")
            }

        } else {
            throw new ObjectNotFoundException("ACL error: domain is null! Unable to process project auth checking")
        }

    }


    void checkisNotReadOnly(def id, Class className) {
        checkisNotReadOnly(id,className.getName())
    }


    void checkisNotReadOnly(def id, String className) {
        try {
            def domain = Class.forName(className, false, Thread.currentThread().contextClassLoader).read(id)
            if (domain) {
                checkisNotReadOnly(domain)
            } else {
                throw new ObjectNotFoundException("ACL error: ${className} with id ${id} was not found! Unable to process auth checking")
            }
        } catch(IllegalArgumentException ex) {
            throw new ObjectNotFoundException("ACL error: ${className} with id ${id} was not found! Unable to process auth checking")
        }

    }


    //check if the container (e.g. Project) is not in readonly. If in readonly, only admins can edit this.
    void checkisNotReadOnly(CytomineDomain domain) {
        if (domain) {
            boolean readOnly = !domain.container().canUpdateContent()
            boolean containerAdmin = domain.container().hasACLPermission(domain.container(), BasePermission.ADMINISTRATION)
            if(readOnly && !containerAdmin) {
                throw new ForbiddenException("The project for this data is in readonly mode! You must be project manager to add, edit or delete this resource in a readonly project.")
            }

        } else {
            throw new ObjectNotFoundException("ACL error: domain is null! Unable to process project auth checking")
        }
    }

    void checkFullOrRestrictedForOwner(def id, Class className, String owner = null) {
        checkFullOrRestrictedForOwner(id,className.getName(), owner)
    }


    void checkFullOrRestrictedForOwner(def id, String className, String owner = null) {
        try {
            def domain = Class.forName(className, false, Thread.currentThread().contextClassLoader).read(id)
            if (domain) {
                checkFullOrRestrictedForOwner(domain, owner ? domain."$owner" : null)
            } else {
                throw new ObjectNotFoundException("ACL error: ${className} with id ${id} was not found! Unable to process auth checking")
            }
        } catch(IllegalArgumentException ex) {
            throw new ObjectNotFoundException("ACL error: ${className} with id ${id} was not found! Unable to process auth checking")
        }

    }
    //check if the container (e.g. Project) has the minimal editing mode or is Admin. If not, exception will be thown
    void checkFullOrRestrictedForOwner(CytomineDomain domain, SecUser owner = null) {
        if (domain) {
            if(domain.container().hasACLPermission(domain.container(), BasePermission.ADMINISTRATION)) return;
            switch (domain.container().mode) {
                case Project.EditingMode.CLASSIC :
                    return;
                case Project.EditingMode.RESTRICTED :
                    if(owner) {
                        if (owner.id!=cytomineService.currentUser.id) {
                            throw new ForbiddenException("You don't have the right to do this. You must be the creator or the container admin")
                        }
                    } else {
                        throw new ForbiddenException("The project for this data is in "+domain.container().mode.name()+" mode! You must be project manager to add, edit or delete this resource.")
                    }
                    break;
                case Project.EditingMode.READ_ONLY :
                    throw new ForbiddenException("The project for this data is in "+domain.container().mode.name()+" mode! You must be project manager to add, edit or delete this resource.")
                default :
                    throw new ObjectNotFoundException("ACL error: project editing mode is unknown! Unable to process project auth checking")

            }
        } else {
            throw new ObjectNotFoundException("ACL error: domain is null! Unable to process project auth checking")
        }
    }

    public List<Storage> getStorageList(SecUser user) {
        //faster method
        if (currentRoleServiceProxy.isAdminByNow(user)) return Storage.list();
        while (user instanceof UserJob) {
            user = ((UserJob) user).user
        }
        return Storage.executeQuery(
                "select distinct storage "+
                        "from AclObjectIdentity as aclObjectId, AclEntry as aclEntry, AclSid as aclSid,  Storage as storage "+
                        "where aclObjectId.objectId = storage.id " +
                        "and aclEntry.aclObjectIdentity = aclObjectId.id "+
                        "and aclEntry.sid = aclSid.id and aclSid.sid like '"+user.username+"'")
    }

    public List<Ontology> getOntologyList(SecUser user) {
        //faster method
        if (currentRoleServiceProxy.isAdminByNow(user)) return Ontology.list()
        else {
            return Ontology.executeQuery(
                    "select distinct ontology "+
                            "from AclObjectIdentity as aclObjectId, AclEntry as aclEntry, AclSid as aclSid, Ontology as ontology "+
                            "where aclObjectId.objectId = ontology.id " +
                            "and aclEntry.aclObjectIdentity = aclObjectId.id "+
                            "and aclEntry.sid = aclSid.id and aclSid.sid like '"+user.username+"'")
        }
    }


    public List<Project> getProjectList(SecUser user) {
        //faster method
        if (currentRoleServiceProxy.isAdminByNow(user)) {
            Project.findAllByDeletedIsNull()
        }
        else {
            return Project.executeQuery(
                    "select distinct project "+
                            "from AclObjectIdentity as aclObjectId, AclEntry as aclEntry, AclSid as aclSid, Project as project "+
                            "where aclObjectId.objectId = project.id " +
                            "and aclEntry.aclObjectIdentity = aclObjectId.id "+
                            "and aclEntry.sid = aclSid.id and aclSid.sid like '"+user.username+"' and project.deleted is null")
        }
    }

    public List<Project> getProjectList(SecUser user, Ontology ontology) {
        //faster method
        if (currentRoleServiceProxy.isAdminByNow(user)) {
            def projects = Project.findAllByOntologyAndDeletedIsNull(ontology)
            return projects
        }
        else {
            return Project.executeQuery(
                    "select distinct project "+
                            "from AclObjectIdentity as aclObjectId, AclEntry as aclEntry, AclSid as aclSid, Project as project "+
                            "where aclObjectId.objectId = project.id " +
                            "and aclEntry.aclObjectIdentity = aclObjectId.id "+
                            (ontology? "and project.ontology.id = ${ontology.id} " : " ") +
                            "and aclEntry.sid = aclSid.id and aclSid.sid like '"+user.username+"' and project.deleted is null")
        }
    }

    public List<Project> getProjectList(SecUser user, Software software) {
        //faster method
        if (currentRoleServiceProxy.isAdminByNow(user)) {
            SoftwareProject.findAllBySoftware(software).collect{it.project}.findAll{!it.checkDeleted()}
        }
        else {
            return Project.executeQuery(
                    "select distinct project "+
                            "from AclObjectIdentity as aclObjectId, AclEntry as aclEntry, AclSid as aclSid, Project as project, SoftwareProject as softwareProject "+
                            "where aclObjectId.objectId = project.id " +
                            "and aclEntry.aclObjectIdentity = aclObjectId.id "+
                            (software? " and project.id = softwareProject.project.id and softwareProject.software.id = ${software.id} " : " ") +
                            "and aclEntry.sid = aclSid.id and aclSid.sid like '"+user.username+"' and project.deleted is null")
        }
    }



    public def checkAdmin(SecUser user) {
        if (!currentRoleServiceProxy.isAdminByNow(user)) {
            throw new ForbiddenException("You don't have the right to read this resource! You must be admin!")
        }
    }

    public def checkGuest(SecUser user) {
        if (!currentRoleServiceProxy.isAdminByNow(user) && !currentRoleServiceProxy.isUserByNow(user) && !currentRoleServiceProxy.isGuestByNow(user)) {
            throw new ForbiddenException("You don't have the right to read this resource! You must be user!")
        }
    }

    public def checkUser(SecUser user) {
        if (!currentRoleServiceProxy.isAdminByNow(user) && !currentRoleServiceProxy.isUserByNow(user)) {
            throw new ForbiddenException("You don't have the right to read this resource! You must be user!")
        }
    }

    public def checkIsSameUser(SecUser user, SecUser currentUser) {
        boolean sameUser = (user.id == currentUser.id)
        sameUser |= currentRoleServiceProxy.isAdminByNow(currentUser)
        sameUser |= (currentUser instanceof UserJob && user.id==((UserJob)currentUser).user.id)
        if (!sameUser) {
            throw new ForbiddenException("You don't have the right to read this resource! You must be the same user!")
        }
    }

    public def checkIsAdminContainer(CytomineDomain domain, SecUser currentUser) {
        if (domain) {
            if (!domain.container().checkPermission(BasePermission.ADMINISTRATION,currentRoleServiceProxy.isAdminByNow(cytomineService.currentUser))) {
                throw new ForbiddenException("You don't have the right to do this. You must be the creator or the container admin")
            }
        } else {
            throw new ObjectNotFoundException("ACL error: domain is null! Unable to process project auth checking")
        }

    }

    public def checkIsSameUserOrAdminContainer(CytomineDomain domain, SecUser user, SecUser currentUser) {
        boolean isNotSameUser = (!currentRoleServiceProxy.isAdminByNow(currentUser) && (user.id!=currentUser.id))
        if (isNotSameUser) {
            if (domain) {
                if (!domain.container().checkPermission(BasePermission.ADMINISTRATION,currentRoleServiceProxy.isAdminByNow(cytomineService.currentUser))) {
                    throw new ForbiddenException("You don't have the right to do this. You must be the creator or the container admin")
                }
            } else {
                throw new ObjectNotFoundException("ACL error: domain is null! Unable to process project auth checking")
            }
        }

    }

    public def checkIsCreator(CytomineDomain domain, SecUser currentUser) {
        if (!currentRoleServiceProxy.isAdminByNow(currentUser) && (currentUser.id!=domain.userDomainCreator().id)) {
            throw new ForbiddenException("You don't have the right to read this resource! You must be the same user!")
        }
    }

    public def checkIsNotSameUser(SecUser user, SecUser currentUser) {
        if ((currentUser.id==user.id)) {
            throw new ForbiddenException("You cannot do this action with your own profil!")
        }
    }

    /**
     * Check if currentUserId is memeber of the group set in constructor
     */
    public def checkIfUserIsMemberGroup(SecUser user, Group group) {
        if(!group) {
            throw new ObjectNotFoundException("Group from domain ${group} was not found! Unable to process group/user auth checking")
        }
        boolean isInside = false
        UserGroup.findAllByGroup(group).each {
            if(it.user.id==user.id) {
                isInside = true
                return true
            }
        }
        if (!isInside && !currentRoleServiceProxy.isAdminByNow(user))
            throw new ForbiddenException("User must be in this group!")
    }


}
