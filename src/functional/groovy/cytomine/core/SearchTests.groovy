package cytomine.core

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

import cytomine.core.image.AbstractImage
import cytomine.core.image.ImageInstance
import cytomine.core.ontology.Property
import cytomine.core.ontology.UserAnnotation
import cytomine.core.project.Project
import cytomine.core.test.BasicInstanceBuilder
import cytomine.core.test.Infos
import cytomine.core.test.http.DomainAPI
import cytomine.core.test.http.SearchAPI
import cytomine.core.utils.Description
import cytomine.core.utils.SearchFilter
import cytomine.core.utils.SearchOperator
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray


class SearchTests {

    //Test LISTPROJECT
    void testListProject () {
        Project project1 = BasicInstanceBuilder.getProjectNotExist(true)
        Project project2 = BasicInstanceBuilder.getProjectNotExist(true)

        Property project1Property1 = BasicInstanceBuilder.getProjectPropertyNotExist()
        project1Property1.value = "Poney"
        project1Property1.domain = project1
        BasicInstanceBuilder.saveDomain(project1Property1)

        Property project1Property2 = BasicInstanceBuilder.getProjectPropertyNotExist()
        project1Property2.value = "Cheval"
        project1Property2.domain = project1
        BasicInstanceBuilder.saveDomain(project1Property2)

        Property project2Property2 = BasicInstanceBuilder.getProjectPropertyNotExist()
        project2Property2.value = "Cheval"
        project2Property2.domain = project2
        BasicInstanceBuilder.saveDomain(project2Property2)

        def result = SearchAPI.listDomain("Poney,Cheval", SearchOperator.OR, SearchFilter.PROJECT, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert DomainAPI.containsInJSONList(project1.id, json)
        assert DomainAPI.containsInJSONList(project2.id, json)

        result = SearchAPI.listDomain("Poney,Cheval", SearchOperator.AND, SearchFilter.PROJECT, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert DomainAPI.containsInJSONList(project1.id, json)
        assert !DomainAPI.containsInJSONList(project2.id, json)


        result = SearchAPI.listDomain(null, SearchOperator.OR, SearchFilter.PROJECT, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert DomainAPI.containsInJSONList(project1.id, json)
        assert DomainAPI.containsInJSONList(project2.id, json)

    }

    void testListProjectWithSpecialChar () {
         Project project1 = BasicInstanceBuilder.getProjectNotExist(true)
         Project project2 = BasicInstanceBuilder.getProjectNotExist(true)

         Property project1Property1 = BasicInstanceBuilder.getProjectPropertyNotExist()
         project1Property1.value = "Ponêy"
         project1Property1.domain = project1
         BasicInstanceBuilder.saveDomain(project1Property1)

         Property project1Property2 = BasicInstanceBuilder.getProjectPropertyNotExist()
         project1Property2.value = "Chéval"
         project1Property2.domain = project1
         BasicInstanceBuilder.saveDomain(project1Property2)

        Property project1Property3 = BasicInstanceBuilder.getProjectPropertyNotExist()
        project1Property3.value = "Chéval"
        project1Property3.domain = project2
        BasicInstanceBuilder.saveDomain(project1Property3)

         def result = SearchAPI.listDomain("Ponêy,Chéval", SearchOperator.OR, SearchFilter.PROJECT, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 200 == result.code

         def json = JSON.parse(result.data)
         assert json.collection instanceof JSONArray

         assert DomainAPI.containsInJSONList(project1.id, json)
         assert DomainAPI.containsInJSONList(project2.id, json)

         result = SearchAPI.listDomain("Ponêy,Chéval", SearchOperator.AND, SearchFilter.PROJECT, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
         assert 200 == result.code

         json = JSON.parse(result.data)
         assert json.collection instanceof JSONArray

         assert DomainAPI.containsInJSONList(project1.id, json)
         assert !DomainAPI.containsInJSONList(project2.id, json)

     }

    //Test LISTANNOTATION
    void testListAnnotation () {
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        UserAnnotation userAnnotation1 = BasicInstanceBuilder.getUserAnnotationNotExist(project, true)
        UserAnnotation userAnnotation2 = BasicInstanceBuilder.getUserAnnotationNotExist(project, true)

        Property annotation1Property1 = BasicInstanceBuilder.getAnnotationPropertyNotExist()
        annotation1Property1.value = "Poney"
        annotation1Property1.domain = userAnnotation1
        BasicInstanceBuilder.saveDomain(annotation1Property1)

        Property annotation1Property2 = BasicInstanceBuilder.getAnnotationPropertyNotExist()
        annotation1Property2.value = "Cheval"
        annotation1Property2.domain = userAnnotation1
        BasicInstanceBuilder.saveDomain(annotation1Property2)

        Property annotation2Property2 = BasicInstanceBuilder.getAnnotationPropertyNotExist()
        annotation2Property2.value = "Cheval"
        annotation2Property2.domain = userAnnotation2
        BasicInstanceBuilder.saveDomain(annotation2Property2)

        def result = SearchAPI.listDomain("Poney,Cheval", SearchOperator.OR, SearchFilter.ANNOTATION, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert DomainAPI.containsInJSONList(userAnnotation1.id, json)
        assert DomainAPI.containsInJSONList(userAnnotation2.id, json)

        result = SearchAPI.listDomain("Poney,Cheval", SearchOperator.AND, SearchFilter.ANNOTATION, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert DomainAPI.containsInJSONList(userAnnotation1.id, json)
        assert !DomainAPI.containsInJSONList(userAnnotation2.id, json)

    }

    //Test LISTIMAGE
    void testListImage () {
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        ImageInstance imageInstance1 = BasicInstanceBuilder.getImageInstanceNotExist(project, true)
        ImageInstance imageInstance2 = BasicInstanceBuilder.getImageInstanceNotExist(project, true)

        Property image1Property1 = BasicInstanceBuilder.getImageInstancePropertyNotExist()
        image1Property1.value = "Poney"
        image1Property1.domain = imageInstance1
        BasicInstanceBuilder.saveDomain(image1Property1)

        Property image1Property2 = BasicInstanceBuilder.getImageInstancePropertyNotExist()
        image1Property2.value = "Cheval"
        image1Property2.domain = imageInstance1
        BasicInstanceBuilder.saveDomain(image1Property2)

        Property image2Property2 = BasicInstanceBuilder.getImageInstancePropertyNotExist()
        image2Property2.value = "Cheval"
        image2Property2.domain = imageInstance2
        BasicInstanceBuilder.saveDomain(image2Property2)

        def result = SearchAPI.listDomain("Poney,Cheval", SearchOperator.OR, SearchFilter.IMAGE, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert DomainAPI.containsInJSONList(imageInstance1.id, json)
        assert DomainAPI.containsInJSONList(imageInstance2.id, json)

        result = SearchAPI.listDomain("Poney,Cheval", SearchOperator.AND, SearchFilter.IMAGE, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert DomainAPI.containsInJSONList(imageInstance1.id, json)
        assert !DomainAPI.containsInJSONList(imageInstance2.id, json)
    }


    //Test LISTPROJECT
    void testListAbstractImage () {
        AbstractImage abstractImage1 = BasicInstanceBuilder.getAbstractImageNotExist(true)
        AbstractImage abstractImage2 = BasicInstanceBuilder.getAbstractImageNotExist(true)

        Property abstractImage1Property1 = BasicInstanceBuilder.getAbstractImagePropertyNotExist()
        abstractImage1Property1.value = "Poney"
        abstractImage1Property1.domain = abstractImage1
        BasicInstanceBuilder.saveDomain(abstractImage1Property1)

        Property abstractImage1Property2 = BasicInstanceBuilder.getAbstractImagePropertyNotExist()
        abstractImage1Property2.value = "Cheval"
        abstractImage1Property2.domain = abstractImage1
        BasicInstanceBuilder.saveDomain(abstractImage1Property2)

        Property abstractImage2Property2 = BasicInstanceBuilder.getAbstractImagePropertyNotExist()
        abstractImage2Property2.value = "Cheval"
        abstractImage2Property2.domain = abstractImage2
        BasicInstanceBuilder.saveDomain(abstractImage2Property2)

        def result = SearchAPI.listDomain("Poney,Cheval", SearchOperator.OR, SearchFilter.ABSTRACTIMAGE, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert DomainAPI.containsInJSONList(abstractImage1.id, json)
        assert DomainAPI.containsInJSONList(abstractImage2.id, json)

        result = SearchAPI.listDomain("Poney,Cheval", SearchOperator.AND, SearchFilter.ABSTRACTIMAGE, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert DomainAPI.containsInJSONList(abstractImage1.id, json)
        assert !DomainAPI.containsInJSONList(abstractImage2.id, json)


        result = SearchAPI.listDomain(null, SearchOperator.OR, SearchFilter.ABSTRACTIMAGE, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert DomainAPI.containsInJSONList(abstractImage1.id, json)
        assert DomainAPI.containsInJSONList(abstractImage2.id, json)

    }    


    void testListWithDescription() {
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        ImageInstance imageInstance1 = BasicInstanceBuilder.getImageInstanceNotExist(project, true)
        ImageInstance imageInstance2 = BasicInstanceBuilder.getImageInstanceNotExist(project, true)

        Property image1Property1 = BasicInstanceBuilder.getImageInstancePropertyNotExist()
        image1Property1.value = "Poney"
        image1Property1.domain = imageInstance1
        BasicInstanceBuilder.saveDomain(image1Property1)

        Description image2Description1 =  BasicInstanceBuilder.getDescriptionNotExist(imageInstance2,true)
        image2Description1.data = "Blablbla Cheval Poney Blablabla"
        BasicInstanceBuilder.saveDomain(image2Description1)

        def result = SearchAPI.listDomain("Poney,Cheval", SearchOperator.OR, SearchFilter.IMAGE, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert DomainAPI.containsInJSONList(imageInstance1.id, json)
        assert DomainAPI.containsInJSONList(imageInstance2.id, json)

        result = SearchAPI.listDomain("Poney,Cheval", SearchOperator.AND, SearchFilter.IMAGE, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        assert DomainAPI.containsInJSONList(imageInstance2.id, json)

        result = SearchAPI.listDomain("Poney,Cheval", SearchOperator.AND, null, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
        assert DomainAPI.containsInJSONList(imageInstance2.id, json)

    }

    //Test LISTIMAGE
    void testListBadRequest () {
        def result = SearchAPI.listDomain("Poney,Cheval", "BAD", SearchFilter.IMAGE, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 400 == result.code
        result = SearchAPI.listDomain("Poney,Cheval", "OR", "BAD", Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 400 == result.code
    }
}
