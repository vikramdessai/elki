package de.lmu.ifi.dbs.elki.math.geometry;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.lmu.ifi.dbs.elki.JUnit4Test;

/**
 * Test class for Prim's minmum spanning tree algorithm.
 * 
 * @author Erich Schubert
 */
public class TestPrimsMinimumSpanningTree implements JUnit4Test {
  @Test
  public void testSimple() {
    // A simple test.
    final double inf = Double.POSITIVE_INFINITY;
    double[][] mat = new double[][] {//
    { 0.0, 7.0, inf, 5.0, inf, inf, inf }, //
    { 7.0, 0.0, 8.0, 9.0, 7.0, inf, inf }, //
    { inf, 8.0, 0.0, inf, 5.0, inf, inf }, //
    { 5.0, 9.0, inf, 0.0, 15., 6.0, inf }, //
    { inf, 7.0, 5.0, 15., 0.0, 8.0, 9.0 }, //
    { inf, inf, inf, 6.0, 8.0, 0.0, 11. }, //
    { inf, inf, inf, inf, 9.0, 11., 0.0 }, //
    };
    int[] ret = PrimsMinimumSpanningTree.processDense(mat);
    // "correct" edges (ignore order and direction!)
    int[] correct = new int[] { 0, 1, 0, 3, 1, 4, 2, 4, 4, 6, 3, 5 };
    assertEquals("Graph size does not match expected size.", correct.length, ret.length);

    // Flags so we find edges only once.
    int[] flags = new int[correct.length];
    for (int i = 0; i < ret.length; i += 2) {
      boolean found = false;
      for (int j = 0; j < correct.length; j += 2) {
        if (flags[j] == 1) {
          continue;
        }
        if ((correct[j] == ret[i] && correct[j + 1] == ret[i + 1]) || (correct[j] == ret[i + 1] && correct[j + 1] == ret[i])) {
          found = true;
          flags[j] = 1;
          break;
        }
      }
      assertTrue("Edge not found: " + (char) ('A' + ret[i]) + " -> " + (char) ('A' + ret[i + 1]), found);
    }
  }
  // We could also check that every even flag is set. But as we checked the
  // length and found all edges, all must have been used...
}
