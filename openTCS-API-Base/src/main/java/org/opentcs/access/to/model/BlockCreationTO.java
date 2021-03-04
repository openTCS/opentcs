/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.to.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opentcs.access.to.CreationTO;

/**
 * A transfer object describing a block in the plant model.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class BlockCreationTO
    extends CreationTO
    implements Serializable {

  /**
   * This block's member names.
   */
  @Nonnull
  private Set<String> memberNames = new HashSet<>();

  /**
   * Creates a new instance.
   *
   * @param name The name of this block.
   */
  public BlockCreationTO(String name) {
    super(name);
  }

  /**
   * Sets the name of this block.
   *
   * @param name The new name.
   * @return The modified block.
   */
  @Nonnull
  @Override
  public BlockCreationTO setName(@Nonnull String name) {
    return (BlockCreationTO) super.setName(name);
  }

  /**
   * Sets the properties of this block.
   *
   * @param properties The new properties.
   * @return The modified block.
   */
  @Nonnull
  @Override
  public BlockCreationTO setProperties(@Nonnull Map<String, String> properties) {
    return (BlockCreationTO) super.setProperties(properties);
  }

  /**
   * Sets a single property of this block.
   *
   * @param key The property key.
   * @param value The property value.
   * @return The modified block.
   */
  @Nonnull
  @Override
  public BlockCreationTO setProperty(@Nonnull String key, @Nonnull String value) {
    return (BlockCreationTO) super.setProperty(key, value);
  }

  /**
   * Returns the names of this block's members.
   *
   * @return The names of this block's members.
   */
  @Nonnull
  public Set<String> getMemberNames() {
    return memberNames;
  }

  /**
   * Sets the names of this block's members.
   *
   * @param memberNames The names of this block's members.
   * @return The modified block.
   */
  @Nonnull
  public BlockCreationTO setMemberNames(@Nonnull Set<String> memberNames) {
    this.memberNames = requireNonNull(memberNames, "memberNames");
    return this;
  }
}
