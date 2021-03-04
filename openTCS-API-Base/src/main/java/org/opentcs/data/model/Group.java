/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;

/**
 * An aggregation of model elements.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Group
    extends TCSObject<Group>
    implements Serializable, Cloneable {

  /**
   * The model elements aggregated in this group.
   */
  private Set<TCSObjectReference<?>> members = new HashSet<>();

  /**
   * Creates a new, empty group.
   *
   * @param objectID This group's ID.
   * @param name This group's name.
   */
  public Group(int objectID, String name) {
    super(objectID, name);
  }

  /**
   * Returns a set of all members of this group.
   *
   * @return A set of all members of this group.
   */
  public Set<TCSObjectReference<?>> getMembers() {
    return new HashSet<>(members);
  }

  /**
   * Adds a new member to this group.
   *
   * @param newMember The new member to be added to this group.
   */
  public void addMember(TCSObjectReference<?> newMember) {
    Objects.requireNonNull(newMember, "newMember is null");
    members.add(newMember);
  }

  /**
   * Removes a member from this group.
   *
   * @param rmMember The member to be removed from this group.
   */
  public void removeMember(TCSObjectReference<?> rmMember) {
    Objects.requireNonNull(rmMember, "rmMember is null");
    members.remove(rmMember);
  }

  /**
   * Checks if this group contains a given object.
   *
   * @param chkMember The object to be checked for membership.
   * @return <code>true</code> if, and only if, the given object is a member of
   * this group.
   */
  public boolean containsMember(TCSObjectReference<?> chkMember) {
    Objects.requireNonNull(chkMember, "chkMember is null");
    return members.contains(chkMember);
  }

  @Override
  public Group clone() {
    Group clone = (Group) super.clone();
    clone.members = new HashSet<>();
    for (TCSObjectReference<?> curRef : members) {
      clone.members.add(curRef);
    }
    return clone;
  }
}
