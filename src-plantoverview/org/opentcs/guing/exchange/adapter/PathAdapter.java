/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.exchange.adapter;

import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.LayoutElement;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.guing.components.drawing.figures.PathConnection;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.type.BooleanProperty;
import org.opentcs.guing.components.properties.type.IntegerProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.components.properties.type.SpeedProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.event.ConnectionChangeEvent;
import org.opentcs.guing.event.ConnectionChangeListener;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.AbstractConnection;
import org.opentcs.guing.model.elements.PathModel;

/**
 * An adapter for Path objects.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class PathAdapter
    extends OpenTCSProcessAdapter
    implements ConnectionChangeListener {

  /**
   * This class's logger.
   */
  private static final Logger logger = Logger.getLogger(PathAdapter.class.getName());

  /**
   * Creates a new instance of PathAdapter.
   */
  public PathAdapter() {
    super();
  }

  @Override
  @SuppressWarnings("unchecked")
  public TCSObjectReference<Path> getProcessObject() {
    return (TCSObjectReference<Path>) super.getProcessObject();
  }

  @Override
  public PathModel getModel() {
    return (PathModel) super.getModel();
  }

  @Override
  public void setModel(ModelComponent model) {
    if (!PathModel.class.isInstance(model)) {
      throw new IllegalArgumentException(model + " is not a PathModel");
    }
    super.setModel(model);
  }

  @Override // AbstractProcessAdapter
  public void register() {
    super.register();
    getModel().addConnectionChangeListener(this);
  }

  @Override // AbstractProcessAdapter
  public void releaseProcessObject() {
    releaseLayoutElement();
    removePath();
    getModel().removeConnectionChangeListener(this);
    super.releaseProcessObject();
  }

  @Override // AbstractProcessAdapter
  public Object createProcessObject() {
    register();

    return null;
  }

  @Override // OpenTCSProcessAdapter
  public void propertiesChanged(AttributesChangeEvent event) {
    if (event.getInitiator() != this) {
      updateProcessProperties(false);
    }
  }

  @Override // OpenTCSProcessAdapter
  public void updateModelProperties() {
    TCSObjectReference<Path> reference = getProcessObject();

    if (reference != null) {
      synchronized (reference) {
        try {
          Path path = kernel().getTCSObject(Path.class, reference);
          // NAME: Name of the path
          StringProperty pName = (StringProperty) getModel().getProperty(ModelComponent.NAME);
          pName.setText(path.getName());
          // LENGTH: Length in [mm]
          LengthProperty pLength = (LengthProperty) getModel().getProperty(PathModel.LENGTH);
          pLength.setValueAndUnit(path.getLength(), LengthProperty.Unit.MM);
          // ROUTING_COST:
          IntegerProperty pCost = (IntegerProperty) getModel().getProperty(PathModel.ROUTING_COST);
          pCost.setValue((int) path.getRoutingCost());
          // MAX_VELOCITY: Maximum forward speed in [m/s]
          SpeedProperty pSpeed = (SpeedProperty) getModel().getProperty(PathModel.MAX_VELOCITY);
          pSpeed.setValueAndUnit(path.getMaxVelocity() * 0.001, SpeedProperty.Unit.M_S);
          // MAX_REVERSE_VELOCITY: Maximum backward speed in [m/s]
          pSpeed = (SpeedProperty) getModel().getProperty(PathModel.MAX_REVERSE_VELOCITY);
          pSpeed.setValueAndUnit(path.getMaxReverseVelocity() * 0.001, SpeedProperty.Unit.M_S);
          // LOCKED: Is the path locked?
          BooleanProperty pLocked = (BooleanProperty) getModel().getProperty(PathModel.LOCKED);
          pLocked.setValue(path.isLocked());
          // MISCELLANEOUS:
          updateMiscModelProperties(path);
          updateModelLayoutProperties();
        }
        catch (CredentialsException e) {
          logger.log(Level.WARNING, null, e);
        }
      }
    }
  }

  private void updateModelLayoutProperties() {
    if (fLayoutElement != null) {
      Map<String, String> properties = fLayoutElement.getProperties();
      // PATH_CONN_TYPE: DIRECT, BEZIER, ...
      SelectionProperty pConnectionType = (SelectionProperty) getModel().getProperty(ElementPropKeys.PATH_CONN_TYPE);
      String sConnectionType = properties.get(ElementPropKeys.PATH_CONN_TYPE);

      if (sConnectionType != null) {
        pConnectionType.setValue(sConnectionType);
      }
      // PATH_CONTROL_POINTS: Only when PATH_CONN_TYPE BEZIER
      StringProperty pControlPoints = (StringProperty) getModel().getProperty(ElementPropKeys.PATH_CONTROL_POINTS);
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
  }

  @Override // OpenTCSProcessAdapter
  public void updateProcessProperties(boolean updateAllProperties) {
    super.updateProcessProperties(updateAllProperties);
    TCSObjectReference<Path> reference = getProcessObject();

    if (isInTransition()) {
      return;
    }

    if (reference != null) {
      synchronized (reference) {
        // The kernel object will be created when the points to connect are
        // known. Before the reference is null.
        StringProperty pName = (StringProperty) getModel().getProperty(ModelComponent.NAME);
        String name = pName.getText();

        try {
          if (hasModelingState()) {
            // NAME
            if (updateAllProperties || pName.hasChanged()) {
              kernel().renameTCSObject(reference, name);
            }

            updateProcessLength(updateAllProperties, reference);
            updateProcessVelocity(updateAllProperties, reference);
            updateProcessRoutingCost(updateAllProperties, reference);

            // Write liner type and position of the control points into
            // the model layout element
            Set<VisualLayout> layouts = kernel().getTCSObjects(VisualLayout.class);

            for (VisualLayout layout : layouts) {
              updateLayoutElement(layout);
            }

            // MISCELLANEOUS
            updateMiscProcessProperties(updateAllProperties);
          }
          updateProcessLocked(updateAllProperties, reference);
        }
        catch (ObjectExistsException e) {
          undo(name, e);
        }
        catch (CredentialsException | ObjectUnknownException e) {
          logger.log(Level.WARNING, null, e);
        }
      }
    }
  }

  private void updateProcessLocked(boolean updateAllProperties,
                                   TCSObjectReference<Path> reference)
      throws ObjectUnknownException, CredentialsException {
    // LOCKED: This state is allowed to be changed in OPERATING mode
    BooleanProperty pLocked = (BooleanProperty) getModel().getProperty(PathModel.LOCKED);

    if (updateAllProperties || pLocked.hasChanged()) {
      if (pLocked.getValue() instanceof Boolean) {
        kernel().setPathLocked(reference, (boolean) pLocked.getValue());
      }
    }
  }

  private void updateProcessVelocity(boolean updateAllProperties,
                                     TCSObjectReference<Path> reference)
      throws ObjectUnknownException, CredentialsException, IllegalArgumentException {
    SpeedProperty pSpeed = (SpeedProperty) getModel().getProperty(PathModel.MAX_VELOCITY);
    int speed = (int) Math.abs(pSpeed.getValueByUnit(SpeedProperty.Unit.MM_S));

    if (updateAllProperties || pSpeed.hasChanged()) {
      kernel().setPathMaxVelocity(reference, speed);
    }

    // MAX_REVERSE_VELOCITY - must not be negative!
    pSpeed = (SpeedProperty) getModel().getProperty(PathModel.MAX_REVERSE_VELOCITY);
    speed = (int) Math.abs(pSpeed.getValueByUnit(SpeedProperty.Unit.MM_S));

    if (updateAllProperties || pSpeed.hasChanged()) {
      kernel().setPathMaxReverseVelocity(reference, speed);
    }
  }

  private void updateProcessRoutingCost(boolean updateAllProperties,
                                        TCSObjectReference<Path> reference)
      throws IllegalArgumentException, CredentialsException, ObjectUnknownException {
    IntegerProperty pCost = (IntegerProperty) getModel().getProperty(PathModel.ROUTING_COST);
    int value = (int) pCost.getValue();
    if (value <= 0) {
        // Costs less than 1 are invalid and as IntegerProperty isn't
      // an AbstractQuantity we can't use the valid range parameter to
      // check for correct values. In this case set the value to the kernel value
      updateModelProperties();
      return;
    }

    if ((updateAllProperties || pCost.hasChanged()) && pCost.getValue() instanceof Integer) {
      kernel().setPathRoutingCost(reference, (int) pCost.getValue());
    }
  }

  private void updateProcessLength(boolean updateAllProperties,
                                   TCSObjectReference<Path> reference)
      throws IllegalArgumentException, CredentialsException, ObjectUnknownException {
    LengthProperty pLength = (LengthProperty) getModel().getProperty(PathModel.LENGTH);

    if (updateAllProperties || pLength.hasChanged()) {
      if ((double) pLength.getValue() <= 0) {
        try {
          pLength.setValueAndUnit(1.0, pLength.getUnit());
          pLength.markChanged();
        }
        catch (IllegalArgumentException ex) {
          logger.log(Level.WARNING, null, ex);
        }
      }

      kernel().setPathLength(reference, (long) pLength.getValueByUnit(LengthProperty.Unit.MM));
    }
  }

  /**
   * Refreshes the properties of the layout element and saves it in the kernel.
   *
   * @param layout The VisualLayout.
   */
  private void updateLayoutElement(VisualLayout layout) {
    // Beim ersten Aufruf ein neues Model-Layout-Element erzeugen
    if (fLayoutElement == null) {
      fLayoutElement = new ModelLayoutElement(getProcessObject());
    }

    AbstractConnection model = (AbstractConnection) getModel();
    // Connection type
    SelectionProperty pType = (SelectionProperty) model.getProperty(ElementPropKeys.PATH_CONN_TYPE);
    PathModel.LinerType type = (PathModel.LinerType) pType.getValue();
    Map<String, String> layoutProperties = fLayoutElement.getProperties();
    layoutProperties.put(ElementPropKeys.PATH_CONN_TYPE, type.name());
    // BEZIER control points
    String sControlPoints = "";

    if (type.equals(PathModel.LinerType.BEZIER)) {
      sControlPoints = buildBezierControlPoints(model, sControlPoints, layoutProperties);
    }
    else {
      layoutProperties.remove(ElementPropKeys.PATH_CONTROL_POINTS);
    }

    StringProperty pControlPoints = (StringProperty) model.getProperty(ElementPropKeys.PATH_CONTROL_POINTS);
    pControlPoints.setText(sControlPoints);

    fLayoutElement.setProperties(layoutProperties);

    Set<LayoutElement> layoutElements = layout.getLayoutElements();
    Iterator<LayoutElement> iElements = layoutElements.iterator();

    while (iElements.hasNext()) {
      ModelLayoutElement element = (ModelLayoutElement) iElements.next();
      TCSObjectReference<?> visualizedObject = element.getVisualizedObject();

      if (visualizedObject.getId() == fLayoutElement.getVisualizedObject().getId()) {
        layoutElements.remove(element);
        break;
      }
    }

    layoutElements.add(fLayoutElement);
    kernel().setVisualLayoutElements(layout.getReference(), layoutElements);
  }

  private String buildBezierControlPoints(AbstractConnection model, String sControlPoints, Map<String, String> layoutProperties) {
    PathConnection figure = (PathConnection) model.getFigure();
    Point2D.Double cp1 = figure.getCp1();
    if (cp1 != null) {
      Point2D.Double cp2 = figure.getCp2();

      if (cp2 != null) {
        // Format: x1,y1;x2,y2
        sControlPoints = String.format("%d,%d;%d,%d", (int) (cp1.x), (int) (cp1.y), (int) (cp2.x), (int) (cp2.y));
      }
      else {
        // Format: x1,y1
        sControlPoints = String.format("%d,%d", (int) (cp1.x), (int) (cp1.y));
      }

      layoutProperties.put(ElementPropKeys.PATH_CONTROL_POINTS, sControlPoints);
    }
    return sControlPoints;
  }

  @Override // ConnectionChangeListener
  public void connectionChanged(ConnectionChangeEvent evt) {
    removePath();
    establishPath();
  }

  /**
   * Removes the path from the kernel.
   */
  private void removePath() {
    if (getProcessObject() == null) {
      return;
    }

    try {
      kernel().removeTCSObject(getProcessObject());
    }
    catch (KernelRuntimeException e) {
      logger.log(Level.WARNING, null, e);
    }
    setProcessObject(null);
  }

  /**
   * Establishes the path.
   */
  private void establishPath() {
    AbstractConnection connection = (AbstractConnection) getModel();
    ModelComponent sourcePoint = connection.getStartComponent();
    ModelComponent destinationPoint = connection.getEndComponent();

    if (sourcePoint != null && destinationPoint != null) {
      PointAdapter startAdapter = (PointAdapter) getEventDispatcher().findProcessAdapter(sourcePoint);
      PointAdapter endAdapter = (PointAdapter) getEventDispatcher().findProcessAdapter(destinationPoint);

      if (startAdapter != null && endAdapter != null) {
        TCSObjectReference<Point> startRef = startAdapter.getProcessObject();
        TCSObjectReference<Point> endRef = endAdapter.getProcessObject();

        try {
          Path path = kernel().createPath(startRef, endRef);
          setProcessObject(path.getReference());
          getEventDispatcher().addProcessAdapter(this);
          // if an "old" object was restored by undo() save its properties
          // in the kernel
          updateProcessProperties(true);
        }
        catch (ObjectUnknownException | CredentialsException e) {
          logger.log(Level.WARNING, null, e);
        }
      }
    }
  }
}
