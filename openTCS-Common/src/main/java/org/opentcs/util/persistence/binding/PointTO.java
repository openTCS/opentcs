/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.persistence.binding;

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
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = {"name", "id", "xPosition", "yPosition", "zPosition",
                      "vehicleOrientationAngle", "type", "outgoingPaths", "properties"})
public class PointTO
    extends PlantModelElementTO {
  
  private Long xPosition = 0L;
  private Long yPosition = 0L;
  private Long zPosition = 0L;
  private Float vehicleOrientationAngle = 0.0F;
  private String type = "";
  private List<OutgoingPath> outgoingPaths = new ArrayList<>();

  @XmlAttribute(required = true)
  public Long getxPosition() {
    return xPosition;
  }

  public PointTO setxPosition(@Nonnull Long xPosition) {
    requireNonNull(xPosition, "xPosition");
    this.xPosition = xPosition;
    return this;
  }

  @XmlAttribute(required = true)
  public Long getyPosition() {
    return yPosition;
  }

  public PointTO setyPosition(@Nonnull Long yPosition) {
    requireNonNull(yPosition, "yPosition");
    this.yPosition = yPosition;
    return this;
  }

  @XmlAttribute
  public Long getzPosition() {
    return zPosition;
  }

  public PointTO setzPosition(@Nonnull Long zPosition) {
    requireNonNull(zPosition, "zPosition");
    this.zPosition = zPosition;
    return this;
  }

  @XmlAttribute
  public Float getVehicleOrientationAngle() {
    return vehicleOrientationAngle;
  }

  public PointTO setVehicleOrientationAngle(@Nonnull Float vehicleOrientationAngle) {
    requireNonNull(vehicleOrientationAngle, "vehicleOrientationAngle");
    this.vehicleOrientationAngle = vehicleOrientationAngle;
    return this;
  }

  @XmlAttribute(required = true)
  public String getType() {
    return type;
  }

  public PointTO setType(@Nonnull String type) {
    requireNonNull(type, "type");
    this.type = type;
    return this;
  }

  @XmlElement(name = "outgoingPath")
  public List<OutgoingPath> getOutgoingPaths() {
    return outgoingPaths;
  }

  public PointTO setOutgoingPaths(@Nonnull List<OutgoingPath> outgoingPath) {
    requireNonNull(outgoingPath, "outgoingPath");
    this.outgoingPaths = outgoingPath;
    return this;
  }

  @XmlAccessorType(XmlAccessType.PROPERTY)
  public static class OutgoingPath {

    private String name = "";

    @XmlAttribute(required = true)
    public String getName() {
      return name;
    }

    public OutgoingPath setName(@Nonnull String name) {
      requireNonNull(name, "name");
      this.name = name;
      return this;
    }
  }
}
