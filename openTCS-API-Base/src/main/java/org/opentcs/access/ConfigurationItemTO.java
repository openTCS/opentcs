/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access;

import java.io.Serializable;
import java.util.Objects;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A transfer object for configuration data.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated Use interface bindings and configuration mechanism provided via applications'
 * dependency injection.
 */
@Deprecated
@ScheduledApiChange(when = "5.0", details = "Will be removed.")
public class ConfigurationItemTO
    implements Serializable {

  /**
   * The configuration item's namespace.
   */
  private String namespace = "";
  /**
   * The configuration item's key.
   */
  private String key = "";
  /**
   * The configuration item's description.
   */
  private String description = "";
  /**
   * The configuration item's current value.
   */
  private String value = "";
  /**
   * A constraint containing the data type of an Item.
   */
  private org.opentcs.util.configuration.ItemConstraint constraint;

  /**
   * Creates a new ConfigurationItemTO.
   */
  public ConfigurationItemTO() {
    // Do nada.
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
   * @param description The new description.
   */
  public void setDescription(String description) {
    this.description = Objects.requireNonNull(description,
                                              "description is null");
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
   * Sets this configuration item's key.
   *
   * @param key The new key.
   */
  public void setKey(String key) {
    this.key = Objects.requireNonNull(key, "key is null");
  }

  /**
   * Returns this configuration item's namespace.
   *
   * @return This configuration item's namespace.
   */
  public String getNamespace() {
    return namespace;
  }

  /**
   * Sets this configuration item's namespace.
   *
   * @param namespace The new namespace.
   */
  public void setNamespace(String namespace) {
    this.namespace = Objects.requireNonNull(namespace, "namespace is null");
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
   * @param value The new value.
   */
  public void setValue(String value) {
    this.value = Objects.requireNonNull(value, "value is null");
  }

  /**
   * Returns this configuration item's constraint.
   *
   * @return A constraint containing the data type of an Item.
   */
  public org.opentcs.util.configuration.ItemConstraint getConstraint() {
    return constraint;
  }

  /**
   * Sets this configuration item's constraint.
   *
   * @param newConstraint The new data constraint.
   */
  public void setConstraint(org.opentcs.util.configuration.ItemConstraint newConstraint) {
    this.constraint = Objects.requireNonNull(newConstraint, "newType is null");
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ConfigurationItemTO) {
      ConfigurationItemTO other = (ConfigurationItemTO) obj;
      return namespace.equals(other.namespace) && key.equals(other.key);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 97 * hash + Objects.hashCode(this.namespace);
    hash = 97 * hash + Objects.hashCode(this.key);
    return hash;
  }

  @Override
  public String toString() {
    return getNamespace() + "." + getKey();
  }
}
