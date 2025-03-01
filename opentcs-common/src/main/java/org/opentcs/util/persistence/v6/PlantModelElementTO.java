// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.persistence.v6;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 */
@XmlTransient
@XmlAccessorType(XmlAccessType.PROPERTY)
public class PlantModelElementTO {

  private String name = "";
  private List<PropertyTO> properties = new ArrayList<>();

  /**
   * Creates a new instance.
   */
  public PlantModelElementTO() {
  }

  @XmlAttribute(required = true)
  public String getName() {
    return name;
  }

  public PlantModelElementTO setName(
      @Nonnull
      String name
  ) {
    requireNonNull(name, "name");
    this.name = name;
    return this;
  }

  @XmlElement(name = "property")
  public List<PropertyTO> getProperties() {
    return properties;
  }

  public PlantModelElementTO setProperties(
      @Nonnull
      List<PropertyTO> properties
  ) {
    requireNonNull(properties, "properties");
    this.properties = properties;
    return this;
  }
}
