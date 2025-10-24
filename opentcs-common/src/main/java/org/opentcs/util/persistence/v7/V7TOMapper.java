// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.persistence.v7;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentcs.access.to.model.BlockCreationTO;
import org.opentcs.access.to.model.BoundingBoxCreationTO;
import org.opentcs.access.to.model.CoupleCreationTO;
import org.opentcs.access.to.model.EnvelopeCreationTO;
import org.opentcs.access.to.model.LayerCreationTO;
import org.opentcs.access.to.model.LayerGroupCreationTO;
import org.opentcs.access.to.model.LocationCreationTO;
import org.opentcs.access.to.model.LocationRepresentationTO;
import org.opentcs.access.to.model.LocationTypeCreationTO;
import org.opentcs.access.to.model.PathCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.PointCreationTO;
import org.opentcs.access.to.model.PoseCreationTO;
import org.opentcs.access.to.model.TripleCreationTO;
import org.opentcs.access.to.model.VehicleCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.access.to.peripherals.PeripheralOperationCreationTO;
import org.opentcs.util.Colors;

/**
 * Provides methods for mapping {@link PlantModelCreationTO} to {@link V7PlantModelTO} and
 * vice versa.
 */
public class V7TOMapper {

  /**
   * Creates a new instance.
   */
  public V7TOMapper() {
  }

  /**
   * Maps the given model to a {@link PlantModelCreationTO} instance.
   *
   * @param model The model to map.
   * @return The mapped {@link PlantModelCreationTO} instance.
   */
  public PlantModelCreationTO map(V7PlantModelTO model) {
    return new PlantModelCreationTO(model.getName())
        .withPoints(toPointCreationTO(model.getPoints()))
        .withVehicles(toVehicleCreationTO(model.getVehicles()))
        .withPaths(toPathCreationTO(model.getPaths()))
        .withLocationTypes(toLocationTypeCreationTO(model.getLocationTypes()))
        .withLocations(toLocationCreationTO(model.getLocations()))
        .withBlocks(toBlockCreationTO(model.getBlocks()))
        .withVisualLayout(toVisualLayoutCreationTO(model.getVisualLayout()))
        .withProperties(convertProperties(model.getProperties()));
  }

  /**
   * Maps the given model to a {@link V7PlantModelTO} instance.
   *
   * @param model The model to map.
   * @return The mapped {@link V7PlantModelTO} instance.
   */
  public V7PlantModelTO map(PlantModelCreationTO model) {
    V7PlantModelTO result = new V7PlantModelTO();

    result.setName(model.getName());
    result.setVersion(V7PlantModelTO.VERSION_STRING);
    result.getPoints().addAll(toPointTO(model.getPoints(), model.getPaths()));
    result.getVehicles().addAll(toVehicleTO(model.getVehicles()));
    result.getPaths().addAll(toPathTO(model.getPaths()));
    result.getLocationTypes().addAll(toLocationTypeTO(model.getLocationTypes()));
    result.getLocations().addAll(toLocationTO(model.getLocations()));
    result.getBlocks().addAll(toBlockTO(model.getBlocks()));
    result.setVisualLayout(toVisualLayoutTO(model.getVisualLayout()));
    result.getProperties().addAll(convertProperties(model.getProperties()));

    return result;
  }

  //Methods for mapping from PlantModelElementTO to CreationTO start here.
  private List<PointCreationTO> toPointCreationTO(List<PointTO> points) {
    List<PointCreationTO> result = new ArrayList<>();

    for (PointTO point : points) {
      result.add(
          new PointCreationTO(point.getName())
              .withPose(
                  new PoseCreationTO(
                      new TripleCreationTO(
                          point.getPositionX(),
                          point.getPositionY(),
                          point.getPositionZ()
                      ),
                      point.getVehicleOrientationAngle().doubleValue()
                  )
              )
              .withType(toPointType(point.getType()))
              .withVehicleEnvelopes(toEnvelopeMap(point.getVehicleEnvelopes()))
              .withMaxVehicleBoundingBox(toBoundingBoxCreationTO(point.getMaxVehicleBoundingBox()))
              .withProperties(convertProperties(point.getProperties()))
              .withLayout(
                  new PointCreationTO.Layout(
                      new CoupleCreationTO(
                          point.getPointLayout().getLabelOffsetX(),
                          point.getPointLayout().getLabelOffsetY()
                      ),
                      point.getPointLayout().getLayerId()
                  )
              )
      );
    }

    return result;
  }

