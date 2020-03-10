package cytomine.core.utils.bootstrap

import cytomine.core.Exception.InvalidRequestException

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

import cytomine.core.Exception.WrongArgumentException
import cytomine.core.image.AbstractImage
import cytomine.core.image.Mime
import cytomine.core.image.server.*
import cytomine.core.middleware.AmqpQueue
import cytomine.core.middleware.MessageBrokerServer
import cytomine.core.ontology.Relation
import cytomine.core.ontology.RelationTerm
import cytomine.core.processing.ImageFilter
import cytomine.core.processing.ImagingServer
import cytomine.core.processing.ParameterConstraint
import cytomine.core.processing.ProcessingServer
import cytomine.core.security.*
import cytomine.core.social.PersistentImageConsultation
import cytomine.core.social.PersistentProjectConnection
import cytomine.core.utils.Configuration
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Environment
import grails.util.Metadata
import groovy.json.JsonBuilder
import groovy.sql.Sql

/**
 * Cytomine
 * User: stevben
 * Date: 13/03/13
 * Time: 11:59
 */
class BootstrapUtilsService {

    def cytomineService
    def sessionFactory
//    def propertyInstanceMap = org.grails.plugins.DomainClassGrailsPlugin
    def grailsApplication
    def dataSource
    def amqpQueueService
    def amqpQueueConfigService
    def rabbitConnectionService
    def storageService
    def processingServerService
    def configurationService

    public def createUsers(def usersSamples) {
        print getClass().getName() + ' createUsers : ' + '001' + '\n'

        SecRole.findByAuthority("ROLE_USER") ?: new SecRole(authority: "ROLE_USER").save(flush: true)
        SecRole.findByAuthority("ROLE_ADMIN") ?: new SecRole(authority: "ROLE_ADMIN").save(flush: true)
        SecRole.findByAuthority("ROLE_SUPER_ADMIN") ?: new SecRole(authority: "ROLE_SUPER_ADMIN").save(flush: true)
        SecRole.findByAuthority("ROLE_GUEST") ?: new SecRole(authority: "ROLE_GUEST").save(flush: true)
        print getClass().getName() + ' createUsers : ' + '002' + '\n'


        def usersCreated = []
        usersSamples.each { item ->
            String str = null;
            if(str)
                print 'je suis dans le if' + '\n'
            else
                print 'je suis dans le else' + '\n'


            print getClass().getName() + ' createUsers : ' + '002.1' + '\n'
            print 'voici item.username -->'  + item.username + ' !!!' + '\n '
            print 'voici item.firstname -->'  + item.firstname + ' !!!' + '\n '
            print 'voici item.lastname -->'  + item.lastname + ' !!!' + '\n '
            print 'voici item.email -->'  + item.email + ' !!!' + '\n '
            print 'voici item.color -->'  + item.color + ' !!!' + '\n '
            print 'voici item.password -->'  + item.password + ' !!!' + '\n '
            User user = User.findByUsername(item.username)
            print getClass().getName() + ' createUsers : ' + '003' + '\n'
            if (user)  return
            user = new User(
                    username: item.username,
                    firstname: item.firstname,
                    lastname: item.lastname,
                    email: item.email,
                    color: item.color,
                    password: item.password,
                    enabled: true)

            print getClass().getName() + ' createUsers : ' + 'USER INFORMATION ' + '\n'
            print getClass().getName() + ' createUsers : ' +  'username ' + user.getUsername() + ' !!!\n'
            print getClass().getName() + ' createUsers : ' +  'firstname ' + user.getFirstname() + ' !!!\n'
            print getClass().getName() + ' createUsers : ' +  'lastname ' + user.getLastname() + ' !!!\n'
            print getClass().getName() + ' createUsers : ' +  'email ' + user.getEmail() + ' !!!\n'
            print getClass().getName() + ' createUsers : ' +  'color ' + user.getColor() + ' !!!\n'
            print getClass().getName() + ' createUsers : ' +  'password ' + user.getPassword() + ' !!!\n'
            print getClass().getName() + ' createUsers : ' +  'user.toString ' + user.toString() + ' !!!\n'
            print getClass().getName() + ' createUsers : ' + 'USER INFORMATION ' + ' !!!\n'


            print getClass().getName() + ' createUsers : ' + '004' + '\n'

            user.generateKeys()
            print getClass().getName() + ' createUsers : ' + '005' + '\n'


            log.info "Before validating ${user.username}..."
            if (user.validate()) {
                log.info "Creating user ${user.username}..."
                print getClass().getName() + ' createUsers : ' + '006' + '\n'

                try {
                    user.save(flush: true)
                } catch(Exception e) {
                    print getClass().getName() + ' createUsers : ' + '006.1' + '\n'
                    log.info e.toString()
                }
                log.info "Save ${user.username}..."

                usersCreated << user
                print getClass().getName() + ' createUsers : ' + '007' + '\n'

                /* Add Roles */
                item.roles.each { authority ->
                    log.info "Add SecRole " + authority + " for user " + user.username
                    SecRole secRole = SecRole.findByAuthority(authority)
                    if (secRole) SecUserSecRole.create(user, secRole)
                }
            } else {
                log.info("\n\n\n Errors in account boostrap for ${item.username}!\n\n\n")
                user.errors.each {
                    err -> log.info err.toString()
                }
            }
        }
        print getClass().getName() + ' createUsers : ' + '008' + '\n'


        SpringSecurityUtils.reauthenticate "admin", null
        print getClass().getName() + ' createUsers : ' + '009' + '\n'

        usersCreated.each { user ->
            /*Create Storage*/
            storageService.initUserStorage(user)
        }
        return usersCreated
    }

