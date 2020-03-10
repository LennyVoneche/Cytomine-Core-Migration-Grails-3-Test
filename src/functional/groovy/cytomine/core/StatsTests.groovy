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

import cytomine.core.ontology.Term
import cytomine.core.processing.Job
import cytomine.core.project.Project
import cytomine.core.security.User
import cytomine.core.test.BasicInstanceBuilder
import cytomine.core.test.HttpClient
import cytomine.core.test.Infos
import cytomine.core.test.http.ProjectAPI
import cytomine.core.test.http.StatsAPI
import cytomine.core.test.http.TermAPI
import cytomine.core.test.http.UserAnnotationAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class StatsTests  {


    void testStatTerm() {
        Project project = BasicInstanceBuilder.getProject()

        def result;

        result = StatsAPI.statTerm(project.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        int total = json.collection.size()


        // we add a terme with an annotation

        Term termToAdd = BasicInstanceBuilder.getTermNotExist()
        result = TermAPI.create(termToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code



        def annotationToAdd = BasicInstanceBuilder.getUserAnnotationNotExist(project)
        Term term2 = BasicInstanceBuilder.getTermNotExist()
        term2.ontology = project.ontology
        BasicInstanceBuilder.saveDomain(term2)

        def annotationWithTerm = JSON.parse((String)annotationToAdd.encodeAsJSON())
        annotationWithTerm.term = [term2.id]

        result = UserAnnotationAPI.create(annotationWithTerm.toString(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code





        result = StatsAPI.statTerm(project.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert json.collection.size() == total+1


        result = StatsAPI.statTerm(-1, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    void testStatUser() {
        Project project = BasicInstanceBuilder.getProject()
        def result;

        result = StatsAPI.statUser(project.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        int total = json.collection.size()

        User user = BasicInstanceBuilder.getUser2();
        ProjectAPI.addUserProject(project.id, user.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)

        result = StatsAPI.statUser(project.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert json.collection.size() == total+1

        result = StatsAPI.statUser(-1, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code

    }


    void testStatsTermslide() {
        Project project = BasicInstanceBuilder.getProject()
        def result;

        result = StatsAPI.statTermSlide(project.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        int total = json.collection.size()

        // we add a terme with an annotation

        Term termToAdd = BasicInstanceBuilder.getTermNotExist()
        result = TermAPI.create(termToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code



        def annotationToAdd = BasicInstanceBuilder.getUserAnnotationNotExist(project)
        Term term2 = BasicInstanceBuilder.getTermNotExist()
        term2.ontology = project.ontology
        BasicInstanceBuilder.saveDomain(term2)

        def annotationWithTerm = JSON.parse((String)annotationToAdd.encodeAsJSON())
        annotationWithTerm.term = [term2.id]

        result = UserAnnotationAPI.create(annotationWithTerm.toString(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code




        result = StatsAPI.statTermSlide(project.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert json.collection.size() == total+1


        result = StatsAPI.statTermSlide(-1, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code

    }

    void testStatsUserAnnotation() {
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        def result;
        User admin = BasicInstanceBuilder.getUser(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)

        result = StatsAPI.statUserAnnotations(project.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        int total = json.collection.find{it.key == admin.firstname + " " + admin.lastname}.terms.size()

        // we add a terme with an annotation

        Term termToAdd = BasicInstanceBuilder.getTermNotExist()
        result = TermAPI.create(termToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code



        def annotationToAdd = BasicInstanceBuilder.getUserAnnotationNotExist(project)
        Term term2 = BasicInstanceBuilder.getTermNotExist()
        term2.ontology = project.ontology
        BasicInstanceBuilder.saveDomain(term2)

        def annotationWithTerm = JSON.parse((String)annotationToAdd.encodeAsJSON())
        annotationWithTerm.term = [term2.id]

        result = UserAnnotationAPI.create(annotationWithTerm.toString(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code





        result = StatsAPI.statUserAnnotations(project.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert json.collection.find{it.key == admin.firstname + " " + admin.lastname}.terms.size() == total+1


        result = StatsAPI.statUserAnnotations(-1, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    void testStatsUserSlide() {
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        def result;
        //User admin = BasicInstanceBuilder.getUser(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)

        result = StatsAPI.statUserSlide(project.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        int total = json.collection.size()

        User user = BasicInstanceBuilder.getUser2();
        ProjectAPI.addUserProject(project.id, user.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)

        result = StatsAPI.statUser(project.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert json.collection.size() == total+1

        result = StatsAPI.statUser(-1, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code


    }

    void testStatsEvolution() {
        Project project = BasicInstanceBuilder.getProject()
        def result;

        result = StatsAPI.statAnnotationEvolution(project.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }

    void testStatAnnotationTermedByProject() {
        Project project = BasicInstanceBuilder.getProjectNotExist(true)
        Term term = BasicInstanceBuilder.getTermNotExist(project.ontology,true)
        def result;

        result = StatsAPI.statAnnotationTermedByProject(term.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        int total = json.collection.find{it.key == project.name}.value

        def annotationToAdd = BasicInstanceBuilder.getUserAnnotationNotExist(project)

        def annotationWithTerm = JSON.parse((String)annotationToAdd.encodeAsJSON())
        annotationWithTerm.term = [term.id]

        result = UserAnnotationAPI.create(annotationWithTerm.toString(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code


        result = StatsAPI.statAnnotationTermedByProject(term.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray

        assert json.collection.find{it.key == project.name}.value == total +1
    }

    void testTotalNumberOfConnectionsByProject() {
        def result = StatsAPI.totalNumberOfConnectionsByProject(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }
    void testTotalDomains() {
        def result;

        result = StatsAPI.totalProjects(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.total >= 0
        assert json.total instanceof Integer

        result = StatsAPI.totalUsers(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        json = JSON.parse(result.data)
        assert json.total >= 0
        assert json.total instanceof Integer
    }
    void testStatsOfCurrentActions() {
        def result = StatsAPI.statsOfCurrentActions(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.users >= 0
        assert json.projects >= 0
    }
    void testStatUsedStorage() {
        def result = StatsAPI.statUsedStorage(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert (200 == result.code)
        def json = JSON.parse(result.data)
        assert json.total > 0
    }

    private void doGET(String URL, int expect) {
        HttpClient client = new HttpClient();
        client.connect(URL, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD);
        client.get()
        int code = client.getResponseCode()
        String response = client.getResponseData()
        client.disconnect();
        assert expect==code
    }

    void testRetrievalAVG() {
        Job job = BasicInstanceBuilder.createJobWithAlgoAnnotationTerm()
        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval/avg.json?job=${job.id}"
        doGET(URL,200)

        URL = Infos.CYTOMINEURL + "/api/stats/retrieval/avg.json?job=-99"
        doGET(URL,404)
    }

    void testRetrievalAVGNotExist() {
        Job job = BasicInstanceBuilder.createJobWithAlgoAnnotationTerm()
        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval/avg.json?job=${job.id}"
        doGET(URL,200)

        URL = Infos.CYTOMINEURL + "/api/stats/retrieval/avg.json?job=-99"
        doGET(URL,404)
    }


    void testRetrievalConfusionMatrix() {
        Job job = BasicInstanceBuilder.createJobWithAlgoAnnotationTerm()
        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval/confusionmatrix.json?job=${job.id}"
        doGET(URL,200)

        URL = Infos.CYTOMINEURL + "/api/stats/retrieval/confusionmatrix.json?job=-99"
        doGET(URL,404)
    }

    void testRetrievalWorstTerm() {
        Job job = BasicInstanceBuilder.createJobWithAlgoAnnotationTerm()
        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval/worstTerm.json?job=${job.id}"
        doGET(URL,200)
        URL = Infos.CYTOMINEURL + "/api/stats/retrieval/worstTerm.json?job=-99"
        doGET(URL,404)
    }

    void testRetrievalWorstAnnotation() {
        Job job = BasicInstanceBuilder.createJobWithAlgoAnnotationTerm()
        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval/worstAnnotation.json?job=${job.id}"
        doGET(URL,200)
        URL = Infos.CYTOMINEURL + "/api/stats/retrieval/worstAnnotation.json?job=${job.id}"
        doGET(URL,200)
    }

    void testRetrievalWorstTermWithSuggest() {
        Job job = BasicInstanceBuilder.createJobWithAlgoAnnotationTerm()
        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval/worstTermWithSuggest.json?job=${job.id}"
        doGET(URL,200)
        URL = Infos.CYTOMINEURL + "/api/stats/retrieval/worstTermWithSuggest.json?job=-99"
        doGET(URL,404)
    }

    void testRetrievalEvolution() {
        Job job = BasicInstanceBuilder.createJobWithAlgoAnnotationTerm()
        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval/evolution.json?job=${job.id}"
        doGET(URL,200)
        URL = Infos.CYTOMINEURL + "/api/stats/retrieval/evolution.json?job=-99"
        doGET(URL,404)
    }

    void testRetrievalEvolutionAlgo() {
        Job job = BasicInstanceBuilder.createJobWithAlgoAnnotationTerm()
        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval-evolution/evolution.json?job=${job.id}"
        doGET(URL,200)
        URL = Infos.CYTOMINEURL + "/api/stats/retrieval-evolution/evolution.json?job=-99"
        doGET(URL,404)
    }

    void testRetrievalEvolutionAlgoForTerm() {
        Job job = BasicInstanceBuilder.createJobWithAlgoAnnotationTerm()
        String URL = Infos.CYTOMINEURL + "/api/stats/retrieval-evolution/evolutionByTerm.json?job=${job.id}&term=${Term.findByOntology(job.project.ontology).id}"
        doGET(URL,200)
        URL = Infos.CYTOMINEURL + "/api/stats/retrieval-evolution/evolutionByTerm.json?job=-99&term=${Term.findByOntology(job.project.ontology).id}"
        doGET(URL,404)
    }


}
