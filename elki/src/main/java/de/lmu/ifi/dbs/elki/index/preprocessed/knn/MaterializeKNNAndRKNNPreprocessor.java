package de.lmu.ifi.dbs.elki.index.preprocessed.knn;

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
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import de.lmu.ifi.dbs.elki.database.datastore.DataStoreFactory;
import de.lmu.ifi.dbs.elki.database.datastore.DataStoreUtil;
import de.lmu.ifi.dbs.elki.database.datastore.WritableDataStore;
import de.lmu.ifi.dbs.elki.database.ids.ArrayDBIDs;
import de.lmu.ifi.dbs.elki.database.ids.ArrayModifiableDBIDs;
import de.lmu.ifi.dbs.elki.database.ids.DBID;
import de.lmu.ifi.dbs.elki.database.ids.DBIDIter;
import de.lmu.ifi.dbs.elki.database.ids.DBIDRef;
import de.lmu.ifi.dbs.elki.database.ids.DBIDUtil;
import de.lmu.ifi.dbs.elki.database.ids.DBIDs;
import de.lmu.ifi.dbs.elki.database.ids.DoubleDBIDList;
import de.lmu.ifi.dbs.elki.database.ids.DoubleDBIDListIter;
import de.lmu.ifi.dbs.elki.database.ids.DoubleDBIDPair;
import de.lmu.ifi.dbs.elki.database.ids.HashSetModifiableDBIDs;
import de.lmu.ifi.dbs.elki.database.ids.KNNHeap;
import de.lmu.ifi.dbs.elki.database.ids.KNNList;
import de.lmu.ifi.dbs.elki.database.ids.ModifiableDoubleDBIDList;
import de.lmu.ifi.dbs.elki.database.ids.SetDBIDs;
import de.lmu.ifi.dbs.elki.database.query.distance.DistanceQuery;
import de.lmu.ifi.dbs.elki.database.query.rknn.PreprocessorRKNNQuery;
import de.lmu.ifi.dbs.elki.database.query.rknn.RKNNQuery;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.distance.distancefunction.DistanceFunction;
import de.lmu.ifi.dbs.elki.index.RKNNIndex;
import de.lmu.ifi.dbs.elki.logging.Logging;
import de.lmu.ifi.dbs.elki.logging.progress.FiniteProgress;
import de.lmu.ifi.dbs.elki.logging.progress.StepProgress;
import de.lmu.ifi.dbs.elki.utilities.documentation.Description;
import de.lmu.ifi.dbs.elki.utilities.documentation.Title;

/**
 * A preprocessor for annotation of the k nearest neighbors and the reverse k
 * nearest neighbors (and their distances) to each database object.
 *
 * BUG: This class currently does <em>not</em> seem able to correctly keep track
 * of the nearest neighbors!
 *
 * @author Elke Achtert
 *
 * @param <O> the type of database objects the preprocessor can be applied to
 * @param the type of distance the used distance function will return
 */
// TODO: rewrite the double optimization. Maybe use a specialized subclass?
@Title("Materialize kNN and RkNN Neighborhood preprocessor")
@Description("Materializes the k nearest neighbors and the reverse k nearest neighbors of objects of a database.")
public class MaterializeKNNAndRKNNPreprocessor<O> extends MaterializeKNNPreprocessor<O>implements RKNNIndex<O> {
  /**
   * Logger to use.
   */
  private static final Logging LOG = Logging.getLogger(MaterializeKNNAndRKNNPreprocessor.class);

  /**
   * Additional data storage for RkNN.
   */
  private WritableDataStore<TreeSet<DoubleDBIDPair>> materialized_RkNN;

  /**
   * Constructor.
   *
   * @param relation Relation to process
   * @param distanceFunction the distance function to use
   * @param k query k
   */
  public MaterializeKNNAndRKNNPreprocessor(Relation<O> relation, DistanceFunction<? super O> distanceFunction, int k) {
    super(relation, distanceFunction, k);
  }

  @Override
  protected void preprocess() {
    createStorage();
    materialized_RkNN = DataStoreUtil.makeStorage(relation.getDBIDs(), DataStoreFactory.HINT_HOT, TreeSet.class);
    FiniteProgress progress = getLogger().isVerbose() ? new FiniteProgress("Materializing k nearest neighbors and reverse k nearest neighbors (k=" + k + ")", relation.size(), getLogger()) : null;
    materializeKNNAndRKNNs(DBIDUtil.ensureArray(relation.getDBIDs()), progress);
  }

