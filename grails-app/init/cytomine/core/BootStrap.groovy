package cytomine.core

class BootStrap {
    def springSecurityService

    def init = { servletContext ->
        new Welcome(message: 'Hello World !').save()

        def authorities = ['ROLE_USER']
        authorities.each {
            if ( !Role.findByAuthority(it) ) {
                new Role(authority: it).save()
            }
        }
        if ( !User.findByUsername('root') ) {
            def u = new User(username: 'root', password: 'root')

            u.save()
            def ur = new UserRole(user: u, role:  Role.findByAuthority('ROLE_USER'))
            ur.save()
        }

    }
    def destroy = {
    }
}
