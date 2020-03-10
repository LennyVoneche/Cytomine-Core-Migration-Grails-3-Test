package cytomine.core.utils.bootstrap

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

import cytomine.core.security.SecUser
import grails.util.Metadata
import groovy.sql.Sql
import org.apache.commons.lang.RandomStringUtils

/**
 * Cytomine @ ULG
 * User: stevben
 * Date: 13/03/13
 * Time: 11:30
 */
class BootstrapDataService {

    def grailsApplication
    def bootstrapUtilsService
    def dataSource
    def amqpQueueConfigService

    def initData() {
        print getClass().getName() + ' BootstrapDataService : ' + '001' + '\n'
        recreateTableFromNotDomainClass()
        print getClass().getName() + ' BootstrapDataService : ' + '002' + '\n'
        amqpQueueConfigService.initAmqpQueueConfigDefaultValues()
        print getClass().getName() + ' BootstrapDataService : ' + '003' + '\n'

        def imagingServer = bootstrapUtilsService.createNewImagingServer()
        print getClass().getName() + ' BootstrapDataService : ' + '004' + '\n'

        def filters = [
                [name: "Binary", baseUrl: "/vision/process?method=binary&url=", imagingServer: imagingServer],
                [name: "Huang Threshold", baseUrl: "/vision/process?method=huang&url=", imagingServer: imagingServer],
                [name: "Intermodes Threshold", baseUrl: "/vision/process?method=intermodes&url=", imagingServer: imagingServer],
                [name: "IsoData Threshold", baseUrl: "/vision/process?method=isodata&url=", imagingServer: imagingServer],
                [name: "Li Threshold", baseUrl: "/vision/process?method=li&url=", imagingServer: imagingServer],
                [name: "Max Entropy Threshold", baseUrl: "/vision/process?method=maxentropy&url=", imagingServer: imagingServer],
                [name: "Mean Threshold", baseUrl: "/vision/process?method=mean&url=", imagingServer: imagingServer],
                [name: "Minimum Threshold", baseUrl: "/vision/process?method=minimum&url=", imagingServer: imagingServer],
                [name: "MinError(I) Threshold", baseUrl: "/vision/process?method=minerror&url=", imagingServer: imagingServer],
                [name: "Moments Threshold", baseUrl: "/vision/process?method=moments&url=", imagingServer: imagingServer],
                [name: "Otsu Threshold", baseUrl: "/vision/process?method=otsu&url=", imagingServer: imagingServer],
                [name: "Renyi Entropy Threshold", baseUrl: "/vision/process?method=renyientropy&url=", imagingServer: imagingServer],
                [name: "Shanbhag Threshold", baseUrl: "/vision/process?method=shanbhag&url=", imagingServer: imagingServer],
                [name: "Triangle Threshold", baseUrl: "/vision/process?method=triangle&url=", imagingServer: imagingServer],
                [name: "Yen Threshold", baseUrl: "/vision/process?method=yen&url=", imagingServer: imagingServer],
                [name: "Percentile Threshold", baseUrl: "/vision/process?method=percentile&url=", imagingServer: imagingServer],
                [name: "H&E Haematoxylin", baseUrl: "/vision/process?method=he-haematoxylin&url=", imagingServer: imagingServer],
                [name: "H&E Eosin", baseUrl: "/vision/process?method=he-eosin&url=", imagingServer: imagingServer],
                [name: "HDAB Haematoxylin", baseUrl: "/vision/process?method=hdab-haematoxylin&url=", imagingServer: imagingServer],
                [name: "HDAB DAB", baseUrl: "/vision/process?method=hdab-dab&url=", imagingServer: imagingServer],
                [name: "Haematoxylin", baseUrl: "/vision/process?method=haematoxylin&url=", imagingServer: imagingServer],
                [name: "Eosin", baseUrl: "/vision/process?method=eosin&url=", imagingServer: imagingServer],
                [name: "Red (RGB)", baseUrl: "/vision/process?method=r_rgb&url=", imagingServer: imagingServer],
                [name: "Green (RGB)", baseUrl: "/vision/process?method=g_rgb&url=", imagingServer: imagingServer],
                [name: "Blue (RGB)", baseUrl: "/vision/process?method=b_rgb&url=", imagingServer: imagingServer],
                [name: "Cyan (CMY)", baseUrl: "/vision/process?method=c_cmy&url=", imagingServer: imagingServer],
                [name: "Magenta (CMY)", baseUrl: "/vision/process?method=m_cmy&url=", imagingServer: imagingServer],
                [name: "Yellow (CMY)", baseUrl: "/vision/process?method=y_cmy&url=", imagingServer: imagingServer],
        ]
        print getClass().getName() + ' BootstrapDataService : ' + '005' + '\n'

        bootstrapUtilsService.createFilters(filters)
        print getClass().getName() + ' BootstrapDataService : ' + '006' + '\n'

        def IIPMimeSamples = [
                [extension : 'mrxs', mimeType : 'openslide/mrxs'],
                [extension : 'vms', mimeType : 'openslide/vms'],
                [extension : 'tif', mimeType : 'openslide/ventana'],
                [extension : 'tif', mimeType : 'image/tif'],
                [extension : 'tif', mimeType : 'philips/tif'],
                [extension : 'tiff', mimeType : 'image/tiff'],
                [extension : 'tif', mimeType : 'image/pyrtiff'],
                [extension : 'svs', mimeType : 'openslide/svs'],
                [extension : 'jp2', mimeType : 'image/jp2'],
                [extension : 'scn', mimeType : 'openslide/scn'],
                [extension : 'ndpi', mimeType : 'openslide/ndpi'],
                [extension : 'bif', mimeType : 'openslide/bif'],
                [extension : 'zvi', mimeType : 'zeiss/zvi']
        ]
        print getClass().getName() + ' BootstrapDataService : ' + '007' + '\n'

        bootstrapUtilsService.createMimes(IIPMimeSamples)
        print getClass().getName() + ' BootstrapDataService : ' + '008' + '\n'


        def usersSamples = [
                [username: 'ImageServer1', firstname: 'Image', lastname: 'Server', email: Metadata.current.'grails.admin.email'.toString(), group: [[name : "Cytomine"]], password: RandomStringUtils.random(32,  (('A'..'Z') + ('0'..'0')).join().toCharArray()), color: "#FF0000", roles: ["ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER_ADMIN"]],
                [username: 'superadmin', firstname: 'Super', lastname: 'Admin', email: Metadata.current.'grails.admin.email'.toString(), group: [[name: "Cytomine"]], password: Metadata.current.'grails.adminPassword'.toString(), color: "#FF0000", roles: ["ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER_ADMIN"]],
                [username : 'admin', firstname : 'Just an', lastname : 'Admin', email : Metadata.current.'grails.admin.email'.toString(), group : [[name : "Cytomine"]], password : Metadata.current.'grails.adminPassword'.toString(), color : "#FF0000", roles : ["ROLE_USER", "ROLE_ADMIN"]],
                [username: 'rabbitmq', firstname: 'rabbitmq', lastname: 'user', email: Metadata.current.'grails.admin.email'.toString(), group: [[name : "Cytomine"]], password: RandomStringUtils.random(32,  (('A'..'Z') + ('0'..'0')).join().toCharArray()), color: "#FF0000", roles: ["ROLE_USER", "ROLE_SUPER_ADMIN"]],
                [username: 'monitoring', firstname: 'Monitoring', lastname: 'Monitoring', email: Metadata.current.'grails.admin.email'.toString(), group: [[name : "Cytomine"]], password: RandomStringUtils.random(32,  (('A'..'Z') + ('0'..'0')).join().toCharArray()), color: "#FF0000", roles: ["ROLE_USER", "ROLE_SUPER_ADMIN"]]
        ]
        print getClass().getName() + ' BootstrapDataService : ' + '009' + '\n'

        bootstrapUtilsService.createUsers(usersSamples)
        bootstrapUtilsService.createRelation()
        bootstrapUtilsService.createConfigurations()
        print getClass().getName() + ' BootstrapDataService : ' + '010' + '\n'

        SecUser admin = SecUser.findByUsername("admin")
        if(!Metadata.current.'grails.adminPrivateKey'.toString()) {
            throw new IllegalArgumentException("adminPrivateKey must be set!")
        }
        if(!Metadata.current.'grails.adminPublicKey'.toString()) {
            throw new IllegalArgumentException("adminPublicKey must be set!")
        }
        admin.setPrivateKey(Metadata.current.'grails.adminPrivateKey'.toString())
        admin.setPublicKey(Metadata.current.'grails.adminPublicKey'.toString())
        admin.save(flush : true)
        print getClass().getName() + ' BootstrapDataService : ' + '011' + '\n'

        SecUser superAdmin = SecUser.findByUsername("superadmin")
        if(!Metadata.current.'grails.superAdminPrivateKey'.toString()) {
            throw new IllegalArgumentException("superAdminPrivateKey must be set!")
        }
        if(!Metadata.current.'grails.superAdminPublicKey'.toString()) {

            throw new IllegalArgumentException("superAdminPublicKey must be set!")
        }
        print getClass().getName() + ' BootstrapDataService : ' + '012' + '\n'

        superAdmin.setPrivateKey(Metadata.current.'grails.superAdminPrivateKey'.toString())
        superAdmin.setPublicKey(Metadata.current.'grails.superAdminPublicKey'.toString())
        superAdmin.save(flush : true)

        SecUser rabbitMQUser = SecUser.findByUsername("rabbitmq")
        if(!Metadata.current.'grails.rabbitMQPrivateKey'.toString()) {
            throw new IllegalArgumentException("rabbitMQPrivateKey must be set!")
        }
        if(!Metadata.current.'grails.rabbitMQPublicKey'.toString()) {
            throw new IllegalArgumentException("rabbitMQPublicKey must be set!")
        }
        print getClass().getName() + ' BootstrapDataService : ' + '013' + '\n'

        rabbitMQUser.setPrivateKey(Metadata.current.'grails.rabbitMQPrivateKey'.toString())
        rabbitMQUser.setPublicKey(Metadata.current.'grails.rabbitMQPublicKey'.toString())
        rabbitMQUser.save(flush : true)

        bootstrapUtilsService.addDefaultProcessingServer()
        bootstrapUtilsService.addDefaultConstraints()
    }

    public void recreateTableFromNotDomainClass() {
        new Sql(dataSource).executeUpdate("DROP TABLE IF EXISTS  task_comment")
        new Sql(dataSource).executeUpdate("DROP TABLE IF EXISTS  task")

        new Sql(dataSource).executeUpdate("CREATE TABLE task (id bigint,progress bigint,project_id bigint,user_id bigint,print_in_activity boolean)")
        new Sql(dataSource).executeUpdate("CREATE TABLE task_comment (task_id bigint,comment character varying(255),timestamp bigint)")
    }

}
