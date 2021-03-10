/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.model;

import java.util.ArrayList;
import java.util.List;
import org.opentcs.guing.model.elements.AbstractConnection;

/**
 * A {@link ModelComponent} that can be connected with another model component.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class AbstractConnectableModelComponent
    extends AbstractModelComponent
    implements ConnectableModelComponent {

  /**
   * The Links and Paths connected with this model component (Location or Point).
   */
  private List<AbstractConnection> fConnections = new ArrayList<>();

  /**
   * Creates a new instance.
   */
  public AbstractConnectableModelComponent() {
    // Do nada.
  }

  @Override // ConnectableModelComponent
  public void addConnection(AbstractConnection connection) {
    fConnections.add(connection);
  }

  @Override // ConnectableModelComponent
  public void removeConnection(AbstractConnection connection) {
    fConnections.remove(connection);
  }

  @Override // ConnectableModelComponent
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
  public boolean hasConnectionTo(ConnectableModelComponent component) {
    return getConnectionTo(component) != null;
  }

  /**
   * Returns the connection to the given component.
   *
   * @param component The component.
   * @return The connection to the given component, or <code>null</code>, if
   * there is none.
   */
  public AbstractConnection getConnectionTo(ConnectableModelComponent component) {
    for (AbstractConnection connection : fConnections) {
      if (connection.getStartComponent() == this
          && connection.getEndComponent() == component) {
        return connection;
      }
    }

    return null;
  }

  @Override // AbstractModelComponent
  public AbstractModelComponent clone()
      throws CloneNotSupportedException {
    AbstractConnectableModelComponent clone = (AbstractConnectableModelComponent) super.clone();
    clone.fConnections = new ArrayList<>();

    return clone;
  }
}
