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

import cytomine.core.ontology.AnnotationTerm
import cytomine.core.ontology.UserAnnotation
import cytomine.core.test.BasicInstanceBuilder
import cytomine.core.test.Infos
import cytomine.core.test.http.AnnotationTermAPI
import cytomine.core.test.http.ProjectAPI
import cytomine.core.test.http.UserAnnotationAPI
import grails.converters.JSON

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class AnnotationTermSecurityTests extends SecurityTestsAbstract {

    void testAnnotationTermSecurityForCytomineAdmin() {
        //Get User 1
        User user = getUser1()

        //Get cytomine admin
        User admin = getUserAdmin()

        //Create Annotation with user 1
        UserAnnotation annotation = UserAnnotationAPI.buildBasicUserAnnotation(SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)

        //Add annotation-Term for annotation 1 with cytomine admin
        AnnotationTerm annotationTerm = BasicInstanceBuilder.getAnnotationTermNotExist(annotation)

        def result = AnnotationTermAPI.createAnnotationTerm(annotationTerm.encodeAsJSON(), SecurityTestsAbstract.USERNAMEADMIN, SecurityTestsAbstract.PASSWORDADMIN)
        assert 200 == result.code
        annotationTerm = result.data

        //Get/List annotation-term with cytomine admin
        assert (200 == AnnotationTermAPI.showAnnotationTerm(annotationTerm.userAnnotation.id,annotationTerm.term.id,annotationTerm.user.id, SecurityTestsAbstract.USERNAMEADMIN, SecurityTestsAbstract.PASSWORDADMIN).code)
        result = AnnotationTermAPI.listAnnotationTermByAnnotation(annotationTerm.userAnnotation.id, SecurityTestsAbstract.USERNAMEADMIN, SecurityTestsAbstract.PASSWORDADMIN)
        assert 200 == result.code
        assert (true ==AnnotationTermAPI.containsInJSONList(annotationTerm.id, JSON.parse(result.data)))
        //Delete annotation 2 with cytomine admin
        assert (200 == AnnotationTermAPI.deleteAnnotationTerm(annotationTerm.userAnnotation.id,annotationTerm.term.id,annotationTerm.user.id, SecurityTestsAbstract.USERNAMEADMIN, SecurityTestsAbstract.PASSWORDADMIN).code)


    }

    void testAnnotationTermSecurityForAnnotationTermCreator() {
        //Get User 1
        User user = getUser1()

        //Add annotation 1 with cytomine admin
        UserAnnotation annotation = UserAnnotationAPI.buildBasicUserAnnotation(SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        Infos.addUserRight(user,annotation.project)

        //Add annotation-Term for annotation 1 with cytomine admin
        AnnotationTerm annotationTerm = BasicInstanceBuilder.getAnnotationTermNotExist()
        annotationTerm.term.ontology = annotation.project.ontology
        BasicInstanceBuilder.saveDomain(annotationTerm.term)
        annotationTerm.userAnnotation = annotation
        annotationTerm.user = User.findByUsername(SecurityTestsAbstract.USERNAME1)
        def result = AnnotationTermAPI.createAnnotationTerm(annotationTerm.encodeAsJSON(), SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assert 200 == result.code
        annotationTerm = result.data

        //Get/List annotation-term with cytomine admin
        assert (200 == AnnotationTermAPI.showAnnotationTerm(annotationTerm.userAnnotation.id,annotationTerm.term.id,annotationTerm.user.id, SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1).code)
        result = AnnotationTermAPI.listAnnotationTermByAnnotation(annotationTerm.userAnnotation.id, SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assert 200 == result.code
        assert (true ==AnnotationTermAPI.containsInJSONList(annotationTerm.id, JSON.parse(result.data)))
        //Delete annotation 2 with cytomine admin
        assert (200 == AnnotationTermAPI.deleteAnnotationTerm(annotationTerm.userAnnotation.id,annotationTerm.term.id,annotationTerm.user.id, SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1).code)
    }

    void testAnnotationTermSecurityForProjectUser() {
        //Get User 1
        User user = getUser1()

        //Get User 2
        User user2 = getUser2()

        //Create project with user 1
        UserAnnotation annotation = UserAnnotationAPI.buildBasicUserAnnotation(SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        Infos.addUserRight(user,annotation.project.ontology)

        //Add project right for user 2
        def resAddUser = ProjectAPI.addUserProject(annotation.project.id, user2.id, SecurityTestsAbstract.USERNAMEADMIN, SecurityTestsAbstract.PASSWORDADMIN)
        assert 200 == resAddUser.code

        //Add annotation-Term for annotation 1 with cytomine admin
        AnnotationTerm annotationTerm = BasicInstanceBuilder.getAnnotationTermNotExist()
        annotationTerm.term.ontology = annotation.project.ontology
        BasicInstanceBuilder.saveDomain(annotationTerm.term)
        annotationTerm.userAnnotation = annotation
        def result = AnnotationTermAPI.createAnnotationTerm(annotationTerm.encodeAsJSON(), SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assert 200 == result.code
        annotationTerm = result.data

        //Get/List annotation-term with cytomine admin
        assert (200 == AnnotationTermAPI.showAnnotationTerm(annotationTerm.userAnnotation.id,annotationTerm.term.id,annotationTerm.user.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2).code)
        result = AnnotationTermAPI.listAnnotationTermByAnnotation(annotationTerm.userAnnotation.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2)
        assert 200 == result.code
        assert (true ==AnnotationTermAPI.containsInJSONList(annotationTerm.id, JSON.parse(result.data)))
        //Delete annotation 2 with cytomine admin
        assert (403 == AnnotationTermAPI.deleteAnnotationTerm(annotationTerm.userAnnotation.id,annotationTerm.term.id,annotationTerm.user.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2).code)
    }

    void testAnnotationTermSecurityForUser() {
        //Get User 1
        User user = getUser1()

        //Get User 2
        User user2 = getUser2()

        //Create project with user 1
        UserAnnotation annotation = UserAnnotationAPI.buildBasicUserAnnotation(SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)

        //Add annotation-Term for annotation 1 with cytomine admin
        AnnotationTerm annotationTerm = BasicInstanceBuilder.getAnnotationTermNotExist(annotation)
        def result = AnnotationTermAPI.createAnnotationTerm(annotationTerm.encodeAsJSON(), SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assert 200 == result.code
        annotationTerm = result.data

        //Get/List annotation-term with cytomine admin
        assert (403 == AnnotationTermAPI.showAnnotationTerm(annotationTerm.userAnnotation.id,annotationTerm.term.id,annotationTerm.user.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2).code)
        result = AnnotationTermAPI.listAnnotationTermByAnnotation(annotationTerm.userAnnotation.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2)
        assert (403 == result.code)
        //assert (true ==AnnotationTermAPI.containsInJSONList(annotationTerm.id, JSON.parse(result.data)))
        //Delete annotation 2 with cytomine admin
        assert (403 == AnnotationTermAPI.deleteAnnotationTerm(annotationTerm.userAnnotation.id,annotationTerm.term.id,annotationTerm.user.id, SecurityTestsAbstract.USERNAME2, SecurityTestsAbstract.PASSWORD2).code)
    }

    void testAnnotationTermSecurityForAnonymous() {
        //Get User 1
        User user = getUser1()

        //Create project with user 1
        UserAnnotation annotation = UserAnnotationAPI.buildBasicUserAnnotation(SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)

        //Add annotation-Term for annotation 1 with cytomine admin
        AnnotationTerm annotationTerm = BasicInstanceBuilder.getAnnotationTermNotExist(annotation)
        def result = AnnotationTermAPI.createAnnotationTerm(annotationTerm.encodeAsJSON(), SecurityTestsAbstract.USERNAME1, SecurityTestsAbstract.PASSWORD1)
        assert 200 == result.code
        annotationTerm = result.data

        //Get/List annotation-term with cytomine admin
        assert (401 == AnnotationTermAPI.showAnnotationTerm(annotationTerm.userAnnotation.id,annotationTerm.term.id,annotationTerm.user.id, SecurityTestsAbstract.USERNAMEBAD, SecurityTestsAbstract.PASSWORDBAD).code)
        assert (401 == AnnotationTermAPI.listAnnotationTermByAnnotation(annotationTerm.userAnnotation.id, SecurityTestsAbstract.USERNAMEBAD, SecurityTestsAbstract.PASSWORDBAD).code)
        assert (401 == AnnotationTermAPI.deleteAnnotationTerm(annotationTerm.userAnnotation.id,annotationTerm.term.id,annotationTerm.user.id, SecurityTestsAbstract.USERNAMEBAD, SecurityTestsAbstract.PASSWORDBAD).code)
    }
}
