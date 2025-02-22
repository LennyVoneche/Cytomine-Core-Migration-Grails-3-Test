package cytomine.core.api.social

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
import cytomine.core.Exception.CytomineException
import cytomine.core.api.RestController
//import org.restapidoc.annotation.RestApi
//import org.restapidoc.annotation.RestApiMethod
//import org.restapidoc.annotation.RestApiParam
//import org.restapidoc.annotation.RestApiParams
//import org.restapidoc.pojo.RestApiParamType
import cytomine.core.security.SecUser
import grails.util.Metadata
import org.springframework.security.acls.domain.BasePermission

import static org.springframework.security.acls.domain.BasePermission.READ

import java.text.SimpleDateFormat


//@RestApi(name="Social | image consultation services", description="Methods to manage the consultation records of an image by a user")
class RestImageConsultationController extends RestController {

    def projectService
    def imageConsultationService
    def exportService
    def securityACLService
    def cytomineService
    def secUserService

//    @RestApiMethod(description="Add a new image consultation record")
    def add() {
        try {
            responseSuccess(imageConsultationService.add(request.JSON))
        } catch (CytomineException e) {
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

//    @RestApiMethod(description = "List the last consulted image by each user for a given project")
//    @RestApiParams(params=[
//            @RestApiParam(name="project", type="long", paramType = RestApiParamType.PATH, description = "The project id")
//    ])
    def lastImageOfUsersByProject() {
        Project project = projectService.read(params.project)
        responseSuccess(imageConsultationService.lastImageOfUsersByProject(project))
    }

//    @RestApiMethod(description="Get the last consultations of an user into a project")
//    @RestApiParams(params=[
//            @RestApiParam(name="project", type="long", paramType = RestApiParamType.PATH, description = "The project id"),
//            @RestApiParam(name="user", type="long", paramType = RestApiParamType.PATH, description = "The user id")
//    ])
    def listImageConsultationByProjectAndUser(){
        securityACLService.check(params.project, Project, BasePermission.READ)
        Project project = projectService.read(params.project)
        SecUser user = secUserService.read(params.user)
        securityACLService.checkIsSameUserOrAdminContainer(project, user, cytomineService.currentUser)

        responseSuccess(imageConsultationService.listImageConsultationByProjectAndUser(Long.parseLong(params.project), Long.parseLong(params.user), Boolean.parseBoolean(params.distinctImages), params.int("max",0), params.int("offset",0)))
    }

//    @RestApiMethod(description = "Summarize the consulted images for a given user and a given project")
//    @RestApiParams(params=[
//            @RestApiParam(name="user", type="long", paramType = RestApiParamType.QUERY, description = "The user id", required=true),
//            @RestApiParam(name="project", type="long", paramType = RestApiParamType.QUERY, description = "The project id", required=true),
//            @RestApiParam(name="export", type="string", paramType = RestApiParamType.QUERY, description = "The export format (supported: csv). Otherwise, return a json", required=false),
//    ])
    def resumeByUserAndProject() {
        def result = imageConsultationService.resumeByUserAndProject(Long.parseLong(params.user), Long.parseLong(params.project))

        if(params.export.equals("csv")) {
            Long user = Long.parseLong(params.user)
            Long project = Long.parseLong(params.project)
            SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyyMMdd_hhmmss");
            String now = simpleFormat.format(new Date())
//            response.contentType = grailsApplication.config.grails.mime.types[params.format]
            response.contentType = Metadata.current.'grails.mime.types'.toString()
            response.setHeader("Content-disposition", "attachment; filename=image_consultations_of_user_${user}_project_${project}_${now}.${params.export}")

            def exporterIdentifier = params.export;
            def exportResult = []
            List fields = ["time", "first", "last", "frequency", "imageId", "imageName", "imageThumb", "numberOfCreatedAnnotations"]
            Map labels = ["time": "Cumulated duration (ms)", "first" : "First consultation", "last" : "Last consultation", "frequency" :"Number of consultations","imageId": "Id of image", "imageName": "Name", "imageThumb": "Thumb", "numberOfCreatedAnnotations": "Number of created annotations"]
            result.each {
                def data = [:]
                data.time = it.time ?: 0;
                data.first = it.first
                data.last = it.last
                data.frequency = it.frequency
                data.imageId = it.image
                data.imageName = it.imageName
                data.imageThumb = it.imageThumb
                data.numberOfCreatedAnnotations = it.countCreatedAnnotations
                exportResult << data
            }

            String title = "Consultations of images into project ${project} by user ${user}"
            exportService.export(exporterIdentifier, response.outputStream, exportResult, fields, labels, null, ["column.widths": [0.12, 0.12, 0.12, 0.12, 0.12, 0.12, 0.12, 0.12], "title": title, "csv.encoding": "UTF-8", "separator": ";"])
        } else {
            responseSuccess(result)
        }
    }

}
