// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.persistence.v7;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.List;

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
  public VehicleEnvelopeTO setKey(
      @Nonnull
      String key
  ) {
    this.key = requireNonNull(key, "key");
    return this;
  }

  @XmlElement(name = "vertex")
  public List<CoupleTO> getVertices() {
    return vertices;
  }

  public VehicleEnvelopeTO setVertices(
      @Nonnull
      List<CoupleTO> vertices
  ) {
    this.vertices = requireNonNull(vertices, "vertices");
    return this;
  }
}
