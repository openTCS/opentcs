/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel;

import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.TripleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PropertyTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.EnvelopeTO;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.data.model.Point;

/**
 */
public class PointTO {

  private String name;
  private TripleTO position = new TripleTO(0, 0, 0);
  private double vehicleOrientationAngle = Double.NaN;
  private String type = Point.Type.HALT_POSITION.name();
  private Layout layout = new Layout();
  private List<EnvelopeTO> vehicleEnvelopes = List.of();
  private List<PropertyTO> properties = List.of();

  @JsonCreator
  public PointTO(@Nonnull @JsonProperty(value = "name", required = true) String name) {
    this.name = requireNonNull(name, "name");
  }

  @Nonnull
  public String getName() {
    return name;
  }

  public PointTO setName(@Nonnull String name) {
    this.name = requireNonNull(name, "name");
    return this;
  }

  @Nonnull
  public List<PropertyTO> getProperties() {
    return properties;
  }

  public PointTO setProperties(@Nonnull List<PropertyTO> properties) {
    this.properties = requireNonNull(properties, "properties");
    return this;
  }

  @Nonnull
  public TripleTO getPosition() {
    return position;
  }

  public PointTO setPosition(@Nonnull TripleTO position) {
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

  public PointTO setType(@Nonnull String type) {
    this.type = requireNonNull(type, "type");
    return this;
  }

  @Nonnull
  public Layout getLayout() {
    return layout;
  }

  public PointTO setLayout(@Nonnull Layout pointLayout) {
    this.layout = requireNonNull(pointLayout, "pointLayout");
    return this;
  }

  @Nonnull
  public List<EnvelopeTO> getVehicleEnvelopes() {
    return vehicleEnvelopes;
  }

  public PointTO setVehicleEnvelopes(@Nonnull List<EnvelopeTO> vehicleEnvelopes) {
    this.vehicleEnvelopes = requireNonNull(vehicleEnvelopes, "vehicleEnvelopes");
    return this;
  }

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

    public Layout setPosition(@Nonnull CoupleTO position) {
      this.position = requireNonNull(position, "position");
      return this;
    }

    @Nonnull
    public CoupleTO getLabelOffset() {
      return labelOffset;
    }

    public Layout setLabelOffset(@Nonnull CoupleTO labelOffset) {
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
