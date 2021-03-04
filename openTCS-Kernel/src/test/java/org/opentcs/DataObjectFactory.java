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

  private String pointNamePrefix = "MyPoint-";

  private String pathNamePrefix = "MyPath-";

  private String vehicleNamePrefix = "MyVehicle-";

  private String locTypeNamePrefix = "MyLocType-";

  private String locationNamePrefix = "MyLocation-";

  private int uniqueIdCounter;

  public DataObjectFactory() {
  }

  public void setPointNamePrefix(String pointNamePrefix) {
    this.pointNamePrefix = pointNamePrefix;
  }

  public void setPathNamePrefix(String pathNamePrefix) {
    this.pathNamePrefix = pathNamePrefix;
  }

  public void setVehicleNamePrefix(String vehicleNamePrefix) {
    this.vehicleNamePrefix = vehicleNamePrefix;
  }

  public void setLocTypeNamePrefix(String locTypeNamePrefix) {
    this.locTypeNamePrefix = locTypeNamePrefix;
  }

  public void setLocationNamePrefix(String locationNamePrefix) {
    this.locationNamePrefix = locationNamePrefix;
  }

  public Point createPoint() {
    ++uniqueIdCounter;
    return new Point(pointNamePrefix + uniqueIdCounter);
  }

  public Path createPath(TCSObjectReference<Point> srcRef,
                         TCSObjectReference<Point> dstRef) {
    ++uniqueIdCounter;
    return new Path(pathNamePrefix + uniqueIdCounter, srcRef, dstRef);
  }

  public Path createPath(TCSObjectReference<Point> dstRef) {
    Point srcPoint = createPoint();
    ++uniqueIdCounter;
    return createPath(srcPoint.getReference(), dstRef);
  }

  public Path createPath() {
    Point dstPoint = createPoint();
    ++uniqueIdCounter;
    return createPath(dstPoint.getReference());
  }

  public Vehicle createVehicle() {
    ++uniqueIdCounter;
    return new Vehicle(vehicleNamePrefix + uniqueIdCounter);
  }

  public LocationType createLocationType() {
    ++uniqueIdCounter;
    return new LocationType(locTypeNamePrefix + uniqueIdCounter);
  }

  public Location createLocation(TCSObjectReference<LocationType> locTypeRef) {
    ++uniqueIdCounter;
    return new Location(locationNamePrefix + uniqueIdCounter, locTypeRef);
  }

  public Location createLocation() {
    LocationType locType = createLocationType();
    ++uniqueIdCounter;
    return new Location(locationNamePrefix + uniqueIdCounter, locType.getReference());
  }

}
