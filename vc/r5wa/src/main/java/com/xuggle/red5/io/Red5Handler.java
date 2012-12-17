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

import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.red5.io.amf.Output;
import org.red5.io.object.Serializer;
import org.red5.io.utils.IOUtils;
import org.red5.logging.Red5LoggerFactory;

import org.red5.server.net.rtmp.event.AudioData;
import org.red5.server.net.rtmp.event.IRTMPEvent;
import org.red5.server.net.rtmp.event.Notify;
import org.red5.server.net.rtmp.event.Unknown;
import org.red5.server.net.rtmp.event.VideoData;
import org.red5.server.stream.IStreamData;

import org.slf4j.Logger;

import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.ISimpleMediaFile;
import com.xuggle.xuggler.SimpleMediaFile;
import com.xuggle.xuggler.ITimeValue;
import com.xuggle.xuggler.io.IURLProtocolHandler;

import com.xuggle.red5.io.Red5Message.Type;

import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmMonitor;
import etm.core.monitor.EtmPoint;

/**
 * And implementation of IURLProtocolHandler that converts from
 * IRTMPEvent into a format that ffmpeg can read.
 * 
 * It does this by creating FLV headers and reading FLV headers.
 * 
 * It only supports the FLV container as of Flash 9.  No support
 * for H264 FLV files here, but you can build your own.
 * 
 * @author aclarke
 *
 */
public class Red5Handler implements IURLProtocolHandler
{
  private final EtmMonitor profiler = EtmManager.getEtmMonitor();

  // Include following 4-byte "size of prior tag"
  private static final int FLV_FILE_HEADER_SIZE = 9 + 4;
  // Include following 4-byte "size of prior tag"
  private static final int FLV_TAG_HEADER_SIZE = 11 + 4;

  final private Logger log = Red5LoggerFactory.getLogger(this.getClass());

  IRTMPEventIOHandler mHandler;

  IoBuffer mCurrentInputBuffer = null;
  IoBuffer mCurrentOutputBuffer = null;

  String mUrl;
  int mOpenFlags;

  private long mReadPosition = 0;
  private long mWritePosition = 0;

  private int mFirstAudioTimestamp = -1;
  private int mFirstVideoTimestamp = -1;
  private boolean mReadHeader = false;
  private boolean mReadEndOfStream = false;
  private boolean mReadMetaData = false;

  private boolean mWrittenHeader = false;
  private final ISimpleMediaFile mStreamInfo;

  // Only package members can create
  Red5Handler(IRTMPEventIOHandler handler,
      ISimpleMediaFile metaInfo,
      String url,
      int flags)
  {
    if (metaInfo == null)
      metaInfo = new SimpleMediaFile(); // get defaults if none specified
    mStreamInfo = metaInfo;
    mHandler = handler;
  }

  private int unsafe_close() throws InterruptedException
  {
    // Odd, but FFMPEG told us to close.  For now, we just
    // ignore.  Long term, we should probably signal the
    // BufferStream that we're no longer actively handling it.
    if (mCurrentInputBuffer != null)
    {
      mCurrentInputBuffer = null;
    }
    if (mCurrentOutputBuffer != null)
    {
      mCurrentOutputBuffer = null;
    }
    if (mOpenFlags == IURLProtocolHandler.URL_RDWR
        || mOpenFlags == IURLProtocolHandler.URL_WRONLY_MODE)
    {
      // As a convention, we send a IMediaDataWrapper object wrapping NULL for end of streams
      mHandler.write(new Red5Message(Type.END_STREAM, null));
    }
    mWrittenHeader = false;

    return 0;
  }

  private int unsafe_open(String url, int flags)
  {
    // For an open, we assume the ProtocolManager has done it's job
    // correctly and we're working on the right input and output
    // streams.
    mUrl = url;
    mOpenFlags = flags;

    if (mCurrentInputBuffer != null)
    {
      mCurrentInputBuffer = null;
    }
    if (mCurrentOutputBuffer != null)
    {
      mCurrentOutputBuffer = null;
    }
    mReadEndOfStream = false;
    mReadHeader = false;
    mReadMetaData = false;
    mFirstVideoTimestamp = -1;
    mFirstAudioTimestamp = -1;

    return 0;
  }

