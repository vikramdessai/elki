package de.lmu.ifi.dbs.elki.database.datastore;

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

import de.lmu.ifi.dbs.elki.database.ids.DBIDRef;

/**
 * Represents a storage which stores multiple values per object in a record fashion.
 * 
 * @author Erich Schubert
 * 
 * @apiviz.landmark
 * 
 * @apiviz.has de.lmu.ifi.dbs.elki.database.datastore.WritableDataStore oneway - - projectsTo
 */
public interface WritableRecordStore extends RecordStore {
  /**
   * Get a {@link WritableDataStore} instance for a particular record column.
   * 
   * @param <T> Data type
   * @param col Column number
   * @param datatype data class
   * @return writable storage
   */
  @Override
  public <T> WritableDataStore<T> getStorage(int col, Class<? super T> datatype);
  
  /**
   * Remove an object from the store, all columns.
   * 
   * @param id object ID to remove
   * @return success code
   */
  public boolean remove(DBIDRef id);
}
