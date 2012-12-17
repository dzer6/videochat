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

import com.xuggle.red5.io.Red5Message.Type;
import com.xuggle.utils.TimeValue;
import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmMonitor;
import etm.core.monitor.EtmPoint;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;

/**
 * This class is an implementation of {@link IRTMPEventIOHandler}
 * 
 * It works as a blocking queue where readers block until at least one event
 * is available, and writers block until space is available.
 * 
 * It's up to the user to make sure that only one thread writes and only one thread
 * reads to avoid deadlock.
 * 
 * Lastly, this queue will attempt to cache reads from the queue so that we minimize
 * the amount of blocking required.  In tests this can lead to up to a 10% performance
 * improvement.
 * 
 * @author aclarke
 *
 */
public class Red5StreamingQueue extends LinkedBlockingQueue<Red5Message>
implements IRTMPEventIOHandler
{
  final private Logger log = Red5LoggerFactory.getLogger(this.getClass());
  private final EtmMonitor profiler = EtmManager.getEtmMonitor();

  /**
   * A default serial id
   */
  private static final long serialVersionUID = 1L;

  private TimeValue mReadTimeout = null;
  private TimeValue mWriteTimeout = null;
  
  private Queue<Red5Message> mCacheQueue = new LinkedList<Red5Message>();

  public Red5StreamingQueue()
  {
    log.trace("<init>");
  }

  public Red5Message read() throws InterruptedException
  {
    //    log.debug("PRE  take");
    Red5Message result = null;
    EtmPoint point = profiler.createPoint(this.getClass().getName()+"#read");
    
    try {
      if (mCacheQueue.size() > 0)
      {
        result = mCacheQueue.poll();
      } else {
        // Always drain currently available to the cache; this method doesn't
        // block, so we may need to block later.
        this.drainTo(mCacheQueue);

        // See if the drain added anything to the cache 
        if (mCacheQueue.size() > 0)
        {
          result = mCacheQueue.poll();
        }
        else
        {
          EtmPoint missPoint = profiler.createPoint(this.getClass().getName()+"#read;cache-miss");
          try
          {
            if (mReadTimeout == null)
            {
              result = super.take();
            } else {
              result = super.poll(mReadTimeout.get(TimeUnit.MICROSECONDS), TimeUnit.MICROSECONDS);
            }
          }
          finally
          {
            missPoint.collect();
          }
        }
      }
      if (result == null)
      {
        result = new Red5Message(Type.END_STREAM, null);
      }
      //    log.debug("POST take: {}", result);
    } finally {
      point.collect();
    }
    return result;
  }

  public void write(Red5Message msg) throws InterruptedException
  {
    //    log.debug("PRE  put: {}", msg);
    EtmPoint point = profiler.createPoint(this.getClass().getName()+"#write");
    try {
      if (mWriteTimeout==null)
      {
        super.put(msg);
      }
      else
      {
        super.offer(msg, mWriteTimeout.get(TimeUnit.MICROSECONDS), TimeUnit.MICROSECONDS);
      }
    } finally {
      point.collect();
    }
    //    log.debug("POST put");
  }

  /**
   * Set the amount of time we will wait on a queue before giving up on reading.  If
   * null, we will wait forever.
   * @param readTimeout the readTimeout to set.  null means wait forever.
   */
  public void setReadTimeout(TimeValue readTimeout)
  {
    mReadTimeout = readTimeout;
  }

  /**
   * The amount of time we'll wait on a queue before giving up on reading.  If null,
   * we will wait forever.
   * 
   * @return the readTimeout
   */
  public TimeValue getReadTimeout()
  {
    return mReadTimeout;
  }

  /**
   * Set the amount of time we'll wait when adding to the queue.  Null means we'll wait forever.
   * @param writeTimeout the writeTimeout to set
   */
  public void setWriteTimeout(TimeValue writeTimeout)
  {
    mWriteTimeout = writeTimeout;
  }

  /**
   * Get the amount of time we'll wait when adding to the the queue.  Null means we'll wait forever. 
   * @return the writeTimeout
   */
  public TimeValue getWriteTimeout()
  {
    return mWriteTimeout;
  }

}
