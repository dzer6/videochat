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
    
        User me = getUser(sessionId)
        
        if (me != null) {
            log.info("user id = ${me.id}")
            
            userService.changeUser(me, [broadcasting: false])
            
            User opponent = me.opponent

            if (opponent != null) {
                flashClient.stopStream(opponent.id)
                bayeuxWrapperService.turnOffChat(opponent)
                userService.addPreviousOpponent(me.id, opponent.id)
            }
    
            bayeuxWrapperService.turnOffChat(me)

            sessionStorageService.connectionLost(sessionId)  
        }
    }

    void connectionStarted(String sessionId) {
        log.info("connectionStarted($sessionId)")
        
        User me = getUser(sessionId)
        
        if (me != null) {
            log.info("user id = ${me.id}")
            User opponent = me.opponent
            
            if (opponent != null && me.playing) {
                flashClient.playStream(me.id, me.rtmpServer.url + config.R5WA_APPLICATION_NAME_POSTFIX, opponent.id)
                flashClient.playStream(opponent.id, opponent.rtmpServer.url + config.R5WA_APPLICATION_NAME_POSTFIX, me.id)
                
                bayeuxWrapperService.turnOnChat(me)
                bayeuxWrapperService.turnOnChat(opponent)
                
                userService.removePreviousOpponent(me.id, opponent.id)
            }
            
            sessionStorageService.connectionStarted(sessionId)
        }
    }
  
    void broadcastingStoped(String sessionId) {
        log.info("broadcastingStoped($sessionId)")
        
        User me = getUser(sessionId)
        
        if (me != null) {
            log.info("user id = ${me.id}")
            userService.changeUser(me, [broadcasting: false])
        }
    }
  
    void broadcastingStarted(String sessionId) {
        log.info("broadcastingStarted($sessionId)")
        
        User me = getUser(sessionId)
        
        if (me != null) {
            log.info("user id = ${me.id}")
            userService.changeUser(me, [broadcasting: true])
        }
    }
    
    private User getUser(String sessionId) {
        if (sessionId == null) {
            log.warn("sessionId is null")
            return null
        }
        
        String myId
      
        try {
            myId = sessionStorageService.get(sessionId, config.SESSION_PARAMETER_USER_ID)
        } catch(SessionNotFoundException e) {
            log.warn(e.message)
            return null
        }
    
        return userService.getUser(myId)
    }
}
