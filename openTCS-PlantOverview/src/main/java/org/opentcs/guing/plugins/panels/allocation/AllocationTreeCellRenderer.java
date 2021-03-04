/*
 * openTCS copyright information:
 * Copyright (c) 2017 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.allocation;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResource;
import org.opentcs.guing.util.IconToolkit;

/**
 * Changes the icons of the different tree levels to the standard vehicle, point and path icons
 * used in the plant overview.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 */
public class AllocationTreeCellRenderer
    extends DefaultTreeCellRenderer {

  /**
   * The icon for vehicles in the tree view.
   */
  private static final ImageIcon VEHICLE_ICON
      = IconToolkit.instance().createImageIcon("tree/vehicle.18x18.png");
  /**
   * The icon for points in the tree view.
   */
  private static final ImageIcon POINT_ICON
      = IconToolkit.instance().createImageIcon("tree/point.18x18.png");
  /**
   * The icon for paths in the tree view.
   */
  private static final ImageIcon PATH_ICON
      = IconToolkit.instance().createImageIcon("tree/path.18x18.png");

  /**
   * Creates a new instance.
   */
  public AllocationTreeCellRenderer() {
  }

  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object node, boolean selected,
                                                boolean expanded, boolean isLeaf, int row,
                                                boolean hasFocus) {
    //Let the superclass handle all its stuff related to rendering
    super.getTreeCellRendererComponent(tree, node, selected, expanded, isLeaf, row, hasFocus);
    if (node instanceof DefaultMutableTreeNode) {
      DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
      //User object is of type string only if the node contains a vehicle or is the root node
      if (treeNode.getUserObject() instanceof String) {
        setIcon(VEHICLE_ICON);
      }
      //User object is of type TCSResource only if the node contains a path or a point
      else if (treeNode.getUserObject() instanceof TCSResource) {
        TCSResource<?> resource = (TCSResource<?>) treeNode.getUserObject();
        setText(resource.getName());
        if (resource instanceof Path) {
          setIcon(PATH_ICON);
        }
        else if (resource instanceof Point) {
          setIcon(POINT_ICON);
        }
      }
    }
    return this;
  }

}
