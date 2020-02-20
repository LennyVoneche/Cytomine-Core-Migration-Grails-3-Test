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


