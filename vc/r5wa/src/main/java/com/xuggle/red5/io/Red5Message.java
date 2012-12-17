/*******************************************************************************
 * Copyright (c) 2008, 2010 Xuggle Inc.  All rights reserved.
 *  
 * This file is part of Xuggle-Xuggler-Red5.
 *
 * Xuggle-Xuggler-Red5 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Xuggle-Xuggler-Red5 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Xuggle-Xuggler-Red5.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/

package com.xuggle.red5.io;

import org.apache.mina.core.buffer.IoBuffer;
import org.red5.logging.Red5LoggerFactory;
import org.red5.server.net.rtmp.event.AudioData;
import org.red5.server.net.rtmp.event.IRTMPEvent;
import org.red5.server.net.rtmp.event.Notify;
import org.red5.server.net.rtmp.event.VideoData;
import org.slf4j.Logger;

import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmMonitor;
import etm.core.monitor.EtmPoint;

/**
 * A wrapper object that contains a type, and the message data.
 * 
 * It is passed around via the {@link Red5Handler} for RTMP messages.
 * 
 * We use this instead of a raw IRTMPEvent because queues often can't
 * handle null events, and we sometimes want to pass null IRTMPValues
 * (for example for HEADER and END_STREAM messages).
 * 
 * @author aclarke
 *
 */
public class Red5Message
{
  final private Logger log = Red5LoggerFactory.getLogger(this.getClass());
  private final EtmMonitor profiler = EtmManager.getEtmMonitor();

  /**
   * The types of messages that can be put into
   * a {@link Red5Message}
   * @author aclarke
   *
   */
  public enum Type
  {
    /**
     * A HEADER.  The payload is usually null.
     */
    HEADER,
    /**
     * A Video B or P MPEG frame (interframe).
     * The payload is usually a {@link VideoData}
     * object.
     */
    INTERFRAME,
    /**
     * A Disposable Interframe.  The payload
     * is usually a {@link VideoData} object.
     * 
     * Note that most FFMPEG decoders seem to
     * choke on these frames, so feel free to dispose
     * them.
     */
    DISPOSABLE_INTERFRAME,
    
    /**
     * A Video I MPEG frame (key frame).
     * The payload is usually a {@link VideoData}
     * object.
     */
    KEY_FRAME,
    /**
     * An audio packet.  The payload is
     * usually a {@link AudioData} object.
     */
    AUDIO,
    /**
     * Some other type of data.  For FLV
     * METADATA can creep in here.  The payload
     * is ually a {@link Notify} object.
     */
    OTHER,
    /**
     * A end of stream marker.  The payload
     * is usually null.
     */
    END_STREAM;
    
    /**
     * Is this message audio data?
     * @return true if audio; false if a chicken.
     */
    public boolean isAudio() {
      return this == AUDIO;
    }
    /**
     * Is this message video data?
     * @return true if any type of video; false if a nostril.
     */
    public boolean isVideo() {
      return (this == INTERFRAME || this == KEY_FRAME || this==DISPOSABLE_INTERFRAME);
    }
    /**
     * Is this message other data?
     * @return true if other; false if a Thundercats Ho!
     */
    public boolean isOther() {
      return this == OTHER;
    }
    /**
     * Is this an end of stream marker?
     * @return true if the end; false if the end is not nigh.
     */
    public boolean isEnd() {
      return this == END_STREAM;
    }
    /**
     * Is this the beginning of a stream?
     * @return true if a header; false if it feels like it.  what do you care.  you never
     * take me dancing anymore.  what happened to the stream I feel in love with?  have
     * I gotten too fat?
     */
    public boolean isHeader() {
      return this == HEADER;
    }
  };
  
  /**
   * The different Codecs that FLV (and hence Red5)
   * can support as of Flash 9.
   * 
   * These must be translated separately into
   * Xuggler codecs if you need to.
   * @author aclarke
   *
   */
  public enum VideoCodec
  {
    JPEG(1),
    H263(2),
    Screen(3),
    VP6(4),
    VP6a(5),
    Screen2(6),
    AVC(7);
    
