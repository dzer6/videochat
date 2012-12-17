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

/**
 * Called by the UrlProtocolHandler to read and
 * write IRTMPEvents
 * 
 * @author aclarke
 *
 */
public interface IRTMPEventIOHandler
{
  /**
   * Called by the aaffmpeg handler to get the next IRTMPEvent message.
   * 
   * @return The next IAVMessage; blocks until a message is ready.
   * @throws InterruptedException if interrupted while waiting
   */
  Red5Message read() throws InterruptedException;
  /**
   * Writes the given message.
   * 
   * @param msg The message to write
   * @throws InterruptedException if interrupted while waiting
   */
  void write(Red5Message msg) throws InterruptedException;
}
