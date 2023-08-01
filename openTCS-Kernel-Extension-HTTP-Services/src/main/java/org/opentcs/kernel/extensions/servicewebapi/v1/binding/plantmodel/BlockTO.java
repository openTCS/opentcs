/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel;

import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PropertyTO;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opentcs.data.model.Block;

/**
 */
public class BlockTO {

  private String name;
  private String type = Block.Type.SINGLE_VEHICLE_ONLY.name();
  private Layout layout = new Layout();
  private Set<String> memberNames = Set.of();
  private List<PropertyTO> properties = List.of();

  @JsonCreator
  public BlockTO(@Nonnull @JsonProperty(value = "name", required = true) String name) {
    this.name = requireNonNull(name, "name");
  }

  @Nonnull
  public String getName() {
    return name;
  }

  public BlockTO setName(@Nonnull String name) {
    this.name = requireNonNull(name, "name");
    return this;
  }

  @Nonnull
  public List<PropertyTO> getProperties() {
    return properties;
  }

  public BlockTO setProperties(@Nonnull List<PropertyTO> properties) {
    this.properties = requireNonNull(properties, "properties");
    return this;
  }

  @Nonnull
  public String getType() {
    return type;
  }

  public BlockTO setType(@Nonnull String type) {
    this.type = requireNonNull(type, "type");
    return this;
  }

  @Nonnull
  public Layout getLayout() {
    return layout;
  }

  public BlockTO setLayout(@Nonnull Layout layout) {
    this.layout = requireNonNull(layout, "layout");
    return this;
  }

  @Nonnull
  public Set<String> getMemberNames() {
    return memberNames;
  }

  public BlockTO setMemberNames(@Nonnull Set<String> memberNames) {
    this.memberNames = requireNonNull(memberNames, "memberNames");
    return this;
  }

  public static class Layout {

    private String color = "#FF0000";

    public Layout() {
    }

    @Nonnull
    public String getColor() {
      return color;
    }

    public Layout setColor(@Nonnull String color) {
      this.color = requireNonNull(color, "color");
      return this;
    }

  }

}
