/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.persistence.v003;

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
@XmlType(propOrder = {"name", "sourcePoint", "destinationPoint", "length", "maxVelocity",
                      "maxReverseVelocity", "locked", "properties"})
public class PathTO
    extends PlantModelElementTO {

  private String sourcePoint = "";
  private String destinationPoint = "";
  private Long length = 0L;
  private Long maxVelocity = 0L;
  private Long maxReverseVelocity = 0L;
  private Boolean locked = false;

  @XmlAttribute(required = true)
  public String getSourcePoint() {
    return sourcePoint;
  }

  public PathTO setSourcePoint(@Nonnull String sourcePoint) {
    requireNonNull(sourcePoint, "sourcePoint");
    this.sourcePoint = sourcePoint;
    return this;
  }

  @XmlAttribute(required = true)
  public String getDestinationPoint() {
    return destinationPoint;
  }

  public PathTO setDestinationPoint(@Nonnull String destinationPoint) {
    requireNonNull(destinationPoint, "destinationPoint");
    this.destinationPoint = destinationPoint;
    return this;
  }

  @XmlAttribute
  @XmlSchemaType(name = "unsignedInt")
  public Long getLength() {
    return length;
  }

  public PathTO setLength(@Nonnull Long length) {
    requireNonNull(length, "length");
    this.length = length;
    return this;
  }

  @XmlAttribute(required = true)
  @XmlSchemaType(name = "unsignedInt")
  public Long getMaxVelocity() {
    return maxVelocity;
  }

  public PathTO setMaxVelocity(@Nonnull Long maxVelocity) {
    requireNonNull(maxVelocity, "maxVelocity");
    this.maxVelocity = maxVelocity;
    return this;
  }

  @XmlAttribute(required = true)
  @XmlSchemaType(name = "unsignedInt")
  public Long getMaxReverseVelocity() {
    return maxReverseVelocity;
  }

  public PathTO setMaxReverseVelocity(@Nonnull Long maxReverseVelocity) {
    requireNonNull(maxReverseVelocity, "maxReverseVelocity");
    this.maxReverseVelocity = maxReverseVelocity;
    return this;
  }

  @XmlAttribute(required = true)
  public Boolean isLocked() {
    return locked;
  }

  public PathTO setLocked(Boolean locked) {
    this.locked = locked;
    return this;
  }
}
