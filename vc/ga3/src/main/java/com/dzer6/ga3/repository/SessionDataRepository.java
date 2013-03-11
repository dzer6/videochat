package com.dzer6.ga3.repository;

import com.dzer6.ga3.domain.Session;
import com.dzer6.ga3.domain.SessionData;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface SessionDataRepository extends JpaRepository<SessionData, Long>, JpaSpecificationExecutor<SessionData> {

    @Modifying
    @Query("delete from SessionData sd where sd.session = :session and sd.key = :key")
    void deleteBySessionAndKey(@Param("session") Session session,
                               @Param("key") String key);

    @Modifying
    @Query("delete from SessionData sd where sd.session = :session")
    void deleteAllBySession(@Param("session") Session session);

    List<SessionData> findBySessionAndKey(Session session, String key);
}
