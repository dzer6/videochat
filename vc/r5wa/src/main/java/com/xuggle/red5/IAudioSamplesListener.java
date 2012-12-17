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

import com.xuggle.xuggler.IAudioSamples;

/**
 * A listener for intercepting samples decoded, re-sampled,and encoded by the {@link Transcoder}.
 */
public interface IAudioSamplesListener
{
  /**
   * Called by the {@link Transcoder} right after it decodes a packet.
   * Callers can return a new object which will be substituted into
   * the {@link Transcoder} work flow instead of the original decoded object.
   * @param aObject The object that was decoded.
   * @return A new object to replace aObject, or aObject to do nothing.
   */
  IAudioSamples postDecode(IAudioSamples aObject);
  
  /**
   * Called by the {@link Transcoder} right before it resamples an object.
   * Callers can return a new object which will be substituted into
   * the {@link Transcoder} work flow instead of the aObject object.
   * @param aObject The object that is about to be resampled.
   * @return A new object to replace aObject, or aObject to do nothing.
   */
  IAudioSamples preResample(IAudioSamples aObject);
  
  
  /**
   * Called by the {@link Transcoder} right after it resamples an object.
   * Callers can return a new object which will be substituted into
   * the {@link Transcoder} work flow instead of the aObject object.
   * @param aObject The object that was just resampled.
   * @return A new object to replace aObject, or aObject to do nothing.
   */
  IAudioSamples postResample(IAudioSamples aObject);
  
  /**
   * Called by the {@link Transcoder} right before it encodes an object.
   * Callers can return a new object which will be substituted into
   * the {@link Transcoder} work flow instead of the aObject object.
   * @param aObject The object that is about to be encoded.
   * @return A new object to replace aObject, or aObject to do nothing.
   */
  IAudioSamples preEncode(IAudioSamples aObject);

}
