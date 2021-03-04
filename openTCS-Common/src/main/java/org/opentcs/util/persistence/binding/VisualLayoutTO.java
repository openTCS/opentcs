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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = {"name", "id", "scaleX", "scaleY", "colors", "shapeLayoutElements",
                      "modelLayoutElements", "viewBookmarks", "properties"})
public class VisualLayoutTO
    extends PlantModelElementTO {

  private Float scaleX = 0.0F;
  private Float scaleY = 0.0F;
  private List<Color> colors = new ArrayList<>();
  private List<ShapeLayoutElement> shapeLayoutElements = new ArrayList<>();
  private List<ModelLayoutElement> modelLayoutElements = new ArrayList<>();
  private List<ViewBookmark> viewBookmarks = new ArrayList<>();

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

  @XmlElement(name = "color")
  public List<Color> getColors() {
    return colors;
  }

  public VisualLayoutTO setColors(@Nonnull List<Color> colors) {
    requireNonNull(colors, "colors");
    this.colors = colors;
    return this;
  }

  @XmlElement(name = "shapeLayoutElement")
  public List<ShapeLayoutElement> getShapeLayoutElements() {
    return shapeLayoutElements;
  }

  public VisualLayoutTO setShapeLayoutElements(@Nonnull List<ShapeLayoutElement> shapeLayoutElements) {
    requireNonNull(shapeLayoutElements, "shapeLayoutElements");
    this.shapeLayoutElements = shapeLayoutElements;
    return this;
  }

  @XmlElement(name = "modelLayoutElement")
  public List<ModelLayoutElement> getModelLayoutElements() {
    return modelLayoutElements;
  }

  public VisualLayoutTO setModelLayoutElements(@Nonnull List<ModelLayoutElement> modelLayoutElements) {
    requireNonNull(modelLayoutElements, "modelLayoutElements");
    this.modelLayoutElements = modelLayoutElements;
    return this;
  }

  @XmlElement(name = "viewBookmark")
  public List<ViewBookmark> getViewBookmarks() {
    return viewBookmarks;
  }

  public VisualLayoutTO setViewBookmarks(@Nonnull List<ViewBookmark> viewBookmarks) {
    requireNonNull(viewBookmarks, "viewBookmarks");
    this.viewBookmarks = viewBookmarks;
    return this;
  }

  @XmlAccessorType(XmlAccessType.PROPERTY)
  @XmlType(propOrder = {"name", "redValue", "greenValue", "blueValue"})
  public static class Color {

    private String name = "";
    private Long redValue = 0L;
    private Long greenValue = 0L;
    private Long blueValue = 0L;

    @XmlAttribute(required = true)
    public String getName() {
      return name;
    }

    public Color setName(@Nonnull String name) {
      requireNonNull(name, "name");
      this.name = name;
      return this;
    }

    @XmlAttribute(required = true)
    @XmlSchemaType(name = "unsignedInt")
    public Long getRedValue() {
      return redValue;
    }

    public Color setRedValue(@Nonnull Long redValue) {
      requireNonNull(redValue, "redValue");
      this.redValue = redValue;
      return this;
    }

    @XmlAttribute(required = true)
    @XmlSchemaType(name = "unsignedInt")
    public Long getGreenValue() {
      return greenValue;
    }

    public Color setGreenValue(@Nonnull Long greenValue) {
      requireNonNull(greenValue, "greenValue");
      this.greenValue = greenValue;
      return this;
    }

    @XmlAttribute(required = true)
    @XmlSchemaType(name = "unsignedInt")
    public Long getBlueValue() {
      return blueValue;
    }

    public Color setBlueValue(@Nonnull Long blueValue) {
      requireNonNull(blueValue, "blueValue");
      this.blueValue = blueValue;
      return this;
    }
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

  @XmlAccessorType(XmlAccessType.PROPERTY)
  @XmlType(propOrder = {"label", "centerX", "centerY", "viewScaleX", "viewScaleY", "viewRotation"})
  public static class ViewBookmark {

    private String label = "";
    private Integer centerX = 0;
    private Integer centerY = 0;
    private Float viewScaleX = 0.0F;
    private Float viewScaleY = 0.0F;
    private Integer viewRotation = 0;

    @XmlAttribute(required = true)
    public String getLabel() {
      return label;
    }

    public ViewBookmark setLabel(@Nonnull String label) {
      requireNonNull(label, "label");
      this.label = label;
      return this;
    }

    @XmlAttribute(required = true)
    @XmlSchemaType(name = "int")
    public Integer getCenterX() {
      return centerX;
    }

    public ViewBookmark setCenterX(@Nonnull Integer centerX) {
      requireNonNull(centerX, "centerX");
      this.centerX = centerX;
      return this;
    }

    @XmlAttribute(required = true)
    @XmlSchemaType(name = "int")
    public Integer getCenterY() {
      return centerY;
    }

    public ViewBookmark setCenterY(@Nonnull Integer centerY) {
      requireNonNull(centerY, "centerY");
      this.centerY = centerY;
      return this;
    }

    @XmlAttribute(required = true)
    @XmlSchemaType(name = "float")
    public Float getViewScaleX() {
      return viewScaleX;
    }

    public ViewBookmark setViewScaleX(@Nonnull Float viewScaleX) {
      requireNonNull(viewScaleX, "viewScaleX");
      this.viewScaleX = viewScaleX;
      return this;
    }

    @XmlAttribute(required = true)
    @XmlSchemaType(name = "float")
    public Float getViewScaleY() {
      return viewScaleY;
    }

    public ViewBookmark setViewScaleY(@Nonnull Float viewScaleY) {
      requireNonNull(viewScaleY, "viewScaleY");
      this.viewScaleY = viewScaleY;
      return this;
    }

    @XmlAttribute(required = true)
    @XmlSchemaType(name = "int")
    public Integer getViewRotation() {
      return viewRotation;
    }

    public ViewBookmark setViewRotation(@Nonnull Integer viewRotation) {
      requireNonNull(viewRotation, "viewRotation");
      this.viewRotation = viewRotation;
      return this;
    }
  }
}
