package com.dzer6.ga3.services

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dzer6.vc.session.storage.SessionNotFoundException
import com.dzer6.vc.session.storage.SessionStorage

import org.springframework.transaction.annotation.Transactional

import org.springframework.dao.OptimisticLockingFailureException

import com.dzer6.ga3.domain.*
import com.dzer6.ga3.*
import com.dzer6.ga3.repository.*

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service("sessionStorageService")
@Scope("singleton")
@Transactional
class SessionStorageService implements SessionStorage {

    private static final Logger log = LoggerFactory.getLogger(SessionStorageService.class)
  
    @Autowired
    @Qualifier("config")
    def config
  
    @Autowired
    private SessionRepository sessionRepository
  
    @Autowired
    private SessionDataRepository sessionDataRepository

    @Delegate
    private ServicesUtil servicesUtil = new ServicesUtil()

    @Override
    String createSession() {
        Session session = new Session()
        long sessionLiveTime = config.SESSION_LIVE_TIME as long
        session.lastUserDisconnection = new Date(System.currentTimeMillis() + sessionLiveTime)
        sessionRepository.save(session)
    
        log.info("createSession(), session.id = ${session.id}")
        return session.id
    }

    @Override
    void disposeSession(String sessionId) {
        log.info("disposeSession(${sessionId})")
        invokeAgainIfOptimisticLockingFailureCatched("SessionStorageService.disposeSession(${sessionId})") {
            Session session = sessionRepository.findOne(sessionId)
            if (session == null) {
                throw new SessionNotFoundException(sessionId)
            }
            sessionDataRepository.deleteAllBySession(session)
            sessionRepository.delete(session)
        }
    }
  
    boolean sessionExists(String sessionId) {
        log.info("sessionExists(${sessionId})")
        return sessionId != null && sessionId != "" && sessionRepository.findOne(sessionId) != null
    }

    @Override
    Serializable get(String sessionId, String key) {
        log.info("get(${sessionId}, ${key})")
        if (sessionId == null || sessionId == "") {
            throw new SessionNotFoundException(sessionId)
        }
    
        Session session = sessionRepository.findOne(sessionId)
        if (session == null) {
            throw new SessionNotFoundException(sessionId)
        }

        return sessionDataRepository.findBySessionAndKey(session, key)[0]?.data
    }

    @Override
    void put(String sessionId, String key, Serializable value) {
        log.info("put(${sessionId}, ${key})")
        if (sessionId == null || sessionId == "") {
            throw new SessionNotFoundException(sessionId)
        }
    
        Session session = sessionRepository.findOne(sessionId)
        if (session == null) {
            throw new SessionNotFoundException(sessionId)
        }

        invokeAgainIfOptimisticLockingFailureCatched("SessionStorageService.put(${sessionId}, ${key})") {
            SessionData sessionData = sessionDataRepository.findBySessionAndKey(session, key)[0]
            if (sessionData == null) {
                sessionData = new SessionData()
                sessionData.session = session
                sessionData.key = key
            }
            sessionData.data = value
            sessionDataRepository.save(sessionData)
        }
    }

    @Override
    void remove(String sessionId, String key) {
        log.info("remove(${sessionId}, ${key})")
    
        Session session = sessionRepository.findOne(sessionId)
        if (session == null) {
            throw new SessionNotFoundException(sessionId)
        }

        invokeAgainIfOptimisticLockingFailureCatched("SessionStorageService.remove(${sessionId}, ${key})") {
            sessionDataRepository.deleteBySessionAndKey(session, key)
        }
    }

    @Override
    void connectionLost(String sessionId) {
        log.info("connectionLost(${sessionId})")
    
        Session session = sessionRepository.findOne(sessionId)
        if (session == null) {
            throw new SessionNotFoundException(sessionId)
        }

        invokeAgainIfOptimisticLockingFailureCatched("SessionStorageService.connectionLost(${sessionId})") {
            session.lastUserDisconnection = new Date()
            sessionRepository.save(session)
        }
    }

    @Override
    void connectionStarted(String sessionId) {
        log.info("connectionStarted(${sessionId})")
    
        Session session = sessionRepository.findOne(sessionId)
        if (session == null) {
            throw new SessionNotFoundException(sessionId)
        }

        long sessionLiveTime = config.SESSION_LIVE_TIME as long

        invokeAgainIfOptimisticLockingFailureCatched("SessionStorageService.connectionStarted(${sessionId})") {
            session.lastUserDisconnection = new Date(System.currentTimeMillis() + sessionLiveTime)
            sessionRepository.save(session)
        }
    }
}
