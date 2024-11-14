package com.rxlogix.config

import com.rxlogix.signal.AdHocAlert
import com.rxlogix.signal.Alert
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Ignore
import unit.utils.ConstraintUnitSpec

@TestFor(Alert)
@Mock([AdHocAlert, Priority, User, Group])
@Ignore
class AlertSpec extends ConstraintUnitSpec {

    def setup() {
        new Priority(value: "High", defaultPriority: true, displayName: "High").save()
    }

    def "to test the alert persistence"() {
        setup:
        def adHocAlert = new AdHocAlert(
                productSelection: "something",
                //   detectedDate: new DateTime(2015,12, 15, 0, 0, 0).toDate(),
                name: "Test Name",
                alertVersion: 0,
                initialDataSource: "newspaper",
                topic: "test",
                priority: new Priority(value: "High", displayName: "High"),
                detectedDate: new Date(),
                detectedBy: "user"
        )
        adHocAlert.save(failOnError: true)
        expect:
        adHocAlert.id != null
    }

}
