// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.application;

import java.util.List;
import org.opentcs.guing.common.components.tree.elements.UserObject;

/**
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
