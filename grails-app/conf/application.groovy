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
grails.plugin.springsecurity.userLookup.userDomainClassName = 'cytomine.core.User'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'cytomine.core.UserRole'
grails.plugin.springsecurity.authority.className = 'cytomine.core.Role'
grails.plugin.springsecurity.controllerAnnotations.staticRules = [
	[pattern: '/',               access: ['permitAll']],
	[pattern: '/error',          access: ['permitAll']],
	[pattern: '/index',          access: ['permitAll']],
	[pattern:'/monitoring/**',   access:['permitAll']],
	[pattern: '/index.gsp',      access: ['permitAll']],
	[pattern: '/shutdown',       access: ['permitAll']],
	[pattern: '/assets/**',      access: ['permitAll']],
	[pattern: '/**/js/**',       access: ['permitAll']],
	[pattern: '/**/css/**',      access: ['permitAll']],
	[pattern: '/**/images/**',   access: ['permitAll']],
	[pattern: '/**/favicon.ico', access: ['permitAll']],
	[pattern: '/securityInfo/**', access: ['permitAll']]
]

grails.plugin.springsecurity.filterChain.chainMap = [
	[pattern: '/assets/**',      filters: 'none'],
	[pattern: '/**/js/**',       filters: 'none'],
	[pattern: '/**/css/**',      filters: 'none'],
	[pattern: '/**/images/**',   filters: 'none'],
	[pattern: '/**/favicon.ico', filters: 'none'],
	[pattern: '/**',             filters: 'JOINED_FILTERS']
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