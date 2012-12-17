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

package com.xuggle.red5;

import com.xuggle.xuggler.IPacket;

/**
 * A listener for intercepting {@link IPacket} objects just after they are decoded and just before
 * they are encoded by the {@link Transcoder}.
 */
public interface IPacketListener
{
  /**
   * Called by the {@link Transcoder} before it decodes a packet.  Callers can
   * return a new packet which will be substituted for aObject.
   * @param aObject The packet the transcoder is about to decode.
   * @return A new object to encode instead, or aObject to do nothing.
   */
  IPacket preDecode(IPacket aObject);
  /**
   * Called by the {@link Transcoder} right after it encodes an object.
   * Callers can return a new packet which will be substituted into
   * the {@link Transcoder} work flow instead of the aObject packet.
   * @param aObject The packet that was just encoded.
   * @return A new object to replace aObject, or aObject to do nothing.
   */
  IPacket postEncode(IPacket aObject);
}
