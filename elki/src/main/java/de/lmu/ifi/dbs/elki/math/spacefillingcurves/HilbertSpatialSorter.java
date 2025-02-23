package de.lmu.ifi.dbs.elki.math.spacefillingcurves;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.lmu.ifi.dbs.elki.data.spatial.SpatialComparable;
import de.lmu.ifi.dbs.elki.utilities.BitsUtil;
import de.lmu.ifi.dbs.elki.utilities.documentation.Reference;

/**
 * Sort object along the Hilbert Space Filling curve by mapping them to their
 * Hilbert numbers and sorting them.
 * 
 * Objects are mapped using 31 bits per dimension.
 * 
 * Reference:
 * <p>
 * D. Hilbert<br />
 * Über die stetige Abbildung einer Linie auf ein Flächenstück<br />
 * In: Mathematische Annalen, 38(3)
 * </p>
 * 
 * @author Erich Schubert
 * 
 * @apiviz.composedOf HilbertRef
 */
@Reference(authors = "D. Hilbert", title = "Über die stetige Abbildung einer Linie auf ein Flächenstück", booktitle = "Mathematische Annalen, 38(3)")
public class HilbertSpatialSorter extends AbstractSpatialSorter {
  /**
   * Constructor.
   */
  public HilbertSpatialSorter() {
    super();
  }

  @Override
  public <T extends SpatialComparable> void sort(List<T> objs, int start, int end, double[] minmax, int[] dims) {
    final int dim = (dims != null) ? dims.length : (minmax.length >> 1);
    List<HilbertRef<T>> tmp = new ArrayList<>(end - start);
    int[] buf = new int[dim];
    for (int i = start; i < end; i++) {
      T v = objs.get(i);
      // Convert into integers
      for (int d = 0; d < dim; d++) {
        final int ed = (dims != null) ? dims[d] : d, ed2 = ed << 1;
        double val = (v.getMin(ed) + v.getMax(ed)) * .5;
        val = Integer.MAX_VALUE * ((val - minmax[ed2]) / (minmax[ed2 + 1] - minmax[ed2]));
        buf[d] = (int) val;
      }
      tmp.add(new HilbertRef<>(v, coordinatesToHilbert(buf, Integer.SIZE - 1, 1)));
    }
    // Sort and copy back
    Collections.sort(tmp);
    for (int i = start; i < end; i++) {
      objs.set(i, tmp.get(i - start).vec);
    }
  }

  /**
   * Object used in spatial sorting, combining the spatial object and the object
   * ID.
   * 
   * @author Erich Schubert
   */
  private static class HilbertRef<T extends SpatialComparable> implements Comparable<HilbertRef<T>> {
    /**
     * The referenced object.
     */
    protected T vec;

    /**
     * Hilbert representation.
     */
    protected long[] bits;

    /**
     * Constructor.
     * 
     * @param vec Vector
     * @param bits Bit representation
     */
    protected HilbertRef(T vec, long[] bits) {
      super();
      this.vec = vec;
      this.bits = bits;
    }

    @Override
    public int compareTo(HilbertRef<T> o) {
      return BitsUtil.compare(this.bits, o.bits);
    }
  }

  /**
   * Interleave one long per dimension (using the "bitsperdim" highest bits) to
   * a hilbert address.
   * 
   * @param coords Original coordinates
   * @param bitsperdim Number of bits to use.
   * @param offset offset
   * @return Hilbert address
   */
  public static long[] coordinatesToHilbert(long[] coords, int bitsperdim, int offset) {
    final int numdim = coords.length;
    final int numbits = numdim * bitsperdim;
    final long[] output = BitsUtil.zero(numbits);

    int rotation = 0;
    long[] refl = BitsUtil.zero(numdim);
    for (int i = 0; i < bitsperdim; i++) {
      final long[] hist = interleaveBits(coords, i + offset);
      // System.err.println(BitsUtil.toString(hist,
      // numdim)+" rot:"+rotation+" refl: "+BitsUtil.toString(refl, numdim));
      final long[] bits = BitsUtil.copy(hist);
      BitsUtil.xorI(bits, refl);
      BitsUtil.cycleRightI(bits, rotation, numdim);
      final int nextrot = (rotation + BitsUtil.numberOfTrailingZerosSigned(bits) + 2) % numdim;
      BitsUtil.invgrayI(bits);
      BitsUtil.orI(output, bits, numbits - (i + 1) * numdim);
      // System.err.println(BitsUtil.toString(output,
      // numbits)+" bits: "+BitsUtil.toString(bits, numdim));
      refl = hist;
      BitsUtil.flipI(refl, rotation);
      if (!BitsUtil.get(bits, 0)) {
        BitsUtil.flipI(refl, (nextrot - 1 + numdim) % numdim);
      }
      rotation = nextrot;
    }

    return output;
  }

