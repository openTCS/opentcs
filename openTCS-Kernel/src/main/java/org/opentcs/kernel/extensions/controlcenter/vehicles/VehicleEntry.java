/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.controlcenter.vehicles;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapter;
import org.opentcs.drivers.vehicle.VehicleCommAdapterFactory;
import org.opentcs.drivers.vehicle.VehicleProcessModel;

/**
 * Represents a {@link Vehicle} in the {@link VehicleEntryPool}.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class VehicleEntry
    implements PropertyChangeListener {

  /**
   * The vehicle this entry represents.
   */
  private final Vehicle vehicle;
  /**
   * Used for implementing property change events.
   */
  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  /**
   * The process model for the vehicle.
   */
  private VehicleProcessModel processModel;
  /**
   * The comm adapter factory for this vehicle.
   */
  private VehicleCommAdapterFactory commAdapterFactory = new NullVehicleCommAdapterFactory();
  /**
   * The comm adapter that is attached to this vehicle.
   */
  private VehicleCommAdapter commAdapter;

  /**
   * Creates a vehicle entry.
   *
   * @param vehicle The vehicle this entry represents.
   */
  public VehicleEntry(Vehicle vehicle) {
    this.vehicle = requireNonNull(vehicle, "vehicle");
    this.processModel = new VehicleProcessModel(vehicle);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (!(evt.getSource() instanceof VehicleProcessModel)) {
      return;
    }

    pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
  }

  /**
   * Add a property change listener.
   *
   * @param listener The listener to add.
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    pcs.addPropertyChangeListener(listener);
  }

  /**
   * Remove a property change listener.
   *
   * @param listener The listener to remove.
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    pcs.removePropertyChangeListener(listener);
  }

  /**
   * Returns the vehicle that is represented by this entry.
   *
   * @return The vehicle that is represented by this entry.
   */
  @Nonnull
  public Vehicle getVehicle() {
    return vehicle;
  }

  /**
   * Returns the name of the vehicle that is represented by this entry.
   *
   * @return The name of the vehicle that is represented by this entry.
   */
  @Nonnull
  public String getVehicleName() {
    return vehicle.getName();
  }

  /**
   * Returns the process model of the vehicle that is represented by this entry.
   *
   * @return The process model of the vehicle that is represented by this entry.
   */
  @Nonnull
  public VehicleProcessModel getProcessModel() {
    return processModel;
  }

  /**
   * Sets the process model for the vehicle represented by this entry.
   *
   * @param processModel The new process model for the vehicle.
   */
  public void setProcessModel(@Nonnull VehicleProcessModel processModel) {
    VehicleProcessModel oldProcessModel = this.processModel;
    this.processModel = requireNonNull(processModel, "processModel");

    oldProcessModel.removePropertyChangeListener(this);
    processModel.addPropertyChangeListener(this);

    pcs.firePropertyChange(Attribute.PROCESS_MODEL.name(), oldProcessModel, processModel);
  }

  @Nonnull
  public VehicleCommAdapterFactory getCommAdapterFactory() {
    return commAdapterFactory;
  }

  /**
   * Sets the comm adapter factory for this entry.
   *
   * @param commAdapterFactory The new comm adapter factory.
   */
  public void setCommAdapterFactory(@Nonnull VehicleCommAdapterFactory commAdapterFactory) {
    VehicleCommAdapterFactory oldValue = this.commAdapterFactory;
    this.commAdapterFactory = commAdapterFactory;

    pcs.firePropertyChange(Attribute.COMM_ADAPTER_FACTORY.name(), oldValue, commAdapterFactory);
  }

  /**
   * Returns the comm adapter factory for this entry.
   *
   * @return The comm adapter factory for this entry
   */
  @Nullable
  public VehicleCommAdapter getCommAdapter() {
    return commAdapter;
  }

  /**
   * Sets the comm adapter for this entry.
   *
   * @param commAdapter The new comm adapter.
   */
  public void setCommAdapter(@Nullable VehicleCommAdapter commAdapter) {
    VehicleCommAdapter oldValue = this.commAdapter;
    this.commAdapter = commAdapter;

    pcs.firePropertyChange(Attribute.COMM_ADAPTER.name(), oldValue, commAdapter);
  }

  /**
   * Enum elements used as notification arguments to specify which argument changed.
   */
  public enum Attribute {
    /**
     * Indicates a change of the process model reference.
     */
    PROCESS_MODEL,
    /**
     * Indicates a change of the comm adapter factory reference.
     */
    COMM_ADAPTER_FACTORY,
    /**
     * Indicates a change of the comm adapter reference.
     */
    COMM_ADAPTER
  }
}
