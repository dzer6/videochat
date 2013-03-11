package com.dzer6.ga3.repository;

import com.dzer6.ga3.domain.User;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
    
    @Query("select u from User u " +
           "where u <> :user and " + // not me
           "u.playing = true and " + // camera turned on
           "u.broadcasting = true") // broadcasting
    List<User> findAnyBroadcastingUserNotMe(@Param("user") User user);
  
    @Query("select u from User u where " + 
           "u <> :user and " +  // not me
           "u.playing = true and " + // camera turned on
           "u.broadcasting = true and " +  // broadcasting
           "u.id not in " +  
               "(select po.opponent.id from PreviousOpponent po where " + 
               "po.user = :user and " + 
               "po.disconnection >= :date)")
    List<User> findBroadcastingUserNotSeenSinceDate(@Param("user") User user,
                                                    @Param("date") Date date);
    
    @Modifying
    @Query("delete User u where u = :user")
    void deleteByUser(@Param("user") User user);
}

