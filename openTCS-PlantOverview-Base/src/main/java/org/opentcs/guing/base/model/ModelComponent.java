/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.base.model;

import java.util.List;
import java.util.Map;
import org.opentcs.guing.base.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.base.components.properties.type.Property;
import org.opentcs.guing.base.components.properties.type.StringProperty;

/**
 * Defines a component in the system model.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface ModelComponent
    extends Cloneable {

  /**
   * Key for the name property.
   */
  String NAME = "Name";
  /**
   * Key for the miscellaneous properties.
   */
  String MISCELLANEOUS = "Miscellaneous";

  /**
   * Adds a child component.
   *
   * @param component The model component to add.
   */
  void add(ModelComponent component);

  /**
   * Removes a child component.
   *
   * @param component The model component to remove.
   */
  void remove(ModelComponent component);

  /**
   * Returns all child components.
   *
   * @return A list of all child components.
   */
  List<ModelComponent> getChildComponents();

  /**
   * Retruns this model component's name that is displayed in the tree view.
   *
   * @return The name that is displayed in the tree view.
   */
  String getTreeViewName();

  /**
   * Returns whether the given component is a child of this model component.
   *
   * @param component The component.
   * @return {@code true}, if the given component is a child of this model component, otherwise
   * {@code false}.
   */
  boolean contains(ModelComponent component);

  /**
   * Returns this model component's parent component.
   *
   * @return The parent component.
   */
  ModelComponent getParent();

  /**
   * Returns the actual parent of this model component.
   * PropertiesCollection e.g. overwrites it. May be null.
   *
   * @return The actual parent.
   */
  ModelComponent getActualParent();

  /**
   * Set this model component's parent component.
   *
   * @param parent The new parent component.
   */
  void setParent(ModelComponent parent);

  /**
   * Returns whether this model component is to be shown in the tree view.
   *
   * @return {@code true}, if the model component is to be shown in the tree view, otherwise {@code false}.
   */
  boolean isTreeViewVisible();

  /**
   * Sets this model component's visibility in the tree view.
   *
   * @param visibility Whether the model component is to be shown in the tree view or not.
   */
  void setTreeViewVisibility(boolean visibility);

  /**
   * Returns a description for the model component.
   *
   * @return A description for the model component.
   */
  String getDescription();

  /**
   * Returns the name of the component.
   *
   * @return The name of the component.
   */
  String getName();

  /**
   * Sets this model component's name.
   *
   * @param name The new name.
   */
  void setName(String name);

  /**
   * Returns the property with the given name.
   *
   * @param name The name of the property.
   * @return The property with the given name.
   */
  Property getProperty(String name);

  /**
   * Returns all properties.
   *
   * @return A map containing all properties.
   */
  Map<String, Property> getProperties();

  /**
   * Sets the property with the given name.
   *
   * @param name The name of the property.
   * @param property The property.
   */
  void setProperty(String name, Property property);

  /**
   * Returns this component's name property.
   *
   * @return This component's name property.
   */
  default StringProperty getPropertyName() {
    return (StringProperty) getProperty(NAME);
  }

  /**
   * Adds the given {@link AttributesChangeListener}.
   * The {@link AttributesChangeListener} is notified when properties of the model component
   * have changed.
   *
   * @param l The listener.
   */
  void addAttributesChangeListener(AttributesChangeListener l);

  /**
   * Removes the given {@link AttributesChangeListener}.
   *
   * @param l The listener.
   */
  void removeAttributesChangeListener(AttributesChangeListener l);

  /**
   * Returns whether the given {@link AttributesChangeListener} is already registered with the model
   * component.
   *
   * @param l The listener.
   * @return {@code true}, if the given {@link AttributesChangeListener} is already registered with the model
   * component, otherwise {@code false}.
   */
  boolean containsAttributesChangeListener(AttributesChangeListener l);

  /**
   * Notifies all registered {@link AttributesChangeListener}s that properties of the model
   * component have changed.
   *
   * @param l The initiator of the change.
   */
  void propertiesChanged(AttributesChangeListener l);

  /**
   * Clones this ModelComponent.
   *
   * @return A clone of this ModelComponent.
   * @throws java.lang.CloneNotSupportedException If the model component doesn't implement the
   * {@link Cloneable} interface.
   */
  ModelComponent clone()
      throws CloneNotSupportedException;
}