  /**
   * Interleave one int per dimension (using the "bitsperdim" highest bits) to a
   * hilbert address.
   * 
   * @param coords Original coordinates
   * @param bitsperdim Number of bits to use.
   * @param offset offset
   * @return Hilbert address
   */
  public static long[] coordinatesToHilbert(int[] coords, int bitsperdim, int offset) {
    final int numdim = coords.length;
    final int numbits = numdim * bitsperdim;
    final long[] output = BitsUtil.zero(numbits);

    int rotation = 0;
    long[] refl = BitsUtil.zero(numdim);
    for (int i = 0; i < bitsperdim; i++) {
      final long[] hist = interleaveBits(coords, i + offset);
      // System.err.println(BitsUtil.toString(hist,
      // numdim)+" rot:"+rotation+" refl: "+BitsUtil.toString(refl, numdim));
      final long[] bits = BitsUtil.copy(hist);
      BitsUtil.xorI(bits, refl);
      BitsUtil.cycleRightI(bits, rotation, numdim);
      final int nextrot = (rotation + BitsUtil.numberOfTrailingZerosSigned(bits) + 2) % numdim;
      BitsUtil.invgrayI(bits);
      BitsUtil.orI(output, bits, numbits - (i + 1) * numdim);
      // System.err.println(BitsUtil.toString(output,
      // numbits)+" bits: "+BitsUtil.toString(bits, numdim));
      refl = hist;
      BitsUtil.flipI(refl, rotation);
      if (!BitsUtil.get(bits, 0)) {
        BitsUtil.flipI(refl, (nextrot - 1 + numdim) % numdim);
      }
      rotation = nextrot;
    }

    return output;
  }

  /**
   * Interleave one short per dimension (using the "bitsperdim" highest bits) to
   * a hilbert address.
   * 
   * @param coords Original coordinates
   * @param bitsperdim Number of bits to use.
   * @param offset offset
   * @return Hilbert address
   */
  public static long[] coordinatesToHilbert(short[] coords, int bitsperdim, int offset) {
    final int numdim = coords.length;
    final int numbits = numdim * bitsperdim;
    final long[] output = BitsUtil.zero(numbits);

    int rotation = 0;
    long[] refl = BitsUtil.zero(numdim);
    for (int i = 0; i < bitsperdim; i++) {
      final long[] hist = interleaveBits(coords, i + offset);
      // System.err.println(BitsUtil.toString(hist,
      // numdim)+" rot:"+rotation+" refl: "+BitsUtil.toString(refl, numdim));
      final long[] bits = BitsUtil.copy(hist);
      BitsUtil.xorI(bits, refl);
      BitsUtil.cycleRightI(bits, rotation, numdim);
      final int nextrot = (rotation + BitsUtil.numberOfTrailingZerosSigned(bits) + 2) % numdim;
      BitsUtil.invgrayI(bits);
      BitsUtil.orI(output, bits, numbits - (i + 1) * numdim);
      // System.err.println(BitsUtil.toString(output,
      // numbits)+" bits: "+BitsUtil.toString(bits, numdim));
      refl = hist;
      BitsUtil.flipI(refl, rotation);
      if (!BitsUtil.get(bits, 0)) {
        BitsUtil.flipI(refl, (nextrot - 1 + numdim) % numdim);
      }
      rotation = nextrot;
    }

    return output;
  }

