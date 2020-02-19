import grails.util.BuildSettings

// See http://logback.qos.ch/manual/groovy.html for details on configuration
appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%level %logger - %msg%n"
    }
}
//
logger'grails.app.init', INFO
//logger'grails.app.init', DEBUG

logger'grails.app.domain', INFO
//logger'grails.app.domain', DEBUG

logger'grails.app.services', INFO
//logger'grails.app.services', DEBUG

def targetDir = BuildSettings.TARGET_DIR
if (targetDir != null) {
    appender("FULL_STACKTRACE", FileAppender) {
        file = "${targetDir}/stacktrace.log"
        append = true
        encoder(PatternLayoutEncoder) {
            pattern = "%level %logger - %msg%n"
        }
    }
    logger("StackTrace", ERROR, ['FULL_STACKTRACE'], false)
    root(ERROR, ['STDOUT', 'FULL_STACKTRACE'])
}
else {
    root(ERROR, ['STDOUT'])
}
