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

    private Thread sweeperThread
    private volatile boolean sweeperOn = false
    // thread semaphore
    private static final Object semaphore = new Object()

    @PostConstruct
    def init() {
        long sweeperInterval = config.SWEEPER_INTERVAL as long
        long sessionTimeout = config.SESSION_TIMEOUT as long
        sweeperThread = new Thread({
                log.info("started")
                while (sweeperOn) {
                    try {
                        synchronized (semaphore) {
                            semaphore.wait(sweeperInterval)
                        }
                        def oldSessions = sessionRepository.findAllByLastUserDisconnectionLessThan(new Date(System.currentTimeMillis() - sessionTimeout))
                        oldSessions.each({
                            deleteSession(it.id)
                        })
                    } catch (Throwable t) {
                        log.error("Sweeper error.", t)
                    }
                }
                log.info("stopped")
            } as Runnable, "SessionCleanerService")
        sweeperThread.daemon = true
        sweeperOn = true
        sweeperThread.start()
        log.info("init()")
    }

    @PreDestroy
    def destroy() {
        sweeperOn = false
        semaphore.notify()
        log.info("destroy()")
    }

    @Transactional
    void deleteSession(String sessionId) {
        log.info("deleteSession() " + sessionId)

        def myId = sessionStorageService.get(sessionId, config.SESSION_PARAMETER_USER_ID)
    
        User me = userService.getUser(myId)

        if (me != null) {
            userService.changeUser(me, [playing: false])
            userService.releaseUser(me)
        }
    
        sessionStorageService.disposeSession(sessionId)
    }
}