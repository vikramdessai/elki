package de.lmu.ifi.dbs.elki.math.statistics.distribution.estimator;

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
import de.lmu.ifi.dbs.elki.math.statistics.distribution.NormalDistribution;
import de.lmu.ifi.dbs.elki.utilities.documentation.Reference;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.AbstractParameterizer;

/**
 * Estimator using Medians. More robust to outliers, and just slightly more
 * expensive (needs to copy the data for partial sorting to find the median).
 * 
 * References:
 * <p>
 * F. R. Hampel<br />
 * The Influence Curve and Its Role in Robust Estimation<br />
 * in: Journal of the American Statistical Association, June 1974, Vol. 69, No.
 * 346
 * </p>
 * <p>
 * P. J. Rousseeuw, C. Croux<br />
 * Alternatives to the Median Absolute Deviation<br />
 * in: Journal of the American Statistical Association, December 1993, Vol. 88,
 * No. 424, Theory and Methods
 * </p>
 * 
 * @author Erich Schubert
 * 
 * @apiviz.has NormalDistribution - - estimates
 */
@Reference(authors = "F. R. Hampel", title = "The Influence Curve and Its Role in Robust Estimation", booktitle = "Journal of the American Statistical Association, June 1974, Vol. 69, No. 346", url = "http://www.jstor.org/stable/10.2307/2285666")
public class NormalMADEstimator extends AbstractMADEstimator<NormalDistribution> {
  /**
   * Static estimator, more robust to outliers by using the median.
   */
  public static NormalMADEstimator STATIC = new NormalMADEstimator();

  /**
   * Constructor. Private: use static instance!
   */
  private NormalMADEstimator() {
    super();
  }

  @Override
  public NormalDistribution estimateFromMedianMAD(double median, double mad) {
    return new NormalDistribution(median, Math.max(NormalDistribution.ONEBYPHIINV075 * mad, Double.MIN_NORMAL));
  }

  @Override
  public Class<? super NormalDistribution> getDistributionClass() {
    return NormalDistribution.class;
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
    protected NormalMADEstimator makeInstance() {
      return STATIC;
    }
  }
}
