package de.lmu.ifi.dbs.elki.math.statistics.intrinsicdimensionality;

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
import de.lmu.ifi.dbs.elki.utilities.datastructures.arraylike.NumberArrayAdapter;
import de.lmu.ifi.dbs.elki.utilities.documentation.Reference;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.AbstractParameterizer;

/**
 * Methods of moments estimator, using the first moment (i.e. average).
 *
 * This could be generalized to higher order moments, but the variance increases
 * with the order, and we need this to work well with small sample sizes.
 *
 * Reference:
 * <p>
 * L. Amsaleg and O. Chelly and T. Furon and S. Girard and M. E. Houle and K.
 * Kawarabayashi and M. Nett<br />
 * Estimating Local Intrinsic Dimensionality<br />
 * Proc. SIGKDD International Conference on Knowledge Discovery and Data Mining
 * 2015
 * </p>
 *
 * @author Erich Schubert
 */
@Reference(authors = "L. Amsaleg and O. Chelly and T. Furon and S. Girard and M. E. Houle and K. Kawarabayashi and M. Nett", //
title = "Estimating Local Intrinsic Dimensionality", //
booktitle = "Proc. SIGKDD International Conference on Knowledge Discovery and Data Mining 2015", //
url = "http://dx.doi.org/10.1145/2783258.2783405")
public class MOMEstimator extends AbstractIntrinsicDimensionalityEstimator {
  /**
   * Static instance.
   */
  public static final MOMEstimator STATIC = new MOMEstimator();

  @Override
  public <A> double estimate(A data, NumberArrayAdapter<?, A> adapter, final int len) {
    if(len < 2) {
      throw new ArithmeticException("ID estimates require at least 2 non-zero distances");
    }
    double v1 = 0.;
    final int num = len - 1;
    for(int i = 0; i < num; i++) {
      v1 += adapter.getDouble(data, i);
    }
    v1 /= num * adapter.getDouble(data, num);
    return v1 / (1 - v1);
  }

  /**
   * Parameterization class.
   *
   * @author Erich Schubert
   *
   * @apiviz.exclude
   */
  public static class Parameterizer extends AbstractParameterizer {
    @Override
    protected MOMEstimator makeInstance() {
      return STATIC;
    }
  }
}
