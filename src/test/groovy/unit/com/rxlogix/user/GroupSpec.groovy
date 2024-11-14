package com.rxlogix.user

import com.rxlogix.config.Disposition
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

/**
 *
 */
@TestFor(Group)
@Mock([User, Group,Disposition])
@Ignore
class GroupSpec extends Specification {
    @Shared
    def user1
    @Shared
    def user2
    @Shared
    def grp1
    Disposition dispositionInstance

    def setup() {
        dispositionInstance = new Disposition(displayName: "Test Dispositon", value: "Test Disposition", id: 1234)
        user1 = new User(username: 'u1', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user1.save()
        user2 = new User(username: 'u2', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user2.save()
        grp1 = new Group(name: 'test group')

        grp1.addToMembers(user1)
        grp1.addToMembers(user2)
        grp1.createdBy = user1.username
        grp1.modifiedBy = user1.username
        grp1.save()
    }

    def cleanup() {}

    def "members testing" () {
        expect:
            grp1.members.sort() == [user1, user2].sort()
    }

    def "allowed product Ids save tests save" () {
        when:
            def grp = Group.findByName('test group')
            grp.allowedProductList.add("1111")
            grp.allowedProductList.add('3333')
            grp.save()

            def grpRst = Group.findByName('test group')
        then:
            grpRst.allowedProd == "1111,3333"
    }

}
