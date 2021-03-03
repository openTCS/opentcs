/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.persistence;

import com.google.common.base.Strings;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.data.ObjectExistsException;
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
import org.opentcs.util.persistence.binding.AllowedOperationTO;
import org.opentcs.util.persistence.binding.BlockTO;
import org.opentcs.util.persistence.binding.PlantModelTO;
import org.opentcs.util.persistence.binding.GroupTO;
import org.opentcs.util.persistence.binding.LocationTO;
import org.opentcs.util.persistence.binding.LocationTypeTO;
import org.opentcs.util.persistence.binding.MemberTO;
import org.opentcs.util.persistence.binding.PathTO;
import org.opentcs.util.persistence.binding.PointTO;
import org.opentcs.util.persistence.binding.PropertyTO;
import org.opentcs.util.persistence.binding.StaticRouteTO;
import org.opentcs.util.persistence.binding.VehicleTO;
import org.opentcs.util.persistence.binding.VisualLayoutTO;
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
  static final String VERSION_STRING = "0.0.2";
  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(XMLModel002Builder.class);

  /**
   * Creates a new XMLModel001Builder.
   */
  public XMLModel002Builder() {
  }

  // Implementation of interface XMLModelWriter starts here.
  @Override
  public String getVersionString() {
    return VERSION_STRING;
  }

  @Override
  public void writeXMLModel(Model model,
                            @Nullable String name,
                            OutputStream outStream)
      throws IOException {
    Objects.requireNonNull(model, "model is null");
    Objects.requireNonNull(outStream, "outStream is null");

    PlantModelTO plantModel = new PlantModelTO();
    plantModel.setName(name != null ? name : model.getName());
    plantModel.setVersion(VERSION_STRING)
        .setPoints(getPoints(model))
        .setPaths(getPath(model))
        .setVehicles(getVehicles(model))
        .setLocationTypes(getLocationTypes(model))
        .setLocations(getLocations(model))
        .setBlocks(getBlocks(model))
        .setStaticRoutes(getStaticRoutes(model))
        .setGroups(getGroups(model))
        .setVisualLayouts(getVisualLayouts(model));

    String xmlOutput = plantModel.toXml();
    outStream.write(xmlOutput.getBytes());
    outStream.flush();
  }

  // Implementation of interface XMLModelReader starts here.
  @Override
  public String readModelName(InputStream inStream)
      throws InvalidModelException, IOException {
    requireNonNull(inStream, "inStream");

    String modelName = PlantModelTO.fromXml(inStream).getName();

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

    PlantModelTO plantModel = PlantModelTO.fromXml(inStream);

    String modelName = plantModel.getName();
    if (modelName.isEmpty()) {
      modelName = "ModelNameMissing";
      //throw new InvalidModelException("Model name missing");
    }

    String modelVersion = plantModel.getVersion();
    if (!VERSION_STRING.equals(modelVersion)) {
      throw new InvalidModelException("Bad model version: " + modelVersion);
    }

    // Clear the model before reading the new data.
    model.clear();
    // Set the model's name.
    model.setName(modelName);

    // Fill the model with components.
    List<PointTO> pointElements = plantModel.getPoints();
    List<PathTO> pathElements = plantModel.getPaths();
    List<VehicleTO> vehicleElements = plantModel.getVehicles();
    List<LocationTypeTO> locTypeElements = plantModel.getLocationTypes();
    List<LocationTO> locationElements = plantModel.getLocations();
    List<BlockTO> blockElements = plantModel.getBlocks();
    List<StaticRouteTO> staticRouteElements = plantModel.getStaticRoutes();
    List<GroupTO> groupElements = plantModel.getGroups();
    List<VisualLayoutTO> visuLayoutElements = plantModel.getVisualLayouts();
    try {
      readPoints(pointElements, model);
      readPaths(pathElements, model);
      readVehicles(vehicleElements, model);
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

  /**
   * Returns a list of {@link PointTO Points} for all points in a model.
   *
   * @param model The model data.
   * @return A list of {@link PointTO Points} for all points in a model.
   */
  private static List<PointTO> getPoints(Model model) {
    Set<Point> points = new TreeSet<>(Comparators.objectsByName());
    points.addAll(model.getPoints(null));
    List<PointTO> result = new ArrayList<>();
    for (Point curPoint : points) {
      PointTO point = new PointTO();
      point.setName(curPoint.getName());
      point.setxPosition(curPoint.getPosition().getX())
          .setyPosition(curPoint.getPosition().getY())
          .setzPosition(curPoint.getPosition().getZ())
          .setVehicleOrientationAngle((float) curPoint.getVehicleOrientationAngle())
          .setType(curPoint.getType().toString());

      List<PointTO.OutgoingPath> outgoingPathList = new ArrayList<>();
      Set<TCSObjectReference<Path>> outgoingPathRefsSorted
          = new TreeSet<>(Comparators.referencesByName());
      outgoingPathRefsSorted.addAll(curPoint.getOutgoingPaths());
      for (TCSObjectReference<Path> curRef : outgoingPathRefsSorted) {
        outgoingPathList.add(new PointTO.OutgoingPath().setName(curRef.getName()));
      }
      point.setOutgoingPaths(outgoingPathList)
          .setProperties(getProperties(curPoint));

      result.add(point);
    }
    return result;
  }

  /**
   * Returns a list of {@link PathTO Paths} for all paths in a model.
   *
   * @param model The model data.
   * @return A list of {@link PathTO Paths} for all paths in a model.
   */
  private static List<PathTO> getPath(Model model) {
    Set<Path> paths = new TreeSet<>(Comparators.objectsByName());
    paths.addAll(model.getPaths(null));
    List<PathTO> result = new ArrayList<>();
    for (Path curPath : paths) {
      PathTO path = new PathTO();
      path.setName(curPath.getName());
      path.setSourcePoint(curPath.getSourcePoint().getName())
          .setDestinationPoint(curPath.getDestinationPoint().getName())
          .setLength(curPath.getLength())
          .setRoutingCost(curPath.getRoutingCost())
          .setMaxVelocity((long) curPath.getMaxVelocity())
          .setMaxReverseVelocity((long) curPath.getMaxReverseVelocity())
          .setLocked(curPath.isLocked())
          .setProperties(getProperties(curPath));
      result.add(path);
    }
    return result;
  }

  /**
   * Returns a list of {@link VehicleTO Vehicles} for all vehicles in a model.
   *
   * @param model The model data.
   * @return A list of {@link VehicleTO Vehicles} for all vehicles in a model.
   */
  private static List<VehicleTO> getVehicles(Model model) {
    Set<Vehicle> vehicles = new TreeSet<>(Comparators.objectsByName());
    vehicles.addAll(model.getVehicles(null));
    List<VehicleTO> result = new ArrayList<>();
    for (Vehicle curVehicle : vehicles) {
      VehicleTO vehicle = new VehicleTO();
      vehicle.setName(curVehicle.getName());
      vehicle.setLength((long) curVehicle.getLength())
          .setEnergyLevelCritical((long) curVehicle.getEnergyLevelCritical())
          .setEnergyLevelGood((long) curVehicle.getEnergyLevelGood())
          .setProperties(getProperties(curVehicle));
      result.add(vehicle);
    }

    return result;
  }

  /**
   * Returns a list of {@link LocationTypeTO LocationTypes} for all location types in a model.
   *
   * @param model The model data.
   * @return A list of {@link LocationTypeTO LocationType} for all location types in a model.
   */
  private static List<LocationTypeTO> getLocationTypes(Model model) {
    Set<LocationType> locTypes = new TreeSet<>(Comparators.objectsByName());
    locTypes.addAll(model.getLocationTypes(null));
    List<LocationTypeTO> result = new ArrayList<>();
    for (LocationType curType : locTypes) {
      LocationTypeTO locationType = new LocationTypeTO();
      locationType.setName(curType.getName());

      List<AllowedOperationTO> operations = new ArrayList<>();
      for (String curOperation : curType.getAllowedOperations()) {
        AllowedOperationTO operation = new AllowedOperationTO();
        operation.setName(curOperation);
        operations.add(operation);
      }
      locationType.setAllowedOperations(operations)
          .setProperties(getProperties(curType));

      result.add(locationType);
    }
    return result;
  }

  /**
   * Returns a list of {@link LocationTO Locations} for all locations in a model.
   *
   * @param model The model data.
   * @return A list of {@link LocationTO Locations} for all locations in a model.
   */
  private static List<LocationTO> getLocations(Model model) {
    Set<Location> locations = new TreeSet<>(Comparators.objectsByName());
    locations.addAll(model.getLocations(null));
    List<LocationTO> result = new ArrayList<>();
    for (Location curLoc : locations) {
      LocationTO location = new LocationTO();
      location.setName(curLoc.getName());
      location.setxPosition(curLoc.getPosition().getX())
          .setyPosition(curLoc.getPosition().getY())
          .setzPosition(curLoc.getPosition().getZ())
          .setType(curLoc.getType().getName());

      List<LocationTO.Link> links = new ArrayList<>();
      for (Location.Link curLink : curLoc.getAttachedLinks()) {
        LocationTO.Link link = new LocationTO.Link()
            .setPoint(curLink.getPoint().getName());

        List<AllowedOperationTO> operations = new ArrayList<>();
        for (String curOperation : curLink.getAllowedOperations()) {
          AllowedOperationTO operation = new AllowedOperationTO();
          operation.setName(curOperation);
          operations.add(operation);
        }
        link.setAllowedOperations(operations);
        links.add(link);
      }
      location.setLinks(links)
          .setProperties(getProperties(curLoc));

      result.add(location);
    }
    return result;
  }

  /**
   * Returns a list of {@link BlockTO Blocks} for all blocks in a model.
   *
   * @param model The model data.
   * @return A list of {@link BlockTO Blocks} for all blocks in a model.
   */
  private static List<BlockTO> getBlocks(Model model) {
    Set<Block> blocks = new TreeSet<>(Comparators.objectsByName());
    blocks.addAll(model.getBlocks(null));
    List<BlockTO> result = new ArrayList<>();
    for (Block curBlock : blocks) {
      BlockTO block = new BlockTO();
      block.setName(curBlock.getName());

      List<MemberTO> members = new ArrayList<>();
      for (TCSResourceReference<?> curRef : curBlock.getMembers()) {
        MemberTO member = new MemberTO();
        member.setName(curRef.getName());
        members.add(member);
      }
      block.setMembers(members)
          .setProperties(getProperties(curBlock));

      result.add(block);
    }
    return result;
  }

  /**
   * Returns a list of {@link StaticRouteTO StaticRoutes} for all static routes in a model.
   *
   * @param model The model data.
   * @return A list of {@link StaticRouteTO StaticRoutes} for all static routes in a model.
   */
  private static List<StaticRouteTO> getStaticRoutes(Model model) {
    Set<StaticRoute> routes = new TreeSet<>(Comparators.objectsByName());
    routes.addAll(model.getStaticRoutes(null));
    List<StaticRouteTO> result = new ArrayList<>();
    for (StaticRoute curRoute : routes) {
      StaticRouteTO staticRoute = new StaticRouteTO();
      staticRoute.setName(curRoute.getName());

      List<StaticRouteTO.Hop> hops = new ArrayList<>();
      for (TCSObjectReference<Point> curRef : curRoute.getHops()) {
        hops.add(new StaticRouteTO.Hop().setName(curRef.getName()));
      }
      staticRoute.setHops(hops)
          .setProperties(getProperties(curRoute));

      result.add(staticRoute);
    }
    return result;
  }

  /**
   * Returns a list of {@link GroupTO Groups} for all groups in a model.
   *
   * @param model The model data.
   * @return A list of {@link GroupTO Groups} for all groups in a model.
   */
  private static List<GroupTO> getGroups(Model model) {
    Set<Group> groups = new TreeSet<>(Comparators.objectsByName());
    groups.addAll(model.getGroups(null));
    List<GroupTO> result = new ArrayList<>();
    for (Group curGroup : groups) {
      GroupTO group = new GroupTO();
      group.setName(curGroup.getName());

      List<MemberTO> members = new ArrayList<>();
      for (TCSObjectReference<?> curRef : curGroup.getMembers()) {
        MemberTO member = new MemberTO();
        member.setName(curRef.getName());
        members.add(member);
      }
      group.setMembers(members)
          .setProperties(getProperties(curGroup));

      result.add(group);
    }
    return result;
  }

  /**
   * Returns a list of {@link VisualLayoutTO VisualLayouts} for all visual layouts in a model.
   *
   * @param model The model data.
   * @return A list of {@link VisualLayoutTO VisualLayouts} for all visual layouts in a model.
   */
  private static List<VisualLayoutTO> getVisualLayouts(Model model) {
    Set<VisualLayout> layouts = new TreeSet<>(Comparators.objectsByName());
    layouts.addAll(model.getObjectPool().getObjects(VisualLayout.class));
    List<VisualLayoutTO> result = new ArrayList<>();
    for (VisualLayout curLayout : layouts) {
      VisualLayoutTO layout = new VisualLayoutTO();
      layout.setName(curLayout.getName());
      layout.setScaleX((float) curLayout.getScaleX())
          .setScaleY((float) curLayout.getScaleY());

      // Persist named colors.
      List<VisualLayoutTO.Color> colors = new ArrayList<>();
      for (Map.Entry<String, Color> colorEntry : curLayout.getColors().entrySet()) {
        colors.add(new VisualLayoutTO.Color()
            .setName(colorEntry.getKey())
            .setRedValue((long) colorEntry.getValue().getRed())
            .setGreenValue((long) colorEntry.getValue().getGreen())
            .setBlueValue((long) colorEntry.getValue().getBlue()));
      }
      layout.setColors(colors);

      // Separate our various kinds of layout elements.
      List<ShapeLayoutElement> shapeLayoutElements = new LinkedList<>();
      List<ImageLayoutElement> imageLayoutElements = new LinkedList<>();
      Map<TCSObject<?>, ModelLayoutElement> modelLayoutElements
          = new TreeMap<>(Comparators.objectsByName());

      for (LayoutElement layoutElement : curLayout.getLayoutElements()) {
        if (layoutElement instanceof ShapeLayoutElement) {
          shapeLayoutElements.add((ShapeLayoutElement) layoutElement);
        }
        else if (layoutElement instanceof ImageLayoutElement) {
          imageLayoutElements.add((ImageLayoutElement) layoutElement);
        }
        else if (layoutElement instanceof ModelLayoutElement) {
          // Map the result of getVisualizedObject() to the corresponding TCSObject, since the name
          // of the TCSObject might change but won't be changed in the reference the 
          // ModelLayoutElement holds.
          ModelLayoutElement mle = (ModelLayoutElement) layoutElement;
          TCSObject<?> vObj = model.getObjectPool().getObject(mle.getVisualizedObject());
          // Don't persist layout elements for model elements that don't exist, but leave a log 
          // message in that case.
          if (vObj == null) {
            LOG.error("Visualized object {} does not exist (any more?), not persisting layout element",
                      mle.getVisualizedObject());
            continue;
          }
          modelLayoutElements.put(vObj, mle);
        }
        // XXX GroupLayoutElement is not implemented, yet.
//        else if (layoutElement instanceof GroupLayout)
      }

      // Persist ImageLayoutElement 
      // The xml shema definition does not include ImageLayoutElement, yet.
//      List<VisualLayoutTO.ImageLayoutElement> ilElements = new ArrayList<>();
//      for (ImageLayoutElement curILE : imageLayoutElements) {
//        VisualLayoutTO.ImageLayoutElement ilElement = new VisualLayoutTO.ImageLayoutElement();
//        ilElement.setLayer((long) curILE.getLayer());
//        
//        // XXX Persist image data/a reference on the file containing it.
//        List<PropertyTO> properties = new ArrayList<>();
//        for (Map.Entry<String, String> curEntry : curILE.getProperties().entrySet()) {
//          PropertyTO property = new PropertyTO();
//          property.setName(curEntry.getKey());
//          property.setValue(curEntry.getValue());
//          properties.add(property);
//        }
//        ilElement.setProperties(properties);
//      }
      // Persist ShapeLayoutElements
      List<VisualLayoutTO.ShapeLayoutElement> slElements = new ArrayList<>();
      for (ShapeLayoutElement curSLE : shapeLayoutElements) {
        VisualLayoutTO.ShapeLayoutElement slElement = new VisualLayoutTO.ShapeLayoutElement();
        slElement.setLayer((long) curSLE.getLayer());

        List<PropertyTO> properties = new ArrayList<>();
        for (Map.Entry<String, String> curEntry : curSLE.getProperties().entrySet()) {
          PropertyTO property = new PropertyTO();
          property.setName(curEntry.getKey());
          property.setValue(curEntry.getValue());
          properties.add(property);
        }
        slElement.setProperties(properties);
      }
      layout.setShapeLayoutElements(slElements);

      // Persist ModelLayoutElements
      List<VisualLayoutTO.ModelLayoutElement> mlElements = new ArrayList<>();
      for (Map.Entry<TCSObject<?>, ModelLayoutElement> curMLE : modelLayoutElements.entrySet()) {
        VisualLayoutTO.ModelLayoutElement mlElement = new VisualLayoutTO.ModelLayoutElement();

        mlElement.setVisualizedObjectName(curMLE.getKey().getName());
        mlElement.setLayer((long) curMLE.getValue().getLayer());

        List<PropertyTO> properties = new ArrayList<>();
        for (Map.Entry<String, String> curEntry : curMLE.getValue().getProperties().entrySet()) {
          PropertyTO property = new PropertyTO();
          property.setName(curEntry.getKey());
          property.setValue(curEntry.getValue());
          properties.add(property);
        }
        mlElement.setProperties(properties);

        mlElements.add(mlElement);
      }
      layout.setModelLayoutElements(mlElements);

      // Add ViewBookmarks.
      List<VisualLayoutTO.ViewBookmark> viewBookmarks = new ArrayList<>();
      for (ViewBookmark curBookmark : curLayout.getViewBookmarks()) {
        viewBookmarks.add(new VisualLayoutTO.ViewBookmark()
            .setLabel(curBookmark.getLabel())
            .setCenterX(curBookmark.getCenterX())
            .setCenterY(curBookmark.getCenterY())
            .setViewScaleX((float) curBookmark.getViewScaleX())
            .setViewScaleY((float) curBookmark.getViewScaleY())
            .setViewRotation(curBookmark.getViewRotation()));
      }
      layout.setViewBookmarks(viewBookmarks)
          .setProperties(getProperties(curLayout));

      result.add(layout);
    }
    return result;
  }

  /**
   * Reads the given list of {@link PointTO Points} into the model.
   *
   * @param pointTOs The point elements.
   * @param model The model.
   * @throws ObjectExistsException In case of duplicate objects.
   */
  private void readPoints(List<PointTO> pointTOs, Model model)
      throws ObjectExistsException {
    for (PointTO pointTO : pointTOs) {
      Point curPoint = model.createPoint(null);
      TCSObjectReference<Point> pointRef = curPoint.getReference();
      model.getObjectPool().renameObject(pointRef, pointTO.getName());

      Triple position = new Triple();
      position.setX(pointTO.getxPosition());
      position.setY(pointTO.getyPosition());
      position.setZ(pointTO.getzPosition());
      model.setPointPosition(pointRef, position);

      model.setPointVehicleOrientationAngle(pointRef,
                                            pointTO.getVehicleOrientationAngle().doubleValue());

      model.setPointType(pointRef, Point.Type.valueOf(pointTO.getType()));

      setModelProperties(model, pointTO.getProperties(), pointRef);
    }
  }

  /**
   * Reads the given list of {@link VehicleTO Vehicles} into the model.
   *
   * @param vehicleTOs The vehicle elements.
   * @param model The model.
   * @throws ObjectExistsException In case of duplicate objects.
   */
  private void readVehicles(List<VehicleTO> vehicleTOs, Model model)
      throws ObjectExistsException {
    for (VehicleTO vehicleTO : vehicleTOs) {
      Vehicle curVehicle = model.createVehicle(null);
      TCSObjectReference<Vehicle> vehicleRef = curVehicle.getReference();
      model.getObjectPool().renameObject(vehicleRef, vehicleTO.getName());

      model.setVehicleLength(vehicleRef, vehicleTO.getLength().intValue());
      model.setVehicleEnergyLevelCritical(vehicleRef, vehicleTO.getEnergyLevelCritical().intValue());
      model.setVehicleEnergyLevelGood(vehicleRef, vehicleTO.getEnergyLevelGood().intValue());

      setModelProperties(model, vehicleTO.getProperties(), vehicleRef);
    }
  }

  /**
   * Reads the given list of {@link PathTO Paths} into the model.
   *
   * @param pathTOs The path elements.
   * @param model The model.
   * @throws ObjectExistsException In case of duplicate objects.
   */
  private void readPaths(List<PathTO> pathTOs, Model model)
      throws ObjectExistsException {
    for (PathTO pathTO : pathTOs) {
      Point srcPoint = model.getPoint(pathTO.getSourcePoint());
      Point destPoint = model.getPoint(pathTO.getDestinationPoint());

      Path curPath = model.createPath(null, srcPoint.getReference(), destPoint.getReference());
      TCSObjectReference<Path> pathRef = curPath.getReference();
      model.getObjectPool().renameObject(pathRef, pathTO.getName());

      model.setPathLength(pathRef, pathTO.getLength());
      model.setPathRoutingCost(pathRef, pathTO.getRoutingCost());
      model.setPathLocked(pathRef, pathTO.isLocked());
      model.setPathMaxVelocity(pathRef, pathTO.getMaxVelocity().intValue());
      model.setPathMaxReverseVelocity(pathRef, pathTO.getMaxReverseVelocity().intValue());

      setModelProperties(model, pathTO.getProperties(), pathRef);
    }

    // Loop through all paths. Add the path to its source point as an outgoing path and to its 
    // destination point as an incoming path.
    for (Path curPath : model.getPaths(null)) {
      model.addPointOutgoingPath(curPath.getSourcePoint(), curPath.getReference());
      model.addPointIncomingPath(curPath.getDestinationPoint(), curPath.getReference());
    }
  }

  /**
   * Reads the given list of {@link LocationTypeTO LocationTypes} into the model.
   *
   * @param locationTypeTOs The location type elements.
   * @param model The model.
   * @throws ObjectExistsException In case of duplicate objects.
   */
  private void readLocationTypes(List<LocationTypeTO> locationTypeTOs, Model model)
      throws ObjectExistsException {
    for (LocationTypeTO locationTypeTO : locationTypeTOs) {
      LocationType curType = model.createLocationType(null);
      TCSObjectReference<LocationType> typeRef = curType.getReference();
      model.getObjectPool().renameObject(typeRef, locationTypeTO.getName());

      for (AllowedOperationTO operation : locationTypeTO.getAllowedOperations()) {
        model.addLocationTypeAllowedOperation(typeRef, operation.getName());
      }

      setModelProperties(model, locationTypeTO.getProperties(), typeRef);
    }
  }

  /**
   * Reads the given list of {@link LocationTO Locations} into the model.
   *
   * @param locationTOs The location elements.
   * @param model The model.
   * @throws ObjectExistsException In case of duplicate objects.
   */
  private void readLocations(List<LocationTO> locationTOs, Model model)
      throws ObjectExistsException {
    for (LocationTO locationTO : locationTOs) {
      String typeName = Strings.isNullOrEmpty(locationTO.getType())
          ? "LocationType unknown" : locationTO.getType();
      TCSObjectReference<LocationType> typeRef = model.getLocationType(typeName).getReference();
      Location curLocation = model.createLocation(null, typeRef);
      TCSObjectReference<Location> locRef = curLocation.getReference();
      model.getObjectPool().renameObject(locRef, locationTO.getName());

      Triple position = new Triple();
      position.setX(locationTO.getxPosition());
      position.setY(locationTO.getyPosition());
      position.setZ(locationTO.getzPosition());
      model.setLocationPosition(locRef, position);

      for (LocationTO.Link linkTO : locationTO.getLinks()) {
        TCSObjectReference<Point> pointRef = model.getPoint(linkTO.getPoint()).getReference();
        model.connectLocationToPoint(locRef, pointRef);

        for (AllowedOperationTO curOpElement : linkTO.getAllowedOperations()) {
          model.addLocationLinkAllowedOperation(locRef, pointRef, curOpElement.getName());
        }
      }

      setModelProperties(model, locationTO.getProperties(), locRef);
    }

  }

  /**
   * Reads the given list of {@link BlockTO Blocks} into the model.
   *
   * @param blockTOs The block elements.
   * @param model The model.
   * @throws ObjectExistsException In case of duplicate objects.
   */
  private void readBlocks(List<BlockTO> blockTOs, Model model)
      throws ObjectExistsException {
    for (BlockTO blockTO : blockTOs) {
      Block curBlock = model.createBlock(null);
      TCSObjectReference<Block> blockRef = curBlock.getReference();
      model.getObjectPool().renameObject(curBlock.getReference(), blockTO.getName());

      for (MemberTO memberTO : blockTO.getMembers()) {
        TCSResource<?> curMember = (TCSResource<?>) model.getObjectPool()
            .getObject(memberTO.getName());
        curBlock.addMember(curMember.getReference());
      }

      setModelProperties(model, blockTO.getProperties(), blockRef);
    }
  }

  /**
   * Reads the given list of {@link GroupTO Groups} into the model.
   *
   * @param groupTOs The group elements.
   * @param model The model.
   * @throws ObjectExistsException In case of duplicate objects.
   */
  private void readGroups(List<GroupTO> groupTOs, Model model)
      throws ObjectExistsException {
    for (GroupTO groupTO : groupTOs) {
      Group curGroup = model.createGroup(null);
      TCSObjectReference<Group> groupRef = curGroup.getReference();
      model.getObjectPool().renameObject(curGroup.getReference(), groupTO.getName());

      for (MemberTO memberTO : groupTO.getMembers()) {
        TCSObject<?> curMember = model.getObjectPool().getObject(memberTO.getName());
        curGroup.addMember(curMember.getReference());
      }

      setModelProperties(model, groupTO.getProperties(), groupRef);
    }
  }

  /**
   * Reads the given list of {@link StaticRouteTO StaticRoutes} into the model.
   *
   * @param staticRouteTOs The static route elements.
   * @param model The model.
   * @throws ObjectExistsException In case of duplicate objects.
   */
  private void readStaticRoutes(List<StaticRouteTO> staticRouteTOs, Model model)
      throws ObjectExistsException {
    for (StaticRouteTO staticRouteTO : staticRouteTOs) {
      StaticRoute curRoute = model.createStaticRoute(null);
      TCSObjectReference<StaticRoute> routeRef = curRoute.getReference();
      model.getObjectPool().renameObject(curRoute.getReference(), staticRouteTO.getName());

      for (StaticRouteTO.Hop hopTO : staticRouteTO.getHops()) {
        curRoute.addHop(model.getPoint(hopTO.getName()).getReference());
      }

      setModelProperties(model, staticRouteTO.getProperties(), routeRef);
    }
  }

  /**
   * Reads the given list of {@link VisualLayoutTO VisualLayouts} into the model.
   *
   * @param visuLayoutTOs The visual layout elements.
   * @param model The model.
   * @throws ObjectExistsException In case of duplicate objects.
   */
  private void readVisualLayouts(List<VisualLayoutTO> visuLayoutTOs, Model model)
      throws ObjectExistsException {
    for (VisualLayoutTO visualLayoutTO : visuLayoutTOs) {
      VisualLayout vLayout = model.createVisualLayout(null);
      TCSObjectReference<VisualLayout> layoutRef = vLayout.getReference();
      model.getObjectPool().renameObject(layoutRef, visualLayoutTO.getName());

      model.setVisualLayoutScaleX(layoutRef, visualLayoutTO.getScaleX());
      model.setVisualLayoutScaleY(layoutRef, visualLayoutTO.getScaleY());

      // Named colors in the visual layout.
      Map<String, Color> colors = new HashMap<>();
      for (VisualLayoutTO.Color colorTO : visualLayoutTO.getColors()) {
        colors.put(colorTO.getName(), new Color(colorTO.getRedValue().intValue(),
                                                colorTO.getGreenValue().intValue(),
                                                colorTO.getBlueValue().intValue()));
      }
      model.setVisualLayoutColors(layoutRef, colors);

      Set<LayoutElement> layoutElements = new HashSet<>();
      // ShapeLayoutElements
      for (VisualLayoutTO.ShapeLayoutElement slElement : visualLayoutTO.getShapeLayoutElements()) {
        ShapeLayoutElement sle = new ShapeLayoutElement();

        sle.setLayer(slElement.getLayer().intValue());

        Map<String, String> properties = new HashMap<>();
        for (PropertyTO propertyTO : slElement.getProperties()) {
          properties.put(propertyTO.getName(), propertyTO.getValue());
        }
        sle.setProperties(properties);

        layoutElements.add(sle);
      }

//        // ImageLayoutElements
//        List<Element> ileElements = vLayoutElement.getChildren("imageLayoutElement");
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
      for (VisualLayoutTO.ModelLayoutElement mlElement : visualLayoutTO.getModelLayoutElements()) {
        TCSObject<?> vObj = model.getObjectPool().getObject(mlElement.getVisualizedObjectName());
        ModelLayoutElement mle = new ModelLayoutElement(vObj.getReference());

        mle.setLayer(mlElement.getLayer().intValue());

        Map<String, String> properties = new HashMap<>();
        for (PropertyTO propertyTO : mlElement.getProperties()) {
          properties.put(propertyTO.getName(), propertyTO.getValue());
        }
        mle.setProperties(properties);

        layoutElements.add(mle);
      }

      // XXX Load GroupLayoutElements
      model.setVisualLayoutElements(layoutRef, layoutElements);

      // ViewBookmarks
      List<ViewBookmark> viewBookmarks = new LinkedList<>();
      for (VisualLayoutTO.ViewBookmark bookmarkTO : visualLayoutTO.getViewBookmarks()) {
        ViewBookmark bookmark = new ViewBookmark();
        bookmark.setLabel(bookmarkTO.getLabel());
        bookmark.setCenterX(bookmarkTO.getCenterX());
        bookmark.setCenterY(bookmarkTO.getCenterY());
        bookmark.setViewScaleX(bookmarkTO.getViewScaleX().doubleValue());
        bookmark.setViewScaleY(bookmarkTO.getViewScaleY().doubleValue());
        bookmark.setViewRotation(bookmarkTO.getViewRotation());
        viewBookmarks.add(bookmark);
      }
      model.setVisualLayoutViewBookmarks(layoutRef, viewBookmarks);

      setModelProperties(model, visualLayoutTO.getProperties(), layoutRef);
    }
  }

  private static List<PropertyTO> getProperties(@Nonnull TCSObject<?> object) {
    requireNonNull(object, "object");

    List<PropertyTO> properties = new ArrayList<>();
    for (Map.Entry<String, String> entry : object.getProperties().entrySet()) {
      PropertyTO property = new PropertyTO();
      property.setName(entry.getKey());
      property.setValue(entry.getValue());
      properties.add(property);
    }
    return properties;
  }

  private void setModelProperties(@Nonnull Model model,
                                  @Nonnull List<PropertyTO> properties,
                                  @Nonnull TCSObjectReference<?> ref) {
    requireNonNull(model, "model");
    requireNonNull(properties, "properties");
    requireNonNull(ref, "ref");

    for (PropertyTO property : properties) {
      String propName
          = Strings.isNullOrEmpty(property.getName()) ? "Property unknown" : property.getName();
      String propValue
          = Strings.isNullOrEmpty(property.getValue()) ? "Value unknown" : property.getValue();

      model.getObjectPool().setObjectProperty(ref, propName, propValue);
    }
  }
}
