/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.data.model.Path;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.EnvelopeTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PropertyTO;

/**
 */
public class PathTO {

  private String name;
  private String srcPointName;
  private String destPointName;
  private long length = 1;
  private int maxVelocity;
  private int maxReverseVelocity;
  private List<PeripheralOperationTO> peripheralOperations = List.of();
  private boolean locked;
  private Layout layout = new Layout();
  private List<EnvelopeTO> vehicleEnvelopes = List.of();
  private List<PropertyTO> properties = List.of();

  @JsonCreator
  public PathTO(
      @Nonnull @JsonProperty(value = "name", required = true) String name,
      @Nonnull @JsonProperty(value = "srcPointName", required = true) String srcPointName,
      @Nonnull @JsonProperty(value = "destPointName", required = true) String destPointName
  ) {
    this.name = requireNonNull(name, "name");
    this.srcPointName = requireNonNull(srcPointName, "srcPointName");
    this.destPointName = requireNonNull(destPointName, "destPointName");
  }

  @Nonnull
  public String getName() {
    return name;
  }

  public PathTO setName(@Nonnull String name) {
    this.name = requireNonNull(name, "name");
    return this;
  }

  @Nonnull
  public List<PropertyTO> getProperties() {
    return properties;
  }

  public PathTO setProperties(@Nonnull List<PropertyTO> properties) {
    this.properties = requireNonNull(properties, "properties");
    return this;
  }

  @Nonnull
  public String getSrcPointName() {
    return srcPointName;
  }

  public PathTO setSrcPointName(@Nonnull String srcPointName) {
    this.srcPointName = requireNonNull(srcPointName, "srcPointName");
    return this;
  }

  @Nonnull
  public String getDestPointName() {
    return destPointName;
  }

  public PathTO setDestPointName(@Nonnull String destPointName) {
    this.destPointName = requireNonNull(destPointName, "destPointName");
    return this;
  }

  public long getLength() {
    return length;
  }

  public PathTO setLength(long length) {
    this.length = length;
    return this;
  }

  public int getMaxVelocity() {
    return maxVelocity;
  }

  public PathTO setMaxVelocity(int maxVelocity) {
    this.maxVelocity = maxVelocity;
    return this;
  }

  public int getMaxReverseVelocity() {
    return maxReverseVelocity;
  }

  public PathTO setMaxReverseVelocity(int maxReverseVelocity) {
    this.maxReverseVelocity = maxReverseVelocity;
    return this;
  }

  @Nonnull
  public List<PeripheralOperationTO> getPeripheralOperations() {
    return peripheralOperations;
  }

  public PathTO setPeripheralOperations(
      @Nonnull List<PeripheralOperationTO> peripheralOperations) {
    this.peripheralOperations = requireNonNull(peripheralOperations, "peripheralOperations");
    return this;
  }

  public boolean isLocked() {
    return locked;
  }

  public PathTO setLocked(boolean locked) {
    this.locked = locked;
    return this;
  }

  @Nonnull
  public Layout getLayout() {
    return layout;
  }

  public PathTO setLayout(@Nonnull Layout layout) {
    this.layout = requireNonNull(layout, "layout");
    return this;
  }

  @Nonnull
  public List<EnvelopeTO> getVehicleEnvelopes() {
    return vehicleEnvelopes;
  }

  public PathTO setVehicleEnvelopes(@Nonnull List<EnvelopeTO> vehicleEnvelopes) {
    this.vehicleEnvelopes = requireNonNull(vehicleEnvelopes, "vehicleEnvelopes");
    return this;
  }

  public static class Layout {

    private String connectionType = Path.Layout.ConnectionType.DIRECT.name();
    private List<CoupleTO> controlPoints = List.of();
    private int layerId;

    public Layout() {

    }

    @Nonnull
    public String getConnectionType() {
      return connectionType;
    }

    public Layout setConnectionType(@Nonnull String connectionType) {
      this.connectionType = requireNonNull(connectionType, "connectionType");
      return this;
    }

    @Nonnull
    public List<CoupleTO> getControlPoints() {
      return controlPoints;
    }

    public Layout setControlPoints(@Nonnull List<CoupleTO> controlPoints) {
      this.controlPoints = requireNonNull(controlPoints, "controlPoints");
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
