/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange.adapter;

import java.awt.geom.Point2D;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nullable;
import org.opentcs.access.to.model.ModelLayoutElementCreationTO;
import org.opentcs.access.to.model.PathCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.guing.components.drawing.figures.PathConnection;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.SpeedProperty;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.PathModel;
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
  @SuppressWarnings("deprecation")
  public void updateModelProperties(TCSObject<?> tcsObject,
                                    ModelComponent modelComponent,
                                    SystemModel systemModel,
                                    TCSObjectService objectService,
                                    @Nullable ModelLayoutElement layoutElement) {
    Path path = requireNonNull((Path) tcsObject, "tcsObject");
    PathModel model = (PathModel) modelComponent;

    model.getPropertyName().setText(path.getName());
    model.getPropertyStartComponent().setText(path.getSourcePoint().getName());
    model.getPropertyEndComponent().setText(path.getDestinationPoint().getName());
    model.getPropertyLength().setValueAndUnit(path.getLength(), LengthProperty.Unit.MM);
    model.getPropertyRoutingCost().setValue((int) path.getRoutingCost());
    model.getPropertyMaxVelocity().setValueAndUnit(path.getMaxVelocity(),
                                                   SpeedProperty.Unit.MM_S);
    model.getPropertyMaxReverseVelocity().setValueAndUnit(path.getMaxReverseVelocity(),
                                                          SpeedProperty.Unit.MM_S);
    model.getPropertyLocked().setValue(path.isLocked());
    updateMiscModelProperties(model, path);
    if (layoutElement != null) {
      updateModelLayoutProperties(model, layoutElement);
    }
    model.propertiesChanged(model);
  }

  @Override
  @SuppressWarnings("deprecation")
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
                .withRoutingCost(getRoutingCost(pathModel))
                .withProperties(getKernelProperties(pathModel))
                .withLocked(getLocked(pathModel))
        )
        .withVisualLayouts(updatedLayouts(pathModel, plantModel.getVisualLayouts(), systemModel));

    unmarkAllPropertiesChanged(pathModel);

    return result;
  }

  private void updateModelLayoutProperties(PathModel model, ModelLayoutElement layoutElement) {
    Map<String, String> properties = layoutElement.getProperties();

    // PATH_CONN_TYPE: DIRECT, BEZIER, ...
    String sConnectionType = properties.get(ElementPropKeys.PATH_CONN_TYPE);
    if (sConnectionType != null) {
      model.getPropertyPathConnType()
          .setValue(PathModel.Type.valueOfNormalized(sConnectionType));
    }

    // PATH_CONTROL_POINTS: Only when PATH_CONN_TYPE BEZIER
    String sControlPoints = properties.get(ElementPropKeys.PATH_CONTROL_POINTS);
    if (sControlPoints != null) {
      model.getPropertyPathControlPoints().setText(sControlPoints);
    }
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

  private int getRoutingCost(PathModel model) {
    return (int) model.getPropertyRoutingCost().getValue();
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

  @Override
  protected VisualLayoutCreationTO updatedLayout(ModelComponent model,
                                                 VisualLayoutCreationTO layout,
                                                 SystemModel systemModel) {
    PathModel pathModel = (PathModel) model;
    // Connection type
    PathModel.Type type = (PathModel.Type) pathModel.getPropertyPathConnType().getValue();

    // BEZIER control points
    String sControlPoints = "";
    if (type.equals(PathModel.Type.BEZIER) || type.equals(PathModel.Type.BEZIER_3)) {
      sControlPoints = buildBezierControlPoints(pathModel, systemModel);
    }
    else if( type.equals(PathModel.Type.POLYPATH)){
      sControlPoints = buildPolyPathControlPoints(pathModel, systemModel);
    }
    

    pathModel.getPropertyPathControlPoints().setText(sControlPoints);

    ModelLayoutElementCreationTO modelLayoutElement
        = new ModelLayoutElementCreationTO(pathModel.getName())
            .withProperty(ElementPropKeys.PATH_CONN_TYPE, type.name());
    if (!sControlPoints.isEmpty()) {
      modelLayoutElement = modelLayoutElement
          .withProperty(ElementPropKeys.PATH_CONTROL_POINTS, sControlPoints);
    }
    return layout.withModelElement(modelLayoutElement);
  }
  
  private String buildPolyPathControlPoints(PathModel model, SystemModel systemModel){
    PathConnection figure = (PathConnection) systemModel.getFigure(model);
    return figure.getModel().getPropertyPathControlPoints().getText();
  }

  private String buildBezierControlPoints(PathModel model, SystemModel systemModel) {
    String result = "";
    PathConnection figure = (PathConnection) systemModel.getFigure(model);
    Point2D.Double cp1 = figure.getCp1();
    if (cp1 != null) {
      Point2D.Double cp2 = figure.getCp2();
      if (cp2 != null) {
        Point2D.Double cp3 = figure.getCp3();
        Point2D.Double cp4 = figure.getCp4();
        Point2D.Double cp5 = figure.getCp5();
        if (cp3 != null && cp4 != null && cp5 != null) {
          // Format: x1,y1;x2,y2;x3,y3;x4,y4;x5,y5
          result = String.format("%d,%d;%d,%d;%d,%d;%d,%d;%d,%d",
                                 (int) (cp1.x),
                                 (int) (cp1.y),
                                 (int) (cp2.x),
                                 (int) (cp2.y),
                                 (int) (cp3.x),
                                 (int) (cp3.y),
                                 (int) (cp4.x),
                                 (int) (cp4.y),
                                 (int) (cp5.x),
                                 (int) (cp5.y));
        }
        else {
          // Format: x1,y1;x2,y2
          result = String.format("%d,%d;%d,%d", (int) (cp1.x),
                                 (int) (cp1.y), (int) (cp2.x),
                                 (int) (cp2.y));
        }
      }
      else {
        // Format: x1,y1
        result = String.format("%d,%d", (int) (cp1.x), (int) (cp1.y));
      }
    }
    return result;
  }
}
