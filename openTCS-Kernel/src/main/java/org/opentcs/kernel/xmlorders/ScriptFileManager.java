/*
 * openTCS copyright information:
 * Copyright (c) 2009 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.xmlorders;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;
import org.opentcs.access.Kernel;
import org.opentcs.access.LocalKernel;
import org.opentcs.customizations.ApplicationHome;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.xmlorders.binding.Destination;
import org.opentcs.kernel.xmlorders.binding.TCSScriptFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides helper methods for retrieving order script files.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class ScriptFileManager {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ScriptFileManager.class);
  /**
   * The directory in which to look for scripts.
   */
  private final File scriptDir;
  /**
   * The kernel we work with.
   */
  private final Kernel localKernel;

  /**
   * Creates a new ScriptFileManager.
   *
   * @param kernel The kernel.
   * @param dir The application's home directory.
   */
  @Inject
  public ScriptFileManager(LocalKernel kernel,
                           @ApplicationHome File dir) {
    localKernel = Objects.requireNonNull(kernel);
    Objects.requireNonNull(dir);
    scriptDir = new File(dir, "scripts");
    if (!scriptDir.isDirectory() && !scriptDir.mkdirs()) {
      throw new IllegalArgumentException(scriptDir.getPath()
          + " is not an existing directory and could not be created, either.");
    }
    // Create a template for script files if it doesn't exist, yet.
    createTemplate(new File(scriptDir, "template.tcs"));
  }

  /**
   * Returns a list of names of (potential) script files.
   *
   * @return A list of names of (potential) script files.
   */
  public List<String> listScriptFileNames() {
    List<String> result = new LinkedList<>();
    // Find all children in the script directory that are normal files.
    for (File curFile : scriptDir.listFiles()) {
      if (curFile.isFile()) {
        result.add(curFile.getName());
      }
    }
    return result;
  }

  /**
   * Parses the file with the given name and returns <code>TCSScriptFile</code>
   * representing its content.
   *
   * @param fileName The name of the file to be parsed.
   * @return A <code>TCSScriptFile</code> instance representing the content of
   * the file with the given name.
   * @throws IOException If there was a problem reading/parsing the file.
   */
  public TCSScriptFile getScriptFile(String fileName)
      throws IOException {
    // Make sure we only look for scripts in the script directory.
    File inputFile = new File(fileName);
    inputFile = new File(scriptDir, inputFile.getName());
    // Parse the script file and return the resulting instance.
    return TCSScriptFile.fromFile(inputFile);
  }

  /**
   * Creates and returns a list of transport orders defined in a script file.
   *
   * @param fileName The name of the script file defining the transport orders
   * to be created.
   * @return The list of transport orders created. If none were created, the
   * returned list is empty.
   * @throws ObjectUnknownException If any object referenced in the script file
   * does not exist.
   * @throws IOException If there was a problem reading or parsing the file with
   * the given name.
   */
  public List<TransportOrder> createTransportOrdersFromScript(String fileName)
      throws IOException {
    if (fileName == null) {
      throw new NullPointerException("fileName is null");
    }
    List<TransportOrder> result = new LinkedList<>();
    TCSScriptFile scriptFile = getScriptFile(fileName);
    TCSObjectReference<TransportOrder> prevOrderRef = null;
    for (TCSScriptFile.Order curOrder : scriptFile.getOrders()) {
      TransportOrder order = createTransportOrder(curOrder.getDestinations());
      setIntendedVehicle(order, curOrder.getIntendedVehicle());
      if (prevOrderRef != null) {
        localKernel.addTransportOrderDependency(order.getReference(),
                                                prevOrderRef);
      }
      localKernel.activateTransportOrder(order.getReference());
      if (scriptFile.getSequentialDependencies()) {
        prevOrderRef = order.getReference();
      }
      result.add(order);
    }
    return result;
  }

  /**
   * Creates a transport order.
   * 
   * @param destinations The destinations of this order.
   * @return The newly created transport order.
   */
  TransportOrder createTransportOrder(List<Destination> destinations) {
    List<DriveOrder.Destination> realDests
        = new LinkedList<>();
    for (Destination curDest : destinations) {
      // XXX Avoid NullPointerException if location doesn't exist.
      Location curLoc = localKernel.getTCSObject(Location.class,
                                                 curDest.getLocationName());
      if (curLoc == null) {
        throw new ObjectUnknownException("Unknown destination location: "
            + curDest.getLocationName());
      }
      TCSObjectReference<Location> curDestLoc = curLoc.getReference();
      String curDestOp = curDest.getOperation();
      realDests.add(new DriveOrder.Destination(curDestLoc, curDestOp));
    }
    return localKernel.createTransportOrder(realDests);
  }

  /**
   * Sets the intended vehicle.
   * 
   * @param order The transport order the vehicle shall be set.
   * @param vehicleName The name of the vehicle.
   */
  void setIntendedVehicle(TransportOrder order, String vehicleName) {
    if (vehicleName != null && !vehicleName.isEmpty()) {
      Vehicle vehicle = localKernel.getTCSObject(Vehicle.class, vehicleName);
      if (vehicle == null) {
        // XXX Set state of created order to FAILED?
        throw new ObjectUnknownException("Unknown vehicle: " + vehicleName);
      }
      localKernel.setTransportOrderIntendedVehicle(order.getReference(),
                                                   vehicle.getReference());
    }
  }

  private void createTemplate(File templateFile) {
    Objects.requireNonNull(templateFile);
    if (templateFile.exists()) {
      return;
    }

    TCSScriptFile scriptFile = new TCSScriptFile();
    TCSScriptFile.Order order = new TCSScriptFile.Order();
    Destination dest = new Destination();
    dest.setLocationName("A location");
    dest.setOperation("An operation");
    order.getDestinations().add(dest);
    dest = new Destination();
    dest.setLocationName("Another location");
    dest.setOperation("Another operation");
    order.getDestinations().add(dest);
    order.setIntendedVehicle("The intended vehicle");
    scriptFile.getOrders().add(order);
    try {
      Writer writer = new FileWriter(new File(scriptDir, "template.tcs"));
      writer.write(scriptFile.toXml());
      writer.close();
    }
    catch (IOException exc) {
      LOG.warn("Exception writing template script", exc);
    }
  }
}
