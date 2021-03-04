/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.to.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.access.to.CreationTO;
import org.opentcs.components.kernel.Router;
import org.opentcs.util.annotations.ScheduledApiChange;

/**
 * A transfer object describing a static route in the plant model.
 *
 * @author Stefan Walter (Fraunhofer IML)
 * @deprecated Static routes are an undesirable exception to routes computed by a {@link Router}
 * implementation and will not be supported in the future.
 */
@Deprecated
@ScheduledApiChange(when = "5.0")
public class StaticRouteCreationTO
    extends CreationTO
    implements Serializable {

  /**
   * This static route's hop names.
   */
  @Nonnull
  private List<String> hopNames = new LinkedList<>();

  /**
   * Creates a new instance.
   *
   * @param name The name of this static route.
   */
  public StaticRouteCreationTO(String name) {
    super(name);
  }

  /**
   * creates a new StaticRoute.
   *
   * @param name the name of the new route.
   * @param properties the properties.
   * @param hopNames the hopnames.
   */
  private StaticRouteCreationTO(@Nonnull String name,
                                @Nonnull Map<String, String> properties,
                                @Nonnull List<String> hopNames) {
    super(name, properties);
    this.hopNames = requireNonNull(hopNames, "hopNames");
  }

  /**
   * Sets the name of this static route.
   *
   * @param name The new name.
   * @return The modified static route.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  @Override
  public StaticRouteCreationTO setName(@Nonnull String name) {
    return (StaticRouteCreationTO) super.setName(name);
  }

  /**
   * Creates a copy of this object with the given name.
   *
   * @param name The new name.
   * @return A copy of this object, differing in the given name.
   */
  @Override
  public StaticRouteCreationTO withName(@Nonnull String name) {
    return new StaticRouteCreationTO(name, getModifiableProperties(), hopNames);
  }

  /**
   * Sets the properties of this static route.
   *
   * @param properties The new properties.
   * @return The modified static route.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  @Override
  public StaticRouteCreationTO setProperties(@Nonnull Map<String, String> properties) {
    return (StaticRouteCreationTO) super.setProperties(properties);
  }

  /**
   * Creates a copy of this object with the given properties.
   *
   * @param properties The new properties.
   * @return A copy of this object, differing in the given properties.
   */
  @Override
  public StaticRouteCreationTO withProperties(@Nonnull Map<String, String> properties) {
    return new StaticRouteCreationTO(getName(), properties, hopNames);
  }

  /**
   * Sets a single property of this static route.
   *
   * @param key The property key.
   * @param value The property value.
   * @return The modified static route.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  @Override
  public StaticRouteCreationTO setProperty(@Nonnull String key, @Nonnull String value) {
    return (StaticRouteCreationTO) super.setProperty(key, value);
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
  public StaticRouteCreationTO withProperty(@Nonnull String key, @Nonnull String value) {
    return new StaticRouteCreationTO(getName(), propertiesWith(key, value), hopNames);
  }

  /**
   * Returns the names of this static route's hops.
   *
   * @return The names of this static route's hops.
   */
  @Nonnull
  public List<String> getHopNames() {
    return Collections.unmodifiableList(hopNames);
  }

  /**
   * Sets the names of this static route's hops.
   *
   * @param hopNames The names of this static route's hops.
   * @return The modified static route.
   */
  @Deprecated
  @ScheduledApiChange(when = "5.0")
  @Nonnull
  public StaticRouteCreationTO setHopNames(@Nonnull List<String> hopNames) {
    this.hopNames = requireNonNull(hopNames, "hopNames");
    return this;
  }

  /**
   * Creates a copy of this object with the names of the static route's hops.
   *
   * @param hopNames The names of this static route's hops.
   * @return a copy of this object, differing in the given hop names.
   */
  public StaticRouteCreationTO withHopNames(@Nonnull List<String> hopNames) {
    requireNonNull(hopNames, "hopNames");
    return new StaticRouteCreationTO(getName(), getModifiableProperties(), hopNames);
  }
}