    private int mId;
    private VideoCodec(int id)
    {
      mId = id;
    }
    /**
     * Returns back a numeric id for this codec,
     * that happens to correspond to the numeric
     * identifier that FLV will use for this codec.
     * @return the codec id
     */
    public int getId()
    {
      return mId;
    }
  }
  
  /**
   * The set of Audio Codecs that FLV (and hence
   * Red5) support as of Flash 9.
   * @author aclarke
   *
   */
  public enum AudioCodec
  {
    PCM(0),
    ADPCM(1),
    MP3(2),
    PCM_LE(3),
    Nellymoser_16khz(4),
    Nellymoser_8khz(5),
    Nellymoser(6),
    PCM_ALAW(7),
    PCM_MULAW(8),
    RESERVED(9),
    AAC(10),
    SPEEX(11),
    MP3_8khz(14),
    DEVICE_SPECIFIC(15);
    
    private int mId;
    private AudioCodec(int id)
    {
      mId = id;
    }
    /**
     * Returns back a numeric id for this codec,
     * that happens to correspond to the numeric
     * identifier that FLV will use for this codec.
     * @return the codec id
     */
    public int getId()
    {
      return mId;
    }
  }
  
  final private Type mType;
  final private IRTMPEvent mData;
  final private AudioCodec mAudioCodec;
  final private VideoCodec mVideoCodec;
  final private int mSampleRate;
  final private int mSampleSize;
  final private boolean mStereo;
  
