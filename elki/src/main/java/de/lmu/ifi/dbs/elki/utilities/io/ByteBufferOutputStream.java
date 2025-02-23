package de.lmu.ifi.dbs.elki.utilities.io;

/*
 This file is part of ELKI:
 Environment for Developing KDD-Applications Supported by Index-Structures

 Copyright (C) 2015
 Ludwig-Maximilians-Universität München
 Lehr- und Forschungseinheit für Datenbanksysteme
 ELKI Development Team

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Wrap an existing ByteBuffer as OutputStream.
 * 
 * @author Erich Schubert
 * 
 * @apiviz.has ByteBuffer
 */
public class ByteBufferOutputStream extends OutputStream {
  /**
   * The actual buffer we're using.
   */
  final ByteBuffer buffer;

  /**
   * Constructor.
   * 
   * @param buffer ByteBuffer to wrap.
   */
  public ByteBufferOutputStream(ByteBuffer buffer) {
    super();
    this.buffer = buffer;
  }
  
  @Override
  public void write(int b) {
    buffer.put((byte) b);
  }

  @Override
  public void write(byte[] b, int off, int len) {
    buffer.put(b, off, len);
  }
}