package com.dzer6.ga3

import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.dzer6.vc.ga.RtmpServerInterface

@Service("rtmpServerMock")
@Scope("singleton")
class RtmpServerMock implements RtmpServerInterface {
	
    private static final Logger log = LoggerFactory.getLogger(RtmpServerMock.class)
    
    void getFreeStreamsNumberFromAllServers() {
        log.info("")
    }
}

