package de.lmu.ifi.dbs.elki.math.linearalgebra.fitting;

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


/**
 * Interface for a function used in Levenberg-Marquard-Fitting
 * 
 * @author Erich Schubert
 */
public interface FittingFunction {
  /**
   * Compute value at position x as well as gradients for the parameters
   * 
   * @param x Current coordinate
   * @param params Function parameters parameters
   * @return Array consisting of y value and parameter gradients
   */
  FittingFunctionResult eval(double x, double[] params);
}