  private int unsafe_read(byte[] buf, int size) throws InterruptedException
  {
    int bytesToRead = 0;

    if (mReadEndOfStream)
    {
      // we've read the end of stream already; we shouldn't be reading any more.
      return -1;
    }

    // if we don't have a buffer, create one.
    if (mCurrentInputBuffer == null)
    {
      // start with the requested side.
      mCurrentInputBuffer = IoBuffer.allocate(size); 

      // and let it auto grow.
      mCurrentInputBuffer.setAutoExpand(true);
      // and set the limit to zero.
      mCurrentInputBuffer.flip();
    }
    bytesToRead = mCurrentInputBuffer.remaining();

    while (bytesToRead < size && bytesToRead == 0 && !mReadEndOfStream)
    {
      // keep getting new messages until we have enough
      // to fill the requested buffer.
      // now the magic begins.
      IRTMPEvent event = null;
      Red5Message msg = null;

      try
      {
        msg = mHandler.read();
        Red5Message.Type type = msg.getType();
        event = msg.getData();

        switch (type)
        {
        case HEADER:
          // We need to create and send a header.
          if (mReadHeader)
          {
            log.warn("Already sent a header for this stream; ignoring");
          }
          else
          {
            appendFLVHeader(mCurrentInputBuffer);
            mReadHeader = true;
            mReadEndOfStream = false;
          }
          break;
        case END_STREAM:
          // end of stream; send back -1;
          mReadHeader = false;
          mReadEndOfStream = true;
          break;
        case INTERFRAME:
        case DISPOSABLE_INTERFRAME:
        case KEY_FRAME:
        case AUDIO:
        case OTHER:
          // Valid stream data; parse and send in right
          // format to the caller
          if (!mReadHeader)
          {
            log.warn("Starting to send messages for a stream, but "
                + "we have not yet sent a header.  Faking "
                + "it until we make it: {}", msg);
            // we need to jam a header in.
            appendFLVHeader(mCurrentInputBuffer);
            mReadHeader = true;
          }
          if (!mReadMetaData )
          {
            if (event.getDataType() == 0x12)
            {
              mReadMetaData = true;
            } else {
              // fake the meta data
              log.debug("did not get meta data as first packet; so faking some meta data: {}", msg);
              appendMetaData(0, mCurrentInputBuffer);
              mReadMetaData=true;
            }
          }
          appendRTMPEvent(event, mCurrentInputBuffer);
          break;
        }
      }
      finally
      {
        if (event != null)
        {
          event.release();
          event = null;
        }
      }
      bytesToRead = mCurrentInputBuffer.remaining();
      log.trace("Bytes to read: {}; size: {}", bytesToRead, size);
    }

    // and copy into the requested buffer
    int bytesToCopy = Math.min(size, bytesToRead);

    if (bytesToCopy > 0)
    {
      mCurrentInputBuffer.get(buf, 0, bytesToCopy);
      // remove the read bytes
      mCurrentInputBuffer.compact();
      // and set the limit to the bytes remaining
      mCurrentInputBuffer.flip();
    }
    mReadPosition += bytesToCopy;
    return bytesToCopy;
  }

  private long unsafe_seek(long offset, int whence)
  {
    long newPos = -1;

    switch (whence)
    {
    case SEEK_END:
    case SEEK_CUR:
    case SEEK_SET:
    case SEEK_SIZE:
    default:
      // Unsupported
      newPos = -1;
    break;
    }

    return newPos;
  }

