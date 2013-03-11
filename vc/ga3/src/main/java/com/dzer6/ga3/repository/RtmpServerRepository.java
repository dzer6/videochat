package com.dzer6.ga3.repository;

import com.dzer6.ga3.domain.RtmpServer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface RtmpServerRepository extends JpaRepository<RtmpServer, Long>, JpaSpecificationExecutor<RtmpServer> {

    RtmpServer findByUrl(String url);

    List<RtmpServer> findByFreeStreamsNumberGreaterThanOrderByFreeStreamsNumberDesc(long freeStreamsNumber);

    @Query("select count (rs) from RtmpServer rs where rs.freeStreamsNumber > 0")
    long countFreeRtmpServers();
    
}
