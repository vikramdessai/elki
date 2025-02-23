package de.lmu.ifi.dbs.elki.algorithm.statistics;

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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import de.lmu.ifi.dbs.elki.algorithm.AbstractDistanceBasedAlgorithm;
import de.lmu.ifi.dbs.elki.algorithm.clustering.trivial.ByLabelOrAllInOneClustering;
import de.lmu.ifi.dbs.elki.data.Cluster;
import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.data.model.Model;
import de.lmu.ifi.dbs.elki.data.type.CombinedTypeInformation;
import de.lmu.ifi.dbs.elki.data.type.TypeInformation;
import de.lmu.ifi.dbs.elki.data.type.TypeUtil;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDUtil;
import de.lmu.ifi.dbs.elki.database.ids.DoubleDBIDPair;
import de.lmu.ifi.dbs.elki.database.ids.KNNList;
import de.lmu.ifi.dbs.elki.database.query.distance.DistanceQuery;
import de.lmu.ifi.dbs.elki.database.query.knn.KNNQuery;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.distance.distancefunction.DistanceFunction;
import de.lmu.ifi.dbs.elki.evaluation.scores.ROCEvaluation;
import de.lmu.ifi.dbs.elki.logging.Logging;
import de.lmu.ifi.dbs.elki.logging.progress.FiniteProgress;
import de.lmu.ifi.dbs.elki.math.MathUtil;
import de.lmu.ifi.dbs.elki.math.MeanVariance;
import de.lmu.ifi.dbs.elki.math.linearalgebra.CovarianceMatrix;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Matrix;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;
import de.lmu.ifi.dbs.elki.result.CollectionResult;
import de.lmu.ifi.dbs.elki.result.HistogramResult;
import de.lmu.ifi.dbs.elki.utilities.datastructures.histogram.MeanVarianceStaticHistogram;
import de.lmu.ifi.dbs.elki.utilities.datastructures.histogram.ObjHistogram;
import de.lmu.ifi.dbs.elki.utilities.documentation.Description;
import de.lmu.ifi.dbs.elki.utilities.documentation.Title;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.constraints.CommonConstraints;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.IntParameter;

/**
 * Evaluate a distance function with respect to kNN queries. For each point, the
 * neighbors are sorted by distance, then the ROC AUC is computed. A score of 1
 * means that the distance function provides a perfect ordering of relevant
 * neighbors first, then irrelevant neighbors. A value of 0.5 can be obtained by
 * random sorting. A value of 0 means the distance function is inverted, i.e. a
 * similarity.
 *
 * In contrast to {@link RankingQualityHistogram}, this method uses a binning
 * based on the centrality of objects. This allows analyzing whether or not a
 * particular distance degrades for the outer parts of a cluster.
 *
 * TODO: Allow fixed binning range, configurable
 *
 * TODO: Add sampling
 *
 * @author Erich Schubert
 * @param <V> Vector type
 */
@Title("Evaluate Ranking Quality")
@Description("Evaluates the effectiveness of a distance function via the obtained rankings.")
public class EvaluateRankingQuality<V extends NumberVector> extends AbstractDistanceBasedAlgorithm<V, CollectionResult<DoubleVector>> {
  /**
   * The logger for this class.
   */
  private static final Logging LOG = Logging.getLogger(EvaluateRankingQuality.class);

  /**
   * Number of bins to use.
   */
  int numbins = 20;

  /**
   * Constructor.
   *
   * @param distanceFunction Distance function
   * @param numbins Number of bins
   */
  public EvaluateRankingQuality(DistanceFunction<? super V> distanceFunction, int numbins) {
    super(distanceFunction);
    this.numbins = numbins;
  }

