// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.util.persistence.v005;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.opentcs.util.persistence.v004.V004ModelParser;
import org.opentcs.util.persistence.v004.V004PlantModelTO;

/**
 * The parser for V005 models.
 */
public class V005ModelParser {

  /**
   * Creates a new instance.
   */
  public V005ModelParser() {
  }

  /**
   * Reads a model with the given reader and parses it to a {@link V005PlantModelTO} instance.
   *
   * @param reader The reader to use.
   * @param modelVersion The model version.
   * @return The parsed {@link V005PlantModelTO}.
   * @throws IOException If there was an error reading the model.
   */
  public V005PlantModelTO readRaw(Reader reader, String modelVersion)
      throws IOException {
    if (Objects.equals(modelVersion, V005PlantModelTO.VERSION_STRING)) {
      return V005PlantModelTO.fromXml(reader);
    }
    else {
      return convert(new V004ModelParser().readRaw(reader, modelVersion));
    }
  }

  private V005PlantModelTO convert(V004PlantModelTO to) {
    return new V005PlantModelTO()
        .setName(to.getName())
        .setPoints(convertPoints(to))
        .setPaths(convertPaths(to))
        .setVehicles(convertVehicles(to))
        .setLocationTypes(convertLocationTypes(to))
        .setLocations(convertLocations(to))
        .setBlocks(convertBlocks(to))
        .setVisualLayout(convertVisualLayout(to))
        .setProperties(convertProperties(to.getProperties()));
  }

  private List<PropertyTO> convertProperties(
      List<org.opentcs.util.persistence.v004.PropertyTO> tos
  ) {
    return tos.stream()
        .map(property -> new PropertyTO().setName(property.getName()).setValue(property.getValue()))
        .toList();
  }

  private List<PointTO> convertPoints(V004PlantModelTO to) {
    return to.getPoints().stream()
        .map(point -> {
          PointTO result = new PointTO();
          result.setName(point.getName())
              .setProperties(convertProperties(point.getProperties()));
          result.setxPosition(point.getxPosition())
              .setyPosition(point.getyPosition())
              .setzPosition(point.getzPosition())
              .setVehicleOrientationAngle(point.getVehicleOrientationAngle())
              .setType(convertPointTOType(point.getType()))
              .setVehicleEnvelopes(convertVehicleEnvelopes(point.getVehicleEnvelopes()))
              .setOutgoingPaths(convertOutgoingPaths(point))
              .setPointLayout(
                  new PointTO.PointLayout()
                      .setxPosition(point.getPointLayout().getxPosition())
                      .setyPosition(point.getPointLayout().getyPosition())
                      .setxLabelOffset(point.getPointLayout().getxLabelOffset())
                      .setyLabelOffset(point.getPointLayout().getyLabelOffset())
                      .setLayerId(point.getPointLayout().getLayerId())
              );
          return result;
        })
        .toList();
  }

  private List<VehicleEnvelopeTO> convertVehicleEnvelopes(
      List<org.opentcs.util.persistence.v004.VehicleEnvelopeTO> tos
  ) {
    return tos.stream()
        .map(
            vehicleEnvelope -> new VehicleEnvelopeTO()
                .setKey(vehicleEnvelope.getKey())
                .setVertices(
                    vehicleEnvelope.getVertices().stream()
                        .map(
                            couple -> new CoupleTO()
                                .setX(couple.getX())
                                .setY(couple.getY())
                        )
                        .toList()
                )
        )
        .toList();
  }

  private Map<String, String> toPropertiesMap(
      List<org.opentcs.util.persistence.v004.PropertyTO> properties
  ) {
    Map<String, String> result = new HashMap<>();
    for (org.opentcs.util.persistence.v004.PropertyTO property : properties) {
      result.put(property.getName(), property.getValue());
    }
    return result;
  }

  private List<PointTO.OutgoingPath> convertOutgoingPaths(
      org.opentcs.util.persistence.v004.PointTO to
  ) {
    return to.getOutgoingPaths().stream()
        .map(path -> new PointTO.OutgoingPath().setName(path.getName()))
        .toList();
  }

