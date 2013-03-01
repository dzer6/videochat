package com.dzer6.ga3.exception.handler

import org.springframework.context.annotation.Scope

import org.springframework.stereotype.Service

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import com.dzer6.vc.session.storage.SessionNotFoundException

@Service("sessionNotFoundExceptionHandler")
@Scope("singleton")
class SessionNotFoundExceptionHandler extends ExceptionHandler<SessionNotFoundException> {
    
    void handle(SessionNotFoundException e, HttpServletRequest request, HttpServletResponse response) {
        log.info("handle SessionNotFoundException = $e")
        
        request.getRequestDispatcher("/home_index.groovy").forward(request, response)
    }
}