  private List<VehicleCreationTO> toVehicleCreationTO(List<VehicleTO> vehicles) {
    List<VehicleCreationTO> result = new ArrayList<>();

    for (VehicleTO vehicle : vehicles) {
      result.add(
          new VehicleCreationTO(vehicle.getName())
              .withBoundingBox(toBoundingBoxCreationTO(vehicle.getBoundingBox()))
              .withEnergyLevelThresholdSet(toEnergyLevelThresholdSetCreationTO(vehicle))
              .withMaxReverseVelocity(vehicle.getMaxReverseVelocity())
              .withMaxVelocity(vehicle.getMaxVelocity())
              .withEnvelopeKey(vehicle.getEnvelopeKey())
              .withProperties(convertProperties(vehicle.getProperties()))
              .withLayout(
                  new VehicleCreationTO.Layout(
                      Colors.decodeFromHexRGB(vehicle.getVehicleLayout().getColor())
                  )
              )
      );
    }

    return result;
  }

  private List<PathCreationTO> toPathCreationTO(List<PathTO> paths) {
    List<PathCreationTO> result = new ArrayList<>();

    for (PathTO path : paths) {
      result.add(
          new PathCreationTO(
              path.getName(),
              path.getSourcePoint(),
              path.getDestinationPoint()
          )
              .withLength(path.getLength())
              .withLocked(path.isLocked())
              .withMaxVelocity(path.getMaxVelocity().intValue())
              .withMaxReverseVelocity(path.getMaxReverseVelocity().intValue())
              .withPeripheralOperations(
                  toPeripheralOperationCreationTOs(path.getPeripheralOperations())
              )
              .withVehicleEnvelopes(toEnvelopeMap(path.getVehicleEnvelopes()))
              .withProperties(convertProperties(path.getProperties()))
              .withLayout(
                  new PathCreationTO.Layout(
                      toConnectionType(path.getPathLayout().getConnectionType()),
                      path.getPathLayout().getControlPoints().stream()
                          .map(
                              controlPoint -> new CoupleCreationTO(
                                  controlPoint.getX(), controlPoint.getY()
                              )
                          )
                          .toList(),
                      path.getPathLayout().getLayerId()
                  )
              )
      );
    }

    return result;
  }

  private List<PeripheralOperationCreationTO> toPeripheralOperationCreationTOs(
      List<PeripheralOperationTO> tos
  ) {
    return tos.stream()
        .map(
            to -> new PeripheralOperationCreationTO(to.getName(), to.getLocationName())
                .withExecutionTrigger(
                    toExecutionTrigger(to.getExecutionTrigger())
                )
                .withCompletionRequired(to.isCompletionRequired())
        )
        .toList();
  }

  private List<LocationTypeCreationTO> toLocationTypeCreationTO(
      List<LocationTypeTO> locationTypes
  ) {
    List<LocationTypeCreationTO> result = new ArrayList<>();

    for (LocationTypeTO locationType : locationTypes) {
      result.add(
          new LocationTypeCreationTO(locationType.getName())
              .withAllowedOperations(getOperationNames(locationType.getAllowedOperations()))
              .withAllowedPeripheralOperations(
                  getPeripheralOperationNames(
                      locationType.getAllowedPeripheralOperations()
                  )
              )
              .withProperties(convertProperties(locationType.getProperties()))
              .withLayout(
                  new LocationTypeCreationTO.Layout(
                      toLocationRepresentation(
                          locationType.getLocationTypeLayout().getLocationRepresentation()
                      )
                  )
              )
      );
    }

    return result;
  }

  private List<LocationCreationTO> toLocationCreationTO(List<LocationTO> locations) {
    List<LocationCreationTO> result = new ArrayList<>();

    for (LocationTO location : locations) {
      result.add(
          new LocationCreationTO(
              location.getName(),
              location.getType(),
              new TripleCreationTO(
                  location.getPositionX(),
                  location.getPositionY(),
                  location.getPositionZ()
              )
          )
              .withLinks(getLinks(location))
              .withLocked(location.isLocked())
              .withProperties(convertProperties(location.getProperties()))
              .withLayout(
                  new LocationCreationTO.Layout(
                      new CoupleCreationTO(
                          location.getLocationLayout().getLabelOffsetX(),
                          location.getLocationLayout().getLabelOffsetY()
                      ),
                      toLocationRepresentation(
                          location.getLocationLayout().getLocationRepresentation()
                      ),
                      location.getLocationLayout().getLayerId()
                  )
              )
      );
    }

    return result;
  }

