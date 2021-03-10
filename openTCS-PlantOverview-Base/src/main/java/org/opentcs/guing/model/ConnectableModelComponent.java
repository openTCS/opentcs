/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.model;

import java.util.List;
import org.opentcs.guing.model.elements.AbstractConnection;

/**
 * A {@link ModelComponent} that can be connected with another model component.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface ConnectableModelComponent
    extends ModelComponent {

  /**
   * Adds a connection.
   *
   * @param connection The connection to be added.
   */
  void addConnection(AbstractConnection connection);

  /**
   * Returns all connections.
   *
   * @return All connections.
   */
  List<AbstractConnection> getConnections();

  /**
   * Removes a connection.
   *
   * @param connection The connection to be removed.
   */
  void removeConnection(AbstractConnection connection);
}
