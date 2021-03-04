/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.persistence;

import com.google.common.base.Strings;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.access.to.model.BlockCreationTO;
import org.opentcs.access.to.model.GroupCreationTO;
import org.opentcs.access.to.model.LocationCreationTO;
import org.opentcs.access.to.model.LocationTypeCreationTO;
import org.opentcs.access.to.model.ModelLayoutElementCreationTO;
import org.opentcs.access.to.model.PathCreationTO;
import org.opentcs.access.to.model.PointCreationTO;
import org.opentcs.access.to.model.ShapeLayoutElementCreationTO;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Group;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.visualization.ImageLayoutElement;
import org.opentcs.data.model.visualization.LayoutElement;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.model.visualization.ShapeLayoutElement;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.kernel.workingset.Model;
import org.opentcs.util.Comparators;
import org.opentcs.util.persistence.binding.AllowedOperationTO;
import org.opentcs.util.persistence.binding.BlockTO;
import org.opentcs.util.persistence.binding.GroupTO;
import org.opentcs.util.persistence.binding.LocationTO;
import org.opentcs.util.persistence.binding.LocationTypeTO;
import org.opentcs.util.persistence.binding.MemberTO;
import org.opentcs.util.persistence.binding.PathTO;
import org.opentcs.util.persistence.binding.PlantModelTO;
import org.opentcs.util.persistence.binding.PointTO;
import org.opentcs.util.persistence.binding.PropertyTO;
import org.opentcs.util.persistence.binding.StaticRouteTO;
import org.opentcs.util.persistence.binding.StaticRouteTO.Hop;
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
   * Creates a new instance.
   */
  public XMLModel002Builder() {
  }

  // Implementation of interface XMLModelWriter starts here.
  @Override
  public String getVersionString() {
    return VERSION_STRING;
  }

  @Override
  public void writeXMLModel(@Nonnull Model model,
                            @Nullable String name,
                            @Nonnull OutputStream outStream)
      throws IOException {
    requireNonNull(model, "model");
    requireNonNull(outStream, "outStream");

    PlantModelTO plantModel = new PlantModelTO();
    plantModel.setName(name != null ? name : model.getName());
    plantModel.setProperties(convertProperties(model.getProperties()));
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

    Writer writer = new BufferedWriter(new OutputStreamWriter(outStream, Charset.forName("UTF-8")));
    plantModel.toXml(writer);
    writer.flush();
  }

  // Implementation of interface XMLModelReader starts here.
  @Override
  public String readModelName(@Nonnull InputStream inStream)
      throws InvalidModelException, IOException {
    requireNonNull(inStream, "inStream");

    try (Reader reader = new BufferedReader(new InputStreamReader(inStream,
                                                                  Charset.forName("UTF-8")))) {
      String modelName = PlantModelTO.fromXml(reader).getName();

      if (modelName.isEmpty()) {
        modelName = "ModelNameMissing";
        //throw new InvalidModelException("Model name missing");
      }
      return modelName;
    }
  }

  @Override
  public void readXMLModel(@Nonnull InputStream inStream, @Nonnull Model model)
      throws InvalidModelException, IOException {
    requireNonNull(inStream, "inStream");
    requireNonNull(model, "model");

    try (Reader reader = new BufferedReader(new InputStreamReader(inStream,
                                                                  Charset.forName("UTF-8")))) {
      PlantModelTO plantModel = PlantModelTO.fromXml(reader);

      String modelVersion = plantModel.getVersion();
      if (!VERSION_STRING.equals(modelVersion)) {
        throw new InvalidModelException("Bad model version: " + modelVersion);
      }

      model.clear();
      model.setName(plantModel.getName());
      model.setProperties(convertProperties(plantModel.getProperties()));

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
    points.addAll(model.getObjectPool().getObjects(Point.class));
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
          .setProperties(convertProperties(curPoint.getProperties()));

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
    paths.addAll(model.getObjectPool().getObjects(Path.class));
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
          .setProperties(convertProperties(curPath.getProperties()));
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
    vehicles.addAll(model.getObjectPool().getObjects(Vehicle.class));
    List<VehicleTO> result = new ArrayList<>();
    for (Vehicle curVehicle : vehicles) {
      VehicleTO vehicle = new VehicleTO();
      vehicle.setName(curVehicle.getName());
      vehicle.setLength((long) curVehicle.getLength())
          .setEnergyLevelCritical((long) curVehicle.getEnergyLevelCritical())
          .setEnergyLevelGood((long) curVehicle.getEnergyLevelGood())
          .setMaxVelocity(curVehicle.getMaxVelocity())
          .setMaxReverseVelocity(curVehicle.getMaxReverseVelocity())
          .setProperties(convertProperties(curVehicle.getProperties()));
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
    locTypes.addAll(model.getObjectPool().getObjects(LocationType.class));
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
          .setProperties(convertProperties(curType.getProperties()));

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
    locations.addAll(model.getObjectPool().getObjects(Location.class));
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
          .setProperties(convertProperties(curLoc.getProperties()));

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
    blocks.addAll(model.getObjectPool().getObjects(Block.class));
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
          .setProperties(convertProperties(curBlock.getProperties()));

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
  @SuppressWarnings("deprecation")
  private static List<StaticRouteTO> getStaticRoutes(Model model) {
    Set<org.opentcs.data.model.StaticRoute> routes = new TreeSet<>(Comparators.objectsByName());
    routes.addAll(model.getStaticRoutes(null));
    List<StaticRouteTO> result = new ArrayList<>();
    for (org.opentcs.data.model.StaticRoute curRoute : routes) {
      StaticRouteTO staticRoute = new StaticRouteTO();
      staticRoute.setName(curRoute.getName());

      List<StaticRouteTO.Hop> hops = new ArrayList<>();
      for (TCSObjectReference<Point> curRef : curRoute.getHops()) {
        hops.add(new StaticRouteTO.Hop().setName(curRef.getName()));
      }
      staticRoute.setHops(hops)
          .setProperties(convertProperties(curRoute.getProperties()));

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
    groups.addAll(model.getObjectPool().getObjects(Group.class));
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
          .setProperties(convertProperties(curGroup.getProperties()));

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
  @SuppressWarnings("deprecation")
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
          TCSObject<?> vObj = model.getObjectPool().getObjectOrNull(mle.getVisualizedObject());
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
      for (org.opentcs.data.model.visualization.ViewBookmark curBookmark
               : curLayout.getViewBookmarks()) {
        viewBookmarks.add(new VisualLayoutTO.ViewBookmark()
            .setLabel(curBookmark.getLabel())
            .setCenterX(curBookmark.getCenterX())
            .setCenterY(curBookmark.getCenterY())
            .setViewScaleX((float) curBookmark.getViewScaleX())
            .setViewScaleY((float) curBookmark.getViewScaleY())
            .setViewRotation(curBookmark.getViewRotation()));
      }
      layout.setViewBookmarks(viewBookmarks)
          .setProperties(convertProperties(curLayout.getProperties()));

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
      model.createPoint(
          new PointCreationTO(pointTO.getName())
              .withPosition(new Triple(pointTO.getxPosition(),
                                       pointTO.getyPosition(),
                                       pointTO.getzPosition()))
              .withVehicleOrientationAngle(pointTO.getVehicleOrientationAngle().doubleValue())
              .withType(Point.Type.valueOf(pointTO.getType()))
              .withProperties(convertProperties(pointTO.getProperties())));
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
      model.createVehicle(
          new VehicleCreationTO(vehicleTO.getName())
              .withLength(vehicleTO.getLength().intValue())
              .withEnergyLevelCritical(vehicleTO.getEnergyLevelCritical().intValue())
              .withEnergyLevelGood(vehicleTO.getEnergyLevelGood().intValue())
              .withMaxReverseVelocity(vehicleTO.getMaxReverseVelocity())
              .withMaxVelocity(vehicleTO.getMaxVelocity())
              .withProperties(convertProperties(vehicleTO.getProperties()))
      );
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
      model.createPath(
          new PathCreationTO(pathTO.getName(),
                             pathTO.getSourcePoint(),
                             pathTO.getDestinationPoint())
              .withLength(pathTO.getLength())
              .withRoutingCost(pathTO.getRoutingCost())
              .withLocked(pathTO.isLocked())
              .withMaxVelocity(pathTO.getMaxVelocity().intValue())
              .withMaxReverseVelocity(pathTO.getMaxReverseVelocity().intValue())
              .withProperties(convertProperties(pathTO.getProperties())));
    }
//
//    // Loop through all paths. Add the path to its source point as an outgoing path and to its 
//    // destination point as an incoming path.
//    for (Path curPath : model.getPaths(null)) {
//      model.addPointOutgoingPath(curPath.getSourcePoint(), curPath.getReference());
//      model.addPointIncomingPath(curPath.getDestinationPoint(), curPath.getReference());
//    }
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
      model.createLocationType(
          new LocationTypeCreationTO(locationTypeTO.getName())
              .withAllowedOperations(getAllowedOperations(locationTypeTO.getAllowedOperations()))
              .withProperties(convertProperties(locationTypeTO.getProperties())));
    }
  }

  private List<String> getAllowedOperations(List<AllowedOperationTO> ops) {
    List<String> result = new LinkedList<>();
    for (AllowedOperationTO operation : ops) {
      result.add(operation.getName());
    }
    return result;
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
      model.createLocation(
          new LocationCreationTO(locationTO.getName(),
                                 locationTO.getType(),
                                 new Triple(locationTO.getxPosition(),
                                            locationTO.getyPosition(),
                                            locationTO.getzPosition()))
              .withLinks(getLinks(locationTO))
              .withProperties(convertProperties(locationTO.getProperties()))
      );
    }

  }

  private Map<String, Set<String>> getLinks(LocationTO to) {
    Map<String, Set<String>> result = new HashMap<>();
    for (LocationTO.Link linkTO : to.getLinks()) {
      result.put(linkTO.getPoint(),
                 new HashSet<>(getAllowedOperations(linkTO.getAllowedOperations())));
    }

    return result;
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
      model.createBlock(
          new BlockCreationTO(blockTO.getName())
              .withMemberNames(getMemberNames(blockTO.getMembers()))
              .withProperties(convertProperties(blockTO.getProperties())));
    }
  }

  private Set<String> getMemberNames(Collection<MemberTO> members) {
    Set<String> result = new HashSet<>();
    for (MemberTO member : members) {
      result.add(member.getName());
    }
    return result;
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
      model.createGroup(
          new GroupCreationTO(groupTO.getName())
              .withMemberNames(getMemberNames(groupTO.getMembers()))
              .withProperties(convertProperties(groupTO.getProperties())));
    }
  }

  /**
   * Reads the given list of {@link StaticRouteTO StaticRoutes} into the model.
   *
   * @param staticRouteTOs The static route elements.
   * @param model The model.
   * @throws ObjectExistsException In case of duplicate objects.
   */
  @SuppressWarnings("deprecation")
  private void readStaticRoutes(List<StaticRouteTO> staticRouteTOs, Model model)
      throws ObjectExistsException {
    for (StaticRouteTO staticRouteTO : staticRouteTOs) {
      model.createStaticRoute(
          new org.opentcs.access.to.model.StaticRouteCreationTO(staticRouteTO.getName())
              .setHopNames(getHopNames(staticRouteTO.getHops()))
              .setProperties(convertProperties(staticRouteTO.getProperties())));
    }
  }

  private List<String> getHopNames(Collection<Hop> hops) {
    List<String> result = new LinkedList<>();
    for (Hop hop : hops) {
      result.add(hop.getName());
    }
    return result;
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
      model.createVisualLayout(
          new VisualLayoutCreationTO(visualLayoutTO.getName())
              .withScaleX(visualLayoutTO.getScaleX())
              .withScaleY(visualLayoutTO.getScaleY())
              .withShapeElements(getShapeElements(visualLayoutTO.getShapeLayoutElements()))
              .withModelElements(getModelElements(visualLayoutTO.getModelLayoutElements()))
              .withProperties(convertProperties(visualLayoutTO.getProperties()))
      );
    }
  }

  private List<ModelLayoutElementCreationTO> getModelElements(
      List<VisualLayoutTO.ModelLayoutElement> elements) {
    List<ModelLayoutElementCreationTO> result = new LinkedList<>();
    for (VisualLayoutTO.ModelLayoutElement mlElement : elements) {
      result.add(new ModelLayoutElementCreationTO(mlElement.getVisualizedObjectName())
          .withLayer(mlElement.getLayer().intValue())
          .withProperties(convertProperties(mlElement.getProperties()))
      );
    }
    return result;
  }

  private List<ShapeLayoutElementCreationTO> getShapeElements(
      List<VisualLayoutTO.ShapeLayoutElement> shapes) {
    List<ShapeLayoutElementCreationTO> result = new LinkedList<>();
    for (VisualLayoutTO.ShapeLayoutElement slElement : shapes) {
      result.add(new ShapeLayoutElementCreationTO("")
          .withLayer(slElement.getLayer().intValue())
          .withProperties(convertProperties(slElement.getProperties())));
    }
    return result;
  }

  private static List<PropertyTO> convertProperties(@Nonnull Map<String, String> properties) {
    requireNonNull(properties, "properties");

    return properties.entrySet().stream()
        .sorted((prop1, prop2) -> prop1.getKey().compareTo(prop2.getKey()))
        .map(prop -> new PropertyTO().setName(prop.getKey()).setValue(prop.getValue()))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private Map<String, String> convertProperties(List<PropertyTO> propsList) {
    Map<String, String> result = new HashMap<>();
    for (PropertyTO property : propsList) {
      String propName
          = Strings.isNullOrEmpty(property.getName()) ? "Property unknown" : property.getName();
      String propValue
          = Strings.isNullOrEmpty(property.getValue()) ? "Value unknown" : property.getValue();

      result.put(propName, propValue);
    }
    return result;
  }
}
