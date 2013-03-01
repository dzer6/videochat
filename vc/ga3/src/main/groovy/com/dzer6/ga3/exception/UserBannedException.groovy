package com.dzer6.ga3.exception

class UserBannedException extends RuntimeException {
	
    String userId
    
    UserBannedException(String message, String userId) {
        super(message)
        this.userId = userId
    }
    
    UserBannedException(String userId) {
        super()
        this.userId = userId
    }
    
    @Override
    String toString() {
        return "UserBannedException userId=$userId"
    }
}