  private List<BlockCreationTO> toBlockCreationTO(List<BlockTO> blocks) {
    List<BlockCreationTO> result = new ArrayList<>();

    for (BlockTO block : blocks) {
      result.add(
          new BlockCreationTO(block.getName())
              .withType(toBlockType(block.getType()))
              .withMemberNames(
                  block.getMembers().stream()
                      .map(member -> member.getName())
                      .collect(Collectors.toSet())
              )
              .withProperties(convertProperties(block.getProperties()))
              .withLayout(
                  new BlockCreationTO.Layout(
                      Colors.decodeFromHexRGB(block.getBlockLayout().getColor())
                  )
              )
      );
    }

    return result;
  }

  private VisualLayoutCreationTO toVisualLayoutCreationTO(VisualLayoutTO visualLayout) {
    return new VisualLayoutCreationTO(visualLayout.getName())
        .withScaleX(visualLayout.getScaleX())
        .withScaleY(visualLayout.getScaleY())
        .withLayers(convertLayers(visualLayout.getLayers()))
        .withLayerGroups(convertLayerGroups(visualLayout.getLayerGroups()))
        .withProperties(convertProperties(visualLayout.getProperties()));
  }

  private List<LayerCreationTO> convertLayers(List<VisualLayoutTO.Layer> layers) {
    List<LayerCreationTO> result = new ArrayList<>();

    for (VisualLayoutTO.Layer layer : layers) {
      result.add(
          new LayerCreationTO(
              layer.getId(),
              layer.getOrdinal(),
              layer.isVisible(),
              layer.getName(),
              layer.getGroupId()
          )
      );
    }

    return result;
  }

  private List<LayerGroupCreationTO> convertLayerGroups(
      List<VisualLayoutTO.LayerGroup> layerGroups
  ) {
    List<LayerGroupCreationTO> result = new ArrayList<>();

    for (VisualLayoutTO.LayerGroup layerGroup : layerGroups) {
      result.add(
          new LayerGroupCreationTO(
              layerGroup.getId(),
              layerGroup.getName(),
              layerGroup.isVisible()
          )
      );
    }

    return result;
  }

  private Map<String, String> convertProperties(List<PropertyTO> propsList) {
    Map<String, String> result = new HashMap<>();
    for (PropertyTO property : propsList) {
      String propName
          = isNullOrEmpty(property.getName()) ? "Property unknown" : property.getName();
      String propValue
          = isNullOrEmpty(property.getValue()) ? "Value unknown" : property.getValue();

      result.put(propName, propValue);
    }

    return result;
  }

  private List<String> getOperationNames(List<AllowedOperationTO> ops) {
    List<String> result = new ArrayList<>(ops.size());
    for (AllowedOperationTO operation : ops) {
      result.add(operation.getName());
    }
    return result;
  }

  private List<String> getPeripheralOperationNames(List<AllowedPeripheralOperationTO> ops) {
    List<String> result = new ArrayList<>(ops.size());
    for (AllowedPeripheralOperationTO operation : ops) {
      result.add(operation.getName());
    }
    return result;
  }

  private Map<String, Set<String>> getLinks(LocationTO to) {
    Map<String, Set<String>> result = new HashMap<>();
    for (LocationTO.Link linkTO : to.getLinks()) {
      result.put(
          linkTO.getPoint(),
          new HashSet<>(getOperationNames(linkTO.getAllowedOperations()))
      );
    }

    return result;
  }