  private int unsafe_write(byte[] buf, int size) throws InterruptedException
  {
    int retval = 0;
    if (mCurrentOutputBuffer == null)
    {
      mCurrentOutputBuffer = IoBuffer.allocate(size
          + FLV_FILE_HEADER_SIZE + FLV_TAG_HEADER_SIZE);
      mCurrentOutputBuffer.setAutoExpand(true);
      // reset position and limit to 0.  we use the limit() as the
      // capacity we've got to deal with, and let these
      // buffers auto expand.
      mCurrentOutputBuffer.flip();
    }
    // add the bytes that FFMPEG just sent us.
    int oldPos = mCurrentOutputBuffer.position();
    // put the pointer to the end of the buffer.
    int bufEnd = oldPos + mCurrentOutputBuffer.remaining();
    mCurrentOutputBuffer.position(bufEnd);
    // append the new data to the end of our buffer
    mCurrentOutputBuffer.put(buf, 0, size);
    // reset back to the position we had before so we can
    // keep parsing from where we were.
    mCurrentOutputBuffer.position(oldPos);

    // We always return that we read size even if we don't fully
    // parse things yet.
    retval = size;
    mWritePosition += size;

    // now let's figure out if we have enough to generate
    // an RTMPEvent
    int bytesToProcess = mCurrentOutputBuffer.remaining();
    if (!mWrittenHeader)
    {
      if (bytesToProcess >= FLV_FILE_HEADER_SIZE)
      {
        // there had better be a header at the start of this buffer
        parseFLVHeader(mCurrentOutputBuffer);

        // and send a header message
        mHandler.write(new Red5Message(Type.HEADER, null));
        // compact down our buffer.
        mCurrentOutputBuffer.compact();
        mCurrentOutputBuffer.flip();
        bytesToProcess = mCurrentOutputBuffer.remaining();
        mWrittenHeader = true;
      }
      else
      {
        // we don't yet have enough data to parse the header, but
        // let's return for now and fix on a later call to write.
        do {} while(false);
      }
    }

    // note that we can write both a tag and a header in one
    // call to write, hence the check here.
    while (mWrittenHeader && bytesToProcess >= FLV_TAG_HEADER_SIZE)
    {
      // The next thing we should try to process should be a tag.

      // get and ignore the data type
      int tagStartPos = mCurrentOutputBuffer.position();
      mCurrentOutputBuffer.get();
      // find the size of the body of this tag.
      int bodySize = IOUtils.readMediumInt(mCurrentOutputBuffer);
      // reset the read head to ignore the 4 bytes we read getting
      // here.
      mCurrentOutputBuffer.position(tagStartPos);

      if (bodySize + FLV_TAG_HEADER_SIZE <= bytesToProcess)
      {
        // we have enough to parse a tag.
        // parse the tag.
        IRTMPEvent event = parseFLVTag(mCurrentOutputBuffer);
        if (event != null)
        {
          Red5Message.Type type = Red5Message.Type.OTHER;

          if (event instanceof VideoData)
          {
            switch (((VideoData) event).getFrameType())
            {
            case DISPOSABLE_INTERFRAME:
              type = Red5Message.Type.DISPOSABLE_INTERFRAME;
              break;
            case INTERFRAME:
              type = Red5Message.Type.INTERFRAME;
              break;
            case KEYFRAME:
              type = Red5Message.Type.KEY_FRAME;
              break;
            case UNKNOWN:
              type = Red5Message.Type.OTHER;
              break;
            }
            if (mFirstVideoTimestamp < 0)
              mFirstVideoTimestamp = event.getTimestamp();

          }
          else if (event instanceof AudioData)
          {
            type = Red5Message.Type.AUDIO;
            if (mFirstAudioTimestamp < 0)
              mFirstAudioTimestamp = event.getTimestamp();
          }
          else
          {
            type = Red5Message.Type.OTHER;
          }

          // and pass it to our buffer
          // note: since we created the event, we don't
          // need a retain on it; but the consumer end does
          // need to release.
          mHandler.write(new Red5Message(type, event));
        }
        // compact out the tag we just read
        mCurrentOutputBuffer.compact();
        mCurrentOutputBuffer.flip();
        bytesToProcess = mCurrentOutputBuffer.remaining();
      }
      else
      {
        // we don't have enough data for a tag yet, so
        // just return for now and fill up on the next call
        // to write.
        // so exit the while loop
        break;
      }
    }
    return retval;
  }

