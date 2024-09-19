/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.persistence.v6;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = {"length", "width", "height", "referenceOffsetX", "referenceOffsetY"})
public class BoundingBoxTO {

  private long length;
  private long width;
  private long height;
  private long referenceOffsetX;
  private long referenceOffsetY;

  /**
   * Creates a new instance.
   */
  public BoundingBoxTO() {
  }

  @XmlAttribute(required = true)
  public long getLength() {
    return length;
  }

  public BoundingBoxTO setLength(long length) {
    this.length = length;
    return this;
  }

  @XmlAttribute(required = true)
  public long getWidth() {
    return width;
  }

  public BoundingBoxTO setWidth(long width) {
    this.width = width;
    return this;
  }

  @XmlAttribute(required = true)
  public long getHeight() {
    return height;
  }

  public BoundingBoxTO setHeight(long height) {
    this.height = height;
    return this;
  }

  @XmlAttribute(required = true)
  public long getReferenceOffsetX() {
    return referenceOffsetX;
  }

  public BoundingBoxTO setReferenceOffsetX(long referenceOffsetX) {
    this.referenceOffsetX = referenceOffsetX;
    return this;
  }

  @XmlAttribute(required = true)
  public long getReferenceOffsetY() {
    return referenceOffsetY;
  }

  public BoundingBoxTO setReferenceOffsetY(long referenceOffsetY) {
    this.referenceOffsetY = referenceOffsetY;
    return this;
  }
}
