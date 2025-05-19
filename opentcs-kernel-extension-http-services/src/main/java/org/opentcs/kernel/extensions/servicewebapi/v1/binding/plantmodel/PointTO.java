// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nonnull;
import java.util.List;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.BoundingBoxTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.EnvelopeTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PropertyTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.TripleTO;

/**
 */
public class PointTO {

  private String name;
  private TripleTO position = new TripleTO(0, 0, 0);
  private double vehicleOrientationAngle = Double.NaN;
  private String type = Type.HALT_POSITION.name();
  private Layout layout = new Layout();
  private List<EnvelopeTO> vehicleEnvelopes = List.of();
  private BoundingBoxTO maxVehicleBoundingBox
      = new BoundingBoxTO(1000, 1000, 1000, new CoupleTO(0, 0));
  private List<PropertyTO> properties = List.of();

  @JsonCreator
  public PointTO(
      @Nonnull
      @JsonProperty(value = "name", required = true)
      String name
  ) {
    this.name = requireNonNull(name, "name");
  }

  @Nonnull
  public String getName() {
    return name;
  }

  public PointTO setName(
      @Nonnull
      String name
  ) {
    this.name = requireNonNull(name, "name");
    return this;
  }

  @Nonnull
  public List<PropertyTO> getProperties() {
    return properties;
  }

  public PointTO setProperties(
      @Nonnull
      List<PropertyTO> properties
  ) {
    this.properties = requireNonNull(properties, "properties");
    return this;
  }

  @Nonnull
  public TripleTO getPosition() {
    return position;
  }

  public PointTO setPosition(
      @Nonnull
      TripleTO position
  ) {
    this.position = requireNonNull(position, "position");
    return this;
  }

  public double getVehicleOrientationAngle() {
    return vehicleOrientationAngle;
  }

  public PointTO setVehicleOrientationAngle(double vehicleOrientationAngle) {
    this.vehicleOrientationAngle = vehicleOrientationAngle;
    return this;
  }

  @Nonnull
  public String getType() {
    return type;
  }

  public PointTO setType(
      @Nonnull
      String type
  ) {
    this.type = requireNonNull(type, "type");
    return this;
  }

  @Nonnull
  public Layout getLayout() {
    return layout;
  }

  public PointTO setLayout(
      @Nonnull
      Layout pointLayout
  ) {
    this.layout = requireNonNull(pointLayout, "pointLayout");
    return this;
  }

  @Nonnull
  public List<EnvelopeTO> getVehicleEnvelopes() {
    return vehicleEnvelopes;
  }

  public PointTO setVehicleEnvelopes(
      @Nonnull
      List<EnvelopeTO> vehicleEnvelopes
  ) {
    this.vehicleEnvelopes = requireNonNull(vehicleEnvelopes, "vehicleEnvelopes");
    return this;
  }

  public BoundingBoxTO getMaxVehicleBoundingBox() {
    return maxVehicleBoundingBox;
  }

  public PointTO setMaxVehicleBoundingBox(BoundingBoxTO maxVehicleBoundingBoxTO) {
    this.maxVehicleBoundingBox = requireNonNull(maxVehicleBoundingBoxTO, "maxVehicleBoundingBox");
    return this;
  }

  // CHECKSTYLE:OFF
  public enum Type {

    HALT_POSITION,
  }
  // CHECKSTYLE:ON

  public static class Layout {

    private CoupleTO position = new CoupleTO(0, 0);
    private CoupleTO labelOffset = new CoupleTO(0, 0);
    private int layerId;

    public Layout() {

    }

    @Nonnull
    public CoupleTO getPosition() {
      return position;
    }

    public Layout setPosition(
        @Nonnull
        CoupleTO position
    ) {
      this.position = requireNonNull(position, "position");
      return this;
    }

    @Nonnull
    public CoupleTO getLabelOffset() {
      return labelOffset;
    }

    public Layout setLabelOffset(
        @Nonnull
        CoupleTO labelOffset
    ) {
      this.labelOffset = requireNonNull(labelOffset, "labelOffset");
      return this;
    }

    public int getLayerId() {
      return layerId;
    }

    public Layout setLayerId(int layerId) {
      this.layerId = layerId;
      return this;
    }

  }

}
