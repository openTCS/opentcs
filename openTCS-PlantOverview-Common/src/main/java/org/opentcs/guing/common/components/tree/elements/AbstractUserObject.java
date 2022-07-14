/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.tree.elements;

import static java.util.Objects.requireNonNull;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.common.application.GuiManager;
import org.opentcs.guing.common.persistence.ModelManager;

/**
 * Abstract implementation of a UserObject.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public abstract class AbstractUserObject
    implements UserObject {

  /**
   * The model component this user object represents.
   */
  private final ModelComponent fModelComponent;
  /**
   * The gui manager.
   */
  private final GuiManager guiManager;
  /**
   * The model manager.
   */
  private final ModelManager modelManager;
  /**
   * The parent object.
   */
  private ModelComponent parent;

  /**
   * Creates a new instance.
   *
   * @param modelComponent The corresponding model component.
   * @param guiManager The gui manager.
   * @param modelManager Provides access to the currently loaded system model.
   */
  public AbstractUserObject(ModelComponent modelComponent,
                            GuiManager guiManager,
                            ModelManager modelManager) {
    this.fModelComponent = requireNonNull(modelComponent, "modelComponent");
    this.guiManager = requireNonNull(guiManager, "guiManager");
    this.modelManager = requireNonNull(modelManager, "modelManager");
  }

  @Override // Object
  public String toString() {
    return fModelComponent.getTreeViewName();
  }

  @Override // UserObject
  public ModelComponent getModelComponent() {
    return fModelComponent;
  }

  @Override // UserObject
  public void selected() {
    getGuiManager().selectModelComponent(getModelComponent());
  }

  @Override // UserObject
  public boolean removed() {
    return getGuiManager().treeComponentRemoved(fModelComponent);
  }

  @Override // UserObject
  public void rightClicked(JComponent component, int x, int y) {
    JPopupMenu popupMenu = getPopupMenu();
    if (popupMenu != null) {
      popupMenu.show(component, x, y);
    }
  }

  @Override // UserObject
  public void doubleClicked() {
  }

  @Override // UserObject
  public JPopupMenu getPopupMenu() {
    return new JPopupMenu();
  }

  @Override // UserObject
  public ImageIcon getIcon() {
    return null;
  }

  /**
   * Is called when multiple objects in a tree are selected.
   */
  public void selectMultipleObjects() {
    getGuiManager().addSelectedModelComponent(getModelComponent());
  }

  @Override
  public ModelComponent getParent() {
    return parent;
  }

  @Override
  public void setParent(ModelComponent parent) {
    this.parent = parent;
  }

  protected GuiManager getGuiManager() {
    return guiManager;
  }

  protected ModelManager getModelManager() {
    return modelManager;
  }
}