  private List<PathTO> convertPaths(V004PlantModelTO to) {
    return to.getPaths().stream()
        .map(path -> {
          PathTO result = new PathTO();
          result.setName(path.getName())
              .setProperties(convertProperties(path.getProperties()));
          result.setSourcePoint(path.getSourcePoint())
              .setDestinationPoint(path.getDestinationPoint())
              .setLength(path.getLength())
              .setMaxVelocity(path.getMaxVelocity())
              .setMaxReverseVelocity(path.getMaxReverseVelocity())
              .setPeripheralOperations(convertPeripheralOperations(path.getPeripheralOperations()))
              .setLocked(path.isLocked())
              .setVehicleEnvelopes(convertVehicleEnvelopes(path.getVehicleEnvelopes()))
              .setPathLayout(
                  new PathTO.PathLayout()
                      .setConnectionType(
                          convertConnectionType(path.getPathLayout().getConnectionType())
                      )
                      .setControlPoints(
                          path.getPathLayout().getControlPoints().stream()
                              .map(
                                  controlPoint -> new PathTO.ControlPoint()
                                      .setX(controlPoint.getX())
                                      .setY(controlPoint.getY())
                              )
                              .toList()
                      )
                      .setLayerId(path.getPathLayout().getLayerId())
              );
          return result;
        })
        .toList();
  }

  private List<PeripheralOperationTO> convertPeripheralOperations(
      List<org.opentcs.util.persistence.v004.PeripheralOperationTO> tos
  ) {
    return tos.stream()
        .map(
            peripheralOperation -> {
              PeripheralOperationTO result = new PeripheralOperationTO();
              result.setName(peripheralOperation.getName())
                  .setProperties(convertProperties(peripheralOperation.getProperties()));
              result.setLocationName(peripheralOperation.getLocationName())
                  .setExecutionTrigger(
                      convertExecutionTrigger(peripheralOperation.getExecutionTrigger())
                  )
                  .setCompletionRequired(peripheralOperation.isCompletionRequired());
              return result;
            }
        )
        .toList();
  }

  private List<VehicleTO> convertVehicles(V004PlantModelTO to) {
    return to.getVehicles().stream()
        .map(vehicle -> {
          VehicleTO result = new VehicleTO();
          result.setName(vehicle.getName())
              .setProperties(convertProperties(vehicle.getProperties()));
          result.setLength(vehicle.getLength())
              .setEnergyLevelCritical(vehicle.getEnergyLevelCritical())
              .setEnergyLevelGood(vehicle.getEnergyLevelGood())
              .setEnergyLevelFullyRecharged(vehicle.getEnergyLevelFullyRecharged())
              .setEnergyLevelSufficientlyRecharged(vehicle.getEnergyLevelSufficientlyRecharged())
              .setMaxVelocity(vehicle.getMaxVelocity())
              .setMaxReverseVelocity(vehicle.getMaxReverseVelocity())
              .setEnvelopeKey(vehicle.getEnvelopeKey())
              .setVehicleLayout(
                  new VehicleTO.VehicleLayout()
                      .setColor(vehicle.getVehicleLayout().getColor())
              );
          return result;
        })
        .toList();
  }

  private List<LocationTypeTO> convertLocationTypes(V004PlantModelTO to) {
    return to.getLocationTypes().stream()
        .map(locationType -> {
          LocationTypeTO result = new LocationTypeTO();
          result.setName(locationType.getName())
              .setProperties(convertProperties(locationType.getProperties()));
          result.setAllowedOperations(convertAllowedOperations(locationType.getAllowedOperations()))
              .setAllowedPeripheralOperations(
                  convertAllowedPeripheralOperations(locationType.getAllowedPeripheralOperations())
              )
              .setLocationTypeLayout(
                  new LocationTypeTO.LocationTypeLayout()
                      .setLocationRepresentation(
                          convertLocationRepresentation(
                              locationType.getLocationTypeLayout().getLocationRepresentation()
                          )
                      )
              );
          return result;
        })
        .toList();
  }

  private List<AllowedOperationTO> convertAllowedOperations(
      List<org.opentcs.util.persistence.v004.AllowedOperationTO> tos
  ) {
    return tos.stream()
        .map(allowedOperation -> {
          AllowedOperationTO result = new AllowedOperationTO();
          result.setName(allowedOperation.getName());
          result.setProperties(convertProperties(allowedOperation.getProperties()));
          return result;
        })
        .toList();
  }

  private List<AllowedPeripheralOperationTO> convertAllowedPeripheralOperations(
      List<org.opentcs.util.persistence.v004.AllowedPeripheralOperationTO> tos
  ) {
    return tos.stream()
        .map(
            allowedPeripheralOperation -> {
              AllowedPeripheralOperationTO result = new AllowedPeripheralOperationTO();
              result.setName(allowedPeripheralOperation.getName());
              result.setProperties(convertProperties(allowedPeripheralOperation.getProperties()));
              return result;
            }
        )
        .toList();
  }