    public def createRelation() {
        def relationSamples = [
                [name: RelationTerm.names.PARENT],
                [name: RelationTerm.names.SYNONYM]
        ]

        log.info("createRelation")
        relationSamples.each { item ->
            if (Relation.findByName(item.name)) return
            def relation = new Relation(name: item.name)
            log.info("create relation=" + relation.name)

            if (relation.validate()) {
                log.info("Creating relation : ${relation.name}...")
                relation.save(flush: true)

            } else {
                log.info("\n\n\n Errors in account boostrap for ${item.name}!\n\n\n")
                relation.errors.each {
                    err -> log.info err
                }

            }
        }
    }

    def createFilters(def filters) {
        print getClass().getName() + ' createFilters : ' + '001' + '\n'

        filters.each {
            print getClass().getName() + ' createFilters : ' + '002' + '\n'

            if (!ImageFilter.findByName(it.name)) {
                print getClass().getName() + ' createFilters : ' + '003' + '\n'

                ImageFilter filter = new ImageFilter(name: it.name, baseUrl: it.baseUrl, imagingServer: it.imagingServer)
                if (filter.validate()) {
                    filter.save(flush:true)
                } else {
                    filter.errors?.each {
                        log.info it
                    }
                }
            }
        }
        print getClass().getName() + ' createFilters : ' + '005' + '\n'

    }

    public def createMimes(def mimeSamples) {
        print getClass().getName() + ' createMimes : ' + '001' + '\n'

        mimeSamples.each {
            print getClass().getName() + ' createMimes : ' + '002' + '\n'

            if(!Mime.findByMimeType(it.mimeType)) {
                print getClass().getName() + ' createMimes : ' + '003' + '\n'

                Mime mime = new Mime(extension : it.extension, mimeType: it.mimeType)
                if (mime.validate()) {
                    mime.save(flush:true)
                } else {
                    mime.errors?.each {
                        log.info it
                    }
                }
            }

        }
        print getClass().getName() + ' createMimes : ' + '004' + '\n'

    }

