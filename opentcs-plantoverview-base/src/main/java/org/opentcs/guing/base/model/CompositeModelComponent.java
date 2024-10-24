// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.base.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract implementation of a composite model component that holds a set of child components.
 */
public abstract class CompositeModelComponent
    extends
      AbstractModelComponent {

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
  public boolean contains(ModelComponent component) {
    return getChildComponents().contains(component);
  }

  @Override
  public CompositeModelComponent clone()
      throws CloneNotSupportedException {
    CompositeModelComponent clone = (CompositeModelComponent) super.clone();
    clone.fChildComponents = new ArrayList<>(fChildComponents);
    return clone;
  }
}
