package com.dzer6.vc.session.storage;

import java.io.Serializable;

public interface SessionStorage {
  
  String createSession();

  void disposeSession(String sessionId) throws SessionNotFoundException;
  
  boolean sessionExists(String sessionId);

  Serializable get(String sessionId, String key) throws SessionNotFoundException;

  void put(String sessionId, String key, Serializable value) throws SessionNotFoundException;

  void remove(String sessionId, String key) throws SessionNotFoundException;

  void connectionLost(String sessionId) throws SessionNotFoundException;

  void connectionStarted(String sessionId) throws SessionNotFoundException;
  
}