  //Methods for mapping from CreationTO to PlantModelElementTO start here.
  private List<PointTO> toPointTO(List<PointCreationTO> points, List<PathCreationTO> paths) {
    List<PointTO> result = new ArrayList<>();

    // Group paths by their source point (i.e. map points to their respective outgoing paths) and
    // avoid iterating over all paths repeatedly.
    Map<String, List<PathCreationTO>> pathsBySourcePoint = paths.stream()
        .collect(Collectors.groupingBy(PathCreationTO::getSrcPointName));

    for (PointCreationTO point : points) {
      PointTO pointTO = new PointTO();
      pointTO.setName(point.getName());
      pointTO.setPositionX(point.getPose().getPosition().getX())
          .setPositionY(point.getPose().getPosition().getY())
          .setVehicleOrientationAngle((float) point.getPose().getOrientationAngle())
          .setType(toPointTOType(point.getType()))
          .setOutgoingPaths(getOutgoingPaths(point, pathsBySourcePoint))
          .setVehicleEnvelopes(toVehicleEnvelopeTOs(point.getVehicleEnvelopes()))
          .setMaxVehicleBoundingBox(toBoundingBoxTO(point.getMaxVehicleBoundingBox()))
          .setPointLayout(
              new PointTO.PointLayout()
                  .setLabelOffsetX(point.getLayout().getLabelOffset().getX())
                  .setLabelOffsetY(point.getLayout().getLabelOffset().getY())
                  .setLayerId(point.getLayout().getLayerId())
          )
          .setProperties(convertProperties(point.getProperties()));

      result.add(pointTO);
    }

    Collections.sort(result, Comparators.elementsByName());

    return result;
  }

  private List<VehicleTO> toVehicleTO(List<VehicleCreationTO> vehicles) {
    List<VehicleTO> result = new ArrayList<>();

    for (VehicleCreationTO vehicle : vehicles) {
      VehicleTO vehicleTO = new VehicleTO();
      vehicleTO.setName(vehicle.getName());
      vehicleTO.setBoundingBox(toBoundingBoxTO(vehicle.getBoundingBox()))
          .setMaxVelocity(vehicle.getMaxVelocity())
          .setMaxReverseVelocity(vehicle.getMaxReverseVelocity())
          .setEnergyLevelGood((long) vehicle.getEnergyLevelThresholdSet().getEnergyLevelGood())
          .setEnergyLevelCritical(
              (long) vehicle.getEnergyLevelThresholdSet().getEnergyLevelCritical()
          )
          .setEnergyLevelFullyRecharged(
              (long) vehicle.getEnergyLevelThresholdSet().getEnergyLevelFullyRecharged()
          )
          .setEnergyLevelSufficientlyRecharged(
              (long) vehicle.getEnergyLevelThresholdSet().getEnergyLevelSufficientlyRecharged()
          )
          .setEnvelopeKey(vehicle.getEnvelopeKey())
          .setVehicleLayout(
              new VehicleTO.VehicleLayout()
                  .setColor(Colors.encodeToHexRGB(vehicle.getLayout().getRouteColor()))
          )
          .setProperties(convertProperties(vehicle.getProperties()));

      result.add(vehicleTO);
    }

    Collections.sort(result, Comparators.elementsByName());

    return result;
  }

  private List<PathTO> toPathTO(List<PathCreationTO> paths) {
    List<PathTO> result = new ArrayList<>();

    for (PathCreationTO path : paths) {
      PathTO pathTO = new PathTO();
      pathTO.setName(path.getName());
      pathTO.setSourcePoint(path.getSrcPointName())
          .setDestinationPoint(path.getDestPointName())
          .setLength(path.getLength())
          .setMaxVelocity((long) path.getMaxVelocity())
          .setMaxReverseVelocity((long) path.getMaxReverseVelocity())
          .setPeripheralOperations(toPeripheralOperationTOs(path.getPeripheralOperations()))
          .setLocked(path.isLocked())
          .setVehicleEnvelopes(toVehicleEnvelopeTOs(path.getVehicleEnvelopes()))
          .setPathLayout(
              new PathTO.PathLayout()
                  .setConnectionType(toPathTOConnectionType(path.getLayout().getConnectionType()))
                  .setControlPoints(
                      path.getLayout().getControlPoints().stream()
                          .map(controlPoint -> {
                            return new PathTO.ControlPoint()
                                .setX(controlPoint.getX())
                                .setY(controlPoint.getY());
                          })
                          .toList()
                  )
                  .setLayerId(path.getLayout().getLayerId())
          )
          .setProperties(convertProperties(path.getProperties()));

      result.add(pathTO);
    }

    Collections.sort(result, Comparators.elementsByName());

    return result;
  }

  private List<PeripheralOperationTO> toPeripheralOperationTOs(
      List<PeripheralOperationCreationTO> tos
  ) {
    return tos.stream()
        .map(
            to -> (PeripheralOperationTO) new PeripheralOperationTO()
                .setLocationName(to.getLocationName())
                .setExecutionTrigger(
                    toPeripheralOperationTOExecutionTrigger(to.getExecutionTrigger())
                )
                .setCompletionRequired(to.isCompletionRequired())
                .setName(to.getOperation())
        )
        .toList();
  }

