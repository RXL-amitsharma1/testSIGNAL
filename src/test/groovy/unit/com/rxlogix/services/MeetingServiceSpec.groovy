package com.rxlogix.services

import com.rxlogix.MeetingService
import com.rxlogix.UserService
import com.rxlogix.config.Meeting
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import spock.lang.Ignore
import spock.lang.Specification
import com.rxlogix.user.User
import com.rxlogix.user.Preference


@Mock([Meeting, UserService,User,Preference])
@Ignore
@TestFor(MeetingService)

class MeetingServiceSpec extends Specification {

    UserService userService

    def setup() {
        userService = new UserService()
    }

    def "test isMeetingDateNotPassed method"() {
        when:
        Boolean isMeetingDateNotPassed = service.isMeetingDateNotPassed(meetingDate)

        then:
        isMeetingDateNotPassed == result

        where:
        sno | meetingDate                          | result
        1   | new DateTime().minusDays(5).toDate() | false
        2   | new DateTime().plusDays(5).toDate() | true

    }

    def "test deleteRecurrenceMeetings method"() {
        given:
        Meeting.findAllByLinkingId(_) >> [new Meeting(id: 1, meetingTitle: 'abc')]
        String masterId = 5

        when:
        service.deleteRecurrenceMeetings(masterId)

        then:
        notThrown(Exception)
    }

    def "test attendee logic"(){
        when:
        Map map = service.getMeetingAttendeeList(meetingAttendees)

        then:
        map.attendeeList.join(',') == result1.join(',')
        map.guestList.join(',') == result2.join(',')

        where:
        sno|meetingAttendees|result1|result2
        1|"123,tushar,345,saxena"|[123,345]|["tushar","saxena"]
    }

    def "test generateICSFile "() {
        given:
            Preference preference = new Preference(timeZone: 'UTC')
            def mockUserService = Mock(UserService)
            mockUserService.getUser() >> {
                new User(username: 'ownerUser', preference: preference)
            }
            service.userService = mockUserService
            User owner = new User(username: 'ownerUser')
            User attendee = new User(username: 'attendingUser')
            Meeting meeting = new Meeting(meetingTitle: 'test Meeting', meetingDate: new Date() + 1, meetingOwner: owner, attendees: attendee)

        when:
            File generatedFile = service.generateICSFile(meeting)

        then:
            generatedFile.text != null

    }

}
