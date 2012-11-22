package com.dzer6.ga3

import org.springframework.dao.OptimisticLockingFailureException

import org.springframework.transaction.support.TransactionTemplate
import org.springframework.transaction.support.TransactionCallback

import org.springframework.orm.jpa.JpaTransactionManager

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ServicesUtil {

    private static final Logger log = LoggerFactory.getLogger(ServicesUtil.class)
  
    public Object invokeAgainIfOptimisticLockingFailureCatched(String message, Closure c) {
        try {
            JpaTransactionManager tm = ApplicationContextWrapper.instance.transactionManager
            TransactionTemplate tt = new TransactionTemplate(tm)
            tt.execute({status ->
                    log.info("Execute closure in transaction. TransactionManager = ${tm}. TransactionTemplate = ${tt}.")
                    return c.call(status)
                } as TransactionCallback)
        } catch(OptimisticLockingFailureException e) {
            log.info("OptimisticLockingFailureException: ${message}, try again")
            return invokeAgainIfOptimisticLockingFailureCatched(message, c)
        }
    }
}

