package com.dzer6.ga3.repository;

import com.dzer6.ga3.domain.Session;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface SessionRepository extends JpaRepository<Session, String>, JpaSpecificationExecutor<Session> {

    List<Session> findAllByLastUserDisconnectionLessThan(Date date);
    
}