    def createConfigurations(){
        Configuration.Role adminRole = Configuration.Role.ADMIN
        Configuration.Role allUsers = Configuration.Role.ALL
        print getClass().getName() + ' createConfigurations : ' + '001' + '\n'
        print getClass().getName() + ' createConfigurations : ' + grailsApplication.config.grails.retrieval.enabled + '\n'
        print getClass().getName() + ' createConfigurations : ' + grailsApplication.config.grails.admin.email + '\n'
        print getClass().getName() + ' createConfigurations : ' + grailsApplication.config.grails.notification.email + '\n'
        print getClass().getName() + ' createConfigurations : ' + grailsApplication.config.grails.notification.password + '\n'
        print getClass().getName() + ' createConfigurations : ' + grailsApplication.config.grails.notification.smtp.host + '\n'
        print getClass().getName() + ' createConfigurations : ' + grailsApplication.config.grails.notification.smtp.port + '\n'


        def configs = []

        configs << new Configuration(key: "WELCOME", value: "<p>Welcome to the Cytomine software.</p><p>This software is supported by the <a href='https://cytomine.coop'>Cytomine company</a></p>", readingRole: allUsers)

        configs << new Configuration(key: "retrieval.enabled", value: grailsApplication.config.grails.retrieval.enabled, readingRole: allUsers)

        configs << new Configuration(key: "admin.email", value: grailsApplication.config.grails.admin.email, readingRole: adminRole)

        //SMTP values
        configs << new Configuration(key: "notification.email", value: grailsApplication.config.grails.notification.email, readingRole: adminRole)
        configs << new Configuration(key: "notification.password", value: grailsApplication.config.grails.notification.password, readingRole: adminRole)
        configs << new Configuration(key: "notification.smtp.host", value: grailsApplication.config.grails.notification.smtp.host, readingRole: adminRole)
        configs << new Configuration(key: "notification.smtp.port", value: grailsApplication.config.grails.notification.smtp.port, readingRole: adminRole)


        //Default project values
        //configs << new Configuration(key: , value: , readingRole: )

        //LDAP values
        configs << new Configuration(key: "ldap.active", value: grailsApplication.config.grails.plugin.springsecurity.ldap.active, readingRole: allUsers)
        if(grailsApplication.config.grails.plugin.springsecurity.ldap.active) {
            configs << new Configuration(key: "ldap.context.server", value: grailsApplication.config.grails.plugin.springsecurity.ldap.context.server, readingRole: adminRole)
            configs << new Configuration(key: "ldap.search.base", value: grailsApplication.config.grails.plugin.springsecurity.ldap.search.base, readingRole: adminRole)
            configs << new Configuration(key: "ldap.context.managerDn", value: grailsApplication.config.grails.plugin.springsecurity.ldap.context.managerDn, readingRole: adminRole)
            configs << new Configuration(key: "ldap.context.managerPassword", value: grailsApplication.config.grails.plugin.springsecurity.ldap.context.managerPassword, readingRole: adminRole)
            //grails.plugin.springsecurity.ldap.authorities.groupSearchBase = ''
        }

        //LTI values
        //grailsApplication.config.grails.LTIConsumer.each{}
        //add key secret and name
        //role invited user values


        configs.each { config ->
            if (config.validate()) {
                config.save()
            } else {
                config.errors.each {
                    log.error it.toString()
                }
            }
        }
    }

    def saveDomain(def newObject, boolean flush = true) {
        if (!newObject.validate()) {
            log.error newObject.errors
            log.error newObject.retrieveErrors().toString()
            throw new WrongArgumentException(newObject.retrieveErrors().toString())
        }
        if (!newObject.save(flush: flush)) {
            throw new InvalidRequestException(newObject.retrieveErrors().toString())
        }
    }

