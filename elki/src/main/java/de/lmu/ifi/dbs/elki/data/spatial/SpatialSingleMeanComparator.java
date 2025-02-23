package de.lmu.ifi.dbs.elki.data.spatial;

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
import java.util.Comparator;

/**
 * Comparator for sorting spatial objects by the mean value in a single
 * dimension.
 * 
 * @author Erich Schubert
 * 
 * @apiviz.uses SpatialComparable
 */
public class SpatialSingleMeanComparator implements Comparator<SpatialComparable> {
  /**
   * Current dimension.
   */
  int dim;

  /**
   * Constructor.
   * 
   * @param dim Dimension to sort by.
   */
  public SpatialSingleMeanComparator(int dim) {
    super();
    this.dim = dim;
  }

  /**
   * Set the dimension to sort by.
   * 
   * @param dim Dimension
   */
  public void setDimension(int dim) {
    this.dim = dim;
  }

  @Override
  public int compare(SpatialComparable o1, SpatialComparable o2) {
    final double v1 = o1.getMin(dim) + o1.getMax(dim);
    final double v2 = o2.getMin(dim) + o2.getMax(dim);
    return Double.compare(v1, v2);
  }
}
