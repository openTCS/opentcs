// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.exchange.adapter;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.opentcs.access.to.model.CoupleCreationTO;
import org.opentcs.access.to.model.EnvelopeCreationTO;
import org.opentcs.access.to.model.PathCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.peripherals.PeripheralOperationCreationTO;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Envelope;
import org.opentcs.data.model.Path;
import org.opentcs.data.peripherals.PeripheralOperation;
import org.opentcs.guing.base.components.layer.LayerWrapper;
import org.opentcs.guing.base.components.properties.type.LengthProperty;
import org.opentcs.guing.base.components.properties.type.SpeedProperty;
import org.opentcs.guing.base.model.EnvelopeModel;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.PeripheralOperationModel;
import org.opentcs.guing.base.model.elements.LayoutModel;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.common.model.SystemModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An adapter for Path objects.
 */
public class PathAdapter
    extends
      AbstractProcessAdapter {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PathAdapter.class);

  /**
   * Creates a new instance.
   */
  public PathAdapter() {
  }

  @Override
  public void updateModelProperties(
      TCSObject<?> tcsObject,
      ModelComponent modelComponent,
      SystemModel systemModel,
      TCSObjectService objectService
  ) {
    Path path = requireNonNull((Path) tcsObject, "tcsObject");
    PathModel model = (PathModel) modelComponent;

    model.getPropertyName().setText(path.getName());
    model.getPropertyStartComponent().setText(path.getSourcePoint().getName());
    model.getPropertyEndComponent().setText(path.getDestinationPoint().getName());
    model.getPropertyLength().setValueAndUnit(path.getLength(), LengthProperty.Unit.MM);
    model.getPropertyMaxVelocity().setValueAndUnit(
        path.getMaxVelocity(),
        SpeedProperty.Unit.MM_S
    );
    model.getPropertyMaxReverseVelocity().setValueAndUnit(
        path.getMaxReverseVelocity(),
        SpeedProperty.Unit.MM_S
    );
    model.getPropertyLocked().setValue(path.isLocked());
    for (PeripheralOperation operation : path.getPeripheralOperations()) {
      model.getPropertyPeripheralOperations().getValue().add(
          new PeripheralOperationModel(
              operation.getLocation().getName(),
              operation.getOperation(),
              operation.getExecutionTrigger(),
              operation.isCompletionRequired()
          )
      );
    }

    for (Map.Entry<String, Envelope> entry : path.getVehicleEnvelopes().entrySet()) {
      model.getPropertyVehicleEnvelopes().getValue().add(
          new EnvelopeModel(entry.getKey(), entry.getValue().getVertices())
      );
    }

    updateMiscModelProperties(model, path);
    updateModelLayoutProperties(model, path, systemModel.getLayoutModel());
    model.propertiesChanged(model);
  }

  @Override
  public PlantModelCreationTO storeToPlantModel(
      ModelComponent modelComponent,
      SystemModel systemModel,
      PlantModelCreationTO plantModel
  ) {
    PathModel pathModel = (PathModel) modelComponent;

    LOG.debug(
        "Path {}: srcPoint is {}, dstPoint is {}.",
        pathModel.getName(),
        getSourcePoint(pathModel),
        getDestinationPoint(pathModel)
    );

    PlantModelCreationTO result = plantModel
        .withPath(
            new PathCreationTO(
                pathModel.getName(),
                getSourcePoint(pathModel),
                getDestinationPoint(pathModel)
            )
                .withLength(getLength(pathModel))
                .withMaxVelocity(getMaxVelocity(pathModel))
                .withMaxReverseVelocity(getMaxReverseVelocity(pathModel))
                .withProperties(getKernelProperties(pathModel))
                .withLocked(getLocked(pathModel))
                .withPeripheralOperations(getPeripheralOperations(pathModel))
                .withVehicleEnvelopes(getKernelVehicleEnvelopes(pathModel))
                .withLayout(getLayout(pathModel))
        );

    unmarkAllPropertiesChanged(pathModel);

    return result;
  }

  private void updateModelLayoutProperties(PathModel model, Path path, LayoutModel layoutModel) {
    model.getPropertyPathConnType()
        .setValue(toPathModelConnectionType(path.getLayout().getConnectionType()));
    String controlPointsString = path.getLayout().getControlPoints().stream()
        .map(point -> String.format("%d,%d", point.getX(), point.getY()))
        .collect(Collectors.joining(";"));
    model.getPropertyPathControlPoints().setText(controlPointsString);
    LayerWrapper layerWrapper = layoutModel.getPropertyLayerWrappers()
        .getValue().get(path.getLayout().getLayerId());
    model.getPropertyLayerWrapper().setValue(layerWrapper);
  }

  private PathModel.Type toPathModelConnectionType(Path.Layout.ConnectionType connectionType) {
    return switch (connectionType) {
      case DIRECT -> PathModel.Type.DIRECT;
      case ELBOW -> PathModel.Type.ELBOW;
      case SLANTED -> PathModel.Type.SLANTED;
      case POLYPATH -> PathModel.Type.POLYPATH;
      case BEZIER -> PathModel.Type.BEZIER;
      case BEZIER_3 -> PathModel.Type.BEZIER_3;
    };
  }

  private boolean getLocked(PathModel model) {
    if (model.getPropertyLocked().getValue() instanceof Boolean) {
      return (boolean) model.getPropertyLocked().getValue();
    }
    return false;
  }

  private int getMaxVelocity(PathModel model) {
    return (int) Math.abs(
        model.getPropertyMaxVelocity()
            .getValueByUnit(SpeedProperty.Unit.MM_S)
    );
  }

  private int getMaxReverseVelocity(PathModel model) {
    return (int) Math.abs(
        model.getPropertyMaxReverseVelocity()
            .getValueByUnit(SpeedProperty.Unit.MM_S)
    );
  }

  private String getSourcePoint(PathModel model) {
    return model.getPropertyStartComponent().getText();
  }

  private String getDestinationPoint(PathModel model) {
    return model.getPropertyEndComponent().getText();
  }

  private long getLength(PathModel model) {
    LengthProperty pLength = model.getPropertyLength();

    if ((double) pLength.getValue() <= 0) {
      try {
        pLength.setValueAndUnit(1.0, pLength.getUnit());
        pLength.markChanged();
      }
      catch (IllegalArgumentException ex) {
        LOG.warn("", ex);
      }
    }

    return (long) pLength.getValueByUnit(LengthProperty.Unit.MM);
  }

  private Map<String, EnvelopeCreationTO> getKernelVehicleEnvelopes(PathModel model) {
    return model.getPropertyVehicleEnvelopes().getValue().stream()
        .collect(
            Collectors.toMap(
                EnvelopeModel::getKey,
                envelopeModel -> new EnvelopeCreationTO(toCoupleTOs(envelopeModel.getVertices()))
            )
        );
  }

  private PathCreationTO.Layout getLayout(PathModel model) {
    List<Couple> controlPoints
        = Arrays.asList(model.getPropertyPathControlPoints().getText().split(";")).stream()
            .filter(controlPointString -> !controlPointString.isEmpty())
            .map(controlPointString -> {
              String[] coordinateStrings = controlPointString.split(",");
              return new Couple(
                  Long.parseLong(coordinateStrings[0]),
                  Long.parseLong(coordinateStrings[1])
              );
            })
            .collect(Collectors.toList());

    return new PathCreationTO.Layout(
        toPathConnectionType((PathModel.Type) model.getPropertyPathConnType().getValue()),
        toCoupleTOs(controlPoints),
        model.getPropertyLayerWrapper().getValue().getLayer().getId()
    );
  }

  private PathCreationTO.Layout.ConnectionType toPathConnectionType(PathModel.Type type) {
    return switch (type) {
      case DIRECT -> PathCreationTO.Layout.ConnectionType.DIRECT;
      case ELBOW -> PathCreationTO.Layout.ConnectionType.ELBOW;
      case SLANTED -> PathCreationTO.Layout.ConnectionType.SLANTED;
      case POLYPATH -> PathCreationTO.Layout.ConnectionType.POLYPATH;
      case BEZIER -> PathCreationTO.Layout.ConnectionType.BEZIER;
      case BEZIER_3 -> PathCreationTO.Layout.ConnectionType.BEZIER_3;
    };
  }

  private List<CoupleCreationTO> toCoupleTOs(List<Couple> couples) {
    return couples.stream()
        .map(
            couple -> new CoupleCreationTO(
                couple.getX(),
                couple.getY()
            )
        )
        .toList();
  }

  private List<PeripheralOperationCreationTO> getPeripheralOperations(PathModel path) {
    return path.getPropertyPeripheralOperations().getValue().stream()
        .map(
            model -> new PeripheralOperationCreationTO(
                model.getOperation(), model.getLocationName()
            )
                .withExecutionTrigger(toExecutionTriggerCreationTO(model.getExecutionTrigger()))
                .withCompletionRequired(model.isCompletionRequired())
        )
        .collect(Collectors.toList());
  }

  private PeripheralOperationCreationTO.ExecutionTrigger toExecutionTriggerCreationTO(
      PeripheralOperation.ExecutionTrigger executionTrigger
  ) {
    return switch (executionTrigger) {
      case IMMEDIATE -> PeripheralOperationCreationTO.ExecutionTrigger.IMMEDIATE;
      case AFTER_ALLOCATION -> PeripheralOperationCreationTO.ExecutionTrigger.AFTER_ALLOCATION;
      case AFTER_MOVEMENT -> PeripheralOperationCreationTO.ExecutionTrigger.AFTER_MOVEMENT;
    };
  }
}
