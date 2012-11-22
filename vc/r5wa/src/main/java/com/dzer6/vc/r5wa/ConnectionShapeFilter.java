package com.dzer6.vc.r5wa;

import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.mina.core.filterchain.IoFilterAdapter;

import org.apache.mina.core.session.IoSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filters connections and discards those exceeding the maximum value.
 * 
 */
public class ConnectionShapeFilter extends IoFilterAdapter {

  protected static Logger log = LoggerFactory.getLogger(ConnectionShapeFilter.class);
  //Maximum connections allowed
  private int maxConnections = -1;
  //Holder for registered sessions
  private volatile CopyOnWriteArraySet<IoSession> sessions = new CopyOnWriteArraySet<IoSession>();

  @Override
  public void sessionClosed(NextFilter filter, IoSession session) throws Exception {
    log.trace("Session closed");
    filter.sessionClosed(session);
    closeSession(session);
  }

  @Override
  public void messageReceived(NextFilter filter, IoSession session, Object msg) {
    log.trace("Message received - session id: {}", session.getId());
    if (!sessions.contains(session)) {
      log.debug("New session detected on read");
      increment(session);
    }
    if (!isConnectionMaximumExceeded()) {
      filter.messageReceived(session, msg);
    } else {
      closeSession(session);
    }
  }

  private void closeSession(IoSession session) {
    decrement(session);
    //dont call close if its already closing
    if (!session.isClosing()) {
      session.close(false);
    }
  }

  private void increment(IoSession session) {
    sessions.add(session);
    log.trace("Current connections (inc): {}", sessions.size());
  }

  private void decrement(IoSession session) {
    sessions.remove(session);
    log.trace("Current connections (dec): {}", sessions.size());
    if (sessions.size() < 0) {
      log.warn("The connection count is no longer valid");
    }
  }

  private boolean isConnectionMaximumExceeded() {
    boolean result = (maxConnections > 0 && sessions.size() > maxConnections);
    if (result) {
      log.warn("Maximum number of sessions exceeded. Discard session");
    }
    return result;
  }

  public void setMaxConnections(int maxConnections) {
    log.debug("Max connections: {}", maxConnections);
    this.maxConnections = maxConnections;
  }
}