  private List<LocationTypeTO> toLocationTypeTO(
      List<LocationTypeCreationTO> locationTypes
  ) {
    List<LocationTypeTO> result = new ArrayList<>();

    for (LocationTypeCreationTO locationType : locationTypes) {
      LocationTypeTO locationTypeTO = new LocationTypeTO();
      locationTypeTO.setName(locationType.getName());
      locationTypeTO.setAllowedOperations(
          toAllowedOperationTOs(locationType.getAllowedOperations())
      )
          .setAllowedPeripheralOperations(
              toAllowedPeripheralOperationTOs(locationType.getAllowedPeripheralOperations())
          )
          .setLocationTypeLayout(
              new LocationTypeTO.LocationTypeLayout()
                  .setLocationRepresentation(
                      toLocationTOLocationRepresentation(
                          locationType.getLayout().getLocationRepresentation()
                      )
                  )
          )
          .setProperties(convertProperties(locationType.getProperties()));

      result.add(locationTypeTO);
    }

    Collections.sort(result, Comparators.elementsByName());

    return result;
  }

  private List<LocationTO> toLocationTO(List<LocationCreationTO> locations) {
    List<LocationTO> result = new ArrayList<>();

    for (LocationCreationTO location : locations) {
      LocationTO locationTO = new LocationTO();
      locationTO.setName(location.getName());
      locationTO.setPositionX(location.getPosition().getX())
          .setPositionY(location.getPosition().getY())
          .setType(location.getTypeName())
          .setLinks(toLocationTOLinks(location.getLinks()))
          .setLocked(location.isLocked())
          .setLocationLayout(
              new LocationTO.LocationLayout()
                  .setLabelOffsetX(location.getLayout().getLabelOffset().getX())
                  .setLabelOffsetY(location.getLayout().getLabelOffset().getY())
                  .setLocationRepresentation(
                      toLocationTOLocationRepresentation(
                          location.getLayout().getLocationRepresentation()
                      )
                  )
                  .setLayerId(location.getLayout().getLayerId())
          )
          .setProperties(convertProperties(location.getProperties()));

      result.add(locationTO);
    }

    Collections.sort(result, Comparators.elementsByName());

    return result;
  }

  private List<BlockTO> toBlockTO(List<BlockCreationTO> blocks) {
    List<BlockTO> result = new ArrayList<>();

    for (BlockCreationTO block : blocks) {
      BlockTO blockTO = new BlockTO();
      blockTO.setName(block.getName());
      blockTO.setType(toBlockTOType(block.getType()))
          .setMembers(toMemberTOs(block.getMemberNames()))
          .setBlockLayout(
              new BlockTO.BlockLayout()
                  .setColor(Colors.encodeToHexRGB(block.getLayout().getColor()))
          )
          .setProperties(convertProperties(block.getProperties()));

      result.add(blockTO);
    }

    Collections.sort(result, Comparators.elementsByName());

    return result;
  }

  private VisualLayoutTO toVisualLayoutTO(VisualLayoutCreationTO layout) {
    VisualLayoutTO result = new VisualLayoutTO();

    result.setName(layout.getName())
        .setProperties(convertProperties(layout.getProperties()));
    result.setScaleX((float) layout.getScaleX())
        .setScaleY((float) layout.getScaleY())
        .setLayers(toLayerTOs(layout.getLayers()))
        .setLayerGroups(toLayerGroupTOs(layout.getLayerGroups()));

    return result;
  }

  private List<VisualLayoutTO.Layer> toLayerTOs(List<LayerCreationTO> layers) {
    List<VisualLayoutTO.Layer> result = new ArrayList<>();

    for (LayerCreationTO layer : layers) {
      result.add(
          new VisualLayoutTO.Layer()
              .setId(layer.getId())
              .setOrdinal(layer.getOrdinal())
              .setVisible(layer.isVisible())
              .setName(layer.getName())
              .setGroupId(layer.getGroupId())
      );
    }

    return result;
  }

  private List<VisualLayoutTO.LayerGroup> toLayerGroupTOs(List<LayerGroupCreationTO> layerGroups) {
    List<VisualLayoutTO.LayerGroup> result = new ArrayList<>();

    for (LayerGroupCreationTO layerGroup : layerGroups) {
      result.add(
          new VisualLayoutTO.LayerGroup()
              .setId(layerGroup.getId())
              .setName(layerGroup.getName())
              .setVisible(layerGroup.isVisible())
      );
    }

    return result;
  }

