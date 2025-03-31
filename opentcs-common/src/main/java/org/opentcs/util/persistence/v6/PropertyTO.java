// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.persistence.v6;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = {"name", "value"})
public class PropertyTO {

  private String name = "";
  private String value = "";

  /**
   * Creates a new instance.
   */
  public PropertyTO() {
  }

  @XmlAttribute(required = true)
  public String getName() {
    return name;
  }

  public PropertyTO setName(
      @Nonnull
      String name
  ) {
    this.name = requireNonNull(name, "name");
    return this;
  }

  @XmlAttribute(required = true)
  public String getValue() {
    return value;
  }

  public PropertyTO setValue(
      @Nonnull
      String value
  ) {
    requireNonNull(value, "value");
    this.value = value;
    return this;
  }
}
