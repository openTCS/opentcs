/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.persistence;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.opentcs.customizations.ApplicationHome;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.order.DriveOrder;
import org.opentcs.data.order.Route;
import org.opentcs.data.order.Route.Step;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.kernel.workingset.TransportOrderPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implementation of <code>OrderPersister</code> realizes persistence of
 * transport orders in XML files.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class XMLFileOrderPersister
    implements OrderPersister {

  /**
   * This class's Logger.
   */
  private static final Logger log
      = LoggerFactory.getLogger(XMLFileOrderPersister.class);
  /**
   * The name of the file in which the data of finished transport orders is
   * archived.
   */
  private static final String archiveFileName = "finished_orders.xml";
  /**
   * The name of the file in which the data of unfinished transport orders is
   * archived.
   */
  private static final String storeFileName = "active_orders.xml";
  /**
   * An <code>XMLOutputter</code> instance for writing XML documents in pretty
   * format.
   */
  private static final XMLOutputter xmlOutputter
      = new XMLOutputter(Format.getPrettyFormat());
  /**
   * A <code>DateFormat</code> instance for formatting dates written to the
   * archive.
   */
  private final DateFormat dateFormat = DateFormat.getDateTimeInstance();
  /**
   * The file in which the data of finished transport orders is archived.
   */
  private final File archiveFile;
  /**
   * The file in which the data of unfinished transport orders is archived.
   */
  private final File storeFile;
  /**
   * The directory in which the transport order data is stored.
   */
  private final File dataDirectory;
  /**
   * The document containing the archive data.
   */
  private final Document archiveDocument;

  /**
   * Creates a new XMLFileOrderPersister.
   *
   * @param directory The application's home directory.
   * @throws IllegalArgumentException If <code>directory</code> is not a
   * directory.
   */
  @Inject
  public XMLFileOrderPersister(@ApplicationHome File directory) {
    log.debug("method entry");
    Objects.requireNonNull(directory, "directory is null");
    dataDirectory = new File(directory, "data");
    if (!dataDirectory.isDirectory() && !dataDirectory.mkdirs()) {
      throw new IllegalArgumentException(dataDirectory.getPath()
          + " is not an existing directory and could not be created, either.");
    }
    archiveFile = new File(dataDirectory, archiveFileName);
    storeFile = new File(dataDirectory, storeFileName);
    archiveDocument = new Document(new Element("TransportOrderArchive"));
  }

  @Override
  public synchronized void archiveTransportOrder(TransportOrder order)
      throws IOException, IllegalArgumentException {
    if (order == null) {
      throw new NullPointerException("order is null");
    }
    if (!order.getState().equals(TransportOrder.State.FINISHED)) {
      throw new IllegalArgumentException(
          "order " + order.getName() + " is not FINISHED");
    }
    File tempFile = File.createTempFile(
        "finished_orders", null, dataDirectory);
    OutputStream outStream
        = new BufferedOutputStream(new FileOutputStream(tempFile));
    archiveDocument.getRootElement().addContent(getXMLTransportOrder(order));
    xmlOutputter.output(archiveDocument, outStream);
    outStream.close();
    archiveFile.delete();
    tempFile.renameTo(archiveFile);
  }

  @Override
  public synchronized void archiveTransportOrders(Set<TransportOrder> orders)
      throws IOException, IllegalArgumentException {
    if (orders == null) {
      throw new NullPointerException("orders is null");
    }
    for (TransportOrder curOrder : orders) {
      if (!curOrder.getState().equals(TransportOrder.State.FINISHED)) {
        throw new IllegalArgumentException(
            "order " + curOrder.getName() + " is not FINISHED");
      }
    }
    File tempFile = File.createTempFile(
        "finished_orders", null, dataDirectory);
    OutputStream outStream
        = new BufferedOutputStream(new FileOutputStream(tempFile));
    Element rootElement = archiveDocument.getRootElement();
    // Add all finished transport orders to the archive.
    for (TransportOrder curOrder : orders) {
      if (curOrder.getState().equals(TransportOrder.State.FINISHED)) {
        rootElement.addContent(getXMLTransportOrder(curOrder));
      }
    }
    xmlOutputter.output(archiveDocument, outStream);
    outStream.close();
    archiveFile.delete();
    tempFile.renameTo(archiveFile);
  }

  @Override
  public synchronized void saveTransportOrders(TransportOrderPool pool)
      throws IOException {
    if (pool == null) {
      throw new NullPointerException("pool is null");
    }
    File tempFile = File.createTempFile("active_orders", ".xml", dataDirectory);
    OutputStream outStream
        = new BufferedOutputStream(new FileOutputStream(tempFile));
    Document xmlDocument = new Document();
    Element rootElement = new Element("TransportOrderPool");
    xmlDocument.setRootElement(rootElement);
    for (TransportOrder curOrder : pool.getTransportOrders((Pattern) null)) {
      if (!curOrder.getState().equals(TransportOrder.State.FINISHED)) {
        rootElement.addContent(getXMLTransportOrder(curOrder));
      }
    }
    xmlOutputter.output(xmlDocument, outStream);
    outStream.close();
    storeFile.delete();
    tempFile.renameTo(storeFile);
  }

  @SuppressWarnings("unchecked")
  @Override
  public synchronized void loadTransportOrders(TransportOrderPool pool)
      throws IOException {
    if (pool == null) {
      throw new NullPointerException("pool is null");
    }
    try {
      InputStream inStream
          = new BufferedInputStream(new FileInputStream(storeFile));
      Document xmlDocument = (new SAXBuilder()).build(inStream);
      inStream.close();
      Element rootElement = xmlDocument.getRootElement();
      if (!rootElement.getName().equals("TransportOrderPool")) {
        // XXX Better suited exception for this?
        throw new IllegalArgumentException("Invalid order pool file");
      }
      for (Element curElement : rootElement.getChildren("")) {
        createXMLTransportOrder(curElement, pool);
      }
    }
    catch (FileNotFoundException | JDOMException exc) {
      log.warn("Exception loading transport orders", exc);
      // XXX Do we need to react in any way? Probably not.
    }
    // XXX Implement this.
  }

  /**
   *
   */
  private static void createXMLTransportOrder(Element orderElement,
                                              TransportOrderPool pool) {
    String orderName = orderElement.getAttributeValue("name");

    //TransportOrder order = pool.cre
  }

  /**
   *
   */
  private Element getXMLTransportOrder(TransportOrder order) {
    Element result = new Element("transportOrder");
    result.setAttribute("name", order.getName());
    result.setAttribute("creationTime",
                        dateFormat.format(new Date(order.getCreationTime())));
    result.setAttribute("finishedTime",
                        dateFormat.format(new Date(order.getFinishedTime())));
    result.setAttribute("deadline",
                        dateFormat.format(new Date(order.getDeadline())));
    result.setAttribute("state", order.getState().name());
    for (TCSObjectReference<TransportOrder> curDep : order.getDependencies()) {
      Element depElement = new Element("dependency");
      depElement.setAttribute("name", curDep.getName());
      result.addContent(depElement);
    }
    for (DriveOrder curDriveOrder : order.getPastDriveOrders()) {
      Element driveOrderElement = new Element("pastDriveOrder");
      driveOrderElement.setAttribute("state", curDriveOrder.getState().name());
      driveOrderElement.setAttribute("destinationLocation",
                                     curDriveOrder.getDestination().getLocation().getName());
      driveOrderElement.setAttribute("destinationOperation",
                                     curDriveOrder.getDestination().getOperation());
      Route route = curDriveOrder.getRoute();
      Element routeElement = new Element("route");
      if (route != null) {
        for (Step curStep : route.getSteps()) {
          Element stepElement = new Element("step");
          stepElement.setAttribute("path", curStep.getPath().getName());
          stepElement.setAttribute(
              "destinationPoint", curStep.getDestinationPoint().getName());
          routeElement.addContent(stepElement);
        }
      }
      driveOrderElement.addContent(routeElement);
      // driveOrderElement.setAttribute("currentRouteStepIndex", );
      result.addContent(driveOrderElement);
    }
    for (DriveOrder curDriveOrder : order.getFutureDriveOrders()) {
      Element driveOrderElement = new Element("pastDriveOrder");
      driveOrderElement.setAttribute("state", curDriveOrder.getState().name());
      driveOrderElement.setAttribute("destinationLocation",
                                     curDriveOrder.getDestination().getLocation().getName());
      driveOrderElement.setAttribute("destinationOperation",
                                     curDriveOrder.getDestination().getOperation());
      Route route = curDriveOrder.getRoute();
      Element routeElement = new Element("route");
      if (route != null) {
        for (Step curStep : route.getSteps()) {
          Element stepElement = new Element("step");
          stepElement.setAttribute("path", curStep.getPath().getName());
          stepElement.setAttribute(
              "destinationPoint", curStep.getDestinationPoint().getName());
          routeElement.addContent(stepElement);
        }
      }
      driveOrderElement.addContent(routeElement);
      // driveOrderElement.setAttribute("currentRouteStepIndex", );
      result.addContent(driveOrderElement);
    }
    return result;
  }
}
