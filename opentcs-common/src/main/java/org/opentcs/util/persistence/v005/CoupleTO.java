// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.persistence.v005;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = {"x", "y"})
public class CoupleTO {

  private Long x;
  private Long y;

  public CoupleTO() {
  }

  @XmlAttribute(required = true)
  public Long getX() {
    return x;
  }

  public CoupleTO setX(
      @Nonnull
      Long x
  ) {
    this.x = requireNonNull(x, "x");
    return this;
  }

  @XmlAttribute(required = true)
  public Long getY() {
    return y;
  }

  public CoupleTO setY(
      @Nonnull
      Long y
  ) {
    this.y = requireNonNull(y, "y");
    return this;
  }
}
