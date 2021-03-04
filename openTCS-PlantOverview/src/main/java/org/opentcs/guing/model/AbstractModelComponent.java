/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.concurrent.CopyOnWriteArrayList;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.components.properties.type.Property;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.tree.TreeViewManager;

/**
 * Standardimplementierung für eine Blatt-Komponente des Systemmodells.
 * <p>
 * <b>Entwurfsmuster:</b> Kompositum. AbstractModelComponent ist die Komponente.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class AbstractModelComponent
    implements ModelComponent {

  /**
   * Der Name der Komponente, wie er im TreeView erscheint.
   */
  private final String fTreeViewName;
  /**
   * Ob die Komponente im TreeView angezeigt werden soll.
   */
  private boolean fTreeViewVisibility = true;
  /**
   * Die direkte Elternkomponente.
   */
  private transient ModelComponent fParent;
  /**
   * Die Objekte, die an Änderungen der Attribute interessiert sind.
   */
  private transient List<AttributesChangeListener> fAttributesChangeListeners
      = new CopyOnWriteArrayList<>();
  /**
   * The actual parent of this component. PropertiesCollection e.g.
   * overwrites it.
   */
  private ModelComponent actualParent;
  /**
   * The component's attributes.
   */
  private Map<String, Property> fProperties = new LinkedHashMap<>();

  /**
   * Creates a new instance.
   */
  public AbstractModelComponent() {
    this("");
  }

  /**
   * Creates a new instance with the given name.
   *
   * @param treeViewName The name.
   */
  public AbstractModelComponent(String treeViewName) {
    fTreeViewName = requireNonNull(treeViewName, "treeViewName");
  }

  @Override
  public void treeRestore(ModelComponent parent, TreeViewManager treeViewManager) {
    treeViewManager.addItem(parent, this);
  }

  @Override
  public void add(ModelComponent component) {
  }

  @Override
  public void remove(ModelComponent component) {
  }

  @Override
  public List<ModelComponent> getChildComponents() {
    return new ArrayList<>();
  }

  @Override
  public String getTreeViewName() {
    return fTreeViewName;
  }

  @Override
  public boolean contains(ModelComponent component) {
    return false;
  }

  @Override
  public ModelComponent getParent() {
    return fParent;
  }

  @Override
  public ModelComponent getActualParent() {
    return actualParent;
  }

  @Override
  public void setParent(ModelComponent parent) {
    if (parent instanceof PropertiesCollection) {
      actualParent = fParent;
    }
    fParent = parent;
  }

  @Override
  public boolean isTreeViewVisible() {
    return fTreeViewVisibility;
  }

  @Override
  public final void setTreeViewVisibility(boolean visibility) {
    fTreeViewVisibility = visibility;
  }

  @Override // ModelComponent
  public String getDescription() {
    return "";
  }

  @Override
  public String getName() {
    StringProperty property = getPropertyName();
    return property == null ? "" : property.getText();
  }

  @Override
  public void setName(String name) {
    getPropertyName().setText(name);
  }

  @Override
  public Property getProperty(String name) {
    return fProperties.get(name);
  }

  @Override
  public Map<String, Property> getProperties() {
    return fProperties;
  }

  @Override
  public void setProperty(String name, Property property) {
    fProperties.put(name, property);
  }

  @Override
  public void addAttributesChangeListener(AttributesChangeListener listener) {
    if (fAttributesChangeListeners == null) {
      fAttributesChangeListeners = new CopyOnWriteArrayList<>();
    }

    if (!fAttributesChangeListeners.contains(listener)) {
      fAttributesChangeListeners.add(listener);
    }
  }

  @Override
  public void removeAttributesChangeListener(AttributesChangeListener listener) {
    if (fAttributesChangeListeners != null) {
      fAttributesChangeListeners.remove(listener);
    }
  }

  @Override
  public boolean containsAttributesChangeListener(AttributesChangeListener listener) {
    if (fAttributesChangeListeners == null) {
      return false;
    }

    return fAttributesChangeListeners.contains(listener);
  }

  @Override
  public void propertiesChanged(AttributesChangeListener initiator) {
    if (fAttributesChangeListeners != null) {
      for (AttributesChangeListener listener : fAttributesChangeListeners) {
        if (initiator != listener) {
          listener.propertiesChanged(new AttributesChangeEvent(initiator, this));
        }
      }
    }
    unmarkAllPropertyChanges();
  }

  @Override
  public AbstractModelComponent clone()
      throws CloneNotSupportedException {
    AbstractModelComponent clonedModelComponent = (AbstractModelComponent) super.clone();
    clonedModelComponent.fAttributesChangeListeners = new CopyOnWriteArrayList<>();
    // "Shallow" copy of the Map
    clonedModelComponent.fProperties = new LinkedHashMap<>();
    // "Deep" copy: clone all properties
    for (Map.Entry<String, Property> entry : getProperties().entrySet()) {
      Property clonedProperty = (Property) entry.getValue().clone();
      // XXX Don't clone the name but create a new, unique one here!
      if (entry.getKey().equals(NAME)) {
        ((StringProperty) clonedProperty).setText("");
      }

      clonedProperty.setModel(clonedModelComponent);
      clonedModelComponent.setProperty(entry.getKey(), clonedProperty);
    }

    return clonedModelComponent;
  }

  private void unmarkAllPropertyChanges() {
    for (Property property : fProperties.values()) {
      property.unmarkChanged();
    }
  }
}
