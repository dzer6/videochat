package com.dzer6.ga3

import org.junit.runner.RunWith
import org.junit.Test
import org.junit.Before
import org.junit.After
import org.junit.Ignore

import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import org.springframework.transaction.annotation.Transactional

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Bean

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import com.dzer6.ga3.domain.User

import com.dzer6.ga3.services.UserService
import com.dzer6.vc.ga.UserConnectionInterface
import com.dzer6.vc.session.storage.SessionStorage
import com.dzer6.vc.ga.FlashClientInterface


@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = ["classpath:test-application-context.xml"])
@Ignore
class ReloadBrowserTest {
    
    @Autowired
    UserService userService
    
    @Autowired
    @Qualifier("sessionStorageService")
    SessionStorage sessionStorageService
    
    @Autowired
    @Qualifier("userConnectionService")
	UserConnectionInterface userConnectionService
    
    @Autowired
    @Qualifier("flashClientMock")
    FlashClientInterface flashClientMock
    
    @Autowired
    @Qualifier("config")
    def config
    
    def playStreamMethod
    def stopStreamMethod
    def cameraOnMethod
    def cameraOffMethod

    @Before
    void before() {
        playStreamMethod = flashClientMock.metaClass.playStream
        stopStreamMethod = flashClientMock.metaClass.playStream
        cameraOnMethod = flashClientMock.metaClass.cameraOn
        cameraOffMethod = flashClientMock.metaClass.cameraOff
    }
    
    @After
    void after() {
        flashClientMock.metaClass.playStream = playStreamMethod
        flashClientMock.metaClass.playStream = stopStreamMethod
        flashClientMock.metaClass.cameraOn = cameraOnMethod
        flashClientMock.metaClass.cameraOff = cameraOffMethod
    }
    
    @Test
    void test() {
        User me = userService.createUser()
        User opponent = userService.createUser()
        
        userService.changeUser(me.id, [playing: true])
        userService.changeUser(me.id, [broadcasting: true])
        
        userService.changeUser(opponent.id, [playing: true])
        userService.changeUser(opponent.id, [broadcasting: true])
        
        String sessionId = sessionStorageService.createSession()
        sessionStorageService.put(sessionId, config.SESSION_PARAMETER_USER_ID, me.id)
        
        userService.releaseUser(me.id)
        userService.chooseOpponent(me.id)
        
        // TODO: add flashClientMock callbacks for checking order of calls
        
        userConnectionService.connectionClosed(sessionId)
        userConnectionService.connectionStarted(sessionId)
    }
    
}

