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
import org.opentcs.data.ObjectHistory;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * An aggregation of model elements.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated Will be removed.
 */
@Deprecated
@ScheduledApiChange(details = "Will be removed.", when = "6.0")
public class Group
    extends TCSObject<Group>
    implements Serializable {

  /**
   * The model elements aggregated in this group.
   */
  private final Set<TCSObjectReference<?>> members;

  /**
   * Creates a new, empty group.
   *
   * @param name This group's name.
   */
  public Group(String name) {
    super(name);
    this.members = new HashSet<>();
  }

  private Group(String name,
                Map<String, String> properties,
                ObjectHistory history,
                Set<TCSObjectReference<?>> members) {
    super(name, properties, history);
    this.members = new HashSet<>(requireNonNull(members, "members"));
  }

  @Override
  public Group withProperty(String key, String value) {
    return new Group(getName(),
                     propertiesWith(key, value),
                     getHistory(),
                     members);
  }

  @Override
  public Group withProperties(Map<String, String> properties) {
    return new Group(getName(),
                     properties,
                     getHistory(),
                     members);
  }

  @Override
  public TCSObject<Group> withHistoryEntry(ObjectHistory.Entry entry) {
    return new Group(getName(),
                     getProperties(),
                     getHistory().withEntryAppended(entry),
                     members);
  }

  @Override
  public TCSObject<Group> withHistory(ObjectHistory history) {
    return new Group(getName(),
                     getProperties(),
                     history,
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
   * Creates a copy of this object, with the given members.
   *
   * @param members The value to be set in the copy.
   * @return A copy of this object, differing in the given value.
   */
  public Group withMembers(Set<TCSObjectReference<?>> members) {
    return new Group(getName(),
                     getProperties(),
                     getHistory(),
                     members);
  }
}
