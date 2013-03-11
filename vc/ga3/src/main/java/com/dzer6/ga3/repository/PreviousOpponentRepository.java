package com.dzer6.ga3.repository;

import com.dzer6.ga3.domain.PreviousOpponent;
import com.dzer6.ga3.domain.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface PreviousOpponentRepository extends JpaRepository<PreviousOpponent, Long>, JpaSpecificationExecutor<PreviousOpponent> {

    List<PreviousOpponent> findByUserAndOpponent(User user, User opponent);

    @Query("select count (po) from PreviousOpponent po where po.user = :user")
    long countByUser(@Param("user") User user);
    
    @Modifying
    @Query("delete PreviousOpponent po where po.user = :user")
    void deleteByUser(@Param("user") User user);
    
    @Modifying
    @Query("delete PreviousOpponent po where po.opponent = :opponent")
    void deleteByOpponent(@Param("opponent") User opponent);
    
}
