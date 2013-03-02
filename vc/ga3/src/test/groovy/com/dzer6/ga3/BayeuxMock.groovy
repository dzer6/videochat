package com.dzer6.ga3

import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.dzer6.vc.ga.BayeuxInterface

@Service("bayeuxMock")
@Scope("singleton")
class BayeuxMock implements BayeuxInterface {
    
    private static final Logger log = LoggerFactory.getLogger(BayeuxMock.class)
    
	void deliver(String channel, Map<String, Object> data) {
        log.info("")
    }
}

