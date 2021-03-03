/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.plugins.panels.statistics;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import org.opentcs.util.statistics.StatisticsRecord;

/**
 * Aggregates statistics records.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
final class AnalysisData {

  /**
   * The first time stamp in the log.
   */
  private final long startTimestamp;
  /**
   * The last time stamp in the log.
   */
  private final long finishTimestamp;
  /**
   * All known vehicles, mapped by their names.
   */
  private final Map<String, VehicleStats> vehiclesByName = new TreeMap<>();
  /**
   * All known points, referenced by their names.
   */
  private final Map<String, PointStats> pointsByName = new TreeMap<>();
  /**
   * All known orders, referenced by their names.
   */
  private final Map<String, OrderStats> ordersByName = new TreeMap<>();

  /**
   * Creates a new instance.
   *
   * @param startTimestamp The first time stamp.
   * @param finishTimestamp The last time stamp.
   */
  private AnalysisData(final long startTimestamp, final long finishTimestamp) {
    this.startTimestamp = startTimestamp;
    this.finishTimestamp = finishTimestamp;
  }

  /**
   * Returns the total runtime between the first and last recorded time stamp.
   *
   * @return The total runtime between the first and last recorded time stamp.
   */
  public long getTotalRuntime() {
    return finishTimestamp - startTimestamp;
  }

  /**
   * Returns a list of statstics data for all vehicles.
   *
   * @return A list of statstics data for all vehicles.
   */
  public List<VehicleStats> getVehicles() {
    return new LinkedList<>(vehiclesByName.values());
  }

  /**
   * Returns a list of statstics data for all points.
   *
   * @return A list of statstics data for all points.
   */
  public List<PointStats> getPoints() {
    return new LinkedList<>(pointsByName.values());
  }

  /**
   * Returns a list of statstics data for all orders.
   *
   * @return A list of statstics data for all orders.
   */
  public List<OrderStats> getOrders() {
    return new LinkedList<>(ordersByName.values());
  }

  /**
   * Aggregates the given list of statistics records in an
   * <code>AnalysisData</code> instance and returns it.
   *
   * @param records The list of statistics records to be aggregated.
   * @return The instance containing the aggregated data, or <code>null</code>,
   * if the given list of records is empty.
   */
  public static AnalysisData analyzeRecords(List<StatisticsRecord> records) {
    Objects.requireNonNull(records, "records is null");
    if (records.isEmpty()) {
      return null;
    }

    long startTime = records.get(0).getTimestamp();
    long finishTime = records.get(records.size() - 1).getTimestamp();
    AnalysisData analysisData = new AnalysisData(startTime, finishTime);

    for (StatisticsRecord curRecord : records) {
      handleRecord(curRecord, analysisData);
    }

    // For sensible analysis results, assume all processes ended.
    for (VehicleStats curVehicle : analysisData.getVehicles()) {
      curVehicle.stopCharging(finishTime);
      curVehicle.stopProcessingOrder(finishTime);
    }
    for (PointStats curPoint : analysisData.getPoints()) {
      curPoint.stopOccupation(finishTime);
    }
    for (OrderStats curOrder : analysisData.getOrders()) {
      if (curOrder.getFinishedTime() <= 0) {
        curOrder.finish(finishTime, true);
      }
    }

    return analysisData;
  }

  /**
   * Processes the given record and adds its data to the given instance.
   *
   * @param record The record to be processed.
   * @param analysisData The instance to contain the data extracted from the
   * record.
   */
  private static void handleRecord(StatisticsRecord record,
                                   AnalysisData analysisData) {
    assert record != null;

    switch (record.getEvent()) {
      case VEHICLE_STARTS_PROCESSING:
        analysisData.getVehicle(
            record.getLabel()).startProcessingOrder(record.getTimestamp());
        break;
      case VEHICLE_STOPS_PROCESSING:
        analysisData.getVehicle(
            record.getLabel()).stopProcessingOrder(record.getTimestamp());
        break;
      case VEHICLE_STARTS_CHARGING:
        analysisData.getVehicle(
            record.getLabel()).startCharging(record.getTimestamp());
        break;
      case VEHICLE_STOPS_CHARGING:
        analysisData.getVehicle(
            record.getLabel()).stopCharging(record.getTimestamp());
        break;
      case VEHICLE_STARTS_WAITING:
        analysisData.getVehicle(
            record.getLabel()).startWaiting(record.getTimestamp());
        break;
      case VEHICLE_STOPS_WAITING:
        analysisData.getVehicle(
            record.getLabel()).stopWaiting(record.getTimestamp());
        break;
      case POINT_OCCUPIED:
        analysisData.getPoint(
            record.getLabel()).startOccupation(record.getTimestamp());
        break;
      case POINT_FREED:
        analysisData.getPoint(
            record.getLabel()).stopOccupation(record.getTimestamp());
        break;
      case ORDER_ACTIVATED:
        analysisData.getOrder(
            record.getLabel()).activate(record.getTimestamp());
        break;
      case ORDER_ASSIGNED:
        analysisData.getOrder(record.getLabel()).assign(record.getTimestamp());
        break;
      case ORDER_FINISHED_SUCC:
        analysisData.getOrder(
            record.getLabel()).finish(record.getTimestamp(), true);
        break;
      case ORDER_FINISHED_FAIL:
        analysisData.getOrder(
            record.getLabel()).finish(record.getTimestamp(), false);
        break;
      case ORDER_CROSSED_DEADLINE:
        analysisData.getOrder(record.getLabel()).crossDeadline();
        break;
      default:
    }
  }

  /**
   * Returns the statistics data for the order with the given name, creating
   * a new object if necessary.
   *
   * @param name The name of the order for which to return the data.
   * @return The statistics data for the order with the given name, creating
   * a new object if necessary.
   */
  private OrderStats getOrder(String name) {
    assert name != null;

    OrderStats order = ordersByName.get(name);
    if (order == null) {
      order = new OrderStats(name, getTotalRuntime());
      ordersByName.put(name, order);
    }
    return order;
  }

  /**
   * Returns the statistics data for the point with the given name, creating
   * a new object if necessary.
   *
   * @param name The name of the point for which to return the data.
   * @return The statistics data for the point with the given name, creating
   * a new object if necessary.
   */
  private PointStats getPoint(String name) {
    assert name != null;

    PointStats point = pointsByName.get(name);
    if (point == null) {
      point = new PointStats(name, getTotalRuntime());
      pointsByName.put(name, point);
    }
    return point;
  }

  /**
   * Returns the statistics data for the vehicle with the given name, creating
   * a new object if necessary.
   *
   * @param name The name of the vehicle for which to return the data.
   * @return The statistics data for the vehicle with the given name, creating
   * a new object if necessary.
   */
  private VehicleStats getVehicle(String name) {
    assert name != null;

    VehicleStats vehicle = vehiclesByName.get(name);
    if (vehicle == null) {
      vehicle = new VehicleStats(name, getTotalRuntime());
      vehiclesByName.put(name, vehicle);
    }
    return vehicle;
  }
}
