package com.dzer6.vc.r5wa;

import com.dzer6.vc.ga.FlashClientInterface;
import com.dzer6.vc.ga.FrontendServerInterface;
import com.dzer6.vc.ga.RtmpServerInterface;
import com.dzer6.vc.ga.UserConnectionInterface;
import com.xuggle.xuggler.IContainer;
import etm.core.configuration.BasicEtmConfigurator;
import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmMonitor;
import etm.core.renderer.SimpleTextRenderer;
import etm.core.timer.Java15NanoTimer;
import java.util.*;
import org.red5.server.adapter.ApplicationAdapter;
import org.red5.server.api.IConnection;
import org.red5.server.api.IScope;
import org.red5.server.api.Red5;
import org.red5.server.api.service.ServiceUtils;
import org.red5.server.api.stream.IBroadcastStream;

public class Application extends ApplicationAdapter implements FlashClientInterface, RtmpServerInterface {

    public static final String SESSION_ID_PARAMETER = "sessionId";
    public static final String WATCHER_ID_PARAMETER = "watcherId";
    public static final String CAMERA_ID_PARAMETER = "cameraId";
    private VideoTranscoder resampler;
    private EtmMonitor profiler = EtmManager.getEtmMonitor();
    private Timer profilerRenderer = new Timer("profilerRenderer", true);
    private int mProfilerFrequency = 0;
    private UserConnectionInterface userConnectionQueue;
    private FrontendServerInterface frontendServerQueue;
    private Map<String, IConnection> watchers = Collections.synchronizedMap(new HashMap<String, IConnection>());
    private Map<String, IConnection> cameras = Collections.synchronizedMap(new HashMap<String, IConnection>());
    private Map<IConnection, String> sessionIdByConnectionMap = Collections.synchronizedMap(new HashMap<IConnection, String>());
    private String rtmpServerAddress;
    private long rtmpServerCapacity;

    public void setRtmpServerAddress(String rtmpServerAddress) {
        this.rtmpServerAddress = rtmpServerAddress;
    }

    public void setRtmpServerCapacity(long rtmpServerCapacity) {
        this.rtmpServerCapacity = rtmpServerCapacity;
    }

    public void setFrontendServerQueue(FrontendServerInterface frontendServerQueue) {
        this.frontendServerQueue = frontendServerQueue;
    }

    public void setUserConnectionQueue(UserConnectionInterface userConnectionQueue) {
        this.userConnectionQueue = userConnectionQueue;
    }

    public void setResampler(VideoTranscoder resampler) {
        this.resampler = resampler;
    }

