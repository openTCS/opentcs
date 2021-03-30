/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.persistence.v003;

import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = {"name", "scaleX", "scaleY", "shapeLayoutElements", "modelLayoutElements",
                      "properties"})
public class VisualLayoutTO
    extends PlantModelElementTO {

  private Float scaleX = 0.0F;
  private Float scaleY = 0.0F;
  private List<ShapeLayoutElement> shapeLayoutElements = new ArrayList<>();
  private List<ModelLayoutElement> modelLayoutElements = new ArrayList<>();

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

  @XmlElement(name = "shapeLayoutElement")
  public List<ShapeLayoutElement> getShapeLayoutElements() {
    return shapeLayoutElements;
  }

  public VisualLayoutTO setShapeLayoutElements(
      @Nonnull List<ShapeLayoutElement> shapeLayoutElements) {
    requireNonNull(shapeLayoutElements, "shapeLayoutElements");
    this.shapeLayoutElements = shapeLayoutElements;
    return this;
  }

  @XmlElement(name = "modelLayoutElement")
  public List<ModelLayoutElement> getModelLayoutElements() {
    return modelLayoutElements;
  }

  public VisualLayoutTO setModelLayoutElements(
      @Nonnull List<ModelLayoutElement> modelLayoutElements) {
    requireNonNull(modelLayoutElements, "modelLayoutElements");
    this.modelLayoutElements = modelLayoutElements;
    return this;
  }

  @XmlAccessorType(XmlAccessType.PROPERTY)
  @XmlType(propOrder = {"layer", "properties"})
  public static class ShapeLayoutElement {

    private Long layer = 0L;
    private List<PropertyTO> properties = new ArrayList<>();

    @XmlAttribute(required = true)
    @XmlSchemaType(name = "unsignedInt")
    public Long getLayer() {
      return layer;
    }

    public ShapeLayoutElement setLayer(@Nonnull Long layer) {
      requireNonNull(layer, "layer");
      this.layer = layer;
      return this;
    }

    @XmlElement(name = "property")
    public List<PropertyTO> getProperties() {
      return properties;
    }

    public ShapeLayoutElement setProperties(@Nonnull List<PropertyTO> properties) {
      requireNonNull(properties, "properties");
      this.properties = properties;
      return this;
    }
  }

  @XmlAccessorType(XmlAccessType.PROPERTY)
  @XmlType(propOrder = {"visualizedObjectName", "layer", "properties"})
  public static class ModelLayoutElement {

    private String visualizedObjectName = "";
    private Long layer = 0L;
    private List<PropertyTO> properties = new ArrayList<>();

    @XmlAttribute(required = true)
    public String getVisualizedObjectName() {
      return visualizedObjectName;
    }

    public ModelLayoutElement setVisualizedObjectName(@Nonnull String visualizedObjectName) {
      requireNonNull(visualizedObjectName, "visualizedObjectName");
      this.visualizedObjectName = visualizedObjectName;
      return this;
    }

    @XmlAttribute(required = true)
    @XmlSchemaType(name = "unsignedInt")
    public Long getLayer() {
      return layer;
    }

    public ModelLayoutElement setLayer(@Nonnull Long layer) {
      requireNonNull(layer, "layer");
      this.layer = layer;
      return this;
    }

    @XmlElement(name = "property")
    public List<PropertyTO> getProperties() {
      return properties;
    }

    public ModelLayoutElement setProperties(@Nonnull List<PropertyTO> properties) {
      requireNonNull(properties, "properties");
      this.properties = properties;
      return this;
    }
  }
}
