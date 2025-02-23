package de.lmu.ifi.dbs.elki.index.idistance;

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

import org.junit.Test;

import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.FarthestPointsInitialMeans;
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase;
import de.lmu.ifi.dbs.elki.distance.distancefunction.minkowski.EuclideanDistanceFunction;
import de.lmu.ifi.dbs.elki.index.AbstractTestIndexStructures;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.ListParameterization;

/**
 * Unit test for the iDistance index.
 * 
 * @author Erich Schubert
 */
public class TestIDistance extends AbstractTestIndexStructures {
  /**
   * Test {@link InMemoryIDistanceIndex}.
   */
  @Test
  public void testIDistance() {
    ListParameterization spatparams = new ListParameterization();
    spatparams.addParameter(StaticArrayDatabase.Parameterizer.INDEX_ID, InMemoryIDistanceIndex.Factory.class);
    spatparams.addParameter(InMemoryIDistanceIndex.Factory.Parameterizer.K_ID, 4);
    spatparams.addParameter(InMemoryIDistanceIndex.Factory.Parameterizer.DISTANCE_ID, EuclideanDistanceFunction.class);
    spatparams.addParameter(InMemoryIDistanceIndex.Factory.Parameterizer.REFERENCE_ID, FarthestPointsInitialMeans.class);
    testExactEuclidean(spatparams, InMemoryIDistanceIndex.IDistanceKNNQuery.class, InMemoryIDistanceIndex.IDistanceRangeQuery.class);
  }
}
