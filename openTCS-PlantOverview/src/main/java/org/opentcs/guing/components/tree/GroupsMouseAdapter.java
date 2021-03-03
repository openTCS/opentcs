/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.tree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.components.tree.elements.FigureUserObject;
import org.opentcs.guing.components.tree.elements.LocationUserObject;
import org.opentcs.guing.components.tree.elements.PathUserObject;
import org.opentcs.guing.components.tree.elements.PointUserObject;
import org.opentcs.guing.components.tree.elements.UserObject;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.GroupModel;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * A mouse adapter for the <code>TreeView</code> for groups.
 *
 * @author Philipp Seifert (Philipp.Seifert@iml.fraunhofer.de)
 */
public class GroupsMouseAdapter
    extends TreeMouseAdapter {

  @Inject
  public GroupsMouseAdapter(TreeView treeView) {
    super(treeView);
  }

  @Override
  protected void evaluateRightClick(MouseEvent e,
                                    UserObject userObject, 
                                    Set<UserObject> oldSelection) {
    JTree objectTree = treeView.getTree();
    if (userObject instanceof PointUserObject
        || userObject instanceof LocationUserObject
        || userObject instanceof PathUserObject) {
      if (e.isControlDown()) {
        oldSelection.add(userObject);
        Set<ModelComponent> dataObjects = new HashSet<>();

        for (UserObject userObj : oldSelection) {
          dataObjects.add(userObj.getModelComponent());
        }

        treeView.selectItems(dataObjects);
      }

      FigureUserObject pub = (FigureUserObject) userObject;
      pub.rightClicked(objectTree, e.getX(), e.getY());
    }
    else {
      showPopupMenuGroup(userObject.getModelComponent(), e.getX(), e.getY());
    }
  }

  /**
   * A special popup menu when clicking on a group.
   *
   * @param folder Folder containing the group.
   * @param x x position.
   * @param y y position.
   */
  private void showPopupMenuGroup(ModelComponent folder, int x, int y) {
    final GroupModel groupFolder = (GroupModel) folder;
    JPopupMenu menu = new JPopupMenu();
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();

    final OpenTCSView openTCSView = OpenTCSView.instance();

    final JCheckBoxMenuItem cbItemAll = new JCheckBoxMenuItem(
        labels.getString("tree.group.showInAll"), groupFolder.isGroupVisible());
    cbItemAll.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        openTCSView.setGroupVisibilityInAllDrawingViews(groupFolder,
                                                        cbItemAll.isSelected());
      }
    });
    menu.add(cbItemAll);

    for (final String title : openTCSView.getDrawingViewNames()) {
      final JCheckBoxMenuItem cbItem = new JCheckBoxMenuItem(
          labels.getFormatted("tree.group.show", title),
          groupFolder.isGroupInDrawingViewVisible(title));
      cbItem.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          openTCSView.setGroupVisibilityInDrawingView(title,
                                                      groupFolder,
                                                      cbItem.isSelected());
        }
      });
      menu.add(cbItem);
    }

    menu.addSeparator();

    JMenuItem item = new JMenuItem(labels.getString("tree.group.add"));
    item.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        OpenTCSView.instance().addSelectedItemsToGroup(groupFolder);
      }
    });
    menu.add(item);

    item = new JMenuItem(labels.getString("tree.group.delete"));
    item.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        OpenTCSView.instance().deleteGroup(groupFolder);
      }
    });

    menu.add(item);
    menu.show(treeView.getTree(), x, y);
  }  
}