  private IRTMPEvent parseFLVTag(IoBuffer in)
  {
    IRTMPEvent retval = null;
    EtmPoint point = profiler.createPoint(this.getClass().getName()+"#parseFLVTag");
    try {
    byte junkByte = 0;

    // Byte 0 is a data type.
    byte dataType = in.get();

    // Bytes 1-3 are the size of the body of this tag.  Note this utility
    // reads 3 bytes into an int, which is what we want.
    int bodySize = IOUtils.readUnsignedMediumInt(in);

    // Bytes 4-6 are the bottom 3 bytes of the timestamp int.
    int timestamp = IOUtils.readUnsignedMediumInt(in);
    // Byte 7 is the top byte of the timestamp.
    junkByte = in.get();

    // To make this match FLVReader, hard code junkByte to 0.  This will however mean that
    // videos longer than about 4 hours will fail
    // junkByte = 0;
    timestamp += (junkByte & 0xFF) * 256 * 256 * 256;

    // Bytes 8-10 are the stream id, which is always zero in FLV files.
    junkByte = in.get();
    junkByte = in.get();
    junkByte = in.get();

    // I hate what I'm about to do here, but it appears the easiest
    // way.
    IoBuffer bodyBuffer = null;
    try
    {
      byte[] copyBuf = new byte[1024];
      int totalBytesToCopy = bodySize;
      bodyBuffer = IoBuffer.allocate(bodySize);

      while (totalBytesToCopy > 0)
      {
        int bytesToCopy = Math.min(copyBuf.length, totalBytesToCopy);
        in.get(copyBuf, 0, bytesToCopy);
        bodyBuffer.put(copyBuf, 0, bytesToCopy);
        totalBytesToCopy -= bytesToCopy;
      }
      bodyBuffer.flip();
      // now we have the body of the actual tag.
      switch (dataType)
      {
      case 0x08: // AUDIO
        retval = new AudioData(bodyBuffer);
        bodyBuffer = null;
        break;
      case 0x09: // VIDEO
        retval = new VideoData(bodyBuffer);
        bodyBuffer = null;
        break;
      case 0x12: // Metadata
        retval = new Notify(bodyBuffer);
        bodyBuffer = null;
        //        log.debug("dropping meta data tag ({} bytes) from stream: {}", bodySize
        //            + FLV_TAG_HEADER_SIZE, this);
        break;
      default:
        // unknown
        log.debug("got unknown type of tag ({}); dropping on floor: {}",
            dataType, this);
      break;
      }
    }
    finally
    {
      if (bodyBuffer != null)
      {
        bodyBuffer = null;
      }
    }
    if (retval != null)
    {
      retval.setTimestamp(timestamp);
    }
    // The last 4 bytes is the size of the prior tag
    int previousTagSize = in.getInt();
    if (previousTagSize != bodySize + FLV_TAG_HEADER_SIZE - 4)
      throw new RuntimeException("error parsing tag header: size mismatch");
    } finally {
      point.collect();
    }
    return retval;
  }

  /*
   * @return # of bytes processed
   */
  private int parseFLVHeader(IoBuffer in)
  {
    EtmPoint point = profiler.createPoint(this.getClass().getName()+"#parseFLVHeader");
    try {

    // Check that the marker in the header is correct
    Byte[] fileMarker =
    {
        'F', 'L', 'V'
    };
    for (int i = 0; i < fileMarker.length; i++)
    {
      byte leByte = in.get();
      if (leByte != fileMarker[i])
      {
        throw new RuntimeException("Invalid header in streaming FLV file");
      }
    }

    // next, get the version
    byte version = in.get();
    log.trace("flv header version: {}", version);

    byte flags = in.get();
    log.trace("flv header flags: {}", flags);

    int headerSize = in.getInt();
    if (headerSize != FLV_FILE_HEADER_SIZE - 4)
      throw new RuntimeException("wrong header size");

    int priorTagSize = in.getInt();
    if (priorTagSize != 0)
      throw new RuntimeException("wrong prior tag size");
    } finally {
      point.collect();
    }
    return FLV_FILE_HEADER_SIZE;
  }

