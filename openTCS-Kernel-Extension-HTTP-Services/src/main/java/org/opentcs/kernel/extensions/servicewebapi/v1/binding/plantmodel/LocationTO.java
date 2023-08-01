/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel;

import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.LinkTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.TripleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PropertyTO;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.data.model.visualization.LocationRepresentation;

/**
 */
public class LocationTO {

  private String name;
  private String typeName;
  private TripleTO position;
  private List<LinkTO> links = List.of();
  private boolean locked;
  private Layout layout = new Layout();
  private List<PropertyTO> properties = List.of();

  @JsonCreator
  public LocationTO(
      @Nonnull @JsonProperty(value = "name", required = true) String name,
      @Nonnull @JsonProperty(value = "typeName", required = true) String typeName,
      @Nonnull @JsonProperty(value = "position", required = true) TripleTO position
  ) {
    this.name = requireNonNull(name, "name");
    this.typeName = requireNonNull(typeName, "typeName");
    this.position = requireNonNull(position, "position");
  }

  @Nonnull
  public String getName() {
    return name;
  }

  public LocationTO setName(@Nonnull String name) {
    this.name = requireNonNull(name, "name");
    return this;
  }

  @Nonnull
  public List<PropertyTO> getProperties() {
    return properties;
  }

  public LocationTO setProperties(@Nonnull List<PropertyTO> properties) {
    this.properties = requireNonNull(properties, "properties");
    return this;
  }

  @Nonnull
  public String getTypeName() {
    return typeName;
  }

  public LocationTO setTypeName(@Nonnull String typeName) {
    this.typeName = requireNonNull(typeName, "typeName");
    return this;
  }

  @Nonnull
  public TripleTO getPosition() {
    return position;
  }

  public LocationTO setPosition(@Nonnull TripleTO position) {
    this.position = requireNonNull(position, "position");
    return this;
  }

  @Nonnull
  public List<LinkTO> getLinks() {
    return links;
  }

  public LocationTO setLinks(@Nonnull List<LinkTO> links) {
    this.links = requireNonNull(links, "links");
    return this;
  }

  public boolean isLocked() {
    return locked;
  }

  public LocationTO setLocked(boolean locked) {
    this.locked = locked;
    return this;
  }

  @Nonnull
  public Layout getLayout() {
    return layout;
  }

  @Nonnull
  public LocationTO setLayout(@Nonnull Layout layout) {
    this.layout = requireNonNull(layout, "layout");
    return this;
  }

  public static class Layout {

    private CoupleTO position = new CoupleTO(0, 0);
    private CoupleTO labelOffset = new CoupleTO(0, 0);
    private String locationRepresentation = LocationRepresentation.DEFAULT.name();
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

    @Nonnull
    public String getLocationRepresentation() {
      return locationRepresentation;
    }

    public Layout setLocationRepresentation(
        @Nonnull String locationRepresentation) {
      this.locationRepresentation = requireNonNull(
          locationRepresentation, "locationRepresentation");
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
