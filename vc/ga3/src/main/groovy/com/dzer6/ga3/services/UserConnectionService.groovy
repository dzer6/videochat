package com.dzer6.ga3.services

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dzer6.vc.session.storage.SessionNotFoundException
import com.dzer6.vc.ga.UserConnectionInterface
import com.dzer6.ga3.domain.*

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service("userConnectionService")
@Scope("singleton")
class UserConnectionService implements UserConnectionInterface {

    private static final Logger log = LoggerFactory.getLogger(UserConnectionService.class)
  
    @Autowired
    @Qualifier("sessionStorageService")
    def sessionStorageService
  
    @Autowired
    @Qualifier("flashClient")
    def flashClient
  
    @Autowired
    @Qualifier("userService")
    def userService
  
    @Autowired
    @Qualifier("bayeuxWrapperService")
    def bayeuxWrapperService

    @Autowired
    @Qualifier("config")
    def config

    void connectionClosed(String sessionId) {
        log.info("connectionClosed($sessionId)")
    
        try {
            String userId = getUserId(sessionId)
        
            userService.changeUser(userId, [broadcasting: false])
            
            User me = userService.getUser(userId)
            User opponent = me.opponent

            if (opponent != null) {
                flashClient.stopStream(opponent.id)
                bayeuxWrapperService.turnOffChat(opponent)
                userService.addPreviousOpponent(userId, opponent.id)
            }
    
            bayeuxWrapperService.turnOffChat(me)

            sessionStorageService.connectionLost(sessionId)  
        } catch(e) {
            log.error("Unable to handle connection closed event", e)
        }
    }

    void connectionStarted(String sessionId) {
        log.info("connectionStarted($sessionId)")
        
        try {
            String userId = getUserId(sessionId)
            
            User me = userService.getUser(userId)
            User opponent = me.opponent
            
            if (opponent != null && me.playing) {
                flashClient.playStream(userId, me.rtmpServer.url + config.R5WA_APPLICATION_NAME_POSTFIX, opponent.id)
                flashClient.playStream(opponent.id, opponent.rtmpServer.url + config.R5WA_APPLICATION_NAME_POSTFIX, userId)
                
                bayeuxWrapperService.turnOnChat(me)
                bayeuxWrapperService.turnOnChat(opponent)
                
                userService.removePreviousOpponent(userId, opponent.id)
            }
            
            sessionStorageService.connectionStarted(sessionId)
        } catch(e) {
            log.error("Unable to handle connection started event", e)
        }
    }
  
    void broadcastingStoped(String sessionId) {
        log.info("broadcastingStoped($sessionId)")
        
        try {
            String userId = getUserId(sessionId)
            userService.changeUser(userId, [broadcasting: false])
        } catch(e) {
            log.error("Unable to set broadcasting false for user", e)
        }
    }
  
    void broadcastingStarted(String sessionId) {
        log.info("broadcastingStarted($sessionId)")
        
        try {
            String userId = getUserId(sessionId)
            userService.changeUser(userId, [broadcasting: true])
        } catch(e) {
            log.error("Unable to set broadcasting true for user", e)
        }
    }
    
    private String getUserId(String sessionId) {
        String userId = sessionStorageService.get(sessionId, config.SESSION_PARAMETER_USER_ID)
        log.info("user id = ${userId}")
        return userId
    }
}
