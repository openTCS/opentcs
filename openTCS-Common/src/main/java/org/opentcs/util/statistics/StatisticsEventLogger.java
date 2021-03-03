/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.statistics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.util.QueueProcessor;
import org.opentcs.util.eventsystem.TCSEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes received events to a file.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class StatisticsEventLogger
    extends QueueProcessor<TCSEvent> {

  /**
   * This class's logger.
   */
  private static final Logger log =
      LoggerFactory.getLogger(StatisticsEventLogger.class);
  /**
   * The output file.
   */
  private final File outputFile;
  /**
   * Our output sink.
   */
  private final PrintWriter outputWriter;

  /**
   * Creates a new instance.
   * 
   * @param outputFile The file to append log output to.
   * @throws IOException If there was a problem opening a <code>Writer</code>
   * for the given file.
   */
  public StatisticsEventLogger(final File outputFile)
      throws IOException {
    this.outputFile = Objects.requireNonNull(outputFile, "outputFile is null");
    outputWriter = new PrintWriter(new FileWriter(outputFile, true), true);
  }

  @Override
  protected void processQueueElement(TCSEvent element) {
    if (isTerminated()) {
      log.warn("Cannot process element, terminated already.");
      return;
    }
    if (element instanceof TCSObjectEvent) {
      processObjectEvent((TCSObjectEvent) element);
    }
  }

  @Override
  protected void terminated() {
    outputWriter.close();
    // If we did not write anything to the file, remove it again for the sake of
    // a cleaner hard drive.
    if (outputFile.length() == 0) {
      outputFile.delete();
    }
  }

  /**
   * Processes a {@link TCSObjectEvent} and logs it if it is interesting.
   *
   * @param event The event to be processed.
   */
  private void processObjectEvent(TCSObjectEvent event) {
    assert event != null : "event is null";

    TCSObject<?> object = event.getCurrentOrPreviousObjectState();
    if (object instanceof TransportOrder) {
      processOrderEvent(event);
    }
    else if (object instanceof Vehicle) {
      processVehicleEvent(event);
    }
    else if (object instanceof Point) {
      processPointEvent(event);
    }
  }

  /**
   * Processes an event for a {@link TransportOrder} if it is interesting.
   *
   * @param event The event to be processed.
   */
  private void processOrderEvent(TCSObjectEvent event) {
    assert event != null : "event is null";

    if (event.getPreviousObjectState() == null
        || event.getCurrentObjectState() == null) {
      // We cannot compare two states to find out what happened - ignore.
      return;
    }

    TransportOrder orderOld = (TransportOrder) event.getPreviousObjectState();
    TransportOrder orderNow = (TransportOrder) event.getCurrentObjectState();

    // Has the order been activated?
    if (orderNow.hasState(TransportOrder.State.ACTIVE)
        && !orderOld.hasState(TransportOrder.State.ACTIVE)) {
      writeEvent(StatisticsEvent.ORDER_ACTIVATED, orderNow.getName());
    }
    // Has the order been assigned to a vehicle?
    if (orderNow.hasState(TransportOrder.State.BEING_PROCESSED)
        && !orderOld.hasState(TransportOrder.State.BEING_PROCESSED)) {
      writeEvent(StatisticsEvent.ORDER_ASSIGNED, orderNow.getName());
    }
    // Has the order been finished?
    if (orderNow.hasState(TransportOrder.State.FINISHED)
        && !orderOld.hasState(TransportOrder.State.FINISHED)) {
      writeEvent(StatisticsEvent.ORDER_FINISHED_SUCC, orderNow.getName());
      // Check the order's deadline. Has it been crossed?
      if (orderNow.getFinishedTime() > orderNow.getDeadline()) {
        writeEvent(StatisticsEvent.ORDER_CROSSED_DEADLINE, orderNow.getName());
      }
    }
    // Has the order failed?
    if (orderNow.hasState(TransportOrder.State.FAILED)
        && !orderOld.hasState(TransportOrder.State.FAILED)) {
      writeEvent(StatisticsEvent.ORDER_FINISHED_FAIL, orderNow.getName());
    }
  }

  /**
   * Processes an event for a {@link Vehicle} if it is intersting.
   *
   * @param event The event to be processed.
   */
  private void processVehicleEvent(TCSObjectEvent event) {
    assert event != null : "event is null";

    if (event.getPreviousObjectState() == null
        || event.getCurrentObjectState() == null) {
      // We cannot compare two states to find out what happened - ignore.
      return;
    }

    Vehicle vehicleOld = (Vehicle) event.getPreviousObjectState();
    Vehicle vehicleNow = (Vehicle) event.getCurrentObjectState();

    // Did the vehicle get a transport order?
    if (vehicleNow.getTransportOrder() != null
        && vehicleOld.getTransportOrder() == null) {
      writeEvent(StatisticsEvent.VEHICLE_STARTS_PROCESSING,
                 vehicleNow.getName());
    }
    // Did the vehicle finish a transport order?
    if (vehicleNow.getTransportOrder() == null
        && vehicleOld.getTransportOrder() != null) {
      writeEvent(StatisticsEvent.VEHICLE_STOPS_PROCESSING,
                 vehicleNow.getName());
    }
    // Did the vehicle start charging?
    if (vehicleNow.hasState(Vehicle.State.CHARGING)
        && !vehicleOld.hasState(Vehicle.State.CHARGING)) {
      writeEvent(StatisticsEvent.VEHICLE_STARTS_CHARGING,
                 vehicleNow.getName());
    }
    // Did the vehicle start charging?
    if (!vehicleNow.hasState(Vehicle.State.CHARGING)
        && vehicleOld.hasState(Vehicle.State.CHARGING)) {
      writeEvent(StatisticsEvent.VEHICLE_STOPS_CHARGING,
                 vehicleNow.getName());
    }
    // If the vehicle is processing an order AND is not in state EXECUTING AND
    // it was either EXECUTING before or not processing, yet, consider it being
    // blocked.
    if (vehicleNow.hasProcState(Vehicle.ProcState.PROCESSING_ORDER)
        && !vehicleNow.hasState(Vehicle.State.EXECUTING)
        && (vehicleOld.hasState(Vehicle.State.EXECUTING)
            || !vehicleOld.hasProcState(Vehicle.ProcState.PROCESSING_ORDER))) {
      writeEvent(StatisticsEvent.VEHICLE_STARTS_WAITING,
                 vehicleNow.getName());
    }
    // Is the vehicle processing an order AND has its state changed from
    // something else to EXECUTING? - Consider it not blocked any more, then.
    if (vehicleNow.hasProcState(Vehicle.ProcState.PROCESSING_ORDER)
        && vehicleNow.hasState(Vehicle.State.EXECUTING)
        && !vehicleOld.hasState(Vehicle.State.EXECUTING)) {
      writeEvent(StatisticsEvent.VEHICLE_STOPS_WAITING,
                 vehicleNow.getName());
    }
  }

  /**
   * Processes an event for a {@link Point} if it is interesting.
   *
   * @param event The event to be processed.
   */
  private void processPointEvent(TCSObjectEvent event) {
    assert event != null : "event is null";

    if (event.getPreviousObjectState() == null
        || event.getCurrentObjectState() == null) {
      // We cannot compare two states to find out what happened - ignore.
      return;
    }

    Point pointOld = (Point) event.getPreviousObjectState();
    Point pointNow = (Point) event.getCurrentObjectState();

    // Did a vehicle move to this point?
    if (pointNow.getOccupyingVehicle() != null
        && pointOld.getOccupyingVehicle() == null) {
      writeEvent(StatisticsEvent.POINT_OCCUPIED, pointNow.getName());
    }
    // Did a vehicle move off this point?
    if (pointNow.getOccupyingVehicle() == null
        && pointOld.getOccupyingVehicle() != null) {
      writeEvent(StatisticsEvent.POINT_FREED, pointNow.getName());
    }
  }

  /**
   * Logs an event for the named object.
   *
   * @param event The event to be logged.
   * @param objectName The name of the object for which the event happened.
   */
  private void writeEvent(StatisticsEvent event, String objectName) {
    assert event != null : "event is null";
    assert objectName != null : "objectName is null";

    StatisticsRecord record = new StatisticsRecord(System.currentTimeMillis(),
                                                   event,
                                                   objectName);
    outputWriter.println(record.toString());
  }
}
