package de.lmu.ifi.dbs.elki.math.linearalgebra.randomprojections;

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

import de.lmu.ifi.dbs.elki.math.random.RandomFactory;
import de.lmu.ifi.dbs.elki.utilities.documentation.Reference;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.CommonConstraints;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.DoubleParameter;

/**
 * Random projections as suggested by Dimitris Achlioptas.
 *
 * Reference:
 * <p>
 * D. Achlioptas:<br />
 * Database-friendly random projections: Johnson-Lindenstrauss with binary coins
 * <br />
 * Proc. 20th ACM SIGMOD-SIGACT-SIGART symposium on Principles of database
 * systems
 * </p>
 *
 * TODO: faster implementation exploiting sparsity.
 *
 * @author Erich Schubert
 */
@Reference(title = "Database-friendly random projections: Johnson-Lindenstrauss with binary coins", //
authors = "D. Achlioptas", //
booktitle = "Proc. 20th ACM SIGMOD-SIGACT-SIGART symposium on Principles of database systems", //
url = "http://dx.doi.org/10.1145/375551.375608")
public class AchlioptasRandomProjectionFamily extends AbstractRandomProjectionFamily {
  /**
   * Projection sparsity.
   */
  private double sparsity;

  /**
   * Constructor.
   *
   * @param sparsity Projection sparsity
   * @param random Random number generator.
   */
  public AchlioptasRandomProjectionFamily(double sparsity, RandomFactory random) {
    super(random);
    this.sparsity = sparsity;
  }

  @Override
  public Projection generateProjection(int idim, int odim) {
    final double pPos = .5 / sparsity;
    final double pNeg = pPos + pPos; // Threshold
    double baseValuePart = Math.sqrt(this.sparsity);

    double[][] matrix = new double[odim][idim];
    for(int i = 0; i < odim; ++i) {
      double[] row = matrix[i];
      for(int j = 0; j < idim; ++j) {
        final double r = random.nextDouble();
        row[j] = (r < pPos) ? baseValuePart : (r < pNeg) ? -baseValuePart : 0;
      }
    }
    return new MatrixProjection(matrix);
  }

  /**
   * Parameterization class.
   *
   * @author Erich Schubert
   *
   * @apiviz.exclude
   */
  public static class Parameterizer extends AbstractRandomProjectionFamily.Parameterizer {
    /**
     * Parameter for the projection sparsity.
     */
    public static final OptionID SPARSITY_ID = new OptionID("achlioptas.sparsity", "Frequency of zeros in the projection matrix.");

    /**
     * Projection sparsity
     */
    private double sparsity = 3.;

    @Override
    protected void makeOptions(Parameterization config) {
      super.makeOptions(config);
      DoubleParameter sparsP = new DoubleParameter(SPARSITY_ID);
      sparsP.setDefaultValue(3.);
      sparsP.addConstraint(CommonConstraints.GREATER_EQUAL_ONE_DOUBLE);
      if(config.grab(sparsP)) {
        sparsity = sparsP.doubleValue();
      }
    }

    @Override
    protected AchlioptasRandomProjectionFamily makeInstance() {
      return new AchlioptasRandomProjectionFamily(sparsity, random);
    }
  }
}
