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
@XmlType(propOrder = {"name", "id", "xPosition", "yPosition", "zPosition", "links", "properties"})
public class LocationTO
    extends PlantModelElementTO {

  private Long xPosition = 0L;
  private Long yPosition = 0L;
  private Long zPosition = 0L;
  private String type = "";
  private List<Link> links = new ArrayList<>();
  
  @XmlAttribute
  public Long getxPosition() {
    return xPosition;
  }

  public LocationTO setxPosition(@Nonnull Long xPosition) {
    requireNonNull(xPosition, "xPosition");
    this.xPosition = xPosition;
    return this;
  }

  @XmlAttribute
  public Long getyPosition() {
    return yPosition;
  }

  public LocationTO setyPosition(@Nonnull Long yPosition) {
    requireNonNull(yPosition, "yPosition");
    this.yPosition = yPosition;
    return this;
  }

  @XmlAttribute
  public Long getzPosition() {
    return zPosition;
  }

  public LocationTO setzPosition(@Nonnull Long zPosition) {
    requireNonNull(zPosition, "zPosition");
    this.zPosition = zPosition;
    return this;
  }

  @XmlAttribute
  public String getType() {
    return type;
  }

  public LocationTO setType(@Nonnull String type) {
    requireNonNull(type, "type");
    this.type = type;
    return this;
  }

  @XmlElement(name = "link", required = true)
  public List<Link> getLinks() {
    return links;
  }

  public LocationTO setLinks(@Nonnull List<Link> links) {
    requireNonNull(links, "links");
    this.links = links;
    return this;
  }

  @XmlAccessorType(XmlAccessType.PROPERTY)
  @XmlType(propOrder = {"point", "allowedOperations"})
  public static class Link {

    private String point = "";
    private List<AllowedOperationTO> allowedOperations = new ArrayList<>();

    @XmlAttribute(required = true)
    public String getPoint() {
      return point;
    }

    public Link setPoint(@Nonnull String point) {
      requireNonNull(point, "point");
      this.point = point;
      return this;
    }

    @XmlElement(name = "allowedOperation")
    public List<AllowedOperationTO> getAllowedOperations() {
      return allowedOperations;
    }

    public Link setAllowedOperations(@Nonnull List<AllowedOperationTO> allowedOperations) {
      requireNonNull(allowedOperations, "allowedOperations");
      this.allowedOperations = allowedOperations;
      return this;
    }
  }
}