  private boolean unsafe_isStreamed(String url, int flags)
  {
    return true;
  }

  public String toString()
  {
    return this.getClass().getName() + ":" + mUrl;
  }

  private void appendFLVTag(byte dataType, int timestamp,
      IoBuffer data, IoBuffer in)
  {
    EtmPoint point = profiler.createPoint(this.getClass().getName()+"#appendFLVTag");
    try
    {
      int oldPos = in.position();
      int bufEnd = in.limit();
      // put us at the end of the buffer
      in.position(bufEnd);

      data.rewind();
      int msgBodySize = data.remaining();

      in.put(dataType);

      // Body Size (this method writes the bottom 3 bytes as
      // a 24-byte integer).
      IOUtils.writeMediumInt(in, (msgBodySize));

      // Timestamp; FLV Timestamps are annoying.
      // First 3 bytes is the UPPER 
      byte[] timestampBytes = new byte[4];
      timestampBytes[0] = (byte) ((timestamp >>> 16) & 0xFF);
      timestampBytes[1] = (byte) ((timestamp >>> 8) & 0xFF);
      timestampBytes[2] = (byte) (timestamp & 0xFF);
      timestampBytes[3] = (byte) ((timestamp >>> 24) & 0xFF);
      in.put(timestampBytes);

      // Reserved 3 bytes set to zero (StreamId)
      IOUtils.writeMediumInt(in, 0);

      // Now, write out out the actual data
      in.put(data);

      // Officially we're done with the tag, and now are in the
      // next message in the FLV stream.
      // Which happens to be the size of the previous tag.
      in.putInt(msgBodySize + FLV_TAG_HEADER_SIZE - 4);

      // now reset us back to the old position
      in.position(oldPos);
    } finally {
      point.collect();
    }
  }

  private void appendRTMPEvent(IRTMPEvent event, IoBuffer in)
  {
    EtmPoint point = profiler.createPoint(this.getClass().getName()+"#appendRTMPEvent");
    try
    {
      IoBuffer data = null;

      if (event instanceof IStreamData)
      {
        data = ((IStreamData) event).getData();
      }
      else if (event instanceof Unknown)
      {
        data = ((Unknown) event).getData();
      }
      if (data != null)
      {
        appendFLVTag(event.getDataType(), event.getTimestamp(), data, in);
      }
      else
      {
        throw new RuntimeException("No data in tag.");
      }
    } finally {
      point.collect();
    }
  }

  private void appendFLVHeader(IoBuffer in)
  {
    EtmPoint point = profiler.createPoint(this.getClass().getName()+"#appendFLVHeader");
    try {
      int oldPos = in.position();
      // set us to the end of the buffer
      in.position(in.limit());

      // Add the header items.

      // 'F'
      in.put((byte) 0x46);
      // 'L'
      in.put((byte) 0x4C);
      // 'V'
      in.put((byte) 0x56);

      // Write version: always 1
      in.put((byte) 0x01);

      // Both video and audio, which is 0x04 & 0x01 or 0x05
      // IMPORTANT NOTE:
      // There is a "feature" in FFMPEG that wreaks havoc with this setting.  If 
      // we don't say that every stream has both audio and video, FFMPEG will assume a new
      // stream may show up at any point in time and add a video stream.  As a result when
      // you try to get information about the file, it will keep reading ahead until it finds
      // the first audio and the first video packet.
      // However if you NEVER add a video or audio packet, then we will hang forever.

      // AAFFMPEG works around this by clearing this flag for FLV files; but then we MUST
      // ensure we never ever add data audio or video data if we said it wouldn't be there.

      byte audioVideoFlag = 0;
      if (mStreamInfo.hasAudio())
        audioVideoFlag |= 0x04;
      if (mStreamInfo.hasVideo())
        audioVideoFlag |= 0x01;
      in.put(audioVideoFlag);

      // Total size of header, not including the 4 bytes for the "last tag"
      in.putInt(FLV_FILE_HEADER_SIZE - 4);

      // And the size of the last tag, which for the header is always zero.
      // Always zero
      in.putInt(0);

      // and put us back where we were before
      in.position(oldPos);

    } finally {
      point.collect();
    }
  }