  private List<PointTO.OutgoingPath> getOutgoingPaths(
      PointCreationTO point,
      Map<String, List<PathCreationTO>> pathsBySourcePoint
  ) {
    List<PointTO.OutgoingPath> result = new ArrayList<>();

    for (PathCreationTO path : pathsBySourcePoint.getOrDefault(point.getName(), List.of())) {
      result.add(new PointTO.OutgoingPath().setName(path.getName()));
    }

    Collections.sort(result, Comparators.outgoingPathsByName());

    return result;
  }

  private List<AllowedOperationTO> toAllowedOperationTOs(Collection<String> allowedOperations) {
    return allowedOperations.stream()
        .sorted()
        .map(allowedOperation -> {
          return (AllowedOperationTO) new AllowedOperationTO().setName(allowedOperation);
        })
        .toList();
  }

  private List<AllowedPeripheralOperationTO> toAllowedPeripheralOperationTOs(
      Collection<String> allowedOperations
  ) {
    return allowedOperations.stream()
        .sorted()
        .map(allowedOperation -> {
          return (AllowedPeripheralOperationTO) new AllowedPeripheralOperationTO()
              .setName(allowedOperation);
        })
        .toList();
  }

  private List<LocationTO.Link> toLocationTOLinks(Map<String, Set<String>> links) {
    List<LocationTO.Link> result = new ArrayList<>();

    links.forEach((key, value) -> {
      result.add(
          new LocationTO.Link()
              .setPoint(key)
              .setAllowedOperations(toAllowedOperationTOs(value))
      );
    });

    Collections.sort(result, Comparators.linksByPointName());

    return result;
  }

  private List<MemberTO> toMemberTOs(Collection<String> members) {
    return members.stream()
        .map(member -> (MemberTO) new MemberTO().setName(member))
        .sorted(Comparators.elementsByName())
        .toList();
  }

  private List<PropertyTO> convertProperties(Map<String, String> properties) {
    List<PropertyTO> result = new ArrayList<>();

    properties.forEach((key, value) -> {
      result.add(new PropertyTO().setName(key).setValue(value));
    });

    Collections.sort(result, Comparators.propertiesByName());

    return result;
  }

  private boolean isNullOrEmpty(String s) {
    return s == null || s.isEmpty();
  }

  private Map<String, EnvelopeCreationTO> toEnvelopeMap(List<VehicleEnvelopeTO> envelopeTOs) {
    return envelopeTOs.stream()
        .collect(
            Collectors.toMap(
                VehicleEnvelopeTO::getKey,
                vehicleEnvelopeTO -> toEnvelope(vehicleEnvelopeTO)
            )
        );
  }

  private EnvelopeCreationTO toEnvelope(VehicleEnvelopeTO vehicleEnvelopeTO) {
    return new EnvelopeCreationTO(
        vehicleEnvelopeTO.getVertices().stream()
            .map(coupleTO -> new CoupleCreationTO(coupleTO.getX(), coupleTO.getY()))
            .toList()
    );
  }

  private List<VehicleEnvelopeTO> toVehicleEnvelopeTOs(
      Map<String, EnvelopeCreationTO> envelopeMap
  ) {
    return envelopeMap.entrySet()
        .stream()
        .sorted(Map.Entry.comparingByKey())
        .map(
            entry -> new VehicleEnvelopeTO()
                .setKey(entry.getKey())
                .setVertices(toCoupleTOs(entry.getValue().getVertices()))
        )
        .toList();
  }

  private List<CoupleTO> toCoupleTOs(List<CoupleCreationTO> couples) {
    return couples.stream()
        .map(
            couple -> new CoupleTO()
                .setX(couple.getX())
                .setY(couple.getY())
        )
        .toList();
  }

  private BoundingBoxCreationTO toBoundingBoxCreationTO(BoundingBoxTO boundingBox) {
    return new BoundingBoxCreationTO(
        boundingBox.getLength(),
        boundingBox.getWidth(),
        boundingBox.getHeight()
    )
        .withReferenceOffset(
            new CoupleCreationTO(
                boundingBox.getReferenceOffsetX(),
                boundingBox.getReferenceOffsetY()
            )
        );
  }