    def createMultipleRetrieval() {
//        Configuration retrieval = Configuration.findByKey("retrieval.enabled")
//        if(retrieval && retrieval.value.equals("false")){
        print getClass().getName() + ' createMultipleRetrieval : ' + '001' + '\n'

        if (!grailsApplication.config.grails.retrieval.enabled) {
            RetrievalServer.list().each { server ->
                server.delete()
            }
            return
        }
        print getClass().getName() + ' createMultipleRetrieval : ' + '002' + '\n'

        RetrievalServer.list().each { server ->
            if(!Metadata.current.'grails.retrievalServerURL'.toString().contains(server.url)) {
                log.info server.url + " is not in config, drop it"
                log.info "delete Retrieval $server"
                server.delete()
            }

        }
        print getClass().getName() + ' createMultipleRetrieval : ' + '003' + '\n'

        if (Environment.getCurrent() != Environment.TEST) {
            print getClass().getName() + ' createMultipleRetrieval : ' + '004' + '\n'

            Metadata.current.'grails.retrievalServerURL'.toString().eachWithIndex { it, index ->

                if (!RetrievalServer.findByUrl(it)) {
                    print getClass().getName() + ' createMultipleRetrieval : ' + '005' + '\n'
                    RetrievalServer server =
                            new RetrievalServer(
                                    description: "retrieval $index",
                                    url: "${it}",
                                    path: '/retrieval-web/api/resource.json',
                                    username: grailsApplication.config.grails.retrievalUsername,
                                    password: grailsApplication.config.grails.retrievalPassword
                            )
                    if (server.validate()) {
                        server.save()
                    } else {
                        server.errors?.each {
                            log.info it
                        }
                    }
                }
                print getClass().getName() + ' createMultipleRetrieval : ' + '006' + '\n'
            }
        }
    }

    def createMultipleIS() {
        print getClass().getName() + ' : ' + Metadata.current.'grails.imageServerURL'.toString() + '\n'

        ImageServer.list().each { server ->
            if(Metadata.current.'grails.imageServerURL'.toString() != server.url) {
                log.info server.url + " is not in config, drop it"
                MimeImageServer.findAllByImageServer(server).each {
                    log.info "delete $it"
                    it.delete()
                }

                ImageServerStorage.findAllByImageServer(server).each {
                    log.info "delete $it"
                    it.delete()
                }
                log.info "delete IS $server"
                server.delete()
            }

        }

        print getClass().getName() + ' createMultipleIS : ' + '001' + '\n'

        Metadata.current.'grails.imageServerURL'.eachWithIndex { it, index ->
            createNewIS(index.toString(), it as String)
        }
    }


    def createNewIS(String name = "", String url) {

        if(!ImageServer.findByUrl(url)) {
            log.info "Create new IMS: $url"
            print getClass().getName() + ' createNewIS : ' + '001' + '\n'

            def IIPImageServer = [className : 'IIPResolver', name : 'IIP'+name, service : '/image/tile', url : url, available : true]
            ImageServer imageServer = new ImageServer(
                    className: IIPImageServer.className,
                    name: IIPImageServer.name,
                    service : IIPImageServer.service,
                    url : IIPImageServer.url,
                    available : IIPImageServer.available
            )
            print getClass().getName() + ' createNewIS : ' + '002' + '\n'

            if (imageServer.validate()) {
                imageServer.save()
            } else {
                imageServer.errors?.each {
                    log.info it
                }
            }
            print getClass().getName() + ' createNewIS : ' + '003' + '\n'

            Storage.list().each {
                print getClass().getName() + ' createNewIS : ' + '003.1' + '\n'
                new ImageServerStorage(
                        storage : it,
                        imageServer: imageServer
                ).save()
            }
            print getClass().getName() + ' createNewIS : ' + '004' + '\n'

            Mime.list().each {
                print getClass().getName() + ' createNewIS : ' + '004.1' + '\n'
                new MimeImageServer(
                        mime : it,
                        imageServer: imageServer
                ).save()
            }
            print getClass().getName() + ' createNewIS : ' + '005' + '\n'

        }
    }

    def createNewImagingServer() {
        def url = grailsApplication.config.grails.imageServerURL[0]
        def imagingServer = ImagingServer.findByUrl(url)
        if(!imagingServer) {
            return new ImagingServer(url: grailsApplication.config.grails.imageServerURL[0]).save(flush: true,
                    failOnError: true)
        }

        return imagingServer
    }

