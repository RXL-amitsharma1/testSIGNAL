package unit.com.rxlogix.config

import com.rxlogix.config.LiteratureActivity
import com.rxlogix.config.LiteratureActivityService
import com.rxlogix.dto.AlertLevelDispositionDTO
import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(LiteratureActivityService)
class LiteratureActivityServiceSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test something"() {
        expect:"fix me"
            true
    }

    def "test createLiteratureActivityAlertLevelDisposition() method"(){
        given:
        Map alertMap = [productName:'Test Product']
        AlertLevelDispositionDTO alertLevelDispositionDTO = new AlertLevelDispositionDTO()

        when:
        LiteratureActivity literatureActivity = service.createLiteratureActivityAlertLevelDisposition(alertMap,alertLevelDispositionDTO)

        then:
        alertMap.productName == literatureActivity.productName

    }
}
