package cytomine.core.test.http

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

import cytomine.core.image.ImageInstance
import cytomine.core.ontology.UserAnnotation
import cytomine.core.test.BasicInstanceBuilder
import cytomine.core.test.Infos
import grails.converters.JSON
import org.grails.web.json.JSONArray

/**
 * User: lrollus
 * Date: 6/12/11
 * This class implement all method to easily get/create/update/delete/manage Annotation to Cytomine with HTTP request during functional test
 */
class UserAnnotationAPI extends DomainAPI {

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/userannotation/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def countByUser(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "/api/user/$id/userannotation/count.json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/userannotation.json"
        return doGET(URL, username, password)
    }

    static def listByProject(Long id, String username, String password) {
        listByProject(id,false,username,password)
    }

    static def listByProject(Long id, boolean offset, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation.json?project=$id" + (offset? "&offset=0&max=3":"")
        return doGET(URL, username, password)
    }

    static def listByProject(Long id, Long idUser, Long idImage, String username, String password) {
        //String URL = Infos.CYTOMINEURL + "api/project/$id/userannotation.json?users="+idUser+"&images="+idImage
        String URL = Infos.CYTOMINEURL + "api/annotation.json?project=$id&users="+idUser+"&images="+idImage
        return doGET(URL, username, password)
    }

    static def listByImage(Long id, String username, String password, List propertiesToShow = null) {
        String URL = Infos.CYTOMINEURL + "api/annotation.json?image=$id&" + buildPropertiesToShowURLParams(propertiesToShow)
        return doGET(URL, username, password)
    }

    static def listByImages(Long project,List<Long> ids, String username, String password, List propertiesToShow = null) {
        String URL = Infos.CYTOMINEURL + "api/annotation.json?project=${project}&images=${ids.join(',')}&" + buildPropertiesToShowURLParams(propertiesToShow)
        return doGET(URL, username, password)
    }

    static def buildPropertiesToShowURLParams(List propertiesToShow) {
        if(!propertiesToShow)  return ""
        def params = []
        propertiesToShow.each {
            params << it + "=true"
        }
        return params.join("&")
    }


    static def listByProjectAndTerm(Long idProject, Long idTerm, Long idUser,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation.json?term=$idTerm&project=$idProject&users="+idUser
        return doGET(URL, username, password)
    }

    static def listByProjectAndTerm(Long idProject, Long idTerm,Long idImage, Long idUser,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/annotation.json?term=$idTerm&project=$idProject&users="+idUser+"&offset=0&max=5"
        return doGET(URL, username, password)
    }

    static def listByImageAndUser(Long idImage,Long idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/annotation.json?user="+ idUser +"&image="+idImage
        return doGET(URL, username, password)
    }

    static def listByProjectAndUsers(Long id,Long idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/annotation.json?project=$id&users=" +idUser
        return doGET(URL, username, password)
    }

    static def listByProjectAndUsersWithoutTerm(Long id,Long idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/annotation.json?project=$id&noTerm=true&users=$idUser"
        return doGET(URL, username, password)
    }

    static def listByProjectAndUsersSeveralTerm(Long id,Long idUser, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/annotation.json?project=$id&multipleTerm=true&users=" +idUser
        return doGET(URL, username, password)
    }

    static def listByImageAndUser(Long idImage,Long idUser, String bbox, boolean netReviewedOnly,Integer force,String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/annotation.json?user=$idUser&image=$idImage&bbox=${bbox.replace(" ","%20")}&notReviewedOnly=$netReviewedOnly" + (force? "&kmeansValue=$force": "")
        return doGET(URL, username, password)
    }

    static def downloadDocumentByProject(Long idProject,Long idUser, Long idTerm, Long idImageInstance, String username, String password) {
        String URL = Infos.CYTOMINEURL+"api/project/"+ idProject +"/userannotation/download?users=" +idUser + "&terms=" + idTerm +"&images=" + idImageInstance + "&format=pdf"
        return doGET(URL, username, password)
    }

    static def create(String jsonAnnotation, String username, String password) {
        create(jsonAnnotation,null,null,username,password)
    }

    static def create(String jsonAnnotation, def minPoint,def maxPoint,String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/userannotation.json?"+(minPoint? "&minPoint=$minPoint": "")+(maxPoint? "&maxPoint=$maxPoint": "")
        def result = doPOST(URL,jsonAnnotation,username,password)
        def json = JSON.parse(result.data)
        if(JSON.parse(jsonAnnotation) instanceof JSONArray) return [code: result.code]
        Long idAnnotation = json?.annotation?.id
        return [data: UserAnnotation.get(idAnnotation), code: result.code]
    }

    static def update(def id, def jsonAnnotation, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/userannotation/" + id + ".json"
        return doPUT(URL,jsonAnnotation,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/userannotation/" + id + ".json"
        return doDELETE(URL,username,password)
    }

    static def buildBasicUserAnnotation(String username, String password) {
        def project = BasicInstanceBuilder.getProjectNotExist()
        Infos.addUserRight(username,project.ontology)
        //Create project with user 1
        def result = ProjectAPI.create(project.encodeAsJSON(), username, password)
        assert 200==result.code
        project = result.data

        //Add image with user 1
        ImageInstance image = BasicInstanceBuilder.getImageInstanceNotExist()
        image.project = project
        result = ImageInstanceAPI.create(image.encodeAsJSON(), username, password)
        assert 200==result.code
        image = result.data

        //Add annotation 1 with cytomine admin
        UserAnnotation annotation = BasicInstanceBuilder.getUserAnnotationNotExist()
        annotation.image = image
        annotation.project = image.project
        result = UserAnnotationAPI.create(annotation.encodeAsJSON(), username, password)
        assert 200==result.code
        annotation = result.data
        return annotation
    }

}
