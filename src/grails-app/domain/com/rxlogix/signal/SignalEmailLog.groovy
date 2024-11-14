package com.rxlogix.signal

class SignalEmailLog {

    static auditable = false

    String assignedTo
    String subject
    String body
    static attachmentable = true
    static mapping = {
        autoTimestamp false
    }

    static constraints = {
        body maxSize: 8000
        assignedTo maxSize: 8000
    }
}