  private VehicleCreationTO.EnergyLevelThresholdSet toEnergyLevelThresholdSetCreationTO(
      VehicleTO vehicle
  ) {
    return new VehicleCreationTO.EnergyLevelThresholdSet(
        vehicle.getEnergyLevelCritical().intValue(),
        vehicle.getEnergyLevelGood().intValue(),
        vehicle.getEnergyLevelSufficientlyRecharged().intValue(),
        vehicle.getEnergyLevelFullyRecharged().intValue()
    );
  }

  private BoundingBoxTO toBoundingBoxTO(BoundingBoxCreationTO boundingBox) {
    return new BoundingBoxTO()
        .setLength(boundingBox.getLength())
        .setWidth(boundingBox.getWidth())
        .setHeight(boundingBox.getHeight())
        .setReferenceOffsetX(boundingBox.getReferenceOffset().getX())
        .setReferenceOffsetY(boundingBox.getReferenceOffset().getY());
  }

  private PointCreationTO.Type toPointType(PointTO.Type type) {
    return switch (type) {
      case HALT_POSITION -> PointCreationTO.Type.HALT_POSITION;
      case PARK_POSITION -> PointCreationTO.Type.PARK_POSITION;
    };
  }

  private PathCreationTO.Layout.ConnectionType toConnectionType(
      PathTO.PathLayout.ConnectionType connectionType
  ) {
    return switch (connectionType) {
      case BEZIER -> PathCreationTO.Layout.ConnectionType.BEZIER;
      case BEZIER_3 -> PathCreationTO.Layout.ConnectionType.BEZIER_3;
      case DIRECT -> PathCreationTO.Layout.ConnectionType.DIRECT;
      case ELBOW -> PathCreationTO.Layout.ConnectionType.ELBOW;
      case POLYPATH -> PathCreationTO.Layout.ConnectionType.POLYPATH;
      case SLANTED -> PathCreationTO.Layout.ConnectionType.SLANTED;
    };
  }

  private PeripheralOperationCreationTO.ExecutionTrigger toExecutionTrigger(
      PeripheralOperationTO.ExecutionTrigger executionTrigger
  ) {
    return switch (executionTrigger) {
      case AFTER_ALLOCATION -> PeripheralOperationCreationTO.ExecutionTrigger.AFTER_ALLOCATION;
      case AFTER_MOVEMENT -> PeripheralOperationCreationTO.ExecutionTrigger.AFTER_MOVEMENT;
    };
  }

  private LocationRepresentationTO toLocationRepresentation(
      org.opentcs.util.persistence.v7.LocationRepresentation locRepresentation
  ) {
    return switch (locRepresentation) {
      case DEFAULT -> LocationRepresentationTO.DEFAULT;
      case LOAD_TRANSFER_ALT_1 -> LocationRepresentationTO.LOAD_TRANSFER_ALT_1;
      case LOAD_TRANSFER_ALT_2 -> LocationRepresentationTO.LOAD_TRANSFER_ALT_2;
      case LOAD_TRANSFER_ALT_3 -> LocationRepresentationTO.LOAD_TRANSFER_ALT_3;
      case LOAD_TRANSFER_ALT_4 -> LocationRepresentationTO.LOAD_TRANSFER_ALT_4;
      case LOAD_TRANSFER_ALT_5 -> LocationRepresentationTO.LOAD_TRANSFER_ALT_5;
      case LOAD_TRANSFER_GENERIC -> LocationRepresentationTO.LOAD_TRANSFER_GENERIC;
      case NONE -> LocationRepresentationTO.NONE;
      case RECHARGE_ALT_1 -> LocationRepresentationTO.RECHARGE_ALT_1;
      case RECHARGE_ALT_2 -> LocationRepresentationTO.RECHARGE_ALT_2;
      case RECHARGE_GENERIC -> LocationRepresentationTO.RECHARGE_GENERIC;
      case WORKING_ALT_1 -> LocationRepresentationTO.WORKING_ALT_1;
      case WORKING_ALT_2 -> LocationRepresentationTO.WORKING_ALT_2;
      case WORKING_GENERIC -> LocationRepresentationTO.WORKING_GENERIC;
    };
  }

  private BlockCreationTO.Type toBlockType(BlockTO.Type type) {
    return switch (type) {
      case SAME_DIRECTION_ONLY -> BlockCreationTO.Type.SAME_DIRECTION_ONLY;
      case SINGLE_VEHICLE_ONLY -> BlockCreationTO.Type.SINGLE_VEHICLE_ONLY;
    };
  }

