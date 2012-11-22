package com.dzer6.vc.session.storage;

public class SessionNotFoundException extends Exception {

  private String sessionId;

  public SessionNotFoundException(String sessionId) {
    this.sessionId = sessionId;
  }

  public String getSessionId() {
    return sessionId;
  }

  @Override
  public String toString() {
    return "Session expired or does not exists : " + sessionId;
  }
}
