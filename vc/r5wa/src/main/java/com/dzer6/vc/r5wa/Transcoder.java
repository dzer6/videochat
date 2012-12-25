package com.dzer6.vc.r5wa;

import com.xuggle.red5.IAudioSamplesListener;
import com.xuggle.red5.IPacketListener;
import com.xuggle.red5.IVideoPictureListener;
import com.xuggle.red5.io.BroadcastStream;
import com.xuggle.red5.io.IRTMPEventIOHandler;
import com.xuggle.red5.io.Red5HandlerFactory;
import com.xuggle.red5.io.Red5Message;
import com.xuggle.red5.io.Red5StreamingQueue;
import com.xuggle.xuggler.Configuration;
import com.xuggle.xuggler.IAudioResampler;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IContainerFormat;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.ISimpleMediaFile;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import com.xuggle.xuggler.SimpleMediaFile;
import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmMonitor;
import etm.core.monitor.EtmPoint;
import java.util.Properties;
import org.apache.mina.core.buffer.IoBuffer;
import org.red5.logging.Red5LoggerFactory;
import org.red5.server.api.stream.IBroadcastStream;
import org.red5.server.api.stream.IStreamListener;
import org.red5.server.api.stream.IStreamPacket;
import org.red5.server.net.rtmp.event.AudioData;
import org.red5.server.net.rtmp.event.IRTMPEvent;
import org.red5.server.net.rtmp.event.Notify;
import org.red5.server.net.rtmp.event.VideoData;
import org.red5.server.stream.ClientBroadcastStream;
import org.slf4j.Logger;

/**
 * Transcodes video and audio of a particular type from a red5 stream
 * into a new audio and video red5 stream.
 * <p>
 * It does this by opening an input URL, decoding all packets
 * in the container, resampling the decoded data if needed,
 * and then encoding into a new container.
 * </p>
 * <p>
 * It also allows hooking of call-back
 * listeners so you can be notified of key event and
 * potentially modify decoded data before re-encoding.
 * </p>
 */
public class Transcoder implements Runnable {

  final private Logger log = Red5LoggerFactory.getLogger(this.getClass());
  private final EtmMonitor profiler = EtmManager.getEtmMonitor();
  // This line initializes the AAFFMPEG IO libraries and gets a factory
  // we can register streams with.
  final static private Red5HandlerFactory mFactory = Red5HandlerFactory.getFactory();
  private final IBroadcastStream mInputStream;
  private final Red5StreamingQueue mInputQueue;
  private final IStreamListener mInputListener;
  private final ClientBroadcastStream mOutputStream;
  private final ISimpleMediaFile mOutputInfo;
  private final IRTMPEventIOHandler mOutputHandler;
  private final IPacketListener mPacketListener;
  private final IVideoPictureListener mVideoPictureListener;
  private final IAudioSamplesListener mSamplesListener;
  private volatile boolean mIsRunning = false;
  private volatile boolean mKeepRunning = true;
  private String mInputURL;
  private String mOutputURL;
  private IContainer mOutContainer;
  private IStreamCoder mOutAudioCoder;
  private IStreamCoder mOutVideoCoder;
  private IContainer mInContainer;
  private IStreamCoder mInAudioCoder;
  private IStreamCoder mInVideoCoder;
  private int mAudioStreamId;
  private IAudioResampler mAudioResampler;
  private IVideoResampler mVideoResampler;
  private int mVideoStreamId;
  private String x264PresetsPath;

  /**
   * Create a new transcoder object.
   *
   * All listeners are set to null.
   *
   * @param aInputStream The stream to get input packets from.
   * @param aOutputStream The stream to publish output packets to.
   * @param aOutputInfo Meta data about what type of packets you want to publish.
   */
  public Transcoder(
          IBroadcastStream aInputStream,
          ClientBroadcastStream aOutputStream,
          ISimpleMediaFile aOutputInfo,
          String x264PresetsPath) {
    this(aInputStream, aOutputStream, aOutputInfo, null, null, null, x264PresetsPath);
  }

