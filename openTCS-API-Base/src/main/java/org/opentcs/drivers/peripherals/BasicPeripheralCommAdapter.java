/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.peripherals;

import static java.util.Objects.requireNonNull;
import org.opentcs.data.model.PeripheralInformation;
import org.opentcs.drivers.peripherals.management.PeripheralProcessModelEvent;
import org.opentcs.util.event.EventHandler;

/**
 * A base class for peripheral communication adapters mainly providing command queue processing.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public abstract class BasicPeripheralCommAdapter
    implements PeripheralCommAdapter {

  /**
   * The handler used to send events to.
   */
  private final EventHandler eventHandler;
  /**
   * A model of the peripheral device's and its communication adapter's attributes.
   */
  private PeripheralProcessModel processModel;
  /**
   * Indicates whether this comm adapter is initialized.
   */
  private boolean initialized;

  /**
   * Creates a new instance.
   *
   * @param processModel A model of the peripheral device's and its communication adapter's
   * attributes.
   * @param eventHandler The handler used to send events to.
   */
  public BasicPeripheralCommAdapter(PeripheralProcessModel processModel,
                                    EventHandler eventHandler) {
    this.processModel = requireNonNull(processModel, "processModel");
    this.eventHandler = requireNonNull(eventHandler, "eventHandler");
  }

  /**
   * {@inheritDoc}
   * <p>
   * <em>Overriding methods are expected to call this implementation, too.</em>
   * </p>
   */
  @Override
  public void initialize() {
    if (isInitialized()) {
      return;
    }

    setProcessModel(getProcessModel().withState(PeripheralInformation.State.UNKNOWN));
    sendProcessModelChangedEvent(PeripheralProcessModel.Attribute.STATE);

    initialized = true;
  }

  @Override
  public boolean isInitialized() {
    return initialized;
  }

  /**
   * {@inheritDoc}
   * <p>
   * <em>Overriding methods are expected to call this implementation, too.</em>
   * </p>
   */
  @Override
  public void terminate() {
    if (!isInitialized()) {
      return;
    }

    initialized = false;
  }

  /**
   * {@inheritDoc}
   * <p>
   * <em>Overriding methods are expected to call this implementation, too.</em>
   * </p>
   */
  @Override
  public void enable() {
    if (isEnabled()) {
      return;
    }

    connectPeripheral();
    setProcessModel(getProcessModel().withCommAdapterEnabled(true));
    sendProcessModelChangedEvent(PeripheralProcessModel.Attribute.COMM_ADAPTER_ENABLED);
  }

  @Override
  public boolean isEnabled() {
    return processModel.isCommAdapterEnabled();
  }

  /**
   * {@inheritDoc}
   * <p>
   * <em>Overriding methods are expected to call this implementation, too.</em>
   * </p>
   */
  @Override
  public void disable() {
    if (!isEnabled()) {
      return;
    }

    disconnectPeripheral();
    setProcessModel(getProcessModel().withCommAdapterEnabled(false)
        .withState(PeripheralInformation.State.UNKNOWN));
    sendProcessModelChangedEvent(PeripheralProcessModel.Attribute.COMM_ADAPTER_ENABLED);
    sendProcessModelChangedEvent(PeripheralProcessModel.Attribute.STATE);
  }

  @Override
  public PeripheralProcessModel getProcessModel() {
    return processModel;
  }

  protected void setProcessModel(PeripheralProcessModel processModel) {
    this.processModel = processModel;
  }

  protected EventHandler getEventHandler() {
    return eventHandler;
  }

  protected void sendProcessModelChangedEvent(PeripheralProcessModel.Attribute attributeChanged) {
    eventHandler.onEvent(new PeripheralProcessModelEvent(processModel.getLocation(),
                                                         attributeChanged.name(),
                                                         processModel));
  }

  /**
   * Initiates a communication channel to the peripheral device.
   * This method should not block, i.e. it should not wait for the actual connection to be
   * established, as the peripheral device could be temporarily absent or not responding at all.
   * If that's the case, the communication adapter should continue trying to establish a connection
   * until successful or until {@link #disconnectPeripheral()} is called.
   */
  protected abstract void connectPeripheral();

  /**
   * Closes the communication channel to the peripheral device.
   */
  protected abstract void disconnectPeripheral();
}
