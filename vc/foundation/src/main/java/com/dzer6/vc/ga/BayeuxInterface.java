package com.dzer6.vc.ga;

import java.util.Map;

public interface BayeuxInterface {
  void deliver(String channel, Map<String, Object> data);
}
