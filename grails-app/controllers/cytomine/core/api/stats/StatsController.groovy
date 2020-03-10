package cytomine.core.api.stats

import cytomine.core.Exception.WrongArgumentException

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

import cytomine.core.api.RestController
import cytomine.core.image.ImageInstance
import cytomine.core.ontology.AlgoAnnotation
import cytomine.core.ontology.Ontology
import cytomine.core.ontology.Term
import cytomine.core.ontology.UserAnnotation
import cytomine.core.processing.Job
import cytomine.core.processing.Software
import cytomine.core.project.Project
import cytomine.core.security.User
//import org.restapidoc.annotation.RestApiMethod
//import org.restapidoc.annotation.RestApiParam
//import org.restapidoc.annotation.RestApiParams
//import org.restapidoc.pojo.RestApiParamType
import org.springframework.security.acls.domain.BasePermission

import static org.springframework.security.acls.domain.BasePermission.READ

class StatsController extends RestController {

    def cytomineService
    def securityACLService
    def termService
    def jobService
    def secUserService
    def projectConnectionService
    def statsService

    def allGlobalStats = {
        securityACLService.checkAdmin(cytomineService.getCurrentUser())

        def result = [:];
        result["users"] = statsService.total(User).total
        result["projects"] = statsService.total(Project).total
        result["images"] = statsService.total(ImageInstance).total
        result["userAnnotations"] = statsService.total(UserAnnotation).total
        result["jobAnnotations"] = statsService.total(AlgoAnnotation).total
        result["terms"] = statsService.total(Term).total
        result["ontologies"] = statsService.total(Ontology).total
        result["softwares"] = statsService.total(Software).total
        result["jobs"] = statsService.total(Job).total
        responseSuccess(result)

    }

    /**
     * Compute for each user, the number of annotation of each term
     */
    def statUserAnnotations = {

        //Get project
        Project project = Project.read(params.id)
        if (project == null) {
            responseNotFound("Project", params.id)
            return
        }

        securityACLService.check(project, BasePermission.READ)
        responseSuccess(statsService.statUserAnnotations(project))
    }

    /**
     * Compute number of annotation for each user
     */
    def statUser = {

        //Get project
        Project project = Project.read(params.id)
        if (!project) {
            responseNotFound("Project", params.id)
            return
        }

        securityACLService.check(project, BasePermission.READ)
        responseSuccess(statsService.statUser(project))
    }

    /**
     * Compute the number of annotation for each term
     */
    def statTerm = {

        //Get project
        Project project = Project.read(params.id)
        if (project == null) {
            responseNotFound("Project", params.id)
            return
        }

        securityACLService.check(project, BasePermission.READ)
        responseSuccess(statsService.statTerm(project))
    }

    /**
     * Compute the number of annotation for each sample and for each term
     */
    def statTermSlide = {

        //Get project
        Project project = Project.read(params.id)
        if (project == null) {
            responseNotFound("Project", params.id)
            return
        }

        securityACLService.check(project, BasePermission.READ)
        responseSuccess(statsService.statTermSlide(project))
    }

    /**
     * For each user, compute the number of sample where he made annotation
     */
    def statUserSlide = {
        Project project = Project.read(params.id)
        if (!project) {
            responseNotFound("Project", params.id)
            return
        }

        securityACLService.check(project, BasePermission.READ)
        responseSuccess(statsService.statUserSlide(project))
    }

    /**
     * Compute user annotation number evolution over the time for a project (start = project creation, stop = today)
     * params.daysRange = number of days between each measure
     * param.term = (optional) filter on a specific term
     */
    def statAnnotationEvolution = {

        Project project = Project.read(params.id)
        if (project == null) {
            responseNotFound("Project", params.id)
            return
        }

        securityACLService.check(project, BasePermission.READ)
        int daysRange = params.daysRange!=null ? params.getInt('daysRange') : 1
        Term term = Term.read(params.getLong('term'))
        responseSuccess(statsService.statAnnotationEvolution(project, term, daysRange))
    }

//    @RestApiMethod(description="Get the total of annotations with a term by project.")
//    @RestApiParams(params=[
//            @RestApiParam(name="id", type="long", paramType = RestApiParamType.PATH, description = "The term id")
//    ])
    def statAnnotationTermedByProject() {
        Term term = Term.read(params.id)
        if (!term) {
            responseNotFound("Term", params.id)
            return
        }
        securityACLService.check(term.container(), BasePermission.READ)
        responseSuccess(statsService.statAnnotationTermedByProject(term))
    }

//    @RestApiMethod(description="Get the total of user connection into a project by project.")
    def totalNumberOfConnectionsByProject(){
        securityACLService.checkAdmin(cytomineService.getCurrentUser())
        responseSuccess(projectConnectionService.totalNumberOfConnectionsByProject());
    }

//    @RestApiMethod(description="Get the total of the domains made on this instance.")
//    @RestApiParams(params=[
//            @RestApiParam(name="domain", type="string", paramType = RestApiParamType.PATH, description = "The domain name")
//    ])
    def totalDomains() {

        securityACLService.checkAdmin(cytomineService.getCurrentUser())
        def clazz = grailsApplication.domainClasses.find { it.clazz.simpleName.toLowerCase() == params.domain.toLowerCase() }
        if(!clazz){
            throw new WrongArgumentException("This domain doesn't exist!")
        }
        responseSuccess(statsService.total(clazz.clazz));
    }

//    @RestApiMethod(description="Get information about the current activity of Cytomine.")
    def statsOfCurrentActions() {
        securityACLService.checkAdmin(cytomineService.getCurrentUser())

        def result = [:];
        result["users"] = statsService.numberOfCurrentUsers().total
        result["projects"] = statsService.numberOfActiveProjects().total
        result["mostActiveProject"] = statsService.mostActiveProjects()
        responseSuccess(result)
    }

    def statUsedStorage(){
        securityACLService.checkAdmin(cytomineService.getCurrentUser())
        responseSuccess(statsService.statUsedStorage())
    }
}
