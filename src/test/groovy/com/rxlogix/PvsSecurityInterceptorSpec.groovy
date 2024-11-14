package com.rxlogix


import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(PvsSecurityInterceptor)
class PvsSecurityInterceptorSpec extends Specification {

    def setup() {
    }

    def cleanup() {

    }

    void "Test pvsSecurity interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"pvsSecurity")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
