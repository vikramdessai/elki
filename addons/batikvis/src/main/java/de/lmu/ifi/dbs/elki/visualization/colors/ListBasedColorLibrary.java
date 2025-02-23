package de.lmu.ifi.dbs.elki.visualization.colors;

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
 * Color library using the color names from a list.
 * 
 * @author Erich Schubert
 */
public class ListBasedColorLibrary implements ColorLibrary {
  /**
   * Array of color names.
   */
  private String[] colors;

  /**
   * Color scheme name
   */
  private String name;

  /**
   * Constructor without a properties file name.
   * 
   * @param colors Colors
   * @param name Library name
   */
  public ListBasedColorLibrary(String[] colors, String name) {
    this.colors = colors;
    this.name = name;
  }

  @Override
  public String getColor(int index) {
    return colors[Math.abs(index) % colors.length];
  }

  @Override
  public int getNumberOfNativeColors() {
    return colors.length;
  }

  /**
   * Get the color scheme name.
   * 
   * @return the name
   */
  protected String getName() {
    return name;
  }
}