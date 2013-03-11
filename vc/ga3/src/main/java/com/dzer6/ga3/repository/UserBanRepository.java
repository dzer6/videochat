package com.dzer6.ga3.repository;

import com.dzer6.ga3.domain.User;
import com.dzer6.ga3.domain.UserBan;
import java.util.Date;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface UserBanRepository extends JpaRepository<UserBan, Long>, JpaSpecificationExecutor<UserBan> {

    @Query("select count (ub) from UserBan ub where ub.user = :user and ub.dateCreated > :start and ub.dateCreated < :end")
    long countByUserAndDateCreatedBetween(@Param("user") User user,
                                          @Param("start") Date start,
                                          @Param("end") Date end);

    @Modifying
    @Query("delete UserBan ub where ub.user = :user and ub.dateCreated < :date")
    void deleteBansOlderThanDate(@Param("user") User user,
                                 @Param("date") Date date);
    
    @Modifying
    @Query("delete UserBan ub where ub.user = :user")
    void deleteByUser(@Param("user") User user);
    
}
