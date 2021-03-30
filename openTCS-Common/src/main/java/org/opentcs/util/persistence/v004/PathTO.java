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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = {"name",
                      "sourcePoint",
                      "destinationPoint",
                      "length",
                      "maxVelocity",
                      "maxReverseVelocity",
                      "peripheralOperations",
                      "locked",
                      "properties",
                      "pathLayout"})
public class PathTO
    extends PlantModelElementTO {

  private String sourcePoint = "";
  private String destinationPoint = "";
  private Long length = 0L;
  private Long maxVelocity = 0L;
  private Long maxReverseVelocity = 0L;
  private List<PeripheralOperationTO> peripheralOperations = new ArrayList<>();
  private Boolean locked = false;
  private PathLayout pathLayout = new PathLayout();

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

  @XmlElement(name = "peripheralOperation")
  public List<PeripheralOperationTO> getPeripheralOperations() {
    return peripheralOperations;
  }

  public PathTO setPeripheralOperations(List<PeripheralOperationTO> peripheralOperations) {
    this.peripheralOperations = requireNonNull(peripheralOperations, "peripheralOperations");
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

  @XmlElement(required = true)
  public PathLayout getPathLayout() {
    return pathLayout;
  }

  public PathTO setPathLayout(@Nonnull PathLayout pathLayout) {
    this.pathLayout = requireNonNull(pathLayout, "pathLayout");
    return this;
  }

  @XmlAccessorType(XmlAccessType.PROPERTY)
  @XmlType(propOrder = {"connectionType", "layerId", "controlPoints"})
  public static class PathLayout {

    private String connectionType = "";
    private Integer layerId = 0;
    private List<ControlPoint> controlPoints = new ArrayList<>();

    @XmlAttribute(required = true)
    public String getConnectionType() {
      return connectionType;
    }

    public PathLayout setConnectionType(@Nonnull String connectionType) {
      this.connectionType = requireNonNull(connectionType, "connectionType");
      return this;
    }

    @XmlAttribute(required = true)
    public Integer getLayerId() {
      return layerId;
    }

    public PathLayout setLayerId(@Nonnull Integer layerId) {
      this.layerId = requireNonNull(layerId, "layerId");
      return this;
    }

    @XmlElement(name = "controlPoint")
    public List<ControlPoint> getControlPoints() {
      return controlPoints;
    }

    public PathLayout setControlPoints(@Nonnull List<ControlPoint> controlPoints) {
      this.controlPoints = requireNonNull(controlPoints, "controlPoints");
      return this;
    }
  }

  @XmlAccessorType(XmlAccessType.PROPERTY)
  @XmlType(propOrder = {"x", "y"})
  public static class ControlPoint {

    private Long x = 0L;
    private Long y = 0L;

    @XmlAttribute(required = true)
    public Long getX() {
      return x;
    }

    public ControlPoint setX(@Nonnull Long x) {
      this.x = requireNonNull(x, "x");
      return this;
    }

    @XmlAttribute(required = true)
    public Long getY() {
      return y;
    }

    public ControlPoint setY(@Nonnull Long y) {
      this.y = requireNonNull(y, "y");
      return this;
    }
  }
}
