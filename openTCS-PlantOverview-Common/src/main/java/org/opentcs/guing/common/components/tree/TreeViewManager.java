/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.tree;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Enumeration;
import java.util.HashSet;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.function.Predicate;
import javax.inject.Inject;
import javax.swing.tree.TreeNode;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.common.components.tree.elements.UserObject;
import org.opentcs.guing.common.components.tree.elements.UserObjectUtil;
import org.opentcs.guing.common.event.ModelNameChangeEvent;
import org.opentcs.util.event.EventHandler;

/**
 * The tree view manager is the interface between the application and the TreeView.
 * It passes actions on to the TreeView.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @see TreeView
 */
public abstract class TreeViewManager
    implements EventHandler {

  /**
   * The tree view.
   */
  private final TreeView fTreeView;
  /**
   * A factory for UserObjects.
   */
  protected final UserObjectUtil userObjectUtil;
  /**
   * This manager's component filter.
   */
  private Predicate<ModelComponent> componentFilter = (component) -> true;

  /**
   * Creates a new instance.
   *
   * @param treeView The actual tree view.
   * @param userObjectUtil A factory for UserObjects.
   * @param mouseListener The MouseListener for the TreeView.
   */
  @Inject
  public TreeViewManager(TreeView treeView,
                         UserObjectUtil userObjectUtil,
                         MouseListener mouseListener) {
    this.fTreeView = requireNonNull(treeView, "treeView is null");
    this.userObjectUtil = requireNonNull(userObjectUtil, "userObjectUtil");
    requireNonNull(mouseListener, "mouseListener");
    fTreeView.getTree().addMouseListener(mouseListener);
  }

  public TreeView getTreeView() {
    return fTreeView;
  }

  /**
   * Delegates the call to the <code>TreeView</code>.
   *
   * @param mouseListener The Listener to add.
   */
  public void addMouseListener(MouseListener mouseListener) {
    getTreeView().addMouseListener(mouseListener);
  }

  /**
   * Delegates the call to the <code>TreeView</code>.
   *
   * @param mouseListener The Listener to add.
   */
  public void addMouseMotionListener(MouseMotionListener mouseListener) {
    getTreeView().addMouseMotionListener(mouseListener);
  }

  /**
   * Sorts the items of the <code>TreeView</code>.
   */
  public void sortItems() {
    Enumeration<? extends TreeNode> eTreeNodes
        = ((TreeNode) getTreeView().getTree().getModel().getRoot()).children();

    while (eTreeNodes.hasMoreElements()) {
      getTreeView().sortItems(eTreeNodes.nextElement());
    }
  }

  @Override
  public void onEvent(Object event) {
    if (event instanceof ModelNameChangeEvent) {
      updateModelName((ModelNameChangeEvent) event);
    }
  }

  /**
   * Updates the text at the top of the <code>TreeView</code>.
   *
   * @param event The <code>ModelNameChangeEvent</code>.
   */
  private void updateModelName(ModelNameChangeEvent event) {
    // Updates the text at the top of the tree view.
    String newName = event.getNewName();
    getTreeView().updateText(newName);
  }

  /**
   * Returns whether the <code>TreeView</code> has buffered objects.
   *
   * @return <code>true</code> if it has some.
   */
  public boolean hasBufferedObjects() {
    return getTreeView().hasBufferedObjects();
  }

  /**
   * Returns the dragged user object of the <code>TreeView</code>.
   *
   * @param e The event where the mouse click happened.
   * @return The user object that was dragged.
   */
  public UserObject getDraggedUserObject(MouseEvent e) {
    return getTreeView().getDraggedUserObject(e);
  }

  /**
   * Sets the cursor in the <code>TreeView</code>.
   *
   * @param cursor The new cursor.
   */
  public void setCursor(Cursor cursor) {
    getTreeView().setCursor(cursor);
  }

  /**
   * Returns the currently selected item.
   *
   * @return The currently selected item.
   */
  public ModelComponent getSelectedItem() {
    UserObject userObject = fTreeView.getSelectedItem();

    if (userObject != null) {
      return userObject.getModelComponent();
    }
    else {
      return null;
    }
  }

  /**
   * Returns the currently selected items.
   *
   * @return The currently selected items.
   */
  public Set<ModelComponent> getSelectedItems() {
    Set<ModelComponent> components = new HashSet<>();

    for (UserObject object : fTreeView.getSelectedItems()) {
      components.add(object.getModelComponent());
    }

    return components;
  }

  /**
   * Creates the tree view from the specified model component.
   *
   * @param component The component to restore.
   */
  public void restoreTreeView(ModelComponent component) {
    restoreTreeViewRecursively(null, component);
  }

  public void setComponentFilter(Predicate<ModelComponent> componentFilter) {
    this.componentFilter = requireNonNull(componentFilter, "componentFilter");
  }

  private boolean accepts(ModelComponent component) {
    return componentFilter.test(component);
  }

  /**
   * Add an item to the TreeView.
   *
   * @param parent The parent item to add to.
   * @param item The item to add.
   */
  public abstract void addItem(Object parent, ModelComponent item);

  /**
   * Notifies the TreeView that the item has changed.
   *
   * @param item the item that was changed.
   */
  public void itemChanged(Object item) {
    fTreeView.itemChanged(item);
  }

  /**
   * Notifies the TreeView that an item has been deleted.
   *
   * @param item The item that has been deleted.
   */
  public void removeItem(Object item) {
    fTreeView.removeItem(item);
  }

  /**
   * Notifies the TreeView that all child elements of the specified element should be deleted.
   *
   * @param item The item of which to delete its children.
   */
  public void removeChildren(Object item) {
    fTreeView.removeChildren(item);
  }

  /**
   * Select an item in the TreeView.
   *
   * @param component The component to select.
   */
  public void selectItem(ModelComponent component) {
    if (component != null && component.isTreeViewVisible()) {
      fTreeView.selectItem(component);
      Set<ModelComponent> comps = new HashSet<>(1);
      comps.add(component);
    }
  }

  /**
   * Selects multiple items in the tree view.
   *
   * @param components The components to select. Pass <code>null</code>
   * to deselect everything currently selected.
   */
  public void selectItems(Set<ModelComponent> components) {
    Set<ModelComponent> visibleComponents = new HashSet<>();

    if (components != null) {
      for (ModelComponent comp : components) {
        if (comp.isTreeViewVisible()) {
          visibleComponents.add(comp);
        }
      }
    }

    fTreeView.selectItems(visibleComponents);
  }

  private void restoreTreeViewRecursively(ModelComponent parent, ModelComponent item) {
    if (accepts(item)) {
      addItem(parent, item);
    }

    for (ModelComponent child : item.getChildComponents()) {
      if (!child.getChildComponents().isEmpty()) {
        restoreTreeViewRecursively(item, child);
      }
      else if (accepts(child)) {
        addItem(item, child);
      }
    }
  }
}
