/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.exchange.adapter;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Point;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.type.StringSetProperty;
import org.opentcs.guing.event.ConnectionChangeEvent;
import org.opentcs.guing.event.ConnectionChangeListener;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.PointModel;

/**
 * An adapter for <code>Links</code>.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class LinkAdapter
    extends OpenTCSProcessAdapter
    implements ConnectionChangeListener {

  /**
   * This class's logger.
   */
  private static final Logger log = Logger.getLogger(LinkAdapter.class.getName());

  /**
   * The point the link is connected to.
   */
  private TCSObjectReference<Point> fPoint;
  /**
   * The location that is connected with the point by this link.
   */
  private TCSObjectReference<Location> fLocation;

  /**
   * Creates a new instance of LinkAdapter.
   */
  public LinkAdapter() {
    super();
  }

  @Override
  public LinkModel getModel() {
    return (LinkModel) super.getModel();
  }

  @Override
  public void setModel(ModelComponent model) {
    if (!LinkModel.class.isInstance(model)) {
      throw new IllegalArgumentException(model + " is not a LinkModel");
    }
    super.setModel(model);
  }

  /**
   * Sets the objects that are connected by the link.
   *
   * @param point The point to connect.
   * @param location The location to connect.
   */
  public void setConnectedProcessObjects(TCSObjectReference<Point> point,
                                         TCSObjectReference<Location> location) {
    fPoint = point;
    fLocation = location;
  }

  @Override // AbstractProcessAdapter
  public void register() {
    super.register();
    getModel().addConnectionChangeListener(this);
  }

  @Override // AbstractProcessAdapter
  public void releaseProcessObject() {
    getModel().removeConnectionChangeListener(this);
    removeLink();
    super.releaseProcessObject();
  }

  @Override // AbstractProcessAdapter
  public Object createProcessObject() {
    register();
    // To a link there is no object in the kernel.

    return null;
  }

  @Override // OpenTCSProcessAdapter
  public void propertiesChanged(AttributesChangeEvent event) {
    if (hasModelingState() && event.getInitiator() != this) {
      updateProcessProperties(false);
    }
  }

  @Override // OpenTCSProcessAdapter
  public void updateModelProperties() {
    // There is no object in the kernel for a link.
  }

  @Override // OpenTCSProcessAdapter
  public void updateProcessProperties(boolean updateAllProperties) {
    super.updateProcessProperties(updateAllProperties);
    if (fLocation == null || fPoint == null) {
      establishLink();
    }

    StringSetProperty pOperations = (StringSetProperty) getModel().getProperty(LinkModel.ALLOWED_OPERATIONS);

    if (updateAllProperties || pOperations.hasChanged()) {
      // Delete known action
      try {
        kernel().clearLocationLinkAllowedOperations(fLocation, fPoint);
        // Set current actions
        for (String operations : pOperations.getItems()) {
          kernel().addLocationLinkAllowedOperation(fLocation, fPoint, operations);
        }
      }
      catch (ObjectUnknownException | CredentialsException e) {
        log.log(Level.WARNING, null, e);
      }
    }
  }

  @Override  // ConnectionChangeListener
  public void connectionChanged(ConnectionChangeEvent e) {
    if (hasModelingState()) {
      removeLink();
      establishLink();
    }
  }

  /**
   * Establishes the link.
   */
  private void establishLink() {
    PointModel point = getModel().getPoint();
    LocationModel location = getModel().getLocation();
    if (point == null || location == null) {
      return;
    }
    ProcessAdapter pointAdapter = getEventDispatcher().findProcessAdapter(point);
    ProcessAdapter locationAdapter = getEventDispatcher().findProcessAdapter(location);
    if (pointAdapter == null || locationAdapter == null) {
      return;
    }

    fPoint = (TCSObjectReference<Point>) pointAdapter.getProcessObject();
    fLocation = (TCSObjectReference<Location>) locationAdapter.getProcessObject();
    try {
      kernel().connectLocationToPoint(fLocation, fPoint);
          // if a "old" object was restored by undo() save the properties in
      // the kernel
      updateProcessProperties(true);
    }
    catch (KernelRuntimeException e) {
      log.log(Level.WARNING, null, e);
    }
  }

  /**
   * Removes the connection from the kernel.
   */
  private void removeLink() {
    if (fPoint == null || fLocation == null) {
      return;
    }

    try {
      kernel().disconnectLocationFromPoint(fLocation, fPoint);
    }
    catch (KernelRuntimeException e) {
      log.log(Level.WARNING, null, e);
    }
  }
}
