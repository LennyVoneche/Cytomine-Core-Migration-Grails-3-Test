/******************************************************************************
 * EXTERNAL configuration
 ******************************************************************************/
grails.config.locations = [""]
environments {
	production {
		grails.config.locations = ["file:${userHome}/.grails/cytomineconfig.groovy"]
	}
	development {
		// Update the file path so that it matches the generated configuration file in your bootstrap
		grails.config.locations = ["file:${userHome}/Cytomine/Cytomine-bootstrap/configs/core/cytomineconfig.groovy"]
	}
}
println "External configuration file : ${grails.config.locations}"
File configFile = new File(grails.config.locations.first().minus("file:") as String)
println "Found configuration file ? ${configFile.exists()}"
/******************************************************************************
 * SPRING SECURITY CORE
 ******************************************************************************/
// Added by the Spring Security Core plugin:
grails.plugin.springsecurity.useHttpSessionEventPublisher = true
grails.plugin.springsecurity.userLookup.userDomainClassName = 'cytomine.core.security.SecUser'
grails.plugin.springsecurity.userLookup.passwordPropertyName = 'password'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'cytomine.core.security.SecUserSecRole'
grails.plugin.springsecurity.authority.className = 'cytomine.core.security.SecRole'
grails.plugin.springsecurity.authority.nameField = 'authority'
grails.plugin.springsecurity.projectClass = 'cytomine.core.project.Project'
grails.plugin.springsecurity.rememberMe.parameter = 'remember_me'
grails.plugin.springsecurity.password.algorithm = 'SHA-256'
grails.plugin.springsecurity.password.hash.iterations = 1
grails.plugin.springsecurity.rejectIfNoRule = false
grails.plugin.springsecurity.fii.rejectPublicInvocations = false
grails.plugin.springsecurity.useSwitchUserFilter = true
grails.plugin.springsecurity.securityConfigType = "InterceptUrlMap"

//grails.plugin.springsecurity.interceptUrlMap = [
//		[pattern: '/admin/**',               		access: ['ROLE_ADMIN','ROLE_SUPER_ADMIN']],
//		[pattern: '/admincyto/**',          		access: ['ROLE_ADMIN','ROLE_SUPER_ADMIN']],
//		[pattern: '/monitoring/**',          		access: ['ROLE_ADMIN','ROLE_SUPER_ADMIN']],
//		[pattern: '/j_spring_security_switch_user', access:['ROLE_ADMIN','ROLE_SUPER_ADMIN']],
//		[pattern: '/securityInfo/**',      			access: ['ROLE_ADMIN','ROLE_SUPER_ADMIN']],
//		[pattern: '/api/**',       					access: ['IS_AUTHENTICATED_REMEMBERED']],
//		[pattern: '/lib/**',     					access: ['IS_AUTHENTICATED_ANONYMOUSLY']],
//		[pattern: '/css/**',       					access: ['IS_AUTHENTICATED_ANONYMOUSLY']],
//		[pattern: '/images/**',      				access: ['IS_AUTHENTICATED_ANONYMOUSLY']],
//		[pattern: '/*',   							access: ['permitAll']],
//		[pattern: '/login/**',						access: ['IS_AUTHENTICATED_ANONYMOUSLY']],
//		[pattern: '/logout/**', 					access: ['IS_AUTHENTICATED_ANONYMOUSLY']],
//		[pattern: '/status/**', 					access: ['IS_AUTHENTICATED_ANONYMOUSLY']]
//]

grails.plugin.springsecurity.interceptUrlMap = [
		[pattern: '/admin/**',               		access: ['permitAll']],
		[pattern: '/admincyto/**',          		access: ['permitAll']],
		[pattern: '/monitoring/**',          		access: ['permitAll']],
		[pattern: '/j_spring_security_switch_user', access: ['permitAll']],
		[pattern: '/securityInfo/**',      			access: ['permitAll']],
		[pattern: '/api/**',       					access: ['permitAll']],
		[pattern: '/lib/**',     					access: ['permitAll']],
		[pattern: '/css/**',       					access: ['permitAll']],
		[pattern: '/images/**',      				access: ['permitAll']],
		[pattern: '/*',   							access: ['permitAll']],
		[pattern: '/login/**',						access: ['permitAll']],
		[pattern: '/logout/**', 					access: ['permitAll']],
		[pattern: '/status/**', 					access: ['permitAll']]
]