    void convertMimeTypes(){
        SpringSecurityUtils.doWithAuth("admin", {

            Mime oldTif = Mime.findByMimeType("image/tif");
            Mime oldTiff = Mime.findByMimeType("image/tiff");
            Mime newTiff = Mime.findByMimeType("image/pyrtiff");

            List<AbstractImage> abstractImages = AbstractImage.findAllByMimeInList([oldTif, oldTiff]);
            log.info "images to convert : "+abstractImages.size()

            abstractImages.each {
                it.mime = newTiff;
                it.save();
            }
        })
    }

    void initRabbitMq() {
        log.info "init RabbitMQ connection..."
        MessageBrokerServer mbs = MessageBrokerServer.first()
        boolean toUpdate = false
        print getClass().getName() + ' : ' + '001' + '\n'

        MessageBrokerServer.list().each { messageBroker ->
            if(!grailsApplication.config.grails.messageBrokerServerURL.equals(messageBroker.host+":"+messageBroker.port)) {
                toUpdate = true
                log.info messageBroker.host + "is not in config, drop it"
                log.info "delete Message Broker Server " + messageBroker
                messageBroker.delete(flush: true)
            }
        }

        print getClass().getName() + ' : ' + '002' + '\n'

        String messageBrokerURL = grailsApplication.config.grails.messageBrokerServerURL
        def splittedURL = messageBrokerURL.split(':')
        print getClass().getName() + ' : ' + '002' + '\n'

        if(toUpdate || (mbs == null)) {
            // create MBS
            mbs = new MessageBrokerServer(name: "MessageBrokerServer", host: splittedURL[0], port: splittedURL[1].toInteger())
            if (mbs.validate()) {
                mbs.save()
            } else {
                mbs.errors?.each {
                    log.info it
                }
            }
            print getClass().getName() + ' : ' + '003' + '\n'

            // Update the queues
            AmqpQueue.findAll().each {
                it.host = mbs.host
                it.save(failOnError:true)
                if(!amqpQueueService.checkRabbitQueueExists("queueCommunication",mbs)) {
                    AmqpQueue queueCommunication = amqpQueueService.read("queueCommunication")
                    amqpQueueService.createAmqpQueueDefault(queueCommunication)
                }
            }
            print getClass().getName() + ' : ' + '004' + '\n'

        }

        // Initialize default configurations for amqp queues
        amqpQueueConfigService.initAmqpQueueConfigDefaultValues()
        print getClass().getName() + ' : ' + '005' + '\n'

        // Initialize RabbitMQ queue to communicate software added
        if(!AmqpQueue.findByName("queueCommunication")) {
            print getClass().getName() + ' : ' + '005.1' + '\n'
            AmqpQueue queueCommunication = new AmqpQueue(name: "queueCommunication", host: mbs.host, exchange: "exchangeCommunication")
            queueCommunication.save(failOnError: true, flush: true)
            amqpQueueService.createAmqpQueueDefault(queueCommunication)
        }
        else if(!amqpQueueService.checkRabbitQueueExists("queueCommunication",mbs)) {
            print getClass().getName() + ' : ' + '005.2' + '\n'
            AmqpQueue queueCommunication = amqpQueueService.read("queueCommunication")
            amqpQueueService.createAmqpQueueDefault(queueCommunication)
        }
        print getClass().getName() + ' : ' + '006' + '\n'
        //Inserting a MessageBrokerServer for testing purpose
        if (Environment.getCurrent() == Environment.TEST) {
            rabbitConnectionService.getRabbitConnection(mbs)
        }
        print getClass().getName() + ' : ' + '007' + '\n'
    }

