/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.persistence.v004;

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
@XmlType(propOrder = {"name", "scaleX", "scaleY", "layers", "layerGroups", "properties"})
public class VisualLayoutTO
    extends PlantModelElementTO {

  private Float scaleX = 0.0F;
  private Float scaleY = 0.0F;
  private List<Layer> layers = new ArrayList<>();
  private List<LayerGroup> layerGroups = new ArrayList<>();

  @XmlAttribute(required = true)
  public Float getScaleX() {
    return scaleX;
  }

  public VisualLayoutTO setScaleX(@Nonnull Float scaleX) {
    requireNonNull(scaleX, "scaleX");
    this.scaleX = scaleX;
    return this;
  }

  @XmlAttribute(required = true)
  public Float getScaleY() {
    return scaleY;
  }

  public VisualLayoutTO setScaleY(@Nonnull Float scaleY) {
    requireNonNull(scaleY, "scaleY");
    this.scaleY = scaleY;
    return this;
  }

  @XmlElement(name = "layer")
  public List<Layer> getLayers() {
    return layers;
  }

  public VisualLayoutTO setLayers(@Nonnull List<Layer> layers) {
    this.layers = requireNonNull(layers, "layers");
    return this;
  }

  @XmlElement(name = "layerGroup")
  public List<LayerGroup> getLayerGroups() {
    return layerGroups;
  }

  public VisualLayoutTO setLayerGroups(@Nonnull List<LayerGroup> layerGroups) {
    this.layerGroups = requireNonNull(layerGroups, "layerGroups");
    return this;
  }

  @XmlAccessorType(XmlAccessType.PROPERTY)
  @XmlType(propOrder = {"id", "ordinal", "visible", "name", "groupId"})
  public static class Layer {

    private Integer id = 0;
    private Integer ordinal = 0;
    private Boolean visible = true;
    private String name = "";
    private Integer groupId = 0;

    @XmlAttribute(required = true)
    public Integer getId() {
      return id;
    }

    public Layer setId(Integer id) {
      this.id = requireNonNull(id, "id");
      return this;
    }

    @XmlAttribute(required = true)
    public Integer getOrdinal() {
      return ordinal;
    }

    public Layer setOrdinal(Integer ordinal) {
      this.ordinal = requireNonNull(ordinal, "ordinal");
      return this;
    }

    @XmlAttribute(required = true)
    public Boolean isVisible() {
      return visible;
    }

    public Layer setVisible(Boolean visible) {
      this.visible = requireNonNull(visible, "visible");
      return this;
    }

    @XmlAttribute(required = true)
    public String getName() {
      return name;
    }

    public Layer setName(String name) {
      this.name = requireNonNull(name, "name");
      return this;
    }

    @XmlAttribute(required = true)
    public Integer getGroupId() {
      return groupId;
    }

    public Layer setGroupId(Integer groupId) {
      this.groupId = requireNonNull(groupId, "groupId");
      return this;
    }
  }

  @XmlAccessorType(XmlAccessType.PROPERTY)
  @XmlType(propOrder = {"id", "name", "visible"})
  public static class LayerGroup {

    private Integer id = 0;
    private String name = "";
    private Boolean visible = true;

    @XmlAttribute(required = true)
    public Integer getId() {
      return id;
    }

    public LayerGroup setId(Integer id) {
      this.id = requireNonNull(id, "id");
      return this;
    }

    @XmlAttribute(required = true)
    public String getName() {
      return name;
    }

    public LayerGroup setName(String name) {
      this.name = requireNonNull(name, "name");
      return this;
    }

    @XmlAttribute(required = true)
    public Boolean isVisible() {
      return visible;
    }

    public LayerGroup setVisible(Boolean visible) {
      this.visible = requireNonNull(visible, "visible");
      return this;
    }
  }
}
