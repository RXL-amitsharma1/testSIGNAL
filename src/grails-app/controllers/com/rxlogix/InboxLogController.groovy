package com.rxlogix

import com.rxlogix.dto.ResponseDTO
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.User
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

import static java.util.Calendar.MONTH

@Secured(["isAuthenticated()"])
class InboxLogController {
    def userService
    def messageSource
    def dataSource
    def cacheService

    def index() {

        def list = InboxLog.findAllByInboxUserIdAndIsDeleted(userService.getUser().getId(), false)
        def user = userService.getUser()
        String timezone = user?.preference?.getTimeZone()

        def activeFilter = "today"
        Date compareTo = new Date()
        if(params.dueIn == "lastWeek"){
            activeFilter = "lastWeek"
            compareTo = compareTo - 7
        }else if(params.dueIn == "lastMonth"){
            activeFilter = "lastMonth"
            def prevMonth = compareTo[MONTH] - 1
            compareTo.set(month:prevMonth)
        }else if(params.dueIn == "all"){
            activeFilter = "all"
        }
        def  inboxlist= list.findAll() {
            if (params.dueIn) {
                if(params.dueIn == "all"){
                    return it
                }
                if ((it.createdOn.clone().clearTime().after(compareTo.clearTime()) || it.createdOn.clone().clearTime() == compareTo.clearTime()) && !it.isDeleted) {
                    it
                }
            } else {
                if (it.createdOn.clone().clearTime() == new Date().clearTime() && !it.isDeleted) {
                    it
                }
            }
        }.sort{it.createdOn}.collect {
            it.toDTO(timezone)
        }

        inboxlist = inboxlist.reverse()
        [inboxList: inboxlist,activeFilter:activeFilter]
    }

    def list(){
        String timezone = userService.getCurrentUserPreference()?.getTimeZone()
        def list = InboxLog.findAll().collect{
            it.toDTO(timezone)
        }


        render(list as JSON)
    }

    def forUser() {
        Long id = params.long("id")
        if (id) {
            def notificationList = InboxLog.createCriteria().list {
                        eq("inboxUserId", id)
                        eq("isRead", false)
                        eq("isDeleted", false)
                        order("createdOn", "desc")
                        maxResults(100)
                    }.collect {
                        def val = [it.messageArgs]
                        User user = cacheService.getUserByUserId(id)
                        Locale locale = cacheService.getPreferenceByUserId(user.id)?.locale
                        String message
                        if(it.type==Constants.SignalHistory.SIGNAL_TYPE)
                             message= Constants.SignalHistory.SIGNAL_CREATED
                        else message = (it?.executedConfigId) ? messageSource.getMessage(it.message, val.toArray(), locale) : it.subject
                        [id  : it.id, message: message, type: it.type,content: it.content,
                         user: user, createdOn: it.createdOn, level: it.level?.name, executedConfigId: it.executedConfigId, detailUrl: it.detailUrl]
                    }
            respond notificationList, [formats: ['json']]
        }
    }

    def markAsRead() {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        def inboxLog = InboxLog.findById(params.id)
        if (inboxLog) {
            inboxLog.isRead = true
            inboxLog.isNotification = false
            inboxLog.save(flush: true)
        } else {
            responseDTO.status = false
            flash.params = "Some error has occured"
        }
        render(responseDTO as JSON)
    }

    def markAsUnread() {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        def inboxLog = InboxLog.findById(params.id)
        if (inboxLog) {
            inboxLog.isRead = false
            inboxLog.isNotification = false
            inboxLog.save(flush: true)
        } else {
            responseDTO.status = false
            flash.params = "Some error has occured"
        }
        render(responseDTO as JSON)
    }

    def deleteInboxLog() {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        def inboxLog = InboxLog.findById(params.id)
        if (inboxLog) {
            inboxLog.isDeleted = true
            inboxLog.isNotification = false
            inboxLog.save(flush: true)
        } else {
            responseDTO.status = false
            flash.params = "Some error has occurred"
        }
        render(responseDTO as JSON)
    }

    def deleteNotificationsForUserId() {
        def id = params.id
        if (id) {
            User user
            try {
                user = User.get(id)
                InboxLog.findAllByInboxUserId(id)?.each {
                    it.isDeleted = true
                    it.save(flush: true)
                }
                render true
            } catch (Exception e) {
                log.info("Could not delete notifications for user $user! $e.localizedMessage")
                render false
            }
        }
    }

    def markAsReadNotificationsForUserId(Long id) {
        if (id) {
            User user
            try {
                InboxLog.executeUpdate("update InboxLog set isRead=:isRead where inboxUserId=:id",[isRead:true,id: id])
                render true
            } catch (Exception e) {
                log.info("Could not delete notifications for user $user! $e.localizedMessage")
                render false
            }
        }
    }

    def deleteNotificationById() {
        def id = params.id
        if (id) {
            try {
                def inboxLog = InboxLog.get(id.toInteger())
                inboxLog.isDeleted = true
                inboxLog.save(flush: true)
                render true
            } catch (Exception e) {
                log.info("Could not delete notification! $e.localizedMessage")
                render false
            }
        }
    }

}