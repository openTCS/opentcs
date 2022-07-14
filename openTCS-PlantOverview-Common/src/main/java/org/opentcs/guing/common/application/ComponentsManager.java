/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.application;

import java.util.List;
import org.opentcs.guing.common.components.tree.elements.UserObject;

/**
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface ComponentsManager {

  /**
   * Adds the given model components to the data model. (e.g. when pasting)
   *
   * @param userObjects The user objects to restore.
   * @return The restored user objects.
   */
  List<UserObject> restoreModelComponents(List<UserObject> userObjects);
}
