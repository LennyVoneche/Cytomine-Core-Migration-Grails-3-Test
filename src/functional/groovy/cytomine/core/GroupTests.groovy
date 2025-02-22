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

import cytomine.core.security.Group
import cytomine.core.security.UserGroup
import cytomine.core.test.BasicInstanceBuilder
import cytomine.core.test.Infos
import cytomine.core.test.http.GroupAPI
import cytomine.core.test.http.UserGroupAPI
import cytomine.core.utils.UpdateData
import grails.converters.JSON
import grails.util.Holders
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 16/03/11
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
class GroupTests  {

  void testListGroup() {
      def result = GroupAPI.list(Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json.collection instanceof JSONArray
  }

  void testShowGroup() {
      def result = GroupAPI.show(BasicInstanceBuilder.getGroup().id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject

      result = GroupAPI.show(-99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == result.code
  }

  void testAddGroupCorrect() {
      def groupToAdd = BasicInstanceBuilder.getGroupNotExist()
      def result = GroupAPI.create(groupToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      int idGroup = result.data.id

      result = GroupAPI.show(idGroup, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
  }

  void testAddGroupAlreadyExist() {
      def groupToAdd = BasicInstanceBuilder.getGroup()
      def result = GroupAPI.create(groupToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 409 == result.code
  }

  void testUpdateGroupCorrect() {
      Group group = BasicInstanceBuilder.getGroup()
      def data = UpdateData.createUpdateSet(group,[name: ["OLDNAME","NEWNAME"]])
      def result = GroupAPI.update(group.id, data.postData,Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 200 == result.code
      def json = JSON.parse(result.data)
      assert json instanceof JSONObject
  }

  void testUpdateGroupNotExist() {
      Group groupWithOldName = BasicInstanceBuilder.getGroup()
      Group groupWithNewName = BasicInstanceBuilder.getGroupNotExist()
      groupWithNewName.save(flush: true)
      Group groupToEdit = Group.get(groupWithNewName.id)
      def jsonGroup = groupToEdit.encodeAsJSON()
      def jsonUpdate = JSON.parse(jsonGroup)
      jsonUpdate.name = groupWithOldName.name
      jsonUpdate.id = -99
      jsonGroup = jsonUpdate.toString()
      def result = GroupAPI.update(-99, jsonGroup, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == result.code
  }

  void testUpdateGroupWithNameAlreadyExist() {
      Group groupWithOldName = BasicInstanceBuilder.getGroup()
      Group groupWithNewName = BasicInstanceBuilder.getGroupNotExist()
      groupWithNewName.save(flush: true)
      Group groupToEdit = Group.get(groupWithNewName.id)
      def jsonGroup = groupToEdit.encodeAsJSON()
      def jsonUpdate = JSON.parse(jsonGroup)
      jsonUpdate.name = groupWithOldName.name
      jsonGroup = jsonUpdate.toString()
      def result = GroupAPI.update(groupToEdit.id, jsonGroup, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 409 == result.code
  }

    void testDeleteGroup() {
        def groupToDelete = BasicInstanceBuilder.getGroupNotExist()
        assert groupToDelete.save(flush: true)!= null
        def id = groupToDelete.id
        def result = GroupAPI.delete(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 200 == result.code

        def showResult = GroupAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
        assert 404 == showResult.code
    }

    void testDeleteDependanciesWithLDAP() {
        def ldapDisabled = Holders.getGrailsApplication().config.grails.plugin.springsecurity.ldap.active.toString()=="false"
        println "CREATE FROM LDAP"
        def groupToDelete = BasicInstanceBuilder.getGroupNotExist()
        groupToDelete.name = '2e an. master sc. mathématiques, fin. appr.'
        def result = GroupAPI.createFromLDAP(groupToDelete.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)

        if(ldapDisabled) {
            assert 404 == result.code
        } else {
            if(200 == result.code){
                assert JSON.parse(result.data).name == '2e an. master sc. mathématiques, fin. appr.'

                def group = Group.read(JSON.parse(result.data).id)

                def id = group.id
                result = GroupAPI.delete(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
                assert 200 == result.code

                assert 0 == UserGroup.findAllByGroup(group).size()

                def showResult = GroupAPI.show(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
                assert 404 == showResult.code
            }
        }
    }

    void testDeleteGroupNotExist() {
      def result = GroupAPI.delete(-99, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
      assert 404 == result.code
  }

    void testLDAPCorrect() {

        def ldapDisabled = Holders.getGrailsApplication().config.grails.plugin.springsecurity.ldap.active.toString()=="false"
        println "CREATE FROM LDAP"
        def groupToAdd = BasicInstanceBuilder.getGroupNotExist()
        groupToAdd.name = '2e an. master sc. math., fin. spéc. infor.'
        def result = GroupAPI.createFromLDAP(groupToAdd.encodeAsJSON(), Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)

        if(ldapDisabled) {
            assert 404 == result.code
        } else {
            if(200 == result.code) {
                assert JSON.parse(result.data).name == '2e an. master sc. math., fin. spéc. infor.'

                println "userGroup size"
                def normalSize;
                def group = Group.read(JSON.parse(result.data).id)
                def userGroup = UserGroup.findByGroup(group)
                normalSize = UserGroup.findAllByGroup(group).size()
                println normalSize
                UserGroupAPI.delete(userGroup.user.id, group.id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
                println UserGroup.findAllByGroup(group).size()

                println "RESET FROM LDAP"
                result = GroupAPI.resetFromLDAP(JSON.parse(result.data).id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)
                assert 200 == result.code || 409 == result.code
                assert normalSize == UserGroup.findAllByGroup(group).size()

                println "IS IN LDAP"
                result = GroupAPI.isInLDAP(JSON.parse(result.data).id, Infos.SUPERADMINLOGIN,Infos.SUPERADMINPASSWORD)
                assert 200 == result.code
                assert JSON.parse(result.data).result == true
            }
        }
    }

    void testLDAPResetNotLDAPGroup() {

        def ldapDisabled = Holders.getGrailsApplication().config.grails.plugin.springsecurity.ldap.active.toString()=="false"

        if(!ldapDisabled) {
            def group = Group.findByName("BASICGROUP")
            if(group == null) {
                BasicInstanceBuilder.getGroup()
                group = Group.findByName("BASICGROUP")
            }
            def id = group.id
            println "RESET FROM LDAP"
            def result = GroupAPI.resetFromLDAP(id, Infos.SUPERADMINLOGIN, Infos.SUPERADMINPASSWORD)

            assert 404 == result.code
        }


    }

}
