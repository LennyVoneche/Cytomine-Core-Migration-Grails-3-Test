package cytomine.core.perf

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
import cytomine.core.test.http.UserAPI
import cytomine.core.test.http.UserPositionAPI
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by hoyoux on 11.04.16.
 */
class UserPositionPerformanceTests {
    void testPerfPosition() {

        def users = [];
        users << [login : Infos.SUPERADMINLOGIN, password : Infos.SUPERADMINPASSWORD]

        def result;
        (1..100).each {
            User userToAdd = BasicInstanceBuilder.getUserNotExist()
            def jsonUser = new JSONObject(userToAdd.encodeAsJSON()).put("password", "password").toString()
            println "jsonUser =" + jsonUser
            result = UserAPI.create(jsonUser.toString(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
            assert 200 == result.code
            users << [login : userToAdd.username, password : "password"]
        }


        def image;
        (1..100).each {
            image = BasicInstanceBuilder.getImageInstance()
            def json = JSON.parse("{image:${image.id},lon:100,lat:100, zoom: 1}")

            users.each {
                result = UserPositionAPI.create(image.id, json.toString(),it.login, it.password)
                assert 200 == result.code
            }
        }

        def times = []
        long begin;
        long end;
        (1..100).each {
            begin = System.currentTimeMillis()
            result = UserPositionAPI.listLastByImage(image.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
            end = System.currentTimeMillis()

            times << (end-begin)
            assert 200 == result.code
        }
        println "times"
        println "max : "+times.max()+" ms"
        println "avg : "+times.sum()/times.size()+" ms"

        //OLD (createCriteria)
        //251 ms moyenne (max 538) pour 100 user avec chacun 1 position
        //255 ms moyenne (max 498) pour 100 user avec chacun 10 position
        //293 ms moyenne (max 581) pour 100 user avec chacun 100 position
        //311 ms moyenne (max 837) pour 200 user avec chacun 100 position

        //NEW (mongo aggregation request)
        //35 ms moyenne (max 1335) pour 100 user avec chacun 1 position
        //23.87 ms moyenne (max 165) pour 100 user avec chacun 10 position
        //20 ms moyenne (max 111) pour 100 user avec chacun 100 position
        //24.48 ms moyenne (max 128) pour 200 user avec chacun 100 position

    }
}
