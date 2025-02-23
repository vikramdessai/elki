package de.lmu.ifi.dbs.elki.utilities.datastructures.arrays;

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

import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

import de.lmu.ifi.dbs.elki.JUnit4Test;

/**
 * Test the integer array (index array) quick sort.
 * 
 * @author Erich Schubert
 */
public class TestIntegerArrayQuickSort implements JUnit4Test {
  /**
   * Array size to use.
   */
  final int SIZE = 10000;

  @Test
  public void testRandomDoubles() {
    for(int i = 1; i < 10; i++) {
      testQuickSort(i);
    }
    testQuickSort(SIZE);
    testQuickSort(SIZE + 1);
  }

  private void testQuickSort(int size) {
    final double[] data = new double[size];
    int[] idx = new int[size];

    // Make a random generator, but remember the seed for debugging.
    Random r = new Random();
    long seed = r.nextLong();
    r = new Random(seed);

    // Produce data
    for(int i = 0; i < size; i++) {
      data[i] = r.nextDouble();
      idx[i] = i;
    }

    // Run QuickSort and validate monotonicity.
    IntegerArrayQuickSort.sort(idx, new IntegerComparator() {
      @Override
      public int compare(int x, int y) {
        return Double.compare(data[x], data[y]);
      }
    });
    double prev = data[idx[0]];
    for(int i = 1; i < size; i++) {
      double val = data[idx[i]];
      assertTrue("Resulting array is not sorted. Seed=" + seed, prev <= val);
      prev = val;
    }
  }

  @Test(timeout = 500)
  public void testTies() {
    int size = 1000000;
    int[] idx = new int[size];

    // Initialize indexes
    for(int i = 0; i < size; i++) {
      idx[i] = 0;
    }

    // Run QuickSort and validate monotonicity.
    IntegerArrayQuickSort.sort(idx, new IntegerComparator() {
      @Override
      public int compare(int x, int y) {
        return (x < y) ? -1 : (x == y) ? 0 : +1;
      }
    });

    int prev = idx[0];
    for(int i = 1; i < size; i++) {
      int val = idx[i];
      assertTrue("Resulting array is not sorted.", prev <= val);
      prev = val;
    }
  }

  @Test(timeout = 500)
  public void testSorted() {
    int size = 1000000;
    int[] idx = new int[size];

    // Initialize indexes
    for(int i = 0; i < size; i++) {
      idx[i] = size - i;
    }

    // Run QuickSort and validate monotonicity.
    IntegerArrayQuickSort.sort(idx, new IntegerComparator() {
      @Override
      public int compare(int x, int y) {
        return (x < y) ? -1 : (x == y) ? 0 : +1;
      }
    });

    int prev = idx[0];
    for(int i = 1; i < size; i++) {
      int val = idx[i];
      assertTrue("Resulting array is not sorted.", prev <= val);
      prev = val;
    }
  }
}