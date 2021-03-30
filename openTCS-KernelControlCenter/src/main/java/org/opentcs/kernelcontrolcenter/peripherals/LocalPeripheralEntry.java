/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernelcontrolcenter.peripherals;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.drivers.peripherals.PeripheralCommAdapterDescription;
import org.opentcs.drivers.peripherals.PeripheralProcessModel;

/**
 * An entry for a peripheral device registered with the kernel.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class LocalPeripheralEntry {

  /**
   * Used for implementing property change events.
   */
  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  /**
   * The location representing the peripheral device.
   */
  private final TCSResourceReference<Location> location;
  /**
   * The description of the attached peripheral comm adapter.
   */
  private PeripheralCommAdapterDescription attachedCommAdapter;
  /**
   * The process model that describes the peripheral device's state.
   */
  private PeripheralProcessModel processModel;

  /**
   * Creates a new instance.
   *
   * @param location The location representing the peripheral device.
   * @param attachedCommAdapter The description of the attached peripheral comm adapter.
   * @param processModel The process model that describes the peripheral device's state.
   */
  public LocalPeripheralEntry(@Nonnull TCSResourceReference<Location> location,
                              @Nonnull PeripheralCommAdapterDescription attachedCommAdapter,
                              @Nonnull PeripheralProcessModel processModel) {
    this.location = requireNonNull(location, "location");
    this.attachedCommAdapter = requireNonNull(attachedCommAdapter, "attachedCommAdapter");
    this.processModel = requireNonNull(processModel, "processModel");
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(listener);
  }

  /**
   * Returns the location representing the peripheral device.
   *
   * @return The location representing the peripheral device.
   */
  @Nonnull
  public TCSResourceReference<Location> getLocation() {
    return location;
  }

  /**
   * Returns the description of the attached peripheral comm adapter.
   *
   * @return The description of the attached peripheral comm adapter.
   */
  @Nonnull
  public PeripheralCommAdapterDescription getAttachedCommAdapter() {
    return attachedCommAdapter;
  }

  public void setAttachedCommAdapter(@Nonnull PeripheralCommAdapterDescription attachedCommAdapter) {
    PeripheralCommAdapterDescription oldAttachedCommAdapter = this.attachedCommAdapter;
    this.attachedCommAdapter = requireNonNull(attachedCommAdapter, "attachedCommAdapter");

    pcs.firePropertyChange(Attribute.ATTACHED_COMM_ADAPTER.name(),
                           oldAttachedCommAdapter,
                           attachedCommAdapter);
  }

  /**
   * Returns the process model that describes the peripheral device's state.
   *
   * @return The process model that describes the peripheral device's state.
   */
  @Nonnull
  public PeripheralProcessModel getProcessModel() {
    return processModel;
  }

  public void setProcessModel(@Nonnull PeripheralProcessModel processModel) {
    PeripheralProcessModel oldProcessModel = this.processModel;
    this.processModel = requireNonNull(processModel, "processModel");

    pcs.firePropertyChange(Attribute.PROCESS_MODEL.name(),
                           oldProcessModel,
                           processModel);
  }

  /**
   * Enum elements used as notification arguments to specify which argument changed.
   */
  public static enum Attribute {
    /**
     * Indicates a change of the process model.
     */
    PROCESS_MODEL,
    /**
     * Indicates a change of the attached comm adapter.
     */
    ATTACHED_COMM_ADAPTER
  }
}
