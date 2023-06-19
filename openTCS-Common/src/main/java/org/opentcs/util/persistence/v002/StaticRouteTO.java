/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.persistence.v002;

import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = {"name", "id", "hops", "properties"})
public class StaticRouteTO
    extends PlantModelElementTO {

  private List<Hop> hops = new ArrayList<>();

  /**
   * Creates a new instance.
   */
  public StaticRouteTO() {
  }

  @XmlElement(name = "hop")
  public List<Hop> getHops() {
    return hops;
  }

  public StaticRouteTO setHops(@Nonnull List<Hop> hops) {
    requireNonNull(hops, "hops");
    this.hops = hops;
    return this;
  }

  @XmlAccessorType(XmlAccessType.PROPERTY)
  public static class Hop {

    private String name = "";

    /**
     * Creates a new instance.
     */
    public Hop() {
    }

    @XmlAttribute(required = true)
    public String getName() {
      return name;
    }

    public Hop setName(@Nonnull String name) {
      requireNonNull(name, "name");
      this.name = name;
      return this;
    }
  }
}
