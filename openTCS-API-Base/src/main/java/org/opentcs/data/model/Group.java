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
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * An aggregation of model elements.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
@ScheduledApiChange(when = "5.0", details = "Will not implement Cloneable any more")
public class Group
    extends TCSObject<Group>
    implements Serializable,
               Cloneable {

  /**
   * The model elements aggregated in this group.
   */
  private final Set<TCSObjectReference<?>> members;

  /**
   * Creates a new, empty group.
   *
   * @param objectID This group's ID.
   * @param name This group's name.
   * @deprecated Will be removed.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Group(int objectID, String name) {
    super(objectID, name);
    this.members = new HashSet<>();
  }

  /**
   * Creates a new, empty group.
   *
   * @param name This group's name.
   */
  public Group(String name) {
    super(name);
    this.members = new HashSet<>();
  }

  @SuppressWarnings("deprecation")
  private Group(int objectID,
                String name,
                Map<String, String> properties,
                Set<TCSObjectReference<?>> members) {
    super(objectID, name, properties);
    this.members = new HashSet<>(requireNonNull(members, "members"));
  }

  @Override
  public Group withProperty(String key, String value) {
    return new Group(getIdWithoutDeprecationWarning(),
                     getName(),
                     propertiesWith(key, value),
                     members);
  }

  @Override
  public Group withProperties(Map<String, String> properties) {
    return new Group(getIdWithoutDeprecationWarning(),
                     getName(),
                     properties,
                     members);
  }

  /**
   * Returns an unmodifiable set of all members of this group.
   *
   * @return An unmodifiable set of all members of this group.
   */
  public Set<TCSObjectReference<?>> getMembers() {
    return Collections.unmodifiableSet(members);
  }

  /**
   * Adds a new member to this group.
   *
   * @param newMember The new member to be added to this group.
   * @deprecated Set via constructor instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void addMember(TCSObjectReference<?> newMember) {
    Objects.requireNonNull(newMember, "newMember is null");
    members.add(newMember);
  }

  /**
   * Removes a member from this group.
   *
   * @param rmMember The member to be removed from this group.
   * @deprecated Set via constructor instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public void removeMember(TCSObjectReference<?> rmMember) {
    Objects.requireNonNull(rmMember, "rmMember is null");
    members.remove(rmMember);
  }

  /**
   * Creates a copy of this object, with the given members.
   *
   * @param members The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Group withMembers(Set<TCSObjectReference<?>> members) {
    return new Group(getIdWithoutDeprecationWarning(), getName(), getProperties(), members);
  }

  /**
   * Checks if this group contains a given object.
   *
   * @param chkMember The object to be checked for membership.
   * @return <code>true</code> if, and only if, the given object is a member of
   * this group.
   * @deprecated Use getMembers().contains() instead.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public boolean containsMember(TCSObjectReference<?> chkMember) {
    Objects.requireNonNull(chkMember, "chkMember is null");
    return members.contains(chkMember);
  }

  /**
   * {@inheritDoc}
   *
   * @deprecated Will become immutable and not implement Cloneable any more.
   */
  @Override
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  public Group clone() {
    return new Group(getIdWithoutDeprecationWarning(), getName(), getProperties(), members);
  }

  @SuppressWarnings("deprecation")
  private int getIdWithoutDeprecationWarning() {
    return getId();
  }

}