  private void addMetaData(Map<Object, Object> params, String key, Number value)
  {
    log.debug("add metaData[{}]={}", key, value);
    params.put(key, value);
  }
  private void addMetaData(Map<Object, Object> params, String key, Boolean value)
  {
    log.debug("add metaData[{}]={}", key, value);
    params.put(key, value);
  }  
  private void appendMetaData(int timestamp, IoBuffer in)
  {
    EtmPoint point = profiler.createPoint(this.getClass().getName()+"#appendMetaData");
    try {
      log.trace("Writing meta data for FFMPEG");
      Map<Object, Object> params = new HashMap<Object, Object>(4);
      if (mStreamInfo.hasAudio())
      {
        if (mStreamInfo.isAudioChannelsKnown())
        {
          boolean value = mStreamInfo.getAudioChannels()!=1;
          addMetaData(params, "stereo", Boolean.valueOf(value));
        }
        if (mStreamInfo.isAudioSampleRateKnown())
        {
          int value = mStreamInfo.getAudioSampleRate();
          addMetaData(params, "audiosamplerate", new Double(value));
        }
        {
          int value = 16;
          addMetaData(params, "audiosamplesize", new Double(value));
        }
        ICodec.ID audCodec = mStreamInfo.getAudioCodec();
        if (audCodec != null && audCodec != ICodec.ID.CODEC_ID_NONE)
        {
          int flvCodecID = 0;
          switch(audCodec)
          {
          case CODEC_ID_PCM_S16BE:
            flvCodecID = 0;
            break;
          case CODEC_ID_PCM_S16LE:
            flvCodecID = 3;
            break;
          case CODEC_ID_ADPCM_SWF:
            flvCodecID = 1;
            break;
          case CODEC_ID_MP3:
            if (mStreamInfo.isAudioSampleRateKnown() && mStreamInfo.getAudioSampleRate()==8000)
              flvCodecID = 14;
            else
              flvCodecID = 2;
            break;
          case CODEC_ID_NELLYMOSER:
            if (mStreamInfo.isAudioSampleRateKnown() && mStreamInfo.getAudioSampleRate()==8000)
              flvCodecID = 5;
            else
              flvCodecID = 6;
            break;
          case CODEC_ID_AAC:
            flvCodecID = 10;
            break;
          case CODEC_ID_SPEEX:
            flvCodecID = 11;
            break;
          }
          {
            int value = flvCodecID;
            addMetaData(params, "audiocodecid", new Double(value));
          }
        }
      }
      if (mStreamInfo.hasVideo())
      {
        if (mStreamInfo.isVideoWidthKnown())
        {
          double value = mStreamInfo.getVideoWidth();
          addMetaData(params, "width", new Double(value));
        }
        if (mStreamInfo.isVideoHeightKnown())
        {
          double value = mStreamInfo.getVideoHeight();
          addMetaData(params, "height", new Double(value));
        }
        if (mStreamInfo.isVideoBitRateKnown())
        {
          double value = mStreamInfo.getVideoBitRate() / 1024.0; // in kbits per sec
          addMetaData(params, "videodatarate", new Double(value));
        }
        IRational frameRate = mStreamInfo.getVideoFrameRate();
        if (frameRate != null)
        {
          double value = frameRate.getDouble();
          addMetaData(params, "framerate", new Double(value));
        }
        ICodec.ID vidCodec = mStreamInfo.getVideoCodec();
        if (vidCodec != null &&
            vidCodec != ICodec.ID.CODEC_ID_NONE)
        {
          int flvCodecID = 2;
          switch(vidCodec)
          {
          case CODEC_ID_FLV1:
            flvCodecID = 2;
            break;
          case CODEC_ID_FLASHSV:
            flvCodecID = 3;
            break;
          case CODEC_ID_VP6F:
            flvCodecID = 4;
            break;
          case CODEC_ID_VP6A:
            flvCodecID = 5;
            break;
          case CODEC_ID_H264:
            flvCodecID = 7;
            break;
          }
          {
            int value = flvCodecID;
            addMetaData(params, "videocodecid", new Double(value));
          }
        }
      }
      ITimeValue duration = mStreamInfo.getDuration();
      if (duration != null)
      {
        double value = duration.get(ITimeValue.Unit.MILLISECONDS) / 1000.0;
        addMetaData(params, "duration", new Double(value));
      }

      // hey by default we're NON seekable streams; deal with it.
      addMetaData(params, "canSeekToEnd", Boolean.FALSE);

      IoBuffer amfData = IoBuffer.allocate(1024);
      amfData.setAutoExpand(true);
      Output amfOutput = new Output(amfData);
      amfOutput.writeString("onMetaData");

      amfOutput.writeMap(params, new Serializer());

      amfData.flip();
      log.trace("Writing AMF object to stream: {} bytes; first byte: {}",
          amfData.remaining(),
          amfData.get(0));
      appendFLVTag((byte)0x12, 0, amfData, in);
    } finally {
      point.collect();
    }
  }

