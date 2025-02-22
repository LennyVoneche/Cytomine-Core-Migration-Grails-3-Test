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

import cytomine.core.security.User
import cytomine.core.test.BasicInstanceBuilder
import cytomine.core.test.Infos
import cytomine.core.test.http.ProjectAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject

class LoginTests {

    void testPingWithCredential() {
        def result = ProjectAPI.doPing(0,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        assert json.alive
        assert json.authenticated
    }

    void testLoginWithIlikeUsername() {
        def username = ""
        int i = Math.abs(((new Random()).nextInt())%(Infos.SUPERADMINLOGIN.size()))

        username += Infos.SUPERADMINLOGIN.substring(0,i)
        if(Infos.SUPERADMINLOGIN.charAt(i).toUpperCase() == Infos.SUPERADMINLOGIN.charAt(i)) {
            username += Infos.SUPERADMINLOGIN.charAt(i).toLowerCase()
        } else {
            username += Infos.SUPERADMINLOGIN.charAt(i).toUpperCase()
        }
        username += Infos.SUPERADMINLOGIN.substring(i+1)

        def result = ProjectAPI.doPing(0,username, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        def json = JSON.parse(result.data)
        assert json instanceof JSONObject
        assert json.alive
        assert json.authenticated
    }

    void testPingWithoutCredential() {
        def result = ProjectAPI.doPing(0,Infos.BADLOGIN, Infos.BADPASSWORD)
        assert 401 == result.code
    }

    void testPingDisabledUser() {
        User user = BasicInstanceBuilder.getUserNotExist()
        user.enabled = false
        BasicInstanceBuilder.saveDomain(user)
        def result = ProjectAPI.doPing(0,user.username, "password")
        assert 401 == result.code
    }

}
