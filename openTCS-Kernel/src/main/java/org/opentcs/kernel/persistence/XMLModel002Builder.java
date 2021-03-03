/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.persistence;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nullable;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectPropConstants;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Group;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.StaticRoute;
import org.opentcs.data.model.TCSResource;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.visualization.ImageLayoutElement;
import org.opentcs.data.model.visualization.LayoutElement;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.model.visualization.ShapeLayoutElement;
import org.opentcs.data.model.visualization.ViewBookmark;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.kernel.workingset.Model;
import org.opentcs.util.Comparators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Version 0.0.2 of the implementation of {@link XMLModelReader XMLModelReader}
 * and {@link XMLModelWriter XMLModelWriter}.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class XMLModel002Builder
    implements XMLModelReader,
               XMLModelWriter {

  /**
   * The file format version this builder reads and writes.
   */
  static final String versionString = "0.0.2";
  /**
   * This class's Logger.
   */
  private static final Logger log
      = LoggerFactory.getLogger(XMLModel002Builder.class);
  /**
   * The URL of the schema for XML model validataion.
   */
  private static final URL schemaUrl = XMLModel002Builder.class.getResource(
      "/org/opentcs/kernel/persistence/model-0.0.2.xsd");

  /**
   * Creates a new XMLModel001Builder.
   */
  public XMLModel002Builder() {
  }

  // Implementation of interface XMLModelWriter starts here.
  @Override
  public String getVersionString() {
    return versionString;
  }

  @Override
  public void writeXMLModel(Model model,
                            @Nullable String name,
                            OutputStream outStream)
      throws IOException {
    Objects.requireNonNull(model, "model is null");
    Objects.requireNonNull(outStream, "outStream is null");

    Element rootElement = new Element("model");
    rootElement.setAttribute("version", versionString);
    rootElement.setAttribute("name", name != null ? name : model.getName());
    // Add model data.
    rootElement.addContent(getXMLPoints(model));
    rootElement.addContent(getXMLPaths(model));
    rootElement.addContent(getXMLVehicles(model));
    rootElement.addContent(getXMLLocationTypes(model));
    rootElement.addContent(getXMLLocations(model));
    rootElement.addContent(getXMLBlocks(model));
    rootElement.addContent(getXMLStaticRoutes(model));
    rootElement.addContent(getXMLGroups(model));
    rootElement.addContent(getXMLVisualLayouts(model));
    // Create a document for the root element...
    Document document = new Document(rootElement);
    // ...and write it to the output stream with native line feeds.
    Format docFormat = Format.getPrettyFormat();
    docFormat.setLineSeparator(System.getProperty("line.separator"));
    XMLOutputter outputter = new XMLOutputter(docFormat);
    outputter.output(document, outStream);
    outStream.flush();
  }

  // Implementation of interface XMLModelReader starts here.
  @Override
  public String readModelName(InputStream inStream)
      throws InvalidModelException, IOException {
    requireNonNull(inStream, "inStream");

    Document modelDocument = getModelDocument(inStream);

    Element rootElement = modelDocument.getRootElement();
    // Verify that this is an openTCS model.
    if (!rootElement.getName().equals("model")) {
      throw new InvalidModelException("Not an openTCS XML model");
    }
    String modelName = rootElement.getAttributeValue("name", "");
    if (modelName.isEmpty()) {
      modelName = "ModelNameMissing";
      //throw new InvalidModelException("Model name missing");
    }
    return modelName;
  }

  @Override
  public void readXMLModel(InputStream inStream, Model model)
      throws InvalidModelException, IOException {
    Objects.requireNonNull(inStream, "inStream is null");
    Objects.requireNonNull(model, "model is null");

    Document modelDocument = getModelDocument(inStream);

    Element rootElement = modelDocument.getRootElement();
    // Verify that this is an openTCS model.
    if (!rootElement.getName().equals("model")) {
      throw new InvalidModelException("Not an openTCS XML model");
    }
    String modelName = rootElement.getAttributeValue("name", "");
    if (modelName.isEmpty()) {
      modelName = "ModelNameMissing";
      //throw new InvalidModelException("Model name missing");
    }
    String modelVersion = rootElement.getAttributeValue("version");
    if (!versionString.equals(modelVersion)) {
      throw new InvalidModelException("Bad model version: " + modelVersion);
    }
    // Clear the model before reading the new data.
    // XXX What about potentially stale transport order data?
    model.clear();
    // Set the model's name.
    model.setName(modelName);
    // Fill the model with components.
    List<Element> pointElements = rootElement.getChildren("point");
    List<Element> pathElements = rootElement.getChildren("path");
    List<Element> vehicleElements = rootElement.getChildren("vehicle");
    List<Element> locTypeElements = rootElement.getChildren("locationType");
    List<Element> locationElements = rootElement.getChildren("location");
    List<Element> blockElements = rootElement.getChildren("block");
    List<Element> staticRouteElements = rootElement.getChildren("staticRoute");
    List<Element> groupElements = rootElement.getChildren("group");
    List<Element> visuLayoutElements = rootElement.getChildren("visualLayout");
    try {
      readVehicles(vehicleElements, model);
      readPoints(pointElements, model);
      readPaths(pathElements, pointElements, model);
      readLocationTypes(locTypeElements, model);
      readLocations(locationElements, model);
      readBlocks(blockElements, model);
      readStaticRoutes(staticRouteElements, model);
      readGroups(groupElements, model);
      readVisualLayouts(visuLayoutElements, model);
    }
    catch (ObjectExistsException exc) {
      throw new InvalidModelException("Duplicate objects found in model", exc);
    }
  }

  // Private methods start here.
  private Document getModelDocument(InputStream inStream)
      throws IOException {
    assert inStream != null;

    try {
      // Create a document builder that validates the XML input using our schema
      SAXBuilder builder = new SAXBuilder();
      builder.setFeature("http://xml.org/sax/features/validation", true);
      builder.setFeature(
          "http://apache.org/xml/features/validation/schema", true);
      builder.setFeature(
          "http://apache.org/xml/features/validation/schema-full-checking",
          true);
      builder.setProperty("http://apache.org/xml/properties/schema/external-"
          + "noNamespaceSchemaLocation", schemaUrl.toString());
      return builder.build(inStream);
    }
    catch (JDOMException exc) {
      log.error("Exception parsing input", exc);
      throw new IOException("Exception parsing input: " + exc.getMessage());
    }
  }

  /**
   * Returns a list of XML elements for all points in a model.
   *
   * @param model The model data.
   * @return A list of XML elements for all points in a model.
   */
  private static List<Element> getXMLPoints(Model model) {
    Set<Point> points = new TreeSet<>(Comparators.objectsById());
    points.addAll(model.getPoints(null));
    List<Element> result = new ArrayList<>(points.size());
    for (Point curPoint : points) {
      Element pointElement = new Element("point");
      pointElement.setAttribute("id", String.valueOf(curPoint.getId()));
      pointElement.setAttribute("name", curPoint.getName());
      pointElement.setAttribute("xPosition",
                                String.valueOf(curPoint.getPosition().getX()));
      pointElement.setAttribute("yPosition",
                                String.valueOf(curPoint.getPosition().getY()));
      pointElement.setAttribute("zPosition",
                                String.valueOf(curPoint.getPosition().getZ()));
      pointElement.setAttribute("vehicleOrientationAngle",
                                String.valueOf(curPoint.getVehicleOrientationAngle()));
      pointElement.setAttribute("type", curPoint.getType().toString());
      for (TCSObjectReference<Path> curRef : curPoint.getOutgoingPaths()) {
        Element outgoingElement = new Element("outgoingPath");
        outgoingElement.setAttribute("name", curRef.getName());
        pointElement.addContent(outgoingElement);
      }
      for (Map.Entry<String, String> curEntry
               : curPoint.getProperties().entrySet()) {
        Element propertyElement = new Element("property");
        propertyElement.setAttribute("name", curEntry.getKey());
        propertyElement.setAttribute("value", curEntry.getValue());
        pointElement.addContent(propertyElement);
      }
      result.add(pointElement);
    }
    return result;
  }

  /**
   * Returns a list of XML elements for all paths in a model.
   *
   * @param model The model data.
   * @return A list of XML elements for all paths in a model.
   */
  private static List<Element> getXMLPaths(Model model) {
    Set<Path> paths = new TreeSet<>(Comparators.objectsById());
    paths.addAll(model.getPaths(null));
    List<Element> result = new ArrayList<>(paths.size());
    for (Path curPath : paths) {
      Element pathElement = new Element("path");
      pathElement.setAttribute("id", String.valueOf(curPath.getId()));
      pathElement.setAttribute("name", curPath.getName());
      pathElement.setAttribute("sourcePoint",
                               curPath.getSourcePoint().getName());
      pathElement.setAttribute("destinationPoint",
                               curPath.getDestinationPoint().getName());
      pathElement.setAttribute("length", String.valueOf(curPath.getLength()));
      pathElement.setAttribute("routingCost",
                               String.valueOf(curPath.getRoutingCost()));
      // velocities
      pathElement.setAttribute("maxVelocity",
                               String.valueOf(curPath.getMaxVelocity()));
      // reverse velocities
      pathElement.setAttribute("maxReverseVelocity",
                               String.valueOf(curPath.getMaxReverseVelocity()));
      // locks
      pathElement.setAttribute("locked", String.valueOf(curPath.isLocked()));
      // actions
      for (Map.Entry<String, String> curEntry
               : curPath.getProperties().entrySet()) {
        Element propertyElement = new Element("property");
        propertyElement.setAttribute("name", curEntry.getKey());
        propertyElement.setAttribute("value", curEntry.getValue());
        pathElement.addContent(propertyElement);
      }
      // XXX Add the path's other attributes as well.
      result.add(pathElement);
    }
    return result;
  }

  /**
   * Returns a list of XML elements for all vehicles in a model.
   *
   * @param model The model data.
   * @return A list of XML elements for all vehicles in a model.
   */
  private static List<Element> getXMLVehicles(Model model) {
    Set<Vehicle> vehicles = new TreeSet<>(Comparators.objectsById());
    vehicles.addAll(model.getVehicles(null));
    List<Element> result = new ArrayList<>(vehicles.size());
    for (Vehicle curVehicle : vehicles) {
      Element vehicleElement = new Element("vehicle");
      vehicleElement.setAttribute("id", String.valueOf(curVehicle.getId()));
      vehicleElement.setAttribute("name", curVehicle.getName());
      vehicleElement.setAttribute("length",
                                  String.valueOf(curVehicle.getLength()));
      vehicleElement.setAttribute("energyLevelCritical",
                                  String.valueOf(curVehicle.getEnergyLevelCritical()));
      vehicleElement.setAttribute("energyLevelGood",
                                  String.valueOf(curVehicle.getEnergyLevelGood()));
      for (Map.Entry<String, String> curEntry
               : curVehicle.getProperties().entrySet()) {
        Element propertyElement = new Element("property");
        propertyElement.setAttribute("name", curEntry.getKey());
        propertyElement.setAttribute("value", curEntry.getValue());
        vehicleElement.addContent(propertyElement);
      }
      result.add(vehicleElement);
    }
    return result;
  }

  /**
   * Returns a list of XML elements for all location types in a model.
   *
   * @param model The model data.
   * @return A list of XML elements for all location types in a model.
   */
  private static List<Element> getXMLLocationTypes(Model model) {
    Set<LocationType> locTypes = new TreeSet<>(Comparators.objectsById());
    locTypes.addAll(model.getLocationTypes(null));
    List<Element> result = new ArrayList<>(locTypes.size());
    for (LocationType curType : locTypes) {
      Element typeElement = new Element("locationType");
      typeElement.setAttribute("id", String.valueOf(curType.getId()));
      typeElement.setAttribute("name", curType.getName());
      for (String curOperation : curType.getAllowedOperations()) {
        Element opElement = new Element("allowedOperation");
        opElement.setAttribute("name", curOperation);
        typeElement.addContent(opElement);
      }
      result.add(typeElement);
      for (Map.Entry<String, String> curEntry
               : curType.getProperties().entrySet()) {
        Element propertyElement = new Element("property");
        propertyElement.setAttribute("name", curEntry.getKey());
        propertyElement.setAttribute("value", curEntry.getValue());
        typeElement.addContent(propertyElement);
      }
    }
    return result;
  }

  /**
   * Returns a list of XML elements for all locations in a model.
   *
   * @param model The model data.
   * @return A list of XML elements for all locations in a model.
   */
  private static List<Element> getXMLLocations(Model model) {
    Set<Location> locations = new TreeSet<>(Comparators.objectsById());
    locations.addAll(model.getLocations(null));
    List<Element> result = new ArrayList<>();
    for (Location curLoc : locations) {
      Element locElement = new Element("location");
      locElement.setAttribute("id", String.valueOf(curLoc.getId()));
      locElement.setAttribute("name", curLoc.getName());
      locElement.setAttribute("xPosition",
                              String.valueOf(curLoc.getPosition().getX()));
      locElement.setAttribute("yPosition",
                              String.valueOf(curLoc.getPosition().getY()));
      locElement.setAttribute("zPosition",
                              String.valueOf(curLoc.getPosition().getZ()));
      locElement.setAttribute("type", curLoc.getType().getName());
      for (Location.Link curLink : curLoc.getAttachedLinks()) {
        Element linkElement = new Element("link");
        linkElement.setAttribute("point", curLink.getPoint().getName());
        for (String operation : curLink.getAllowedOperations()) {
          Element allowedOpElement = new Element("allowedOperation");
          allowedOpElement.setAttribute("name", operation);
          linkElement.addContent(allowedOpElement);
        }
        locElement.addContent(linkElement);
      }
      for (Map.Entry<String, String> curEntry
               : curLoc.getProperties().entrySet()) {
        Element propertyElement = new Element("property");
        propertyElement.setAttribute("name", curEntry.getKey());
        propertyElement.setAttribute("value", curEntry.getValue());
        locElement.addContent(propertyElement);
      }
      result.add(locElement);
    }
    return result;
  }

  /**
   * Returns a list of XML elements for all blocks in a model.
   *
   * @param model The model data.
   * @return A list of XML elements for all blocks in a model.
   */
  private static List<Element> getXMLBlocks(Model model) {
    Set<Block> blocks = new TreeSet<>(Comparators.objectsById());
    blocks.addAll(model.getBlocks(null));
    List<Element> result = new ArrayList<>(blocks.size());
    for (Block curBlock : blocks) {
      Element blockElement = new Element("block");
      blockElement.setAttribute("id", String.valueOf(curBlock.getId()));
      blockElement.setAttribute("name", curBlock.getName());
      for (TCSResourceReference<?> curRef : curBlock.getMembers()) {
        Element resourceElement = new Element("member");
        resourceElement.setAttribute("name", curRef.getName());
        blockElement.addContent(resourceElement);
      }
      for (Map.Entry<String, String> curEntry
               : curBlock.getProperties().entrySet()) {
        Element propertyElement = new Element("property");
        propertyElement.setAttribute("name", curEntry.getKey());
        propertyElement.setAttribute("value", curEntry.getValue());
        blockElement.addContent(propertyElement);
      }
      result.add(blockElement);
    }
    return result;
  }

  /**
   * Returns a list of XML elements for all groups in a model.
   *
   * @param model The model data.
   * @return A list of XML elements for all groups in a model.
   */
  private static List<Element> getXMLGroups(Model model) {
    Set<Group> groups = new TreeSet<>(Comparators.objectsById());
    groups.addAll(model.getGroups(null));
    List<Element> result = new ArrayList<>(groups.size());
    for (Group curGroup : groups) {
      Element groupElement = new Element("group");
      groupElement.setAttribute("id", String.valueOf(curGroup.getId()));
      groupElement.setAttribute("name", curGroup.getName());
      for (TCSObjectReference<?> curRef : curGroup.getMembers()) {
        Element memberElement = new Element("member");
        memberElement.setAttribute("name", curRef.getName());
        groupElement.addContent(memberElement);
      }
      for (Map.Entry<String, String> curEntry
               : curGroup.getProperties().entrySet()) {
        Element propertyElement = new Element("property");
        propertyElement.setAttribute("name", curEntry.getKey());
        propertyElement.setAttribute("value", curEntry.getValue());
        groupElement.addContent(propertyElement);
      }
      result.add(groupElement);
    }
    return result;
  }

  /**
   * Returns a list of XML elements for all static routes in a model.
   *
   * @param model The model data.
   * @return A list of XML elements for all static routes in a model.
   */
  private static List<Element> getXMLStaticRoutes(Model model) {
    Set<StaticRoute> routes = new TreeSet<>(Comparators.objectsById());
    routes.addAll(model.getStaticRoutes(null));
    List<Element> result = new ArrayList<>(routes.size());
    for (StaticRoute curRoute : routes) {
      Element routeElement = new Element("staticRoute");
      routeElement.setAttribute("id", String.valueOf(curRoute.getId()));
      routeElement.setAttribute("name", curRoute.getName());
      for (TCSObjectReference<Point> curRef : curRoute.getHops()) {
        Element hopElement = new Element("hop");
        hopElement.setAttribute("name", curRef.getName());
        routeElement.addContent(hopElement);
      }
      for (Map.Entry<String, String> curEntry
               : curRoute.getProperties().entrySet()) {
        Element propertyElement = new Element("property");
        propertyElement.setAttribute("name", curEntry.getKey());
        propertyElement.setAttribute("value", curEntry.getValue());
        routeElement.addContent(propertyElement);
      }
      result.add(routeElement);
    }
    return result;
  }

  private static List<Element> getXMLVisualLayouts(Model model) {
    Set<VisualLayout> layouts = new TreeSet<>(Comparators.objectsById());
    layouts.addAll(model.getObjectPool().getObjects(VisualLayout.class));

    List<Element> result = new ArrayList<>(layouts.size());
    for (VisualLayout curLayout : layouts) {
      Element visuLayoutElement = new Element("visualLayout");
      visuLayoutElement.setAttribute("id", String.valueOf(curLayout.getId()));
      visuLayoutElement.setAttribute("name", curLayout.getName());
      visuLayoutElement.setAttribute("scaleX",
                                     String.valueOf(curLayout.getScaleX()));
      visuLayoutElement.setAttribute("scaleY",
                                     String.valueOf(curLayout.getScaleY()));

      // Persist named colors.
      for (Map.Entry<String, Color> colorEntry
               : curLayout.getColors().entrySet()) {
        Element colorElement = new Element("color");

        colorElement.setAttribute("name", colorEntry.getKey());
        colorElement.setAttribute("redValue",
                                  Integer.toString(colorEntry.getValue().getRed()));
        colorElement.setAttribute("greenValue",
                                  Integer.toString(colorEntry.getValue().getGreen()));
        colorElement.setAttribute("blueValue",
                                  Integer.toString(colorEntry.getValue().getBlue()));

        visuLayoutElement.addContent(colorElement);
      }
      // Separate our various kinds of layout elements.
      List<ShapeLayoutElement> shapeLayoutElements = new LinkedList<>();
      List<ImageLayoutElement> imageLayoutElements = new LinkedList<>();
      List<ModelLayoutElement> modelLayoutElements = new LinkedList<>();

      for (LayoutElement layoutElement : curLayout.getLayoutElements()) {
        if (layoutElement instanceof ShapeLayoutElement) {
          shapeLayoutElements.add((ShapeLayoutElement) layoutElement);
        }
        else if (layoutElement instanceof ImageLayoutElement) {
          imageLayoutElements.add((ImageLayoutElement) layoutElement);
        }
        else if (layoutElement instanceof ModelLayoutElement) {
          modelLayoutElements.add((ModelLayoutElement) layoutElement);
        }
        // XXX GroupLayoutElement is not implemented, yet.
//        else if (layoutElement instanceof GroupLayout)
      }
      // Sort layout elements. This isn't strictly necessary, but nice for diffs
      // of model contents.
      Collections.sort(modelLayoutElements,
                       Comparators.modelLayoutElementsByName());

      // Persist ShapeLayoutElements
      for (ShapeLayoutElement curSLE : shapeLayoutElements) {
        Element sleElement = new Element("shapeLayoutElement");

        sleElement.setAttribute("layer", String.valueOf(curSLE.getLayer()));

        for (Map.Entry<String, String> prop : curSLE.getProperties().entrySet()) {
          Element propElement = new Element("property");

          propElement.setAttribute("name", prop.getKey());
          propElement.setAttribute("value", prop.getValue());
          sleElement.addContent(propElement);
        }

        visuLayoutElement.addContent(sleElement);
      }
//      // Persist ImageLayoutElements
//      for (ImageLayoutElement curILE : imageLayoutElements) {
//        Element ileElement = new Element("imageLayoutElement");
//
//        ileElement.setAttribute("layer", String.valueOf(curILE.getLayer()));
//
//        // XXX Persist image data/a reference on the file containing it.
//
//        for (Map.Entry<String, String> prop : curILE.getProperties().entrySet()) {
//          Element propElement = new Element("property");
//
//          propElement.setAttribute("name", prop.getKey());
//          propElement.setAttribute("value", prop.getValue());
//          ileElement.addContent(propElement);
//        }
//
//        visuLayoutElement.addContent(ileElement);
//      }

      // Persist ModelLayoutElements
      for (ModelLayoutElement curMLE : modelLayoutElements) {
        Element mleElement = new Element("modelLayoutElement");

        // Don't use the result of getVisualizedObject() directly, since the
        // name of the object might have changed...
        TCSObject<?> vObj
            = model.getObjectPool().getObject(curMLE.getVisualizedObject());
        // Don't persist layout elements for model elements that don't exist,
        // but leave a log message in that case.
        if (vObj == null) {
          log.error("Visualized object " + curMLE.getVisualizedObject()
              + " does not exist (any more?), not persisting layout element");
          continue;
        }
        mleElement.setAttribute("visualizedObjectName",
                                vObj.getName());
        mleElement.setAttribute("layer", String.valueOf(curMLE.getLayer()));

        for (Map.Entry<String, String> prop : curMLE.getProperties().entrySet()) {
          Element propElement = new Element("property");

          propElement.setAttribute("name", prop.getKey());
          propElement.setAttribute("value", prop.getValue());
          mleElement.addContent(propElement);
        }

        visuLayoutElement.addContent(mleElement);
      }
      // XXX Persist GroupLayoutElements

      // Add ViewBookmarks.
      for (ViewBookmark curBookmark : curLayout.getViewBookmarks()) {
        Element bookmarkElement = new Element("viewBookmark");

        bookmarkElement.setAttribute("label", curBookmark.getLabel());
        bookmarkElement.setAttribute("centerX",
                                     Integer.toString(curBookmark.getCenterX()));
        bookmarkElement.setAttribute("centerY",
                                     Integer.toString(curBookmark.getCenterY()));
        bookmarkElement.setAttribute("viewScaleX",
                                     Double.toString(curBookmark.getViewScaleX()));
        bookmarkElement.setAttribute("viewScaleY",
                                     Double.toString(curBookmark.getViewScaleY()));
        bookmarkElement.setAttribute("viewRotation",
                                     Integer.toString(curBookmark.getViewRotation()));

        visuLayoutElement.addContent(bookmarkElement);
      }

      for (Map.Entry<String, String> curEntry
               : curLayout.getProperties().entrySet()) {
        Element propertyElement = new Element("property");
        propertyElement.setAttribute("name", curEntry.getKey());
        propertyElement.setAttribute("value", curEntry.getValue());
        visuLayoutElement.addContent(propertyElement);
      }
      result.add(visuLayoutElement);
    }
    return result;
  }

  /**
   * Reads the given list of point elements into the model.
   *
   * @param pointElements The point elements.
   * @param model The model.
   * @throws ObjectExistsException In case of duplicate objects.
   */
  private void readPoints(List<Element> pointElements, Model model)
      throws ObjectExistsException {
    // Add the points.
    for (int i = 0; i < pointElements.size(); i++) {
      Element curPointElement = pointElements.get(i);
      String attrVal;
      Integer pointID;
      try {
        pointID = Integer.valueOf(curPointElement.getAttributeValue("id"));
      }
      catch (NumberFormatException e) {
        pointID = null;
      }
      Point curPoint = model.createPoint(pointID);
      TCSObjectReference<Point> pointRef = curPoint.getReference();
      String pointName = curPointElement.getAttributeValue("name");
      if (pointName == null || pointName.isEmpty()) {
        pointName = "PointName" + i + "Unknown";
      }
      model.getObjectPool().renameObject(pointRef, pointName);
      Triple position = new Triple();
      attrVal = curPointElement.getAttributeValue("xPosition");
      if (attrVal != null) {
        position.setX(Long.parseLong(attrVal));
      }
      attrVal = curPointElement.getAttributeValue("yPosition");
      if (attrVal != null) {
        position.setY(Long.parseLong(attrVal));
      }
      attrVal = curPointElement.getAttributeValue("zPosition");
      if (attrVal != null) {
        position.setZ(Long.parseLong(attrVal));
      }
      model.setPointPosition(pointRef, position);
      attrVal = curPointElement.getAttributeValue("vehicleOrientationAngle");
      if (attrVal != null) {
        model.setPointVehicleOrientationAngle(pointRef,
                                              Double.parseDouble(attrVal));
      }
      Point.Type pType
          = Point.Type.valueOf(curPointElement.getAttributeValue("type"));
      model.setPointType(pointRef, pType);
      List<Element> pointProps = curPointElement.getChildren("property");
      for (int j = 0; j < pointProps.size(); j++) {
        Element curPointPropElement = pointProps.get(j);
        String propName = curPointPropElement.getAttributeValue("name");
        if (propName == null || propName.isEmpty()) {
          propName = "Property" + j + "Unknown";
        }
        String propValue = curPointPropElement.getAttributeValue("value");
        if (propValue == null || propValue.isEmpty()) {
          propValue = "Value" + j + "Unknown";
        }
        model.getObjectPool().setObjectProperty(pointRef, propName,
                                                propValue);
      }
    }
  }

  /**
   * Reads the given list of vehicle elements into the model.
   *
   * @param vehicleElements The vehicle elements.
   * @param model The model.
   * @throws ObjectExistsException In case of duplicate objects.
   */
  private void readVehicles(List<Element> vehicleElements, Model model)
      throws ObjectExistsException {
    // Add the vehicles.
    for (int i = 0; i < vehicleElements.size(); i++) {
      Element curVehicleElement = vehicleElements.get(i);
      String attrVal;
      Integer vehicleID;
      try {
        vehicleID = Integer.valueOf(curVehicleElement.getAttributeValue("id"));
      }
      catch (NumberFormatException e) {
        vehicleID = null;
      }
      Vehicle curVehicle = model.createVehicle(vehicleID);
      TCSObjectReference<Vehicle> vehicleRef = curVehicle.getReference();
      String vehicleName = curVehicleElement.getAttributeValue("name");
      if (vehicleName == null || vehicleName.isEmpty()) {
        vehicleName = "VehicleName" + i + "Unknown";
      }
      model.getObjectPool().renameObject(vehicleRef, vehicleName);
      attrVal = curVehicleElement.getAttributeValue("length",
                                                    "1000");
      model.setVehicleLength(vehicleRef,
                             Integer.parseInt(attrVal));
      attrVal = curVehicleElement.getAttributeValue("energyLevelCritical",
                                                    "30");
      model.setVehicleEnergyLevelCritical(vehicleRef,
                                          Integer.parseInt(attrVal));
      attrVal = curVehicleElement.getAttributeValue("energyLevelGood",
                                                    "90");
      model.setVehicleEnergyLevelGood(vehicleRef,
                                      Integer.parseInt(attrVal));
      List<Element> properties = curVehicleElement.getChildren("property");
      for (int j = 0; j < properties.size(); j++) {
        Element curPropElement = properties.get(j);
        String curKey = curPropElement.getAttributeValue("name");
        if (curKey == null || curKey.isEmpty()) {
          curKey = "Key" + j + "Unknown";
        }
        String curValue = curPropElement.getAttributeValue("value");
        if (curValue == null || curValue.isEmpty()) {
          curValue = "Value" + j + "Unknown";
        }
        model.getObjectPool().setObjectProperty(vehicleRef, curKey, curValue);
      }
    }
  }

  /**
   * Reads the given list of path elements into the model.
   *
   * @param pathElements The path elements.
   * @param pointElements The point elements.
   * @param model The model.
   * @throws ObjectExistsException In case of duplicate objects.
   */
  private void readPaths(List<Element> pathElements,
                         List<Element> pointElements,
                         Model model)
      throws ObjectExistsException {
    // Add the paths.
    for (int i = 0; i < pathElements.size(); i++) {
      Element curPathElement = pathElements.get(i);
      Integer pathID;
      try {
        pathID = Integer.valueOf(curPathElement.getAttributeValue("id"));
      }
      catch (NumberFormatException e) {
        pathID = null;
      }
      String srcName = curPathElement.getAttributeValue("sourcePoint");
      if (srcName == null || srcName.isEmpty()) {
        srcName = "SourcePoint" + i + "Unknown";
      }
      Point srcPoint = model.getPoint(srcName);
      String destName = curPathElement.getAttributeValue("destinationPoint");
      if (destName == null || destName.isEmpty()) {
        destName = "DestinationPoint" + i + "Unknown";
      }
      Point destPoint = model.getPoint(destName);
      Path curPath = model.createPath(
          pathID, srcPoint.getReference(), destPoint.getReference());
      TCSObjectReference<Path> pathRef = curPath.getReference();
      String pathName = curPathElement.getAttributeValue("name");
      if (pathName == null || pathName.isEmpty()) {
        pathName = "PathName" + i + "Unknown";
      }
      model.getObjectPool().renameObject(pathRef, pathName);
      model.setPathLength(pathRef,
                          Long.parseLong(curPathElement.getAttributeValue("length", "1")));
      model.setPathRoutingCost(pathRef,
                               Long.parseLong(curPathElement.getAttributeValue("routingCost", "1")));
      model.setPathLocked(pathRef,
                          Boolean.parseBoolean(curPathElement.getAttributeValue("locked", "false")));
      int maxV;
      try {
        maxV = Integer.parseInt(curPathElement.getAttributeValue("maxVelocity"));
      }
      catch (NumberFormatException e) {
        maxV = 0;
      }
      model.setPathMaxVelocity(pathRef, maxV);
      int maxRV;
      try {
        maxRV = Integer.parseInt(curPathElement.getAttributeValue("maxReverseVelocity"));
      }
      catch (NumberFormatException e) {
        maxRV = 0;
      }
      model.setPathMaxReverseVelocity(pathRef, maxRV);
      List<Element> properties = curPathElement.getChildren("property");
      for (int m = 0; m < properties.size(); m++) {
        Element curPropElement = properties.get(m);
        String curKey = curPropElement.getAttributeValue("name");
        if (curKey == null || curKey.isEmpty()) {
          curKey = "Key" + m + "Unknown";
        }
        String curValue = curPropElement.getAttributeValue("value");
        if (curValue == null || curValue.isEmpty()) {
          curValue = "Value" + m + "Unknown";
        }
        model.getObjectPool().setObjectProperty(pathRef, curKey, curValue);
      }
    }

    // Loop through all paths. Add the path to its source point as an outgoing
    // path and to its destination point as an incoming path.
    for (Path curPath : model.getPaths(null)) {
      model.addPointOutgoingPath(curPath.getSourcePoint(),
                                 curPath.getReference());
      model.addPointIncomingPath(curPath.getDestinationPoint(),
                                 curPath.getReference());
    }
  }

  /**
   * Reads the given list of location type elements into the model.
   *
   * @param locTypeElements The location type elements.
   * @param model The model.
   * @throws ObjectExistsException In case of duplicate objects.
   */
  private void readLocationTypes(List<Element> locTypeElements, Model model)
      throws ObjectExistsException {
    // Add the location types.
    for (int i = 0; i < locTypeElements.size(); i++) {
      Element curTypeElement = locTypeElements.get(i);
      Integer typeID;
      try {
        typeID = Integer.valueOf(curTypeElement.getAttributeValue("id"));
      }
      catch (NumberFormatException e) {
        typeID = null;
      }
      LocationType curType = model.createLocationType(typeID);
      TCSObjectReference<LocationType> typeRef = curType.getReference();
      String typeName = curTypeElement.getAttributeValue("name");
      if (typeName == null || typeName.isEmpty()) {
        typeName = "LocationType" + i + "Unknown";
      }
      model.getObjectPool().renameObject(typeRef, typeName);
      List<Element> allowedOperations
          = curTypeElement.getChildren("allowedOperation");
      for (int j = 0; j < allowedOperations.size(); j++) {
        Element curOpElement = allowedOperations.get(j);
        String curOperation = curOpElement.getAttributeValue("name");
        if (curOperation == null || curOperation.isEmpty()) {
          curOperation = "Operation" + j + "Unknown";
        }
        model.addLocationTypeAllowedOperation(typeRef, curOperation);
      }
      List<Element> properties = curTypeElement.getChildren("property");
      for (int k = 0; k < properties.size(); k++) {
        Element curPropElement = properties.get(k);
        String curKey = curPropElement.getAttributeValue("name");
        if (curKey == null || curKey.isEmpty()) {
          curKey = "Key" + k + "Unknown";
        }
        String curValue = curPropElement.getAttributeValue("value");
        if (curValue == null || curValue.isEmpty()) {
          curValue = "Value" + k + "Unknown";
        }
        model.getObjectPool().setObjectProperty(typeRef, curKey, curValue);
      }
    }
  }

  /**
   * Reads the given list of location elements into the model.
   *
   * @param locationElements The location elements.
   * @param model The model.
   * @throws ObjectExistsException In case of duplicate objects.
   */
  private void readLocations(List<Element> locationElements,
                             Model model)
      throws ObjectExistsException {
    // Add the locations.
    for (int i = 0; i < locationElements.size(); i++) {
      Element curLocationElement = locationElements.get(i);
      Integer locID;
      try {
        locID = Integer.valueOf(curLocationElement.getAttributeValue("id"));
      }
      catch (NumberFormatException e) {
        locID = null;
      }
      String typeName = curLocationElement.getAttributeValue("type");
      if (typeName == null || typeName.isEmpty()) {
        typeName = "LocationType" + i + "Unknown";
      }
      TCSObjectReference<LocationType> typeRef
          = model.getLocationType(typeName).getReference();
      Location curLocation = model.createLocation(locID, typeRef);
      TCSObjectReference<Location> locRef = curLocation.getReference();
      String locName = curLocationElement.getAttributeValue("name");
      if (locName == null || locName.isEmpty()) {
        locName = "LocationName" + i + "Unknown";
      }
      model.getObjectPool().renameObject(locRef, locName);
      // Set position.
      Triple position = new Triple();
      String attrVal;
      attrVal = curLocationElement.getAttributeValue("xPosition");
      if (attrVal != null) {
        position.setX(Long.parseLong(attrVal));
      }
      attrVal = curLocationElement.getAttributeValue("yPosition");
      if (attrVal != null) {
        position.setY(Long.parseLong(attrVal));
      }
      attrVal = curLocationElement.getAttributeValue("zPosition");
      if (attrVal != null) {
        position.setZ(Long.parseLong(attrVal));
      }
      model.setLocationPosition(locRef, position);
      // Add links.
      List<Element> linkElements = curLocationElement.getChildren("link");
      for (int j = 0; j < linkElements.size(); j++) {
        Element curLinkElement = linkElements.get(j);
        String pointName = curLinkElement.getAttributeValue("point");
        if (pointName == null || pointName.isEmpty()) {
          pointName = "PointName" + j + "Unknown";
        }
        TCSObjectReference<Point> pointRef
            = model.getPoint(pointName).getReference();
        model.connectLocationToPoint(locRef, pointRef);
        List<Element> allowedOpElements
            = curLinkElement.getChildren("allowedOperation");
        for (Element curOpElement : allowedOpElements) {
          String allowedOp = curOpElement.getAttributeValue("name", "NOP");
          model.addLocationLinkAllowedOperation(locRef, pointRef, allowedOp);
        }
      }
      List<Element> properties = curLocationElement.getChildren("property");
      for (int m = 0; m < properties.size(); m++) {
        Element curPropElement = properties.get(m);
        String curKey = curPropElement.getAttributeValue("name");
        if (curKey == null || curKey.isEmpty()) {
          curKey = "Key" + m + "Unknown";
        }
        String curValue = curPropElement.getAttributeValue("value");
        if (curValue == null || curValue.isEmpty()) {
          curValue = "Value" + m + "Unknown";
        }
        model.getObjectPool().setObjectProperty(locRef, curKey, curValue);
      }
    }

  }

  /**
   * Reads the given list of block elements into the model.
   *
   * @param blockElements The block elements.
   * @param model The model.
   * @throws ObjectExistsException In case of duplicate objects.
   */
  private void readBlocks(List<Element> blockElements, Model model)
      throws ObjectExistsException {
    // Add the blocks.
    for (int i = 0; i < blockElements.size(); i++) {
      Element curBlockElement = blockElements.get(i);
      Integer blockID;
      try {
        blockID = Integer.valueOf(curBlockElement.getAttributeValue("id"));
      }
      catch (NumberFormatException e) {
        blockID = null;
      }
      Block curBlock = model.createBlock(blockID);
      TCSObjectReference<Block> blockRef = curBlock.getReference();
      String blockName = curBlockElement.getAttributeValue("name");
      if (blockName == null || blockName.isEmpty()) {
        blockName = "BlockName" + i + "Unknown";
      }
      model.getObjectPool().renameObject(curBlock.getReference(), blockName);
      // Add members.
      List<Element> memberElements = curBlockElement.getChildren("member");
      for (int j = 0; j < memberElements.size(); j++) {
        Element curMemberElement = memberElements.get(j);
        String memberName = curMemberElement.getAttributeValue("name");
        if (memberName == null || memberName.isEmpty()) {
          memberName = "MemberName" + j + "Unknown";
        }
        TCSResource<?> curMember
            = (TCSResource<?>) model.getObjectPool().getObject(memberName);
        curBlock.addMember(curMember.getReference());
      }
      List<Element> properties = curBlockElement.getChildren("property");
      for (int k = 0; k < properties.size(); k++) {
        Element curPropElement = properties.get(k);
        String curKey = curPropElement.getAttributeValue("name");
        if (curKey == null || curKey.isEmpty()) {
          curKey = "Key" + k + "Unknown";
        }
        String curValue = curPropElement.getAttributeValue("value");
        if (curValue == null || curValue.isEmpty()) {
          curValue = "Value" + k + "Unknown";
        }
        model.getObjectPool().setObjectProperty(blockRef, curKey, curValue);
      }
    }
  }

  /**
   * Reads the given list of group elements into the model.
   *
   * @param groupElements The group elements.
   * @param model The model.
   * @throws ObjectExistsException In case of duplicate objects.
   */
  private void readGroups(List<Element> groupElements, Model model)
      throws ObjectExistsException {
    // Add the groups.
    for (int i = 0; i < groupElements.size(); i++) {
      Element curGroupElement = groupElements.get(i);
      Integer groupID;
      try {
        groupID = Integer.valueOf(curGroupElement.getAttributeValue("id"));
      }
      catch (NumberFormatException e) {
        groupID = null;
      }
      Group curGroup = model.createGroup(groupID);
      TCSObjectReference<Group> groupRef = curGroup.getReference();
      String groupName = curGroupElement.getAttributeValue("name");
      if (groupName == null || groupName.isEmpty()) {
        groupName = "GroupName" + i + "Unknown";
      }
      model.getObjectPool().renameObject(curGroup.getReference(), groupName);
      // Add members.
      List<Element> memberElements = curGroupElement.getChildren("member");
      for (int j = 0; j < memberElements.size(); j++) {
        Element curMemberElement = memberElements.get(j);
        String memberName = curMemberElement.getAttributeValue("name");
        if (memberName == null || memberName.isEmpty()) {
          memberName = "MemberName" + j + "Unknown";
        }
        TCSObject<?> curMember = model.getObjectPool().getObject(memberName);
        curGroup.addMember(curMember.getReference());
      }
      List<Element> properties = curGroupElement.getChildren("property");
      for (int k = 0; k < properties.size(); k++) {
        Element curPropElement = properties.get(k);
        String curKey = curPropElement.getAttributeValue("name");
        if (curKey == null || curKey.isEmpty()) {
          curKey = "Key" + k + "Unknown";
        }
        String curValue = curPropElement.getAttributeValue("value");
        if (curValue == null || curValue.isEmpty()) {
          curValue = "Value" + k + "Unknown";
        }
        model.getObjectPool().setObjectProperty(groupRef, curKey, curValue);
      }
    }
  }

  /**
   * Reads the given list of static route elements into the model.
   *
   * @param staticRouteElements The static route elements.
   * @param model The model.
   * @throws ObjectExistsException In case of duplicate objects.
   */
  private void readStaticRoutes(List<Element> staticRouteElements, Model model)
      throws ObjectExistsException {
    // Add the static routes.
    for (int i = 0; i < staticRouteElements.size(); i++) {
      Element curRouteElement = staticRouteElements.get(i);
      Integer routeID;
      try {
        routeID = Integer.valueOf(curRouteElement.getAttributeValue("id"));
      }
      catch (NumberFormatException e) {
        routeID = null;
      }

      StaticRoute curRoute = model.createStaticRoute(routeID);
      TCSObjectReference<StaticRoute> routeRef = curRoute.getReference();
      String routeName = curRouteElement.getAttributeValue("name");
      if (routeName == null || routeName.isEmpty()) {
        routeName = "RouteName" + i + "Unknown";
      }
      model.getObjectPool().renameObject(curRoute.getReference(), routeName);
      // Add hops.
      List<Element> hopElements = curRouteElement.getChildren("hop");
      for (int j = 0; j < hopElements.size(); j++) {
        Element curHopElement = hopElements.get(j);
        String pointName = curHopElement.getAttributeValue("name");
        if (pointName == null || pointName.isEmpty()) {
          pointName = "PointName" + j + "Unknown";
        }
        TCSObjectReference<Point> pointRef
            = model.getPoint(pointName).getReference();
        curRoute.addHop(pointRef);
      }
      List<Element> properties = curRouteElement.getChildren("property");
      for (int k = 0; k < properties.size(); k++) {
        Element curPropElement = properties.get(k);
        String curKey = curPropElement.getAttributeValue("name");
        if (curKey == null || curKey.isEmpty()) {
          curKey = "Key" + k + "Unknown";
        }
        String curValue = curPropElement.getAttributeValue("value");
        if (curValue == null || curValue.isEmpty()) {
          curValue = "Value" + k + "Unknown";
        }
        model.getObjectPool().setObjectProperty(routeRef, curKey, curValue);
      }
    }
  }

  /**
   * Reads the given list of visual layout elements into the model.
   *
   * @param visuLayoutElements The visual layout elements.
   * @param model The model.
   * @throws ObjectExistsException In case of duplicate objects.
   */
  private void readVisualLayouts(List<Element> visuLayoutElements,
                                 Model model)
      throws ObjectExistsException {
    // Add the visual layout elements.
    for (int i = 0; i < visuLayoutElements.size(); i++) {
      Element vLayoutElement = visuLayoutElements.get(i);
      String attrVal;
      Integer layoutID;
      try {
        layoutID = Integer.valueOf(vLayoutElement.getAttributeValue("id"));
      }
      catch (NumberFormatException exc) {
        layoutID = null;
      }

      VisualLayout vLayout = model.createVisualLayout(layoutID);

      attrVal = vLayoutElement.getAttributeValue("name");
      if (attrVal != null) {
        model.getObjectPool().renameObject(vLayout.getReference(), attrVal);
      }

      attrVal = vLayoutElement.getAttributeValue("scaleX");
      if (attrVal != null) {
        model.setVisualLayoutScaleX(vLayout.getReference(),
                                    Double.parseDouble(attrVal));
      }

      attrVal = vLayoutElement.getAttributeValue("scaleY");
      if (attrVal != null) {
        model.setVisualLayoutScaleY(vLayout.getReference(),
                                    Double.parseDouble(attrVal));
      }

      attrVal = vLayoutElement.getAttributeValue("locTheme");
      if (attrVal != null) {
        vLayout.setProperty(ObjectPropConstants.LOCATION_THEME_CLASS, attrVal);
      }

      attrVal = vLayoutElement.getAttributeValue("vehTheme");
      if (attrVal != null) {
        vLayout.setProperty(ObjectPropConstants.VEHICLE_THEME_CLASS, attrVal);
      }

      // Named colors in the visual layout.
      Map<String, Color> colors = new HashMap<>();
      List<Element> colorElements = vLayoutElement.getChildren("color");
      for (Element colorElement : colorElements) {
        String colorName = colorElement.getAttributeValue("name");
        int redValue
            = Integer.parseInt(colorElement.getAttributeValue("redValue"));
        int greenValue
            = Integer.parseInt(colorElement.getAttributeValue("greenValue"));
        int blueValue
            = Integer.parseInt(colorElement.getAttributeValue("blueValue"));
        colors.put(colorName, new Color(redValue, greenValue, blueValue));
      }
      model.setVisualLayoutColors(vLayout.getReference(), colors);

      Set<LayoutElement> layoutElements = new HashSet<>();
      // ShapeLayoutElements
      List<Element> sleElements
          = vLayoutElement.getChildren("shapeLayoutElement");
      for (Element sleElement : sleElements) {
        ShapeLayoutElement sle = new ShapeLayoutElement();

        attrVal = sleElement.getAttributeValue("layer");
        if (attrVal != null) {
          sle.setLayer(Integer.parseInt(attrVal));
        }

        Map<String, String> props = new HashMap<>();
        for (Element propElem : sleElement.getChildren("property")) {
          String key = propElem.getAttributeValue("name");
          String value = propElem.getAttributeValue("value");

          props.put(key, value);
        }
        sle.setProperties(props);

        layoutElements.add(sle);
      }

//        // ImageLayoutElements
//        List<Element> ileElements =
//            vLayoutElement.getChildren("imageLayoutElement");
//        for (Element ileElement : ileElements) {
//          ImageLayoutElement ile = new ImageLayoutElement();
//
//          attrVal = ileElement.getAttributeValue("layer");
//          if (attrVal != null) {
//            ile.setLayer(Integer.parseInt(attrVal));
//          }
//
//          Map<String, String> props = new HashMap<>();
//          for (Element propElem : ileElement.getChildren("property")) {
//            String key = propElem.getAttributeValue("name");
//            String value = propElem.getAttributeValue("value");
//
//            props.put(key, value);
//          }
//          ile.setProperties(props);
//          
//          // XXX Load image data from somewhere...
//
//          layoutElements.add(ile);
//        }
      // ModelLayoutElements
      List<Element> mleElements
          = vLayoutElement.getChildren("modelLayoutElement");
      for (Element mleElement : mleElements) {
        attrVal = mleElement.getAttributeValue("visualizedObjectName");
        TCSObject<?> vObj = model.getObjectPool().getObject(attrVal);
        ModelLayoutElement mle = new ModelLayoutElement(vObj.getReference());

        attrVal = mleElement.getAttributeValue("layer");
        if (attrVal != null) {
          mle.setLayer(Integer.parseInt(attrVal));
        }

        Map<String, String> props = new HashMap<>();
        for (Element propElem : mleElement.getChildren("property")) {
          String key = propElem.getAttributeValue("name");
          String value = propElem.getAttributeValue("value");

          props.put(key, value);
        }
        mle.setProperties(props);

        layoutElements.add(mle);
      }

      // XXX Load GroupLayoutElements
      model.setVisualLayoutElements(vLayout.getReference(), layoutElements);

      // ViewBookmarks
      List<ViewBookmark> viewBookmarks = new LinkedList<>();
      for (Element bookmarkElement : vLayoutElement.getChildren("viewBookmark")) {
        ViewBookmark bookmark = new ViewBookmark();
        bookmark.setLabel(bookmarkElement.getAttributeValue("label"));
        bookmark.setCenterX(
            Integer.parseInt(bookmarkElement.getAttributeValue("centerX")));
        bookmark.setCenterY(
            Integer.parseInt(bookmarkElement.getAttributeValue("centerY")));
        bookmark.setViewScaleX(Double.parseDouble(
            bookmarkElement.getAttributeValue("viewScaleX")));
        bookmark.setViewScaleY(Double.parseDouble(
            bookmarkElement.getAttributeValue("viewScaleY")));
        bookmark.setViewRotation(
            Integer.parseInt(bookmarkElement.getAttributeValue("viewRotation")));
        viewBookmarks.add(bookmark);
      }
      model.setVisualLayoutViewBookmarks(vLayout.getReference(), viewBookmarks);

      List<Element> properties = vLayoutElement.getChildren("property");
      for (int k = 0; k < properties.size(); k++) {
        Element curPropElement = properties.get(k);
        String curKey = curPropElement.getAttributeValue("name");
        if (curKey == null || curKey.isEmpty()) {
          curKey = "Key" + k + "Unknown";
        }
        String curValue = curPropElement.getAttributeValue("value");
        if (curValue == null || curValue.isEmpty()) {
          curValue = "Value" + k + "Unknown";
        }
        model.getObjectPool().setObjectProperty(vLayout.getReference(), curKey, curValue);
      }
    }
  }
}