  /**
   * Crate a new message
   * @param aType The type of the message.
   * @param aData The payload
   */
  public Red5Message(Type aType, IRTMPEvent aData)
  {
    EtmPoint point = profiler.createPoint(this.getClass().getName()+"#<init>");
    mType = aType;
    mData = aData;
    int dataSize = -1;

    VideoCodec vidCodec=null;
    AudioCodec audCodec=null;
    int sampleRate = 0;
    int sampleSize = 0;
    boolean isStereo=false;
    try {
      if (mData != null)
      {
        // Retain so we can cross a thread boundary
        mData.retain();
      }
      if (aType.isAudio())
      {
        if (aData == null)
          throw new IllegalArgumentException("cannot pass null audio data");
        if (!(aData instanceof AudioData))
          throw new IllegalArgumentException("cannot pass non AudioData for audio");
        IoBuffer data = ((AudioData)aData).getData();
        if (data == null)
          throw new IllegalArgumentException("cannot pass empty audio data for audio");
        data.rewind();
        dataSize = data.remaining();
        if (dataSize == 0)
          throw new IllegalArgumentException("cannot pass empty audio data");
        
        byte firstByte = data.get(0);
        byte soundFormat = (byte)((firstByte>>4) & 0x0F);
        switch (soundFormat)
        {
        case 0:
          audCodec = AudioCodec.PCM;
          break;
        case 1:
          audCodec = AudioCodec.ADPCM;
          break;
        case 2:
          audCodec = AudioCodec.MP3;
          break;
        case 3:
          audCodec = AudioCodec.PCM_LE;
          break;
        case 4:
          audCodec = AudioCodec.Nellymoser_16khz;
          break;
        case 5:
          audCodec = AudioCodec.Nellymoser_8khz;
          break;
        case 6:
          audCodec = AudioCodec.Nellymoser;
          break;
        case 7:
          audCodec = AudioCodec.PCM_ALAW;
          break;
        case 8:
          audCodec = AudioCodec.PCM_MULAW;
          break;
        case 9:
          audCodec = AudioCodec.RESERVED;
          break;
        case 10:
          audCodec = AudioCodec.AAC;
          break;
        case 11:
          audCodec = AudioCodec.SPEEX;
          break;
        case 14:
          audCodec = AudioCodec.MP3_8khz;
          break;
        case 15:
          audCodec = AudioCodec.DEVICE_SPECIFIC;
          break;
        default:
          throw new RuntimeException("Unrecognized type of audio data: " + soundFormat);
        }

        byte soundRate = (byte)((firstByte>>2) & 0x03);
        if (audCodec == AudioCodec.SPEEX)
          sampleRate = 16000;
        else if (audCodec == AudioCodec.Nellymoser_8khz)
          sampleRate = 8000;
        else if (audCodec == AudioCodec.Nellymoser_16khz)
          sampleRate = 16000;
        else if (audCodec == AudioCodec.MP3_8khz)
          sampleRate = 8000;
        else
        {
          switch(soundRate)
          {
          case 0:
            sampleRate = 5512;
            break;
          case 1:
            sampleRate = 11025;
            break;
          case 2:
            sampleRate = 22050;
            break;
          case 3:
            sampleRate = 44100;
            break;
          }
        }

        byte soundSize = (byte)((firstByte>>1) & 0x01);
        sampleSize = soundSize > 0 ? 16 : 8;
        byte stereo = (byte)((firstByte) & 0x01);
        isStereo = stereo > 0;

      }
      else if (aType.isVideo())
      {
        if (aData == null)
          throw new IllegalArgumentException("cannot pass null video data");
        if (!(aData instanceof VideoData))
          throw new IllegalArgumentException("cannot pass non VideoData for video");
        IoBuffer data = ((VideoData)aData).getData();
        if (data == null)
          throw new IllegalArgumentException("cannot pass empty video data for video");
        data.rewind();
        dataSize = data.remaining();
        byte firstByte = data.get(0);
        byte codecId = (byte)((firstByte)& 0x0F);
        switch(codecId)
        {
        case 1:
          vidCodec = VideoCodec.JPEG;
          break;
        case 2:
          vidCodec = VideoCodec.H263;
          break;
        case 3:
          vidCodec = VideoCodec.Screen;
          break;
        case 4:
          vidCodec = VideoCodec.VP6;
          break;
        case 5:
          vidCodec = VideoCodec.VP6a;
          break;
        case 6:
          vidCodec = VideoCodec.Screen2;
          break;
        case 7:
          vidCodec = VideoCodec.AVC;
          break;
        default:
          throw new RuntimeException("Unrecognized video codec: " + codecId);
        }
      }
      mVideoCodec = vidCodec;
      mAudioCodec = audCodec;
      mSampleRate = sampleRate;
      mSampleSize = sampleSize;
      mStereo = isStereo;
      log.debug("Created red5 message; type: {}; data: {}; data size: {}; video codec: {}; audio codec: {}; "
          +"sample rate: {}; sample size: {}; stereo: {}",
          new Object[]{
            mType,
            mData,
            dataSize,
            mVideoCodec,
            mAudioCodec,
            mSampleRate,
            mSampleSize,
            mStereo
          }
          );
    }
    catch (RuntimeException ex)
    {
      log.error("Got exception: {}", ex);
      throw ex;
    }
    finally
    {
      point.collect();
    }
  }

  /**
   * Returns the data in this RTMP event.
   * 
   * Important note: Caller MUST call {@link IRTMPEvent#release()} on the returned object.
   *
   * @return The data in this message
   */
  public IRTMPEvent getData()
  {
    return mData;
  }
  /**
   * Returns the type of this red5 message
   * @return The type
   */
  public Type getType()
  {
    return mType;
  }
  
  /**
   * Get the audio sample rate in hertz
   * @return Audio sample rate in hertz, or 0 if not audio.
   */
  public int getAudioSampleRate()
  {
    return mSampleRate;
  }
  
  /**
   * Get the audio sample size.
   * @return audio sample size, in bits, or 0 if not audio.
   */
  public int getAudioSampleSize()
  {
    return mSampleSize;
  }
  
  /**
   * Find out if the audio stream is present.
   * @return true if stereo; false if mono or not audio.
   */
  public boolean isAudioStereo()
  {
    return mStereo;
  }

  /**
   * @return The audio codec, or null if not audio.
   */
  public AudioCodec getAudioCodec()
  {
    return mAudioCodec;
  }

  /**
   * @return The video codec, or null if not video.
   */
  public VideoCodec getVideoCodec()
  {
    return mVideoCodec;
  }

}