    void initProcessingServerQueues() {
        log.info "init RabbitMQ connection for processing servers..."
        MessageBrokerServer mbs = MessageBrokerServer.first()
        ProcessingServer.list().each {
            if (it.name != null) {
                String queueName = amqpQueueService.queuePrefixProcessingServer + ((it as ProcessingServer).name).capitalize()
                if(!amqpQueueService.checkAmqpQueueDomainExists(queueName)) {
                    String exchangeName = amqpQueueService.exchangePrefixProcessingServer + ((it as ProcessingServer).name).capitalize()
                    String brokerServerURL = (MessageBrokerServer.findByName("MessageBrokerServer")).host
                    AmqpQueue aq = new AmqpQueue(name: queueName, host: brokerServerURL, exchange: exchangeName)
                    aq.save(failOnError: true)
                }
                if(!amqpQueueService.checkRabbitQueueExists(queueName,mbs)) {
                    AmqpQueue aq = amqpQueueService.read(queueName)

                    // Creates the queue on the rabbit server
                    amqpQueueService.createAmqpQueueDefault(aq)

                    // Notify the queueCommunication that a software has been added
                    def mapInfosQueue = [name: aq.name, host: aq.host, exchange: aq.exchange]
                    JsonBuilder builder = new JsonBuilder()
                    builder(mapInfosQueue)
                    amqpQueueService.publishMessage(AmqpQueue.findByName("queueCommunication"), builder.toString())
                }
            }
        }
    }

    def mongo
    def noSQLCollectionService
    def imageConsultationService
    void fillProjectConnections() {
        print getClass().getName() + ' fillProjectConnections : ' + '001' + '\n'

        SpringSecurityUtils.doWithAuth("superadmin", {
            Date before = new Date();
            print getClass().getName() + ' fillProjectConnections : ' + '002' + '\n'

            def connections = PersistentProjectConnection.findAllByTimeIsNullOrCountCreatedAnnotationsIsNullOrCountViewedImagesIsNull(sort: 'created', order: 'desc', max: Integer.MAX_VALUE)
            log.info "project connections to update " + connections.size().toString()
            print getClass().getName() + ' fillProjectConnections : ' + '003' + '\n'

            def sql = new Sql(dataSource)

            for (PersistentProjectConnection projectConnection : connections) {
                Date after = projectConnection.created;
                print getClass().getName() + ' fillProjectConnections : ' + '003' + '\n'

                // collect {it.created.getTime} is really slow. I just want the getTime of PersistentConnection
                def db = mongo.getDB(noSQLCollectionService.getDatabaseName())
                def lastConnection = db.persistentConnection.aggregate(
                        [$match: [project: projectConnection.project, user: projectConnection.user, $and : [[created: [$gte: after]],[created: [$lte: before]]]]],
                        [$sort: [created: 1]],
                        [$project: [dateInMillis: [$subtract: ['$created', new Date(0L)]]]]
                );

                def continuousConnections = lastConnection.results().collect { it.dateInMillis }

                //we calculate the gaps between connections to identify the period of non activity
                def continuousConnectionIntervals = []

                continuousConnections.inject(projectConnection.created.time) { result, i ->
                    continuousConnectionIntervals << (i-result)
                    i
                }

                projectConnection.time = continuousConnectionIntervals.split{it < 30000}[0].sum()
                if(projectConnection.time == null) projectConnection.time=0;
                print getClass().getName() + ' fillProjectConnections : ' + '004' + '\n'

                // count viewed images
                projectConnection.countViewedImages = imageConsultationService.getImagesOfUsersByProjectBetween(projectConnection.user, projectConnection.project,after, before).size()

                db.persistentImageConsultation.update(
                        [$and :[ [project:projectConnection.project],[user:projectConnection.user],[created:[$gte:after]],[created:[$lte:before]]]],
                        [$set: [projectConnection: projectConnection.id]])

                // count created annotations
                String request = "SELECT COUNT(*) FROM user_annotation a WHERE a.project_id = ${projectConnection.project} AND a.user_id = ${projectConnection.user} AND a.created < '${before}' AND a.created > '${after}'"
                print getClass().getName() + ' fillProjectConnections : ' + '005' + '\n'

                sql.eachRow(request) {
                    projectConnection.countCreatedAnnotations = it[0];
                }

                projectConnection.save(flush : true, failOnError: true)
                before = projectConnection.created
            }
            print getClass().getName() + ' fillProjectConnections : ' + '006' + '\n'

            sql.close()
        });
    }
    void fillImageConsultations() {
        SpringSecurityUtils.doWithAuth("superadmin", {
            Date before = new Date();
            print getClass().getName() + ' fillImageConsultations : ' + '001' + '\n'

            def consultations = PersistentImageConsultation.findAllByTimeIsNullOrCountCreatedAnnotationsIsNull(sort: 'created', order: 'desc', max: Integer.MAX_VALUE)
            log.info "image consultations to update " + consultations.size().toString()
            print getClass().getName() + ' fillImageConsultations : ' + '002' + '\n'

            def sql = new Sql(dataSource)

            for (PersistentImageConsultation consultation : consultations) {
                Date after = consultation.created;
                print getClass().getName() + ' fillImageConsultations : ' + '003' + '\n'

                // collect {it.created.getTime} is really slow. I just want the getTime of PersistentConnection
                def db = mongo.getDB(noSQLCollectionService.getDatabaseName())
                def positions = db.persistentUserPosition.aggregate(
                        [$match: [project: consultation.project, user: consultation.user, image: consultation.image, $and : [[created: [$gte: after]],[created: [$lte: before]]]]],
                        [$sort: [created: 1]],
                        [$project: [dateInMillis: [$subtract: ['$created', new Date(0L)]]]]
                );

                def continuousConnections = positions.results().collect { it.dateInMillis }
                print getClass().getName() + ' fillImageConsultations : ' + '003' + '\n'

                //we calculate the gaps between connections to identify the period of non activity
                def continuousConnectionIntervals = []

                continuousConnections.inject(consultation.created.time) { result, i ->
                    continuousConnectionIntervals << (i-result)
                    i
                }

                consultation.time = continuousConnectionIntervals.split{it < 30000}[0].sum()
                if(consultation.time == null) consultation.time=0;

                // count created annotations
                String request = "SELECT COUNT(*) FROM user_annotation a WHERE " +
                        "a.project_id = ${consultation.project} " +
                        "AND a.user_id = ${consultation.user} " +
                        "AND a.image_id = ${consultation.image} " +
                        "AND a.created < '${before}' AND a.created > '${after}'"

                sql.eachRow(request) {
                    consultation.countCreatedAnnotations = it[0];
                }
                print getClass().getName() + ' fillImageConsultations : ' + '004' + '\n'

                consultation.save(flush : true, failOnError: true)
                before = consultation.created
            }
            print getClass().getName() + ' fillImageConsultations : ' + '005' + '\n'

            sql.close()
        });
    }