  /**
   * Create a new transcoder object.
   *
   * All listeners are set to null.
   *
   * @param aInputStream The stream to get input packets from.
   * @param aOutputStream The stream to publish output packets to.
   * @param aOutputInfo Meta data about what type of packets you want to publish.
   * @param aPacketListener A packet listener that will be called for interesting events.  Or null to disable.
   * @param aSamplesListener A Audio Samples listener that will be called for interesting events.  Or null to disable.
   * @param aPictureListener A Video Picture listener that will be called for interesting events.  Or null to disable.
   */
  public Transcoder(
          IBroadcastStream aInputStream,
          ClientBroadcastStream aOutputStream,
          ISimpleMediaFile aOutputInfo,
          IPacketListener aPacketListener,
          IAudioSamplesListener aSamplesListener,
          IVideoPictureListener aPictureListener,
          String x264PresetsPath) {
    if (aInputStream == null) {
      throw new IllegalArgumentException("must pass input stream");
    }
    if (aOutputStream == null) {
      throw new IllegalArgumentException("must pass output stream");
    }
    if (aOutputInfo == null) {
      throw new IllegalArgumentException("must pass output stream info");
    }

    mInputStream = aInputStream;
    mOutputStream = aOutputStream;
    mOutputInfo = aOutputInfo;
    mInputQueue = new Red5StreamingQueue();
    mPacketListener = aPacketListener;
    mSamplesListener = aSamplesListener;
    mVideoPictureListener = aPictureListener;

    this.x264PresetsPath = x264PresetsPath;

    mAudioStreamId = -1;
    mVideoStreamId = -1;

    // Check that we have valid input and output formats if specified
    if (mOutputInfo.getContainerFormat() != null) {
      IContainerFormat fmt = mOutputInfo.getContainerFormat();
      if (!"flv".equals(fmt.getInputFormatShortName())) {
        throw new IllegalArgumentException("currently we only support inputs from FLV files");
      }
    }
    // Make sure if specifying audio that we have all required parameters set.
    if (mOutputInfo.hasAudio()) {
      if (!mOutputInfo.isAudioBitRateKnown() || mOutputInfo.getAudioBitRate() <= 0) {
        throw new IllegalArgumentException("must set audio bit rate when outputting audio");
      }
      if (!mOutputInfo.isAudioChannelsKnown() || mOutputInfo.getAudioChannels() <= 0) {
        throw new IllegalArgumentException("must set audio channels when outputting audio");
      }
      if (!mOutputInfo.isAudioSampleRateKnown() || mOutputInfo.getAudioSampleRate() <= 0) {
        throw new IllegalArgumentException("must set audio sample rate when outputting audio");
      }
      if (mOutputInfo.getAudioCodec() == ICodec.ID.CODEC_ID_NONE) {
        throw new IllegalArgumentException("must set audio code when outputting audio");
      }
    }
    if (mOutputInfo.hasVideo()) {
      if (!mOutputInfo.isVideoBitRateKnown() || mOutputInfo.getVideoBitRate() <= 0) {
        throw new IllegalArgumentException("must set video bit rate when outputting video");
      }
      if (!mOutputInfo.isVideoHeightKnown() || mOutputInfo.getVideoHeight() <= 0) {
        throw new IllegalArgumentException("must set video height when outputting video");
      }
      if (!mOutputInfo.isVideoWidthKnown() || mOutputInfo.getVideoWidth() <= 0) {
        throw new IllegalArgumentException("must set video width when outputting video");
      }
      if (mOutputInfo.getVideoCodec() == ICodec.ID.CODEC_ID_NONE) {
        throw new IllegalArgumentException("must set video codec when outputting video");
      }
      if (!IVideoResampler.isSupported(IVideoResampler.Feature.FEATURE_IMAGERESCALING)) {
        log.warn("Your installed version of AAFFMPEG doesn't support video resampling; Transcoding will fail if resizing is required");
      }
    }
    if (!(mOutputInfo.hasAudio() || mOutputInfo.hasVideo())) {
      throw new IllegalArgumentException("must output either audio or video");
    }

    // Make FFMPEG return back if a read takes longer than 2 seconds.  Unfortunately
    // it means FFMPEG will think the stream has ended, but them's the breaks.
    //mInputQueue.setReadTimeout(new TimeValue(2, TimeUnit.SECONDS));
    mInputListener = new IStreamListener() {
      @Override
      public void packetReceived(IBroadcastStream aStream, IStreamPacket aPacket) {
        EtmPoint point = profiler.createPoint(this.getClass().getName() + "#packetReceived");
        try {
          IoBuffer buf = aPacket.getData();
          if (buf != null) {
            buf.rewind();
          }
          if (buf == null || buf.remaining() == 0) {
            log.debug("skipping empty packet with no data");
            return;
          }

          if (aPacket instanceof AudioData) {
            log.debug("adding packet type: {}; ts: {}; on stream: {}", new Object[]{"AUDIO", aPacket.getTimestamp(), aStream.getPublishedName()});
            mInputQueue.put(new Red5Message(Red5Message.Type.AUDIO, (AudioData) aPacket));
          } else if (aPacket instanceof VideoData) {
            Red5Message.Type type = Red5Message.Type.INTERFRAME;
            VideoData dataPacket = (VideoData) aPacket;
            switch (dataPacket.getFrameType()) {
              case DISPOSABLE_INTERFRAME:
                type = Red5Message.Type.DISPOSABLE_INTERFRAME;
                break;
              case INTERFRAME:
                type = Red5Message.Type.INTERFRAME;
                break;
              case KEYFRAME:
              case UNKNOWN:
                type = Red5Message.Type.KEY_FRAME;
                break;
            }
            if (type != Red5Message.Type.DISPOSABLE_INTERFRAME) {// The FFMPEG FLV decoder doesn't handle disposable frames
              log.debug("  adding packet type: {}; ts: {}; on stream: {}", new Object[]{dataPacket.getFrameType(), aPacket.getTimestamp(), aStream.getPublishedName()});
              mInputQueue.put(new Red5Message(type, dataPacket));
            }
          } else if (aPacket instanceof IRTMPEvent) {
            log.debug("  adding packet type: {}; ts: {}; on stream: {}", new Object[]{"OTHER", aPacket.getTimestamp(), aStream.getPublishedName()});
            Red5Message.Type type = Red5Message.Type.OTHER;
            IRTMPEvent dataPacket = (IRTMPEvent) aPacket;
            mInputQueue.put(new Red5Message(type, dataPacket));
          } else {
            log.debug("dropping packet type: {}; ts: {}; on stream: {}", new Object[]{"UNKNOWN", aPacket.getTimestamp(), aStream.getPublishedName()});
          }
        } catch (InterruptedException ex) {
          log.error("exception: {}", ex);
        } finally {
          point.collect();
        }
      }
    };
    mOutputHandler = new IRTMPEventIOHandler() {

      /**
       * Reading not supported on this handler.
       * @return null
       */
      @Override
      public Red5Message read() throws InterruptedException {
        return null;
      }

      @Override
      public void write(Red5Message aMsg) throws InterruptedException {
        EtmPoint point = profiler.createPoint(this.getClass().getName() + "#write");
        try {

          IRTMPEvent event = aMsg.getData();
          if (event != null) {
            // we've live.  Tell Red5 that
            // this is only in the xuggler_timestamp_branch for now, so we leave commented out
            //event.setSourceType((byte)1);

            if (event instanceof AudioData) {
              log.debug("  broadcasting packet type: {}; ts: {}; on stream: {}", new Object[]{"AUDIO", event.getTimestamp(), mOutputStream.getPublishedName()});
            } else if (event instanceof VideoData) {
              VideoData dataPacket = (VideoData) event;
              log.debug("  broadcasting packet type: {}; ts:{}; on stream: {}", new Object[]{dataPacket.getFrameType(), event.getTimestamp(), mOutputStream.getPublishedName()});
            } else if (event instanceof Notify) {
              log.debug("  broadcasting patcket type: {}; ts: {}; on stream: {}", new Object[]{"NOTIFY", event.getTimestamp(), mOutputStream.getPublishedName()});
            } else {
              log.debug("Writing unknown event to output stream: {}; ts: {}", event, event.getTimestamp());
            }
            mOutputStream.dispatchEvent(event);
            event.release();
          }
        } finally {
          point.collect();
        }
      }
    };
  }

