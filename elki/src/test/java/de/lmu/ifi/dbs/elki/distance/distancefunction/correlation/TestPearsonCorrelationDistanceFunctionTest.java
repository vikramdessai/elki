package de.lmu.ifi.dbs.elki.distance.distancefunction.correlation;

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

import org.junit.Test;

import de.lmu.ifi.dbs.elki.JUnit4Test;
import de.lmu.ifi.dbs.elki.data.DoubleVector;

/**
 * Unit test for Pearson correlation distance.
 * 
 * @author Erich Schubert
 */
public class TestPearsonCorrelationDistanceFunctionTest implements JUnit4Test {
  /**
   * Some test series.
   */
  static final DoubleVector[] TESTS = { //
  new DoubleVector(new double[] { 0., 1., 2., 3., 4., 5. }), //
  new DoubleVector(new double[] { 0., .1, .2, .3, .4, .5 }), //
  new DoubleVector(new double[] { -0., -1., -2., -3., -4., -5. }), //
  new DoubleVector(new double[] { -5., -4., -3., -2., -1., 0. }), //
  new DoubleVector(new double[] { 0., 0., 0., 0., 0., 0. }), //
  new DoubleVector(new double[] { 0., 2., 4., 5., 3., 1. }), //
  };

  /** Dissimilarity for even-up,odd-down case */
  static final double C1 = 0.7428571428571429;

  /**
   * The associated scores.
   */
  static final double[][] SCORES = { //
  { 0., 0., 2., 0., 1., C1 },//
  { 0., 0., 2., 0., 1., C1 },//
  { 2., 2., 0., 2., 1., 2. - C1 },//
  { 0., 0., 2., 0., 1., C1 },//
  { 1., 1., 1., 1., 0., 1. },//
  { C1, C1, 2. - C1, C1, 1., 0. },//
  };

  @Test
  public void testPearson() {
    PearsonCorrelationDistanceFunction f = PearsonCorrelationDistanceFunction.STATIC;
    for(int i = 0; i < TESTS.length; i++) {
      for(int j = 0; j < TESTS.length; j++) {
        final double dist = f.distance(TESTS[i], TESTS[j]);
        assertEquals("Distance does not agree: " + TESTS[i] + " <-> " + TESTS[j], SCORES[i][j], dist, 1e-15);
      }
    }
  }
}
