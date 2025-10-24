// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.access.to.model;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import java.awt.Color;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.opentcs.access.to.CreationTO;

/**
 * A transfer object describing a block in the plant model.
 */
public class BlockCreationTO
    extends
      CreationTO
    implements
      Serializable {

  /**
   * This block's type.
   */
  @Nonnull
  private final Type type;
  /**
   * This block's member names.
   */
  @Nonnull
  private final Set<String> memberNames;
  /**
   * The information regarding the grahical representation of this block.
   */
  private final Layout layout;

  /**
   * Creates a new instance.
   *
   * @param name The name of this block.
   */
  public BlockCreationTO(
      @Nonnull
      String name
  ) {
    super(name);
    this.type = Type.SINGLE_VEHICLE_ONLY;
    this.memberNames = Set.of();
    this.layout = new Layout();
  }

  /**
   * Creates a new block.
   *
   * @param name the name of the new block.
   * @param memberNames the names of the block's members.
   * @param properties the properties.
   */
  private BlockCreationTO(
      @Nonnull
      String name,
      @Nonnull
      Map<String, String> properties,
      @Nonnull
      Type type,
      @Nonnull
      Set<String> memberNames,
      @Nonnull
      Layout layout
  ) {
    super(name, properties);
    this.type = requireNonNull(type, "type");
    this.memberNames = requireNonNull(memberNames, "memberNames");
    this.layout = requireNonNull(layout, "layout");
  }

  /**
   * Creates a copy of this object with the given name.
   *
   * @param name The new name.
   * @return A copy of this object, differing in the given name.
   */
  @Override
  public BlockCreationTO withName(
      @Nonnull
      String name
  ) {
    return new BlockCreationTO(
        name,
        getModifiableProperties(),
        type,
        memberNames,
        layout
    );
  }

  /**
   * Creates a copy of this object with the given properties.
   *
   * @param properties The new properties.
   * @return A copy of this object, differing in the given properties.
   */
  @Override
  public BlockCreationTO withProperties(
      @Nonnull
      Map<String, String> properties
  ) {
    return new BlockCreationTO(
        getName(),
        properties,
        type,
        memberNames,
        layout
    );
  }

  /**
   * Creates a copy of this object and adds the given property.
   * If value == null, then the key-value pair is removed from the properties.
   *
   * @param key the key.
   * @param value the value
   * @return A copy of this object that either
   * includes the given entry in it's current properties, if value != null or
   * excludes the entry otherwise.
   */
  @Override
  public BlockCreationTO withProperty(
      @Nonnull
      String key,
      @Nonnull
      String value
  ) {
    return new BlockCreationTO(
        getName(),
        propertiesWith(key, value),
        type,
        memberNames,
        layout
    );
  }

  /**
   * Returns the type of this block.
   *
   * @return The type of this block.
   */
  @Nonnull
  public Type getType() {
    return type;
  }

  /**
   * Creates a copy of this object with the given type.
   *
   * @param type The new type.
   * @return A copy of this object, differing in the given type.
   */
  public BlockCreationTO withType(
      @Nonnull
      Type type
  ) {
    return new BlockCreationTO(
        getName(),
        getModifiableProperties(),
        type,
        memberNames,
        layout
    );
  }

  /**
   * Returns the names of this block's members.
   *
   * @return The names of this block's members.
   */
  @Nonnull
  public Set<String> getMemberNames() {
    return Collections.unmodifiableSet(memberNames);
  }

  /**
   * Creates a copy of this object with the given members.
   *
   * @param memberNames The names of the block's members.
   * @return A copy of this object, differing in the given value.
   */
  public BlockCreationTO withMemberNames(
      @Nonnull
      Set<String> memberNames
  ) {
    return new BlockCreationTO(
        getName(),
        getModifiableProperties(),
        type,
        memberNames,
        layout
    );
  }

  /**
   * Returns the information regarding the grahical representation of this block.
   *
   * @return The information regarding the grahical representation of this block.
   */
  public Layout getLayout() {
    return layout;
  }

  /**
   * Creates a copy of this object, with the given layout.
   *
   * @param layout The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public BlockCreationTO withLayout(Layout layout) {
    return new BlockCreationTO(
        getName(),
        getModifiableProperties(),
        type,
        memberNames,
        layout
    );
  }

  @Override
  public String toString() {
    return "BlockCreationTO{"
        + "name=" + getName()
        + ", type=" + type
        + ", memberNames=" + memberNames
        + ", layout=" + layout
        + ", properties=" + getProperties()
        + '}';
  }

  /**
   * Describes the types of blocks in a driving course.
   */
  public enum Type {

    /**
     * The resources aggregated in this block can only be used by one vehicle at the same time.
     */
    SINGLE_VEHICLE_ONLY,
    /**
     * The resources aggregated in this block can be used by multiple vehicles, but only if they
     * enter the block in the same direction.
     */
    SAME_DIRECTION_ONLY;
  }

  /**
   * Contains information regarding the grahical representation of a block.
   */
  public static class Layout
      implements
        Serializable {

    /**
     * The color in which block elements are to be emphasized.
     */
    private final Color color;

    /**
     * Creates a new instance.
     */
    public Layout() {
      this(Color.RED);
    }

    /**
     * Creates a new instance.
     *
     * @param color The color in which block elements are to be emphasized.
     */
    public Layout(Color color) {
      this.color = requireNonNull(color, "color");
    }

    /**
     * Returns the color in which block elements are to be emphasized.
     *
     * @return The color in which block elements are to be emphasized.
     */
    public Color getColor() {
      return color;
    }

    /**
     * Creates a copy of this object, with the given color.
     *
     * @param color The value to be set in the copy.
     * @return A copy of this object, differing in the given value.
     */
    public Layout withColor(Color color) {
      return new Layout(color);
    }

    @Override
    public String toString() {
      return "Layout{"
          + "color=" + color
          + '}';
    }
  }
}
