package de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.rstar;

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

import de.lmu.ifi.dbs.elki.data.NumberVector;
import de.lmu.ifi.dbs.elki.database.relation.Relation;
import de.lmu.ifi.dbs.elki.index.tree.spatial.SpatialEntry;
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.AbstractRStarTreeFactory;
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.AbstractRTreeSettings;
import de.lmu.ifi.dbs.elki.persistent.PageFile;
import de.lmu.ifi.dbs.elki.persistent.PageFileFactory;
import de.lmu.ifi.dbs.elki.utilities.Alias;

/**
 * Factory for regular R*-Trees.
 * 
 * @author Erich Schubert
 * 
 * @apiviz.landmark factory
 * @apiviz.uses RStarTreeIndex oneway - - «create»
 * 
 * @param <O> Object type
 */
@Alias({"rstar", "r*"})
public class RStarTreeFactory<O extends NumberVector> extends AbstractRStarTreeFactory<O, RStarTreeNode, SpatialEntry, RStarTreeIndex<O>, AbstractRTreeSettings> {
  /**
   * Constructor.
   * 
   * @param pageFileFactory Data storage
   * @param settings Tree settings
   */
  public RStarTreeFactory(PageFileFactory<?> pageFileFactory, AbstractRTreeSettings settings) {
    super(pageFileFactory, settings);
  }

  @Override
  public RStarTreeIndex<O> instantiate(Relation<O> relation) {
    PageFile<RStarTreeNode> pagefile = makePageFile(getNodeClass());
    return new RStarTreeIndex<>(relation, pagefile, settings);
  }

  protected Class<RStarTreeNode> getNodeClass() {
    return RStarTreeNode.class;
  }

  /**
   * Parameterization class.
   * 
   * @author Erich Schubert
   * 
   * @apiviz.exclude
   * 
   * @param <O> Object type
   */
  public static class Parameterizer<O extends NumberVector> extends AbstractRStarTreeFactory.Parameterizer<O, AbstractRTreeSettings> {
    @Override
    protected RStarTreeFactory<O> makeInstance() {
      return new RStarTreeFactory<>(pageFileFactory, settings);
    }

    @Override
    protected AbstractRTreeSettings createSettings() {
      return new AbstractRTreeSettings();
    }
  }
}