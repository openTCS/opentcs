// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.tree.elements;

import java.util.Set;
import javax.swing.JPopupMenu;

/**
 * A null context. Use this when no special context is required.
 */
public class NullContext
    implements
      UserObjectContext {

  /**
   * Creates a new instance.
   */
  public NullContext() {
  }

  @Override
  public JPopupMenu getPopupMenu(Set<UserObject> selectedUserObjects) {
    return new JPopupMenu();
  }

  @Override
  public boolean removed(UserObject userObject) {
    return true;
  }

  @Override
  public ContextType getType() {
    return ContextType.NULL;
  }
}
