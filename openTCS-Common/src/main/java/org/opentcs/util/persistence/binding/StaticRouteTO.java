/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.persistence.binding;

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
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = {"name", "id", "hops", "properties"})
public class StaticRouteTO
    extends PlantModelElementTO {

  private List<Hop> hops = new ArrayList<>();

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
