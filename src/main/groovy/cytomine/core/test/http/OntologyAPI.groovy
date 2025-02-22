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

import cytomine.core.ontology.Ontology
import cytomine.core.test.Infos
import grails.converters.JSON

/**
 * User: lrollus
 * Date: 6/12/11
 * This class implement all method to easily get/create/update/delete/manage Ontology to Cytomine with HTTP request during functional test
 */
class OntologyAPI extends DomainAPI {

    static def show(Long id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/ontology/" + id + ".json"
        return doGET(URL, username, password)
    }

    static def list(String username, String password) {
        list(username,password,false)
    }

    static def list(String username, String password, boolean light) {
        String URL = Infos.CYTOMINEURL +  (light? "api/ontology.json?light=true":"api/ontology.json")
        return doGET(URL, username, password)
    }

    static def create(String json, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/ontology.json"
        def result = doPOST(URL,json,username,password)
        result.data = Ontology.get(JSON.parse(result.data)?.ontology?.id)
        return result
    }

    static def update(def id, def jsonOntology, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/ontology/" + id + ".json"
        return doPUT(URL,jsonOntology,username,password)
    }

    static def delete(def id, String username, String password) {
        String URL = Infos.CYTOMINEURL + "api/ontology/" + id + ".json"
        return doDELETE(URL,username,password)
    }
}
