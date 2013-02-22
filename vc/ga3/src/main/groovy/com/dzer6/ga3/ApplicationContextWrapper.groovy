package com.dzer6.ga3

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ApplicationContextWrapper implements ApplicationContextAware {
  
    private static final Logger log = LoggerFactory.getLogger(ApplicationContextWrapper.class)
    
    private final static Object semaphore = new Object()
  
    private static volatile boolean isInitialized
  
    private static ApplicationContextWrapper instance
  
    private ApplicationContext ctx
  
    private ApplicationContextWrapper() { }
    
    static ApplicationContextWrapper getInstance() { 
        synchronized(semaphore) {
            if(!isInitialized) {
                log.info("ApplicationContextWrapper instantiating")
                instance = new ApplicationContextWrapper()
                isInitialized = true
            }
        }
        return instance
    }
  
    ApplicationContext getApplicationContext() {        
        return ctx
    }
    
    void setApplicationContext(ApplicationContext ctx) {
        this.ctx = ctx
    }
  
    def propertyMissing(String name) {
        log.debug("getBean name = $name")
        ctx.getBean(name)
    }
}

