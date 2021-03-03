/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.exchange.adapter;

import java.io.Serializable;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.exchange.EventDispatcher;
import org.opentcs.guing.model.ModelComponent;

/**
 * Receives messages from a
 * <code>ModelComponent</code> and its kernel equivalent and delegates them
 * to the respectively other one.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public interface ProcessAdapter
    extends AttributesChangeListener, Serializable {

  /**
   * Registers itself at the kernel and model object
   */
  void register();

  /**
   * Returns the <code>EventDispatcher</code>.
   *
   * @return The <code>EventDispatcher</code>.
   */
  EventDispatcher getEventDispatcher();

  /**
   * Sets the <code>EventDispatcher</code>. This is the central point where
   * objects from the kernel are gathered.
   *
   * @param eventDispatcher The  <code>EventDispatcher</code>.
   */
  void setEventDispatcher(EventDispatcher eventDispatcher);

  /**
   * Sets the model component.
   *
   * @param model The model component.
   */
  void setModel(ModelComponent model);

  /**
   * Returns the model component.
   *
   * @return The model component.
   */
  ModelComponent getModel();

  /**
   * Creates the object in the kernel. If successful, a copy will be returned,
   * <code>null</code> otherwise.
   *
   * @return A copy of the created object. <code>null</code> if it failed.
   * @throws KernelRuntimeException If the creation failed.
   */
  Object createProcessObject() throws KernelRuntimeException;

  /**
   * Sets the kernel object.
   *
   * @param processObject A reference to the kernel object.
   */
  void setProcessObject(Object processObject);

  /**
   * Removes the kernel object and the layout elements.
   */
  void releaseProcessObject();

  /**
   * Removed the layout element.
   */
  void releaseLayoutElement();

  /**
   * Returns a reference to the kernel object.
   *
   * @return A reference to the kernel object.
   */
  Object getProcessObject();
}
