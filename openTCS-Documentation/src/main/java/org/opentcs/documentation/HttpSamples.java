/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.documentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import static com.google.common.base.Preconditions.checkArgument;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.extensions.servicewebapi.v1.order.binding.Destination;
import org.opentcs.kernel.extensions.servicewebapi.v1.order.binding.Property;
import org.opentcs.kernel.extensions.servicewebapi.v1.order.binding.Transport;
import org.opentcs.kernel.extensions.servicewebapi.v1.status.binding.OrderStatusMessage;
import org.opentcs.kernel.extensions.servicewebapi.v1.status.binding.StatusMessageList;
import org.opentcs.kernel.extensions.servicewebapi.v1.status.binding.VehicleStatusMessage;
import org.slf4j.LoggerFactory;

/**
 * Generates the sample xml files for the documentation.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 */
public class HttpSamples {

  /**
   * Prevents external instantiation.
   */
  private HttpSamples() {
  }

  public static void main(String[] args)
      throws IOException {
    checkArgument(args.length >= 2, "Need at least two argument for the task and target file");

    SampleType type = SampleType.valueOf(args[0]);
    File outputFile = new File(args[1]);

    switch (type) {
      case STATUS:
        writeStatusEventsSample(outputFile);
        break;

      case CREATE_ORDER:
        writeTransportOrderCreationSample(outputFile);
        break;

      default:
        throw new IllegalArgumentException("Unhandled sample type: " + type);
    }

  }

  private static void writeStatusEventsSample(File outputFile) {
    StatusMessageList result = new StatusMessageList();

    TransportOrder order
        = new TransportOrder("TOrder-XYZ",
                             Arrays.asList(
                                 createDriveOrder("Storage 01", "Load cargo"),
                                 createDriveOrder("Storage 02", "Unload cargo")
                             )
        )
            .withProperty("transport order-specific key", "some value")
            .withState(TransportOrder.State.ACTIVE)
            .withProcessingVehicle(new Vehicle("Vehicle-0001").getReference());

    result.getStatusMessages().add(
        OrderStatusMessage.fromTransportOrder(order,
                                              0,
                                              Instant.now().minus(20, ChronoUnit.SECONDS))
    );

    Vehicle vehicle = new Vehicle("Vehicle-0001")
        .withProcState(Vehicle.ProcState.PROCESSING_ORDER)
        .withState(Vehicle.State.EXECUTING)
        .withEnergyLevel(45)
        .withCurrentPosition(new Point("Point-0001").getReference())
        .withPrecisePosition(new Triple(20560, 17830, 0))
        .withTransportOrder(order.getReference());

    result.getStatusMessages().add(
        VehicleStatusMessage.fromVehicle(vehicle, 1, Instant.now().minus(15, ChronoUnit.SECONDS))
    );

    writeToFile(result, outputFile);
  }

  private static void writeTransportOrderCreationSample(File outputFile) {
    Transport transport = new Transport();
    transport.setDeadline(Instant.now());
    transport.setIntendedVehicle("Vehicle-01");

    Destination dest = new Destination();
    dest.setLocationName("Storage 01");
    dest.setOperation("Load cargo");
    transport.getDestinations().add(dest);

    dest = new Destination();
    dest.setLocationName("Storage 02");
    dest.setOperation("Unload cargo");
    dest.getProperties().add(new Property("destination-specific key", "some value"));
    transport.getDestinations().add(dest);

    transport.getProperties().add(new Property("transport order-specific key",
                                               "some value"));

    writeToFile(transport, outputFile);
  }

  private static DriveOrder createDriveOrder(String locationName, String operation) {
    Location location = new Location(locationName,
                                     new LocationType("Some location type").getReference());
    return new DriveOrder(
        new DriveOrder.Destination(location.getReference())
            .withOperation(operation)
    );
  }

  private static void writeToFile(Object object, File file) {
    try (FileWriter writer = new FileWriter(file)) {
      new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
          .writerWithDefaultPrettyPrinter()
          .writeValue(writer, object);
    }
    catch (IOException ex) {
      LoggerFactory.getLogger(HttpSamples.class).error("", ex);
    }
  }

  private static enum SampleType {
    STATUS,
    CREATE_ORDER
  }
}