  /*
   * These following methods all wrap the unsafe methods in
   * try {} catch {} blocks to ensure we don't pass an exception
   * back to the native C++ function that calls these.
   * 
   * (non-Javadoc)
   * @see com.xuggle.videojuggler.ffmpegio.IURLProtocolHandler#close()
   */
  public int close()
  {
    int retval = -1;
    try
    {
      retval = unsafe_close();
    }
    catch (Exception ex)
    {
      log.error("got uncaught exception: {}", ex);
    }
    log.trace("close({}); {}", mUrl, retval);

    return retval;
  }

  public int open(String url, int flags)
  {
    int retval = -1;
    EtmPoint point = profiler.createPoint(this.getClass().getName()+"#open");
    try
    {
      retval = unsafe_open(url, flags);
    }
    catch (Exception ex)
    {
      log.error("got uncaught exception: {}", ex);
    }
    finally
    {
      point.collect();
    }
    log.trace("open({}, {}); {}", new Object[] { url, flags, retval });
    return retval;
  }

  public int read(byte[] buf, int size)
  {
    int retval = -1;
    EtmPoint point = profiler.createPoint(this.getClass().getName()+"#read");
    try
    {
      retval = unsafe_read(buf, size);
    }
    catch (Exception ex)
    {
      log.error("got uncaught exception: {}", ex);
    }
    finally
    {
      point.collect();
    }
    log.trace("read({}, {}); {}", new Object[] { mUrl, size, retval });
    return retval;
  }

  public long seek(long offset, int whence)
  {
    long retval = -1;
    EtmPoint point = profiler.createPoint(this.getClass().getName()+"#seek");
    try
    {
      retval = unsafe_seek(offset, whence);
    }
    catch (Exception ex)
    {
      log.error("got uncaught exception: {}", ex);
    }
    finally
    {
      point.collect();
    }    log.trace("seek({}, {}, {}); {}", new Object[] { mUrl, offset, whence, retval });
    return retval;
  }

  public int write(byte[] buf, int size)
  {
    int retval = -1;
    EtmPoint point = profiler.createPoint(this.getClass().getName()+"#write");
    try
    {
      retval = unsafe_write(buf, size);
    }
    catch (Exception ex)
    {
      log.error("got uncaught exception: {}", ex);
    }
    finally
    {
      point.collect();
    }
    log.trace("write({}, {}); {}", new Object[] { mUrl, size, retval });

    return retval;
  }

  public boolean isStreamed(String url, int flags)
  {
    boolean retval = false;
    EtmPoint point = profiler.createPoint(this.getClass().getName()+"#isStreamed");
    try
    {
      retval = unsafe_isStreamed(url, flags);
    }
    catch (Exception ex)
    {
      log.error("got uncaught exception: {}", ex);
    }
    finally
    {
      point.collect();
    }
    log.trace("isStreamed({}, {}); {}", new Object[] { mUrl, flags, retval });

    return retval;
  }
}
