/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.exchange.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectEvent;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.type.KeyValueProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.Property;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.exchange.DefaultKernelProxyManager;
import org.opentcs.guing.exchange.OpenTCSEventDispatcher;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.util.eventsystem.TCSEvent;

/**
 * Basis implementation for process adapter that work with kernel objects.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public abstract class OpenTCSProcessAdapter
    extends AbstractProcessAdapter {

  /**
   * This class's logger.
   */
  private static final Logger log
      = Logger.getLogger(OpenTCSProcessAdapter.class.getName());
  /**
   * Flag whether a model is currently being loaded. Some actions will
   * be ignored if this is set to true.
   */
  private boolean inTransition = false;

  /**
   * Creates a new instance.
   */
  public OpenTCSProcessAdapter() {
    // Do nada.
  }

  /**
   * Sets the <code>ModelLayoutElement</code>.
   *
   * @param layoutElement The layout element.
   */
  public void setLayoutElement(ModelLayoutElement layoutElement) {
    fLayoutElement = Objects.requireNonNull(layoutElement, "layoutElement is null");
  }

  @Override
  public TCSObjectReference<?> getProcessObject() {
    return (TCSObjectReference<?>) super.getProcessObject();
  }

  /**
   * Called when an event has happened.
   *
   * @param event The event that has happened.
   * @throws NullPointerException If
   * <code>event</code> is
   * <code>null</code>.
   */
  public void processTCSObjectEvent(TCSEvent event) {
    TCSObjectEvent objectEvent = (TCSObjectEvent) event;
    StringBuilder msg = new StringBuilder();

    switch (objectEvent.getType()) {
      case OBJECT_CREATED:
        msg.append("TCSObject created. Id: ")
            .append(objectEvent.getCurrentObjectState().getId())
            .append(" Name: ")
            .append(objectEvent.getCurrentObjectState().getName());
        log.fine(msg.toString());
        break;

      case OBJECT_MODIFIED:
        msg.append("TCSObject modified. Id: ")
            .append(objectEvent.getCurrentObjectState().getId())
            .append(" Name: ")
            .append(objectEvent.getCurrentObjectState().getName());

        if (objectEvent.getCurrentObjectState() instanceof Point) {
          Point p = (Point) objectEvent.getCurrentObjectState();
          msg.append(" Pos: ").append(p.getPosition().toString());
        }

        log.fine(msg.toString());
        updateModelProperties();
        break;

      case OBJECT_REMOVED:
        msg.append("TCSObject removed. Id: ")
            .append(objectEvent.getPreviousObjectState().getId())
            .append(" Name: ")
            .append(objectEvent.getPreviousObjectState().getName());
        log.fine(msg.toString());
        break;

      default:
        log.log(Level.WARNING,
                "Received unknown event type: {0}. Id: {1} Name: {2}",
                new Object[] {objectEvent.getType(),
                              objectEvent.getCurrentObjectState().getId(),
                              objectEvent.getCurrentObjectState().getName()});
    }
  }

  /**
   * Undoes the last change of the applications. Neccessary if the kernel
   * refused to create an element because the name is already taken.
   *
   * @param name The name of the element that should have been created.
   * @param e The exception that was thrown.
   */
  protected void undo(String name, Exception e) {
    log.log(Level.INFO, "A model element with name \"{0}\" already exists", name);

    OpenTCSEventDispatcher dispatcher = (OpenTCSEventDispatcher) getEventDispatcher();
    dispatcher.undo(e.getMessage());
  }

  /**
   * Returns whether the kernel is in modeling state.
   *
   * @return <code> true </code>, if the kernel is in modeling state.
   */
  protected boolean hasModelingState() {
    try {
      return (DefaultKernelProxyManager.instance().kernel().getState() == Kernel.State.MODELLING);
    }
    catch (CredentialsException e) {
      log.log(Level.WARNING, "Unexpected exception", e);
      return false;
    }
  }

  /**
   * Returns the name of the kernel object.
   *
   * @return The name.
   */
  protected String getName() {
    return getProcessObject().getName();
  }

  @Override // AttributesChangeListener
  public void propertiesChanged(AttributesChangeEvent e) {
  }

  /**
   * Adopts the name of the kernel object to the model.
   *
   * @param tcsObject The <code>TCSObject</code> to copy from.
   */
  protected void nameToModel(TCSObject<?> tcsObject) {
    StringProperty pName = (StringProperty) getModel().getProperty(ModelComponent.NAME);

    if (pName.getText().isEmpty()) {
      // new object created
      pName.setText(tcsObject.getName());
    }

    updateProcessProperties(true);
  }

  /**
   * Reads the current properties from the kernel and adopts these for
   * the model object.
   */
  public abstract void updateModelProperties();

  /**
   * Reads the current misc properties from the kernel and adopts these for
   * the model object.
   *
   * @param tcsObject The <code>TCSObject</code> to read from.
   */
  protected void updateMiscModelProperties(TCSObject<?> tcsObject) {
    List<KeyValueProperty> items = new ArrayList<>();
    Map<String, String> misc = tcsObject.getProperties();

    for (Map.Entry<String, String> curEntry : misc.entrySet()) {
      if (!curEntry.getValue().contains("Unknown")) {
        items.add(new KeyValueProperty(getModel(), curEntry.getKey(), curEntry.getValue()));
      }
    }

    KeyValueSetProperty miscellaneous = (KeyValueSetProperty) getModel().getProperty(ModelComponent.MISCELLANEOUS);
    miscellaneous.setItems(items);
  }

  /**
   * Reads the current properties from the model and adopts these for the
   * kernel object.
   *
   * @param updateAllProperties <code>true</code> to update all properties in
   * the kernel, <code>false</code> to only update those that have changed.
   */
  public void updateProcessProperties(boolean updateAllProperties) {
    for (Object o : getModel().getProperties().values()) {
      Property property = (Property) o;

      if (((kernel().getState() == Kernel.State.MODELLING && property.isModellingEditable())
           || property.isOperatingEditable())
          && property.hasChanged()) {
        OpenTCSView.instance().modelComponentChanged();
        break;
      }
    }
  }

  /**
   * Reads the current misc properties from the model and adopts these for the
   * kernel object.
   *
   * @param updateAllProperties <code>true</code> to update all properties in
   * the kernel, <code>false</code> to only update those that have changed.
   */
  protected void updateMiscProcessProperties(boolean updateAllProperties)
      throws ObjectUnknownException, CredentialsException {

    kernel().clearTCSObjectProperties(getProcessObject());
    KeyValueSetProperty misc = (KeyValueSetProperty) getModel().getProperty(ModelComponent.MISCELLANEOUS);

    if (misc != null) {
      for (KeyValueProperty p : misc.getItems()) {
        kernel().setTCSObjectProperty(getProcessObject(), p.getKey(), p.getValue());
      }
    }
  }

  /**
   * Checks whether a new model is currently being loaded.
   *
   * @return <code>true</code> if, and only if, a new model is currently being
   * loaded.
   */
  public boolean isInTransition() {
    return inTransition;
  }

  /**
   * Sets whether a new model is currently being loaded.
   *
   * @param inTransition True if yes, false otherwise.
   */
  public void setInTransition(boolean inTransition) {
    this.inTransition = inTransition;
  }
}
