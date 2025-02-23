package de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.flat;

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

import de.lmu.ifi.dbs.elki.index.tree.spatial.SpatialEntry;
import de.lmu.ifi.dbs.elki.index.tree.spatial.rstarvariants.AbstractRStarTreeNode;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;

/**
 * Represents a node in a flat R*-Tree.
 * 
 * @author Elke Achtert
 */
public class FlatRStarTreeNode extends AbstractRStarTreeNode<FlatRStarTreeNode, SpatialEntry> {
  /**
   * Serial version
   */
  private static final long serialVersionUID = 1;

  /**
   * Empty constructor for Externalizable interface.
   */
  public FlatRStarTreeNode() {
    // empty constructor
  }

  /**
   * Deletes the entry at the specified index and shifts all entries after the
   * index to left.
   * 
   * @param index the index at which the entry is to be deleted
   */
  @Override
  public boolean deleteEntry(int index) {
    if(this.getPageID() == 0 && index == 0 && getNumEntries() == 1) {
      return false;
    }
    return super.deleteEntry(index);
  }

  /**
   * Creates a new FlatRStarTreeNode with the specified parameters.
   * 
   * @param capacity the capacity (maximum number of entries plus 1 for
   *        overflow) of this node
   * @param isLeaf indicates whether this node is a leaf node
   */
  public FlatRStarTreeNode(int capacity, boolean isLeaf) {
    super(capacity, isLeaf, SpatialEntry.class);
  }

  /**
   * Increases the length of the entries array to entries.length + 1.
   */
  public final void increaseEntries() {
    SpatialEntry[] tmp = entries;
    entries = ClassGenericsUtil.newArrayOfNull(tmp.length + 1, SpatialEntry.class);
    System.arraycopy(tmp, 0, entries, 0, tmp.length);
  }
}