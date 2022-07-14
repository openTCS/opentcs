/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.tree.elements;

import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.JPopupMenu;
import org.opentcs.guing.common.application.GuiManager;

/**
 * Context for the component tree view.
 *
 * @author Philipp Seifert (Philipp.Seifert@iml.fraunhofer.de)
 */
public class ComponentContext
    implements UserObjectContext {

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
