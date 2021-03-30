/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model;

import java.awt.Color;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.TCSObject;

/**
 * An aggregation of resources with distinct usage rules depending on the block's type.
 *
 * @see TCSResource
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Block
    extends TCSResource<Block>
    implements Serializable {

  /**
   * This block's type.
   */
  private final Type type;
  /**
   * The resources aggregated in this block.
   */
  private final Set<TCSResourceReference<?>> members;
  /**
   * The information regarding the grahical representation of this block.
   */
  private final Layout layout;

  /**
   * Creates an empty block.
   *
   * @param name This block's name.
   */
  public Block(String name) {
    super(name);
    this.type = Type.SINGLE_VEHICLE_ONLY;
    this.members = new HashSet<>();
    this.layout = new Layout();
  }

  private Block(String name,
                Map<String, String> properties,
                ObjectHistory history,
                Type type,
                Set<TCSResourceReference<?>> members,
                Layout layout) {
    super(name, properties, history);
    this.type = type;
    this.members = new HashSet<>(requireNonNull(members, "members"));
    this.layout = requireNonNull(layout, "layout");
  }

  @Override
  public Block withProperty(String key, String value) {
    return new Block(getName(),
                     propertiesWith(key, value),
                     getHistory(),
                     type,
                     members,
                     layout);
  }

  @Override
  public Block withProperties(Map<String, String> properties) {
    return new Block(getName(),
                     properties,
                     getHistory(),
                     type,
                     members,
                     layout);
  }

  @Override
  public TCSObject<Block> withHistoryEntry(ObjectHistory.Entry entry) {
    return new Block(getName(),
                     getProperties(),
                     getHistory().withEntryAppended(entry),
                     type,
                     members,
                     layout);
  }

  @Override
  public TCSObject<Block> withHistory(ObjectHistory history) {
    return new Block(getName(),
                     getProperties(),
                     history,
                     type,
                     members,
                     layout);
  }

  /**
   * Retruns the type of this block.
   *
   * @return The type of this block.
   */
  public Type getType() {
    return type;
  }

  /**
   * Creates a copy of this object, with the given type.
   *
   * @param type The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Block withType(Type type) {
    return new Block(getName(),
                     getProperties(),
                     getHistory(),
                     type,
                     members,
                     layout);
  }

  /**
   * Returns an unmodifiable set of all members of this block.
   *
   * @return An unmodifiable set of all members of this block.
   */
  public Set<TCSResourceReference<?>> getMembers() {
    return Collections.unmodifiableSet(members);
  }

  /**
   * Creates a copy of this object, with the given members.
   *
   * @param members The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Block withMembers(Set<TCSResourceReference<?>> members) {
    return new Block(getName(),
                     getProperties(),
                     getHistory(),
                     type,
                     members,
                     layout);
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
  public Block withLayout(Layout layout) {
    return new Block(getName(),
                     getProperties(),
                     getHistory(),
                     type,
                     members,
                     layout);
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
      implements Serializable {

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
  }
}
