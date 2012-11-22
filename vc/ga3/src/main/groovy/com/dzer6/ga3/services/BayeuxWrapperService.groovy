package com.dzer6.ga3.services

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dzer6.ga3.domain.*

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service("bayeuxWrapperService")
@Scope("singleton")
class BayeuxWrapperService {
  
    private static final Logger log = LoggerFactory.getLogger(BayeuxWrapperService.class)

    @Autowired
    @Qualifier("bayeux")
    def bayeux
  
    @Autowired
    @Qualifier("config")
    def config

    def sendMessageToChat(User user, String message) {
        log.info("sendMessageToChat user.id = " + user.id + ", message = " + message)
        message = message.replaceAll("<", "&lt;").
                          replaceAll(">", "&gt;")
    
        bayeux.deliver(user.id + config.PUSH_CHANNEL_CHAT_MESSAGE, [isMe: true, message: message])
        bayeux.deliver(user.opponent.id + config.PUSH_CHANNEL_CHAT_MESSAGE, [isMe: false, message: message])
    }
  
    def turnOnChat(User user) {
        log.info("turnOnChat user.id = " + user.id)
        bayeux.deliver(user.id + config.PUSH_CHANNEL_TURN_ON_CHAT, [:])
    }
  
    def turnOffChat(User user) {
        log.info("turnOffChat user.id = " + user.id)
        bayeux.deliver(user.id + config.PUSH_CHANNEL_TURN_OFF_CHAT, [:])
    }
  
    def clearChat(User user) {
        log.info("clearChat user.id = " + user.id)
        bayeux.deliver(user.id + config.PUSH_CHANNEL_CLEAR_CHAT, [:])
    }
  
    def blockUser(User user, Map params) {
        log.info("blockFirstTime user.id = " + user.id)
        bayeux.deliver(user.id + config.PUSH_CHANNEL_BLOCK, params)
    }
  
    def turnOnBlocking(User user) {
        log.info("turnOnBlocking user.id = " + user.id)
        bayeux.deliver(user.id + config.PUSH_CHANNEL_TURN_ON_BLOCKING, [:])
    }
  
    def turnOffBlocking(User user) {
        log.info("turnOffBlocking user.id = " + user.id)
        bayeux.deliver(user.id + config.PUSH_CHANNEL_TURN_OFF_BLOCKING, [:])
    }
}


