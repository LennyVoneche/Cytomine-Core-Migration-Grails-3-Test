package cytomine.core

import grails.plugins.selenium.SeleniumAware

@Mixin(SeleniumAware)
class WebIndexTests extends GroovyTestCase{

//    void testCytomineTitle() {
//        selenium.open("/");
//        log.info "selenium.title="+selenium.title
//        selenium.title
//        assertTrue(selenium.title.equals("Cytomine"));
//    }
//
//    void testCytomineLogPanel() {
//        selenium.open("/");
//        selenium.waitForTextPresent("Sign in to Cytomine");
//    }
//
//    void testCytomineLogAuth() {
//        selenium.open("/");
//        selenium.type("id=j_username", Infos.ADMINLOGIN)
//        selenium.type("id=j_password", Infos.ADMINPASSWORD)
//        selenium.click("id=submit-login");
//        selenium.waitForTextPresent("Filters");
//    }
//
//    void testCytomineLogNotAuth() {
//        selenium.open("/logout");
//        selenium.open("/");
//        selenium.type("id=j_username", Infos.BADLOGIN)
//        selenium.type("id=j_password", Infos.BADPASSWORD)
//        selenium.click("id=submit-login");
//        selenium.waitForNotTextPresent("Filters");
//    }

    void testJustForNoError() {

    }

}
