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

import cytomine.core.test.BasicInstanceBuilder
import cytomine.core.test.Infos
import cytomine.core.test.http.ImagingServerAPI
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
class ImagingServerTests {

    void testListImagingServerWithCredential() {
        def result = ImagingServerAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json.collection instanceof JSONArray
    }

    void testShowImagingServerWithCredential() {
        def result = ImagingServerAPI.show(BasicInstanceBuilder.getImagingServer().id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject

        result = ImagingServerAPI.show(-99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    void testAddImagingServerCorrect() {
        def imagingServerToAdd = BasicInstanceBuilder.getImagingServerNotExist()

        def result = ImagingServerAPI.create(imagingServerToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        int idSoftware = result.data.id

        result = ImagingServerAPI.show(idSoftware, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }

    void testAddImagingServerAlreadyExist() {
        def imagingServerToAdd = BasicInstanceBuilder.getImagingServer()
        def result = ImagingServerAPI.create(imagingServerToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 409 == result.code
    }

    void testAddImagingServerWithoutCredential() {
        def imagingServerToAdd = BasicInstanceBuilder.getImagingServerNotExist()
        def result = ImagingServerAPI.create(imagingServerToAdd.encodeAsJSON(), Infos.ADMINLOGIN, Infos.ADMINPASSWORD)
        assert 403 == result.code
    }

    void testDeleteImagingServerWithCredential() {
        def imagingServerToDelete = BasicInstanceBuilder.getImagingServerNotExist(true)
        def id = imagingServerToDelete.id
        def result = ImagingServerAPI.delete(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        def showResult = ImagingServerAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == showResult.code
    }

    void testDeleteImagingServerWithoutCredential() {
        def imagingServerToDelete = BasicInstanceBuilder.getImagingServerNotExist(true)
        def id = imagingServerToDelete.id
        def result = ImagingServerAPI.delete(id, Infos.ADMINLOGIN, Infos.ADMINPASSWORD)
        assert 403 == result.code
    }

    void testDeleteSoftwareNotExist() {
        def result = ImagingServerAPI.delete(-99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

}
