package de.lmu.ifi.dbs.elki.index.tree.metrical.mtreevariants.mktrees.mkapp;

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

import de.lmu.ifi.dbs.elki.index.tree.metrical.mtreevariants.MTreeEntry;

/**
 * Defines the requirements for an entry in an MkCop-Tree node. Additionally to
 * an entry in an M-Tree conservative approximation of the knn distances is
 * provided.
 * 
 * @author Elke Achtert
 * 
 * @apiviz.composedOf PolynomialApproximation
 */
interface MkAppEntry extends MTreeEntry {
  /**
   * Returns the approximated value at the specified k.
   * 
   * @param k the parameter k of the knn distance
   * @return the approximated value at the specified k
   */
  public double approximatedValueAt(int k);

  /**
   * Returns the polynomial approximation.
   * 
   * @return the polynomial approximation
   */
  public PolynomialApproximation getKnnDistanceApproximation();

  /**
   * Sets the polynomial approximation.
   * 
   * @param approximation the polynomial approximation to be set
   */
  public void setKnnDistanceApproximation(PolynomialApproximation approximation);
}
