/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.model;

import java.util.ArrayList;
import java.util.List;
import org.opentcs.guing.components.tree.TreeViewManager;
import org.opentcs.guing.components.tree.elements.SimpleFolderUserObject;
import org.opentcs.guing.components.tree.elements.UserObject;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Basisimplementierung für ein Kompositum im Systemmodell. Ein Kompositum
 * enthält eine Menge von Kindobjekten.
 * <p>
 * <b>Entwurfsmuster:</b> Kompositum.
 * CompositeModelComponent ist ein abstraktes Kompositum.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public abstract class CompositeModelComponent
    extends AbstractModelComponent {

  /**
   * The child elements.
   */
  private List<ModelComponent> fChildComponents = new ArrayList<>();

  /**
   * Creates a new instance.
   */
  public CompositeModelComponent() {
    this("Composite");
  }

  /**
   * Creates a new instance with the given name.
   *
   * @param treeViewName The name to be used.
   */
  public CompositeModelComponent(String treeViewName) {
    super(treeViewName);
  }

  @Override // AbstractModelComponent
  public UserObject createUserObject() {
    fUserObject = new SimpleFolderUserObject(this);
    return fUserObject;
  }

  @Override // AbstractModelComponent
  public void add(ModelComponent component) {
    getChildComponents().add(component);
    component.setParent(this);
  }

  @Override // AbstractModelComponent
  public List<ModelComponent> getChildComponents() {
    return fChildComponents;
  }

  public boolean hasProperties() {
    return false;
  }

  @Override // AbstractModelComponent
  public void remove(ModelComponent component) {
    getChildComponents().remove(component);
  }

  @Override // AbstractModelComponent
  public void treeRestore(ModelComponent parent, TreeViewManager treeViewManager) {
    if (getTreeViewName().equals(ResourceBundleUtil.getBundle().getString("tree.blocks.text"))) {
      if (treeViewManager.isHideBlocks()) {
        return;
      }
    }

    super.treeRestore(parent, treeViewManager);
    for (ModelComponent component : getChildComponents()) {
      component.treeRestore(this, treeViewManager);
    }
  }

  @Override // AbstractModelComponent
  public boolean contains(ModelComponent component) {
    return getChildComponents().contains(component);
  }

  @Override
  public CompositeModelComponent clone() throws CloneNotSupportedException {
    CompositeModelComponent clone = (CompositeModelComponent) super.clone();
    clone.fChildComponents = new ArrayList<>(fChildComponents);
    return clone;
  }
}
