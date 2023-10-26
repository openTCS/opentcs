/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.persistence.v004;

import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
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

  public CoupleTO setX(@Nonnull Long x) {
    this.x = requireNonNull(x, "x");
    return this;
  }

  @XmlAttribute(required = true)
  public Long getY() {
    return y;
  }

  public CoupleTO setY(@Nonnull Long y) {
    this.y = requireNonNull(y, "y");
    return this;
  }
}
