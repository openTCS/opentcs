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
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.application.ViewManager;
import org.opentcs.guing.components.drawing.DrawingViewScrollPane;
import org.opentcs.guing.components.drawing.OpenTCSDrawingView;
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

  private final ViewManager viewManager;

  /**
   * Creates a new instance.
   *
   * @param treeView The tree view
   * @param viewManager The view manager
   */
  @Inject
  public GroupsMouseAdapter(TreeView treeView, ViewManager viewManager) {
    super(treeView);
    this.viewManager = requireNonNull(viewManager, "viewManager");
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
    cbItemAll.addActionListener((ActionEvent e)
        -> setGroupVisibilityInAllDrawingViews(groupFolder, cbItemAll.isSelected())
    );
    menu.add(cbItemAll);

    for (final String title : viewManager.getDrawingViewNames()) {
      final JCheckBoxMenuItem cbItem = new JCheckBoxMenuItem(
          labels.getFormatted("tree.group.show", title),
          groupFolder.isGroupInDrawingViewVisible(title));
      cbItem.addActionListener((ActionEvent e)
          -> setGroupVisibilityInDrawingView(title, groupFolder, cbItem.isSelected()));
      menu.add(cbItem);
    }

    menu.addSeparator();

    JMenuItem item = new JMenuItem(labels.getString("tree.group.add"));
    item.addActionListener((ActionEvent e) -> openTCSView.addSelectedItemsToGroup(groupFolder));
    menu.add(item);

    item = new JMenuItem(labels.getString("tree.group.delete"));
    item.addActionListener((ActionEvent e) -> openTCSView.deleteGroup(groupFolder));

    menu.add(item);
    menu.show(treeView.getTree(), x, y);
  }

  /**
   * Toggles the visibility of the group members.
   *
   * @param gm The folder that contains the elements of the group.
   * @param visible Visible or not.
   */
  public void setGroupVisibilityInAllDrawingViews(GroupModel gm, boolean visible) {
    gm.setGroupVisible(visible);

    for (OpenTCSDrawingView drawingView : getDrawingViews()) {
      drawingView.setGroupVisible(gm.getChildComponents(), gm.isGroupVisible());
    }
  }

  /**
   * Toggles the visibility of the group members for a specific
   * <code>OpenTCSDrawingView</code>.
   *
   * @param title The title of the drawing view.
   * @param sf The group folder containing the elements to hide.
   * @param visible Visible or not.
   */
  public void setGroupVisibilityInDrawingView(String title, GroupModel sf, boolean visible) {
    sf.setDrawingViewVisible(title, visible);
    viewManager.setGroupVisibilityInDrawingView(title, sf, visible);
  }

  /**
   * Returns all drawing views (including the modelling view)
   *
   * @return List with all known <code>OpenTCSDrawingViews</code>.
   */
  private List<OpenTCSDrawingView> getDrawingViews() {
    List<OpenTCSDrawingView> views = new ArrayList<>();

    for (DrawingViewScrollPane scrollPane : viewManager.getDrawingViewMap().values()) {
      views.add(scrollPane.getDrawingView());
    }

    return views;
  }

}
