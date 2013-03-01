package com.dzer6.ga3.exception.handler

import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import org.springframework.context.annotation.Scope

import org.springframework.stereotype.Service

import javax.annotation.PostConstruct
import java.lang.reflect.ParameterizedType

import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class ExceptionHandler<E> {
    
    public static final int FIRST_PARAMETRIZED_TYPE = 0;
    
    protected static Logger log
    
    Class<E> exceptionClass
    
    @Autowired
    @Qualifier("servletExceptionHandler")
    def servletExceptionHandlerService
    
    @PostConstruct
    void init() {
        log = LoggerFactory.getLogger(getClass())
        exceptionClass = getClass().genericSuperclass.actualTypeArguments[FIRST_PARAMETRIZED_TYPE];
        servletExceptionHandlerService.registerHandler(exceptionClass, this)
    }
    
	abstract void handle(E e, HttpServletRequest request, HttpServletResponse response)
    
}