/******************************************************************************
 * SPRING SECURITY CAS
 ******************************************************************************/
boolean cas = false
if(configFile.exists()) {
	config = new ConfigSlurper().parse(configFile.text)
	cas = config.grails.plugin.springsecurity.cas.active
}

if(cas) {
	println("enable CAS")
	grails.plugin.springsecurity.cas.useSingleSignout = true
	grails.plugin.springsecurity.cas.active = true
	grails.plugin.springsecurity.ldap.active = true
	grails.plugin.springsecurity.logout.afterLogoutUrl =''

} else {
	println("disable CAS")
	grails.plugin.springsecurity.cas.useSingleSignout = false
	grails.plugin.springsecurity.cas.active = false
	grails.plugin.springsecurity.ldap.active = false
	grails.plugin.springsecurity.interceptUrlMap.remove('/*')
}
grails.plugin.springsecurity.cas.loginUri = '/login'
grails.plugin.springsecurity.cas.serverUrlPrefix = ''
grails.plugin.springsecurity.cas.serviceUrl = 'http://localhost:8080/j_spring_cas_security_check'
/******************************************************************************
 * SPRING SECURITY LDAP
 ******************************************************************************/
grails.plugin.springsecurity.auth.loginFormUrl = '/'
grails.plugin.springsecurity.ldap.search.base = ''
grails.plugin.springsecurity.ldap.context.managerDn = ''
grails.plugin.springsecurity.ldap.context.managerPassword = ''
grails.plugin.springsecurity.ldap.context.server = ''
grails.plugin.springsecurity.ldap.authorities.groupSearchBase = ''
grails.plugin.springsecurity.ldap.authorities.ignorePartialResultException = true
grails.plugin.springsecurity.ldap.authorities.retrieveDatabaseRoles = true
grails.plugin.springsecurity.ldap.context.anonymousReadOnly = true
grails.plugin.springsecurity.ldap.mapper.usePassword= true
grails.plugin.springsecurity.ldap.mapper.userDetailsClass= 'inetOrgPerson'// 'org.springframework.security.ldap.userdetails.InetOrgPerson'

grails.plugins.dynamicController.mixins = [
		'com.burtbeckwith.grails.plugins.appinfo.IndexControllerMixin':'com.burtbeckwith.appinfo_test.AdminManageController',
		'com.burtbeckwith.grails.plugins.appinfo.HibernateControllerMixin':'com.burtbeckwith.appinfo_test.AdminManageController',
		'com.burtbeckwith.grails.plugins.appinfo.Log4jControllerMixin':'com.burtbeckwith.appinfo_test.AdminManageController',
		'com.burtbeckwith.grails.plugins.appinfo.SpringControllerMixin':'com.burtbeckwith.appinfo_test.AdminManageController',
		'com.burtbeckwith.grails.plugins.appinfo.MemoryControllerMixin':'com.burtbeckwith.appinfo_test.AdminManageController',
		'com.burtbeckwith.grails.plugins.appinfo.PropertiesControllerMixin':'com.burtbeckwith.appinfo_test.AdminManageController',
		'com.burtbeckwith.grails.plugins.appinfo.ScopesControllerMixin':'com.burtbeckwith.appinfo_test.AdminManageController'
]