  /**
   * Is the main loop running?
   *
   * @see #run()
   * @return true if the loop is running, false otherwise.
   */
  public boolean isRunning() {
    return mIsRunning;
  }

  /**
   * Stop the {@link Transcoder} loop if it's running
   * on a separate thread.
   * <p>
   * It does this by sending a
   * {@link Red5Message} for the end of stream
   *
   * to the {@link Transcoder} and allowing it to
   * exit gracefully.
   * </p>
   * @see #run()
   */
  public void stop() {
    try {
      mInputQueue.put(new Red5Message(Red5Message.Type.END_STREAM, null));
    } catch (InterruptedException e) {
      log.error("exception: {}", e);
    }
    mKeepRunning = false;
  }

  /**
   * Open up all input and ouput containers (files)
   * and being transcoding.
   * <p>
   * The {@link Transcoder} requires its own thread to
   * do work on, and callers are responsible for
   * allocating the {@link Thread}.
   * </p>
   * <p>
   * This method does not return unless another thread
   * calls {@link Transcoder#stop()}, or it reaches
   * the end of a Red5 stream.  It is meant to
   * be passed as the {@link Runnable#run()} method
   * for a thread.
   * </p>
   */
  @Override
  public void run() {
    try {
      openContainer();
      transcode();
    } catch (Throwable e) {
      log.error("uncaught exception: " + e.getMessage());
      //e.printStackTrace();
    } finally {
      closeContainer();
    }
  }

