package com.dzer6.vc.session.storage;

public interface DeleteSessionListener {
  void beforeDisposeSession(String sessionId);
}
