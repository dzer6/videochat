package com.dzer6.vc.r5wa;

import com.xuggle.red5.io.BroadcastStream;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.ISimpleMediaFile;
import com.xuggle.xuggler.SimpleMediaFile;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.red5.logging.Red5LoggerFactory;
import org.red5.server.api.IContext;
import org.red5.server.api.scope.IBroadcastScope;
import org.red5.server.api.scope.IScope;
import org.red5.server.api.stream.IBroadcastStream;
import org.red5.server.api.stream.IClientBroadcastStream;
import org.red5.server.scope.BroadcastScope;
import org.red5.server.stream.ClientBroadcastStream;
import org.red5.server.stream.IProviderService;
import org.slf4j.Logger;

public class VideoTranscoder {

    final private Logger log = Red5LoggerFactory.getLogger(VideoTranscoder.class);
    final private Map<String, ClientBroadcastStream> mOutputStreams = Collections.synchronizedMap(new HashMap<String, ClientBroadcastStream>());
    final private Map<String, Transcoder> mTranscoders = Collections.synchronizedMap(new HashMap<String, Transcoder>());
    private int audioBitRate = 16384;
    private int audioChannels = 1;
    private int audioSampleRate = 22050;
    private int fps = 15;
    private int videoBitRate = 300000;
    private int videoHeight = 240;
    private int videoWidth = 320;
    private int gop = 200;
    private String x264PresetsPath;
    private String streamPrefix;
    private String audioCodecName;

    public String getAudioCodecName() {
        return audioCodecName;
    }

    public void setAudioCodecName(String audioCodecName) {
        this.audioCodecName = audioCodecName;
    }

    public int getAudioBitRate() {
        return audioBitRate;
    }

    public void setAudioBitRate(int audioBitRate) {
        this.audioBitRate = audioBitRate;
    }

    public int getAudioChannels() {
        return audioChannels;
    }

    public void setAudioChannels(int audioChannels) {
        this.audioChannels = audioChannels;
    }

    public int getAudioSampleRate() {
        return audioSampleRate;
    }

    public void setAudioSampleRate(int audioSampleRate) {
        this.audioSampleRate = audioSampleRate;
    }

    public int getFps() {
        return fps;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public int getVideoBitRate() {
        return videoBitRate;
    }

    public void setVideoBitRate(int videoBitRate) {
        this.videoBitRate = videoBitRate;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public void setVideoHeight(int videoHeight) {
        this.videoHeight = videoHeight;
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public void setVideoWidth(int videoWidth) {
        this.videoWidth = videoWidth;
    }

    public int getGop() {
        return gop;
    }

    public void setGop(int gop) {
        this.gop = gop;
    }

    public void setStreamPrefix(String streamPrefix) {
        this.streamPrefix = streamPrefix;
    }

    public void setX264PresetsPath(String x264PresetsPath) {
        this.x264PresetsPath = x264PresetsPath;
    }

    /**
     * Starts transcoding this stream. This method is a no-op if this stream is already a stream copy created by this
     * transcoder.
     *
     * @param aStream The stream to copy.
     * @param aScope The application scope.
     */
    public void startTranscodingStream(IBroadcastStream aStream, IScope aScope) {
        log.debug("startTranscodingStream({},{})", aStream.getPublishedName(), aScope.getName());
        if (aStream.getPublishedName().startsWith(streamPrefix)) {
            log.debug("Not making a copy of a copy: {}", aStream.getPublishedName());
            return;
        }
        log.debug("Making transcoded version of: {}", aStream.getPublishedName());

        /*
         * Now, we need to set up the output stream we want to broadcast to. Turns out aaffmpeg-red5 provides one of
         * those.
         */
        String outputName = streamPrefix + aStream.getPublishedName();
        ClientBroadcastStream outputStream = new ClientBroadcastStream();
        outputStream.setName(outputName);
        outputStream.setPublishedName(outputName);
        outputStream.setScope(aScope);

        IContext context = aScope.getContext();

        IProviderService providerService = (IProviderService) context.getBean(IProviderService.BEAN_NAME);
        if (providerService.registerBroadcastStream(aScope, outputName, outputStream)) {
            IBroadcastScope bsScope = (BroadcastScope) providerService.getLiveProviderInput(aScope, outputName, true);
            bsScope.setClientBroadcastStream((IClientBroadcastStream)outputStream);
        } else {
            log.error("Got a fatal error; could not register broadcast stream");
            throw new RuntimeException("fooey!");
        }
        mOutputStreams.put(aStream.getPublishedName(), outputStream);
        outputStream.start();

        /**
         * Now let's give aaffmpeg-red5 some information about what we want to transcode as.
         */
        ISimpleMediaFile outputStreamInfo = new SimpleMediaFile();
        outputStreamInfo.setHasAudio(true);
        outputStreamInfo.setAudioBitRate(audioBitRate);
        outputStreamInfo.setAudioChannels(audioChannels);
        outputStreamInfo.setAudioSampleRate(audioSampleRate);
        outputStreamInfo.setAudioCodec(ICodec.findDecodingCodecByName(audioCodecName).getID());
        outputStreamInfo.setHasVideo(true);
        // Unfortunately the Trans-coder needs to know the width and height
        // you want to output as; even if you don't know yet.
        outputStreamInfo.setVideoWidth(videoWidth);
        outputStreamInfo.setVideoHeight(videoHeight);
        outputStreamInfo.setVideoBitRate(videoBitRate);
        outputStreamInfo.setVideoCodec(ICodec.ID.CODEC_ID_H264);
        outputStreamInfo.setVideoPixelFormat(IPixelFormat.Type.YUV420P);
        outputStreamInfo.setVideoNumPicturesInGroupOfPictures(gop);
        outputStreamInfo.setVideoTimeBase(IRational.make(1, fps));
        outputStreamInfo.setVideoFrameRate(IRational.make(fps, 1));
        //outputStreamInfo.setVideoGlobalQuality(0);

        /**
         * And finally, let's create out transcoder
         */
        Transcoder transcoder = new Transcoder(aStream, outputStream, outputStreamInfo, null, null, null, x264PresetsPath);
        Thread transcoderThread = new Thread(transcoder);
        transcoderThread.setDaemon(true);
        mTranscoders.put(aStream.getPublishedName(), transcoder);
        log.debug("Starting transcoding thread for: {}", aStream.getPublishedName());
        transcoderThread.start();
    }

    /**
     * Stop transcoding a stream.
     *
     * @param aStream The stream to stop transcoding.
     * @param aScope The application scope.
     */
    public void stopTranscodingStream(IBroadcastStream aStream, IScope aScope) {
        log.debug("stopTranscodingStream({},{})", aStream.getPublishedName(), aScope.getName());
        String inputName = aStream.getPublishedName();
        Transcoder transcoder = mTranscoders.get(inputName);
        if (transcoder != null) {
            transcoder.stop();
        }
        ClientBroadcastStream outputStream = mOutputStreams.get(inputName);
        if (outputStream != null) {
            outputStream.stop();
        }
    }
}
