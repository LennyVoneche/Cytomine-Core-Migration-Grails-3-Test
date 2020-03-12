package cytomine.core

import cytomine.core.security.SecUser
import cytomine.core.test.Infos
import cytomine.core.utils.Version
import grails.plugin.springsecurity.SecurityFilterPosition
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Environment
import grails.util.Holders
import grails.util.Metadata

import java.awt.GraphicsEnvironment
import java.lang.management.ManagementFactory

class BootStrap {
    def sequenceService
//    def grantService
    def termService



    def grailsApplication

    def marshallersService
    def indexService
    def triggerService
    def tableService
    def secUserService
    def noSQLCollectionService

    def retrieveErrorsService
    def bootstrapDataService
//
    def bootstrapUtilsService
    def bootstrapOldVersionService
//
    def dataSource
    def sessionFactory



    def init = { servletContext ->

//        //Register API Authentifier
        SpringSecurityUtils.clientRegisterFilter( 'apiAuthentificationFilter', SecurityFilterPosition.DIGEST_AUTH_FILTER.order + 1)
        println "Hibernate version is: ${org.hibernate.Version.getVersionString()}"

        log.info "#############################################################################"
        log.info "#############################################################################"
        log.info "#############################################################################"
        String cytomineWelcomMessage = """
                   _____      _                  _
                  / ____|    | |                (_)
                 | |    _   _| |_ ___  _ __ ___  _ _ __   ___
                 | |   | | | | __/ _ \\| '_ ` _ \\| | '_ \\ / _ \\
                 | |___| |_| | || (_) | | | | | | | | | |  __/
                  \\_____\\__, |\\__\\___/|_| |_| |_|_|_| |_|\\___|
                 |  _ \\  __/ |     | |     | |
                 | |_) ||___/  ___ | |_ ___| |_ _ __ __ _ _ __
                 |  _ < / _ \\ / _ \\| __/ __| __| '__/ _` | '_ \\
                 | |_) | (_) | (_) | |_\\__ \\ |_| | | (_| | |_) |
                 |____/ \\___/ \\___/ \\__|___/\\__|_|  \\__,_| .__/
                                                         | |
                                                         |_|
        """
        log.info cytomineWelcomMessage
        log.info "#############################################################################"
        log.info "#############################################################################"
        log.info "#############################################################################"

        log.info "#############################################################################"
        log.info "Information about configuration"

        [
                "Environment" : Environment.getCurrent().name,
                "Client": Metadata.current.'grails.client'.toString(),
                "Server URL": Metadata.current.'grails.serverURL'.toString(),
                "Current directory": new File( './' ).absolutePath,
                "HeadLess: ": GraphicsEnvironment.isHeadless(),
                "SQL": [url: Holders.config.dataSource.url, user:Holders.config.dataSource.username, password:Holders.config.dataSource.password, driver:Holders.config.dataSource.driverClassName],
                "NOSQL": [host:Holders.config.grails.mongo.host, port:Holders.config.grails.mongo.port, databaseName:Holders.config.grails.mongo.databaseName],
//                "Datasource properties": servletContext.getAttribute(ApplicationAttributes.APPLICATION_CONTEXT).dataSourceUnproxied.properties,
                "JVM Args" : ManagementFactory.getRuntimeMXBean().getInputArguments()
        ].each {
            String st = it.key.toString() + " = " + it.value.toString()
            log.info "##### " + st
        }

        log.info "#############################################################################"
        log.info "#############################################################################"
        log.info "#############################################################################"

        def version = new Version()
        if(version.count()==0) {
            log.info "Version was not set, set to last version"
            version.setCurrentVersion(Long.parseLong(Metadata.current.'info.app.cytomineVersion'.toString()))
        }

        //Initialize marshallers and services
        log.info "init marshaller..."
        marshallersService.initMarshallers()

        //TODO sequenceService is deprecated
        // https://stackoverflow.com/questions/41461283/hibernate-sequence-table-is-generated
        log.info "init sequences..."
        sequenceService.initSequences()

        log.info "init trigger..."
        triggerService.initTrigger()

        log.info "init index..."
        indexService.initIndex()

        //TODO grantService is deprecated
//        log.info "init grant..."
//        grantService.initGrant()

        log.info "init table..."
        tableService.initTable()

        log.info "init term service..."
        termService.initialize() //term service needs userservice and userservice needs termservice => init manualy at bootstrap

        log.info "init retrieve errors hack..."
        retrieveErrorsService.initMethods()

        log.info "Initialize RabbitMQ server..."
        bootstrapUtilsService.initRabbitMq()

        /* Fill data just in test environment*/
        log.info "fill with data..."
        if (Environment.getCurrent() == Environment.TEST) {
            bootstrapDataService.initData()
            noSQLCollectionService.cleanActivityDB()
            def usersSamples = [
                    [username : Infos.ANOTHERLOGIN, firstname : 'Just another', lastname : 'User', email : grailsApplication.config.grails.admin.email, group : [[name : "Cytomine"]], password : grailsApplication.config.grails.adminPassword, color : "#FF0000", roles : ["ROLE_USER", "ROLE_ADMIN","ROLE_SUPER_ADMIN"]]
            ]
            bootstrapUtilsService.createUsers(usersSamples)

//            mockServicesForTests()

        }  else if (SecUser.count() == 0) {
            //if database is empty, put minimal data
            bootstrapDataService.initData()
        }
        log.info 'SecUser.count = ' + SecUser.count().toString()
        bootstrapDataService.initData()

        //set public/private keys for special image server user
        //keys regenerated at each deployment with Docker
        //if keys deleted from external config files for security, keep old keys
        //TODO problems recovering data depending on the environment
        if(Metadata.current.'grails.ImageServerPrivateKey' == null && Metadata.current.'grails.ImageServerPublicKey' == null) {
            SecUser imageServerUser = SecUser.findByUsername("ImageServer1")
            imageServerUser.setPrivateKey(Metadata.current.'grails.ImageServerPrivateKey'.toString())
            imageServerUser.setPublicKey(Metadata.current.'grails.ImageServerPublicKey'.toString())
            imageServerUser.save(flush : true)
        }
        if(Metadata.current.'grails.rabbitMQPrivateKey' == null && Metadata.current.'grails.rabbitMQPublicKey' == null) {
            SecUser rabbitMQUser = SecUser.findByUsername("rabbitmq")
            if(rabbitMQUser) {
                rabbitMQUser.setPrivateKey(Metadata.current.'grails.rabbitMQPrivateKey'.toString())
                rabbitMQUser.setPublicKey(Metadata.current.'grails.rabbitMQPublicKey'.toString())
                rabbitMQUser.save(flush : true)
            }
        }

        log.info "init change for old version..."
        bootstrapOldVersionService.execChangeForOldVersion()

        log.info "create multiple IS and Retrieval..."
        bootstrapUtilsService.createMultipleIS()
        bootstrapUtilsService.createMultipleRetrieval()

        bootstrapUtilsService.fillProjectConnections();
        bootstrapUtilsService.fillImageConsultations();

        bootstrapUtilsService.initProcessingServerQueues()

//        fixPlugins()

        log.info "Fin du Bootstrap"
    }
    //
//    private void mockServicesForTests(){
//        //mock services which use IMS
//        ImageProcessingService.metaClass.getImageFromURL = {
//            String url -> println "\n\n mocked getImageFromURL \n\n";
//                return javax.imageio.ImageIO.read(new File("test/functional/be/cytomine/utils/images/thumb256.png"))
//        }
//        ImageGroupHDF5Service.metaClass.callIMSConversion = {
//            SecUser currentUser, def imagesFilenames, String filename -> println "\n\n mocked callIMSConversion \n\n";
//        }
//        ImageServerService.metaClass.getStorageSpaces = {
//            return [[used : 0, available : 10]]
//        }
//        //mock services which use Retrieval
//        ImageRetrievalService.metaClass.doRetrievalIndex = {
//            String url, String username, String password, def image,String id, String storage, Map<String,String> properties -> println "\n\n mocked doRetrievalIndex \n\n";
//                return [code:200,response:"test"]
//        }
//        //mock mail service
//        CytomineMailService.metaClass.send = {
//            String from, String[] to, String cc, String subject, String message, def attachment -> println "\n\n mocked mail send \n\n";
//        }
//    }
//
//    private void fixPlugins(){
//        //grails resources
//        //for https
//        ResourceProcessor.metaClass.redirectToActualUrl = {
//            ResourceMeta res, HttpServletRequest request, HttpServletResponse response ->
//                String url
//                if (URLUtils.isExternalURL(res.linkUrl)) {
//                    url = res.linkUrl
//
//                } else {
//                    url = grailsApplication.config.grails.serverURL + request.contextPath + staticUrlPrefix + res.linkUrl
//                }
//
//                log.debug "Redirecting ad-hoc resource ${request.requestURI} " +
//                        "to $url which makes it UNCACHEABLE - declare this resource " +
//                        "and use resourceLink/module tags to avoid redirects and enable client-side caching"
//
//                response.sendRedirect url
//        }
}
