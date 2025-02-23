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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.lmu.ifi.dbs.elki.database.datastore.WritableDataStore;
import de.lmu.ifi.dbs.elki.database.datastore.WritableRecordStore;
import de.lmu.ifi.dbs.elki.database.ids.DBID;
import de.lmu.ifi.dbs.elki.database.ids.DBIDRef;
import de.lmu.ifi.dbs.elki.database.ids.DBIDUtil;

/**
 * A class to answer representation queries using a map and an index within the
 * record.
 * 
 * @author Erich Schubert
 * 
 * @apiviz.has MapRecordStore.StorageAccessor oneway - - projectsTo
 */
public class MapRecordStore implements WritableRecordStore {
  /**
   * Record length.
   */
  private final int rlen;

  /**
   * Storage Map.
   */
  // TODO: Use trove maps?
  private final Map<DBID, Object[]> data;

  /**
   * Constructor with existing data.
   * 
   * @param rlen Number of columns (record length)
   * @param data Existing data map
   */
  public MapRecordStore(int rlen, Map<DBID, Object[]> data) {
    super();
    this.rlen = rlen;
    this.data = data;
  }

  /**
   * Constructor without existing data.
   * 
   * @param rlen Number of columns (record length)
   */
  public MapRecordStore(int rlen) {
    this(rlen, new ConcurrentHashMap<DBID, Object[]>());
  }

  @Override
  public <T> WritableDataStore<T> getStorage(int col, Class<? super T> datatype) {
    // TODO: add type checking?
    return new StorageAccessor<>(col);
  }

  /**
   * Actual getter.
   * 
   * @param id Database ID
   * @param index column index
   * @param <T> type
   * @return current value
   */
  @SuppressWarnings("unchecked")
  protected <T> T get(DBIDRef id, int index) {
    Object[] d = data.get(DBIDUtil.deref(id));
    if (d == null) {
      return null;
    }
    return (T) d[index];
  }

  /**
   * Actual setter.
   * 
   * @param id Database ID
   * @param index column index
   * @param value new value
   * @param <T> type
   * @return previous value
   */
  @SuppressWarnings("unchecked")
  protected <T> T set(DBIDRef id, int index, T value) {
    Object[] d = data.get(DBIDUtil.deref(id));
    if (d == null) {
      d = new Object[rlen];
      data.put(DBIDUtil.deref(id), d);
    }
    T ret = (T) d[index];
    d[index] = value;
    return ret;
  }

  /**
   * Access a single record in the given data.
   * 
   * @author Erich Schubert
   * 
   * @param <T> Object data type to access
   */
  protected class StorageAccessor<T> implements WritableDataStore<T> {
    /**
     * Representation index.
     */
    private final int index;

    /**
     * Constructor.
     * 
     * @param index In-record index
     */
    protected StorageAccessor(int index) {
      super();
      this.index = index;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T get(DBIDRef id) {
      return (T) MapRecordStore.this.get(id, index);
    }

    @Override
    public T put(DBIDRef id, T value) {
      return MapRecordStore.this.set(id, index, value);
    }

    @Override
    public void destroy() {
      throw new UnsupportedOperationException("Record storage accessors cannot be destroyed.");
    }

    @Override
    public void delete(DBIDRef id) {
      throw new UnsupportedOperationException("Record storage values cannot be deleted.");
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

  @Override
  public boolean remove(DBIDRef id) {
    return data.remove(id) != null;
  }

}
