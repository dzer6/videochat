package com.dzer6.ga3.repository;

import com.dzer6.ga3.domain.Session;
import java.util.Date;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface SessionRepository extends CrudRepository<Session, String> {

    List<Session> findAllByLastUserDisconnectionLessThan(Date date);
    
}
