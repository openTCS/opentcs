/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.model;

import java.util.List;
import org.jhotdraw.draw.Figure;
import org.opentcs.guing.model.elements.AbstractConnection;

/**
 * Interface für solche ModelComponent-Klassen, die jeweils ein Figure als
 * grafische Repräsentation besitzen.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface FigureComponent
    extends ModelComponent {

  /**
   * Setzt das Figure.
   *
   * @param figure
   */
  void setFigure(Figure figure);

  /**
   * Liefert das Figure.
   *
   * @return das Figure
   */
  Figure getFigure();

  /**
   * Adds a Path to another Point or a Link to a Location
   *
   * @param connection The Path or Link to be added.
   */
  void addConnection(AbstractConnection connection);

  /**
   * @return All connected Paths and Links
   */
  List<AbstractConnection> getConnections();

  /**
   * Removes a connection which is no longer connected to this figure.
   *
   * @param connection The Path or Link to be removed.
   */
  void removeConnection(AbstractConnection connection);
}
