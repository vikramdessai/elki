package de.lmu.ifi.dbs.elki.math.linearalgebra.pca;

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

import java.util.ArrayList;
import java.util.List;

import de.lmu.ifi.dbs.elki.math.linearalgebra.EigenPair;
import de.lmu.ifi.dbs.elki.math.linearalgebra.SortedEigenPairs;
import de.lmu.ifi.dbs.elki.utilities.documentation.Description;
import de.lmu.ifi.dbs.elki.utilities.documentation.Title;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.AbstractParameterizer;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.CommonConstraints;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.DoubleParameter;

/**
 * The ProgressiveEigenPairFilter sorts the eigenpairs in descending order of
 * their eigenvalues and marks the first eigenpairs, whose sum of eigenvalues is
 * higher than the given percentage of the sum of all eigenvalues as strong
 * eigenpairs. In contrast to the PercentageEigenPairFilter, it will use a
 * percentage which changes linearly with the subspace dimensionality. This
 * makes the parameter more consistent for different dimensionalities and often
 * gives better results when clusters of different dimensionality exist, since
 * different percentage alpha levels might be appropriate for different
 * dimensionalities.
 *
 * Example calculations of alpha levels:
 *
 * In a 3D space, a progressive alpha value of 0.5 equals:
 *
 * - 1D subspace: 50 % + 1/3 of remainder = 0.667
 *
 * - 2D subspace: 50 % + 2/3 of remainder = 0.833
 *
 * In a 4D space, a progressive alpha value of 0.5 equals:
 *
 * - 1D subspace: 50% + 1/4 of remainder = 0.625
 *
 * - 2D subspace: 50% + 2/4 of remainder = 0.750
 *
 * - 3D subspace: 50% + 3/4 of remainder = 0.875
 *
 * Reasoning why this improves over PercentageEigenPairFilter:
 *
 * In a 100 dimensional space, a single Eigenvector representing over 85% of the
 * total variance is highly significant, whereas the strongest 85 Eigenvectors
 * together will by definition always represent at least 85% of the variance.
 * PercentageEigenPairFilter can thus not be used with these parameters and
 * detect both dimensionalities correctly.
 *
 * The second parameter introduced here, walpha, serves a different function: It
 * prevents the eigenpair filter to use a statistically weak Eigenvalue just to
 * reach the intended level, e.g. 84% + 1% >= 85% when 1% is statistically very
 * weak.
 *
 * @author Erich Schubert
 *
 */
@Title("Progressive Eigenpair Filter")
@Description("Sorts the eigenpairs in decending order of their eigenvalues and returns the first eigenpairs, whose sum of eigenvalues explains more than the a certain percentage of the unexpected variance, where the percentage increases with subspace dimensionality.")
public class ProgressiveEigenPairFilter implements EigenPairFilter {
  /**
   * The default value for alpha.
   */
  public static final double DEFAULT_PALPHA = 0.5;

  /**
   * The default value for alpha.
   */
  public static final double DEFAULT_WALPHA = 0.95;

  /**
   * The threshold for strong eigenvectors: the strong eigenvectors explain a
   * portion of at least alpha of the total variance.
   */
  private double palpha;

  /**
   * The noise tolerance level for weak eigenvectors
   */
  private double walpha;

  /**
   * Constructor.
   *
   * @param palpha palpha
   * @param walpha walpha
   */
  public ProgressiveEigenPairFilter(double palpha, double walpha) {
    super();
    this.palpha = palpha;
    this.walpha = walpha;
  }

  /**
   * Filter eigenpairs.
   */
  @Override
  public FilteredEigenPairs filter(SortedEigenPairs eigenPairs) {
    // init strong and weak eigenpairs
    List<EigenPair> strongEigenPairs = new ArrayList<>();
    List<EigenPair> weakEigenPairs = new ArrayList<>();

    // determine sum of eigenvalues
    double totalSum = 0;
    for(int i = 0; i < eigenPairs.size(); i++) {
      EigenPair eigenPair = eigenPairs.getEigenPair(i);
      totalSum += eigenPair.getEigenvalue();
    }
    double expectedVariance = totalSum / eigenPairs.size() * walpha;

    // determine strong and weak eigenpairs
    double currSum = 0;
    boolean found = false;
    int i;
    for(i = 0; i < eigenPairs.size(); i++) {
      EigenPair eigenPair = eigenPairs.getEigenPair(i);
      // weak Eigenvector?
      if(eigenPair.getEigenvalue() < expectedVariance) {
        break;
      }
      currSum += eigenPair.getEigenvalue();
      // calculate progressive alpha level
      double alpha = 1.0 - (1.0 - palpha) * (1.0 - (i + 1) / eigenPairs.size());
      if(currSum / totalSum >= alpha || i == eigenPairs.size() - 1) {
        found = true;
        strongEigenPairs.add(eigenPair);
        break;
      }
    }
    // if we didn't hit our alpha level, we consider all vectors to be weak!
    if(!found) {
      assert (weakEigenPairs.size() == 0);
      weakEigenPairs = strongEigenPairs;
      strongEigenPairs = new ArrayList<>();
    }
    for(; i < eigenPairs.size(); i++) {
      EigenPair eigenPair = eigenPairs.getEigenPair(i);
      weakEigenPairs.add(eigenPair);
    }

    // the code using this method doesn't expect an empty strong set,
    // if we didn't find any strong ones, we make all vectors strong
    if(strongEigenPairs.size() == 0) {
      return new FilteredEigenPairs(new ArrayList<EigenPair>(), weakEigenPairs);
    }
    return new FilteredEigenPairs(weakEigenPairs, strongEigenPairs);
  }

  /**
   * Parameterization class.
   *
   * @author Erich Schubert
   *
   * @apiviz.exclude
   */
  public static class Parameterizer extends AbstractParameterizer {
    /**
     * Parameter progressive alpha.
     */
    public static final OptionID EIGENPAIR_FILTER_PALPHA = new OptionID("pca.filter.progressivealpha", "The share (0.0 to 1.0) of variance that needs to be explained by the 'strong' eigenvectors." + "The filter class will choose the number of strong eigenvectors by this share.");

    /**
     * The threshold for strong eigenvectors: the strong eigenvectors explain a
     * portion of at least alpha of the total variance.
     */
    private double palpha;

    /**
     * The noise tolerance level for weak eigenvectors
     */
    private double walpha;

    @Override
    protected void makeOptions(Parameterization config) {
      super.makeOptions(config);
      DoubleParameter palphaP = new DoubleParameter(EIGENPAIR_FILTER_PALPHA, DEFAULT_PALPHA);
      palphaP.addConstraint(CommonConstraints.GREATER_THAN_ZERO_DOUBLE);
      palphaP.addConstraint(CommonConstraints.LESS_THAN_ONE_DOUBLE);
      if(config.grab(palphaP)) {
        palpha = palphaP.getValue();
      }

      DoubleParameter walphaP = new DoubleParameter(WeakEigenPairFilter.Parameterizer.EIGENPAIR_FILTER_WALPHA, DEFAULT_WALPHA);
      walphaP.addConstraint(CommonConstraints.GREATER_EQUAL_ZERO_DOUBLE);
      if(config.grab(walphaP)) {
        walpha = walphaP.getValue();
      }
    }

    @Override
    protected ProgressiveEigenPairFilter makeInstance() {
      return new ProgressiveEigenPairFilter(palpha, walpha);
    }
  }
}
