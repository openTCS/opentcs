/*
 * openTCS copyright information:
 * Copyright (c) 2011 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.module.parking;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.opentcs.access.LocalKernel;
import org.opentcs.algorithms.Router;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;

/**
 * A parking strategy that finds a highest prioritated or the closest parking position,
 * depending whether there are any priorities defined.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
class PriorityParkingStrategy
    extends AbstractParkingStrategy {

  /**
   * The property key for assigning parking positions a priority group.
   */
  public static final String priorityGroupPropKey = "tcs:parkPriorityGroup";
  /**
   * The property key for assigning parking positions a priority.
   */
  public static final String priorityPropKey = "tcs:parkPriority";
  /**
   * This class's Logger.
   */
  private static final Logger log
      = Logger.getLogger(PriorityParkingStrategy.class.getName());
  /**
   * bidirectional relation between Groups and Points
   * priority in an Integer form
   * ParkingGroup haven a priority as a parameters.
   */
  private final Map<Integer, ParkingGroup> parkingGroups;

  /**
   * Creates a new PriorityParkingStrategy.
   *
   * @param kernel The kernel
   * @param router The router
   */
  @Inject
  public PriorityParkingStrategy(final LocalKernel kernel, final Router router) {
    super(kernel, router);

    parkingGroups = new TreeMap<>();

    for (Point point : getParkingPositions().keySet()) {
      //store the propertie of the Point in variable result
      String result = point.getProperties().get(priorityGroupPropKey);
      Integer groupID = 0;
      if (result == null) {
        //set the priority on minimal
        groupID = Integer.MAX_VALUE;
      }
      else {
        try {
          //overwrite groupID with the value of the string result
          groupID = Integer.valueOf(result);
        }
        catch (NumberFormatException e) {
          groupID = Integer.MAX_VALUE;
        }
      }
      ParkingGroup group = parkingGroups.get(groupID);
      if (group == null) {
        //if the Group not existing, create new group and set a groupID
        group = new ParkingGroup(groupID);
        //add the newly created group to a Map
        parkingGroups.put(groupID, group);
      }
      /**
       * create new group entry
       * add the newly created group entry to the group
       */
      GroupEntry gEntry = new GroupEntry(point, group);
      group.groupEntries.add(gEntry);

    }

  }

  /**
   * Returns the closest or highest prioritated parking position.
   *
   * @param vehicle The vehicle we're searching a parking position for
   * @return The best parking position
   */
  @Override
  public Point getParkingPosition(final Vehicle vehicle) {
    if (vehicle == null) {
      throw new NullPointerException("vehicle is null");
    }
    if (vehicle.getCurrentPosition() == null) {
      throw new IllegalArgumentException("vehicle's position unknown");
    }

    boolean finished;
    Point currentPoint = null;
    Set<Point> samePrioPoints = null;
    Set<Point> targetedPointsCopySet = getRouter().getTargetedPoints();
    log.info(parkingGroups.toString());

    for (ParkingGroup group : parkingGroups.values()) {
      //iterate over all entries in this group
      Iterator itr = group.groupEntries.iterator();
      samePrioPoints = new HashSet<>();
      currentPoint = null;
      finished = false;

      while (itr.hasNext() && !finished) {
        GroupEntry tmp = (GroupEntry) itr.next();
        Point pointToCompare = getKernel().getTCSObject(Point.class,
                                                        tmp.point.getReference());

        //if current parking position is available
        if (pointToCompare.getOccupyingVehicle() == null
            && !targetedPointsCopySet.contains(pointToCompare)) {
          //if the set is empty take this point as object of comparison
          if (samePrioPoints.isEmpty()
              && pointToCompare.getProperties().get(priorityPropKey) != null) {
            samePrioPoints.add(pointToCompare);
            currentPoint = pointToCompare;
          }
          else //if point to be compared has same priority as our 
          //object of comparison add it to our set
          if (pointToCompare.getProperties().get(priorityPropKey) != null
              && pointToCompare.getProperties().get(priorityPropKey).
              equals(currentPoint.getProperties().get(priorityPropKey))) {
            samePrioPoints.add(pointToCompare);
          }
          else {
            //if current point doesn't have the same priority as our
            //object of comparison we've found a point that has lower
            //priority, so we end the loop
            finished = true;
          }

        }
      }
      //if set is empty no parking position is availabe in this group
      if (!samePrioPoints.isEmpty()) {
        //if size is one return our only position
        if (samePrioPoints.size() == 1) {
          return currentPoint;
        }
        else {
          //return the closest position to the position of our vehicle
          return nearestPoint(vehicle, samePrioPoints);
        }
      }

      //if this groups points dont have any priorities we return
      //the nearest parking position to our vehicle
      Point current = nearestPoint(vehicle, group.toSet());
      if (current != null) {
        return current;
      }

    }
    return null;
  }

  /**
   * If a vehicle is already parked this method checks if there is a
   * better parking position (higher priority) available.
   *
   * @param vehicle The vehicle we're searching a better parking position for.
   * @return A new position if a better one is found, current position otherwise
   */
  public Point getNewParkingPosition(final Vehicle vehicle) {

    // This method exists for further enhancements of this parking strategy.
    // It's not being used right now
    if (vehicle == null) {
      return null;
    }
    Point currentPos = getKernel().getTCSObject(Point.class,
                                                vehicle.getCurrentPosition());
    assert currentPos != null;
    Point newPos = getParkingPosition(vehicle);

    if (newPos == null) {
      return currentPos;
    }

    int newPosPrio = Integer.MAX_VALUE;
    int currentPosPrio = Integer.MAX_VALUE;
    int newGrpPosPrio = Integer.MAX_VALUE;
    int currentGrpPosPrio = Integer.MAX_VALUE;

    if (newPos.getProperties().get(priorityPropKey) != null) {
      try {
        newPosPrio = Integer.parseInt(newPos.getProperties().
            get(priorityPropKey));
        newGrpPosPrio = Integer.parseInt(newPos.getProperties().
            get(priorityGroupPropKey));
      }
      catch (NumberFormatException e) {
        newPosPrio = Integer.MAX_VALUE;
        newGrpPosPrio = Integer.MAX_VALUE;
      }
    }

    if (currentPos.getProperties().get(priorityPropKey) != null) {
      try {
        currentPosPrio = Integer.parseInt(currentPos.getProperties().
            get(priorityPropKey));
        currentGrpPosPrio = Integer.parseInt(currentPos.getProperties().
            get(priorityGroupPropKey));
      }
      catch (NumberFormatException e) {
        currentPosPrio = Integer.MAX_VALUE;
        currentGrpPosPrio = Integer.MAX_VALUE;
      }
    }

    // if the group priority of the new position is lower or equal to the current
    // point AND the priority of the new point is lower, then this point
    // is returned
    if (newGrpPosPrio <= currentGrpPosPrio && newPosPrio < currentPosPrio) {
      return newPos;
    }
    else {
      return currentPos;
    }
  }

  /**
   * Parking Group as Data Structure.
   */
  private class ParkingGroup {

    /**
     * Priority Of Parking Group.
     */
    private final int priority;
    /**
     * Entries in this parking group.
     */
    private final PriorityQueue<GroupEntry> groupEntries
        = new PriorityQueue<>(1, new ComparatorGroupEntry());

    /**
     * Creates a new parking group.
     *
     * @param priority The priority this group should have
     */
    public ParkingGroup(int priority) {
      this.priority = priority;
    }

    @Override
    public String toString() {
      String s = " ParkingGroup with Priority " + priority + "( ";
      for (GroupEntry gEntry : groupEntries) {
        s += "" + "Entrie " + gEntry.toString() + " ";
      }
      s += ")";

      return s;
    }

    /**
     * Returns all points of this group in a set.
     *
     * @return All points
     */
    public Set<Point> toSet() {
      Set<Point> pointsOfGroup = new HashSet<>();
      for (GroupEntry cur : groupEntries) {
        pointsOfGroup.add(cur.point);
      }

      return pointsOfGroup;
    }
  }

  /**
   * Group entry as Data structure.
   */
  private static final class GroupEntry {

    /**
     * The point this entry represents.
     */
    private final Point point;
    /**
     * Priority of point.
     */
    private final int priority;
    /**
     * The parking group this entry belongs to.
     */
    private final ParkingGroup pGroup;

    /**
     * Creates a new GroupEntry.
     *
     * @param point The point this entry represents.
     * @param parkingGroup The parking group this entry belongs to
     */
    private GroupEntry(Point point, ParkingGroup parkingGroup) {
      this.point = point;
      this.pGroup = parkingGroup;
      String result = point.getProperties().get(priorityPropKey);
      int tmp = 0;
      if (result == null) {
        tmp = Integer.MAX_VALUE;
      }
      else {
        try {
          tmp = Integer.valueOf(result);
        }
        catch (NumberFormatException e) {
          tmp = Integer.MAX_VALUE;
        }
      }
      priority = tmp;
    }

    @Override
    public String toString() {
      return point.getName();
    }
  }

  /**
   * Group entry Comparator.
   */
  private static final class ComparatorGroupEntry
      implements Comparator<GroupEntry> {

    /**
     * Creates a new instance.
     */
    private ComparatorGroupEntry() {
      // Do nada.
    }

    @Override
    public int compare(GroupEntry pt1, GroupEntry pt2) {
      int result = 0;
      int pt1Value = pt1.priority;
      int pt2Value = pt2.priority;

      if (pt1Value > pt2Value) {
        result = 1;
      }
      else if (pt1Value < pt2Value) {
        result = -1;
      }
      else {
        result = 0;
      }
      return result;
    }
  }
}
