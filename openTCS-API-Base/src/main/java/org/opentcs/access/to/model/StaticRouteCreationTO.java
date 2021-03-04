/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.access.to.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.access.to.CreationTO;

/**
 * A transfer object describing a static route in the plant model.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
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
   * Sets the name of this static route.
   *
   * @param name The new name.
   * @return The modified static route.
   */
  @Nonnull
  @Override
  public StaticRouteCreationTO setName(@Nonnull String name) {
    return (StaticRouteCreationTO) super.setName(name);
  }

  /**
   * Sets the properties of this static route.
   *
   * @param properties The new properties.
   * @return The modified static route.
   */
  @Nonnull
  @Override
  public StaticRouteCreationTO setProperties(@Nonnull Map<String, String> properties) {
    return (StaticRouteCreationTO) super.setProperties(properties);
  }

  /**
   * Sets a single property of this static route.
   *
   * @param key The property key.
   * @param value The property value.
   * @return The modified static route.
   */
  @Nonnull
  @Override
  public StaticRouteCreationTO setProperty(@Nonnull String key, @Nonnull String value) {
    return (StaticRouteCreationTO) super.setProperty(key, value);
  }

  /**
   * Returns the names of this static route's hops.
   *
   * @return The names of this static route's hops.
   */
  @Nonnull
  public List<String> getHopNames() {
    return hopNames;
  }

  /**
   * Sets the names of this static route's hops.
   *
   * @param hopNames The names of this static route's hops.
   * @return The modified static route.
   */
  @Nonnull
  public StaticRouteCreationTO setHopNames(@Nonnull List<String> hopNames) {
    this.hopNames = requireNonNull(hopNames, "hopNames");
    return this;
  }
}