  /**
   * Materializes the kNNs and RkNNs of the specified object IDs.
   *
   * @param ids the IDs of the objects
   */
  private void materializeKNNAndRKNNs(ArrayDBIDs ids, FiniteProgress progress) {
    // add an empty list to each rknn
    for(DBIDIter iter = ids.iter(); iter.valid(); iter.advance()) {
      if(materialized_RkNN.get(iter) == null) {
        materialized_RkNN.put(iter, new TreeSet<DoubleDBIDPair>());
      }
    }

    // knn query
    List<? extends KNNList> kNNList = knnQuery.getKNNForBulkDBIDs(ids, k);
    int i = 0;
    for(DBIDIter id = ids.iter(); id.valid(); id.advance(), i++) {
      KNNList kNNs = kNNList.get(i);
      storage.put(id, kNNs);
      for(DoubleDBIDListIter iter = kNNs.iter(); iter.valid(); iter.advance()) {
        TreeSet<DoubleDBIDPair> rknns = materialized_RkNN.get(iter);
        rknns.add(makePair(iter, id));
      }
      getLogger().incrementProcessed(progress);
    }

    getLogger().ensureCompleted(progress);
  }

  private DoubleDBIDPair makePair(DoubleDBIDListIter iter, DBIDIter id) {
    return DBIDUtil.newPair(iter.getPair().doubleValue(), id);
  }

  @Override
  protected void objectsInserted(DBIDs ids) {
    StepProgress stepprog = getLogger().isVerbose() ? new StepProgress(3) : null;

    ArrayDBIDs aids = DBIDUtil.ensureArray(ids);
    // materialize the new kNNs and RkNNs
    getLogger().beginStep(stepprog, 1, "New insertions ocurred, materialize their new kNNs and RkNNs.");
    materializeKNNAndRKNNs(aids, null);

    // update the old kNNs and RkNNs
    getLogger().beginStep(stepprog, 2, "New insertions ocurred, update the affected kNNs and RkNNs.");
    ArrayDBIDs rkNN_ids = updateKNNsAndRkNNs(ids);

    // inform listener
    getLogger().beginStep(stepprog, 3, "New insertions ocurred, inform listeners.");
    fireKNNsInserted(ids, rkNN_ids);

    getLogger().ensureCompleted(stepprog);
  }

  /**
   * Updates the kNNs and RkNNs after insertion of the specified ids.
   *
   * @param ids the ids of newly inserted objects causing a change of
   *        materialized kNNs and RkNNs
   * @return the RkNNs of the specified ids, i.e. the kNNs which have been
   *         updated
   */
  private ArrayDBIDs updateKNNsAndRkNNs(DBIDs ids) {
    ArrayModifiableDBIDs rkNN_ids = DBIDUtil.newArray();
    DBIDs oldids = DBIDUtil.difference(relation.getDBIDs(), ids);
    for(DBIDIter id = oldids.iter(); id.valid(); id.advance()) {
      KNNList oldkNNs = storage.get(id);
      double knnDist = oldkNNs.getKNNDistance();
      // look for new kNNs
      KNNHeap heap = null;
      for(DBIDIter newid = ids.iter(); newid.valid(); newid.advance()) {
        double dist = distanceQuery.distance(id, newid);
        if(dist <= knnDist) {
          // New id changes the kNNs of oldid.
          if(heap == null) {
            heap = DBIDUtil.newHeap(oldkNNs);
          }
          heap.insert(dist, newid);
        }
      }
      // kNNs for oldid have changed:
      if(heap != null) {
        KNNList newkNNs = heap.toKNNList();
        storage.put(id, newkNNs);

        // get the difference
        int i = 0;
        int j = 0;
        ModifiableDoubleDBIDList added = DBIDUtil.newDistanceDBIDList();
        ModifiableDoubleDBIDList removed = DBIDUtil.newDistanceDBIDList();
        // TODO: use iterators.
        while(i < oldkNNs.size() && j < newkNNs.size()) {
          DoubleDBIDPair drp1 = oldkNNs.get(i);
          DoubleDBIDPair drp2 = newkNNs.get(j);
          // NOTE: we assume that on ties they are ordered the same way!
          if(!DBIDUtil.equal(drp1, drp2)) {
            added.add(drp2);
            j++;
          }
          else {
            i++;
            j++;
          }
        }
        if(i != j) {
          for(; i < oldkNNs.size(); i++) {
            removed.add(oldkNNs.get(i));
          }
          for(; j < newkNNs.size(); i++) {
            added.add(newkNNs.get(i));
          }
        }
        // add new RkNN
        for(DoubleDBIDListIter newnn = added.iter(); newnn.valid(); newnn.advance()) {
          TreeSet<DoubleDBIDPair> rknns = materialized_RkNN.get(newnn);
          rknns.add(makePair(newnn, id));
        }
        // remove old RkNN
        for(DoubleDBIDListIter oldnn = removed.iter(); oldnn.valid(); oldnn.advance()) {
          TreeSet<DoubleDBIDPair> rknns = materialized_RkNN.get(oldnn);
          rknns.remove(makePair(oldnn, id));
        }

        rkNN_ids.add(id);
      }
    }
    return rkNN_ids;
  }

