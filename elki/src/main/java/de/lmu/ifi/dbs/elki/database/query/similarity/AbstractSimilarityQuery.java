package de.lmu.ifi.dbs.elki.database.query.similarity;

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
import de.lmu.ifi.dbs.elki.database.relation.Relation;

/**
 * A distance query serves as adapter layer for database and primitive
 * similarity functions.
 * 
 * @author Erich Schubert
 * 
 * @param <O> Input object type
 */
public abstract class AbstractSimilarityQuery<O> implements SimilarityQuery<O> {
  /**
   * The data to use for this query
   */
  final protected Relation<? extends O> relation;

  /**
   * Constructor.
   * 
   * @param relation Relation to use.
   */
  public AbstractSimilarityQuery(Relation<? extends O> relation) {
    super();
    this.relation = relation;
  }

  /**
   * Returns the distance between the two objects specified by their object ids.
   * 
   * @param id1 first object id
   * @param id2 second object id
   * @return the distance between the two objects specified by their object ids
   */
  @Override
  public abstract double similarity(DBIDRef id1, DBIDRef id2);

  /**
   * Returns the distance between the two objects specified by their object ids.
   * 
   * @param o1 first object
   * @param id2 second object id
   * @return the distance between the two objects specified by their object ids
   */
  @Override
  public abstract double similarity(O o1, DBIDRef id2);

  /**
   * Returns the distance between the two objects specified by their object ids.
   * 
   * @param id1 first object id
   * @param o2 second object
   * @return the distance between the two objects specified by their object ids
   */
  @Override
  public abstract double similarity(DBIDRef id1, O o2);

  /**
   * Returns the distance between the two objects specified by their object ids.
   * 
   * @param o1 first object
   * @param o2 second object
   * @return the distance between the two objects specified by their object ids
   */
  @Override
  public abstract double similarity(O o1, O o2);

  @Override
  public Relation<? extends O> getRelation() {
    return relation;
  }
}