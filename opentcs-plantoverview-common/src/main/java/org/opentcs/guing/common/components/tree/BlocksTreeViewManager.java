// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.tree;

import jakarta.inject.Inject;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JTree;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.common.components.tree.elements.UserObjectContext;
import org.opentcs.guing.common.components.tree.elements.UserObjectUtil;

/**
 * A tree view manager for blocks.
 */
public class BlocksTreeViewManager
    extends
      TreeViewManager {

  @Inject
  @SuppressWarnings("this-escape")
  public BlocksTreeViewManager(
      TreeView treeView,
      UserObjectUtil userObjectUtil,
      MouseListener mouseListener
  ) {
    super(treeView, userObjectUtil, mouseListener);

    // If the user clicks on an element in the tree view that is contained in several blocks,
    // then we don't want to select the first element but instead the element in the block that
    // the user clicked on.
    initSpecializedSelector();
  }

  @Override
  public void addItem(Object parent, ModelComponent item) {
    if (item.isTreeViewVisible()) {
      UserObjectContext context = userObjectUtil.createContext(UserObjectContext.ContextType.BLOCK);
      getTreeView().addItem(parent, userObjectUtil.createUserObject(item, context));
    }
  }

  /**
   * Registers a listener for mouse events that helps to select the element in the tree view that
   * the user has clicked on.
   * This is necessary as long as {@link AbstractTreeViewPanel#selectItem(java.lang.Object)} simply
   * selects the first occurrence of the given object, not expecting the object to occur in the tree
   * more than once.
   */
  private void initSpecializedSelector() {
    JTree tree = getTreeView().getTree();
    tree.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        tree.setSelectionPath(tree.getClosestPathForLocation(e.getX(), e.getY()));
      }
    });
  }

}
