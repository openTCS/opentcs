/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.persistence.binding;

import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(
      propOrder = {"name", "id", "length", "energyLevelCritical", "energyLevelGood", "maxVelocity", "maxReverseVelocity",
                   "properties"})
public class VehicleTO
    extends PlantModelElementTO {

  //max velocity in mm/s.
  private int maxVelocity;
  //max rev velocity in mm/s.
  private int maxReverseVelocity;
  private String type = "";
  private Long length = 0L;
  private Long energyLevelCritical = 0L;
  private Long energyLevelGood = 0L;

  @XmlAttribute
  public String getType() {
    return type;
  }

  public VehicleTO setType(@Nonnull String type) {
    requireNonNull(type, "type");
    this.type = type;
    return this;
  }

  @XmlAttribute
  @XmlSchemaType(name = "unsignedInt")
  public Long getLength() {
    return length;
  }

  public VehicleTO setLength(@Nonnull Long length) {
    requireNonNull(length, "length");
    this.length = length;
    return this;
  }

  @XmlAttribute
  @XmlSchemaType(name = "unsignedInt")
  public Long getEnergyLevelCritical() {
    return energyLevelCritical;
  }

  public VehicleTO setEnergyLevelCritical(@Nonnull Long energyLevelCritical) {
    requireNonNull(energyLevelCritical, "energyLevelCritical");
    this.energyLevelCritical = energyLevelCritical;
    return this;
  }

  @XmlAttribute
  @XmlSchemaType(name = "unsignedInt")
  public Long getEnergyLevelGood() {
    return energyLevelGood;
  }

  public VehicleTO setEnergyLevelGood(@Nonnull Long energyLevelGood) {
    requireNonNull(energyLevelGood, "energyLevelGood");
    this.energyLevelGood = energyLevelGood;
    return this;
  }

  @XmlAttribute
  @XmlSchemaType(name = "unsignedInt")
  public int getMaxVelocity() {
    return maxVelocity;
  }

  public VehicleTO setMaxVelocity(@Nonnull int maxVelocity) {
    this.maxVelocity = maxVelocity;
    return this;
  }

  @XmlAttribute
  @XmlSchemaType(name = "unsignedInt")
  public int getMaxReverseVelocity() {
    return maxReverseVelocity;
  }

  public VehicleTO setMaxReverseVelocity(@Nonnull int maxReverseVelocity) {
    this.maxReverseVelocity = maxReverseVelocity;
    return this;
  }
}
