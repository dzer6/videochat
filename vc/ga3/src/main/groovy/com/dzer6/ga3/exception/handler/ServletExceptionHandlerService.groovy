package com.dzer6.ga3.exception.handler

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import javax.annotation.PostConstruct

@Service("servletExceptionHandler")
@Scope("singleton")
class ServletExceptionHandlerService {
	
    private static final Logger log = LoggerFactory.getLogger(ServletExceptionHandlerService.class)
    
    private Map handlers = Collections.synchronizedMap(new HashMap())
    
    void registerHandler(Class<? extends Exception> e, ExceptionHandler handler) {
        assert(e != null && handler != null)
        log.info("registerHandler exception = $e, handler = $handler")
        handlers[e] = handler
    }
    
    void handle(Exception e, HttpServletRequest request, HttpServletResponse response) {
        assert(e != null && request != null && response != null)
        log.info("handle exception = $e, request = $request, response = $response")
        
        ExceptionHandler handler = handlers[e.class]
        
        if (handler != null) {
            handler.handle(e, request, response)
        } else {
            throw e
        }
    } 
}

