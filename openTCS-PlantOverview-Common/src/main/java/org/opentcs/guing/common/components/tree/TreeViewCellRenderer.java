/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.tree;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.opentcs.guing.common.components.tree.elements.UserObject;

/**
 * A cell renderer for a node in the tree view.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class TreeViewCellRenderer
    extends DefaultTreeCellRenderer {

  /**
   * Creates a new instance.
   */
  public TreeViewCellRenderer() {
    super();
  }

  @Override
  public Component getTreeCellRendererComponent(
      JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
      int row, boolean hasFocus) {

    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
    Object userObject = node.getUserObject();

    if (userObject instanceof UserObject) {
      ImageIcon icon = ((UserObject) userObject).getIcon();

      if (icon != null) {
        setIcon(icon);
      }
    }

    return this;
  }
}