  private List<LocationTO> convertLocations(V004PlantModelTO to) {
    return to.getLocations().stream()
        .map(location -> {
          LocationTO result = new LocationTO();
          result.setName(location.getName())
              .setProperties(convertProperties(location.getProperties()));
          result.setxPosition(location.getxPosition())
              .setyPosition(location.getyPosition())
              .setzPosition(location.getzPosition())
              .setType(location.getType())
              .setLinks(convertLinks(location))
              .setLocked(location.isLocked())
              .setLocationLayout(
                  new LocationTO.LocationLayout()
                      .setxPosition(location.getLocationLayout().getxPosition())
                      .setyPosition(location.getLocationLayout().getyPosition())
                      .setxLabelOffset(location.getLocationLayout().getxLabelOffset())
                      .setyLabelOffset(location.getLocationLayout().getyLabelOffset())
                      .setLocationRepresentation(
                          convertLocationRepresentation(
                              location.getLocationLayout().getLocationRepresentation()
                          )
                      )
                      .setLayerId(location.getLocationLayout().getLayerId())
              );
          return result;
        })
        .toList();
  }

  private List<LocationTO.Link> convertLinks(org.opentcs.util.persistence.v004.LocationTO to) {
    return to.getLinks().stream()
        .map(link -> {
          return new LocationTO.Link()
              .setPoint(link.getPoint())
              .setAllowedOperations(convertAllowedOperations(link.getAllowedOperations()));
        })
        .toList();
  }

  private List<BlockTO> convertBlocks(V004PlantModelTO to) {
    return to.getBlocks().stream()
        .map(block -> {
          BlockTO result = new BlockTO();
          result.setName(block.getName())
              .setProperties(convertProperties(block.getProperties()));
          result.setType(convertBlockTOType(block.getType()))
              .setMembers(convertMembers(block.getMembers()))
              .setBlockLayout(
                  new BlockTO.BlockLayout()
                      .setColor(block.getBlockLayout().getColor())
              );
          return result;
        })
        .toList();
  }

  private List<MemberTO> convertMembers(List<org.opentcs.util.persistence.v004.MemberTO> tos) {
    return tos.stream()
        .map(member -> {
          MemberTO result = new MemberTO();
          result.setName(member.getName())
              .setProperties(convertProperties(member.getProperties()));
          return result;
        })
        .toList();
  }

  private VisualLayoutTO convertVisualLayout(V004PlantModelTO to) {
    VisualLayoutTO result = new VisualLayoutTO()
        .setScaleX(to.getVisualLayout().getScaleX())
        .setScaleY(to.getVisualLayout().getScaleY())
        .setLayers(
            to.getVisualLayout().getLayers().stream()
                .map(
                    layer -> new VisualLayoutTO.Layer()
                        .setId(layer.getId())
                        .setOrdinal(layer.getOrdinal())
                        .setVisible(layer.isVisible())
                        .setName(layer.getName())
                        .setGroupId(layer.getGroupId())
                )
                .toList()
        )
        .setLayerGroups(
            to.getVisualLayout().getLayerGroups().stream()
                .map(
                    layerGroup -> new VisualLayoutTO.LayerGroup()
                        .setId(layerGroup.getId())
                        .setName(layerGroup.getName())
                        .setVisible(layerGroup.isVisible())
                )
                .toList()
        );
    result
        .setProperties(convertProperties(to.getVisualLayout().getProperties()))
        .setName(to.getVisualLayout().getName());

    return result;
  }

  private PointTO.Type convertPointTOType(org.opentcs.util.persistence.v004.PointTO.Type type) {
    switch (type) {
      case org.opentcs.util.persistence.v004.PointTO.Type.HALT_POSITION:
      case org.opentcs.util.persistence.v004.PointTO.Type.REPORT_POSITION:
        return PointTO.Type.HALT_POSITION;
      case org.opentcs.util.persistence.v004.PointTO.Type.PARK_POSITION:
        return PointTO.Type.PARK_POSITION;
      default:
        throw new IllegalArgumentException(type.name() + " does not exist");
    }
  }

  private PathTO.PathLayout.ConnectionType convertConnectionType(
      org.opentcs.util.persistence.v004.PathTO.PathLayout.ConnectionType connectionType
  ) {
    switch (connectionType) {
      case org.opentcs.util.persistence.v004.PathTO.PathLayout.ConnectionType.BEZIER:
        return PathTO.PathLayout.ConnectionType.BEZIER;
      case org.opentcs.util.persistence.v004.PathTO.PathLayout.ConnectionType.BEZIER_3:
        return PathTO.PathLayout.ConnectionType.BEZIER_3;
      case org.opentcs.util.persistence.v004.PathTO.PathLayout.ConnectionType.DIRECT:
        return PathTO.PathLayout.ConnectionType.DIRECT;
      case org.opentcs.util.persistence.v004.PathTO.PathLayout.ConnectionType.ELBOW:
        return PathTO.PathLayout.ConnectionType.ELBOW;
      case org.opentcs.util.persistence.v004.PathTO.PathLayout.ConnectionType.POLYPATH:
        return PathTO.PathLayout.ConnectionType.POLYPATH;
      case org.opentcs.util.persistence.v004.PathTO.PathLayout.ConnectionType.SLANTED:
        return PathTO.PathLayout.ConnectionType.SLANTED;
      default:
        throw new IllegalArgumentException(connectionType.name() + " does not exist");
    }
  }

