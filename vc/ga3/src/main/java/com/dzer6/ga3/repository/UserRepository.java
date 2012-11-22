package com.dzer6.ga3.repository;

import com.dzer6.ga3.domain.LifePeriod;
import com.dzer6.ga3.domain.SexType;
import com.dzer6.ga3.domain.User;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface UserRepository extends CrudRepository<User, String> {
    
    @Query("select u from User u where u <> :user and " + 
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
                                     
    @Query("select u from User u where " + 
           "u <> :user and " +  // not me
           "u.playing = true and " +  // camera turned on 
           "u.broadcasting = true and " +  // broadcasting 
           "u.myLifePeriod = :lifePeriod and " +  
           "u.mySexType = :sexType and " +  
           "u not in " +  
               "(select po.opponent from PreviousOpponent po where " + 
               "po.user = :user and " + 
               "po.disconnection >= :date)")
    List<User> findBroadcastingUserByLifePeriodAndSexTypeNotSeenSinceDate(@Param("user") User user,
                                                                          @Param("lifePeriod") LifePeriod lifePeriod,
                                                                          @Param("sexType") SexType sexType,
                                                                          @Param("date") Date date);
                                     
  
}