  private void transcode() {
    int retval = -1;
    synchronized (this) {
      mIsRunning = true;
      notifyAll();
    }

    IPacket iPacket = IPacket.make();
    log.debug("Packets and Audio buffers created");

    while (mKeepRunning) {

      EtmPoint fullLoop = profiler.createPoint(this.getClass().getName() + "#transcode_loop");
      try {
        EtmPoint point = profiler.createPoint(this.getClass().getName() + "#readNextPacket");
        try {
          retval = mInContainer.readNextPacket(iPacket);
        } finally {
          point.collect();
        }
        if (retval < 0) {
          log.debug("container is empty; exiting transcoding thread");
          mKeepRunning = false;
          break;
        }
        log.debug("next packet read");
        IPacket decodePacket = iPacket;
        if (mPacketListener != null) {
          point = profiler.createPoint(this.getClass().getName() + "#PacketListener#preDecode");
          try {
            decodePacket = mPacketListener.preDecode(iPacket);
            if (decodePacket == null) {
              decodePacket = iPacket;
            }
          } finally {
            point.collect();
          }
        }
        openInputCoders(decodePacket); // reopen the input coders if we need to
        int i = decodePacket.getStreamIndex();
        if (i == mAudioStreamId) {
          log.debug("audio stream id matches: {}", i);
          if (mInAudioCoder == null) {
            throw new RuntimeException("audio coder not set up");
          }

          if (mOutputInfo.hasAudio()) {
            decodeAudio(decodePacket);
          } else {
            log.debug("dropping audio because output has no audio");
          }
        } else if (i == mVideoStreamId) {
          log.debug("video stream id matches: {}", i);
          if (mInVideoCoder == null) {
            throw new RuntimeException("video coder not set up");
          }

          if (mOutputInfo.hasVideo()) {
            decodeVideo(decodePacket);
          } else {
            log.debug("dropping video because output has no video");
          }
        } else {
          log.debug("dropping packet from stream we haven't set-up: {}", i);
        }
      } finally {
        fullLoop.collect();
      }
    }
  }