  @SuppressWarnings("checkstyle:LineLength")
  private PeripheralOperationTO.ExecutionTrigger convertExecutionTrigger(
      org.opentcs.util.persistence.v004.PeripheralOperationTO.ExecutionTrigger executionTrigger
  ) {
    switch (executionTrigger) {
      case org.opentcs.util.persistence.v004.PeripheralOperationTO.ExecutionTrigger.AFTER_ALLOCATION:
      case org.opentcs.util.persistence.v004.PeripheralOperationTO.ExecutionTrigger.BEFORE_MOVEMENT:
        return PeripheralOperationTO.ExecutionTrigger.AFTER_ALLOCATION;
      case org.opentcs.util.persistence.v004.PeripheralOperationTO.ExecutionTrigger.AFTER_MOVEMENT:
        return PeripheralOperationTO.ExecutionTrigger.AFTER_MOVEMENT;
      default:
        throw new IllegalArgumentException(executionTrigger.name() + " does not exist");
    }
  }

  private LocationRepresentation convertLocationRepresentation(
      org.opentcs.util.persistence.v004.LocationRepresentation locRepresentation
  ) {
    switch (locRepresentation) {
      case org.opentcs.util.persistence.v004.LocationRepresentation.DEFAULT:
        return LocationRepresentation.DEFAULT;
      case org.opentcs.util.persistence.v004.LocationRepresentation.LOAD_TRANSFER_ALT_1:
        return LocationRepresentation.LOAD_TRANSFER_ALT_1;
      case org.opentcs.util.persistence.v004.LocationRepresentation.LOAD_TRANSFER_ALT_2:
        return LocationRepresentation.LOAD_TRANSFER_ALT_2;
      case org.opentcs.util.persistence.v004.LocationRepresentation.LOAD_TRANSFER_ALT_3:
        return LocationRepresentation.LOAD_TRANSFER_ALT_3;
      case org.opentcs.util.persistence.v004.LocationRepresentation.LOAD_TRANSFER_ALT_4:
        return LocationRepresentation.LOAD_TRANSFER_ALT_4;
      case org.opentcs.util.persistence.v004.LocationRepresentation.LOAD_TRANSFER_ALT_5:
        return LocationRepresentation.LOAD_TRANSFER_ALT_5;
      case org.opentcs.util.persistence.v004.LocationRepresentation.LOAD_TRANSFER_GENERIC:
        return LocationRepresentation.LOAD_TRANSFER_GENERIC;
      case org.opentcs.util.persistence.v004.LocationRepresentation.NONE:
        return LocationRepresentation.NONE;
      case org.opentcs.util.persistence.v004.LocationRepresentation.RECHARGE_ALT_1:
        return LocationRepresentation.RECHARGE_ALT_1;
      case org.opentcs.util.persistence.v004.LocationRepresentation.RECHARGE_ALT_2:
        return LocationRepresentation.RECHARGE_ALT_2;
      case org.opentcs.util.persistence.v004.LocationRepresentation.RECHARGE_GENERIC:
        return LocationRepresentation.RECHARGE_GENERIC;
      case org.opentcs.util.persistence.v004.LocationRepresentation.WORKING_ALT_1:
        return LocationRepresentation.WORKING_ALT_1;
      case org.opentcs.util.persistence.v004.LocationRepresentation.WORKING_ALT_2:
        return LocationRepresentation.WORKING_ALT_2;
      case org.opentcs.util.persistence.v004.LocationRepresentation.WORKING_GENERIC:
        return LocationRepresentation.WORKING_GENERIC;
      default:
        throw new IllegalArgumentException(locRepresentation.name() + " does not exist");
    }
  }

  private BlockTO.Type convertBlockTOType(org.opentcs.util.persistence.v004.BlockTO.Type type) {
    switch (type) {
      case org.opentcs.util.persistence.v004.BlockTO.Type.SAME_DIRECTION_ONLY:
        return BlockTO.Type.SAME_DIRECTION_ONLY;
      case org.opentcs.util.persistence.v004.BlockTO.Type.SINGLE_VEHICLE_ONLY:
        return BlockTO.Type.SINGLE_VEHICLE_ONLY;
      default:
        throw new IllegalArgumentException(type.name() + " does not exist");
    }
  }
}
