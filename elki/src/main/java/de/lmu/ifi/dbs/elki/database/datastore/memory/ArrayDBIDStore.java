package de.lmu.ifi.dbs.elki.database.datastore.memory;

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

import de.lmu.ifi.dbs.elki.database.datastore.DataStoreIDMap;
import de.lmu.ifi.dbs.elki.database.datastore.WritableDBIDDataStore;
import de.lmu.ifi.dbs.elki.database.ids.ArrayModifiableDBIDs;
import de.lmu.ifi.dbs.elki.database.ids.DBID;
import de.lmu.ifi.dbs.elki.database.ids.DBIDRef;
import de.lmu.ifi.dbs.elki.database.ids.DBIDUtil;
import de.lmu.ifi.dbs.elki.database.ids.DBIDVar;

/**
 * A class to answer representation queries using the stored Array.
 * 
 * @author Erich Schubert
 * 
 * @apiviz.composedOf de.lmu.ifi.dbs.elki.database.datastore.DataStoreIDMap
 */
public class ArrayDBIDStore implements WritableDBIDDataStore {
  /**
   * Data array
   */
  private ArrayModifiableDBIDs data;

  /**
   * DBID to index map
   */
  private DataStoreIDMap idmap;

  /**
   * Constructor.
   * 
   * @param size Size
   * @param idmap ID map
   */
  public ArrayDBIDStore(int size, DataStoreIDMap idmap) {
    super();
    this.data = DBIDUtil.newArray(size);
    // Initialize
    DBIDRef inv = DBIDUtil.invalid();
    for (int i = 0; i < size; i++) {
      data.add(inv);
    }
    this.idmap = idmap;
  }

  @Override
  @Deprecated
  public DBID get(DBIDRef id) {
    return data.get(idmap.mapDBIDToOffset(id));
  }

  @Override
  public DBIDVar assignVar(DBIDRef id, DBIDVar var) {
    data.assignVar(idmap.mapDBIDToOffset(id), var);
    return var;
  }

  @Override
  @Deprecated
  public DBID put(DBIDRef id, DBID value) {
    final int off = idmap.mapDBIDToOffset(id);
    DBID ret = data.get(off);
    data.set(off, value);
    return ret;
  }

  @Override
  public void putDBID(DBIDRef id, DBIDRef value) {
    final int off = idmap.mapDBIDToOffset(id);
    data.set(off, value);
  }

  @Override
  public void put(DBIDRef id, DBIDRef value) {
    final int off = idmap.mapDBIDToOffset(id);
    data.set(off, value);
  }

  @Override
  public void destroy() {
    data = null;
    idmap = null;
  }

  @Override
  public void delete(DBIDRef id) {
    throw new UnsupportedOperationException("Can't delete from a static array storage.");
  }

  @Override
  public String getLongName() {
    return "raw";
  }

  @Override
  public String getShortName() {
    return "raw";
  }
}
