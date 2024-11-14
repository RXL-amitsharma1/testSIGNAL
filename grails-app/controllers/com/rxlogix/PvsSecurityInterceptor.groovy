package com.rxlogix

import grails.config.Config


class PvsSecurityInterceptor {

    Boolean strictTransportEnabled

    PvsSecurityInterceptor(){
        matchAll()
    }

    void setConfiguration(Config cfg) {
        strictTransportEnabled = cfg.getProperty('pvs.strict.transport.security.enabled', Boolean, false)
    }

    boolean before() {
        response.setHeader("Cache-Control", "no-cache, no-store")
        response.setHeader("Pragma", "no-cache")
        response.setHeader('X-XSS-Protection','1; mode=block')
        response.setHeader('X-Content-Type-Options','nosniff')
        response.setHeader("Content-Security-Policy","script-src 'self' 'unsafe-inline' data: 'unsafe-eval' blob: ;")
        if (strictTransportEnabled && (request.isSecure() || request.getHeader('X-Forwarded-Proto')?.toLowerCase() == 'https')) {
            log.trace("===========Strict-Transport-Security=============")
            response.setHeader('Strict-Transport-Security', "max-age=31536000;includeSubDomains")
        }
        true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
