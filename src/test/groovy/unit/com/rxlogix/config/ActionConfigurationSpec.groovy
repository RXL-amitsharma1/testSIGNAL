package com.rxlogix.config

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import unit.utils.ConstraintUnitSpec

@Mock(ActionConfiguration)
@TestFor(ActionConfiguration)
class ActionConfigurationSpec extends ConstraintUnitSpec {

    def setup() {
        mockForConstraintsTests(ActionConfiguration, [ActionConfiguration.build()])
    }
    /*
    @Unroll("test ActionConfiguration all constraints #field is #error")
    void "test ActionConfiguration all constraints"() {
        when:
        def actionConfiguration = new ActionConfiguration("$field": val)

        then:
        validateConstraints(actionConfiguration, field, error)

        where:
        error      | field               | val
        'nullable' | 'value'             | null
        'nullable' | 'displayName'       | null
        'valid'    | 'displayName_local' | null
        'valid'    | 'description_local' | null
    }
    */
}
