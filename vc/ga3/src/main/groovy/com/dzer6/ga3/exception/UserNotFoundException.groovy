package com.dzer6.ga3.exception

class UserNotFoundException extends RuntimeException {
	
    String userId
    
    UserNotFoundException(String message, String userId) {
        super(message)
        this.userId = userId
    }
    
    UserNotFoundException(String userId) {
        super()
        this.userId = userId
    }
    
    @Override
    String toString() {
        return "UserNotFoundException userId=$userId"
    }
}

