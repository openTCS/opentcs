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
import javax.annotation.Nullable;
import javax.swing.JPopupMenu;

/**
 * A context indicating if an user object is contained in
 * the components, blocks or groups tree view. Currently it only
 * offers a tree dependant popup menu.
 *
 * @author Philipp Seifert (Philipp.Seifert@iml.fraunhofer.de)
 */
public interface UserObjectContext {

  /**
   * Returns a popup menu with actions for this context.
   *
   * @param selectedUserObjects The user objects that are currently selected
   * in the tree view.
   * @return A popup menu.
   */
  JPopupMenu getPopupMenu(@Nullable Set<UserObject> selectedUserObjects);

  /**
   * Called after a specific item was removed from the tree (via the <code>
   * DeleteAction</code>.
   *
   * @param userObject The UserObject affected.
   * @return <code>true</code>, if it was successfully removed.
   */
  boolean removed(UserObject userObject);

  /**
   * Returns the type of this context.
   *
   * @return One of CONTEXT_TYPE.
   */
  ContextType getType();

  public enum ContextType {

    COMPONENT,
    BLOCK,
    GROUP,
    NULL;
  }
}
