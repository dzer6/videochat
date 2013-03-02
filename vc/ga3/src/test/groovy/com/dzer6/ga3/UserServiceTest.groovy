package com.dzer6.ga3

import org.junit.runner.RunWith
import org.junit.Test

import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Bean

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import com.dzer6.ga3.domain.User

import com.dzer6.ga3.services.UserService


@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = ["classpath:test-application-context.xml"])
class UserServiceTest {
    
    @Autowired
    @Qualifier("userService")
    UserService userService
    
    @Test
    void testOpponentChoosing() {
        User me = userService.createUser()
        User opponent1 = userService.createUser()
        User opponent2 = userService.createUser()
        User opponent3 = userService.createUser()
        
        userService.changeUser(me.id, [playing: true])
        userService.changeUser(me.id, [broadcasting: true])
        
        userService.changeUser(opponent1.id, [playing: true])
        userService.changeUser(opponent1.id, [broadcasting: true])
        
        userService.changeUser(opponent2.id, [playing: true])
        userService.changeUser(opponent2.id, [broadcasting: true])
        
        userService.changeUser(opponent3.id, [playing: true])
        userService.changeUser(opponent3.id, [broadcasting: true])
        
        Set previousOpponentIds = new HashSet()
        
        for(int i = 0; i < 3; i++) {
            userService.releaseUser(me.id)

            me = userService.getUser(me.id)
            assert me.opponent == null

            userService.chooseOpponent(me.id)

            me = userService.getUser(me.id)
            assert me.opponent != null
            assert !previousOpponentIds.contains(me.opponent.id)
            previousOpponentIds.add(me.opponent.id)
        }
    }    
}

