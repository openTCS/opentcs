/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.opentcs.access.to.model.BlockCreationTO;
import org.opentcs.access.to.model.LocationCreationTO;
import org.opentcs.access.to.model.LocationTypeCreationTO;
import org.opentcs.access.to.model.PathCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.PointCreationTO;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.access.to.peripherals.PeripheralOperationCreationTO;
import org.opentcs.components.kernel.services.PlantModelService;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Envelope;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.PlantModel;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Pose;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.visualization.Layer;
import org.opentcs.data.model.visualization.LayerGroup;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.data.peripherals.PeripheralOperation;
import org.opentcs.kernel.extensions.servicewebapi.KernelExecutorWrapper;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.PlantModelTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.BlockTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LayerGroupTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LayerTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.LinkTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LocationTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.LocationTypeTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.PathTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.PeripheralOperationTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.PointTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PropertyTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.VehicleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.plantmodel.VisualLayoutTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.CoupleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.TripleTO;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.EnvelopeTO;
import org.opentcs.util.Colors;

/**
 */
public class PlantModelHandler {

  /**
   * Used to set or retrieve plant models.
   */
  private final PlantModelService plantModelService;
  /**
   * Executes calls via the kernel executor and waits for the outcome.
   */
  private final KernelExecutorWrapper executorWrapper;

  /**
   * Creates a new instance.
   *
   * @param plantModelService Used to set or retrieve plant models.
   * @param executorWrapper Executes calls via the kernel executor and waits for the outcome.
   */
  @Inject
  public PlantModelHandler(PlantModelService plantModelService,
                           KernelExecutorWrapper executorWrapper) {
    this.plantModelService = requireNonNull(plantModelService, "plantModelService");
    this.executorWrapper = requireNonNull(executorWrapper, "executorWrapper");
  }

  public void putPlantModel(PlantModelTO putPlantModel)
      throws ObjectUnknownException,
             IllegalArgumentException {
    requireNonNull(putPlantModel, "putPlantModel");

    PlantModelCreationTO plantModelCreationTO = new PlantModelCreationTO(putPlantModel.getName())
        .withPoints(toPointCreationTOs(putPlantModel.getPoints()))
        .withPaths(toPathCreationTOs(putPlantModel.getPaths()))
        .withLocationTypes(toLocationTypeCreationTOs(putPlantModel.getLocationTypes()))
        .withLocations(toLocationCreationTOs(putPlantModel.getLocations()))
        .withBlocks(toBlockCreationTOs(putPlantModel.getBlocks()))
        .withVehicles(toVehicleCreationTOs(putPlantModel.getVehicles()))
        .withVisualLayout(toVisualLayoutCreationTO(putPlantModel.getVisualLayout()))
        .withProperties(toPropertyMap(putPlantModel.getProperties()));

    executorWrapper.callAndWait(() -> plantModelService.createPlantModel(plantModelCreationTO));
  }

  public PlantModelTO getPlantModel() {
    PlantModel plantModel = plantModelService.getPlantModel();
    return new PlantModelTO(plantModel.getName())
        .setPoints(toPointTOs(plantModel.getPoints()))
        .setPaths(toPathTOs(plantModel.getPaths()))
        .setLocationTypes(toLocationTypeTOs(plantModel.getLocationTypes()))
        .setLocations(toLocationTOs(plantModel.getLocations()))
        .setBlocks(toBlockTOs(plantModel.getBlocks()))
        .setVehicles(toVehicleTOs(plantModel.getVehicles()))
        .setVisualLayout(toVisualLayoutTO(plantModel.getVisualLayouts()))
        .setProperties(toPropertyTOs(plantModel.getProperties()));
  }

  private VisualLayoutCreationTO toVisualLayoutCreationTO(VisualLayoutTO vLayout) {
    return new VisualLayoutCreationTO(vLayout.getName())
        .withProperties(toPropertyMap(vLayout.getProperties()))
        .withScaleX(vLayout.getScaleX())
        .withScaleY(vLayout.getScaleY())
        .withLayers(convertLayers(vLayout.getLayers()))
        .withLayerGroups(convertLayerGroups(vLayout.getLayerGroups()));
  }

