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

import cytomine.core.ontology.Ontology
import cytomine.core.test.BasicInstanceBuilder
import cytomine.core.test.Infos
import cytomine.core.test.http.OntologyAPI
import cytomine.core.utils.UpdateData
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class OntologyTests  {

    void testListOntologyWithCredential() {
        def result = OntologyAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }
  
    void testListOntologyWithoutCredential() {
        def result = OntologyAPI.list(Infos.BADLOGIN, Infos.BADPASSWORD)
        assert 401 == result.code
    }

    void testListOntologyLightWithCredential() {
        def result = OntologyAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD,true)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }
  
    void testShowOntologyWithCredential() {
        def result = OntologyAPI.show(BasicInstanceBuilder.getOntology().id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }
  
    void testAddOntologyCorrect() {
        def ontologyToAdd = BasicInstanceBuilder.getOntologyNotExist()
        def result = OntologyAPI.create(ontologyToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        int idOntology = result.data.id
  
        result = OntologyAPI.show(idOntology, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
  
        result = OntologyAPI.undo()
        assert 200 == result.code
  
        result = OntologyAPI.show(idOntology, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
  
        result = OntologyAPI.redo()
        assert 200 == result.code
  
        result = OntologyAPI.show(idOntology, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }
  
    void testAddOntologyAlreadyExist() {
        def ontologyToAdd = BasicInstanceBuilder.getOntology()
        def result = OntologyAPI.create(ontologyToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 409 == result.code
    }
  
    void testUpdateOntologyCorrect() {
        Ontology ontologyToAdd = BasicInstanceBuilder.getOntology()
        def data = UpdateData.createUpdateSet(ontologyToAdd,[name: ["OLDNAME","NEWNAME"], user:[BasicInstanceBuilder.user1,BasicInstanceBuilder.user2]])
        def result = OntologyAPI.update(ontologyToAdd.id, data.postData,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        int idOntology = json.ontology.id
  
        def showResult = OntologyAPI.show(idOntology, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        json = JSON.parse(showResult.data)
        BasicInstanceBuilder.compare(data.mapNew, json)
  
        showResult = OntologyAPI.undo()
        assert 200 == result.code
        showResult = OntologyAPI.show(idOntology, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        BasicInstanceBuilder.compare(data.mapOld, JSON.parse(showResult.data))
  
        showResult = OntologyAPI.redo()
        assert 200 == result.code
        showResult = OntologyAPI.show(idOntology, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        BasicInstanceBuilder.compare(data.mapNew, JSON.parse(showResult.data))
    }
  
    void testUpdateOntologyNotExist() {
        Ontology ontologyWithOldName = BasicInstanceBuilder.getOntology()
        Ontology ontologyWithNewName = BasicInstanceBuilder.getOntologyNotExist()
        ontologyWithNewName.save(flush: true)
        Ontology ontologyToEdit = Ontology.get(ontologyWithNewName.id)
        def jsonOntology = ontologyToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonOntology)
        jsonUpdate.name = ontologyWithOldName.name
        jsonUpdate.id = -99
        jsonOntology = jsonUpdate.toString()
        def result = OntologyAPI.update(-99, jsonOntology, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }
  
    void testUpdateOntologyWithNameAlreadyExist() {
        Ontology ontologyWithOldName = BasicInstanceBuilder.getOntology()
        Ontology ontologyWithNewName = BasicInstanceBuilder.getOntologyNotExist()
        ontologyWithNewName.save(flush: true)
        Ontology ontologyToEdit = Ontology.get(ontologyWithNewName.id)
        def jsonOntology = ontologyToEdit.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonOntology)
        jsonUpdate.name = ontologyWithOldName.name
        jsonOntology = jsonUpdate.toString()
        def result = OntologyAPI.update(ontologyToEdit.id, jsonOntology, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 409 == result.code
    }
      
      void testEditOntologyWithBadName() {
          Ontology ontologyToAdd = BasicInstanceBuilder.getOntology()
          Ontology ontologyToEdit = Ontology.get(ontologyToAdd.id)
          def jsonOntology = ontologyToEdit.encodeAsJSON()
          def jsonUpdate = JSON.parse(jsonOntology)
          jsonUpdate.name = null
          jsonOntology = jsonUpdate.toString()
          def result = OntologyAPI.update(ontologyToAdd.id, jsonOntology, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
          assert 400 == result.code
      }
  
    void testDeleteOntology() {
        def ontologyToDelete = BasicInstanceBuilder.getOntologyNotExist()
        assert ontologyToDelete.save(flush: true)!= null
        def id = ontologyToDelete.id
        def result = OntologyAPI.delete(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
  
        def showResult = OntologyAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == showResult.code
  
        result = OntologyAPI.undo()
        assert 200 == result.code
  
        result = OntologyAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
  
        result = OntologyAPI.redo()
        assert 200 == result.code
  
        result = OntologyAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }
  
    void testDeleteOntologyNotExist() {
        def result = OntologyAPI.delete(-99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }
  
    void testDeleteOntologyWithProject() {
        def project = BasicInstanceBuilder.getProject()
        def ontologyToDelete = project.ontology
        def result = OntologyAPI.delete(ontologyToDelete.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 400 == result.code
    }

  void testDeleteOntologyWithTerms() {

    log.info("create ontology")
    //create project and try to delete his ontology
    def relationTerm = BasicInstanceBuilder.getRelationTermNotExist()
    relationTerm.save(flush:true)
    def ontologyToDelete = relationTerm.term1.ontology
    assert ontologyToDelete.save(flush:true)!=null
    int idOntology = ontologyToDelete.id
      def result = OntologyAPI.delete(idOntology, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code

      def showResult = OntologyAPI.show(idOntology, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == showResult.code

      result = OntologyAPI.undo()
      assert 200 == result.code

      result = OntologyAPI.show(idOntology, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code

      result = OntologyAPI.redo()
      assert 200 == result.code

      result = OntologyAPI.show(idOntology, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == result.code

  }
}
