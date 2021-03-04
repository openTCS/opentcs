/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.documentation;

import static com.google.common.base.Preconditions.checkArgument;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.extensions.xmlhost.orders.binding.Destination;
import org.opentcs.kernel.extensions.xmlhost.orders.binding.Property;
import org.opentcs.kernel.extensions.xmlhost.orders.binding.ScriptResponse;
import org.opentcs.kernel.extensions.xmlhost.orders.binding.TCSOrderSet;
import org.opentcs.kernel.extensions.xmlhost.orders.binding.TCSResponseSet;
import org.opentcs.kernel.extensions.xmlhost.orders.binding.Transport;
import org.opentcs.kernel.extensions.xmlhost.orders.binding.TransportResponse;
import org.opentcs.kernel.extensions.xmlhost.orders.binding.TransportScript;
import org.opentcs.kernel.extensions.xmlhost.status.binding.OrderStatusMessage;
import org.opentcs.kernel.extensions.xmlhost.status.binding.TCSStatusMessageSet;
import org.opentcs.kernel.extensions.xmlhost.status.binding.VehicleStatusMessage;
import org.slf4j.LoggerFactory;

/**
 * Generates the sample xml files for the documentation.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 */
public class SamplesGenerator {

  /**
   * Prevents external instantiation.
   */
  private SamplesGenerator() {
  }

  public static void main(String[] args)
      throws IOException {
    checkArgument(args.length >= 2, "Need at least two argument for the task and target file");

    GeneratorTask task = GeneratorTask.valueOf(args[0]);
    File file = new File(args[1]);

    switch (task) {
      case TELEGRAM_TO_TWO_ORDERS: {
        generateTwoTransportOrderSample(file);
        break;
      }
      case TELEGRAM_TO_STATUS: {
        generateTelegramStatusSample(file);
        break;
      }
      case TELEGRAM_TO_RECEIPT_ORDERS: {
        generateResponseSetSample(file);
        break;
      }
      case TELEGRAM_TO_RECEIPT_BATCH: {
        generateScriptResponseSetSample(file);
        break;
      }
      case TELEGRAM_TO_BATCH: {
        generateTransportScriptOrderSample(file);
        break;
      }
      case TELEGRAM_VEHICLE_STATUS: {
        generateVehicleStatusSample(file);
        break;
      }
      default:
        throw new IllegalArgumentException("Unhandled task: " + task.name());
    }
  }

  /**
   * Generates the transport script order sample.
   *
   * @param directory the output directory
   */
  private static void generateTransportScriptOrderSample(File file)
      throws IOException {
    createFile(file);
    TCSOrderSet orderSet = new TCSOrderSet();

    TransportScript transportScript = new TransportScript();
    transportScript.setId("test.tcs");
    transportScript.setFileName("test.tcs");

    orderSet.getOrders().add(transportScript);

    Writer writer = new StringWriter();
    orderSet.toXml(writer);
    String xmlOutput = writer.toString();
    storeInFile(xmlOutput, file);
  }

  /**
   * Generates the script response set sample.
   *
   * @param directory the output directory
   */
  private static void generateScriptResponseSetSample(File file)
      throws IOException {
    createFile(file);
    TCSResponseSet responseSet = new TCSResponseSet();

    ScriptResponse response = new ScriptResponse();
    response.setId("test.tcs");
    response.setParsingSuccessful(true);

    TransportResponse transportResponse = new TransportResponse();
    transportResponse.setId("test.tcs");
    transportResponse.setOrderName("TOrder-0003");
    transportResponse.setExecutionSuccessful(true);

    response.getTransports().add(transportResponse);

    transportResponse = new TransportResponse();
    transportResponse.setId("test.tcs");
    transportResponse.setOrderName("TOrder-0004");
    transportResponse.setExecutionSuccessful(true);

    response.getTransports().add(transportResponse);

    responseSet.getResponses().add(response);

    Writer writer = new StringWriter();
    responseSet.toXml(writer);
    String xmlOutput = writer.toString();
    storeInFile(xmlOutput, file);
  }

  /**
   * Generates the response set sample.
   *
   * @param directory the output directory
   */
  private static void generateResponseSetSample(File file)
      throws IOException {
    createFile(file);
    TCSResponseSet responseSet = new TCSResponseSet();

    TransportResponse response = new TransportResponse();
    response.setId("TransportOrder-01");
    response.setOrderName("TOrder-0001");
    response.setExecutionSuccessful(true);

    responseSet.getResponses().add(response);

    response = new TransportResponse();
    response.setId("TransportOrder-02");
    response.setOrderName("TOrder-0002");
    response.setExecutionSuccessful(true);

    responseSet.getResponses().add(response);

    Writer writer = new StringWriter();
    responseSet.toXml(writer);
    String xmlOutput = writer.toString();
    storeInFile(xmlOutput, file);
  }