// set per-environment serverURL stem for creating absolute links
environments {
	production {
		grails.serverURL = ""
		grails.uploadURL = ""
		grails.retrievalServerURL = []
		grails.plugin.springsecurity.useBasicAuth = false
		grails.resources.adhoc.patterns = ['/images/*', '/js/*','/css/jsondoc/*']
	}
	development {
		grails.serverURL = "http://localhost-core"
		grails.uploadURL = "http://localhost-upload"
		grails.imageServerURL = ["http://localhost-ims"]
		grails.retrievalServerURL = ["http://localhost-retrieval"]
		grails.plugin.springsecurity.useBasicAuth = false
		grails.resources.adhoc.patterns = ['/images/*', '/js/*','/css/jsondoc/*']
		grails.converters.default.pretty.print = true
		grails.resources.processing.enabled = false;
		grails.retrievalUsername = 'cytomine'
		grails.retrievalPassword = 'retrieval_default'
		grails.adminPassword="admin"
		grails.ImageServerPrivateKey="ABC"
		grails.ImageServerPublicKey="DEF"
		grails.adminPrivateKey="GHI"
		grails.adminPublicKey="JKL"
		grails.superAdminPrivateKey="MNO"
		grails.superAdminPublicKey="PQR"
		grails.rabbitMQPrivateKey="STU"
		grails.rabbitMQPublicKey="VWX"
	}
	test {
		grails.serverURL = "http://localhost:8090"
		grails.imageServerURL = ["http://localhost:9080"]
		grails.uploadURL = "http://localhost-upload"
		grails.retrievalServerURL = ["http://localhost-retrieval"]
		grails.plugin.springsecurity.useBasicAuth = true
		grails.plugin.springsecurity.basic.realmName = "Cytomine log"
		grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']

		grails.adminPassword = "password"
		grails.ImageServerPrivateKey=""
		grails.ImageServerPublicKey=""
		grails.adminPrivateKey="XXX"
		grails.adminPublicKey="XXX"
		grails.superAdminPrivateKey="X"
		grails.superAdminPublicKey="X"
	}
}

cytomine.customUI.global = [
		dashboard: ["ALL"],
		search : ["ROLE_ADMIN"],
		project: ["ALL"],
		ontology: ["ROLE_ADMIN"],
		storage : ["ROLE_USER","ROLE_ADMIN"],
		software : ["ROLE_USER", "ROLE_ADMIN"],
		activity : ["ALL"],
		explore : ["ROLE_USER","ROLE_ADMIN"],
		admin : ["ROLE_ADMIN"],
		help : ["ALL"]
]

cytomine.customUI.project = [
		"project-annotations-tab":["ADMIN_PROJECT":true,"CONTRIBUTOR_PROJECT":true],
		"project-images-tab":["ADMIN_PROJECT":true,"CONTRIBUTOR_PROJECT":true],
		"project-imagegroups-tab":["ADMIN_PROJECT":false,"CONTRIBUTOR_PROJECT":false],
		"project-properties-tab":["ADMIN_PROJECT":true,"CONTRIBUTOR_PROJECT":true],
		"project-jobs-tab":["ADMIN_PROJECT":false,"CONTRIBUTOR_PROJECT":false],
		"project-configuration-tab":["ADMIN_PROJECT":true,"CONTRIBUTOR_PROJECT":false],
		"project-usersconfiguration-tab":["ADMIN_PROJECT":true,"CONTRIBUTOR_PROJECT":false],
		"project-explore-spectra-panel":["ADMIN_PROJECT":false,"CONTRIBUTOR_PROJECT":false]
]

grails.admin.email = "info@cytomine.org"
grails.notification.email = ""
grails.notification.password = ""
grails.notification.smtp.host = "smtp.gmail.com"
grails.notification.smtp.port = "587"

grails.client = "NO"

storage_path="/data/images" //default path for image locations
fast_data_path="/data/images" //default path for HDF5 files location (for ex: a SSD)
cytomine.software.path.softwareImages = "/data/softwares/images"

cytomine.middleware.rabbitmq.user = "router"
cytomine.middleware.rabbitmq.password = "router"
grails.messageBrokerServerURL = "rabbitmq:5672"
grails.LTIConsumer = []

