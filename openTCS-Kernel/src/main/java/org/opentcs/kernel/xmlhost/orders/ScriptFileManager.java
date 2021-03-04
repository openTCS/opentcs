/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.xmlhost.orders;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.opentcs.access.Kernel;
import org.opentcs.access.LocalKernel;
import org.opentcs.access.to.order.DestinationCreationTO;
import org.opentcs.access.to.order.TransportOrderCreationTO;
import org.opentcs.customizations.ApplicationHome;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.xmlhost.orders.binding.Destination;
import org.opentcs.kernel.xmlhost.orders.binding.TCSScriptFile;
import static org.opentcs.util.Assertions.checkArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides helper methods for retrieving order script files.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class ScriptFileManager {

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
  public ScriptFileManager(@Nonnull LocalKernel kernel, @Nonnull @ApplicationHome File dir) {
    localKernel = requireNonNull(kernel, "kernel");
    requireNonNull(dir, "dir");
    scriptDir = new File(dir, "scripts");
    checkArgument(scriptDir.isDirectory() || scriptDir.mkdirs(),
                  "%s is not an existing directory and could not be created, either.",
                  scriptDir.getPath());
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
    requireNonNull(fileName, "fileName");

    List<TransportOrder> result = new LinkedList<>();
    TCSScriptFile scriptFile = getScriptFile(fileName);
    String prevOrderName = null;
    for (TCSScriptFile.Order curOrder : scriptFile.getOrders()) {

      TransportOrderCreationTO orderTO
          = new TransportOrderCreationTO("TOrder-" + UUID.randomUUID(),
                                         createDestinations(curOrder.getDestinations()))
              .setIntendedVehicleName(curOrder.getIntendedVehicle());
      if (scriptFile.getSequentialDependencies() && prevOrderName != null) {
        orderTO.getDependencyNames().add(prevOrderName);
      }

      TransportOrder order = localKernel.createTransportOrder(orderTO);
      localKernel.activateTransportOrder(order.getReference());

      result.add(order);

      prevOrderName = order.getName();
    }
    return result;
  }

  private List<DestinationCreationTO> createDestinations(List<Destination> destinations) {
    List<DestinationCreationTO> result = new ArrayList<>();
    for (Destination curDest : destinations) {
      result.add(new DestinationCreationTO(curDest.getLocationName(), curDest.getOperation()));
    }
    return result;
  }

  private void createTemplate(File templateFile) {
    requireNonNull(templateFile, "templateFile");
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
    try (Writer writer = new FileWriter(new File(scriptDir, "template.tcs"))) {
      writer.write(scriptFile.toXml());
    }
    catch (IOException exc) {
      LOG.warn("Exception writing template script", exc);
    }
  }
}
