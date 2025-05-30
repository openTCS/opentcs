// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PropertyTO;

/**
 */
public class BlockTO {

  private String name;
  private String type = Type.SINGLE_VEHICLE_ONLY.name();
  private Layout layout = new Layout();
  private Set<String> memberNames = Set.of();
  private List<PropertyTO> properties = List.of();

  @JsonCreator
  public BlockTO(
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

  public BlockTO setName(
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

  public BlockTO setProperties(
      @Nonnull
      List<PropertyTO> properties
  ) {
    this.properties = requireNonNull(properties, "properties");
    return this;
  }

  @Nonnull
  public String getType() {
    return type;
  }

  public BlockTO setType(
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

  public BlockTO setLayout(
      @Nonnull
      Layout layout
  ) {
    this.layout = requireNonNull(layout, "layout");
    return this;
  }

  @Nonnull
  public Set<String> getMemberNames() {
    return memberNames;
  }

  public BlockTO setMemberNames(
      @Nonnull
      Set<String> memberNames
  ) {
    this.memberNames = requireNonNull(memberNames, "memberNames");
    return this;
  }

  // CHECKSTYLE:OFF
  public enum Type {

    SINGLE_VEHICLE_ONLY,
  }
  // CHECKSTYLE:ON

  public static class Layout {

    private String color = "#FF0000";

    public Layout() {
    }

    @Nonnull
    public String getColor() {
      return color;
    }

    public Layout setColor(
        @Nonnull
        String color
    ) {
      this.color = requireNonNull(color, "color");
      return this;
    }

  }

}