    public void cleanUpGorm() {
        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
        propertyInstanceMap.get().clear()
    }

    void addDefaultProcessingServer() {
        log.info("Add the default processing server")

        SpringSecurityUtils.doWithAuth {
            if (!ProcessingServer.findByName("local-server")) {
                ProcessingServer processingServer = new ProcessingServer(
                        name: "local-server",
                        host: "slurm",
                        username: "cytomine",
                        port: 22,
                        type: "cpu",
                        processingMethodName: "SlurmProcessingMethod",
                        persistentDirectory: grailsApplication.config.cytomine.software.path.softwareImages,
                        index: 1
                )

                String processingServerName = processingServer.name.capitalize()
                String queueName = amqpQueueService.queuePrefixProcessingServer + processingServerName

                if (!amqpQueueService.checkAmqpQueueDomainExists(queueName)) {
                    // Creation of the default processing server queue
                    String exchangeName = amqpQueueService.exchangePrefixProcessingServer + processingServerName
                    String brokerServerURL = (MessageBrokerServer.findByName("MessageBrokerServer")).host
                    AmqpQueue amqpQueue = new AmqpQueue(name: queueName, host: brokerServerURL, exchange: exchangeName)
                    amqpQueue.save(flush: true, failOnError: true)

                    amqpQueueService.createAmqpQueueDefault(amqpQueue)

                    // Associates the processing server to an amqp queue
                    processingServer.amqpQueue = amqpQueue
                    processingServer.save(flush: true)

                    // Sends a message on the communication queue to warn the software router a new queue has been created
                    def message = [requestType: "addProcessingServer",
                                   name: amqpQueue.name,
                                   host: amqpQueue.host,
                                   exchange: amqpQueue.exchange,
                                   processingServerId: processingServer.id]

                    JsonBuilder jsonBuilder = new JsonBuilder()
                    jsonBuilder(message)

                    amqpQueueService.publishMessage(AmqpQueue.findByName("queueCommunication"), jsonBuilder.toString())
                }
            }
        }
    }

