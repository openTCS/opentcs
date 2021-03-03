/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.tree;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.components.tree.elements.UserObject;
import org.opentcs.guing.model.ModelComponent;

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
public class TreeViewManager {

  /**
   * The tree view.
   */
  private final TreeView fTreeView;
  /**
   * Whether to hide blocks from the tree view or not.
   */
  private boolean hideBlocks;

  /**
   * Creates a new instance.
   *
   * @param treeView
   */
  public TreeViewManager(TreeView treeView) {
    fTreeView = Objects.requireNonNull(treeView, "treeView is null");
  }

  public TreeView getTreeView() {
    return fTreeView;
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
    Set<UserObject> objects = fTreeView.getSelectedItems();
    Set<ModelComponent> components = new HashSet<>();

    for (UserObject object : objects) {
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

  public void setHideBlocks(boolean hideBlocks) {
    this.hideBlocks = hideBlocks;
  }

  public boolean isHideBlocks() {
    return hideBlocks;
  }

  /**
   * Fügt dem TreeView ein Item hinzu.
   *
   * @param parent das Elternobjekt
   * @param item das hinzuzufügende Element
   */
  public void addItem(Object parent, ModelComponent item) {
    if (item.isTreeViewVisible()) {
      fTreeView.addItem(parent, item.createUserObject());
    }
  }

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
      OpenTCSView.instance().editingOptions(comps);
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
    OpenTCSView.instance().editingOptions(components);
  }
}
