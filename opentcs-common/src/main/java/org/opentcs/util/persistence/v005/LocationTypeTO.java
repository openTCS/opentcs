// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.persistence.v005;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(
    propOrder = {"name",
        "allowedOperations",
        "allowedPeripheralOperations",
        "properties",
        "locationTypeLayout"}
)
public class LocationTypeTO
    extends
      PlantModelElementTO {

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

  public LocationTypeTO setAllowedOperations(
      @Nonnull
      List<AllowedOperationTO> allowedOperations
  ) {
    this.allowedOperations = requireNonNull(allowedOperations, "allowedOperations");
    return this;
  }

  @XmlElement(name = "allowedPeripheralOperation")
  public List<AllowedPeripheralOperationTO> getAllowedPeripheralOperations() {
    return allowedPeripheralOperations;
  }

  public LocationTypeTO setAllowedPeripheralOperations(
      List<AllowedPeripheralOperationTO> allowedPeripheralOperations
  ) {
    this.allowedPeripheralOperations = requireNonNull(
        allowedPeripheralOperations,
        "allowedPeripheralOperations"
    );
    return this;
  }

  @XmlElement(required = true)
  public LocationTypeLayout getLocationTypeLayout() {
    return locationTypeLayout;
  }

  public LocationTypeTO setLocationTypeLayout(
      @Nonnull
      LocationTypeLayout locationTypeLayout
  ) {
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

    public LocationTypeLayout setLocationRepresentation(
        @Nonnull
        String locationRepresentation
    ) {
      this.locationRepresentation = requireNonNull(
          locationRepresentation,
          "locationRepresentation"
      );
      return this;
    }
  }
}
