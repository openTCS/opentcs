/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.configuration;

import java.io.Serializable;
import java.util.Objects;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A single configuration item.
 * <p>
 * A configuration item consists of the following data:
 * </p>
 * <ul>
 * <li>A namespace the configuration item belongs to.</li>
 * <li>A key uniquely identifying the configuration item within the namespace it
 * belongs to.</li>
 * <li>A value, representing the actual configuration data.</li>
 * <li>A constraint for the value.</li>
 * <li>An optional human-readable description explaining what can be configured
 * with this configuration item.</li>
 * </ul>
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated Use interface bindings and configuration mechanism provided via applications'
 * dependency injection.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed.")
public class ConfigurationItem
    implements Serializable {

  /**
   * The namespace this configuration item belongs to.
   */
  private final String namespace;
  /**
   * The key/name/label of this configuration item.
   */
  private final String key;
  /**
   * A comment describing the function of this configuration item.
   */
  private String description;
  /**
   * The actual value of this configuration item.
   */
  private String value;
  /**
   * A constraint containing the data type of an Item.
   */
  private ItemConstraint constraint;

  /**
   * Creates a new ConfigurationItem.
   * 
   * @param namespace The namespace this configuration item belongs to.
   * @param key The key/name/label of this configuration item.
   * @param description A comment describing the function of this configuration
   * item.
   * @param constraint A constraint containing the data type of an Item.
   * @param value The value of this configuration item.
   */
  public ConfigurationItem(final String namespace,
                           final String key,
                           final String description,
                           final ItemConstraint constraint,
                           final String value) {
    this.namespace = Objects.requireNonNull(namespace, "namespace");
    this.key = Objects.requireNonNull(key, "key");
    this.description = description == null ? "" : description;
    this.constraint = Objects.requireNonNull(constraint, "constraint");
    this.value = value == null ? "" : value;

  }

  /**
   * Returns this configuration item's description.
   *
   * @return This configuration item's description.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets this configuration item's description.
   *
   * @param newDescription The new description. If <code>null</code>, is
   * implicitly changed to the empty string.
   */
  public void setDescription(String newDescription) {
    description = newDescription == null ? "" : newDescription;
  }

  /**
   * Returns this configuration item's key.
   *
   * @return This configuration item's key.
   */
  public String getKey() {
    return key;
  }

  /**
   * Returns the namespace this configuration item belongs to.
   *
   * @return The namespace this configuration item belongs to.
   */
  public String getNamespace() {
    return namespace;
  }

  /**
   * Returns this configuration item's current value.
   *
   * @return This configuration item's current value.
   */
  public String getValue() {
    return value;
  }

  /**
   * Sets this configuration item's value.
   *
   * @param newValue The new value. If <code>null</code>, is implicitly changed
   * to the empty string.
   * @throws IllegalArgumentException if the enetered value doesn't
   * match the constraints .
   */
  public void setValue(String newValue) throws IllegalArgumentException {

    if (!(constraint.accepts(newValue))) {
      throw new IllegalArgumentException("Enetered Value does not "
          + "match the constraints");
    }
    value = newValue == null ? "" : newValue;
  }

  /**
   * Returns this configuration item's constraints.
   *
   * @return This configuration item's constraints.
   */
  public ItemConstraint getConstraint() {
    return constraint;
  }

  /**
   * Sets this configuration item's constraint.
   *
   * @param constraint A constraint containing the data type of an Item.
   */
  public void setConstraint(ItemConstraint constraint) {
    this.constraint = Objects.requireNonNull(constraint, "newType is null");
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ConfigurationItem) {
      ConfigurationItem other = (ConfigurationItem) obj;
      return namespace.equals(other.namespace)
          && key.equals(other.key)
          && constraint.equals(other.constraint)
          && value.equals(other.value);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 79 * hash + (this.namespace != null ? this.namespace.hashCode() : 0);
    hash = 79 * hash + (this.key != null ? this.key.hashCode() : 0);
    hash = 79 * hash + (this.constraint != null ? this.constraint.hashCode() : 0);
    hash = 79 * hash + (this.value != null ? this.value.hashCode() : 0);
    return hash;
  }
}
