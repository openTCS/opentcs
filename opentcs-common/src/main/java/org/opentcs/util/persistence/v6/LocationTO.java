// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.persistence.v6;

import static java.util.Objects.requireNonNull;

import jakarta.annotation.Nonnull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(
    propOrder = {"name", "positionX", "positionY", "positionZ", "links", "locked",
        "properties", "locationLayout"}
)
public class LocationTO
    extends
      PlantModelElementTO {

  private Long positionX = 0L;
  private Long positionY = 0L;
  private Long positionZ = 0L;
  private String type = "";
  private List<Link> links = new ArrayList<>();
  private Boolean locked = false;
  private LocationLayout locationLayout = new LocationLayout();

  /**
   * Creates a new instance.
   */
  public LocationTO() {
  }

  @XmlAttribute(required = true)
  public Long getPositionX() {
    return positionX;
  }

  public LocationTO setPositionX(
      @Nonnull
      Long positionX
  ) {
    requireNonNull(positionX, "positionX");
    this.positionX = positionX;
    return this;
  }

  @XmlAttribute(required = true)
  public Long getPositionY() {
    return positionY;
  }

  public LocationTO setPositionY(
      @Nonnull
      Long positionY
  ) {
    requireNonNull(positionY, "positionY");
    this.positionY = positionY;
    return this;
  }

  @XmlAttribute(required = true)
  public Long getPositionZ() {
    return positionZ;
  }

  public LocationTO setPositionZ(
      @Nonnull
      Long positionZ
  ) {
    requireNonNull(positionZ, "positionZ");
    this.positionZ = positionZ;
    return this;
  }

  @XmlAttribute
  public String getType() {
    return type;
  }

  public LocationTO setType(
      @Nonnull
      String type
  ) {
    requireNonNull(type, "type");
    this.type = type;
    return this;
  }

  @XmlElement(name = "link", required = true)
  public List<Link> getLinks() {
    return links;
  }

  public LocationTO setLinks(
      @Nonnull
      List<Link> links
  ) {
    requireNonNull(links, "links");
    this.links = links;
    return this;
  }

  @XmlAttribute(required = true)
  public Boolean isLocked() {
    return locked;
  }

  public LocationTO setLocked(Boolean locked) {
    this.locked = locked;
    return this;
  }

  @XmlElement(required = true)
  public LocationLayout getLocationLayout() {
    return locationLayout;
  }

  public LocationTO setLocationLayout(
      @Nonnull
      LocationLayout locationLayout
  ) {
    this.locationLayout = requireNonNull(locationLayout, "locationLayout");
    return this;
  }

  @XmlAccessorType(XmlAccessType.PROPERTY)
  @XmlType(propOrder = {"point", "allowedOperations"})
  public static class Link {

    private String point = "";
    private List<AllowedOperationTO> allowedOperations = new ArrayList<>();

    /**
     * Creates a new instance.
     */
    public Link() {
    }

    @XmlAttribute(required = true)
    public String getPoint() {
      return point;
    }

    public Link setPoint(
        @Nonnull
        String point
    ) {
      requireNonNull(point, "point");
      this.point = point;
      return this;
    }

    @XmlElement(name = "allowedOperation")
    public List<AllowedOperationTO> getAllowedOperations() {
      return allowedOperations;
    }

    public Link setAllowedOperations(
        @Nonnull
        List<AllowedOperationTO> allowedOperations
    ) {
      requireNonNull(allowedOperations, "allowedOperations");
      this.allowedOperations = allowedOperations;
      return this;
    }
  }

  @XmlAccessorType(XmlAccessType.PROPERTY)
  @XmlType(
      propOrder = {"positionX", "positionY", "labelOffsetX", "labelOffsetY",
          "locationRepresentation", "layerId"}
  )
  public static class LocationLayout {

    private Long positionX = 0L;
    private Long positionY = 0L;
    private Long labelOffsetX = 0L;
    private Long labelOffsetY = 0L;
    private LocationRepresentation locationRepresentation = LocationRepresentation.DEFAULT;
    private Integer layerId = 0;

    /**
     * Creates a new instance.
     */
    public LocationLayout() {
    }

    @XmlAttribute(required = true)
    public Long getPositionX() {
      return positionX;
    }

    public LocationLayout setPositionX(Long positionX) {
      this.positionX = requireNonNull(positionX, "positionX");
      return this;
    }

    @XmlAttribute(required = true)
    public Long getPositionY() {
      return positionY;
    }

    public LocationLayout setPositionY(Long positionY) {
      this.positionY = requireNonNull(positionY, "positionY");
      return this;
    }

    @XmlAttribute(required = true)
    public Long getLabelOffsetX() {
      return labelOffsetX;
    }

    public LocationLayout setLabelOffsetX(Long labelOffsetX) {
      this.labelOffsetX = requireNonNull(labelOffsetX, "labelOffsetX");
      return this;
    }

    @XmlAttribute(required = true)
    public Long getLabelOffsetY() {
      return labelOffsetY;
    }

    public LocationLayout setLabelOffsetY(Long labelOffsetY) {
      this.labelOffsetY = requireNonNull(labelOffsetY, "labelOffsetY");
      return this;
    }

    @XmlAttribute(required = true)
    public LocationRepresentation getLocationRepresentation() {
      return locationRepresentation;
    }

    public LocationLayout setLocationRepresentation(LocationRepresentation locationRepresentation) {
      this.locationRepresentation = requireNonNull(
          locationRepresentation,
          "locationRepresentation"
      );
      return this;
    }

    @XmlAttribute(required = true)
    public Integer getLayerId() {
      return layerId;
    }

    public LocationLayout setLayerId(Integer layerId) {
      this.layerId = requireNonNull(layerId, "layerId");
      return this;
    }
  }
}
