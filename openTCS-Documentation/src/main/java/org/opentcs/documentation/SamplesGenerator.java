/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.opentcs.documentation;

import static com.google.common.base.Preconditions.checkArgument;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.xmlorders.binding.Destination;
import org.opentcs.kernel.xmlorders.binding.Property;
import org.opentcs.kernel.xmlorders.binding.ScriptResponse;
import org.opentcs.kernel.xmlorders.binding.TCSOrderSet;
import org.opentcs.kernel.xmlorders.binding.TCSResponseSet;
import org.opentcs.kernel.xmlorders.binding.Transport;
import org.opentcs.kernel.xmlorders.binding.TransportResponse;
import org.opentcs.kernel.xmlorders.binding.TransportScript;
import org.opentcs.kernel.xmlstatus.binding.OrderStatusMessage;
import org.opentcs.kernel.xmlstatus.binding.TCSStatusMessageSet;
import org.slf4j.LoggerFactory;

/**
 * Generates the sample xml files for the documentation.
 *
 * @author Mats Wilhelm (Fraunhofer IML 2017)
 */
public class SamplesGenerator {

  public static void main(String[] args) {
    checkArgument(args.length >= 2, "Need at least two argument for the task and target file");

    GeneratorTask task = GeneratorTask.valueOf(args[0]);
    File file = new File(args[1]);

    switch (task) {
      case TELEGRAM_TWO_ORDERS: {
        generateTwoTransportOrderSample(file);
        break;
      }
      case TELEGRAM_STATUS: {
        generateTelegramStatusSample(file);
        break;
      }
      case TELEGRAM_RECEIPT_ORDERS: {
        generateResponseSetSample(file);
        break;
      }
      case TELEGRAM_RECEIPT_BATCH: {
        generateScriptResponseSetSample(file);
        break;
      }
      case TELEGRAM_BATCH: {
        generateTransportScriptOrderSample(file);
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
  private static void generateTransportScriptOrderSample(File file) {
    createFile(file);
    TCSOrderSet orderSet = new TCSOrderSet();

    TransportScript transportScript = new TransportScript();
    transportScript.setId("test.tcs");
    transportScript.setFileName("test.tcs");

    orderSet.getOrders().add(transportScript);

    String xmlOutput = orderSet.toXml();
    storeInFile(xmlOutput, file);
  }

  /**
   * Generates the script response set sample.
   *
   * @param directory the output directory
   */
  private static void generateScriptResponseSetSample(File file) {
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

    String xmlOutput = responseSet.toXml();
    storeInFile(xmlOutput, file);
  }

  /**
   * Generates the response set sample.
   *
   * @param directory the output directory
   */
  private static void generateResponseSetSample(File file) {
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

    String xmlOutput = responseSet.toXml();
    storeInFile(xmlOutput, file);
  }

  /**
   * Generates the telegram status sample.
   *
   * @param directory the output directory
   */
  private static void generateTelegramStatusSample(File file) {
    createFile(file);
    int objectIdCounter = 0;
    List<DriveOrder.Destination> destinations = new LinkedList<>();
    LocationType locType = new LocationType(objectIdCounter++, "testLocType");
    Location loc1 = new Location(objectIdCounter++, "Storage 01", locType.getReference());
    Location loc2 = new Location(objectIdCounter++, "Storage 02", locType.getReference());
    DriveOrder.Destination dest1 = new DriveOrder.Destination(loc1.getReference(), "Load cargo");
    DriveOrder.Destination dest2 = new DriveOrder.Destination(loc2.getReference(), "Unload cargo");
    destinations.add(dest1);
    destinations.add(dest2);
    TransportOrder order = new TransportOrder(0, "TOrder-0001", destinations, System.currentTimeMillis());
    order.setProperty("waitBefore", "Unload");
    order.setState(TransportOrder.State.ACTIVE);

    OrderStatusMessage message = OrderStatusMessage.fromTransportOrder(order);
    TCSStatusMessageSet messageSet = new TCSStatusMessageSet();
    messageSet.getStatusMessages().add(message);

    String xmlOutput = messageSet.toXml();
    storeInFile(xmlOutput, file);
  }

  /**
   * Generates the two transport orders sample.
   *
   * @param directory the output directory
   */
  private static void generateTwoTransportOrderSample(File file) {
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
    dest.setOperation("Unoad cargo");
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

    String xmlOutput = orderSet.toXml();
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
    TELEGRAM_TWO_ORDERS,
    TELEGRAM_STATUS,
    TELEGRAM_RECEIPT_ORDERS,
    TELEGRAM_RECEIPT_BATCH,
    TELEGRAM_BATCH;
  }
}