//limitations
cytomine.annotation.maxNumberOfPoint = 200
cytomine.maxRequestSize = 10485760

cytomine.jobdata.filesystem = false
cytomine.jobdata.filesystemPath = "algo/data/"
grails.readOnlyProjectsByDefault = false

grails.retrieval.enabled=true
grails.software.enabled=true



dataSource {
	pooled = true
	driverClassName = "org.postgresql.Driver"
//    driverClassName = "com.p6spy.engine.spy.P6SpyDriver" // use this driver to enable p6spy logging
	username = "postgres"
	dialect = org.hibernate.spatial.dialect.postgis.PostgisDialect
	properties {
		//specifies that this tc Server is enabled to be monitored using JMX
		jmxEnabled = true
		//number of connections that are created when the pool is started
		initialSize = 10
		//maximum number of active connections that can be allocated from this pool at the same time
		maxActive = 500
		//minimum number of established connections that should be kept in the pool at all times
		minIdle = 10
		//maximum number of connections that should be kept in the pool at all times
		maxIdle = 500
		//maximum number of milliseconds that the pool will wait
		maxWait = 30000
		//Time in milliseconds to keep this connection
		maxAge = 5 * 60000
		//number of milliseconds to sleep between runs of the idle connection validation/cleaner thread
		timeBetweenEvictionRunsMillis = 5000
		//minimum amount of time an object may sit idle in the pool before it is eligible for eviction
		minEvictableIdleTimeMillis = 60000
	}
}
hibernate {
//  cache.use_second_level_cache = true
//  cache.use_query_cache = true
//    cache.use_second_level_cache = false
//    cache.use_query_cache = false   // Changed to false to be enable the distributed cache
//    cache.provider_class = 'net.sf.ehcache.hibernate.SingletonEhCacheProvider'

	//CLUSTER
//    cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
//    cache.provider_class = 'net.sf.ehcache.hibernate.SingletonEhCacheProvider'
	// hibernate.cache.region.factory_class = 'net.sf.ehcache.hibernate.SingletonEhCacheRegionFactory'
	cache.use_second_level_cache = true
	cache.use_query_cache = false
	//cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory' // Hibernate 3
	cache.region.factory_class = 'org.hibernate.cache.ehcache.EhCacheRegionFactory' // Hibernate 4
	singleSession = true // configure OSIV singleSession mode
}
// environment specific settings
environments {
	scratch {
		dataSource {
			dbCreate = "update"
			url = "jdbc:postgresql://localhost:5432/cytomineempty"
			password = "postgres"
		}
	}
	development {
		dataSource {
			dbCreate = "update"
			url = "jdbc:postgresql://localhost:5432/docker"
			username = "docker"
			password = "docker"
		}
	}
	test {
		dataSource {
			//loggingSql = true
			dbCreate = "create"
			url = "jdbc:postgresql://localhost:5432/docker"
			username = "docker"
			password = "docker"
		}
	}
	production {
		dataSource {
			dbCreate = "update"
			url = "jdbc:postgresql://postgresql:5432/docker"
			username='docker'
			password='docker'
		}
	}
	perf {
		dataSource {
			//loggingSql = true
			dbCreate = "update"
			url = "jdbc:postgresql://localhost:5433/cytomineperf"
			password = "postgres"
		}
	}
	testrun {
		dataSource {
			//loggingSql = true
			dbCreate = "create"
			url = "jdbc:postgresql://localhost:5432/cytominetestrun"
			password = "postgres"
		}
	}
}
grails {
	mongo {
		host = "localhost"
		port = 27017
		databaseName = "cytomine"
		options {
			connectionsPerHost = 10 // The maximum number of connections allowed per host
			threadsAllowedToBlockForConnectionMultiplier = 5 // so it*connectionsPerHost threads can wait for a connection
		}
	}
}
/*
environments {
    test {
        grails {
            mongo {
                databaseName = "cytominetest"
            }
        }
    }
} */
