/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.persistence;

import static java.util.Objects.requireNonNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * JAXB class for Properties.
 *
 * @author Philipp Seifert (Philipp.Seifert@iml.fraunhofer.de)
 */
@XmlSeeAlso({CourseObjectProperty.StringCourseProperty.class,
             CourseObjectProperty.IntegerCourseProperty.class,
             CourseObjectProperty.AngleCourseProperty.class,
             CourseObjectProperty.BooleanCourseProperty.class,
             CourseObjectProperty.ColorCourseProperty.class,
             CourseObjectProperty.CoordinateCourseProperty.class,
             CourseObjectProperty.CoursePointCourseProperty.class,
             CourseObjectProperty.KeyValueCourseProperty.class,
             CourseObjectProperty.KeyValueSetCourseProperty.class,
             CourseObjectProperty.LengthCourseProperty.class,
             CourseObjectProperty.LinkActionsCourseProperty.class,
             CourseObjectProperty.LocationThemeCourseProperty.class,
             CourseObjectProperty.LocationTypeCourseProperty.class,
             CourseObjectProperty.PercentCourseProperty.class,
             CourseObjectProperty.SelectionCourseProperty.class,
             CourseObjectProperty.SpeedCourseProperty.class,
             CourseObjectProperty.StringSetCourseProperty.class,
             CourseObjectProperty.SymbolCourseProperty.class,
             CourseObjectProperty.TripleCourseProperty.class,
             CourseObjectProperty.VehicleThemeCourseProperty.class})
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class CourseObjectProperty {

  /**
   * No-arg default constructor required by JAXB.
   */
  public CourseObjectProperty() {
  }

  /**
   * The key of the property.
   */
  @XmlElement(required = true)
  private String key;
  /**
   * The value of the property.
   */
  @XmlElement(required = true)
  private Object value;

  public CourseObjectProperty(String key, Object value) {
    this.key = requireNonNull(key, "key is null");
    this.value = value; // value can be null
  }

  public String getKey() {
    return key;
  }

  public void setKey(String name) {
    this.key = name;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  @XmlRootElement(name = "stringProperty")
  public static class StringCourseProperty
      extends CourseObjectProperty {

    /**
     * No-arg default constructor required by JAXB.
     */
    public StringCourseProperty() {
    }

    public StringCourseProperty(String key, String value) {
      super(key, value);
    }
  }

  @XmlRootElement(name = "integerProperty")
  public static class IntegerCourseProperty
      extends CourseObjectProperty {

    /**
     * No-arg default constructor required by JAXB.
     */
    public IntegerCourseProperty() {
    }

    public IntegerCourseProperty(String key, Object value) {
      super(key, value);
    }
  }

  @XmlRootElement(name = "angleProperty")
  public static class AngleCourseProperty
      extends CourseObjectProperty {

    /**
     * No-arg default constructor required by JAXB.
     */
    public AngleCourseProperty() {
    }

    public AngleCourseProperty(String key, Object value) {
      super(key, value);
    }
  }

  @XmlRootElement(name = "booleanProperty")
  public static class BooleanCourseProperty
      extends CourseObjectProperty {

    /**
     * No-arg default constructor required by JAXB.
     */
    public BooleanCourseProperty() {
    }

    public BooleanCourseProperty(String key, Object value) {
      super(key, value);
    }
  }

  @XmlRootElement(name = "colorProperty")
  public static class ColorCourseProperty
      extends CourseObjectProperty {

    /**
     * No-arg default constructor required by JAXB.
     */
    public ColorCourseProperty() {
    }

    public ColorCourseProperty(String key, Object value) {
      super(key, value);
    }
  }

  @XmlRootElement(name = "coordinateProperty")
  public static class CoordinateCourseProperty
      extends CourseObjectProperty {

    /**
     * No-arg default constructor required by JAXB.
     */
    public CoordinateCourseProperty() {
    }

    public CoordinateCourseProperty(String key, Object value) {
      super(key, value);
    }
  }

  @XmlRootElement(name = "coursePointProperty")
  public static class CoursePointCourseProperty
      extends CourseObjectProperty {

    /**
     * No-arg default constructor required by JAXB.
     */
    public CoursePointCourseProperty() {
    }

    public CoursePointCourseProperty(String key, Object value) {
      super(key, value);
    }
  }

  @XmlRootElement(name = "keyValueProperty")
  public static class KeyValueCourseProperty
      extends CourseObjectProperty {

    /**
     * No-arg default constructor required by JAXB.
     */
    public KeyValueCourseProperty() {
    }

    public KeyValueCourseProperty(String key, Object value) {
      super(key, value);
    }
  }

  @XmlRootElement(name = "keyValueSetProperty")
  public static class KeyValueSetCourseProperty
      extends CourseObjectProperty {

    /**
     * No-arg default constructor required by JAXB.
     */
    public KeyValueSetCourseProperty() {
    }

    public KeyValueSetCourseProperty(String key, Object value) {
      super(key, value);
    }
  }

  @XmlRootElement(name = "lengthProperty")
  public static class LengthCourseProperty
      extends CourseObjectProperty {

    /**
     * No-arg default constructor required by JAXB.
     */
    public LengthCourseProperty() {
    }

    public LengthCourseProperty(String key, Object value) {
      super(key, value);
    }
  }

  @XmlRootElement(name = "linkActionProperty")
  public static class LinkActionsCourseProperty
      extends CourseObjectProperty {

    /**
     * No-arg default constructor required by JAXB.
     */
    public LinkActionsCourseProperty() {
    }

    public LinkActionsCourseProperty(String key, Object value) {
      super(key, value);
    }
  }

  @XmlRootElement(name = "locationThemeProperty")
  public static class LocationThemeCourseProperty
      extends CourseObjectProperty {

    /**
     * No-arg default constructor required by JAXB.
     */
    public LocationThemeCourseProperty() {
    }

    public LocationThemeCourseProperty(String key, Object value) {
      super(key, value);
    }
  }

  @XmlRootElement(name = "percentProperty")
  public static class PercentCourseProperty
      extends CourseObjectProperty {

    /**
     * No-arg default constructor required by JAXB.
     */
    public PercentCourseProperty() {
    }

    public PercentCourseProperty(String key, Object value) {
      super(key, value);
    }
  }

  @XmlRootElement(name = "selectionProperty")
  public static class SelectionCourseProperty
      extends CourseObjectProperty {

    /**
     * No-arg default constructor required by JAXB.
     */
    public SelectionCourseProperty() {
    }

    public SelectionCourseProperty(String key, Object value) {
      super(key, value);
    }
  }

  @XmlRootElement(name = "locationTypeProperty")
  public static class LocationTypeCourseProperty
      extends CourseObjectProperty {

    /**
     * No-arg default constructor required by JAXB.
     */
    public LocationTypeCourseProperty() {
    }

    public LocationTypeCourseProperty(String key, Object value) {
      super(key, value);
    }
  }

  @XmlRootElement(name = "speedProperty")
  public static class SpeedCourseProperty
      extends CourseObjectProperty {

    /**
     * No-arg default constructor required by JAXB.
     */
    public SpeedCourseProperty() {
    }

    public SpeedCourseProperty(String key, Object value) {
      super(key, value);
    }
  }

  @XmlRootElement(name = "stringSetProperty")
  public static class StringSetCourseProperty
      extends CourseObjectProperty {

    /**
     * No-arg default constructor required by JAXB.
     */
    public StringSetCourseProperty() {
    }

    public StringSetCourseProperty(String key, Object value) {
      super(key, value);
    }
  }

  @XmlRootElement(name = "symbolProperty")
  public static class SymbolCourseProperty
      extends CourseObjectProperty {

    /**
     * No-arg default constructor required by JAXB.
     */
    public SymbolCourseProperty() {
    }

    public SymbolCourseProperty(String key, Object value) {
      super(key, value);
    }
  }

  @XmlRootElement(name = "tripleProperty")
  public static class TripleCourseProperty
      extends CourseObjectProperty {

    /**
     * No-arg default constructor required by JAXB.
     */
    public TripleCourseProperty() {
    }

    public TripleCourseProperty(String key, Object value) {
      super(key, value);
    }
  }

  @XmlRootElement(name = "vehicleThemeProperty")
  public static class VehicleThemeCourseProperty
      extends CourseObjectProperty {

    /**
     * No-arg default constructor required by JAXB.
     */
    public VehicleThemeCourseProperty() {
    }

    public VehicleThemeCourseProperty(String key, Object value) {
      super(key, value);
    }
  }
}
