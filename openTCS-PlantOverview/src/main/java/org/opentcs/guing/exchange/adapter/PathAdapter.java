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

import com.google.inject.assistedinject.Assisted;
import java.awt.geom.Point2D;
import java.util.Map;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import net.engio.mbassy.listener.Handler;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.SharedKernelProvider;
import org.opentcs.access.to.model.ModelLayoutElementCreationTO;
import org.opentcs.access.to.model.PathCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.guing.application.ApplicationState;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.components.drawing.figures.PathConnection;
import org.opentcs.guing.components.properties.type.BooleanProperty;
import org.opentcs.guing.components.properties.type.IntegerProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.components.properties.type.SpeedProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.event.PathLockedEvent;
import org.opentcs.guing.exchange.EventDispatcher;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.AbstractConnection;
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
  /**
   * Provides access to a kernel.
   */
  private final SharedKernelProvider kernelProvider;

  /**
   * The state of the plant overview.
   */
  private final ApplicationState applicationState;

  /**
   * Creates a new instance.
   *
   * @param kernelProvider A kernel provider.
   * @param applicationState The plant overviews mode.
   * @param model The corresponding model component.
   * @param eventDispatcher The event dispatcher.
   */
  @Inject
  public PathAdapter(SharedKernelProvider kernelProvider,
                     ApplicationState applicationState,
                     @Assisted PathModel model,
                     @Assisted EventDispatcher eventDispatcher) {
    super(model, eventDispatcher);
    this.kernelProvider = Objects.requireNonNull(kernelProvider, "kernelProvider");
    this.applicationState = Objects.requireNonNull(applicationState, "applicationState");
  }

  @Handler
  public void handlePathLocked(PathLockedEvent event) {
    if (applicationState.getOperationMode() != OperationMode.OPERATING) {
      LOG.debug("Ignore PathLockedEvent because the application is not in operating mode.");
      return;
    }
    Object localKernelClient = new Object();
    PathConnection pathConnection = (PathConnection) event.getSource();
    ModelComponent modelComponent = pathConnection.getModel();
    try {
      //If the event is from the adapters model we have to update the kernel
      //else ignore it because another adapter is responsible
      if (modelComponent.equals(getModel())) {
        //Try to connect to the kernel
        kernelProvider.register(localKernelClient);
        if (kernelProvider.kernelShared()) {
          Kernel kernel = kernelProvider.getKernel();
          //Check if the kernel is in operating mode too
          boolean kernelInOperating = kernel.getState() == Kernel.State.OPERATING;
          if (kernelInOperating) {
            //Update the path in the kernel if it exists
            TCSObjectReference<Path> ref
                = kernel.getTCSObject(Path.class, modelComponent.getName())
                    .getReference();
            if (ref != null) {
              BooleanProperty locked = (BooleanProperty) modelComponent.getProperty(PathModel.LOCKED);
              kernel.setPathLocked(ref, (boolean) locked.getValue());
            }
          }
        }
      }
    }
    finally {
      kernelProvider.unregister(localKernelClient);
    }
  }

  @Override
  public PathModel getModel() {
    return (PathModel) super.getModel();
  }

  @Override // OpenTCSProcessAdapter
  public void updateModelProperties(Kernel kernel,
                                    TCSObject<?> tcsObject,
                                    @Nullable ModelLayoutElement layoutElement) {
    Path path = requireNonNull((Path) tcsObject, "tcsObject");

    try {
      // NAME: Name of the path
      StringProperty pName
          = (StringProperty) getModel().getProperty(ModelComponent.NAME);
      pName.setText(path.getName());
      // LENGTH: Length in [mm]
      LengthProperty pLength
          = (LengthProperty) getModel().getProperty(PathModel.LENGTH);
      pLength.setValueAndUnit(path.getLength(), LengthProperty.Unit.MM);
      // ROUTING_COST:
      IntegerProperty pCost
          = (IntegerProperty) getModel().getProperty(PathModel.ROUTING_COST);
      pCost.setValue((int) path.getRoutingCost());
      // MAX_VELOCITY: Maximum forward speed in [m/s]
      SpeedProperty pSpeed
          = (SpeedProperty) getModel().getProperty(PathModel.MAX_VELOCITY);
      pSpeed.setValueAndUnit(path.getMaxVelocity() * 0.001,
                             SpeedProperty.Unit.M_S);
      // MAX_REVERSE_VELOCITY: Maximum backward speed in [m/s]
      pSpeed = (SpeedProperty) getModel().getProperty(
          PathModel.MAX_REVERSE_VELOCITY);
      pSpeed.setValueAndUnit(path.getMaxReverseVelocity() * 0.001,
                             SpeedProperty.Unit.M_S);
      // LOCKED: Is the path locked?
      BooleanProperty pLocked
          = (BooleanProperty) getModel().getProperty(PathModel.LOCKED);
      pLocked.setValue(path.isLocked());
      // MISCELLANEOUS:
      updateMiscModelProperties(path);
      if (layoutElement != null) {
        updateModelLayoutProperties(layoutElement);
      }
    }
    catch (CredentialsException e) {
      LOG.warn("", e);
    }
  }

  @Override // OpenTCSProcessAdapter
  public void storeToPlantModel(PlantModelCreationTO plantModel) {
    ModelComponent srcPoint = getModel().getStartComponent();
    ModelComponent dstPoint = getModel().getEndComponent();

    LOG.debug("Path {}: srcPoint is {}, dstPoint is {}.", getModel().getName(), srcPoint, dstPoint);

    try {
      plantModel.getPaths().add(
          new PathCreationTO(getModel().getName(), srcPoint.getName(), dstPoint.getName())
              .setLength(getLength())
              .setMaxVelocity(getMaxVelocity())
              .setMaxReverseVelocity(getMaxReverseVelocity())
              .setRoutingCost(getRoutingCost())
              .setProperties(getKernelProperties())
              .setLocked(getLocked()));

      // Write liner type and position of the control points into the model layout element
      for (VisualLayoutCreationTO layout : plantModel.getVisualLayouts()) {
        updateLayoutElement(layout);
      }

      unmarkAllPropertiesChanged();
    }
    catch (KernelRuntimeException e) {
      LOG.warn("", e);
    }
  }

  private void updateModelLayoutProperties(ModelLayoutElement layoutElement) {
    Map<String, String> properties = layoutElement.getProperties();

    // PATH_CONN_TYPE: DIRECT, BEZIER, ...
    SelectionProperty pConnectionType = (SelectionProperty) getModel()
        .getProperty(ElementPropKeys.PATH_CONN_TYPE);
    String sConnectionType = properties.get(ElementPropKeys.PATH_CONN_TYPE);

    if (sConnectionType != null) {
      pConnectionType.setValue(sConnectionType);
    }

    // PATH_CONTROL_POINTS: Only when PATH_CONN_TYPE BEZIER
    StringProperty pControlPoints = (StringProperty) getModel().getProperty(
        ElementPropKeys.PATH_CONTROL_POINTS);
    String sControlPoints = properties.get(ElementPropKeys.PATH_CONTROL_POINTS);

    if (sControlPoints != null) {
      pControlPoints.setText(sControlPoints);
    }
  }

  private boolean getLocked() {
    BooleanProperty pLocked = (BooleanProperty) getModel().getProperty(PathModel.LOCKED);

    if (pLocked.getValue() instanceof Boolean) {
      return (boolean) pLocked.getValue();
    }
    return false;
  }

  private int getMaxVelocity() {
    SpeedProperty pSpeed = (SpeedProperty) getModel().getProperty(PathModel.MAX_VELOCITY);
    return (int) Math.abs(pSpeed.getValueByUnit(SpeedProperty.Unit.MM_S));
  }

  private int getMaxReverseVelocity() {
    SpeedProperty pSpeed = (SpeedProperty) getModel().getProperty(PathModel.MAX_REVERSE_VELOCITY);
    return (int) Math.abs(pSpeed.getValueByUnit(SpeedProperty.Unit.MM_S));
  }

  private int getRoutingCost() {
    IntegerProperty pCost = (IntegerProperty) getModel().getProperty(PathModel.ROUTING_COST);
    return (int) pCost.getValue();
  }

  private long getLength() {
    LengthProperty pLength = (LengthProperty) getModel().getProperty(PathModel.LENGTH);

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

  /**
   * Refreshes the properties of the layout element and saves it in the kernel.
   *
   * @param layout The VisualLayout.
   */
  private void updateLayoutElement(VisualLayoutCreationTO layout) {
    AbstractConnection model = (AbstractConnection) getModel();
    // Connection type
    SelectionProperty pType = (SelectionProperty) model.getProperty(ElementPropKeys.PATH_CONN_TYPE);
    PathModel.LinerType type = (PathModel.LinerType) pType.getValue();

    // BEZIER control points
    String sControlPoints = "";
    if (type.equals(PathModel.LinerType.BEZIER) || type.equals(PathModel.LinerType.BEZIER_3)) {
      sControlPoints = buildBezierControlPoints();
    }

    StringProperty pControlPoints
        = (StringProperty) model.getProperty(ElementPropKeys.PATH_CONTROL_POINTS);
    pControlPoints.setText(sControlPoints);

    layout.getModelElements().add(
        new ModelLayoutElementCreationTO(model.getName())
            .setProperty(ElementPropKeys.PATH_CONN_TYPE, type.name())
            .setProperty(ElementPropKeys.PATH_CONTROL_POINTS, sControlPoints)
    );
  }

  private String buildBezierControlPoints() {
    String result = "";
    PathConnection figure = (PathConnection) getModel().getFigure();
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
