/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.persistence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.to.model.BlockCreationTO;
import org.opentcs.access.to.model.GroupCreationTO;
import org.opentcs.access.to.model.LocationCreationTO;
import org.opentcs.access.to.model.LocationTypeCreationTO;
import org.opentcs.access.to.model.ModelLayoutElementCreationTO;
import org.opentcs.access.to.model.PathCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.PointCreationTO;
import org.opentcs.access.to.model.ShapeLayoutElementCreationTO;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Group;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.visualization.ImageLayoutElement;
import org.opentcs.data.model.visualization.LayoutElement;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.model.visualization.ShapeLayoutElement;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.kernel.workingset.Model;
import org.opentcs.util.persistence.ModelParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of {@link XMLModelReader XMLModelReader}
 * and {@link XMLModelWriter XMLModelWriter}.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class XMLModelBuilder
    implements XMLModelReader,
               XMLModelWriter {

  /**
   * This class's Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(XMLModelBuilder.class);
  /**
   * The model parser.
   */
  private final ModelParser modelParser;

  /**
   * Creates a new instance.
   */
  @Inject
  public XMLModelBuilder(ModelParser modelParser) {
    this.modelParser = requireNonNull(modelParser, "modelParser");
  }

  // Implementation of interface XMLModelWriter starts here.
  @Override
  public void writeXMLModel(@Nonnull Model model,
                            @Nullable String name,
                            @Nonnull File file)
      throws IOException {
    requireNonNull(model, "model");
    requireNonNull(file, "file");

    PlantModelCreationTO plantModel
        = new PlantModelCreationTO(name != null ? name : model.getName())
            .withProperties(model.getProperties())
            .withPoints(getPoints(model))
            .withPaths(getPath(model))
            .withVehicles(getVehicles(model))
            .withLocationTypes(getLocationTypes(model))
            .withLocations(getLocations(model))
            .withBlocks(getBlocks(model))
            .withGroups(getGroups(model))
            .withVisualLayouts(getVisualLayouts(model));

    modelParser.writeModel(plantModel, file);
  }

  // Implementation of interface XMLModelReader starts here.
  @Override
  public String readModelName(@Nonnull File file)
      throws InvalidModelException, IOException {
    requireNonNull(file, "file");

    String modelName = modelParser.readModel(file).getName();

    if (modelName.isEmpty()) {
      modelName = "ModelNameMissing";
      //throw new InvalidModelException("Model name missing");
    }
    return modelName;
  }

  @Override
  public void readXMLModel(@Nonnull File file, @Nonnull Model model)
      throws InvalidModelException, IOException {
    requireNonNull(file, "file");
    requireNonNull(model, "model");

    PlantModelCreationTO plantModel = modelParser.readModel(file);

    try {
      model.clear();
      model.setName(plantModel.getName());
      model.setProperties(plantModel.getProperties());

      // Fill the model with components.
      List<PointCreationTO> pointElements = plantModel.getPoints();
      List<PathCreationTO> pathElements = plantModel.getPaths();
      List<VehicleCreationTO> vehicleElements = plantModel.getVehicles();
      List<LocationTypeCreationTO> locTypeElements = plantModel.getLocationTypes();
      List<LocationCreationTO> locationElements = plantModel.getLocations();
      List<BlockCreationTO> blockElements = plantModel.getBlocks();
      List<GroupCreationTO> groupElements = plantModel.getGroups();
      List<VisualLayoutCreationTO> visuLayoutElements = plantModel.getVisualLayouts();

      readPoints(pointElements, model);
      readPaths(pathElements, model);
      readVehicles(vehicleElements, model);
      readLocationTypes(locTypeElements, model);
      readLocations(locationElements, model);
      readBlocks(blockElements, model);
      readGroups(groupElements, model);
      readVisualLayouts(visuLayoutElements, model);
    }
    catch (ObjectExistsException exc) {
      throw new InvalidModelException("Duplicate objects found in model", exc);
    }
  }

  /**
   * Returns a list of {@link PointCreationTO Points} for all points in a model.
   *
   * @param model The model data.
   * @return A list of {@link PointCreationTO Points} for all points in a model.
   */
  private List<PointCreationTO> getPoints(Model model) {
    Set<Point> points = model.getObjectPool().getObjects(Point.class);
    List<PointCreationTO> result = new ArrayList<>();

    for (Point curPoint : points) {
      result.add(
          new PointCreationTO(curPoint.getName())
              .withPosition(curPoint.getPosition())
              .withVehicleOrientationAngle(curPoint.getVehicleOrientationAngle())
              .withType(curPoint.getType())
              .withProperties(curPoint.getProperties())
      );
    }

    return result;
  }

  /**
   * Returns a list of {@link PathCreationTO Paths} for all paths in a model.
   *
   * @param model The model data.
   * @return A list of {@link PathCreationTO Paths} for all paths in a model.
   */
  @SuppressWarnings("deprecation")
  private List<PathCreationTO> getPath(Model model) {
    Set<Path> paths = model.getObjectPool().getObjects(Path.class);
    List<PathCreationTO> result = new ArrayList<>();

    for (Path curPath : paths) {
      result.add(
          new PathCreationTO(curPath.getName(),
                             curPath.getSourcePoint().getName(),
                             curPath.getDestinationPoint().getName())
              .withLength(curPath.getLength())
              .withRoutingCost(curPath.getRoutingCost())
              .withMaxVelocity(curPath.getMaxVelocity())
              .withMaxReverseVelocity(curPath.getMaxReverseVelocity())
              .withLocked(curPath.isLocked())
              .withProperties(curPath.getProperties())
      );
    }

    return result;
  }

  /**
   * Returns a list of {@link VehicleCreationTO Vehicles} for all vehicles in a model.
   *
   * @param model The model data.
   * @return A list of {@link VehicleCreationTO Vehicles} for all vehicles in a model.
   */
  private List<VehicleCreationTO> getVehicles(Model model) {
    Set<Vehicle> vehicles = model.getObjectPool().getObjects(Vehicle.class);
    List<VehicleCreationTO> result = new ArrayList<>();

    for (Vehicle curVehicle : vehicles) {
      result.add(
          new VehicleCreationTO(curVehicle.getName())
              .withLength(curVehicle.getLength())
              .withEnergyLevelGood(curVehicle.getEnergyLevelGood())
              .withEnergyLevelCritical(curVehicle.getEnergyLevelCritical())
              .withEnergyLevelFullyRecharged(curVehicle.getEnergyLevelFullyRecharged())
              .withEnergyLevelSufficientlyRecharged(curVehicle.getEnergyLevelSufficientlyRecharged())
              .withMaxVelocity(curVehicle.getMaxVelocity())
              .withMaxReverseVelocity(curVehicle.getMaxReverseVelocity())
              .withProperties(curVehicle.getProperties())
      );
    }

    return result;
  }

  /**
   * Returns a list of {@link LocationTypeCreationTO LocationTypes} for all location types in a
   * model.
   *
   * @param model The model data.
   * @return A list of {@link LocationTypeCreationTO LocationTypes} for all location types in a
   * model.
   */
  private List<LocationTypeCreationTO> getLocationTypes(Model model) {
    Set<LocationType> locTypes = model.getObjectPool().getObjects(LocationType.class);
    List<LocationTypeCreationTO> result = new ArrayList<>();

    for (LocationType curType : locTypes) {
      result.add(
          new LocationTypeCreationTO(curType.getName())
              .withAllowedOperations(curType.getAllowedOperations())
              .withProperties(curType.getProperties())
      );
    }

    return result;
  }

  /**
   * Returns a list of {@link LocationCreationTO Locations} for all locations in a model.
   *
   * @param model The model data.
   * @return A list of {@link LocationCreationTO Locations} for all locations in a model.
   */
  private List<LocationCreationTO> getLocations(Model model) {
    Set<Location> locations = model.getObjectPool().getObjects(Location.class);
    List<LocationCreationTO> result = new ArrayList<>();

    for (Location curLoc : locations) {
      result.add(
          new LocationCreationTO(curLoc.getName(),
                                 curLoc.getType().getName(),
                                 curLoc.getPosition())
              .withLinks(curLoc.getAttachedLinks().stream()
                  .collect(Collectors.toMap(link -> link.getPoint().getName(),
                                            Location.Link::getAllowedOperations)))
              .withProperties(curLoc.getProperties())
      );
    }

    return result;
  }

  /**
   * Returns a list of {@link BlockCreationTO Blocks} for all blocks in a model.
   *
   * @param model The model data.
   * @return A list of {@link BlockCreationTO Blocks} for all blocks in a model.
   */
  private List<BlockCreationTO> getBlocks(Model model) {
    Set<Block> blocks = model.getObjectPool().getObjects(Block.class);
    List<BlockCreationTO> result = new ArrayList<>();

    for (Block curBlock : blocks) {
      result.add(
          new BlockCreationTO(curBlock.getName())
              .withMemberNames(curBlock.getMembers().stream()
                  .map(member -> member.getName())
                  .collect(Collectors.toSet()))
              .withType(curBlock.getType())
              .withProperties(curBlock.getProperties())
      );
    }

    return result;
  }

  /**
   * Returns a list of {@link GroupCreationTO Groups} for all groups in a model.
   *
   * @param model The model data.
   * @return A list of {@link GroupCreationTO Groups} for all groups in a model.
   */
  private List<GroupCreationTO> getGroups(Model model) {
    Set<Group> groups = model.getObjectPool().getObjects(Group.class);
    List<GroupCreationTO> result = new ArrayList<>();

    for (Group curGroup : groups) {
      result.add(
          new GroupCreationTO(curGroup.getName())
              .withMemberNames(curGroup.getMembers().stream()
                  .map(member -> member.getName())
                  .collect(Collectors.toSet()))
              .withProperties(curGroup.getProperties())
      );
    }

    return result;
  }

  /**
   * Returns a list of {@link VisualLayoutCreationTO VisualLayouts} for all visual layouts in a
   * model.
   *
   * @param model The model data.
   * @return A list of {@link VisualLayoutCreationTO VisualLayouts} for all visual layouts in a
   * model.
   */
  private List<VisualLayoutCreationTO> getVisualLayouts(Model model) {
    Set<VisualLayout> layouts = model.getObjectPool().getObjects(VisualLayout.class);
    List<VisualLayoutCreationTO> result = new ArrayList<>();

    // Separate our various kinds of layout elements.
    for (VisualLayout curLayout : layouts) {
      List<ShapeLayoutElement> shapeLayoutElements = new LinkedList<>();
      Map<TCSObject<?>, ModelLayoutElement> modelLayoutElements = new HashMap<>();

      for (LayoutElement layoutElement : curLayout.getLayoutElements()) {
        if (layoutElement instanceof ShapeLayoutElement) {
          shapeLayoutElements.add((ShapeLayoutElement) layoutElement);
        }
        else if (layoutElement instanceof ImageLayoutElement) {
          // XXX Do something with these elements?
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

      // Persist ShapeLayoutElements
      List<ShapeLayoutElementCreationTO> slElements = new ArrayList<>();
      for (ShapeLayoutElement curSLE : shapeLayoutElements) {
        ShapeLayoutElementCreationTO slElement = new ShapeLayoutElementCreationTO("")
            .withLayer(curSLE.getLayer())
            .withProperties(curSLE.getProperties());

        slElements.add(slElement);
      }

      // Persist ModelLayoutElements
      List<ModelLayoutElementCreationTO> mlElements = new ArrayList<>();
      for (Map.Entry<TCSObject<?>, ModelLayoutElement> curMLE : modelLayoutElements.entrySet()) {
        ModelLayoutElementCreationTO mlElement = new ModelLayoutElementCreationTO(curMLE.getKey().getName())
            .withLayer(curMLE.getValue().getLayer())
            .withProperties(curMLE.getValue().getProperties());

        mlElements.add(mlElement);
      }

      result.add(
          new VisualLayoutCreationTO(curLayout.getName())
              .withScaleX(curLayout.getScaleX())
              .withScaleY(curLayout.getScaleY())
              .withModelElements(mlElements)
              .withShapeElements(slElements)
              .withProperties(curLayout.getProperties())
      );
    }
    return result;
  }

  /**
   * Reads the given list of {@link PointCreationTO Points} into the model.
   *
   * @param pointTOs The point elements.
   * @param model The model.
   * @throws ObjectExistsException In case of duplicate objects.
   */
  private void readPoints(List<PointCreationTO> pointTOs, Model model)
      throws ObjectExistsException {
    for (PointCreationTO pointTO : pointTOs) {
      model.createPoint(pointTO);
    }
  }

  /**
   * Reads the given list of {@link VehicleCreationTO Vehicles} into the model.
   *
   * @param vehicleTOs The vehicle elements.
   * @param model The model.
   * @throws ObjectExistsException In case of duplicate objects.
   */
  private void readVehicles(List<VehicleCreationTO> vehicleTOs, Model model)
      throws ObjectExistsException {
    for (VehicleCreationTO vehicleTO : vehicleTOs) {
      model.createVehicle(vehicleTO);
    }
  }

  /**
   * Reads the given list of {@link PathCreationTO Paths} into the model.
   *
   * @param pathTOs The path elements.
   * @param model The model.
   * @throws ObjectExistsException In case of duplicate objects.
   */
  private void readPaths(List<PathCreationTO> pathTOs, Model model)
      throws ObjectExistsException {
    for (PathCreationTO pathTO : pathTOs) {
      model.createPath(pathTO);
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
   * Reads the given list of {@link LocationTypeCreationTO LocationTypes} into the model.
   *
   * @param locationTypeTOs The location type elements.
   * @param model The model.
   * @throws ObjectExistsException In case of duplicate objects.
   */
  private void readLocationTypes(List<LocationTypeCreationTO> locationTypeTOs, Model model)
      throws ObjectExistsException {
    for (LocationTypeCreationTO locationTypeTO : locationTypeTOs) {
      model.createLocationType(locationTypeTO);
    }
  }

  /**
   * Reads the given list of {@link LocationCreationTO Locations} into the model.
   *
   * @param locationTOs The location elements.
   * @param model The model.
   * @throws ObjectExistsException In case of duplicate objects.
   */
  private void readLocations(List<LocationCreationTO> locationTOs, Model model)
      throws ObjectExistsException {
    for (LocationCreationTO locationTO : locationTOs) {
      model.createLocation(locationTO);
    }

  }

  /**
   * Reads the given list of {@link BlockCreationTO Blocks} into the model.
   *
   * @param blockTOs The block elements.
   * @param model The model.
   * @throws ObjectExistsException In case of duplicate objects.
   */
  private void readBlocks(List<BlockCreationTO> blockTOs, Model model)
      throws ObjectExistsException {
    for (BlockCreationTO blockTO : blockTOs) {
      model.createBlock(blockTO);
    }
  }

  /**
   * Reads the given list of {@link GroupCreationTO Groups} into the model.
   *
   * @param groupTOs The group elements.
   * @param model The model.
   * @throws ObjectExistsException In case of duplicate objects.
   */
  private void readGroups(List<GroupCreationTO> groupTOs, Model model)
      throws ObjectExistsException {
    for (GroupCreationTO groupTO : groupTOs) {
      model.createGroup(groupTO);
    }
  }

  /**
   * Reads the given list of {@link VisualLayoutCreationTO VisualLayouts} into the model.
   *
   * @param visuLayoutTOs The visual layout elements.
   * @param model The model.
   * @throws ObjectExistsException In case of duplicate objects.
   */
  private void readVisualLayouts(List<VisualLayoutCreationTO> visuLayoutTOs, Model model)
      throws ObjectExistsException {
    for (VisualLayoutCreationTO visualLayoutTO : visuLayoutTOs) {
      model.createVisualLayout(visualLayoutTO);
    }
  }
}
