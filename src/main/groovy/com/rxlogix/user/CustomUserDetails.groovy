package com.rxlogix.user

import grails.plugin.springsecurity.userdetails.GrailsUser
import org.springframework.security.core.GrantedAuthority
import com.rxlogix.enums.AuthType
import com.rxlogix.enums.UserType

import java.text.SimpleDateFormat

class CustomUserDetails extends GrailsUser {
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    final String fullName
    final String email
    UserType type
    AuthType authType
    transient Date passwordModifiedTime

    CustomUserDetails(String username, String password, boolean enabled,
                        boolean accountNonExpired, boolean credentialsNonExpired,
                        boolean accountNonLocked,
                        Collection<GrantedAuthority> authorities,
                        long id, String fullName, String email) {

        super(username, password?:'', enabled, accountNonExpired,
              credentialsNonExpired, accountNonLocked, authorities, id)

        this.fullName = fullName
        this.email = email
    }

    CustomUserDetails(String username, String password, boolean enabled,
                      boolean accountNonExpired, boolean credentialsNonExpired,
                      boolean accountNonLocked,
                      Collection<GrantedAuthority> authorities,
                      long id, String fullName, String email,
                      UserType userType,
                      AuthType authType,
                      Date passwordModifiedTime) {

        this(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities,
                id, fullName, email)
        this.type = userType
        this.authType = authType
        this.passwordModifiedTime = passwordModifiedTime
        sdf.setTimeZone(TimeZone.default)
    }

}
