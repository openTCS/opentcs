/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.tree;

import java.awt.event.MouseListener;
import javax.inject.Inject;
import org.opentcs.guing.components.tree.elements.UserObjectContext;
import org.opentcs.guing.components.tree.elements.UserObjectUtil;
import org.opentcs.guing.model.ModelComponent;

/**
 * A tree view manager for groups.
 *
 * @author Philipp Seifert (Philipp.Seifert@iml.fraunhofer.de)
 */
public class GroupsTreeViewManager
    extends TreeViewManager {

  /**
   * Creates a new instance.
   *
   * @param treeView The tree view
   * @param userObjectUtil The user object util
   * @param mouseListener The mouse listener
   */
  @Inject
  public GroupsTreeViewManager(TreeView treeView,
                               UserObjectUtil userObjectUtil,
                               MouseListener mouseListener) {
    super(treeView, userObjectUtil, mouseListener);
  }

  @Override
  public void addItem(Object parent, ModelComponent item) {
    if (item.isTreeViewVisible()) {
      UserObjectContext context
          = userObjectUtil.createContext(UserObjectContext.ContextType.GROUP);
      getTreeView().addItem(parent, userObjectUtil.createUserObject(item, context));
    }
  }
}
