package de.lmu.ifi.dbs.elki.distance.distancefunction.external;

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

import java.io.File;
import java.io.IOException;

import de.lmu.ifi.dbs.elki.distance.distancefunction.AbstractDBIDRangeDistanceFunction;
import de.lmu.ifi.dbs.elki.persistent.OnDiskUpperTriangleMatrix;
import de.lmu.ifi.dbs.elki.utilities.documentation.Description;
import de.lmu.ifi.dbs.elki.utilities.documentation.Title;
import de.lmu.ifi.dbs.elki.utilities.io.ByteArrayUtil;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.AbstractParameterizer;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.OptionID;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.WrongParameterValueException;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameterization.Parameterization;
import de.lmu.ifi.dbs.elki.utilities.optionhandling.parameters.FileParameter;

/**
 * Distance function that is based on double distances given by a distance
 * matrix of an external binary matrix file.
 * 
 * @author Erich Schubert
 */
@Title("File based double distance for database objects.")
@Description("Loads double distance values from an external matrix.")
public class DiskCacheBasedDoubleDistanceFunction extends AbstractDBIDRangeDistanceFunction {
  // TODO: constructor with file.

  /**
   * Magic to identify double cache matrices
   */
  public static final int DOUBLE_CACHE_MAGIC = 50902811;

  /**
   * The distance matrix
   */
  private OnDiskUpperTriangleMatrix cache;

  /**
   * Constructor.
   * 
   * @param cache Distance matrix
   */
  public DiskCacheBasedDoubleDistanceFunction(OnDiskUpperTriangleMatrix cache) {
    super();
    this.cache = cache;
  }

  @Override
  public double distance(int i1, int i2) {
    // the smaller id is the first key
    if(i1 > i2) {
      return distance(i2, i1);
    }

    try {
      return cache.getRecordBuffer(i1, i2).getDouble();
    }
    catch(IOException e) {
      throw new RuntimeException("Read error when loading distance " + i1 + "," + i2 + " from cache file.", e);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if(obj == null) {
      return false;
    }
    if(getClass() != obj.getClass()) {
      return false;
    }
    DiskCacheBasedDoubleDistanceFunction other = (DiskCacheBasedDoubleDistanceFunction) obj;
    return this.cache.equals(other.cache);
  }

  /**
   * Parameterization class.
   * 
   * @author Erich Schubert
   * 
   * @apiviz.exclude
   */
  public static class Parameterizer extends AbstractParameterizer {
    // TODO: constructor with file.

    /**
     * Parameter that specifies the name of the distance matrix file.
     * <p>
     * Key: {@code -distance.matrix}
     * </p>
     */
    public static final OptionID MATRIX_ID = new OptionID("distance.matrix", //
    "The name of the file containing the distance matrix.");

    protected OnDiskUpperTriangleMatrix cache = null;

    @Override
    protected void makeOptions(Parameterization config) {
      super.makeOptions(config);
      final FileParameter param = new FileParameter(MATRIX_ID, FileParameter.FileType.INPUT_FILE);
      if(config.grab(param)) {
        File matrixfile = param.getValue();
        try {
          cache = new OnDiskUpperTriangleMatrix(matrixfile, DOUBLE_CACHE_MAGIC, 0, ByteArrayUtil.SIZE_DOUBLE, false);
        }
        catch(IOException e) {
          config.reportError(new WrongParameterValueException(param, matrixfile.toString(), e));
        }
      }
    }

    @Override
    protected DiskCacheBasedDoubleDistanceFunction makeInstance() {
      return new DiskCacheBasedDoubleDistanceFunction(cache);
    }
  }
}