package com.dzer6.ga3.services

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dzer6.ga3.*
import com.dzer6.ga3.domain.*
import com.dzer6.ga3.repository.*

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Service

import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.annotation.Propagation

@Service("userService")
@Scope("singleton")
@Transactional
class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class)
  
    @Delegate
    private ServicesUtil servicesUtil = new ServicesUtil()

    @Autowired
    @Qualifier("config")
    def config
  
    @Autowired
    private UserRepository userRepository
  
    @Autowired
    private UserBanRepository userBanRepository
  
    @Autowired
    private RtmpServerRepository rtmpServerRepository
  
    @Autowired
    private PreviousOpponentRepository previousOpponentRepository

    /**
     * Blocks user and returns code of blocking action for UI.
     *
     * Returned codes: 0, 1, 2, 3, 4
     */
    int blockUser(User user) {
        UserBan ban = new UserBan(user: user)
        userBanRepository.save(ban)

        long nowInMills = System.currentTimeMillis()

        long hour = config.ONE_HOUR_IN_MILLS as long
        long halfHour = config.HALF_HOUR_IN_MILLS as long
        long tenMinutes = config.TEN_MINUTES_IN_MILLS as long
    
        Date hourAgo = new Date(nowInMills - hour)
        Date tenMinutesAgo = new Date(nowInMills - tenMinutes)
        Date now = new Date()

        int numberOfBansDuringHour = userBanRepository.countByUserAndDateCreatedBetween(user, hourAgo, now)
        int numberOfBansDuringTenMinutes = userBanRepository.countByUserAndDateCreatedBetween(user, tenMinutesAgo, now)

        int code

        if (numberOfBansDuringHour >= 5) {
            code = 0
        } else {
            code = numberOfBansDuringTenMinutes // 0 or 1 or 2 or 3 or 4
        }

        invokeAgainIfOptimisticLockingFailureCatched("UserService.blockUser set bannedTill and playing") { status ->
            if (code == 0) { // number of bans during ten minutes
                user.bannedTill = new Date(nowInMills + halfHour)
            } else if (code == 4) { // number of bans during ten minutes
                user.bannedTill = new Date(nowInMills + tenMinutes)
            }
            user.playing = false
            userRepository.save(user)
        }

        invokeAgainIfOptimisticLockingFailureCatched("UserService.blockUser delete bans older than one hour") { status ->
            userBanRepository.deleteBansOlderThanDate(user, hourAgo)
        }

        return code // 0 or 1 or 2 or 3 or 4
    }
  
    void changeUser(User user, Map fields) {
        log.info("changeUser(user: ${user}, fields: ${fields})")
        invokeAgainIfOptimisticLockingFailureCatched("UserService.changeUser") { status ->
            if (userRepository.findOne(user.id) == null) {
                log.info("There is no user with id = ${user.id}")
                //status.setRollbackOnly()
                return
            }

            fields.each() { key, value ->
                user.setProperty(key, value)
            }

            userRepository.save(user)
        }
    }
  
    void addPreviousOpponent(String userId, String opponentId) {
        log.info("addPreviousOpponent(userId: ${userId}, opponentId: ${opponentId})")
        invokeAgainIfOptimisticLockingFailureCatched("UserService.addPreviousOpponentToUser") { status ->
            User user = userRepository.findOne(userId)

            if (user == null) {
                log.info("There is no user with id = ${userId}")
                //status.setRollbackOnly()
                return
            }
      
            User opponent = userRepository.findOne(opponentId)
            if(opponent == null) {
                log.info("There is no user with id = ${opponentId}")
                //status.setRollbackOnly()
                return
            }

            List<PreviousOpponent> result = previousOpponentRepository.findByUserAndOpponent(user, opponent)
                                                
            log.info("previous opponents number = ${result != null ? result.size() : 0}")
                                    
            PreviousOpponent previousOpponent = result != null && result.size() > 0 ? result[0] : null;
      
            if (previousOpponent == null) {
                previousOpponent = new PreviousOpponent([user: user, opponent: opponent])
                log.info("previousOpponent is null, create new one")
            } else {
                log.info("previousOpponent already existing = " + previousOpponent)
                previousOpponent.deleted = false
            }
      
            previousOpponent.deleted = false
            previousOpponent.disconnection = new Date()
      
            previousOpponentRepository.save(previousOpponent)
            log.info("saved previousOpponent = " + previousOpponent)
        }
    }
  
    void removePreviousOpponent(String userId, String opponentId) {
        log.info("removePreviousOpponent(userId: ${userId}, opponentId: ${opponentId})")
        invokeAgainIfOptimisticLockingFailureCatched("UserService.removePreviousOpponent") { status ->
            User user = userRepository.findOne(userId)

            if (user == null) {
                log.info("There is no user with id = ${userId}")
                //status.setRollbackOnly()
                return
            }
      
            User opponent = userRepository.findOne(opponentId)
            if(opponent == null) {
                log.info("There is no user with id = ${opponentId}")
                //status.setRollbackOnly()
                return
            }
      
            List<PreviousOpponent> result = previousOpponentRepository.findByUserAndOpponent(user, opponent)
                                    
            PreviousOpponent previousOpponent = result!= null && result.size() > 0 ? result[0] : null;
      
            if(previousOpponent == null) {
                log.info("There is no previousOpponent with userId = ${userId} and opponentId = ${opponentId}")
                //status.setRollbackOnly()
                return
            }
            previousOpponent.deleted = true
            previousOpponentRepository.save(previousOpponent)
      
            log.info("marked as deleted previousOpponent = " + previousOpponent)
        }
    }

    void releaseUser(User user) {
        log.info("releaseUser(user: ${user})")
        invokeAgainIfOptimisticLockingFailureCatched("UserService.releaseUser") { status ->
            if (userRepository.findOne(user.id) == null) {
                log.info("There is no user with id = ${user.id}")
                //status.setRollbackOnly()
                return
            }
      
            User opponent = user.opponent 

            if (opponent == null) {
                log.info("There is no opponent of user with id = ${user.id}")
                //status.setRollbackOnly()
                return
            }
      
            addPreviousOpponent(user.id, opponent.id)
            addPreviousOpponent(opponent.id, user.id)
	  
            opponent.opponent = null
            user.opponent = null
      
            userRepository.save(opponent)
            userRepository.save(user)
        }
    }

    User getUser(String userId) {
        log.info("getUser(userId: ${userId})")
        if (userId == null || userId == "") {
            log.info("There is no user with id = ${userId}")
            return null;
        }
        return userRepository.findOne(userId)
    }

    User createUser() {
        log.info("createUser()")
    
        return invokeAgainIfOptimisticLockingFailureCatched("UserService.createUser") { status ->
            User user = new User()
      
            List<RtmpServer> rtmpServers = rtmpServerRepository.findByFreeStreamsNumberGreaterThanOrderByFreeStreamsNumberDesc(1)
      
            log.info("rtmpServers = ${rtmpServers}")
      
            if (rtmpServers == null || rtmpServers.size() == 0 || rtmpServers[0] == null) {
                new RuntimeException("There is no free rtmp servers.")
            }
      
            RtmpServer rtmpServer = rtmpServers[0]
      
            log.info("rtmpServer = ${rtmpServer}")

            user.rtmpServer = rtmpServer
            user.playing = false
      
            userRepository.save(user)

            return user
        }
    }

    void setOpponent(String myId, String opponentId) {
        log.info("setOpponent(userId: ${userId}, opponentId: ${opponentId})")
        invokeAgainIfOptimisticLockingFailureCatched("UserService.setOpponent") { status ->
            User me = userRepository.findOne(myId)

            if (me == null) {
                log.info("There is no user with id = ${myId}")
                //status.setRollbackOnly()
                return
            }

            User you = userRepository.findOne(opponentId)

            if (you == null) {
                log.info("There is no user with id = ${opponentId}")
                //status.setRollbackOnly()
                return
            }

            me.opponent = you
            userRepository.save(me)

            you.opponent = me
            userRepository.save(you)
        }
    }

    void chooseOpponent(User me) {
        log.info("chooseOpponent(user.id: ${me.id}")
        invokeAgainIfOptimisticLockingFailureCatched("UserService.chooseOpponent") { status ->
            if (userRepository.findOne(me.id) == null) {
                log.info("There is no user with id = ${user.id}")
                //status.setRollbackOnly()
                return
            }

            if (me.opponent != null) {
                log.info("opponent already choosed, user.id = ${user.id}, opponent.id = ${me.opponent.id}")
                //status.setRollbackOnly()
                return
            }
      
            User you
      
            long numberOfPreviousOpponents = previousOpponentRepository.countByUser(me)
            log.info("numberOfPreviousOpponents = ${numberOfPreviousOpponents}")
      
            if (numberOfPreviousOpponents == 0) {    
                you = findAnyBroadcastingUserNotMe(me)
                log.info("findAnyBroadcastingUserNotMe you = ${you}")
            } else {
                you = findUserNotSeenForFiveMinutes(me)
                log.info("findUserNotSeenForFiveMinutes you = ${you}")    
        
                if (you == null) {
                    you = findAnyBroadcastingUserNotMe(me)
                    log.info("findAnyBroadcastingUserNotMe you = ${you}")
                }
            }
      
            log.info("---> choosed opponent = ${you}")
       
            me.opponent = you
            userRepository.save(me)

            if (you != null) {
                you.opponent = me
                userRepository.save(you)
            }
        }
    }
  
    private User findAnyBroadcastingUserNotMe(User user) {
        List result = userRepository.findAnyBroadcastingUserNotMe(user)
        return result != null && result.size() > 0 ? result.get(0) : null
    }
  
    private User findBroadcastingUserNotSeenSinceDate(User user, Date date) {
        List result = userRepository.findBroadcastingUserNotSeenSinceDate(user, date)
        return result != null && result.size() > 0 ? result.get(0) : null
    }
  
    private User findUserNotSeenForFiveMinutes(User me) {
        long lastWatchedUserInterval = config.LAST_WATCHED_USER_INTERVAL as long
        Date fiveMinutesAgo = new Date(System.currentTimeMillis() - lastWatchedUserInterval)
        
        User result = findBroadcastingUserNotSeenSinceDate(me, fiveMinutesAgo)
                                     
        return result
    }
}
