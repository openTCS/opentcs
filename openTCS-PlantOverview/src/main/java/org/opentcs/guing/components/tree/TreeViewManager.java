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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Enumeration;
import java.util.HashSet;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.tree.TreeNode;
import org.opentcs.guing.components.tree.elements.UserObject;
import org.opentcs.guing.components.tree.elements.UserObjectUtil;
import org.opentcs.guing.event.ModelNameChangeEvent;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.util.event.EventHandler;

/**
 * Der TreeViewManager spielt die Rolle einer Schnittstelle zwischen Applikation
 * und TreeView. Dadurch wird Code aus der Applikation ausgelagert. Der
 * TreeViewManager leitet Aufgaben der Applikation an den TreeView weiter.
 * Umgekehrt gibt es jedoch keinen Kommunikationsweg.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @see ModelingApplication
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
  private Predicate<ModelComponent> componentFilter = Predicates.alwaysTrue();

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
    @SuppressWarnings("unchecked")
    Enumeration<TreeNode> eTreeNodes
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
   * Erstellt die Baumansicht für die übergebene Komponente des Systemmodells.
   * Dieser Aufruf ergeht dann rekursiv an alle Kindkomponenten.
   *
   * @param component
   */
  public void restoreTreeView(ModelComponent component) {
    component.treeRestore(null, this);
  }

  public void setComponentFilter(Predicate<ModelComponent> componentFilter) {
    this.componentFilter = requireNonNull(componentFilter, "componentFilter");
  }

  public boolean accepts(ModelComponent component) {
    return componentFilter.apply(component);
  }

  /**
   * Fügt dem TreeView ein Item hinzu.
   *
   * @param parent das Elternobjekt
   * @param item das hinzuzufügende Element
   */
  public abstract void addItem(Object parent, ModelComponent item);

  /**
   * Teilt dem TreeView mit, dass sich ein Item geändert hat, welches also
   * aktualisiert werden müsste.
   *
   * @param item
   */
  public void itemChanged(Object item) {
    fTreeView.itemChanged(item);
  }

  /**
   * Teilt dem TreeView mit, dass es ein bestimmtes Item löschen soll.
   *
   * @param item
   */
  public void removeItem(Object item) {
    fTreeView.removeItem(item);
  }

  /**
   * Teilt dem TreeView mit, dass alle Kindelemente eines bestimmtes Items
   * gelöscht werden sollen. Das übergebene Item wird jedoch nicht entfernt.
   *
   * @param item
   */
  public void removeChildren(Object item) {
    fTreeView.removeChildren(item);
  }

  /**
   * Selektiert ein Item im TreeView.
   *
   * @param component
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
}
