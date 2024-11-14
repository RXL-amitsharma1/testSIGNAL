package com.rxlogix

import com.rxlogix.signal.Alert
import com.rxlogix.user.User
import grails.test.mixin.Mock
import groovy.mock.interceptor.MockFor
import spock.lang.Specification

@Mock([Alert, User])
class AlertbilitySpec extends Specification {
    def Alert alert = Mock()
    def User user1
    def User user2

    def setup() {
        user1 = new User(email: "test1@test.com", username: 'user1', createdBy: "Test", modifiedBy: "Test")
        user1.save()
        user2 = new User(email: "test2@test.com", username: 'user2', createdBy: "Test", modifiedBy: "Test")
        user2.save()
        alert.assignedTo = user1
    }
}
