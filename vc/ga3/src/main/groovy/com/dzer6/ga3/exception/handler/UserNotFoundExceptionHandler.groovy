package com.dzer6.ga3.exception.handler

import org.springframework.context.annotation.Scope

import org.springframework.stereotype.Service

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import com.dzer6.ga3.exception.UserNotFoundException

@Service("userNotFoundExceptionHandler")
@Scope("singleton")
class UserNotFoundExceptionHandler extends ExceptionHandler<UserNotFoundException> {
    
    void handle(UserNotFoundException e, HttpServletRequest request, HttpServletResponse response) {
        log.info("handle UserNotFoundException = $e")
        
        request.getRequestDispatcher("/home_index.groovy").forward(request, response)
    }
    
}

