package de.lmu.ifi.dbs.elki.distance.distancefunction.strings;

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

/**
 * Unit test for Levenshtein distance.
 * 
 * @author Erich Schubert
 */
public class LevenshteinDistanceFunctionTest implements JUnit4Test {
  /**
   * Some test strings, from the Wikipedia article.
   */
  final String[][] TESTS = { //
  { "kitten", "sitting" }, //
  { "Saturday", "Sunday" }, //
  { "tier", "tor" }, //
  { "abcz", "zabc" }, //
  { "zabc", "abcz" }, //
  };

  /**
   * The associated scores.
   */
  final int[] SCORES = { //
  3, // kitten <-> sitting
  3, // Saturday <-> Sunday
  2, // Tier <-> Tor
  2, // abcz <-> zabc
  2, // zabc <-> abcz
  };

  @Test
  public void testStringLevenshtein() {
    LevenshteinDistanceFunction f = LevenshteinDistanceFunction.STATIC_SENSITIVE;
    for (int i = 0; i < TESTS.length; i++) {
      assertEquals("Distance does not agree: " + TESTS[i][0] + " <-> " + TESTS[i][1], SCORES[i], (int) f.distance(TESTS[i][0], TESTS[i][1]));
    }
  }
}
