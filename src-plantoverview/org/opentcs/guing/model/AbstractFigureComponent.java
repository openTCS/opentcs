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

import java.util.ArrayList;
import java.util.List;
import org.jhotdraw.draw.Figure;
import org.opentcs.guing.model.elements.AbstractConnection;

/**
 * A model component that may keep an associated figure.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class AbstractFigureComponent
    extends AbstractModelComponent
    implements FigureComponent,
               Comparable<AbstractFigureComponent> {

  /**
   * The X cordinate of the corresponding object on the kernel side.
   */
  public static final String MODEL_X_POSITION = "modelXPosition";
  /**
   * The X cordinate of the corresponding object on the kernel side.
   */
  public static final String MODEL_Y_POSITION = "modelYPosition";
  /**
   * The referenced Figure.
   */
  private transient Figure fFigure;
  /**
   * The Links and Paths connected with this Figure (Location or Point).
   */
  private List<AbstractConnection> fConnections = new ArrayList<>();

  /**
   * Creates a new instance.
   */
  public AbstractFigureComponent() {
    // Do nada.
  }

  @Override // Comparable
  public int compareTo(AbstractFigureComponent o) {
    return getName().compareTo(o.getName());
  }

  @Override // FigureComponent
  public void addConnection(AbstractConnection connection) {
    fConnections.add(connection);
  }

  @Override // FigureComponent
  public void removeConnection(AbstractConnection connection) {
    fConnections.remove(connection);
  }

  @Override // FigureComponent
  public List<AbstractConnection> getConnections() {
    return fConnections;
  }

  /**
   * Checks whether there is a connection to the given component.
   *
   * @param component The component.
   * @return <code>true</code> if, and only if, this component is connected to
   * the given one.
   */
  public boolean hasConnectionTo(FigureComponent component) {
    return getConnectionTo(component) != null;
  }

  /**
   * Returns the connection to the given component.
   *
   * @param component The component.
   * @return The connection to the given component, or <code>null</code>, if
   * there is none.
   */
  public AbstractConnection getConnectionTo(FigureComponent component) {
    for (AbstractConnection connection : fConnections) {
      if (connection.getStartComponent() == this
          && connection.getEndComponent() == component) {
        return connection;
      }
    }

    return null;
  }

  @Override // FigureComponent
  public Figure getFigure() {
    return fFigure;
  }

  @Override // FigureComponent
  public void setFigure(Figure figure) {
    fFigure = figure;
  }

  @Override // AbstractModelComponent
  public AbstractModelComponent clone()
      throws CloneNotSupportedException {
    AbstractFigureComponent clone = (AbstractFigureComponent) super.clone();
    clone.fConnections = new ArrayList<>();

    return clone;
  }
}
