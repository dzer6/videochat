package com.dzer6.ga3.repository;

import com.dzer6.ga3.domain.RtmpServer;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface RtmpServerRepository extends CrudRepository<RtmpServer, Long> {

    RtmpServer findByUrl(String url);

    List<RtmpServer> findByFreeStreamsNumberGreaterThanOrderByFreeStreamsNumberDesc(long freeStreamsNumber);

    @Query("select count (rs) from RtmpServer rs where rs.freeStreamsNumber > 0")
    long countFreeRtmpServers();
    
}
