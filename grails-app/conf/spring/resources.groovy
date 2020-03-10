package spring
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


import cytomine.core.ldap.CustomUserContextMapper
import cytomine.core.LogoutEventListener
import cytomine.core.security.CASLdapUserDetailsService
import cytomine.core.security.SimpleUserDetailsService
//import cytomine.core.web.CytomineMultipartHttpServletRequest
import cytomine.web.APIAuthentificationFilters
import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.aop.scope.ScopedProxyFactoryBean
import org.springframework.cache.ehcache.EhCacheFactoryBean
import org.springframework.security.ldap.DefaultSpringSecurityContextSource
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator
import org.springframework.security.ldap.userdetails.LdapUserDetailsService

// Place your Spring DSL code here
beans = {
    LogoutEventListener

    apiAuthentificationFilter(APIAuthentificationFilters) {
        // properties
    }
    print getClass().getName() + ' : ' + '001' + '\n'
//    'multipartResolver'(CytomineMultipartHttpServletRequest) {
//        // Max in memory 100kbytes
//        maxInMemorySize=10240
//
//        //100Gb Max upload size
//        maxUploadSize=102400000000
//    }
    print getClass().getName() + ' : ' + '002' + '\n'

    springConfig.addAlias "springSecurityService", "springSecurityCoreSpringSecurityService"

    //CAS + LDAP STUFF
    def config = SpringSecurityUtils.securityConfig
    SpringSecurityUtils.loadSecondaryConfig 'DefaultLdapSecurityConfig'
    config = SpringSecurityUtils.securityConfig
    print getClass().getName() + ' : ' + config.toString() + '\n'

    if(config.ldap.active){
        print getClass().getName() + ' : ' + 'config.ldap.active' + '\n'

        initialDirContextFactory(DefaultSpringSecurityContextSource,
                config.ldap.context.server){
            userDn = config.ldap.context.managerDn
            password = config.ldap.context.managerPassword
            anonymousReadOnly = config.ldap.context.anonymousReadOnly
        }

        ldapUserSearch(FilterBasedLdapUserSearch,
                config.ldap.search.base,
                config.ldap.search.filter,
                initialDirContextFactory){
        }

        ldapAuthoritiesPopulator(DefaultLdapAuthoritiesPopulator,
                initialDirContextFactory,
                config.ldap.authorities.groupSearchBase){
            groupRoleAttribute = config.ldap.authorities.groupRoleAttribute
            groupSearchFilter = config.ldap.authorities.groupSearchFilter
            searchSubtree = config.ldap.authorities.searchSubtree
            convertToUpperCase = config.ldap.mapper.convertToUpperCase
            ignorePartialResultException = config.ldap.authorities.ignorePartialResultException
        }

        ldapUserDetailsMapper(CustomUserContextMapper)

        ldapUserDetailsService(LdapUserDetailsService,
                ldapUserSearch,
                ldapAuthoritiesPopulator){
            userDetailsMapper = ref('ldapUserDetailsMapper')
        }

        userDetailsService(CASLdapUserDetailsService) {
            ldapUserDetailsService=ref('ldapUserDetailsService')
            grailsApplication = ref('grailsApplication')
        }
    } else {
        print getClass().getName() + ' : ' + '003' + '\n'
        userDetailsService(SimpleUserDetailsService)
    }

    ehcacheAclCache(EhCacheFactoryBean) {
        cacheManager = ref('aclCacheManager')
        cacheName = 'aclCache'
    }

    currentRoleServiceProxy(ScopedProxyFactoryBean) {
        targetBeanName = 'currentRoleService'
        proxyTargetClass = true
    }
    print getClass().getName() + ' : ' + '004' + '\n'

}