  @Override
  protected void objectsRemoved(DBIDs ids) {
    StepProgress stepprog = getLogger().isVerbose() ? new StepProgress(3) : null;

    // For debugging: valid DBIDs still in the database.
    final DBIDs valid = DBIDUtil.ensureSet(distanceQuery.getRelation().getDBIDs());

    ArrayDBIDs aids = DBIDUtil.ensureArray(ids);
    // delete the materialized (old) kNNs and RkNNs
    getLogger().beginStep(stepprog, 1, "New deletions ocurred, remove their materialized kNNs and RkNNs.");
    // Temporary storage of removed lists
    List<KNNList> kNNs = new ArrayList<>(ids.size());
    List<TreeSet<DoubleDBIDPair>> rkNNs = new ArrayList<>(ids.size());
    for(DBIDIter iter = aids.iter(); iter.valid(); iter.advance()) {
      kNNs.add(storage.get(iter));
      for(DBIDIter it = storage.get(iter).iter(); it.valid(); it.advance()) {
        if(!valid.contains(it) && !ids.contains(it)) {
          LOG.warning("False kNN: " + it);
        }
      }
      storage.delete(iter);
      rkNNs.add(materialized_RkNN.get(iter));
      for(DoubleDBIDPair it : materialized_RkNN.get(iter)) {
        if(!valid.contains(it) && !ids.contains(it)) {
          LOG.warning("False RkNN: " + it);
        }
      }
      materialized_RkNN.delete(iter);
    }
    // Keep only those IDs not also removed
    ArrayDBIDs kNN_ids = affectedkNN(kNNs, aids);
    ArrayDBIDs rkNN_ids = affectedRkNN(rkNNs, aids);

    // update the affected kNNs and RkNNs
    getLogger().beginStep(stepprog, 2, "New deletions ocurred, update the affected kNNs and RkNNs.");
    // Recompute the kNN for affected objects (in rkNN lists)
    {
      List<? extends KNNList> kNNList = knnQuery.getKNNForBulkDBIDs(rkNN_ids, k);
      int i = 0;
      for(DBIDIter reknn = rkNN_ids.iter(); reknn.valid(); reknn.advance(), i++) {
        if(kNNList.get(i) == null && !valid.contains(reknn)) {
          LOG.warning("BUG in online kNN/RkNN maintainance: " + DBIDUtil.toString(reknn) + " no longer in database.");
          continue;
        }
        assert(kNNList.get(i) != null);
        storage.put(reknn, kNNList.get(i));
        for(DoubleDBIDListIter it = kNNList.get(i).iter(); it.valid(); it.advance()) {
          materialized_RkNN.get(it).add(makePair(it, reknn));
        }
      }
    }
    // remove objects from RkNNs of objects (in kNN lists)
    {
      SetDBIDs idsSet = DBIDUtil.ensureSet(ids);
      for(DBIDIter nn = kNN_ids.iter(); nn.valid(); nn.advance()) {
        TreeSet<DoubleDBIDPair> rkNN = materialized_RkNN.get(nn);
        for(Iterator<DoubleDBIDPair> it = rkNN.iterator(); it.hasNext();) {
          if(idsSet.contains(it.next())) {
            it.remove();
          }
        }
      }
    }

    // inform listener
    getLogger().beginStep(stepprog, 3, "New deletions ocurred, inform listeners.");
    fireKNNsRemoved(ids, rkNN_ids);

    getLogger().ensureCompleted(stepprog);
  }

