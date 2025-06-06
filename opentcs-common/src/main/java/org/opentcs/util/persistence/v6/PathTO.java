// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.persistence.v6;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(
    propOrder = {"name",
        "sourcePoint",
        "destinationPoint",
        "length",
        "maxVelocity",
        "maxReverseVelocity",
        "peripheralOperations",
        "locked",
        "vehicleEnvelopes",
        "properties",
        "pathLayout"}
)
public class PathTO
    extends
      PlantModelElementTO {

  private String sourcePoint = "";
  private String destinationPoint = "";
  private Long length = 0L;
  private Long maxVelocity = 0L;
  private Long maxReverseVelocity = 0L;
  private List<PeripheralOperationTO> peripheralOperations = new ArrayList<>();
  private Boolean locked = false;
  private List<VehicleEnvelopeTO> vehicleEnvelopes = new ArrayList<>();
  private PathLayout pathLayout = new PathLayout();

  /**
   * Creates a new instance.
   */
  public PathTO() {
  }

  @XmlAttribute(required = true)
  public String getSourcePoint() {
    return sourcePoint;
  }

  public PathTO setSourcePoint(
      @Nonnull
      String sourcePoint
  ) {
    requireNonNull(sourcePoint, "sourcePoint");
    this.sourcePoint = sourcePoint;
    return this;
  }

  @XmlAttribute(required = true)
  public String getDestinationPoint() {
    return destinationPoint;
  }

  public PathTO setDestinationPoint(
      @Nonnull
      String destinationPoint
  ) {
    requireNonNull(destinationPoint, "destinationPoint");
    this.destinationPoint = destinationPoint;
    return this;
  }

  @XmlAttribute(required = true)
  @XmlSchemaType(name = "unsignedInt")
  public Long getLength() {
    return length;
  }

  public PathTO setLength(
      @Nonnull
      Long length
  ) {
    requireNonNull(length, "length");
    this.length = length;
    return this;
  }

  @XmlAttribute(required = true)
  @XmlSchemaType(name = "unsignedInt")
  public Long getMaxVelocity() {
    return maxVelocity;
  }

  public PathTO setMaxVelocity(
      @Nonnull
      Long maxVelocity
  ) {
    requireNonNull(maxVelocity, "maxVelocity");
    this.maxVelocity = maxVelocity;
    return this;
  }

  @XmlAttribute(required = true)
  @XmlSchemaType(name = "unsignedInt")
  public Long getMaxReverseVelocity() {
    return maxReverseVelocity;
  }

  public PathTO setMaxReverseVelocity(
      @Nonnull
      Long maxReverseVelocity
  ) {
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

  @XmlElement(name = "vehicleEnvelope")
  public List<VehicleEnvelopeTO> getVehicleEnvelopes() {
    return vehicleEnvelopes;
  }

  public PathTO setVehicleEnvelopes(
      @Nonnull
      List<VehicleEnvelopeTO> vehicleEnvelopes
  ) {
    this.vehicleEnvelopes = requireNonNull(vehicleEnvelopes, "vehicleEnvelopes");
    return this;
  }

  @XmlElement(required = true)
  public PathLayout getPathLayout() {
    return pathLayout;
  }

  public PathTO setPathLayout(
      @Nonnull
      PathLayout pathLayout
  ) {
    this.pathLayout = requireNonNull(pathLayout, "pathLayout");
    return this;
  }

  @XmlAccessorType(XmlAccessType.PROPERTY)
  @XmlType(propOrder = {"connectionType", "layerId", "controlPoints"})
  public static class PathLayout {

    private ConnectionType connectionType = ConnectionType.DIRECT;
    private Integer layerId = 0;
    private List<ControlPoint> controlPoints = new ArrayList<>();

    /**
     * Creates a new instance.
     */
    public PathLayout() {
    }

    @XmlAttribute(required = true)
    public ConnectionType getConnectionType() {
      return connectionType;
    }

    public PathLayout setConnectionType(
        @Nonnull
        ConnectionType connectionType
    ) {
      this.connectionType = requireNonNull(connectionType, "connectionType");
      return this;
    }

    @XmlAttribute(required = true)
    public Integer getLayerId() {
      return layerId;
    }

    public PathLayout setLayerId(
        @Nonnull
        Integer layerId
    ) {
      this.layerId = requireNonNull(layerId, "layerId");
      return this;
    }

    @XmlElement(name = "controlPoint")
    public List<ControlPoint> getControlPoints() {
      return controlPoints;
    }

    public PathLayout setControlPoints(
        @Nonnull
        List<ControlPoint> controlPoints
    ) {
      this.controlPoints = requireNonNull(controlPoints, "controlPoints");
      return this;
    }

    @XmlType
    public enum ConnectionType {
      // CHECKSTYLE:OFF
      DIRECT,
      ELBOW,
      SLANTED,
      POLYPATH,
      BEZIER,
      BEZIER_3;
      // CHECKSTYLE:ON
    }
  }

  @XmlAccessorType(XmlAccessType.PROPERTY)
  @XmlType(propOrder = {"x", "y"})
  public static class ControlPoint {

    private Long x = 0L;
    private Long y = 0L;

    /**
     * Creates a new instance.
     */
    public ControlPoint() {
    }

    @XmlAttribute(required = true)
    public Long getX() {
      return x;
    }

    public ControlPoint setX(
        @Nonnull
        Long x
    ) {
      this.x = requireNonNull(x, "x");
      return this;
    }

    @XmlAttribute(required = true)
    public Long getY() {
      return y;
    }

    public ControlPoint setY(
        @Nonnull
        Long y
    ) {
      this.y = requireNonNull(y, "y");
      return this;
    }
  }
}
