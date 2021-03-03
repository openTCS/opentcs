/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util;

import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.kernel.workingset.Model;
import org.opentcs.kernel.workingset.TCSObjectPool;

/**
 * A generator for some simple models.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class ModelGenerator {

  /**
   * Prevent creation of instances of this class.
   */
  private ModelGenerator() {
  }

  /**
   * Creates a model in which all points form a ring on which paths can be
   * travelled in forward direction only.
   * 
   * @param pointCount The number of points in the resulting model.
   * @param locationDivisor Indicates how many locations will be created. The
   * actual number of created locations will be (<code>pointCount</code> /
   * @return The created model
   * <code>locationDivisor</code>).
   */
  public static Model getRingModel(int pointCount, int locationDivisor) {
    if (pointCount < 2) {
      throw new IllegalArgumentException("pointCount is less than 2");
    }
    if (locationDivisor < 1) {
      throw new IllegalArgumentException("locationFrequency is less than 1");
    }
    TCSObjectPool globalObjectPool = new TCSObjectPool();
    Model model = new Model(globalObjectPool);
    LocationType transferLocationType = model.createLocationType(null);
    // Generate a ring of Point-Path-pairs.
    Point firstPoint = null;
    Point currentPoint;
    Point lastPoint = null;
    Location currentLocation = null;
    Path currentPath;
    try {
      globalObjectPool.renameObject(transferLocationType.getReference(),
                                    "TransferLocationType");
      for (int i = 0; i < pointCount; i++) {
        currentPoint = model.createPoint(null);
        globalObjectPool.renameObject(currentPoint.getReference(),
                                      "Point-" + i);
        if (i % locationDivisor == 0) {
          currentLocation =
              model.createLocation(null, transferLocationType.getReference());
          model.connectLocationToPoint(
              currentLocation.getReference(), currentPoint.getReference());
        }
        // If we just created the first point, save a reference to it for later.
        if (i == 0) {
          firstPoint = currentPoint;
        }
        // If the point just created was not the first one, create a path from
        // the previous one to it.
        else {
          currentPath = model.createPath(
              null, lastPoint.getReference(), currentPoint.getReference());
          model.setPathLength(currentPath.getReference(), 5000);
          model.setPathMaxVelocity(currentPath.getReference(), 1000);
        }
        // Save a reference to the point just created for the next round.
        lastPoint = currentPoint;
      }
      // Create a path from the ring's last point to its first one.
      currentPath = model.createPath(
          null, lastPoint.getReference(), firstPoint.getReference());
      model.setPathLength(currentPath.getReference(), 5000);
      model.setPathMaxVelocity(currentPath.getReference(), 1000);
    }
    catch (ObjectExistsException exc) {
      throw new IllegalStateException("Unexpected exception", exc);
    }
    return model;
  }

  /**
   * Creates a model in which all points form a ring on which paths can be
   * travelled in both forward and backward directions. 
   * 
   * @param pointCount The number of points in the resulting model.
   * @param locationDivisor Indicates how many locations will be created. The
   * actual number of created locations will be (<code>pointCount</code> /
   * @return The created model
   * <code>locationDivisor</code>).
   */
  public static Model getBidirectionalRingModel(int pointCount,
                                                int locationDivisor) {
    Model model = getRingModel(pointCount, locationDivisor);
    for (Path curPath : model.getPaths(null)) {
      model.setPathLength(curPath.getReference(), 5000);
      model.setPathMaxReverseVelocity(curPath.getReference(), 1000);
    }
    return model;
  }

  /**
   * Returns a new model in which the points form a grid and the paths form
   * cycles clockwise and counter-clockwise in turns; the returned model does
   * not contain any locations.
   * <p>
   * The following ASCII art visualizes the form of the returned model (in this
   * case, a model with three edges on the X axis and two edges on the Y axis):
   * </p>
   * <pre>
   * O-->0<--0-->0
   * ^   |   ^   |
   * |   |   |   |
   * |   v   |   v
   * O<--O-->O<--O
   * |   ^   |   ^
   * |   |   |   |
   * v   |   v   |
   * O-->O<--O-->O
   * </pre>
   * Four locations (all of the same type) are included in the model, each of
   * them linked to one of the corner points. The location links are not
   * assigned any allowed operations by default.
   * 
   * @param xEdges The number of edges on the X axis.
   * @param yEdges The number of edges on the Y axis.
   * @return The created model
   */
  public static Model getCircularGridModel(int xEdges, int yEdges) {
    if (xEdges < 1) {
      throw new IllegalArgumentException("xEdges is less than 1");
    }
    if (yEdges < 1) {
      throw new IllegalArgumentException("yEdges is less than 1");
    }
    TCSObjectPool globalObjectPool = new TCSObjectPool();
    Model model = new Model(globalObjectPool);
    Point[][] points = new Point[xEdges + 1][yEdges + 1];
    try {
      // Create the points.
      for (int x = 0; x <= xEdges; x++) {
        for (int y = 0; y <= yEdges; y++) {
          Point curPoint = model.createPoint(null);
          globalObjectPool.renameObject(curPoint.getReference(),
                                        "Point-" + x + "-" + y);
          model.setPointType(curPoint.getReference(), Point.Type.PARK_POSITION);
          points[x][y] = curPoint;
        }
      }
      // Add locations and link them at the corners.
      LocationType locType = model.createLocationType(null);
      Location location = model.createLocation(null, locType.getReference());
      model.connectLocationToPoint(
          location.getReference(), points[0][0].getReference());
      location = model.createLocation(null, locType.getReference());
      model.connectLocationToPoint(
          location.getReference(), points[xEdges][0].getReference());
      location = model.createLocation(null, locType.getReference());
      model.connectLocationToPoint(
          location.getReference(), points[xEdges][yEdges].getReference());
      location = model.createLocation(null, locType.getReference());
      model.connectLocationToPoint(
          location.getReference(), points[0][yEdges].getReference());
      // Add the paths between the points.
      for (int x = 0; x <= xEdges; x++) {
        for (int y = 0; y <= yEdges; y++) {
          boolean clockWise = (x + y) % 2 == 0;
          Point curPoint = points[x][y];
          // Add a path connecting the right neighbour if we're not at the right
          // border, yet.
          if (x < xEdges) {
            Point rightNeighbour = points[x + 1][y];
            Point srcPoint;
            Point destPoint;
            if (clockWise) {
              srcPoint = curPoint;
              destPoint = rightNeighbour;
            }
            else {
              srcPoint = rightNeighbour;
              destPoint = curPoint;
            }
            Path path = model.createPath(
                null, srcPoint.getReference(), destPoint.getReference());
            model.setPathLength(path.getReference(), 5000);
            model.setPathMaxVelocity(path.getReference(), 1000);
          }
          // Add a path connecting the lower neighbour if we're not at the lower
          // border, yet.
          if (y < yEdges) {
            Point lowerNeighbour = points[x][y + 1];
            Point srcPoint;
            Point destPoint;
            if (clockWise) {
              srcPoint = lowerNeighbour;
              destPoint = curPoint;
            }
            else {
              srcPoint = curPoint;
              destPoint = lowerNeighbour;
            }
            Path path = model.createPath(
                null, srcPoint.getReference(), destPoint.getReference());
            model.setPathLength(path.getReference(), 5000);
            model.setPathMaxVelocity(path.getReference(), 1000);
          }
        }
      }
    }
    catch (ObjectExistsException exc) {
      throw new IllegalStateException("Unexpected exception", exc);
    }
    return model;
  }

  /**
   *
   * <p>
   * The following ASCII art visualizes the form of the returned model (in this
   * case, a model with three edges both on the X and the Y axis):
   * </p>
   * <pre>
   * O-->0-->0-->0
   * ^   |   ^   |
   * |   |   |   |
   * |   v   |   v
   * O<--O<--O<--O
   * ^   |   ^   |
   * |   |   |   |
   * |   v   |   v
   * O-->O-->O-->O
   * ^   |   ^   |
   * |   |   |   |
   * |   v   |   v
   * O<--O<--O<--O
   * </pre>
   * Four locations (all of the same type) are included in the model, each of
   * them linked to one of the corner points. The location links are not
   * assigned any allowed operations by default.
   * 
   * @param xEdges The number of edges on the X axis.
   * @param yEdges The number of edges on the Y axis.
   * @return The created model
   */
  public static Model getCircularGridModel2(int xEdges, int yEdges) {
    if (xEdges < 1) {
      throw new IllegalArgumentException("xEdges is less than 1");
    }
    if (yEdges < 1) {
      throw new IllegalArgumentException("yEdges is less than 1");
    }
    TCSObjectPool globalObjectPool = new TCSObjectPool();
    Model model = new Model(globalObjectPool);
    Point[][] points = new Point[xEdges + 1][yEdges + 1];
    try {
      // Create the points.
      for (int x = 0; x <= xEdges; x++) {
        for (int y = 0; y <= yEdges; y++) {
          Point curPoint = model.createPoint(null);
          globalObjectPool.renameObject(curPoint.getReference(),
                                        "Point-" + x + "-" + y);
          model.setPointType(curPoint.getReference(), Point.Type.PARK_POSITION);
          points[x][y] = curPoint;
        }
      }
      // Add locations and link them at the corners.
      LocationType locType = model.createLocationType(null);
      model.addLocationTypeAllowedOperation(locType.getReference(), "LOAD");
      model.addLocationTypeAllowedOperation(locType.getReference(), "UNLOAD");
      Location location = model.createLocation(null, locType.getReference());
      model.connectLocationToPoint(
          location.getReference(), points[0][0].getReference());
      location = model.createLocation(null, locType.getReference());
      model.connectLocationToPoint(
          location.getReference(), points[xEdges][0].getReference());
      location = model.createLocation(null, locType.getReference());
      model.connectLocationToPoint(
          location.getReference(), points[xEdges][yEdges].getReference());
      location = model.createLocation(null, locType.getReference());
      model.connectLocationToPoint(
          location.getReference(), points[0][yEdges].getReference());
      // Add the paths between the points.
      for (int x = 0; x <= xEdges; x++) {
        boolean topDown = !(x % 2 == 0);
        for (int y = 0; y <= yEdges; y++) {
          boolean leftToRight = y % 2 == 0;
          Point curPoint = points[x][y];
          // Add a path connecting the right neighbour if we're not at the right
          // border, yet.
          if (x < xEdges) {
            Point rightNeighbour = points[x + 1][y];
            Point srcPoint;
            Point destPoint;
            if (leftToRight) {
              srcPoint = curPoint;
              destPoint = rightNeighbour;
            }
            else {
              srcPoint = rightNeighbour;
              destPoint = curPoint;
            }
            Path path = model.createPath(
                null, srcPoint.getReference(), destPoint.getReference());
            model.setPathLength(path.getReference(), 5000);
            model.setPathMaxVelocity(path.getReference(), 1000);
          }
          // Add a path connecting the lower neighbour if we're not at the lower
          // border, yet.
          if (y < yEdges) {
            Point lowerNeighbour = points[x][y + 1];
            Point srcPoint;
            Point destPoint;
            if (topDown) {
              srcPoint = curPoint;
              destPoint = lowerNeighbour;
            }
            else {
              srcPoint = lowerNeighbour;
              destPoint = curPoint;
            }
            Path path = model.createPath(
                null, srcPoint.getReference(), destPoint.getReference());
            model.setPathLength(path.getReference(), 5000);
            model.setPathMaxVelocity(path.getReference(), 1000);
          }
        }
      }
    }
    catch (ObjectExistsException exc) {
      throw new IllegalStateException("Unexpected exception", exc);
    }
    return model;
  }

  /**
   * XXX Incomplete.
   * <p>
   * The following ASCII art visualizes the form of the returned model (in this
   * case, a model with three edges both on the X and the Y axis):
   * </p>
   * <pre>
   * O<->0<->0<->0
   * ^   ^   ^   ^
   * |   |   |   |
   * v   v   v   v
   * O<->O<->O<->O
   * ^   ^   ^   ^
   * |   |   |   |
   * v   v   v   v
   * O<->O<->O<->O
   * ^   ^   ^   ^
   * |   |   |   |
   * v   v   v   v
   * O<->O<->O<->O
   * </pre>
   * 
   * @param xEdges The number of edges on the X axis.
   * @param yEdges The number of edges on the Y axis.
   * @return The created model.
   */
  public static Model getBidirectionalGridModel(int xEdges, int yEdges) {
    Model model = getCircularGridModel(xEdges, yEdges);
    for (Path curPath : model.getPaths(null)) {
      // XXX Instead, we might want to create a path for the opposite
      // direction.
      model.setPathMaxReverseVelocity(curPath.getReference(), 1000);
    }
    return model;
  }

  /**
   * Returns a model equivalent to the one described in <cite>Traffic Analysis
   * for Multiple AGV Systems</cite>.
   * 
   * @return The created model.
   */
  public static Model getThesisModel() {
    TCSObjectPool objectPool = new TCSObjectPool();
    Model model = new Model(objectPool);
    Point pointA = model.createPoint(null);
    Point pointB = model.createPoint(null);
    Point pointC = model.createPoint(null);
    Point pointD = model.createPoint(null);
    Point pointE = model.createPoint(null);
    Point pointF = model.createPoint(null);
    Path pathAC =
        model.createPath(null, pointA.getReference(), pointC.getReference());
    Path pathCE =
        model.createPath(null, pointC.getReference(), pointE.getReference());
    Path pathEA =
        model.createPath(null, pointE.getReference(), pointA.getReference());
    Path pathBF =
        model.createPath(null, pointB.getReference(), pointF.getReference());
    Path pathFD =
        model.createPath(null, pointF.getReference(), pointD.getReference());
    Path pathDB =
        model.createPath(null, pointD.getReference(), pointB.getReference());
    Path pathAB =
        model.createPath(null, pointA.getReference(), pointB.getReference());
    Path pathFE =
        model.createPath(null, pointF.getReference(), pointE.getReference());
    for (Path curPath : model.getPaths(null)) {
      model.setPathLength(curPath.getReference(), 1000);
    }
    return model;
  }
}
