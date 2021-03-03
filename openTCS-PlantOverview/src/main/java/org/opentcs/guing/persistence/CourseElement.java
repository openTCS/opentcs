/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.persistence;

import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import org.opentcs.guing.persistence.CourseElement.Block;
import org.opentcs.guing.persistence.CourseElement.Group;
import org.opentcs.guing.persistence.CourseElement.Layout;
import org.opentcs.guing.persistence.CourseElement.Link;
import org.opentcs.guing.persistence.CourseElement.Location;
import org.opentcs.guing.persistence.CourseElement.LocationType;
import org.opentcs.guing.persistence.CourseElement.OtherGraphicalElement;
import org.opentcs.guing.persistence.CourseElement.Path;
import org.opentcs.guing.persistence.CourseElement.Point;
import org.opentcs.guing.persistence.CourseElement.StaticRoute;
import org.opentcs.guing.persistence.CourseElement.Vehicle;

/**
 * JAXB classes for ModelComponents.
 *
 * @author Tobias Marquardt (Fraunhofer IML)
 */
@XmlSeeAlso({Block.class,
             Group.class,
             Layout.class,
             Link.class,
             Location.class,
             LocationType.class,
             OtherGraphicalElement.class,
             Path.class,
             Point.class,
             StaticRoute.class,
             Vehicle.class})
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class CourseElement {

  /**
   * The name that will be displayed in the tree view.
   */
  @XmlElement(required = true, name = "treeViewName")
  private String treeViewName;
  /**
   * The object properties of the model.
   */
  @XmlElement(required = false, name = "property")
  private Set<CourseObjectProperty> properties;

  public String getTreeViewName() {
    return treeViewName;
  }

  public void setTreeViewName(String treeViewName) {
    this.treeViewName = treeViewName;
  }

  public Set<CourseObjectProperty> getProperties() {
    return properties;
  }

  public void setProperties(Set<CourseObjectProperty> properties) {
    this.properties = properties;
  }

  @XmlRootElement(name = "Point")
  public static class Point
      extends CourseElement {

    public Point() {
      // Nothing to do here...
    }
  }

  @XmlRootElement(name = "Path")
  public static class Path
      extends CourseElement {

    public Path() {
    }
  }

  @XmlRootElement(name = "Block")
  public static class Block
      extends CourseElement {

    public Block() {
    }
  }

  @XmlRootElement(name = "Group")
  public static class Group
      extends CourseElement {

    public Group() {
    }
  }

  @XmlRootElement(name = "Layout")
  public static class Layout
      extends CourseElement {

    public Layout() {
    }
  }

  @XmlRootElement(name = "Link")
  public static class Link
      extends CourseElement {

    public Link() {
    }
  }

  @XmlRootElement(name = "Location")
  public static class Location
      extends CourseElement {

    public Location() {
    }
  }

  @XmlRootElement(name = "LocationType")
  public static class LocationType
      extends CourseElement {

    public LocationType() {
    }
  }

  @XmlRootElement(name = "OtherGraphicalElement")
  public static class OtherGraphicalElement
      extends CourseElement {

    public OtherGraphicalElement() {
    }
  }

  @XmlRootElement(name = "StaticRoute")
  public static class StaticRoute
      extends CourseElement {

    public StaticRoute() {
    }
  }

  @XmlRootElement(name = "Vehicle")
  public static class Vehicle
      extends CourseElement {

    public Vehicle() {
    }
  }
}
