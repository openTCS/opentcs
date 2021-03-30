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
import javax.inject.Inject;
import javax.swing.JPopupMenu;
import org.opentcs.guing.model.elements.BlockModel;

/**
 * Context for the block tree view.
 *
 * @author Philipp Seifert (Philipp.Seifert@iml.fraunhofer.de)
 */
public class BlockContext
    implements UserObjectContext {

  /**
   * Creates a new instance.
   */
  @Inject
  public BlockContext() {
  }

  @Override
  public JPopupMenu getPopupMenu(Set<UserObject> selectedUserObjects) {
    JPopupMenu menu = new JPopupMenu();

    return menu;
  }

  @Override
  public boolean removed(UserObject userObject) {
    if (userObject.getParent() instanceof BlockModel) {
      BlockModel blockModel = (BlockModel) userObject.getParent();
      blockModel.removeCourseElement(userObject.getModelComponent());

      blockModel.courseElementsChanged();

      return true;
    }
    return false;
  }

  @Override
  public ContextType getType() {
    return ContextType.BLOCK;
  }
}
