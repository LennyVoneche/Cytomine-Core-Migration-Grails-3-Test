package cytomine.core
import grails.plugin.springsecurity.annotation.Secured

class WelcomeController {
    @Secured('ROLE_USER')
    def index() {

        def announcements = Welcome.createCriteria().list {
            order("dateCreated", "desc")
            maxResults(2)
        }
        render announcements.first()?.message
    }
}
