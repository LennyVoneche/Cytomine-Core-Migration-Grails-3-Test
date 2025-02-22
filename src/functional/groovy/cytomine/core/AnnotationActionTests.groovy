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
import cytomine.core.test.http.AnnotationActionAPI
import grails.converters.JSON

class AnnotationActionTests {


    void testAddAction() {
        def annotation = BasicInstanceBuilder.getUserAnnotation()
        def json = JSON.parse("{annotationIdent:${annotation.id},action:Test}");

        def result = AnnotationActionAPI.create(json.toString(),Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
    }

    void testList() {
        def image = BasicInstanceBuilder.getImageInstance()
        def annotation = BasicInstanceBuilder.getUserAnnotation()
        def json = JSON.parse("{image:${image.id}, annotationIdent:${annotation.id}, action:Test}")

        def result = AnnotationActionAPI.create(json.toString(),Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        Long creator = JSON.parse(result.data).user

        result = AnnotationActionAPI.listByImage(image.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        assert JSON.parse(result.data).collection.size() == 1

        result = AnnotationActionAPI.listByImageAndUser(image.id, creator, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        assert JSON.parse(result.data).collection.size() == 1

        result = AnnotationActionAPI.listByImageAndUser(image.id, BasicInstanceBuilder.user1.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code
        assert JSON.parse(result.data).collection.size() == 0
    }

    void testListAfterThan() {
        def image = BasicInstanceBuilder.getImageInstance()
        def annotation = BasicInstanceBuilder.getUserAnnotation()
        def json = JSON.parse("{image:${image.id}, annotation:${annotation.id}, action:Test}")

        def result = AnnotationActionAPI.create(json.toString(),Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        Long created = Long.parseLong(JSON.parse(result.data).created)
        Long creator = JSON.parse(result.data).user

        result = AnnotationActionAPI.listByImage(image.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD, created)
        assert 200 == result.code
        assert JSON.parse(result.data).collection.size() == 1

        result = AnnotationActionAPI.listByImageAndUser(image.id, creator, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD, created)
        assert 200 == result.code
        assert JSON.parse(result.data).collection.size() == 1

        result = AnnotationActionAPI.listByImage(image.id,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD, created+1)
        assert 200 == result.code
        assert JSON.parse(result.data).collection.size() == 0

        result = AnnotationActionAPI.listByImageAndUser(image.id, creator, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD, created+1)
        assert 200 == result.code
        assert JSON.parse(result.data).collection.size() == 0
    }
}
