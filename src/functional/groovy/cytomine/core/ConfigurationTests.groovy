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
import cytomine.core.test.http.ConfigurationAPI
import cytomine.core.utils.Configuration
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject

class ConfigurationTests {

    //TEST SHOW

    void testConfigurationShow() {

        def config = BasicInstanceBuilder.getConfiguration()

        def result = ConfigurationAPI.show(config.key, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    void testConfigurationShowNotExist() {
        def result = ConfigurationAPI.show("-1", Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }

    //TEST LIST
    void testConfigurationList() {
        def result = ConfigurationAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
    }

    //TEST DELETE
    void testConfigurationDelete() {
        def configToDelete = BasicInstanceBuilder.getConfigurationNotExist(true)
//        assert configToDelete.save(flush: true) != null

        def key = configToDelete.key
        def result = ConfigurationAPI.delete(key, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        //UNDO & REDO
        result = ConfigurationAPI.show(key, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code

        result = ConfigurationAPI.undo()
        assert 200 == result.code

        result = ConfigurationAPI.show(key, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        result = ConfigurationAPI.redo()
        assert 200 == result.code

        result = ConfigurationAPI.show(key, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code
    }
//
    //TEST ADD
    void testConfigurationAddCorrect() {
        def configToAdd = BasicInstanceBuilder.getConfigurationNotExist()

        def result = ConfigurationAPI.create(configToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)

        assert 200 == result.code
        def json = JSON.parse(result.data).configuration

        String key =  json.key

        //UNDO & REDO
        result = ConfigurationAPI.show(key, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        result = ConfigurationAPI.undo()
        assert 200 == result.code

        result = ConfigurationAPI.show(key, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == result.code

        result = ConfigurationAPI.redo()
        assert 200 == result.code

        result = ConfigurationAPI.show(key, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }

    //TEST UPDATE
    void testConfigurationUpdateCorrect() {
        Configuration configToUpdate = BasicInstanceBuilder.getConfiguration()

        def jsonConfig = configToUpdate.encodeAsJSON()
        def jsonUpdate = JSON.parse(jsonConfig)
        jsonUpdate.value = "test2"

        def result = ConfigurationAPI.update(configToUpdate.key, jsonUpdate.toString(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject

        assert json.configuration.value== "test2"
    }
}
