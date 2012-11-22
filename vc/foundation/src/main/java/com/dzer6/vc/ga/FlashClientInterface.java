package com.dzer6.vc.ga;

public interface FlashClientInterface {

  void playStream(String watcherId, String rtmpServerUrl, String stream);
  void stopStream(String watcherId);
  void cameraOn(String cameraId);
  void cameraOff(String cameraId);
    
}