  /**
   * Extracts and removes the DBIDs in the given collections.
   *
   * @param extract a list of lists of DistanceResultPair to extract
   * @param remove the ids to remove
   * @return the DBIDs in the given collection
   */
  protected ArrayDBIDs affectedkNN(List<? extends KNNList> extract, DBIDs remove) {
    HashSetModifiableDBIDs ids = DBIDUtil.newHashSet();
    for(KNNList drps : extract) {
      for(DBIDIter iter = drps.iter(); iter.valid(); iter.advance()) {
        ids.add(iter);
      }
    }
    ids.removeDBIDs(remove);
    // Convert back to array
    return DBIDUtil.newArray(ids);
  }

  /**
   * Extracts and removes the DBIDs in the given collections.
   *
   * @param extract a list of lists of DistanceResultPair to extract
   * @param remove the ids to remove
   * @return the DBIDs in the given collection
   */
  protected ArrayDBIDs affectedRkNN(List<? extends Collection<DoubleDBIDPair>> extract, DBIDs remove) {
    HashSetModifiableDBIDs ids = DBIDUtil.newHashSet();
    for(Collection<DoubleDBIDPair> drps : extract) {
      for(DoubleDBIDPair drp : drps) {
        ids.add(drp);
      }
    }
    ids.removeDBIDs(remove);
    // Convert back to array
    return DBIDUtil.newArray(ids);
  }

  /**
   * Returns the materialized kNNs of the specified id.
   *
   * @param id the query id
   * @return the kNNs
   */
  public KNNList getKNN(DBID id) {
    return storage.get(id);
  }

  /**
   * Returns the materialized RkNNs of the specified id.
   *
   * @param id the query id
   * @return the RkNNs
   */
  public DoubleDBIDList getRKNN(DBIDRef id) {
    TreeSet<DoubleDBIDPair> rKNN = materialized_RkNN.get(id);
    if(rKNN == null) {
      return null;
    }
    ModifiableDoubleDBIDList ret = DBIDUtil.newDistanceDBIDList(rKNN.size());
    for(DoubleDBIDPair pair : rKNN) {
      ret.add(pair);
    }
    ret.sort();
    return ret;
  }

  @Override
  public RKNNQuery<O> getRKNNQuery(DistanceQuery<O> distanceQuery, Object... hints) {
    if(!this.distanceFunction.equals(distanceQuery.getDistanceFunction())) {
      return null;
    }
    // k max supported?
    for(Object hint : hints) {
      if(hint instanceof Integer) {
        if(((Integer) hint) > k) {
          return null;
        }
        break;
      }
    }
    return new PreprocessorRKNNQuery<>(relation, this);
  }

  @Override
  public String getLongName() {
    return "kNN and RkNN Preprocessor";
  }

  @Override
  public String getShortName() {
    return "knn and rknn preprocessor";
  }

  @Override
  protected Logging getLogger() {
    return LOG;
  }

  /**
   * The parameterizable factory.
   *
   * @author Elke Achtert
   *
   * @param <O> The object type
   * @param The distance type
   */
  public static class Factory<O> extends MaterializeKNNPreprocessor.Factory<O> {
    /**
     * Constructor.
     *
     * @param k k
     * @param distanceFunction distance function
     */
    public Factory(int k, DistanceFunction<? super O> distanceFunction) {
      super(k, distanceFunction);
    }

    @Override
    public MaterializeKNNAndRKNNPreprocessor<O> instantiate(Relation<O> relation) {
      MaterializeKNNAndRKNNPreprocessor<O> instance = new MaterializeKNNAndRKNNPreprocessor<>(relation, distanceFunction, k);
      return instance;
    }

    /**
     * Parameterization class.
     *
     * @author Erich Schubert
     *
     * @apiviz.exclude
     */
    public static class Parameterizer<O> extends MaterializeKNNPreprocessor.Factory.Parameterizer<O> {
      @Override
      protected Factory<O> makeInstance() {
        return new Factory<>(k, distanceFunction);
      }
    }
  }
}