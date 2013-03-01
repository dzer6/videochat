package com.dzer6.ga3.exception.handler

import org.springframework.context.annotation.Scope

import org.springframework.stereotype.Service

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import com.dzer6.vc.session.storage.SessionNotFoundException
import com.dzer6.ga3.exception.UserBannedException

@Service("userBannedExceptionHandler")
@Scope("singleton")
class UserBannedExceptionHandler extends ExceptionHandler<UserBannedException> {
    
    void handle(UserBannedException e, HttpServletRequest request, HttpServletResponse response) {
        log.info("handle UserBannedException = $e")
        
        request.getRequestDispatcher("/WEB-INF/pages/userBanned.gtpl").forward(request, response)
    }
}