  private VisualLayoutTO toVisualLayoutTO(Set<VisualLayout> visualLayouts) {
    return visualLayouts.stream()
        .findFirst()
        .map(
            visualLayout -> new VisualLayoutTO(visualLayout.getName())
                .setScaleX(visualLayout.getScaleX())
                .setScaleY(visualLayout.getScaleY())
                .setLayers(toLayerTOs(visualLayout.getLayers()))
                .setLayerGroups(toLayerGroupTOs(visualLayout.getLayerGroups())))
        .orElse(new VisualLayoutTO("default visual layout"));
  }

  private List<VehicleCreationTO> toVehicleCreationTOs(List<VehicleTO> vehicles) {
    return vehicles.stream()
        .map(
            vehicle -> new VehicleCreationTO(vehicle.getName())
                .withProperties(toPropertyMap(vehicle.getProperties()))
                .withLength(vehicle.getLength())
                .withEnergyLevelCritical(vehicle.getEnergyLevelCritical())
                .withEnergyLevelGood(vehicle.getEnergyLevelGood())
                .withEnergyLevelFullyRecharged(vehicle.getEnergyLevelFullyRecharged())
                .withEnergyLevelSufficientlyRecharged(vehicle.getEnergyLevelSufficientlyRecharged())
                .withMaxVelocity(vehicle.getMaxVelocity())
                .withMaxReverseVelocity(vehicle.getMaxReverseVelocity())
                .withLayout(new VehicleCreationTO.Layout(
                    Colors.decodeFromHexRGB(vehicle.getLayout().getRouteColor()))))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private List<VehicleTO> toVehicleTOs(Set<Vehicle> vehicles) {
    return vehicles.stream()
        .map(vehicle -> new VehicleTO(vehicle.getName())
        .setLength(vehicle.getLength())
        .setEnergyLevelCritical(vehicle.getEnergyLevelCritical())
        .setEnergyLevelGood(vehicle.getEnergyLevelGood())
        .setEnergyLevelFullyRecharged(vehicle.getEnergyLevelFullyRecharged())
        .setEnergyLevelSufficientlyRecharged(vehicle.getEnergyLevelSufficientlyRecharged())
        .setMaxVelocity(vehicle.getMaxVelocity())
        .setMaxReverseVelocity(vehicle.getMaxReverseVelocity())
        .setLayout(new VehicleTO.Layout()
            .setRouteColor(Colors.encodeToHexRGB(vehicle.getLayout().getRouteColor())))
        .setProperties(toPropertyTOs(vehicle.getProperties())))
        .collect(Collectors.toList());
  }

  private List<BlockCreationTO> toBlockCreationTOs(List<BlockTO> blocks) {
    return blocks.stream()
        .map(
            block -> new BlockCreationTO(block.getName())
                .withProperties(toPropertyMap(block.getProperties()))
                .withMemberNames(block.getMemberNames())
                .withType(Block.Type.valueOf(block.getType()))
                .withLayout(new BlockCreationTO.Layout(
                    Colors.decodeFromHexRGB(block.getLayout().getColor()))))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private List<BlockTO> toBlockTOs(Set<Block> blocks) {
    return blocks.stream()
        .map(block -> new BlockTO(block.getName())
        .setType(block.getType().name())
        .setMemberNames(convertMemberNames(block.getMembers()))
        .setLayout(new BlockTO.Layout()
            .setColor(Colors.encodeToHexRGB(block.getLayout().getColor())))
        .setProperties(toPropertyTOs(block.getProperties())))
        .collect(Collectors.toList());

  }

  private List<LocationTypeCreationTO> toLocationTypeCreationTOs(List<LocationTypeTO> locTypes) {
    return locTypes.stream()
        .map(
            locationType -> new LocationTypeCreationTO(locationType.getName())
                .withAllowedOperations(locationType.getAllowedOperations())
                .withAllowedPeripheralOperations(locationType.getAllowedPeripheralOperations())
                .withProperties(toPropertyMap(locationType.getProperties()))
                .withLayout(new LocationTypeCreationTO.Layout(LocationRepresentation.valueOf(
                    locationType.getLayout().getLocationRepresentation()))))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private List<LocationTypeTO> toLocationTypeTOs(Set<LocationType> locationTypes) {
    return locationTypes.stream()
        .map(locationType -> new LocationTypeTO(locationType.getName())
        .setProperties(toPropertyTOs(locationType.getProperties()))
        .setAllowedOperations(locationType.getAllowedOperations())
        .setAllowedPeripheralOperations(locationType.getAllowedPeripheralOperations())
        .setLayout(new LocationTypeTO.Layout()
            .setLocationRepresentation(
                locationType.getLayout().getLocationRepresentation().name())))
        .collect(Collectors.toList());
  }

  private List<LocationCreationTO> toLocationCreationTOs(List<LocationTO> locations) {
    return locations.stream()
        .map(location -> new LocationCreationTO(location.getName(),
                                                location.getTypeName(),
                                                new Triple(location.getPosition().getX(),
                                                           location.getPosition().getY(),
                                                           location.getPosition().getZ()))
        .withProperties(toPropertyMap(location.getProperties()))
        .withLinks(toLinkMap(location.getLinks()))
        .withLocked(location.isLocked())
        .withLayout(new LocationCreationTO.Layout(
            new Couple(location.getLayout().getPosition().getX(),
                       location.getLayout().getPosition().getY()),
            new Couple(location.getLayout().getLabelOffset().getX(),
                       location.getLayout().getLabelOffset().getY()),
            LocationRepresentation.valueOf(
                location.getLayout().getLocationRepresentation()),
            location.getLayout().getLayerId())))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private List<LocationTO> toLocationTOs(Set<Location> locations) {
    return locations.stream()
        .map(location -> new LocationTO(location.getName(),
                                        location.getType().getName(),
                                        new TripleTO(location.getPosition().getX(),
                                                     location.getPosition().getY(),
                                                     location.getPosition().getZ()))
        .setLocked(location.isLocked())
        .setProperties(toPropertyTOs(location.getProperties()))
        .setLinks(toLinkTOs(location.getAttachedLinks()))
        .setLayout(new LocationTO.Layout()
            .setLayerId(location.getLayout().getLayerId())
            .setLocationRepresentation(
                location.getLayout().getLocationRepresentation().name())
            .setLabelOffset(new CoupleTO(location.getLayout().getLabelOffset().getX(),
                                         location.getLayout().getLabelOffset().getY()))
            .setPosition(new CoupleTO(location.getLayout().getPosition().getX(),
                                      location.getLayout().getPosition().getY()))))
        .collect(Collectors.toList());

  }

  private List<PointTO> toPointTOs(Set<Point> points) {
    return points.stream()
        .map(point -> new PointTO(point.getName())
        .setPosition(new TripleTO(point.getPose().getPosition().getX(),
                                  point.getPose().getPosition().getY(),
                                  point.getPose().getPosition().getZ()))
        .setType(point.getType().name())
        .setVehicleOrientationAngle(point.getPose().getOrientationAngle())
        .setVehicleEnvelopes(toEnvelopeTOs(point.getVehicleEnvelopes()))
        .setProperties(toPropertyTOs(point.getProperties()))
        .setLayout(new PointTO.Layout()
            .setLabelOffset(new CoupleTO(point.getLayout().getLabelOffset().getX(),
                                         point.getLayout().getLabelOffset().getY()))
            .setPosition(new CoupleTO(point.getLayout().getPosition().getX(),
                                      point.getLayout().getPosition().getY()))
            .setLayerId(point.getLayout().getLayerId()))
        )
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private List<PointCreationTO> toPointCreationTOs(List<PointTO> points) {
    return points.stream()
        .map(
            point -> new PointCreationTO(point.getName())
                .withProperties(toPropertyMap(point.getProperties()))
                .withPose(
                    new Pose(
                        new Triple(point.getPosition().getX(),
                                   point.getPosition().getY(),
                                   point.getPosition().getZ()),
                        point.getVehicleOrientationAngle()
                    )
                )
                .withType(Point.Type.valueOf(point.getType()))
                .withLayout(new PointCreationTO.Layout(
                    new Couple(point.getLayout().getPosition().getX(),
                               point.getLayout().getPosition().getY()),
                    new Couple(point.getLayout().getLabelOffset().getX(),
                               point.getLayout().getLabelOffset().getY()),
                    point.getLayout().getLayerId()))
                .withVehicleEnvelopes(toVehicleEnvelopeMap(point.getVehicleEnvelopes()))
        )
        .collect(Collectors.toCollection(ArrayList::new));

  }

  private List<PathTO> toPathTOs(Set<Path> paths) {
    return paths.stream()
        .map(path -> new PathTO(path.getName(),
                                path.getSourcePoint().getName(),
                                path.getDestinationPoint().getName())
        .setLength(path.getLength())
        .setLocked(path.isLocked())
        .setMaxReverseVelocity(path.getMaxReverseVelocity())
        .setMaxVelocity(path.getMaxVelocity())
        .setProperties(toPropertyTOs(path.getProperties()))
        .setPeripheralOperations(
            toPeripheralOperationsTOs(path.getPeripheralOperations()))
        .setLayout(new PathTO.Layout()
            .setLayerId(path.getLayout().getLayerId())
            .setConnectionType(path.getLayout().getConnectionType().name())
            .setControlPoints(toCoupleTOs(path.getLayout().getControlPoints()))))
        .collect(Collectors.toList());
  }

  private List<PathCreationTO> toPathCreationTOs(List<PathTO> paths) {
    return paths.stream()
        .map(
            path -> new PathCreationTO(path.getName(),
                                       path.getSrcPointName(),
                                       path.getDestPointName())
                .withName(path.getName())
                .withProperties(toPropertyMap(path.getProperties()))
                .withLength(path.getLength())
                .withMaxVelocity(path.getMaxVelocity())
                .withMaxReverseVelocity(path.getMaxReverseVelocity())
                .withLocked(path.isLocked())
                .withLayout(toPathCreationTOLayout(path.getLayout()))
                .withVehicleEnvelopes(toVehicleEnvelopeMap(path.getVehicleEnvelopes()))
                .withPeripheralOperations(
                    toPeripheralOperationCreationTOs(path.getPeripheralOperations())))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private List<PeripheralOperationCreationTO> toPeripheralOperationCreationTOs(
      List<PeripheralOperationTO> perOps) {
    return perOps.stream()
        .map(
            perOp -> new PeripheralOperationCreationTO(perOp.getOperation(),
                                                       perOp.getLocationName())
                .withCompletionRequired(perOp.isCompletionRequired())
                .withExecutionTrigger(PeripheralOperation.ExecutionTrigger.valueOf(
                    perOp.getExecutionTrigger())))
        .collect(Collectors.toList());
  }

  private PathCreationTO.Layout toPathCreationTOLayout(PathTO.Layout layout) {
    return new PathCreationTO.Layout(
        Path.Layout.ConnectionType.valueOf(layout.getConnectionType()),
        layout.getControlPoints()
            .stream()
            .map(cp -> new Couple(cp.getX(), cp.getY()))
            .collect(Collectors.toList()),
        layout.getLayerId()
    );
  }

  private List<LayerGroup> convertLayerGroups(List<LayerGroupTO> layerGroups) {
    return layerGroups.stream()
        .map(layerGroup -> new LayerGroup(layerGroup.getId(),
                                          layerGroup.getName(),
                                          layerGroup.isVisible()))
        .collect(Collectors.toList());
  }

  private List<LayerGroupTO> toLayerGroupTOs(List<LayerGroup> layerGroups) {
    return layerGroups.stream()
        .map(layerGroup -> new LayerGroupTO(layerGroup.getId(),
                                            layerGroup.getName(),
                                            layerGroup.isVisible()))
        .collect(Collectors.toList());

  }

  private List<Layer> convertLayers(List<LayerTO> layers) {
    return layers.stream()
        .map(layer -> new Layer(layer.getId(),
                                layer.getOrdinal(),
                                layer.isVisible(),
                                layer.getName(),
                                layer.getGroupId()))
        .collect(Collectors.toList());
  }

  private List<LayerTO> toLayerTOs(List<Layer> layers) {
    return layers.stream()
        .map(layer -> new LayerTO(layer.getId(),
                                  layer.getOrdinal(),
                                  layer.isVisible(),
                                  layer.getName(),
                                  layer.getGroupId()))
        .collect(Collectors.toList());
  }

  private Map<String, Set<String>> toLinkMap(List<LinkTO> links) {
    return links.stream()
        .collect(Collectors.toMap(LinkTO::getPointName, LinkTO::getAllowedOperations));
  }

  private List<LinkTO> toLinkTOs(Set<Location.Link> links) {
    return links.stream()
        .map(link -> new LinkTO()
        .setPointName(link.getPoint().getName())
        .setAllowedOperations(link.getAllowedOperations()))
        .collect(Collectors.toList());

  }

  private Map<String, String> toPropertyMap(List<PropertyTO> properties) {
    return properties.stream()
        .collect(Collectors.toMap(PropertyTO::getName, PropertyTO::getValue));
  }

  private Map<String, Envelope> toVehicleEnvelopeMap(List<EnvelopeTO> envelopeEntries) {
    return envelopeEntries.stream()
        .collect(Collectors.toMap(
            EnvelopeTO::getKey,
            entry -> {
              List<Couple> couples = entry.getVertices().stream()
                  .map(coupleTO -> new Couple(coupleTO.getX(), coupleTO.getY()))
                  .collect(Collectors.toList());
              return new Envelope(couples);
            }
        ));
  }

  private List<EnvelopeTO> toEnvelopeTOs(Map<String, Envelope> envelopeMap) {
    return envelopeMap.entrySet().stream()
        .map(entry -> new EnvelopeTO(
        entry.getKey(),
        entry.getValue().getVertices().stream()
            .map(couple -> new CoupleTO(couple.getX(), couple.getY()))
            .collect(Collectors.toList()))
        )
        .collect(Collectors.toList());
  }

  private List<PropertyTO> toPropertyTOs(Map<String, String> properties) {
    return properties.entrySet().stream()
        .map(property -> new PropertyTO(property.getKey(), property.getValue()))
        .collect(Collectors.toList());
  }

  private List<PeripheralOperationTO> toPeripheralOperationsTOs(
      List<PeripheralOperation> peripheralOperations) {
    return peripheralOperations.stream()
        .map(perOp -> new PeripheralOperationTO(perOp.getOperation(),
                                                perOp.getLocation().getName())
        .setCompletionRequired(perOp.isCompletionRequired())
        .setExecutionTrigger(
            perOp.getExecutionTrigger().name())
        )
        .collect(Collectors.toList());
  }

  private List<CoupleTO> toCoupleTOs(List<Couple> controlPoints) {
    return controlPoints.stream()
        .map(cp -> new CoupleTO(cp.getX(), cp.getY()))
        .collect(Collectors.toList());
  }

  private Set<String> convertMemberNames(Set<TCSResourceReference<?>> members) {
    return members.stream()
        .map(TCSResourceReference::getName)
        .collect(Collectors.toSet());
  }
}
