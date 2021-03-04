/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.tree;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.opentcs.guing.components.tree.elements.UserObject;

/**
 * Ein CellRenderer für die Knoten der Baumansicht, die über spezielle Icons
 * verfügen.
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
