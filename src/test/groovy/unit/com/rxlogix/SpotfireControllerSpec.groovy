package unit.com.rxlogix

import com.rxlogix.SpotfireController
import grails.test.mixin.TestFor
import spock.lang.Ignore
import spock.lang.Specification

@TestFor(SpotfireController)
@Ignore
class SpotfireControllerSpec extends Specification {
    def setup() {

    }

    def cleanup() {
    }

    void "test auth method and session parameters"() {
        when:
        controller.params.client_id = "test"
        controller.params.return_uri = "http://localhost:8080/signal"
        controller.params.csrf_token = "test"
        controller.auth()

        then:
        assert controller.session.spotfire_auth == "true"
        assert controller.session.client_id == "test"
        assert controller.session.return_uri == "http://localhost:8080/signal"
        assert controller.session.csrf_token == "test"
    }

}
