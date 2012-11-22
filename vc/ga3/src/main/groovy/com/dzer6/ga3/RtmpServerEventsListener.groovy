package com.dzer6.ga3

import com.dzer6.vc.ga.*

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode

import org.springframework.stereotype.Service

import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Service("rtmpServerEventsListener")
@Scope("singleton")
class RtmpServerEventsListener implements FrontendServerInterface {
  
    private static final Logger log = LoggerFactory.getLogger(RtmpServerEventsListener.class)
  
    @Autowired
    @Qualifier("rtmpService")
    def rtmpService
  
    public void refreshFreeRtmpStreamsNumber(String rtmpServerUrl, long freeStreamsNumber) {
        log.info("refreshFreeRtmpStreamsNumber rtmpServerUrl = $rtmpServerUrl, freeStreamsNumber = $freeStreamsNumber")
        rtmpService.updateRtmpServer(rtmpServerUrl, freeStreamsNumber)
    }
  
}