  private void openContainer() {
    EtmPoint point = profiler.createPoint(this.getClass().getName() + "#open");
    try {
      // set out thread name
      String threadName = "Transcoder[" + mInputStream.getPublishedName() + "]";
      log.debug("Changing thread name: {}; to {};", Thread.currentThread().getName(), threadName);
      Thread.currentThread().setName(threadName);
      int retval = -1;

      // First let's setup our input URL

      // Register a new listener; should hopefully start getting audio packets immediately
      log.debug("Adding packet listener to stream: {}", mInputStream.getPublishedName());
      mInputStream.addStreamListener(mInputListener);

      // Tell AAFFMPEG about our new input URL; we use the unique Red5 names for the url
      mInputURL = Red5HandlerFactory.DEFAULT_PROTOCOL + ":" + mInputStream.getName();
      ISimpleMediaFile inputInfo = new SimpleMediaFile();
      inputInfo.setURL(mInputURL);
      mFactory.registerStream(mInputQueue, inputInfo);

      mInContainer = IContainer.make();
      mInContainer.setInputBufferLength(65536);
      // NOTE: This will block until we get the later of the first audio if it has audio, or first video
      // if it has video
      log.debug("About to open input url: {}", mInputURL);
      IContainerFormat inFormat = IContainerFormat.make();
      inFormat.setInputFormat("flv"); // set the input format to avoid FFMPEG probing
      retval = mInContainer.open(mInputURL, IContainer.Type.READ, inFormat, true, false);
      if (retval < 0) {
        throw new RuntimeException("Could not open input: " + mInputURL);
      }

      // Now, let's first set up our output URL

      // Tell AAFFMPEG about out output URL
      mOutputURL = Red5HandlerFactory.DEFAULT_PROTOCOL + ":" + mOutputStream.getName();
      mOutputInfo.setURL(mOutputURL);
      // For the output URL, every time we get a packet we just dispatch it to
      // a stream; you also use a Red5StreamingQueue here if you wanted to
      // have another thread deal with broadcasting.
      mFactory.registerStream(mOutputHandler, mOutputInfo);

      mOutContainer = IContainer.make();
      mOutContainer.setInputBufferLength(65536);
      IContainerFormat outFormat = IContainerFormat.make();
      outFormat.setOutputFormat("flv", mOutputURL, null);
      retval = mOutContainer.open(mOutputURL, IContainer.Type.WRITE, outFormat);
      if (retval < 0) {
        throw new RuntimeException("could not open output: " + mOutputURL);
      }

      if (mOutputInfo.hasAudio()) {
        // Add an audio stream
        IStream outStream = mOutContainer.addNewStream(0);
        if (outStream == null) {
          throw new RuntimeException("could not add audio stream to output: " + mOutputURL);
        }
        IStreamCoder outCoder = outStream.getStreamCoder();
        ICodec.ID outCodecId = mOutputInfo.getAudioCodec();
        ICodec outCodec = ICodec.findEncodingCodec(outCodecId);
        if (outCodec == null) {
          log.error("Could not encode using the codec: {}", mOutputInfo.getAudioCodec());
          throw new RuntimeException("Could not encode using the codec: " + mOutputInfo.getAudioCodec());
        }
        outCoder.setCodec(outCodec);
        outCoder.setBitRate(mOutputInfo.getAudioBitRate());
        outCoder.setSampleRate(mOutputInfo.getAudioSampleRate());
        outCoder.setChannels(mOutputInfo.getAudioChannels());
        outCoder.setFlag(IStreamCoder.Flags.FLAG_QSCALE, false);

        outCoder.open();
        // if we get here w/o an exception, record the coder
        mOutAudioCoder = outCoder;
      }
      if (mOutputInfo.hasVideo()) {
        // Add an video stream
        IStream outStream = mOutContainer.addNewStream(1);
        if (outStream == null) {
          throw new RuntimeException("could not add video stream to output: " + mOutputURL);
        }
        IStreamCoder outCoder = outStream.getStreamCoder();
        ICodec.ID outCodecId = mOutputInfo.getVideoCodec();
        ICodec outCodec = ICodec.findEncodingCodec(outCodecId);
        if (outCodec == null) {
          log.error("Could not encode using the codec: {}", mOutputInfo.getAudioCodec());
          throw new RuntimeException("Could not encode using the codec: " + mOutputInfo.getAudioCodec());
        }
        outCoder.setCodec(outCodec);
        outCoder.setWidth(mOutputInfo.getVideoWidth());
        outCoder.setHeight(mOutputInfo.getVideoHeight());
        outCoder.setPixelType(mOutputInfo.getVideoPixelFormat());
        outCoder.setGlobalQuality(mOutputInfo.getVideoGlobalQuality());
        outCoder.setBitRate(mOutputInfo.getVideoBitRate());
        outCoder.setNumPicturesInGroupOfPictures(mOutputInfo.getVideoNumPicturesInGroupOfPictures());
        outCoder.setFrameRate(mOutputInfo.getVideoFrameRate());
        outCoder.setFlag(IStreamCoder.Flags.FLAG_QSCALE, false);

        if (mOutputInfo.getVideoTimeBase() != null) {
          outCoder.setTimeBase(mOutputInfo.getVideoTimeBase());
        } else {
          outCoder.setTimeBase(IRational.make(1, 1000));
        }

        outCoder.setProperty("tune", "zerolatency");
        outCoder.setProperty("intra-refresh", true);

        Properties prop = new Properties();
        prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(x264PresetsPath));
        Configuration.configure(prop, outCoder);

        outCoder.open();
        // if we get here w/o an exception, record the coder
        mOutVideoCoder = outCoder;
      }

      retval = mOutContainer.writeHeader();
      if (retval < 0) {
        throw new RuntimeException("could not write header for output");
      }
    } catch(Throwable t) {
      log.error("Open container error", t);
    } finally {
      point.collect();
    }

  }

  private void openInputCoders(IPacket packet) {

    IStreamCoder audioCoder = null;
    IStreamCoder videoCoder = null;
    if (mAudioStreamId == -1 || mVideoStreamId == -1) {
      int numStreams = mInContainer.getNumStreams();
      log.debug("found {} streams in {}", numStreams, mInputURL);

      for (int i = 0; i < numStreams; i++) {
        IStream stream = mInContainer.getStream(i);
        if (stream != null) {
          log.debug("found stream #{} in {}", i, mInputURL);
          IStreamCoder coder = stream.getStreamCoder();
          if (coder != null) {
            log.debug("got stream coder {} (type: {}) in {}", new Object[]{coder, coder.getCodecType(), mInputURL});
            if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO && // if audio
                mAudioStreamId == -1 && // and we haven't already initialized
                packet.getStreamIndex() == i ) { // and this packet is also audio
              log.debug("found audio stream: {} in {}", i, mInputURL);
              if (coder.getCodec() != null) {
                audioCoder = coder;
                mAudioStreamId = i;
              } else {
                log.error("could not find codec for audio stream: {}, {}", i, coder.getCodecID());
                throw new RuntimeException("Could not find codec for audio stream");
              }
            }
            if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO && // if video
                mVideoStreamId == -1 && // and we haven't already initialized
                packet.getStreamIndex() == i ) { // and this packet is also video
              log.debug("found video stream: {} in {}", i, mInputURL);
              if (coder.getCodec() != null) {
                videoCoder = coder;
                mVideoStreamId = i;
              } else {
                log.error("could not find codec for video stream: {}, {}", i, coder.getCodecID());
                throw new RuntimeException("Could not find codec for video stream");
              }
            }
          }
        }
      }
    }
    if (mAudioStreamId != -1 && mInAudioCoder == null) {
      log.debug("opening input audio coder; codec id: {}; actual codec: {}; sample rate: {}; channels: {}", new Object[]{audioCoder.getCodecID(), audioCoder.getCodec().getID(), audioCoder.getSampleRate(), audioCoder.getChannels()});
      if (audioCoder.open() < 0) {
        throw new RuntimeException("could not open audio coder for stream: " + mAudioStreamId);
      }
      mInAudioCoder = audioCoder;
    }
    if (mVideoStreamId != -1 && mInVideoCoder == null) {
      log.debug("opening input video coder; codec id: {}; actual codec: {}; width: {}; height: {}", new Object[]{videoCoder.getCodecID(), videoCoder.getCodec().getID(), videoCoder.getWidth(), videoCoder.getHeight()});
      if (videoCoder.open() < 0) {
        throw new RuntimeException("could not open video coder for stream: " + mVideoStreamId);
      }
      mInVideoCoder = videoCoder;
    }
  }

  private void openVideoResampler(IVideoPicture picture) {
    if (mVideoResampler == null && mOutVideoCoder != null) {
      if (picture.getWidth() <= 0 || picture.getHeight() <= 0) {
        throw new RuntimeException("frame has no data in it so cannot resample");
      }

      // We set up our resampler.
      if (mOutVideoCoder.getPixelType() != picture.getPixelType()
              || mOutVideoCoder.getWidth() != picture.getWidth()
              || mOutVideoCoder.getHeight() != picture.getHeight()) {
        mVideoResampler = IVideoResampler.make(
                mOutVideoCoder.getWidth(),
                mOutVideoCoder.getHeight(),
                mOutVideoCoder.getPixelType(),
                picture.getWidth(),
                picture.getHeight(),
                picture.getPixelType());
        if (mVideoResampler == null) {
          log.error("Could not create a video resampler; this object is only available in the GPL version of aaffmpeg");
          throw new RuntimeException("needed to resample video but couldn't allocate a resampler; you need the GPL version of AAFFMPEG installed?");
        }
        log.debug("Setup resample to convert \"{}x{} {} video\" to \"{}x{} {} video\" audio", new Object[]{mVideoResampler.getInputWidth(), mVideoResampler.getInputHeight(), mVideoResampler.getInputPixelFormat(), mVideoResampler.getOutputWidth(), mVideoResampler.getOutputHeight(), mVideoResampler.getOutputPixelFormat()});
      }
    }
  }

  private void openAudioResampler(IAudioSamples samples) {
    if (mAudioResampler == null && mOutAudioCoder != null) {
      if (mOutAudioCoder.getSampleRate() != samples.getSampleRate()
              || mOutAudioCoder.getChannels() != samples.getChannels()) {
        mAudioResampler = IAudioResampler.make(mOutAudioCoder.getChannels(),
                samples.getChannels(),
                mOutAudioCoder.getSampleRate(),
                samples.getSampleRate());
        if (mAudioResampler == null) {
          throw new RuntimeException("needed to resample audio but couldn't allocate a resampler");
        }
        log.debug("Setup resample to convert \"{}khz {} channel audio\" to \"{}khz {} channel\" audio", new Object[]{mAudioResampler.getInputRate(), mAudioResampler.getInputChannels(), mAudioResampler.getOutputRate(), mAudioResampler.getOutputChannels()});
      }
      // and we write the output header
      log.debug("Converting \"{} {}khz {} channel\" input audio to \"{} {}khz {} channel\" output audio", new Object[]{mInAudioCoder.getCodecID().toString(), samples.getSampleRate(), samples.getChannels(), mOutAudioCoder.getCodecID().toString(), mOutAudioCoder.getSampleRate(), mOutAudioCoder.getChannels()});
    }
  }

  private void closeContainer() {
    EtmPoint point = profiler.createPoint(this.getClass().getName() + "#close");
    try {
      try {
        mInputStream.removeStreamListener(mInputListener);
        if (mOutContainer != null) {
          mOutContainer.writeTrailer();
        }
        if (mOutAudioCoder != null) {
          mOutAudioCoder.close();
        }
        mOutAudioCoder = null;
        if (mInAudioCoder != null) {
          mInAudioCoder.close();
        }
        mInAudioCoder = null;
        if (mOutVideoCoder != null) {
          mOutVideoCoder.close();
        }
        mOutVideoCoder = null;
        if (mInVideoCoder != null) {
          mInVideoCoder.close();
        }
        mInVideoCoder = null;
        if (mOutContainer != null) {
          mOutContainer.close();
        }
        mOutContainer = null;
      } finally {
        synchronized (this) {
          mIsRunning = false;
          notifyAll();
        }
      }
    } finally {
      point.collect();
    }
  }

  private void writePacket(IPacket oPacket) {
    int retval;
    EtmPoint point;
    IPacket encodedPacket = oPacket;
    if (mPacketListener != null) {
      point = profiler.createPoint(this.getClass().getName() + "#PacketListener#postEncode");
      try {
        encodedPacket = mPacketListener.postEncode(oPacket);
        if (encodedPacket == null) {
          encodedPacket = oPacket;
        }
      } finally {
        point.collect();
      }
    }

    log.debug("ready to write packet");

    // Don't force interleaving of data
    point = profiler.createPoint(this.getClass().getName() + "#writePacket");
    try {
      retval = mOutContainer.writePacket(encodedPacket, false);
    } finally {
      point.collect();
    }

    if (retval < 0) {
      throw new RuntimeException("could not write output packet");
    }
    log.debug("write packet completed");
  }

  private void decodeVideo(IPacket decodePacket) {
    int retval = -1;
    // Note that we don't specify the input width and height; the StreamCoder will fill that
    // in when it decodes
    IVideoPicture inPicture = IVideoPicture.make(mInVideoCoder.getPixelType(), mInVideoCoder.getWidth(), mInVideoCoder.getHeight());
    log.debug("made frame to decode into; type: {}; width: {}; height: {}", new Object[]{inPicture.getPixelType(), inPicture.getWidth(), inPicture.getHeight()});
    // resampled video
    IVideoPicture reSample = null;

    EtmPoint point = null;

    int offset = 0;

    while (offset < decodePacket.getSize()) {
      log.debug("ready to decode video; keyframe: {}", decodePacket.isKey());
      point = profiler.createPoint(this.getClass().getName() + "#decodeVideo");
      try {
        retval = mInVideoCoder.decodeVideo(inPicture, decodePacket, offset);
      } finally {
        point.collect();
      }
      log.debug("decode video completed; packet size: {}; offset: {}; bytes consumed: {}; frame complete: {}; width: {}; height: {}", new Object[]{decodePacket.getSize(), offset, retval, inPicture.isComplete(), inPicture.getWidth(), inPicture.getHeight()});
      if (retval <= 0) {
        log.info("Could not decode video: {}", retval);
        return;
      }
      offset += retval;

      IVideoPicture postDecode = inPicture;
      if (mVideoPictureListener != null) {
        point = profiler.createPoint(this.getClass().getName() + "#VideoPictureListener#postDecode");
        try {
          postDecode = mVideoPictureListener.postDecode(inPicture);
          if (postDecode == null) {
            postDecode = inPicture;
          }
        } finally {
          point.collect();
        }
      }

      if (postDecode.isComplete()) {
        reSample = resampleVideo(postDecode);
      } else {
        reSample = postDecode;
      }
      if (reSample.isComplete()) {
        encodeVideo(reSample);
      }
    }
  }

  private void encodeVideo(IVideoPicture picture) {
    int retval;
    EtmPoint point;
    IPacket oPacket = IPacket.make();

    /**
     * NOTE: At this point reSamples contains the actual unencoded raw samples.
     *
     * The next step does an encoding, but you PROBABLY don't need to do that.
     * Instead, you could copy the reSamples.getSamples().getData(...) bytes
     * into your own structure and hand them off, but for now, we'll
     * try re-encoding as FLV with PCM embedded.
     */
    IVideoPicture preEncode = picture;
    if (mVideoPictureListener != null) {
      point = profiler.createPoint(this.getClass().getName() + "#VideoPictureListener#preEncode");
      try {
        preEncode = mVideoPictureListener.preEncode(picture);
        if (preEncode == null) {
          preEncode = picture;
        }
      } finally {
        point.collect();
      }
    }

    int numBytesConsumed = 0;
    if (preEncode.isComplete()) {
      log.debug("ready to encode video");

      point = profiler.createPoint(this.getClass().getName() + "#encodeVideo");
      try {
        retval = mOutVideoCoder.encodeVideo(oPacket, preEncode, 0);
      } finally {
        point.collect();
      }
      if (retval <= 0) {
        // If we fail to encode, complain loudly but still keep going
        log.error("could not encode video picture; continuing anyway");
      } else {
        log.debug("encode video completed");
        numBytesConsumed += retval;
      }
      if (oPacket.isComplete()) {
        writePacket(oPacket);
      }
    }
  }

  private IVideoPicture resampleVideo(IVideoPicture picture) {
    IVideoPicture reSample;
    EtmPoint point;

    openVideoResampler(picture);

    if (mVideoResampler != null) {
      log.debug("ready to resample video");

      IVideoPicture outPicture = IVideoPicture.make(mOutVideoCoder.getPixelType(), mOutVideoCoder.getWidth(), mOutVideoCoder.getHeight());

      IVideoPicture preResample = picture;
      if (mVideoPictureListener != null) {
        point = profiler.createPoint(this.getClass().getName() + "#VideoPictureListener#preResample");
        try {
          preResample = mVideoPictureListener.preResample(picture);
          if (preResample == null) {
            preResample = picture;
          }
        } finally {
          point.collect();
        }
      }

      point = profiler.createPoint(this.getClass().getName() + "#resample");
      int retval = -1;
      try {
        retval = mVideoResampler.resample(outPicture, preResample);
      } finally {
        point.collect();
      }
      if (retval < 0) {
        throw new RuntimeException("could not resample video");
      }
      log.debug("resampled input picture (type: {}; width: {}; height: {}) to output (type: {}; width: {}; height: {})", new Object[]{preResample.getPixelType(), preResample.getWidth(), preResample.getHeight(), outPicture.getPixelType(), outPicture.getWidth(), outPicture.getHeight()});
      IVideoPicture postResample = outPicture;
      if (mVideoPictureListener != null) {
        point = profiler.createPoint(this.getClass().getName() + "#VideoPictureListener#postResample");
        try {
          postResample = mVideoPictureListener.postResample(outPicture);
          if (postResample == null) {
            postResample = outPicture;
          }
        } finally {
          point.collect();
        }
      }

      reSample = postResample;
    } else {
      reSample = picture;
    }
    return reSample;
  }

  private void decodeAudio(IPacket decodePacket) {
    int retval = -1;
    IAudioSamples inSamples = IAudioSamples.make(1024, mInAudioCoder.getChannels());
    // resampled audio
    IAudioSamples reSamples = null;

    EtmPoint point = null;

    int offset = 0;

    while (offset < decodePacket.getSize()) {
      log.debug("ready to decode audio");
      point = profiler.createPoint(this.getClass().getName() + "#decodeAudio");
      try {
        retval = mInAudioCoder.decodeAudio(inSamples, decodePacket, offset);
      } finally {
        point.collect();
      }
      if (retval <= 0) {
        throw new RuntimeException("could not decode audio");
      }
      log.debug("decode audio completed");
      offset += retval;

      IAudioSamples postDecode = inSamples;
      if (mSamplesListener != null) {
        point = profiler.createPoint(this.getClass().getName() + "#AudioSamplesListener#postDecode");
        try {
          postDecode = mSamplesListener.postDecode(inSamples);
          if (postDecode == null) {
            postDecode = inSamples;
          }
        } finally {
          point.collect();
        }
      }

      if (postDecode.isComplete()) {
        reSamples = resampleAudio(postDecode);
      } else {
        reSamples = postDecode;
      }

      if (reSamples.isComplete()) {
        encodeAudio(reSamples);
      }
    }
  }

  private void encodeAudio(IAudioSamples samples) {
    int retval;
    EtmPoint point;
    IPacket oPacket = IPacket.make();

    /**
     * NOTE: At this point reSamples contains the actual unencoded raw samples.
     *
     * The next step does an encoding, but you PROBABLY don't need to do that.
     * Instead, you could copy the reSamples.getSamples().getData(...) bytes
     * into your own structure and hand them off, but for now, we'll
     * try re-encoding as FLV with PCM embedded.
     */
    IAudioSamples preEncode = samples;
    if (mSamplesListener != null) {
      point = profiler.createPoint(this.getClass().getName() + "#AudioSamplesListener#preEncode");
      try {
        preEncode = mSamplesListener.preEncode(samples);
        if (preEncode == null) {
          preEncode = samples;
        }
      } finally {
        point.collect();
      }
    }

    int numSamplesConsumed = 0;
    while (numSamplesConsumed < preEncode.getNumSamples()) {
      log.debug("ready to encode audio");

      point = profiler.createPoint(this.getClass().getName() + "#encodeAudio");
      try {
        retval = mOutAudioCoder.encodeAudio(oPacket, preEncode, numSamplesConsumed);
      } finally {
        point.collect();
      }
      if (retval <= 0) {
        // If we fail to encode, complain loudly but still keep going
        log.error("could not encode audio samples; continuing anyway");
        // and break the loop since these samples are now suspect
        break;
      }
      log.debug("encode audio completed");

      numSamplesConsumed += retval;

      if (oPacket.isComplete()) {
        writePacket(oPacket);
      }
    }
  }

  private IAudioSamples resampleAudio(IAudioSamples samples) {
    IAudioSamples reSamples;
    EtmPoint point;

    openAudioResampler(samples);

    IAudioSamples outSamples = IAudioSamples.make(1024, mOutAudioCoder.getChannels());

    if (mAudioResampler != null && samples.getNumSamples() > 0) {
      log.debug("ready to resample audio");

      IAudioSamples preResample = samples;
      if (mSamplesListener != null) {
        point = profiler.createPoint(this.getClass().getName() + "#AudioSamplesListener#preResample");
        try {
          preResample = mSamplesListener.preResample(samples);
          if (preResample == null) {
            preResample = samples;
          }
        } finally {
          point.collect();
        }
      }

      point = profiler.createPoint(this.getClass().getName() + "#resample");
      int retval = -1;
      try {
        retval = mAudioResampler.resample(outSamples, preResample, preResample.getNumSamples());
      } finally {
        point.collect();
      }
      if (retval < 0) {
        throw new RuntimeException("could not resample audio");
      }
      log.debug("resampled {} input samples ({}khz {} channels) to {} output samples ({}khz {} channels)", new Object[]{preResample.getNumSamples(), preResample.getSampleRate(), preResample.getChannels(), outSamples.getNumSamples(), outSamples.getSampleRate(), outSamples.getChannels()});

      IAudioSamples postResample = outSamples;
      if (mSamplesListener != null) {
        point = profiler.createPoint(this.getClass().getName() + "#AudioSamplesListener#postResample");
        try {
          postResample = mSamplesListener.postResample(outSamples);
          if (postResample == null) {
            postResample = outSamples;
          }
        } finally {
          point.collect();
        }
      }

      reSamples = postResample;
    } else {
      reSamples = samples;
    }
    return reSamples;
  }
}
