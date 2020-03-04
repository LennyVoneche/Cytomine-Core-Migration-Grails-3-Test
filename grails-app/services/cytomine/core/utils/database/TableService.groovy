package cytomine.core.utils.database

import grails.transaction.Transactional

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

import groovy.sql.Sql
import org.postgresql.util.PSQLException

/**
 * Created by IntelliJ IDEA.
 * User: lrollus
 * Date: 7/07/11
 * Time: 15:16
 * Service used to create index at the application begining
 */
@Transactional
class TableService {

    def sessionFactory
    def dataSource
    public final static String SEQ_NAME = "CYTOMINE_SEQ"

    /**
     * Create domain index
     */
    def initTable() {
        log.info "initTable method"

        sessionFactory.getCurrentSession().clear();
        print getClass().getName() + ' initTable : ' + '001' + '\n'

        def connection = sessionFactory.currentSession.connection()
        print getClass().getName() + ' initTable : ' + '002' + '\n'

        try {

            print 'Voici dataSource --> ' + dataSource + ' !!! ' + '\n'
            if(executeSimpleRequest("select character_maximum_length from information_schema.columns where table_name = 'command' and column_name = 'data'")!=null) {
                log.debug "Change type..."
                print getClass().getName() + ' initTable : ' + '002.1' + '\n'
                new Sql(dataSource).executeUpdate("alter table command alter column data type character varying")
            }
            print getClass().getName() + ' initTable : ' + '003' + '\n'

            if(executeSimpleRequest("select character_maximum_length from information_schema.columns where table_name = 'shared_annotation' and column_name = 'comment'")!=null) {
                log.debug "Change type..."
                print getClass().getName() + ' initTable : ' + '003.1' + '\n'
                new Sql(dataSource).executeUpdate("alter table shared_annotation alter column comment type character varying")
            }
            print getClass().getName() + ' initTable : ' + '004' + '\n'

            if(executeSimpleRequest("select character_maximum_length from information_schema.columns where table_name = 'property' and column_name = 'value'")!=null) {
                log.debug "Change type property table..."
                print getClass().getName() + ' initTable : ' + '004.1' + '\n'

                new Sql(dataSource).executeUpdate("alter table property alter column value type character varying")
            }
            print getClass().getName() + ' initTable : ' + '005' + '\n'

            String reqcreate

            reqcreate = "CREATE VIEW user_project AS\n" +
                                "SELECT distinct project.*, sec_user.id as user_id\n" +
                                "FROM project, acl_object_identity, sec_user, acl_sid, acl_entry \n" +
                                "WHERE project.id = acl_object_identity.object_id_identity\n" +
                                "AND acl_sid.sid = sec_user.username\n" +
                                "AND acl_entry.sid = acl_sid.id\n" +
                                "AND acl_entry.acl_object_identity = acl_object_identity.id\n" +
                                "AND sec_user.user_id is null\n" +
                                "AND mask >= 1 AND project.deleted IS NULL"
            createRequest('user_project',reqcreate)
            print getClass().getName() + ' initTable : ' + '006' + '\n'

            reqcreate = "CREATE VIEW admin_project AS\n" +
                                "SELECT distinct project.*, sec_user.id as user_id\n" +
                                "FROM project, acl_object_identity, sec_user, acl_sid, acl_entry \n" +
                                "WHERE project.id = acl_object_identity.object_id_identity\n" +
                                "AND acl_sid.sid = sec_user.username\n" +
                                "AND acl_entry.sid = acl_sid.id\n" +
                                "AND acl_entry.acl_object_identity = acl_object_identity.id\n" +
                                "AND sec_user.user_id is null\n" +
                                "AND mask >= 16 AND project.deleted IS NULL"
            createRequest('admin_project',reqcreate)
            print getClass().getName() + ' initTable : ' + '007' + '\n'

            reqcreate = "CREATE VIEW creator_project AS\n" +
                                "SELECT distinct project.*, sec_user.id as user_id\n" +
                                "FROM project, acl_object_identity, sec_user, acl_sid\n" +
                                "WHERE project.id = acl_object_identity.object_id_identity\n" +
                                "AND acl_sid.sid = sec_user.username\n" +
                                "AND acl_object_identity.owner_sid = acl_sid.id\n" +
                                "AND sec_user.user_id is null AND project.deleted IS NULL"
            createRequest('creator_project',reqcreate)
            print getClass().getName() + ' initTable : ' + '008' + '\n'

            reqcreate = "CREATE VIEW user_image AS\n" +
                    "SELECT distinct image_instance.*, abstract_image.filename, abstract_image.original_filename, project.name as project_name, sec_user.id as user_image_id\n" +
                    "FROM project,  image_instance, abstract_image, acl_object_identity, sec_user, acl_sid, acl_entry \n" +
                    "WHERE project.id = acl_object_identity.object_id_identity\n" +
                    "AND image_instance.deleted IS NULL \n" +
                    "AND project.deleted IS NULL \n" +
                    "AND image_instance.project_id = project.id \n" +
                    "AND image_instance.parent_id IS NULL \n" + //don't get nested images
                    "AND abstract_image.id = image_instance.base_image_id\n" +
                    "AND acl_sid.sid = sec_user.username\n" +
                    "AND acl_entry.sid = acl_sid.id\n" +
                    "AND acl_entry.acl_object_identity = acl_object_identity.id\n" +
                    "AND sec_user.user_id is null\n" +
                    "AND mask >= 1"
            createRequest('user_image',reqcreate)
            print getClass().getName() + ' initTable : ' + '009' + '\n'

        } catch (PSQLException e) {
            log.info "initTable PSQLException method"
            log.info e
        }
    }

    def executeSimpleRequest(String request) {
        def response = null
        log.debug "request = $request"
        print getClass().getName() + ' executeSimpleRequest : ' + '001' + '\n'

        new Sql(dataSource).eachRow(request) {
            print getClass().getName() + ' executeSimpleRequest : ' + '001.1' + '\n'
            log.debug it[0].toString()
            response = it[0]
        }
        print getClass().getName() + ' executeSimpleRequest : ' + '002' + '\n'

        log.debug "response = $response"
        response
    }

    def createRequest(def name,def reqcreate) {
        try {
            print getClass().getName() + ' createRequest : ' + '001' + '\n'

            boolean alreadyExist = false

            new Sql(dataSource).eachRow("select table_name from INFORMATION_SCHEMA.views where table_name like ?",[name]) {
                print getClass().getName() + ' createRequest : ' + '001.1' + '\n'
                alreadyExist = true
            }
            print getClass().getName() + ' createRequest : ' + '002' + '\n'

            if(alreadyExist) {
                def req =  "DROP VIEW " + name
                new Sql(dataSource).execute(req)
                print getClass().getName() + ' createRequest : ' + '002.1' + '\n'

            }
            log.debug reqcreate
            print getClass().getName() + ' createRequest : ' + '004' + '\n'

            new Sql(dataSource).execute(reqcreate)
            print getClass().getName() + ' createRequest : ' + '004' + '\n'


        } catch(Exception e) {
            print getClass().getName() + ' createRequest (Exception) : ' + '005' + '\n'
            log.error e.toString()
        }
    }
}
