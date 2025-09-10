// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.persistence.v7;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(
    propOrder = {"name", "positionX", "positionY", "positionZ", "vehicleOrientationAngle",
        "type", "maxVehicleBoundingBox", "vehicleEnvelopes", "outgoingPaths", "properties",
        "pointLayout"}
)
public class PointTO
    extends
      PlantModelElementTO {

  private Long positionX = 0L;
  private Long positionY = 0L;
  private Long positionZ = 0L;
  private Float vehicleOrientationAngle = 0.0F;
  private Type type = Type.HALT_POSITION;
  private BoundingBoxTO maxVehicleBoundingBox = new BoundingBoxTO();
  private List<VehicleEnvelopeTO> vehicleEnvelopes = new ArrayList<>();
  private List<OutgoingPath> outgoingPaths = new ArrayList<>();
  private PointLayout pointLayout = new PointLayout();

  /**
   * Creates a new instance.
   */
  public PointTO() {
  }

  @XmlAttribute(required = true)
  public Long getPositionX() {
    return positionX;
  }

  public PointTO setPositionX(
      @Nonnull
      Long positionX
  ) {
    requireNonNull(positionX, "positionX");
    this.positionX = positionX;
    return this;
  }

  @XmlAttribute(required = true)
  public Long getPositionY() {
    return positionY;
  }

  public PointTO setPositionY(
      @Nonnull
      Long positionY
  ) {
    requireNonNull(positionY, "positionY");
    this.positionY = positionY;
    return this;
  }

  @XmlAttribute(required = true)
  public Long getPositionZ() {
    return positionZ;
  }

  public PointTO setPositionZ(
      @Nonnull
      Long positionZ
  ) {
    requireNonNull(positionZ, "positionZ");
    this.positionZ = positionZ;
    return this;
  }

  @XmlAttribute
  public Float getVehicleOrientationAngle() {
    return vehicleOrientationAngle;
  }

  public PointTO setVehicleOrientationAngle(
      @Nonnull
      Float vehicleOrientationAngle
  ) {
    requireNonNull(vehicleOrientationAngle, "vehicleOrientationAngle");
    this.vehicleOrientationAngle = vehicleOrientationAngle;
    return this;
  }

  @XmlAttribute(required = true)
  public Type getType() {
    return type;
  }

  public PointTO setType(
      @Nonnull
      Type type
  ) {
    requireNonNull(type, "type");
    this.type = type;
    return this;
  }

  @XmlElement
  @Nonnull
  public BoundingBoxTO getMaxVehicleBoundingBox() {
    return maxVehicleBoundingBox;
  }

  public PointTO setMaxVehicleBoundingBox(
      @Nonnull
      BoundingBoxTO maxVehicleBoundingBox
  ) {
    this.maxVehicleBoundingBox = requireNonNull(maxVehicleBoundingBox, "maxVehicleBoundingBox");
    return this;
  }

  @XmlElement(name = "outgoingPath")
  public List<OutgoingPath> getOutgoingPaths() {
    return outgoingPaths;
  }

  public PointTO setOutgoingPaths(
      @Nonnull
      List<OutgoingPath> outgoingPath
  ) {
    requireNonNull(outgoingPath, "outgoingPath");
    this.outgoingPaths = outgoingPath;
    return this;
  }

  @XmlElement(name = "vehicleEnvelope")
  public List<VehicleEnvelopeTO> getVehicleEnvelopes() {
    return vehicleEnvelopes;
  }

  public PointTO setVehicleEnvelopes(
      @Nonnull
      List<VehicleEnvelopeTO> vehicleEnvelopes
  ) {
    this.vehicleEnvelopes = requireNonNull(vehicleEnvelopes, "vehicleEnvelopes");
    return this;
  }

  @XmlElement(required = true)
  public PointLayout getPointLayout() {
    return pointLayout;
  }

  public PointTO setPointLayout(
      @Nonnull
      PointLayout pointLayout
  ) {
    this.pointLayout = requireNonNull(pointLayout, "pointLayout");
    return this;
  }

  @XmlType(name = "Type")
  public enum Type {
    // CHECKSTYLE:OFF
    HALT_POSITION,
    PARK_POSITION;
    // CHECKSTYLE:ON
  }

  @XmlAccessorType(XmlAccessType.PROPERTY)
  public static class OutgoingPath {

    private String name = "";

    /**
     * Creates a new instance.
     */
    public OutgoingPath() {
    }

    @XmlAttribute(required = true)
    public String getName() {
      return name;
    }

    public OutgoingPath setName(
        @Nonnull
        String name
    ) {
      requireNonNull(name, "name");
      this.name = name;
      return this;
    }
  }

  @XmlAccessorType(XmlAccessType.PROPERTY)
  @XmlType(propOrder = {"labelOffsetX", "labelOffsetY", "layerId"})
  public static class PointLayout {

    private Long labelOffsetX = 0L;
    private Long labelOffsetY = 0L;
    private Integer layerId = 0;

    /**
     * Creates a new instance.
     */
    public PointLayout() {
    }

    @XmlAttribute(required = true)
    public Long getLabelOffsetX() {
      return labelOffsetX;
    }

    public PointLayout setLabelOffsetX(
        @Nonnull
        Long labelOffsetX
    ) {
      this.labelOffsetX = requireNonNull(labelOffsetX, "labelOffsetX");
      return this;
    }

    @XmlAttribute(required = true)
    public Long getLabelOffsetY() {
      return labelOffsetY;
    }

    public PointLayout setLabelOffsetY(
        @Nonnull
        Long labelOffsetY
    ) {
      this.labelOffsetY = requireNonNull(labelOffsetY, "labelOffsetY");
      return this;
    }

    @XmlAttribute(required = true)
    public Integer getLayerId() {
      return layerId;
    }

    public PointLayout setLayerId(
        @Nonnull
        Integer layerId
    ) {
      this.layerId = requireNonNull(layerId, "layerId");
      return this;
    }
  }
}
