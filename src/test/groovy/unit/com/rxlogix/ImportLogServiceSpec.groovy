package com.rxlogix

import com.rxlogix.config.ImportDetail
import com.rxlogix.config.ImportLog
import com.rxlogix.config.PVSState
import com.rxlogix.config.PVSStateValue
import com.rxlogix.config.Priority

import com.rxlogix.signal.AdHocAlert
import com.rxlogix.user.User
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import spock.lang.Ignore
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(ImportLogService)
@Mock([User, AdHocAlert, Priority, PVSState, ImportLog, ImportDetail])
@Ignore
class ImportLogServiceSpec extends Specification {


    def setup() {}

    def cleanup() {}

    void "test save import log with invalid json"() {
        when:
        def log = service.createLog("Import Alert")
        service.saveLog(log, "Errors", null)

        then:
        log.id != null
        log.type == 'Import Alert'
        log.response == 'Errors'

    }
}
