package com.dzer6.vc.ga;

public interface UserConnectionInterface {

  void connectionClosed(String sessionId);
  void connectionStarted(String sessionId);
  void broadcastingStoped(String sessionId);
  void broadcastingStarted(String sessionId);
    
}