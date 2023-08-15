/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.persistence.v004;

import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = {"name",
                      "allowedOperations",
                      "allowedPeripheralOperations",
                      "properties",
                      "locationTypeLayout"})
public class LocationTypeTO
    extends PlantModelElementTO {

  private List<AllowedOperationTO> allowedOperations = new ArrayList<>();
  private List<AllowedPeripheralOperationTO> allowedPeripheralOperations = new ArrayList<>();
  private LocationTypeLayout locationTypeLayout = new LocationTypeLayout();

  /**
   * Creates a new instance.
   */
  public LocationTypeTO() {
  }

  @XmlElement(name = "allowedOperation")
  public List<AllowedOperationTO> getAllowedOperations() {
    return allowedOperations;
  }

  public LocationTypeTO setAllowedOperations(@Nonnull List<AllowedOperationTO> allowedOperations) {
    this.allowedOperations = requireNonNull(allowedOperations, "allowedOperations");
    return this;
  }

  @XmlElement(name = "allowedPeripheralOperation")
  public List<AllowedPeripheralOperationTO> getAllowedPeripheralOperations() {
    return allowedPeripheralOperations;
  }

  public LocationTypeTO setAllowedPeripheralOperations(
      List<AllowedPeripheralOperationTO> allowedPeripheralOperations) {
    this.allowedPeripheralOperations = requireNonNull(allowedPeripheralOperations,
                                                      "allowedPeripheralOperations");
    return this;
  }

  @XmlElement(required = true)
  public LocationTypeLayout getLocationTypeLayout() {
    return locationTypeLayout;
  }

  public LocationTypeTO setLocationTypeLayout(@Nonnull LocationTypeLayout locationTypeLayout) {
    this.locationTypeLayout = requireNonNull(locationTypeLayout, "locationTypeLayout");
    return this;
  }

  @XmlAccessorType(XmlAccessType.PROPERTY)
  public static class LocationTypeLayout {

    private String locationRepresentation = "";

    /**
     * Creates a new instance.
     */
    public LocationTypeLayout() {
    }

    @XmlAttribute(required = true)
    public String getLocationRepresentation() {
      return locationRepresentation;
    }

    public LocationTypeLayout setLocationRepresentation(@Nonnull String locationRepresentation) {
      this.locationRepresentation = requireNonNull(locationRepresentation,
                                                   "locationRepresentation");
      return this;
    }
  }
}
