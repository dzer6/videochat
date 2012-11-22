package com.dzer6.ga3.services

import javax.servlet.http.*

import com.dzer6.ga3.*
import com.dzer6.ga3.domain.*

import com.dzer6.vc.session.storage.SessionNotFoundException

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

import groovy.json.JsonBuilder

@Service("webToolService")
@Scope("singleton")
class WebToolService {
  
    private static final Logger log = LoggerFactory.getLogger(WebToolService.class)
  
    @Autowired
    @Qualifier("sessionStorageService")
    def sessionStorageService
  
    @Autowired
    @Qualifier("userService")
    def userService
  
    @Autowired
    @Qualifier("config")
    def config
  
    void renderJson(HttpServletResponse response, Map map) {
        JsonBuilder builder = new JsonBuilder()
        builder(map)
        log.info("json = " + builder.toPrettyString())
        response.contentType = "text/json"
        builder.writeTo(response.writer)
    }

    String getCookieByName(HttpServletRequest request, String cookieName) {
        return request.cookies.find({ it.name == cookieName })?.value
    }

    String getSessionId(HttpServletRequest request) {
        return request.cookies.find({ it.name == config.SESSION_ID_COOKIE_NAME })?.value
    }
  
    boolean isConversationInProcess(User me) {
        return me != null && me.playing && me.opponent != null && me.opponent.playing
    }
  
    String getChatWithMeURL(HttpServletRequest request, String userId) {
        return getServerURL(request) + "?id=" + userId
    }
  
    String getServerURL(HttpServletRequest request) {
        return request.scheme + "://" + request.serverName + ":" + request.serverPort + request.getContextPath()
    }
    
    void saveSessionIdToCookie(String sessionId, HttpServletResponse response) {
        log.info("saveSessionIdToCookie() sessionId = " + sessionId)
        Cookie cookie = new Cookie(config.SESSION_ID_COOKIE_NAME, sessionId)
        cookie.path = config.SESSION_COOKIE_PATH
        cookie.maxAge = config.SESSION_COOKIE_MAX_AGE as int
        response.addCookie(cookie)
    }
  
    void redirectToIndexPage(String logMessage, Closure forward) {
        log.info(logMessage)
        //forward "/WEB-INF/pages/index.gtpl"
        forward "/home_index.groovy"
    }
  
    void redirectToIndexPage(String logMessage, Throwable t, Closure forward) {
        log.info(logMessage, t)
        //forward "/WEB-INF/pages/index.gtpl"
        forward "/home_index.groovy"
    }
  
    User getUser(HttpServletRequest request, Closure forward) {
        String sessionId = getSessionId(request)

        log.info("sessionId = $sessionId")

        def myId
    
        try {
            myId = sessionStorageService.get(sessionId, config.SESSION_PARAMETER_USER_ID)
        } catch(SessionNotFoundException e) {
            redirectToIndexPage("Session with sessionId = ${sessionId} is not found", forward)
            return null
        }
  
        log.info("myId = $myId")
  
        User me = userService.getUser(myId)
  
        log.info("me = $me")

        if (me == null) {
            sessionStorageService.disposeSession(sessionId)
            redirectToIndexPage("There is no user for current session, sessionId = ${sessionId}, userId = ${myId}", forward)
            return null
        }
    
        Date now = new Date()
    
        if (me.bannedTill != null && me.bannedTill > now) {
            request.bannedTillDelta = me.bannedTill.time - now.time
            forward "/WEB-INF/pages/userBanned.gtpl"
            return null
        }
    
        return me
    }
	
}

