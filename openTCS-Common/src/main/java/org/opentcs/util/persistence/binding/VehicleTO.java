/**
 * Copyright (c) 2017 Fraunhofer IML
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
@XmlType(propOrder = {"name", "id", "length", "energyLevelCritical", "energyLevelGood",
                      "properties"})
public class VehicleTO
    extends PlantModelElementTO {

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
}
