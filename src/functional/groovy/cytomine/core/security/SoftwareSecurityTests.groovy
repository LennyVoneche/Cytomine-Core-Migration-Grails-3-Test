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

import cytomine.core.processing.Software
import cytomine.core.test.BasicInstanceBuilder
import cytomine.core.test.Infos
import cytomine.core.test.http.SoftwareAPI
import grails.converters.JSON

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 2/03/11
 * Time: 11:08
 * To change this template use File | Settings | File Templates.
 */
class SoftwareSecurityTests extends SecurityTestsAbstract {
    
  void testSoftwareSecurityForCytomineAdmin() {

      //Get user1
      User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)

      //Get admin user
      User admin = BasicInstanceBuilder.getSuperAdmin(USERNAMEADMIN,PASSWORDADMIN)

      //Create new software (user1)
      def result = SoftwareAPI.create(BasicInstanceBuilder.getSoftwareNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      Software software = result.data

      //check if admin user can access/update/delete
      assert (200 == SoftwareAPI.show(software.id,USERNAMEADMIN,PASSWORDADMIN).code)
      assert (true ==SoftwareAPI.containsInJSONList(software.id,JSON.parse(SoftwareAPI.list(USERNAMEADMIN,PASSWORDADMIN).data)))
      assert (200 == SoftwareAPI.update(software.id,software.encodeAsJSON(),USERNAMEADMIN,PASSWORDADMIN).code)
      assert (200 == SoftwareAPI.delete(software.id,USERNAMEADMIN,PASSWORDADMIN).code)
  }

  void testSoftwareSecurityForSoftwareCreator() {

      //Get user1
      User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)

      //Create new Software (user1)
      def result = SoftwareAPI.create(BasicInstanceBuilder.getSoftwareNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      Software software = result.data

      //check if user 1 can access/update/delete
      assert (200 == SoftwareAPI.show(software.id,USERNAME1,PASSWORD1).code)
      assert (true ==SoftwareAPI.containsInJSONList(software.id,JSON.parse(SoftwareAPI.list(USERNAME1,PASSWORD1).data)))
      assert (200 == SoftwareAPI.update(software.id,software.encodeAsJSON(),USERNAME1,PASSWORD1).code)
      assert (200 == SoftwareAPI.delete(software.id,USERNAME1,PASSWORD1).code)
  }

  void testSoftwareSecurityForSimpleUser() {

      //Get user1
      User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)
      //Get user2
      User user2 = BasicInstanceBuilder.getUser(USERNAME2,PASSWORD2)

      //Create new Software (user1)
      def result = SoftwareAPI.create(BasicInstanceBuilder.getSoftwareNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      Software software = result.data
      Infos.printRight(software)
      //check if user 2 cannot access/update/delete
      assert (200 == SoftwareAPI.show(software.id,USERNAME2,PASSWORD2).code)
      assert (403 == SoftwareAPI.update(software.id,software.encodeAsJSON(),USERNAME2,PASSWORD2).code)
      assert (403 == SoftwareAPI.delete(software.id,USERNAME2,PASSWORD2).code)

  }

  void testSoftwareSecurityForAnonymous() {

      //Get user1
      User user1 = BasicInstanceBuilder.getUser(USERNAME1,PASSWORD1)

      //Create new Software (user1)
      def result = SoftwareAPI.create(BasicInstanceBuilder.getSoftwareNotExist().encodeAsJSON(),USERNAME1,PASSWORD1)
      assert 200 == result.code
      Software software = result.data
      Infos.printRight(software)
      //check if user 2 cannot access/update/delete
      assert (401 == SoftwareAPI.show(software.id,USERNAMEBAD,PASSWORDBAD).code)
      assert (401 == SoftwareAPI.list(USERNAMEBAD,PASSWORDBAD).code)
      assert (401 == SoftwareAPI.update(software.id,software.encodeAsJSON(),USERNAMEBAD,PASSWORDBAD).code)
      assert (401 == SoftwareAPI.delete(software.id,USERNAMEBAD,PASSWORDBAD).code)
  }
}
