package com.rxlogix.cache

import com.rxlogix.signal.ProductEventHistory
import grails.test.mixin.TestFor
import spock.lang.Ignore
import spock.lang.Specification
import sun.misc.Cache

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(CacheService)
@Ignore
class CacheServiceSpec extends Specification {
    ProductEventHistory peh1
    ProductEventHistory peh2
    ProductEventHistory peh3

    def setup() {
        peh1 = new ProductEventHistory(productName: "p1", eventName: "e1")
        peh1.id = 1L
        peh2 = new ProductEventHistory(productName: "p2", eventName: "e2")
        peh2.id = 2L
        peh3 = new ProductEventHistory(productName: "p2", eventName: "e2")
        peh3.id = 3L
    }

    def cleanup() {
        CacheService.clearAlertCommentCache()
    }

    void "test for producePEHKey with valid product and event names"() {
        expect:
        CacheService.produceKey("a", "b") == "a".hashCode() + "b".hashCode()
    }

    void "test for producePEHKey with valid product and empty event names"() {
        expect:
        CacheService.produceKey("a", "") == "a".hashCode()
    }

    void "test for producePEHKey with valid product and null event names"() {
        expect:
        CacheService.produceKey("a", null) == "a".hashCode()
    }

    void "test for producePEHKey with empty product and null event names"() {
        expect:
        CacheService.produceKey("", "b") == "b".hashCode()
    }

    void "test for producePEHKey with empty product and empty event names"() {
        expect:
        CacheService.produceKey("", "") == null
    }

    void "test for producePEHKey with null product and null event names"() {
        expect:
        CacheService.produceKey(null, null) == null
    }

    void "test getDispositionListById" (){
        when:
        def result = service.getDispositionListById([])
        then:
        println(result)
    }

}
