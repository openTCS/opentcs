/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs;

import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * Creates model elements that can be used in tests.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DataObjectFactory {

  /**
   * Prefix to use for points.
   */
  private String pointNamePrefix = "MyPoint-";
  /**
   * Prefix to use for paths.
   */
  private String pathNamePrefix = "MyPath-";
  /**
   * Prefix to use for vehicles.
   */
  private String vehicleNamePrefix = "MyVehicle-";
  /**
   * Prefix to use for location types.
   */
  private String locTypeNamePrefix = "MyLocType-";
  /**
   * Prefix to use for locations.
   */
  private String locationNamePrefix = "MyLocation-";
  /**
   * counter to create unique names.
   */
  private int uniqueIdCounter;

  /**
   * Creates a new instance.
   */
  public DataObjectFactory() {
  }

  /**
   * Sets the suffix used for points.
   *
   * @param pointNamePrefix The suffix used for points.
   */
  public void setPointNamePrefix(String pointNamePrefix) {
    this.pointNamePrefix = pointNamePrefix;
  }

  /**
   * Sets the prefix used for paths.
   *
   * @param pathNamePrefix The prefix used for paths.
   */
  public void setPathNamePrefix(String pathNamePrefix) {
    this.pathNamePrefix = pathNamePrefix;
  }

  /**
   * Sets the prefix used for vehicles.
   *
   * @param vehicleNamePrefix The prefix used for vehicles.
   */
  public void setVehicleNamePrefix(String vehicleNamePrefix) {
    this.vehicleNamePrefix = vehicleNamePrefix;
  }

  /**
   * Sets the prefix used for location types.
   *
   * @param locTypeNamePrefix The prefix used for location types.
   */
  public void setLocTypeNamePrefix(String locTypeNamePrefix) {
    this.locTypeNamePrefix = locTypeNamePrefix;
  }

  /**
   * Sets the prefix used for locations.
   *
   * @param locationNamePrefix The prefix used for locations.
   */
  public void setLocationNamePrefix(String locationNamePrefix) {
    this.locationNamePrefix = locationNamePrefix;
  }

  /**
   * Creates a point.
   *
   * @return A new point.
   */
  public Point createPoint() {
    ++uniqueIdCounter;
    return new Point(pointNamePrefix + uniqueIdCounter);
  }

  /**
   * Creates a path from a start point to an end point.
   *
   * @param srcRef Reference to the start point.
   * @param dstRef Reference to the end point.
   * @return A new path from the start point to the end point.
   */
  public Path createPath(TCSObjectReference<Point> srcRef,
                         TCSObjectReference<Point> dstRef) {
    ++uniqueIdCounter;
    return new Path(pathNamePrefix + uniqueIdCounter, srcRef, dstRef);
  }

  /**
   * Creates a path leading to a destination point.
   * Creates an anonymous start point.
   *
   * @param dstRef Reference to the destination point.
   * @return A new path from an anonymous start point to the end point.
   */
  public Path createPath(TCSObjectReference<Point> dstRef) {
    Point srcPoint = createPoint();
    ++uniqueIdCounter;
    return createPath(srcPoint.getReference(), dstRef);
  }

  /**
   * Creates a path with anonymous start and destination points.
   *
   * @return A new path with anonymous start and end points.
   */
  public Path createPath() {
    Point dstPoint = createPoint();
    ++uniqueIdCounter;
    return createPath(dstPoint.getReference());
  }

  /**
   * Creates a vehicle.
   *
   * @return A new vehicle.
   */
  public Vehicle createVehicle() {
    ++uniqueIdCounter;
    return new Vehicle(vehicleNamePrefix + uniqueIdCounter);
  }

  /**
   * Creates a location type.
   *
   * @return A new location type.
   */
  public LocationType createLocationType() {
    ++uniqueIdCounter;
    return new LocationType(locTypeNamePrefix + uniqueIdCounter);
  }

  /**
   * Creates a location.
   *
   * @param locTypeRef Reference to the location type to use for the new location.
   * @return A new location with the location type.
   */
  public Location createLocation(TCSObjectReference<LocationType> locTypeRef) {
    ++uniqueIdCounter;
    return new Location(locationNamePrefix + uniqueIdCounter, locTypeRef);
  }

  /**
   * Creates a location with an anonymous location type.
   *
   * @return A new location with an anonymous location type.
   */
  public Location createLocation() {
    LocationType locType = createLocationType();
    ++uniqueIdCounter;
    return new Location(locationNamePrefix + uniqueIdCounter, locType.getReference());
  }

}
