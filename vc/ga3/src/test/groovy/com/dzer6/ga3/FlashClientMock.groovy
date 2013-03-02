package com.dzer6.ga3

import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.dzer6.vc.ga.FlashClientInterface

@Service("flashClientMock")
@Scope("singleton")
class FlashClientMock implements FlashClientInterface {
    
    private static final Logger log = LoggerFactory.getLogger(FlashClientMock.class)
    
    void playStream(String watcherId, String rtmpServerUrl, String stream) {
        log.info("")
    }
    
    void stopStream(String watcherId) {
        log.info("")
    }
    
    void cameraOn(String cameraId) {
        log.info("")
    }
    
    void cameraOff(String cameraId) {
        log.info("")
    }
}

