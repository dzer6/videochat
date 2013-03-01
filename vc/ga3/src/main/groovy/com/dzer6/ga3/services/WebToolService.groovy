package com.dzer6.ga3.services

import javax.servlet.http.*

import com.dzer6.ga3.*
import com.dzer6.ga3.domain.*

import com.dzer6.vc.session.storage.SessionNotFoundException

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

import groovy.json.JsonBuilder
import com.dzer6.ga3.exception.UserBannedException

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
        String sessionId = request.cookies.find({ it.name == config.SESSION_ID_COOKIE_NAME })?.value
        log.info("sessionId = $sessionId")
        return sessionId
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
  
    User getUser(HttpServletRequest request) {
        String sessionId = getSessionId(request)
        String myId = sessionStorageService.get(sessionId, config.SESSION_PARAMETER_USER_ID)
        User me = userService.getUser(myId)
    
        Date now = new Date()
    
        if (me.bannedTill != null && me.bannedTill > now) {
            request.bannedTillDelta = me.bannedTill.time - now.time
            throw new UserBannedException(me.id)
        }
    
        return me
    }
}

