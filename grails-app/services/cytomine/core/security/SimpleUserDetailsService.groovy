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

import grails.plugin.springsecurity.userdetails.GormUserDetailsService
import grails.plugin.springsecurity.userdetails.GrailsUser
import org.springframework.dao.DataAccessException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException

class SimpleUserDetailsService extends GormUserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username, boolean loadRoles) throws UsernameNotFoundException, DataAccessException {
        print getClass().getName() + ' SimpleUserDetailsService : ' + '001' + ' ! \n'
        print getClass().getName() + ' SimpleUserDetailsService : ' + username + ' ! \n'
        SecUser user = SecUser.findByUsernameIlike(username)
        print getClass().getName() + ' SimpleUserDetailsService : ' + user + ' !\n'

        def authorities = []

        def auth = SecUserSecRole.findAllBySecUser(user).collect{new SimpleGrantedAuthority(it.secRole.authority)}
        //by default, we remove the role_admin for the current session
        authorities.addAll(auth.findAll{it.authority!="ROLE_ADMIN"})
        print getClass().getName() + ' SimpleUserDetailsService : ' + '002' + ' ! \n'

        return new GrailsUser(user.username, user.password, user.enabled, !user.accountExpired,
                !user.passwordExpired, !user.accountLocked,
                authorities, user.id)
        print getClass().getName() + ' SimpleUserDetailsService : ' + '003' + ' ! \n'

    }
}