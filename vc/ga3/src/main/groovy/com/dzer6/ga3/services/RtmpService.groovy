package com.dzer6.ga3.services

import com.dzer6.ga3.*
import com.dzer6.ga3.domain.*
import com.dzer6.ga3.repository.*

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired

import org.springframework.transaction.annotation.Transactional

@Service("rtmpService")
@Scope("singleton")
@Transactional
class RtmpService {
  
    private static final Logger log = LoggerFactory.getLogger(RtmpService.class)
  
    @Delegate
    private ServicesUtil servicesUtil = new ServicesUtil()
  
    @Autowired
    private RtmpServerRepository rtmpServerRepository
  
    RtmpServer createRtmpServer(String url, long freeStreamsNumber) {
        log.info("createRtmpServer()")
        log.info("url = " + url)
        log.info("freeStreamsNumber = " + freeStreamsNumber)
        RtmpServer rtmpServer = rtmpServerRepository.findByUrl(url)
        if (rtmpServer == null) {
            rtmpServer = new RtmpServer()
            rtmpServer.url = url
            rtmpServer.freeStreamsNumber = freeStreamsNumber
            rtmpServer = rtmpServerRepository.save(rtmpServer)
        }
    
        return rtmpServer
    }
  
    void updateRtmpServer(String url, long freeStreamsNumber) {
        invokeAgainIfOptimisticLockingFailureCatched("RtmpService.updateRtmpServer url = $url, freeStreamsNumber = $freeStreamsNumber") { status ->
            RtmpServer rtmpServer = rtmpServerRepository.findByUrl(url)
      
            if (rtmpServer == null) {
                throw new RuntimeException("There is now rtmp server with url = $url")
            }
      
            rtmpServer.freeStreamsNumber = freeStreamsNumber
            rtmpServerRepository.save(rtmpServer)
        }
    }

    void deleteRtmpServer(String url) {
        log.info("deleteRtmpServer()")
        log.info("url = " + url)
        RtmpServer rtmpServer = rtmpServerRepository.findByUrl(url)
        rtmpServerRepository.delete(rtmpServer)
    }
  
    boolean isThereRemainsFreeRtmpServers() {
        return rtmpServerRepository.countFreeRtmpServers()
    }
}
