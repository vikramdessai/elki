package de.lmu.ifi.dbs.elki.math.statistics;

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

import de.lmu.ifi.dbs.elki.math.linearalgebra.Matrix;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;
import de.lmu.ifi.dbs.elki.utilities.FormatUtil;

/**
 * Multiple linear regression attempts to model the relationship between two or
 * more explanatory variables and a response variable by fitting a linear
 * equation to observed data. Every value of the independent variable x is
 * associated with a value of the dependent variable y.
 * <p/>
 * The population regression line for p explanatory variables x1, x2, ... , xp
 * is defined to be y = b0 + b1*x1 + b2*x2 + ... + bp*xp + e.
 * 
 * @author Elke Achtert
 */
public class MultipleLinearRegression {
  /**
   * The (n x 1) - vector holding the y-values (y1, ..., yn)^T.
   */
  private final Vector y;

  /**
   * Holds the mean value of the y-values.
   */
  private final double y_mean;

  /**
   * The (n x p+1)-matrix holding the x-values, where the i-th row has the form
   * (1 x1i ... x1p).
   */
  private final Matrix x;

  /**
   * The (p+1 x 1) - vector holding the estimated b-values (b0, b1, ..., bp)^T.
   */
  private final Vector b;

  /**
   * The (n x 1) - vector holding the estimated residuals (e1, ..., en)^T.
   */
  private final Vector e;

  /**
   * The error variance.
   */
  private final double variance;

  /**
   * Holds the matrix (x'x)^-1.
   */
  private final Matrix xx_inverse;

  /**
   * The sum of square residuals
   */
  private final double ssr;

  /**
   * The sum of square totals
   */
  private final double sst;

  /**
   * Constructor.
   * 
   * @param y the (n x 1) - vector holding the response values (y1, ..., yn)^T.
   * @param x the (n x p+1)-matrix holding the explanatory values, where the
   *        i-th row has the form (1 x1i ... x1p).
   */
  public MultipleLinearRegression(Vector y, Matrix x) {
    if(y.getDimensionality() <= x.getColumnDimensionality()) {
      throw new IllegalArgumentException("Number of observed data has to be greater than " + "number of regressors: " + y.getDimensionality() + " > " + x.getColumnDimensionality());
    }

    this.y = y;
    this.x = x;

    double sum = 0;
    for(int i = 0; i < y.getDimensionality(); i++) {
      sum += y.get(i);
    }
    y_mean = sum / y.getDimensionality();

    // estimate b, e
    xx_inverse = x.transposeTimes(x).inverse();
    b = xx_inverse.timesTranspose(x).times(y);
    // b = new Vector(x.solve(y).getColumnPackedCopy());
    e = y.minus(x.times(b));

    // sum of square residuals: ssr
    sum = 0;
    for(int i = 0; i < e.getDimensionality(); i++) {
      sum += e.get(i) * e.get(i);
    }
    ssr = sum;

    // sum of square totals: sst
    sum = 0;
    for(int i = 0; i < y.getDimensionality(); i++) {
      final double diff = y.get(i) - y_mean;
      sum += diff * diff;
    }
    sst = sum;

    // variance
    variance = ssr / (y.getDimensionality() - x.getColumnDimensionality() - 1);
  }

  /**
   * Returns a string representation of the object.
   * 
   * @return a string representation of the object.
   */
  @Override
  public String toString() {
    StringBuilder msg = new StringBuilder();
    msg.append("x = ").append(FormatUtil.format(x, 9, 4));
    msg.append("\ny = ").append(FormatUtil.format(y, 9, 4));
    msg.append("\nb = ").append(FormatUtil.format(b, 9, 4));
    msg.append("\ne = ").append(FormatUtil.format(e, 9, 4));
    msg.append("error variance = ").append(FormatUtil.NF4.format(variance));
    return msg.toString();
  }

  /**
   * Returns the sum of squares total.
   * 
   * @return the sum of squares total
   */
  public double getSumOfSquaresTotal() {
    return sst;
  }

  /**
   * Returns the sum of square residuals.
   * 
   * @return the sum of square residuals
   */
  public double getSumOfSquareResiduals() {
    return ssr;
  }

  /**
   * Returns the estimated coefficients
   * 
   * @return the estimated coefficients
   */
  public Vector getEstimatedCoefficients() {
    return b;
  }

  /**
   * Returns the estimated residuals
   * 
   * @return the estimated residuals
   */
  public Vector getEstimatedResiduals() {
    return e;
  }

  /**
   * Returns the coefficient of determination
   * 
   * @return the coefficient of determination
   */
  public double coefficientOfDetermination() {
    return 1.0 - (ssr / sst);
  }

  /**
   * Perform an estimation of y on the specified matrix.
   * 
   * @param x the matrix for which y is estimated
   * @return the estimation of y
   */
  public double estimateY(Matrix x) {
    return x.times(b).get(0);
  }

  /**
   * Returns the error variance.
   * 
   * @return the error variance
   */
  public double getVariance() {
    return variance;
  }
}