    @Override
    public boolean appStart(IScope app) {
        try {
            log.info("appStart");
            log.info("appStart");
            log.info("XUGGLE_HOME = " + System.getenv().get("XUGGLE_HOME"));
            log.info("LD_LIBRARY_PATH = " + System.getenv().get("LD_LIBRARY_PATH"));
            log.info("DYLD_LIBRARY_PATH = " + System.getenv().get("DYLD_LIBRARY_PATH"));
            IContainer.make();
            BasicEtmConfigurator.configure(true, new Java15NanoTimer());
            profiler.start();
            // very simple timer here that spits out profiling data every 5 seconds
            if (mProfilerFrequency > 0) {
                profilerRenderer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        log.debug("Printing Statistics for: " + this.getClass().getName());
                        profiler.render(new SimpleTextRenderer());
                    }
                }, mProfilerFrequency * 1000, mProfilerFrequency * 1000);
            }

            return super.appStart(app);
        } catch (Exception e) {
            log.error("Got error while started application", e);
            return false;
        }
    }

    @Override
    public boolean appConnect(IConnection conn, Object[] params) {
        try {
            log.info("appConnect");
            log.debug("appConnect params = " + params);

            String sessionId = null;
            String watcherId = null;
            String cameraId = null;

            if (params != null && params.length == 1 && params[0] instanceof Map) {
                Map paramsMap = (Map) params[0];

                sessionId = (String) paramsMap.get(SESSION_ID_PARAMETER);
                watcherId = (String) paramsMap.get(WATCHER_ID_PARAMETER);
                cameraId = (String) paramsMap.get(CAMERA_ID_PARAMETER);
            }

            if (watcherId != null) {
                log.info("add watcher id = " + watcherId);
                watchers.put(watcherId, conn);
                conn.getClient().setAttribute(WATCHER_ID_PARAMETER, watcherId);
                log.info("watchers number = " + watchers.size());
            }

            if (cameraId != null) {
                log.info("add camera id = " + cameraId);
                cameras.put(cameraId, conn);
                conn.getClient().setAttribute(CAMERA_ID_PARAMETER, cameraId);
                log.info("cameras number = " + cameras.size());
                refreshFreeRtmpStreamsNumber();
            }

            if (sessionId != null) {
                sessionIdByConnectionMap.put(conn, sessionId);
                log.info("before connectionStarted for session id = " + sessionId);
                conn.getClient().setAttribute(SESSION_ID_PARAMETER, sessionId);
                userConnectionQueue.connectionStarted(sessionId);
                log.info("after connectionStarted for session id = " + sessionId);
            }

            return super.appConnect(conn, params);
        } catch (Exception e) {
            log.error("Got error during connection", e);
            return false;
        }
    }

    @Override
    public void appDisconnect(IConnection conn) {
        try {
            log.info("appDisconnect");

            String sessionId = (String) conn.getClient().getAttribute(SESSION_ID_PARAMETER);
            log.info("session id = " + sessionId);

            String watcherId = (String) conn.getClient().getAttribute(WATCHER_ID_PARAMETER);

            String cameraId = (String) conn.getClient().getAttribute(CAMERA_ID_PARAMETER);

            if (watcherId != null) {
                log.info("remove watcher id = " + watcherId);
                watchers.remove(watcherId);
                log.info("watchers number = " + watchers.size());
            }

            if (cameraId != null) {
                log.info("remove camera id = " + cameraId);
                cameras.remove(cameraId);
                log.info("cameras number = " + cameras.size());
                refreshFreeRtmpStreamsNumber();
            }

            if (sessionId != null) {
                sessionIdByConnectionMap.remove(conn);
                log.info("before connectionClosed for session id = " + sessionId);
                userConnectionQueue.connectionClosed(sessionId);
                log.info("after connectionClosed for session id = " + sessionId);
            }

            super.appDisconnect(conn);
        } catch (Exception e) {
            log.error("Got error during disconnection", e);
        }
    }

    @Override
    public void playStream(String watcherId, String rtmpServerUrl, String stream) {
        try {
            log.info("playStream");
            log.info("watcher id = " + watcherId);
            log.info("rtmpServerUrl = " + rtmpServerUrl);
            log.info("stream = " + stream);
            IConnection conn = watchers.get(watcherId);
            if (conn != null) {
                log.info("invokeOnConnection playStream");
                ServiceUtils.invokeOnConnection(conn, "playStream", new Object[]{rtmpServerUrl, stream});
            }
        } catch (Exception e) {
            log.error("Got error during playStream", e);
        }
    }

    @Override
    public void stopStream(String watcherId) {
        try {
            log.info("stopStream");
            log.info("watcher id = " + watcherId);
            IConnection conn = watchers.get(watcherId);
            if (conn != null) {
                log.info("invokeOnConnection stopStream");
                ServiceUtils.invokeOnConnection(conn, "stopStream", null);
            }
        } catch (Exception e) {
            log.error("Got error during stopStream", e);
        }
    }

    @Override
    public void cameraOn(String cameraId) {
        try {
            log.info("cameraOn");
            log.info("camera id = " + cameraId);
            IConnection conn = cameras.get(cameraId);
            if (conn != null) {
                log.info("invokeOnConnection cameraOn");
                ServiceUtils.invokeOnConnection(conn, "cameraOn", null);
            }
        } catch (Exception e) {
            log.error("Got error during cameraOn", e);
        }
    }

    @Override
    public void cameraOff(String cameraId) {
        try {
            log.info("cameraOff");
            log.info("camera id = " + cameraId);
            IConnection conn = cameras.get(cameraId);
            if (conn != null) {
                log.info("invokeOnConnection cameraOff");
                ServiceUtils.invokeOnConnection(conn, "cameraOff", null);
            }
        } catch (Exception e) {
            log.error("Got error during cameraOff", e);
        }
    }

    @Override
    public void streamPublishStart(IBroadcastStream stream) {
        try {
            log.info("streamPublishStart: {}; {}", stream, stream.getPublishedName());
            String sessionId = sessionIdByConnectionMap.get(Red5.getConnectionLocal());
            userConnectionQueue.broadcastingStarted(sessionId);
            super.streamPublishStart(stream);
            resampler.startTranscodingStream(stream, Red5.getConnectionLocal().getScope());
        } catch (Exception e) {
            log.error("Got error during streamPublishStart", e);
        }
    }

    @Override
    public void streamBroadcastClose(IBroadcastStream stream) {
        try {
            log.info("streamBroadcastClose: {}; {}", stream, stream.getPublishedName());
            String sessionId = sessionIdByConnectionMap.get(Red5.getConnectionLocal());
            userConnectionQueue.broadcastingStoped(sessionId);
            resampler.stopTranscodingStream(stream, Red5.getConnectionLocal().getScope());
            super.streamBroadcastClose(stream);
        } catch (Exception e) {
            log.error("Got error during streamBroadcastClose", e);
        }
    }

    @Override
    public void getFreeStreamsNumberFromAllServers() {
        refreshFreeRtmpStreamsNumber();
    }

    private void refreshFreeRtmpStreamsNumber() {
        long freeRtmpStreamsNumber = rtmpServerCapacity - cameras.size();
        log.info("refreshFreeRtmpStreamsNumber freeRtmpStreamsNumber = {}", freeRtmpStreamsNumber);
        frontendServerQueue.refreshFreeRtmpStreamsNumber(rtmpServerAddress, freeRtmpStreamsNumber);
    }
}
