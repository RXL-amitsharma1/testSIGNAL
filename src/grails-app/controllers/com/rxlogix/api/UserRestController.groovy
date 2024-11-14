package com.rxlogix.api

import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import grails.rest.RestfulController
import grails.util.Holders
import org.springframework.http.HttpStatus
import org.springframework.session.SessionRepository

import java.util.stream.Collectors

class UserRestController extends  RestfulController{

    UserRestController() {
        super(User, true);
    }

    SessionRepository sessionRepository
    def userService

    def fetchUser() {
        String token = request.getHeader("X-Auth-Token")
        log.info("Session id received: " + token)
        def status
        Map result = [:]
        if (token) {
            String decodedSessionId = com.rxlogix.RxCodec.decode(token)
            def activeSession = sessionRepository.getSession(decodedSessionId)
            if (activeSession) {
                User user = User.findByUsername(activeSession.getAttribute("javamelody.remoteUser"))
                if (user) {
                    result.username = user.username
                    result.fullName = user.fullName
                    result.email = user.email
                    result.fullNameAndUsername = user.fullName + " (" + user.username + ")"
                    if (!user.lastToLastLogin) {
                        result.lastToLastLogin = "User Never logged in."
                    } else {
                        result.lastToLastLogin = DateUtil.toDateStringWithTimeInAmPmFormat(user) + userService.getGmtOffset(user.preference.timeZone)
                    }
                    result.externalUser = true
                    result.globalUser = true
                    result.type = Holders.config.grails.plugin.springsecurity.saml.active ? "LDAP User" : ""
                    result.authType = null
                    result.role = user.getAuthorities()?.stream().map({ r -> r.getAuthority() }).collect(Collectors.toList())
                    status = HttpStatus.OK
                    log.debug("User Details: " + result)
                } else {
                    status = HttpStatus.UNAUTHORIZED
                    result.message = "No User found."
                }
            }else{
                status = HttpStatus.UNAUTHORIZED
                result.message = "No Active session found."
            }
        } else {
            status = HttpStatus.UNAUTHORIZED
            result.message = "User do not have Access"
        }
        log.info("fetch user details Completed. " + result.message)

        render(status: status, text: (result as JSON).toString(), contentType: 'application/json')
    }
}
