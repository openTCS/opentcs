// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.tree.elements;

import static java.util.Objects.requireNonNull;

import jakarta.inject.Inject;
import java.util.Set;
import javax.swing.JPopupMenu;
import org.opentcs.guing.common.application.GuiManager;

/**
 * Context for the component tree view.
 */
public class ComponentContext
    implements
      UserObjectContext {

  private final GuiManager guiManager;

  /**
   * Creates a new instance.
   *
   * @param guiManager The gui manager.
   */
  @Inject
  public ComponentContext(GuiManager guiManager) {
    this.guiManager = requireNonNull(guiManager, "guiManager");
  }

  @Override
  public JPopupMenu getPopupMenu(final Set<UserObject> selectedUserObjects) {
    JPopupMenu menu = new JPopupMenu();
    return menu;
  }

  @Override
  public boolean removed(UserObject userObject) {
    return guiManager.treeComponentRemoved(userObject.getModelComponent());
  }

  @Override
  public ContextType getType() {
    return ContextType.COMPONENT;
  }
}
