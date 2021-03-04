/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * An aggregation of resources that can never be used by more than one vehicle at the same time.
 *
 * @see TCSResource
 * @author Stefan Walter (Fraunhofer IML)
 */
@ScheduledApiChange(when = "5.0", details = "Will not implement Cloneable any more")
public class Block
    extends TCSResource<Block>
    implements Serializable,
               Cloneable {

  /**
   * The resources aggregated in this block.
   */
  private final Set<TCSResourceReference<?>> members;

  /**
   * Creates an empty block.
   *
   * @param objectID This block's ID.
   * @param name This block's name.
   * @deprecated Will be removed.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Block(int objectID, String name) {
    super(objectID, name);
    this.members = new HashSet<>();
  }

  /**
   * Creates an empty block.
   *
   * @param name This block's name.
   */
  public Block(String name) {
    super(name);
    this.members = new HashSet<>();
  }

  @SuppressWarnings("deprecation")
  private Block(int objectID,
                String name,
                Map<String, String> properties,
                Set<TCSResourceReference<?>> members) {
    super(objectID, name, properties);
    this.members = new HashSet<>(requireNonNull(members, "members"));
  }

  @Override
  public Block withProperty(String key, String value) {
    return new Block(getIdWithoutDeprecationWarning(),
                     getName(),
                     propertiesWith(key, value),
                     members);
  }

  @Override
  public Block withProperties(Map<String, String> properties) {
    return new Block(getIdWithoutDeprecationWarning(),
                     getName(),
                     properties,
                     members);
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
   * Adds a new member to this block.
   *
   * @param newMember The new member to be added to this block.
   * @deprecated Set via constructor instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void addMember(@Nonnull TCSResourceReference<?> newMember) {
    requireNonNull(newMember, "newMember");
    members.add(newMember);
  }

  /**
   * Removes a member from this block.
   *
   * @param rmMember The member to be removed from this block.
   * @deprecated Set via constructor instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void removeMember(TCSResourceReference<?> rmMember) {
    members.remove(rmMember);
  }

  /**
   * Creates a copy of this object, with the given members.
   *
   * @param members The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Block withMembers(Set<TCSResourceReference<?>> members) {
    return new Block(getIdWithoutDeprecationWarning(), getName(), getProperties(), members);
  }

  /**
   * Checks if this block contains a given object.
   *
   * @param chkMember The object to be checked for membership.
   * @return <code>true</code> if, and only if, the given object is a member of
   * this block.
   * @deprecated Use getMembers().contains() instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public boolean containsMember(TCSResourceReference<?> chkMember) {
    return members.contains(chkMember);
  }

  @SuppressWarnings("deprecation")
  private int getIdWithoutDeprecationWarning() {
    return getId();
  }

  /**
   * {@inheritDoc}
   *
   * @deprecated Will become immutable and not implement Cloneable any more.
   */
  @Override
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Block clone() {
    return new Block(getIdWithoutDeprecationWarning(), getName(), getProperties(), members);
  }
}
