package com.dzer6.ga3

import org.junit.runner.RunWith
import org.junit.Test
import org.junit.Before
import org.junit.After

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


@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = ["classpath:test-application-context.xml"])
@Transactional
class UserServiceTest {
    
    @Autowired
    UserService userService
    
    User me
    User opponent1
    User opponent2
    User opponent3
    
    @Before
    void init() {
        println "=== init ==="
        me = userService.createUser()
        opponent1 = userService.createUser()
        opponent2 = userService.createUser()
        opponent3 = userService.createUser()
        
        userService.changeUser(me.id, [playing: true])
        userService.changeUser(me.id, [broadcasting: true])
        
        userService.changeUser(opponent1.id, [playing: true])
        userService.changeUser(opponent1.id, [broadcasting: true])
        
        userService.changeUser(opponent2.id, [playing: true])
        userService.changeUser(opponent2.id, [broadcasting: true])
        
        userService.changeUser(opponent3.id, [playing: true])
        userService.changeUser(opponent3.id, [broadcasting: true])
    }
    
    @After
    void clean() {
        println "=== clean ==="
        userService.deleteUser(me.id)
        userService.deleteUser(opponent1.id)
        userService.deleteUser(opponent2.id)
        userService.deleteUser(opponent3.id)
    }
    
    @Test
    void testOpponentChoosing() {
        println "=== testOpponentChoosing ==="
        Set previousOpponentIds = new HashSet()
        User user = me
        for(int i = 0; i < 3; i++) {
            userService.releaseUser(user.id)

            user = userService.getUser(user.id)
            assert user.opponent == null

            userService.chooseOpponent(user.id)

            user = userService.getUser(user.id)
            
            println "$i ============================="
            println "           me = $user.id"
            println "     opponent = $user.opponent.id"
            
            assert user.opponent != null
            assert !previousOpponentIds.contains(user.opponent.id)
            previousOpponentIds.add(user.opponent.id)
        }
    }    
    
    @Test
    void testOpponentChoosingLoop() {
        println "=== testOpponentChoosingLoop ==="
        String previousOpponentId
        User user = me
        for(int i = 0; i < 10; i++) {
            userService.releaseUser(user.id)
            user = userService.getUser(user.id)
            assert user.opponent == null
            
            userService.chooseOpponent(user.id)
            
            user = userService.getUser(user.id)
            
            println "$i ============================="
            println "           me = $user.id"
            println "     opponent = $user.opponent.id"
            println "prev opponent = $previousOpponentId"
            
            assert user.opponent != null
            assert user.opponent.id != previousOpponentId
            
            previousOpponentId = user.opponent.id
        }
    }
}

