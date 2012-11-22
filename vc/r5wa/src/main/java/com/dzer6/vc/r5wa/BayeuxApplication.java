package com.dzer6.vc.r5wa;

import com.dzer6.vc.ga.BayeuxInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import org.red5.server.adapter.ApplicationAdapter;
import org.red5.server.api.IConnection;
import org.red5.server.api.Red5;
import org.red5.server.api.service.ServiceUtils;

public class BayeuxApplication extends ApplicationAdapter implements BayeuxInterface {

  private Map<String, List<IConnection>> connectionsByChannelMap = new HashMap<String, List<IConnection>>();
  private Map<IConnection, String> channelByConnectionMap = new HashMap<IConnection, String>();
  private final Object monitor = new Object();
  
  @Override
  public void appDisconnect(IConnection conn) {
    log.debug("appDisconnect conn = " + conn);
    synchronized (monitor) {
      String channel = channelByConnectionMap.remove(conn);
      log.debug("appDisconnect channel = " + channel);
      if (channel != null) {
        List<IConnection> connections = connectionsByChannelMap.get(channel);
        log.debug("appDisconnect connections = " + connections);
        if (connections != null) {
          log.debug("appDisconnect connections != null, connection removed");
          connections.remove(conn);
        }
      }
    }
  }

  @Override
  public void deliver(String channel, Map<String, Object> data) {
    try {
      String json = new JSONObject(data).toString();
      log.debug("delivery channel = " + channel + ", data = " + json);
      List<IConnection> connections = getConnections(channel);
      log.debug("delivery connections = " + connections);
      for (IConnection connection : connections) {
        log.debug("delivery connection = " + connection);
        ServiceUtils.invokeOnConnection(connection, "delivery", new Object[]{channel, json});
        log.debug("delivery invokeOnConnection invoked");
      }
    } catch (Exception e) {
      log.error("Unable to delivery message", e);
    }
  }

  public void subscribe(String channel) {
    log.debug("subscribe channel = " + channel);
    IConnection connection = Red5.getConnectionLocal();
    log.debug("subscribe connection = " + connection);
    synchronized (monitor) {
      channelByConnectionMap.put(connection, channel);
      List<IConnection> connections = connectionsByChannelMap.get(channel);
      if (connections == null) {
         connections = new ArrayList<IConnection>();
         connectionsByChannelMap.put(channel, connections);
      }
      connections.add(connection);
    }
    log.debug("subscribe channel added for connection");
  }

  private List<IConnection> getConnections(String channel) {
    List<IConnection> result = new ArrayList<IConnection>();
    synchronized (monitor) {
      List<IConnection> connections = connectionsByChannelMap.get(channel);
      if (connections != null && connections.size() > 0) {
        result.addAll(connections);
      }
    }
    return result;
  }
}