  /**
   * Generates the telegram status sample.
   *
   * @param directory the output directory
   */
  private static void generateTelegramStatusSample(File file)
      throws IOException {
    createFile(file);
    List<DriveOrder> driveOrders = new LinkedList<>();
    LocationType locType = new LocationType("testLocType");
    Location loc1 = new Location("Storage 01", locType.getReference());
    Location loc2 = new Location("Storage 02", locType.getReference());
    DriveOrder.Destination dest1 = new DriveOrder.Destination(loc1.getReference())
        .withOperation("Load cargo");
    DriveOrder.Destination dest2 = new DriveOrder.Destination(loc2.getReference())
        .withOperation("Unload cargo");
    driveOrders.add(new DriveOrder(dest1));
    driveOrders.add(new DriveOrder(dest2));
    TransportOrder order = new TransportOrder("TOrder-0001", driveOrders)
        .withProperty("waitBefore", "Unload")
        .withState(TransportOrder.State.ACTIVE)
        .withProcessingVehicle(new Vehicle("Vehicle-0001").getReference());

    OrderStatusMessage message = OrderStatusMessage.fromTransportOrder(order);
    TCSStatusMessageSet messageSet = new TCSStatusMessageSet();
    messageSet.getStatusMessages().add(message);

    Writer writer = new StringWriter();
    messageSet.toXml(writer);
    String xmlOutput = writer.toString();
    storeInFile(xmlOutput, file);
  }

  /**
   * Generates the two transport orders sample.
   *
   * @param directory the output directory
   */
  private static void generateTwoTransportOrderSample(File file)
      throws IOException {
    createFile(file);
    TCSOrderSet orderSet = new TCSOrderSet();

    Transport transport = new Transport();
    transport.setId("TransportOrder-01");
    transport.setDeadline(new Date());
    transport.setIntendedVehicle("Vehicle-01");

    Destination dest = new Destination();
    dest.setLocationName("Storage 01");
    dest.setOperation("Load cargo");
    transport.getDestinations().add(dest);

    dest = new Destination();
    dest.setLocationName("Storage 02");
    dest.setOperation("Unload cargo");
    transport.getDestinations().add(dest);

    transport.getProperties().add(new Property("waitBefore", "Unload"));

    orderSet.getOrders().add(transport);

    transport = new Transport();
    transport.setId("TransportOrder-02");

    dest = new Destination();
    dest.setLocationName("Working station 01");
    dest.setOperation("Drill");
    dest.getProperties().add(new Property("drillSize", "3"));
    transport.getDestinations().add(dest);

    dest = new Destination();
    dest.setLocationName("Working station 02");
    dest.setOperation("Drill");
    dest.getProperties().add(new Property("drillSize", "8"));
    transport.getDestinations().add(dest);

    dest = new Destination();
    dest.setLocationName("Working station 03");
    dest.setOperation("Cut");
    transport.getDestinations().add(dest);

    orderSet.getOrders().add(transport);

    Writer writer = new StringWriter();
    orderSet.toXml(writer);
    String xmlOutput = writer.toString();
    storeInFile(xmlOutput, file);
  }

  private static void generateVehicleStatusSample(File file)
      throws IOException {
    createFile(file);

    Point currentPosition = new Point("Point-000");
    Point nextPosition = new Point("Point-001");
    TransportOrder order = new TransportOrder("TransportOrder-001", Collections.emptyList());

    Vehicle vehicle = new Vehicle("Vehicle-000")
        .withEnergyLevel(50)
        .withCurrentPosition(currentPosition.getReference())
        .withNextPosition(nextPosition.getReference())
        .withPrecisePosition(new Triple(100, 110, 120))
        .withProcState(Vehicle.ProcState.PROCESSING_ORDER)
        .withEnergyLevelCritical(30)
        .withEnergyLevelGood(90)
        .withLength(1)
        .withMaxReverseVelocity(2)
        .withMaxVelocity(2)
        .withOrientationAngle(90)
        .withRouteProgressIndex(3)
        .withTransportOrder(order.getReference())
        .withState(Vehicle.State.EXECUTING);

    VehicleStatusMessage message = VehicleStatusMessage.fromVehicle(vehicle);
    TCSStatusMessageSet messageSet = new TCSStatusMessageSet();
    messageSet.getStatusMessages().add(message);

    Writer writer = new StringWriter();
    messageSet.toXml(writer);
    String xmlOutput = writer.toString();
    storeInFile(xmlOutput, file);
  }

  /**
   * Deletes and creates the file.
   *
   * @param file the file
   */
  private static void createFile(File file) {
    if (file.exists()) {
      file.delete();
    }
    try {
      file.createNewFile();
    }
    catch (IOException ex) {
      LoggerFactory.getLogger(SamplesGenerator.class).error("", ex);
    }
  }

  /**
   * Appends the string to the content of the file.
   *
   * @param xml the string that should be appended
   * @param file the file
   */
  private static void storeInFile(String xml, File file) {
    try (FileWriter writer = new FileWriter(file)) {
      writer.append(xml);
    }
    catch (IOException ex) {
      LoggerFactory.getLogger(SamplesGenerator.class).error("", ex);
    }
  }

  private static enum GeneratorTask {

    TELEGRAM_TO_TWO_ORDERS,
    TELEGRAM_TO_STATUS,
    TELEGRAM_TO_RECEIPT_ORDERS,
    TELEGRAM_TO_RECEIPT_BATCH,
    TELEGRAM_TO_BATCH,
    TELEGRAM_VEHICLE_STATUS;
  }
}
