package de.lmu.ifi.dbs.elki.database.ids.generic;

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
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDRef;
import de.lmu.ifi.dbs.elki.database.ids.DBIDUtil;
import de.lmu.ifi.dbs.elki.database.ids.DoubleDBIDListIter;
import de.lmu.ifi.dbs.elki.database.ids.DoubleDBIDPair;
import de.lmu.ifi.dbs.elki.database.ids.KNNList;

/**
 * Sublist of an existing result to contain only the first k elements.
 * 
 * @author Erich Schubert
 */
public class KNNSubList implements KNNList {
  /**
   * Parameter k.
   */
  private final int k;

  /**
   * Actual size, including ties.
   */
  private final int size;

  /**
   * Wrapped inner result.
   */
  private final KNNList inner;

  /**
   * Constructor.
   * 
   * @param inner Inner instance
   * @param k k value
   */
  public KNNSubList(KNNList inner, int k) {
    this.inner = inner;
    this.k = k;
    // Compute list size
    if(k < inner.getK()) {
      DoubleDBIDPair dist = inner.get(k);
      int i = k;
      while(i + 1 < inner.size()) {
        if(dist.doubleValue() < inner.get(i + 1).doubleValue()) {
          break;
        }
        i++;
      }
      size = i;
    }
    else {
      size = inner.size();
    }
  }

  @Override
  public int getK() {
    return k;
  }

  @Override
  public DoubleDBIDPair get(int index) {
    assert (index < size) : "Access beyond design size of list.";
    return inner.get(index);
  }

  @Override
  public double getKNNDistance() {
    return inner.get(k).doubleValue();
  }

  @Override
  public DoubleDBIDListIter iter() {
    return new Itr();
  }

  @Override
  public boolean contains(DBIDRef o) {
    for(DBIDIter iter = iter(); iter.valid(); iter.advance()) {
      if(DBIDUtil.equal(iter, o)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isEmpty() {
    return size == 0;
  }

  @Override
  public int size() {
    return size;
  }

  /**
   * Iterator for the sublist.
   * 
   * @author Erich Schubert
   * 
   * @apiviz.exclude
   */
  private class Itr implements DoubleDBIDListIter {
    /**
     * Current position.
     */
    private int pos = 0;

    @Override
    public boolean valid() {
      return pos < size && pos >= 0;
    }

    @Override
    public Itr advance() {
      pos++;
      return this;
    }

    @Override
    public double doubleValue() {
      return inner.get(pos).doubleValue();
    }

    @Override
    public DoubleDBIDPair getPair() {
      return inner.get(pos);
    }

    @Override
    public int internalGetIndex() {
      return inner.get(pos).internalGetIndex();
    }

    @Override
    public int getOffset() {
      return pos;
    }

    @Override
    public Itr advance(int count) {
      pos += count;
      return this;
    }

    @Override
    public Itr retract() {
      --pos;
      return this;
    }

    @Override
    public Itr seek(int off) {
      pos = off;
      return this;
    }
  }
}
