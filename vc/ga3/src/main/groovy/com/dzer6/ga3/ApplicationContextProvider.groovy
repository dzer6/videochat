package com.dzer6.ga3

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

class ApplicationContextProvider implements ApplicationContextAware { 
    void setApplicationContext(ApplicationContext ctx) {
        ApplicationContextWrapper.getInstance().applicationContext = ctx
    }	
}

