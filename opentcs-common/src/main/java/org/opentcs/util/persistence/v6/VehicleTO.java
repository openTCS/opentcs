// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.persistence.v6;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(
    propOrder = {"name", "energyLevelCritical", "energyLevelGood", "energyLevelFullyRecharged",
        "energyLevelSufficientlyRecharged", "maxVelocity", "maxReverseVelocity", "boundingBox",
        "properties", "vehicleLayout"}
)
public class VehicleTO
    extends
      PlantModelElementTO {

  //max velocity in mm/s.
  private int maxVelocity;
  //max rev velocity in mm/s.
  private int maxReverseVelocity;
  private Long energyLevelCritical = 0L;
  private Long energyLevelGood = 0L;
  private Long energyLevelFullyRecharged = 0L;
  private Long energyLevelSufficientlyRecharged = 0L;
  private String envelopeKey;
  private BoundingBoxTO boundingBox = new BoundingBoxTO();
  private VehicleLayout vehicleLayout = new VehicleLayout();

  /**
   * Creates a new instance.
   */
  public VehicleTO() {
  }

  @XmlAttribute(required = true)
  @XmlSchemaType(name = "unsignedInt")
  public Long getEnergyLevelCritical() {
    return energyLevelCritical;
  }

  public VehicleTO setEnergyLevelCritical(
      @Nonnull
      Long energyLevelCritical
  ) {
    requireNonNull(energyLevelCritical, "energyLevelCritical");
    this.energyLevelCritical = energyLevelCritical;
    return this;
  }

  @XmlAttribute(required = true)
  @XmlSchemaType(name = "unsignedInt")
  public Long getEnergyLevelGood() {
    return energyLevelGood;
  }

  public VehicleTO setEnergyLevelGood(
      @Nonnull
      Long energyLevelGood
  ) {
    requireNonNull(energyLevelGood, "energyLevelGood");
    this.energyLevelGood = energyLevelGood;
    return this;
  }

  @XmlAttribute(required = true)
  @XmlSchemaType(name = "unsignedInt")
  public Long getEnergyLevelFullyRecharged() {
    return energyLevelFullyRecharged;
  }

  public VehicleTO setEnergyLevelFullyRecharged(
      @Nonnull
      Long energyLevelFullyRecharged
  ) {
    requireNonNull(energyLevelFullyRecharged, "energyLevelFullyRecharged");
    this.energyLevelFullyRecharged = energyLevelFullyRecharged;
    return this;
  }

  @XmlAttribute(required = true)
  @XmlSchemaType(name = "unsignedInt")
  public Long getEnergyLevelSufficientlyRecharged() {
    return energyLevelSufficientlyRecharged;
  }

  public VehicleTO setEnergyLevelSufficientlyRecharged(
      @Nonnull
      Long energyLevelSufficientlyRecharged
  ) {
    requireNonNull(energyLevelSufficientlyRecharged, "energyLevelSufficientlyRecharged");
    this.energyLevelSufficientlyRecharged = energyLevelSufficientlyRecharged;
    return this;
  }

  @XmlAttribute(required = true)
  @XmlSchemaType(name = "unsignedInt")
  public int getMaxVelocity() {
    return maxVelocity;
  }

  public VehicleTO setMaxVelocity(
      @Nonnull
      int maxVelocity
  ) {
    this.maxVelocity = maxVelocity;
    return this;
  }

  @XmlAttribute(required = true)
  @XmlSchemaType(name = "unsignedInt")
  public int getMaxReverseVelocity() {
    return maxReverseVelocity;
  }

  public VehicleTO setMaxReverseVelocity(
      @Nonnull
      int maxReverseVelocity
  ) {
    this.maxReverseVelocity = maxReverseVelocity;
    return this;
  }

  @ScheduledApiChange(when = "7.0", details = "Envelope key will become non-null.")
  @XmlAttribute
  @Nullable
  public String getEnvelopeKey() {
    return envelopeKey;
  }

  @ScheduledApiChange(when = "7.0", details = "Envelope key will become non-null.")
  public VehicleTO setEnvelopeKey(
      @Nullable
      String envelopeKey
  ) {
    this.envelopeKey = envelopeKey;
    return this;
  }

  @XmlElement
  @Nonnull
  public BoundingBoxTO getBoundingBox() {
    return boundingBox;
  }

  public VehicleTO setBoundingBox(
      @Nonnull
      BoundingBoxTO boundingBox
  ) {
    this.boundingBox = requireNonNull(boundingBox, "boundingBox");
    return this;
  }

  @XmlElement(required = true)
  public VehicleLayout getVehicleLayout() {
    return vehicleLayout;
  }

  public VehicleTO setVehicleLayout(
      @Nonnull
      VehicleLayout vehicleLayout
  ) {
    this.vehicleLayout = requireNonNull(vehicleLayout, "vehicleLayout");
    return this;
  }

  @XmlAccessorType(XmlAccessType.PROPERTY)
  public static class VehicleLayout {

    private String color = "";

    /**
     * Creates a new instance.
     */
    public VehicleLayout() {
    }

    @XmlAttribute(required = true)
    public String getColor() {
      return color;
    }

    public VehicleLayout setColor(
        @Nonnull
        String color
    ) {
      this.color = requireNonNull(color, "color");
      return this;
    }
  }
}
