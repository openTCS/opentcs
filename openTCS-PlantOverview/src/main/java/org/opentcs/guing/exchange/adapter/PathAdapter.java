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

import com.google.common.collect.Iterables;
import com.google.inject.assistedinject.Assisted;
import java.awt.geom.Point2D;
import java.util.Map;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;
import net.engio.mbassy.listener.Handler;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.SharedKernelProvider;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.LayoutElement;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.model.visualization.VisualLayout;
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
   * Creates a new instance.
   *
   * @param kernelProvider A kernel provider.
   * @param model The corresponding model component.
   * @param eventDispatcher The event dispatcher.
   */
  @Inject
  public PathAdapter(SharedKernelProvider kernelProvider,
                     @Assisted PathModel model,
                     @Assisted EventDispatcher eventDispatcher) {
    super(model, eventDispatcher);
    this.kernelProvider = Objects.requireNonNull(kernelProvider, "kernelProvider");
  }

  @Handler
  public void handlePathLocked(PathLockedEvent event) {
    Object localKernelClient = new Object();
    try {
      kernelProvider.register(localKernelClient);
      if (kernelProvider.kernelShared()) {
        Kernel kernel = kernelProvider.getKernel();
        boolean kernelInOperating = kernel.getState() == Kernel.State.OPERATING;
        if (kernelInOperating) {
          PathConnection pathConnection = (PathConnection) event.getSource();
          ModelComponent modelComponent = pathConnection.getModel();
          if (modelComponent.equals(getModel())) {
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
  public void updateProcessProperties(Kernel kernel) {
    requireNonNull(kernel, "kernel");
    ModelComponent srcPoint = getModel().getStartComponent();
    ModelComponent dstPoint = getModel().getEndComponent();
    
    LOG.debug("Path {}: srcPoint is {}, dstPoint is {}.", getModel().getName(), srcPoint, dstPoint);

    Point startPoint = kernel.getTCSObject(Point.class, srcPoint.getName());
    if (startPoint == null) {
      LOG.warn("Start point with name {} does not exist in kernel, ignored.", srcPoint.getName());
      return;
    }
    Point endPoint = kernel.getTCSObject(Point.class, dstPoint.getName());
    if (endPoint == null) {
      LOG.warn("End point with name {} does not exist in kernel, ignored.", dstPoint.getName());
      return;
    }
    Path path = kernel.createPath(startPoint.getReference(), endPoint.getReference());
    TCSObjectReference<Path> reference = path.getReference();

    // The kernel object will be created when the points to connect are
    // known. Before the reference is null.
    StringProperty pName = (StringProperty) getModel().getProperty(ModelComponent.NAME);
    String name = pName.getText();

    try {
      // NAME
      kernel.renameTCSObject(reference, name);

      updateProcessLength(kernel, reference);
      updateProcessVelocity(kernel, reference);
      updateProcessRoutingCost(kernel, reference);

      // Write liner type and position of the control points into
      // the model layout element
      Set<VisualLayout> layouts = kernel.getTCSObjects(VisualLayout.class);

      for (VisualLayout layout : layouts) {
        updateLayoutElement(kernel, layout, reference);
      }

      // MISCELLANEOUS
      updateMiscProcessProperties(kernel, reference);
      updateProcessLocked(kernel, reference);
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
    // PATH_ARROW_POSITION
    // HH 2014-02-14: Verschieben der Pfeilspitze ist in der Figure noch nicht
    // implementiert, daher dieses Property auch nicht auswerten
//          PercentProperty pArrowPosition = (PercentProperty) getModel().getProperty(ElementPropKeys.PATH_ARROW_POSITION);
//          String sArrowPosition = properties.get(ElementPropKeys.PATH_ARROW_POSITION);
//
//          if (sArrowPosition != null) {
//            pArrowPosition.setValueAndUnit(Integer.parseInt(sArrowPosition), "%");
//          }
  }

  private void updateProcessLocked(Kernel kernel,
                                   TCSObjectReference<Path> reference)
      throws KernelRuntimeException {
    // LOCKED: This state is allowed to be changed in OPERATING mode
    BooleanProperty pLocked
        = (BooleanProperty) getModel().getProperty(PathModel.LOCKED);

    if (pLocked.getValue() instanceof Boolean) {
      kernel.setPathLocked(reference, (boolean) pLocked.getValue());
    }
  }

  private void updateProcessVelocity(Kernel kernel,
                                     TCSObjectReference<Path> reference)
      throws KernelRuntimeException, IllegalArgumentException {
    SpeedProperty pSpeed
        = (SpeedProperty) getModel().getProperty(PathModel.MAX_VELOCITY);
    int speed = (int) Math.abs(pSpeed.getValueByUnit(SpeedProperty.Unit.MM_S));

    kernel.setPathMaxVelocity(reference, speed);

    // MAX_REVERSE_VELOCITY - must not be negative!
    pSpeed
        = (SpeedProperty) getModel().getProperty(PathModel.MAX_REVERSE_VELOCITY);
    speed = (int) Math.abs(pSpeed.getValueByUnit(SpeedProperty.Unit.MM_S));

    kernel.setPathMaxReverseVelocity(reference, speed);
  }

  private void updateProcessRoutingCost(Kernel kernel,
                                        TCSObjectReference<Path> reference)
      throws IllegalArgumentException, KernelRuntimeException {
    IntegerProperty pCost
        = (IntegerProperty) getModel().getProperty(PathModel.ROUTING_COST);

    kernel.setPathRoutingCost(reference, (int) pCost.getValue());
  }

  private void updateProcessLength(Kernel kernel,
                                   TCSObjectReference<Path> reference)
      throws IllegalArgumentException, KernelRuntimeException {
    LengthProperty pLength
        = (LengthProperty) getModel().getProperty(PathModel.LENGTH);

    if ((double) pLength.getValue() <= 0) {
      try {
        pLength.setValueAndUnit(1.0, pLength.getUnit());
        pLength.markChanged();
      }
      catch (IllegalArgumentException ex) {
        LOG.warn("", ex);
      }
    }

    kernel.setPathLength(reference,
                         (long) pLength.getValueByUnit(LengthProperty.Unit.MM));
  }

  /**
   * Refreshes the properties of the layout element and saves it in the kernel.
   *
   * @param layout The VisualLayout.
   */
  private void updateLayoutElement(Kernel kernel,
                                   VisualLayout layout,
                                   TCSObjectReference<?> ref) {

    ModelLayoutElement layoutElement = new ModelLayoutElement(ref);
    Map<String, String> layoutProperties = layoutElement.getProperties();

    AbstractConnection model = (AbstractConnection) getModel();
    // Connection type
    SelectionProperty pType
        = (SelectionProperty) model.getProperty(ElementPropKeys.PATH_CONN_TYPE);
    PathModel.LinerType type = (PathModel.LinerType) pType.getValue();
    layoutProperties.put(ElementPropKeys.PATH_CONN_TYPE, type.name());

    // BEZIER control points
    String sControlPoints = "";

    if (type.equals(PathModel.LinerType.BEZIER)) {
      sControlPoints
          = buildBezierControlPoints(model, sControlPoints, layoutProperties);
    }
    else {
      layoutProperties.remove(ElementPropKeys.PATH_CONTROL_POINTS);
    }

    StringProperty pControlPoints = (StringProperty) model.getProperty(
        ElementPropKeys.PATH_CONTROL_POINTS);
    pControlPoints.setText(sControlPoints);

    layoutElement.setProperties(layoutProperties);

    Set<LayoutElement> layoutElements = layout.getLayoutElements();
    Iterables.removeIf(layoutElements, layoutElementFor(ref));
    layoutElements.add(layoutElement);

    kernel.setVisualLayoutElements(layout.getReference(), layoutElements);
  }

  private String buildBezierControlPoints(AbstractConnection model,
                                          String sControlPoints,
                                          Map<String, String> layoutProperties) {
    PathConnection figure = (PathConnection) model.getFigure();
    Point2D.Double cp1 = figure.getCp1();
    if (cp1 != null) {
      Point2D.Double cp2 = figure.getCp2();

      if (cp2 != null) {
        // Format: x1,y1;x2,y2
        sControlPoints = String.format("%d,%d;%d,%d", (int) (cp1.x),
                                       (int) (cp1.y), (int) (cp2.x),
                                       (int) (cp2.y));
      }
      else {
        // Format: x1,y1
        sControlPoints = String.format("%d,%d", (int) (cp1.x), (int) (cp1.y));
      }

      layoutProperties.put(ElementPropKeys.PATH_CONTROL_POINTS, sControlPoints);
    }
    return sControlPoints;
  }
}