  @Override
  public HistogramResult<DoubleVector> run(Database database) {
    final Relation<V> relation = database.getRelation(getInputTypeRestriction()[0]);
    final DistanceQuery<V> distQuery = database.getDistanceQuery(relation, getDistanceFunction());
    final KNNQuery<V> knnQuery = database.getKNNQuery(distQuery, relation.size());

    if(LOG.isVerbose()) {
      LOG.verbose("Preprocessing clusters...");
    }
    // Cluster by labels
    Collection<Cluster<Model>> split = (new ByLabelOrAllInOneClustering()).run(database).getAllClusters();

    // Compute cluster averages and covariance matrix
    HashMap<Cluster<?>, Vector> averages = new HashMap<>(split.size());
    HashMap<Cluster<?>, Matrix> covmats = new HashMap<>(split.size());
    for(Cluster<?> clus : split) {
      CovarianceMatrix covmat = CovarianceMatrix.make(relation, clus.getIDs());
      averages.put(clus, covmat.getMeanVector());
      covmats.put(clus, covmat.destroyToNaiveMatrix());
    }

    MeanVarianceStaticHistogram hist = new MeanVarianceStaticHistogram(numbins, 0.0, 1.0);

    if(LOG.isVerbose()) {
      LOG.verbose("Processing points...");
    }
    FiniteProgress rocloop = LOG.isVerbose() ? new FiniteProgress("Computing ROC AUC values", relation.size(), LOG) : null;

    // sort neighbors
    for(Cluster<?> clus : split) {
      ArrayList<DoubleDBIDPair> cmem = new ArrayList<>(clus.size());
      Vector av = averages.get(clus);
      Matrix covm = covmats.get(clus);

      for(DBIDIter iter = clus.getIDs().iter(); iter.valid(); iter.advance()) {
        double d = MathUtil.mahalanobisDistance(covm, relation.get(iter).getColumnVector().minusEquals(av));
        cmem.add(DBIDUtil.newPair(d, iter));
      }
      Collections.sort(cmem);

      for(int ind = 0; ind < cmem.size(); ind++) {
        KNNList knn = knnQuery.getKNNForDBID(cmem.get(ind), relation.size());
        double result = new ROCEvaluation().evaluate(clus, knn);

        hist.put(((double) ind) / clus.size(), result);

        LOG.incrementProcessed(rocloop);
      }
    }
    LOG.ensureCompleted(rocloop);
    // Collections.sort(results);

    // Transform Histogram into a Double Vector array.
    Collection<DoubleVector> res = new ArrayList<>(relation.size());
    for(ObjHistogram.Iter<MeanVariance> iter = hist.iter(); iter.valid(); iter.advance()) {
      DoubleVector row = new DoubleVector(new double[] { iter.getCenter(), iter.getValue().getCount(), iter.getValue().getMean(), iter.getValue().getSampleVariance() });
      res.add(row);
    }
    return new HistogramResult<>("Ranking Quality Histogram", "ranking-histogram", res);
  }

  @Override
  public TypeInformation[] getInputTypeRestriction() {
    return TypeUtil.array(new CombinedTypeInformation(getDistanceFunction().getInputTypeRestriction(), TypeUtil.NUMBER_VECTOR_FIELD));
  }

  @Override
  protected Logging getLogger() {
    return LOG;
  }

  /**
   * Parameterization class.
   *
   * @author Erich Schubert
   *
   * @apiviz.exclude
   *
   * @param <V> Vector type
   */
  public static class Parameterizer<V extends NumberVector> extends AbstractDistanceBasedAlgorithm.Parameterizer<V> {
    /**
     * Option to configure the number of bins to use.
     */
    public static final OptionID HISTOGRAM_BINS_ID = new OptionID("rankqual.bins", "Number of bins to use in the histogram");
    /**
     * Number of bins to use.
     */
    protected int numbins = 20;

    @Override
    protected void makeOptions(Parameterization config) {
      super.makeOptions(config);
      final IntParameter param = new IntParameter(HISTOGRAM_BINS_ID, 20) //
      .addConstraint(CommonConstraints.GREATER_THAN_ONE_INT);
      if(config.grab(param)) {
        numbins = param.getValue();
      }
    }

    @Override
    protected EvaluateRankingQuality<V> makeInstance() {
      return new EvaluateRankingQuality<>(distanceFunction, numbins);
    }
  }
}
