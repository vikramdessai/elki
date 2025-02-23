package de.lmu.ifi.dbs.elki.parallel.variables;

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
 * Variable to share between different processors (within one thread only!)
 * 
 * @author Erich Schubert
 * 
 * @apiviz.has SharedObject.Instance
 * 
 * @param <T> Data type
 */
public class SharedObject<T> implements SharedVariable<SharedObject.Instance<T>> {
  @Override
  public Instance<T> instantiate() {
    return new Instance<>();
  }

  /**
   * Instance for a particular thread.
   * 
   * @author Erich Schubert
   * 
   * @param <T> Data type
   */
  public static class Instance<T> implements SharedVariable.Instance<T> {
    /**
     * Cache for last data consumed/produced
     */
    private T data = null;

    @Override
    public T get() {
      return data;
    }

    @Override
    public void set(T data) {
      this.data = data;
    }
  }
}