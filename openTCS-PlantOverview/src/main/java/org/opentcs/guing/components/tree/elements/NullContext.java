/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.tree.elements;

import java.util.Set;
import javax.swing.JPopupMenu;

/**
 * A null context. Use this when no special context is required.
 *
 * @author Philipp Seifert (Philipp.Seifert@iml.fraunhofer.de)
 */
public class NullContext
    implements UserObjectContext {

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
