package de.lmu.ifi.dbs.elki.utilities.datastructures.heap;

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

import de.lmu.ifi.dbs.elki.utilities.datastructures.iterator.Iter;

/**
 * Basic in-memory heap for K values.
 * 
 * @author Erich Schubert
 * 
 * @apiviz.has UnsortedIter
 * @param <K> Key type
 */
public interface ObjectHeap<K> {
  /**
   * Add a key-value pair to the heap
   * 
   * @param key Key
   */
  void add(K key);

  /**
   * Add a key-value pair to the heap, except if the new element is larger than
   * the top, and we are at design size (overflow)
   * 
   * @param key Key
   * @param max Maximum size of heap
   */
  void add(K key, int max);

  /**
   * Combined operation that removes the top element, and inserts a new element
   * instead.
   * 
   * @param e New element to insert
   * @return Previous top element of the heap
   */
  K replaceTopElement(K e);

  /**
   * Get the current top key
   * 
   * @return Top key
   */
  K peek();

  /**
   * Remove the first element
   * 
   * @return Top element
   */
  K poll();

  /**
   * Delete all elements from the heap.
   */
  void clear();

  /**
   * Query the size
   * 
   * @return Size
   */
  public int size();
  
  /**
   * Is the heap empty?
   * 
   * @return {@code true} when the size is 0.
   */
  public boolean isEmpty();

  /**
   * Get an unsorted iterator to inspect the heap.
   * 
   * @return Iterator
   */
  UnsortedIter<K> unsortedIter();

  /**
   * Unsorted iterator - in heap order. Does not poll the heap.
   * 
   * <pre>
   * {@code
   * for (ObjectHeap.UnsortedIter<K> iter = heap.unsortedIter(); iter.valid(); iter.next()) {
   *   doSomething(iter.get());
   * }
   * }
   * </pre>
   * 
   * @author Erich Schubert
   * @param <K> Key type
   */
  public static interface UnsortedIter<K> extends Iter {
    /**
     * Get the iterators current object.
     * 
     * @return Current object
     */
    K get();
  }
}
