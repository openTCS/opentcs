/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.persistence.v004;

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
@XmlType(propOrder = {"key", "vertices"})
public class VehicleEnvelopeTO {

  private String key;
  private List<CoupleTO> vertices;

  public VehicleEnvelopeTO() {
  }

  public String getKey() {
    return key;
  }

  @XmlAttribute
  public VehicleEnvelopeTO setKey(@Nonnull String key) {
    this.key = requireNonNull(key, "key");
    return this;
  }

  @XmlElement(name = "vertex")
  public List<CoupleTO> getVertices() {
    return vertices;
  }

  public VehicleEnvelopeTO setVertices(@Nonnull List<CoupleTO> vertices) {
    this.vertices = requireNonNull(vertices, "vertices");
    return this;
  }
}
