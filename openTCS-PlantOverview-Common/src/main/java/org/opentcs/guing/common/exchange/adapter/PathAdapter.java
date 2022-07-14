/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.exchange.adapter;

import java.util.Arrays;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.stream.Collectors;
import org.opentcs.access.to.model.PathCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.peripherals.PeripheralOperationCreationTO;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.Couple;
import org.opentcs.data.model.Path;
import org.opentcs.data.peripherals.PeripheralOperation;
import org.opentcs.guing.base.components.layer.LayerWrapper;
import org.opentcs.guing.base.components.properties.type.LengthProperty;
import org.opentcs.guing.base.components.properties.type.SpeedProperty;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.PeripheralOperationModel;
import org.opentcs.guing.base.model.elements.LayoutModel;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.common.model.SystemModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An adapter for Path objects.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class PathAdapter
    extends AbstractProcessAdapter {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PathAdapter.class);

  @Override
  public void updateModelProperties(TCSObject<?> tcsObject,
                                    ModelComponent modelComponent,
                                    SystemModel systemModel,
                                    TCSObjectService objectService) {
    Path path = requireNonNull((Path) tcsObject, "tcsObject");
    PathModel model = (PathModel) modelComponent;

    model.getPropertyName().setText(path.getName());
    model.getPropertyStartComponent().setText(path.getSourcePoint().getName());
    model.getPropertyEndComponent().setText(path.getDestinationPoint().getName());
    model.getPropertyLength().setValueAndUnit(path.getLength(), LengthProperty.Unit.MM);
    model.getPropertyMaxVelocity().setValueAndUnit(path.getMaxVelocity(),
                                                   SpeedProperty.Unit.MM_S);
    model.getPropertyMaxReverseVelocity().setValueAndUnit(path.getMaxReverseVelocity(),
                                                          SpeedProperty.Unit.MM_S);
    model.getPropertyLocked().setValue(path.isLocked());
    for (PeripheralOperation operation : path.getPeripheralOperations()) {
      model.getPropertyPeripheralOperations().getValue().add(
          new PeripheralOperationModel(operation.getLocation().getName(),
                                       operation.getOperation(),
                                       operation.getExecutionTrigger(),
                                       operation.isCompletionRequired())
      );
    }

    updateMiscModelProperties(model, path);
    updateModelLayoutProperties(model, path, systemModel.getLayoutModel());
    model.propertiesChanged(model);
  }

  @Override
  public PlantModelCreationTO storeToPlantModel(ModelComponent modelComponent,
                                                SystemModel systemModel,
                                                PlantModelCreationTO plantModel) {
    PathModel pathModel = (PathModel) modelComponent;

    LOG.debug("Path {}: srcPoint is {}, dstPoint is {}.",
              pathModel.getName(),
              getSourcePoint(pathModel),
              getDestinationPoint(pathModel));

    PlantModelCreationTO result = plantModel
        .withPath(
            new PathCreationTO(pathModel.getName(),
                               getSourcePoint(pathModel),
                               getDestinationPoint(pathModel))
                .withLength(getLength(pathModel))
                .withMaxVelocity(getMaxVelocity(pathModel))
                .withMaxReverseVelocity(getMaxReverseVelocity(pathModel))
                .withProperties(getKernelProperties(pathModel))
                .withLocked(getLocked(pathModel))
                .withPeripheralOperations(getPeripheralOperations(pathModel))
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
    PathModel.Type result = PathModel.Type.DIRECT;

    switch (connectionType) {
      case DIRECT:
        result = PathModel.Type.DIRECT;
        break;
      case ELBOW:
        result = PathModel.Type.ELBOW;
        break;
      case SLANTED:
        result = PathModel.Type.SLANTED;
        break;
      case POLYPATH:
        result = PathModel.Type.POLYPATH;
        break;
      case BEZIER:
        result = PathModel.Type.BEZIER;
        break;
      case BEZIER_3:
        result = PathModel.Type.BEZIER_3;
        break;
      default:
        throw new IllegalArgumentException("Unhandled connection type: " + connectionType);
    }

    return result;
  }

  private boolean getLocked(PathModel model) {
    if (model.getPropertyLocked().getValue() instanceof Boolean) {
      return (boolean) model.getPropertyLocked().getValue();
    }
    return false;
  }

  private int getMaxVelocity(PathModel model) {
    return (int) Math.abs(model.getPropertyMaxVelocity()
        .getValueByUnit(SpeedProperty.Unit.MM_S));
  }

  private int getMaxReverseVelocity(PathModel model) {
    return (int) Math.abs(model.getPropertyMaxReverseVelocity()
        .getValueByUnit(SpeedProperty.Unit.MM_S));
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

  private PathCreationTO.Layout getLayout(PathModel model) {
    List<Couple> controlPoints
        = Arrays.asList(model.getPropertyPathControlPoints().getText().split(";")).stream()
            .filter(controlPointString -> !controlPointString.isEmpty())
            .map(controlPointString -> {
              String[] coordinateStrings = controlPointString.split(",");
              return new Couple(Long.parseLong(coordinateStrings[0]),
                                Long.parseLong(coordinateStrings[1]));
            })
            .collect(Collectors.toList());

    return new PathCreationTO.Layout(
        toPathConnectionType((PathModel.Type) model.getPropertyPathConnType().getValue()),
        controlPoints,
        model.getPropertyLayerWrapper().getValue().getLayer().getId()
    );
  }

  private Path.Layout.ConnectionType toPathConnectionType(PathModel.Type type) {
    Path.Layout.ConnectionType result = Path.Layout.ConnectionType.DIRECT;

    switch (type) {
      case DIRECT:
        result = Path.Layout.ConnectionType.DIRECT;
        break;
      case ELBOW:
        result = Path.Layout.ConnectionType.ELBOW;
        break;
      case SLANTED:
        result = Path.Layout.ConnectionType.SLANTED;
        break;
      case POLYPATH:
        result = Path.Layout.ConnectionType.POLYPATH;
        break;
      case BEZIER:
        result = Path.Layout.ConnectionType.BEZIER;
        break;
      case BEZIER_3:
        result = Path.Layout.ConnectionType.BEZIER_3;
        break;
      default:
        throw new IllegalArgumentException("Unhandled connection type: " + type);
    }

    return result;
  }

  private List<PeripheralOperationCreationTO> getPeripheralOperations(PathModel path) {
    return path.getPropertyPeripheralOperations().getValue().stream()
        .map(model
            -> new PeripheralOperationCreationTO(model.getOperation(), model.getLocationName())
            .withExecutionTrigger(model.getExecutionTrigger())
            .withCompletionRequired(model.isCompletionRequired())
        )
        .collect(Collectors.toList());
  }
}
