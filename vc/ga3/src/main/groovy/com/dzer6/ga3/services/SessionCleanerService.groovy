package com.dzer6.ga3.services

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dzer6.ga3.domain.*
import com.dzer6.ga3.repository.*

import org.springframework.transaction.annotation.Transactional
  
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Service("sessionCleanerService")
@Scope("singleton")
class SessionCleanerService {

    private static final Logger log = LoggerFactory.getLogger(SessionCleanerService.class)
  
    @Autowired
    private SessionRepository sessionRepository
  
    @Autowired
    @Qualifier("sessionStorageService")
    def sessionStorageService
  
    @Autowired
    @Qualifier("userService")
    def userService
  
    @Autowired
    @Qualifier("config")
    def config
    
    private volatile boolean sweeperOn = false
    
    private static final Object semaphore = new Object()
    
    private long sweeperInterval
    
    private long sessionTimeout

    private Thread sweeperThread = new Thread({
        log.info("started")
        while (sweeperOn) {
            try {
                Date date = new Date(System.currentTimeMillis() - sessionTimeout)
                def oldSessions = sessionRepository.findAllByLastUserDisconnectionLessThan(date)
                oldSessions.each({
                    deleteSession(it.id)
                })
                synchronized (semaphore) {
                    semaphore.wait(sweeperInterval)
                }
            } catch (Throwable t) {
                log.error("Sweeper error.", t)
            }
        }
        log.info("stopped")
    } as Runnable, "SessionCleanerService")

    @PostConstruct
    def init() {
        sweeperInterval = config.SWEEPER_INTERVAL as long
        sessionTimeout = config.SESSION_TIMEOUT as long
        sweeperThread.daemon = true
        sweeperOn = true
        sweeperThread.start()
        log.info("init()")
    }

    @PreDestroy
    def destroy() {
        sweeperOn = false
        synchronized (semaphore) {
            semaphore.notify()
        }
        log.info("destroy()")
    }

    void deleteSession(String sessionId) {
        log.info("deleteSession() sessionId = " + sessionId)

        String userId = sessionStorageService.get(sessionId, config.SESSION_PARAMETER_USER_ID)

        userService.changeUser(userId, [playing: false])
        userService.releaseUser(userId)
        
        sessionStorageService.disposeSession(sessionId)
    }
}