  /**
   * Interleave one byte per dimension (using the "bitsperdim" highest bits) to
   * a hilbert address.
   * 
   * @param coords Original coordinates
   * @param bitsperdim Number of bits to use.
   * @param offset offset
   * @return Hilbert address
   */
  public static long[] coordinatesToHilbert(byte[] coords, int bitsperdim, int offset) {
    final int numdim = coords.length;
    final int numbits = numdim * bitsperdim;
    final long[] output = BitsUtil.zero(numbits);

    int rotation = 0;
    long[] refl = BitsUtil.zero(numdim);
    for (int i = 0; i < bitsperdim; i++) {
      final long[] hist = interleaveBits(coords, i + offset);
      // System.err.println(BitsUtil.toString(hist,
      // numdim)+" rot:"+rotation+" refl: "+BitsUtil.toString(refl, numdim));
      final long[] bits = BitsUtil.copy(hist);
      BitsUtil.xorI(bits, refl);
      BitsUtil.cycleRightI(bits, rotation, numdim);
      final int nextrot = (rotation + BitsUtil.numberOfTrailingZerosSigned(bits) + 2) % numdim;
      BitsUtil.invgrayI(bits);
      BitsUtil.orI(output, bits, numbits - (i + 1) * numdim);
      // System.err.println(BitsUtil.toString(output,
      // numbits)+" bits: "+BitsUtil.toString(bits, numdim));
      refl = hist;
      BitsUtil.flipI(refl, rotation);
      if (!BitsUtil.get(bits, 0)) {
        BitsUtil.flipI(refl, (nextrot - 1 + numdim) % numdim);
      }
      rotation = nextrot;
    }

    return output;
  }

  /**
   * Select the "iter" highest bit from each dimension.
   * 
   * @param coords Input coordinates
   * @param iter Bit position (from highest position)
   * @return One bit per dimension
   */
  public static long[] interleaveBits(long[] coords, int iter) {
    final int numdim = coords.length;
    final long[] bitset = BitsUtil.zero(numdim);
    // convert longValues into zValues
    final long mask = 1L << 63 - iter;
    for (int dim = 0; dim < numdim; dim++) {
      if ((coords[dim] & mask) != 0) {
        BitsUtil.setI(bitset, dim);
      }
    }
    return bitset;
  }

  /**
   * Select the "iter" highest bit from each dimension.
   * 
   * @param coords Input coordinates
   * @param iter Bit position (from highest position)
   * @return One bit per dimension
   */
  public static long[] interleaveBits(int[] coords, int iter) {
    final int numdim = coords.length;
    final long[] bitset = BitsUtil.zero(numdim);
    // convert longValues into zValues
    final long mask = 1L << 31 - iter;
    for (int dim = 0; dim < numdim; dim++) {
      if ((coords[dim] & mask) != 0) {
        BitsUtil.setI(bitset, dim);
      }
    }
    return bitset;
  }

  /**
   * Select the "iter" highest bit from each dimension.
   * 
   * @param coords Input coordinates
   * @param iter Bit position (from highest position)
   * @return One bit per dimension
   */
  public static long[] interleaveBits(short[] coords, int iter) {
    final int numdim = coords.length;
    final long[] bitset = BitsUtil.zero(numdim);
    // convert longValues into zValues
    final long mask = 1L << 15 - iter;
    for (int dim = 0; dim < numdim; dim++) {
      if ((coords[dim] & mask) != 0) {
        BitsUtil.setI(bitset, dim);
      }
    }
    return bitset;
  }

  /**
   * Select the "iter" highest bit from each dimension.
   * 
   * @param coords Input coordinates
   * @param iter Bit position (from highest position)
   * @return One bit per dimension
   */
  public static long[] interleaveBits(byte[] coords, int iter) {
    final int numdim = coords.length;
    final long[] bitset = BitsUtil.zero(numdim);
    // convert longValues into zValues
    final long mask = 1L << 7 - iter;
    for (int dim = 0; dim < numdim; dim++) {
      if ((coords[dim] & mask) != 0) {
        BitsUtil.setI(bitset, dim);
      }
    }
    return bitset;
  }
}