    void addDefaultConstraints() {
        log.info("Add the default constraints")

        SpringSecurityUtils.doWithAuth {
            def constraints = []

            // "Number" dataType
            log.info("Add Number constraints")
            constraints.add(new ParameterConstraint(name: "integer", expression: '("[value]".isInteger()', dataType: "Number"))
            constraints.add(new ParameterConstraint(name: "minimum", expression: '(Double.valueOf("[parameterValue]") as Number) <= (Double.valueOf("[value]") as Number)', dataType: "Number"))
            constraints.add(new ParameterConstraint(name: "maximum", expression: '(Double.valueOf("[parameterValue]") as Number) >= (Double.valueOf("[value]") as Number)', dataType: "Number"))
            constraints.add(new ParameterConstraint(name: "equals", expression: '(Double.valueOf("[parameterValue]") as Number) == (Double.valueOf("[value]") as Number)', dataType: "Number"))
            constraints.add(new ParameterConstraint(name: "in", expression: '"[value]".tokenize("[separator]").find { elem -> (Double.valueOf(elem) as Number) == (Double.valueOf("[parameterValue]") as Number) } != null', dataType: "Number"))

            // "String" dataType
            log.info("Add String constraints")
            constraints.add(new ParameterConstraint(name: "minimum", expression: '"[parameterValue]".length() < [value]', dataType: "String"))
            constraints.add(new ParameterConstraint(name: "maximum", expression: '"[parameterValue]".length() > [value]', dataType: "String"))
            constraints.add(new ParameterConstraint(name: "equals", expression: '"[parameterValue]" == "[value]"', dataType: "String"))
            constraints.add(new ParameterConstraint(name: "in", expression: '"[value]".tokenize("[separator]").contains("[parameterValue]")', dataType: "String"))

            // "Boolean" dataType
            log.info("Add Boolean constraints")
            constraints.add(new ParameterConstraint(name: "equals", expression: 'Boolean.parseBoolean("[value]") == Boolean.parseBoolean("[parameterValue]")', dataType: "Boolean"))

            // "Date" dataType
            log.info("Add Date constraints")
            constraints.add(new ParameterConstraint(name: "minimum", expression: 'new Date().parse("HH:mm:ss", "[parameterValue]").format("HH:mm:ss") < new Date().parse("HH:mm:ss", "[value]").format("HH:mm:ss")', dataType: "Date"))
            constraints.add(new ParameterConstraint(name: "maximum", expression: 'new Date().parse("HH:mm:ss", "[parameterValue]").format("HH:mm:ss") > new Date().parse("HH:mm:ss", "[value]").format("HH:mm:ss")', dataType: "Date"))
            constraints.add(new ParameterConstraint(name: "equals", expression: 'new Date().parse("HH:mm:ss", "[parameterValue]").format("HH:mm:ss") == new Date().parse("HH:mm:ss", "[value]").format("HH:mm:ss")', dataType: "Date"))
            constraints.add(new ParameterConstraint(name: "in", expression: '"[value]".tokenize("[separator]").contains("[parameterValue]")', dataType: "Date"))

            // dateMin, dateMax, timeEquals, timeIn
            constraints.each { constraint ->
                if (!ParameterConstraint.findByNameAndDataType(constraint.name as String, constraint.dataType as String)) {
                    constraint.save(flush: true)
                }
            }

        }
    }

}
