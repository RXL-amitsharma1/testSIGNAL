package com.rxlogix.signal

import com.rxlogix.user.User
import com.rxlogix.util.DateUtil

class AlertCommentHistory implements GroovyInterceptable, Serializable{

    Integer aggAlertId
    String comments
    String alertName
    String period

    String createdBy
    Date dateCreated
    Date lastUpdated
    String modifiedBy

    Long configId
    Long execConfigId
    String productName
    String eventName
    BigInteger productId
    Integer eventId

    static mapping = {
        table("ALERT_COMMENT_HISTORY")
        id generator:'sequence', params:[sequence:'alert_comment_history_sequence']
        comments column: "COMMENTS", type: "text", sqlType: "clob"
        eventId nullable: true
    }

    static constraints = {
        aggAlertId nullable: true
        comments nullable: true
        alertName nullable: true
        period nullable: true
        execConfigId nullable: true
        eventId nullable: true
    }

    def toDto(String userTimezone = "UTC"){
        [
                'id'                : this.id,
                'aggAlertId'        : this.aggAlertId,
                'comments'          : this.comments,
                'alertName'         : this.alertName,
                'period'            : this.period,
                'modifiedBy'        : User.findByUsername(this.modifiedBy)?.fullName,
                'lastUpdated'       : new Date(DateUtil.toDateStringWithTime(this.lastUpdated, userTimezone)).format(DateUtil.DATEPICKER_FORMAT_AM_PM).toString()
        ]
    }
}