  private PointTO.Type toPointTOType(PointCreationTO.Type type) {
    return switch (type) {
      case HALT_POSITION -> PointTO.Type.HALT_POSITION;
      case PARK_POSITION -> PointTO.Type.PARK_POSITION;
    };
  }

  private PathTO.PathLayout.ConnectionType toPathTOConnectionType(
      PathCreationTO.Layout.ConnectionType connectionType
  ) {
    return switch (connectionType) {
      case BEZIER -> PathTO.PathLayout.ConnectionType.BEZIER;
      case BEZIER_3 -> PathTO.PathLayout.ConnectionType.BEZIER_3;
      case DIRECT -> PathTO.PathLayout.ConnectionType.DIRECT;
      case ELBOW -> PathTO.PathLayout.ConnectionType.ELBOW;
      case POLYPATH -> PathTO.PathLayout.ConnectionType.POLYPATH;
      case SLANTED -> PathTO.PathLayout.ConnectionType.SLANTED;
    };
  }

  private PeripheralOperationTO.ExecutionTrigger toPeripheralOperationTOExecutionTrigger(
      PeripheralOperationCreationTO.ExecutionTrigger executionTrigger
  ) {
    return switch (executionTrigger) {
      case AFTER_ALLOCATION -> PeripheralOperationTO.ExecutionTrigger.AFTER_ALLOCATION;
      case AFTER_MOVEMENT -> PeripheralOperationTO.ExecutionTrigger.AFTER_MOVEMENT;
      // The case IMMEDIATE will probably never occur in a persisted model, but if it does it will
      // be mapped to AFTER_ALLOCATION.
      case IMMEDIATE -> PeripheralOperationTO.ExecutionTrigger.AFTER_ALLOCATION;
    };
  }

  @SuppressWarnings("checkstyle:LineLength")
  private org.opentcs.util.persistence.v7.LocationRepresentation toLocationTOLocationRepresentation(
      LocationRepresentationTO locRepresentation
  ) {
    return switch (locRepresentation) {
      case DEFAULT -> org.opentcs.util.persistence.v7.LocationRepresentation.DEFAULT;
      case LOAD_TRANSFER_ALT_1 -> org.opentcs.util.persistence.v7.LocationRepresentation.LOAD_TRANSFER_ALT_1;
      case LOAD_TRANSFER_ALT_2 -> org.opentcs.util.persistence.v7.LocationRepresentation.LOAD_TRANSFER_ALT_2;
      case LOAD_TRANSFER_ALT_3 -> org.opentcs.util.persistence.v7.LocationRepresentation.LOAD_TRANSFER_ALT_3;
      case LOAD_TRANSFER_ALT_4 -> org.opentcs.util.persistence.v7.LocationRepresentation.LOAD_TRANSFER_ALT_4;
      case LOAD_TRANSFER_ALT_5 -> org.opentcs.util.persistence.v7.LocationRepresentation.LOAD_TRANSFER_ALT_5;
      case LOAD_TRANSFER_GENERIC -> org.opentcs.util.persistence.v7.LocationRepresentation.LOAD_TRANSFER_GENERIC;
      case NONE -> org.opentcs.util.persistence.v7.LocationRepresentation.NONE;
      case RECHARGE_ALT_1 -> org.opentcs.util.persistence.v7.LocationRepresentation.RECHARGE_ALT_1;
      case RECHARGE_ALT_2 -> org.opentcs.util.persistence.v7.LocationRepresentation.RECHARGE_ALT_2;
      case RECHARGE_GENERIC -> org.opentcs.util.persistence.v7.LocationRepresentation.RECHARGE_GENERIC;
      case WORKING_ALT_1 -> org.opentcs.util.persistence.v7.LocationRepresentation.WORKING_ALT_1;
      case WORKING_ALT_2 -> org.opentcs.util.persistence.v7.LocationRepresentation.WORKING_ALT_2;
      case WORKING_GENERIC -> org.opentcs.util.persistence.v7.LocationRepresentation.WORKING_GENERIC;
    };
  }

  private BlockTO.Type toBlockTOType(BlockCreationTO.Type type) {
    return switch (type) {
      case SAME_DIRECTION_ONLY -> BlockTO.Type.SAME_DIRECTION_ONLY;
      case SINGLE_VEHICLE_ONLY -> BlockTO.Type.SINGLE_VEHICLE_ONLY;
    };
  }
}
