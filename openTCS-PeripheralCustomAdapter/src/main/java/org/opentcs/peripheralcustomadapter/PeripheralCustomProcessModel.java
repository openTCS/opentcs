package org.opentcs.peripheralcustomadapter;

import jakarta.annotation.Nonnull;
import java.io.Serializable;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.PeripheralInformation;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.drivers.peripherals.PeripheralProcessModel;

/**
 * The process model for the peripheral communication adapter.
 */
public class PeripheralCustomProcessModel
    extends
      PeripheralProcessModel
    implements
      Serializable {

  /**
   * Creates a new instance.
   *
   * @param location The reference to the location this adapter is attached to.
   */
  public PeripheralCustomProcessModel(TCSResourceReference<Location> location) {
    this(location, false, false, PeripheralInformation.State.UNKNOWN);
  }

  private PeripheralCustomProcessModel(
      TCSResourceReference<Location> location,
      boolean commAdapterEnabled,
      boolean commAdapterConnected,
      PeripheralInformation.State state
  ) {
    super(location, commAdapterEnabled, commAdapterConnected, state);
  }

  @Override
  public PeripheralCustomProcessModel withLocation(
      @Nonnull
      TCSResourceReference<Location> location
  ) {
    return new PeripheralCustomProcessModel(
        location,
        isCommAdapterEnabled(),
        isCommAdapterConnected(),
        getState()
    );
  }

  @Override
  public PeripheralCustomProcessModel withCommAdapterEnabled(boolean commAdapterEnabled) {
    return new PeripheralCustomProcessModel(
        getLocation(),
        commAdapterEnabled,
        isCommAdapterConnected(),
        getState()
    );
  }

  @Override
  public PeripheralCustomProcessModel withCommAdapterConnected(boolean commAdapterConnected) {
    return new PeripheralCustomProcessModel(
        getLocation(),
        isCommAdapterEnabled(),
        commAdapterConnected,
        getState()
    );
  }

  @Override
  public PeripheralCustomProcessModel withState(PeripheralInformation.State state) {
    return new PeripheralCustomProcessModel(
        getLocation(),
        isCommAdapterEnabled(),
        isCommAdapterConnected(),
        state
    );
  }
}
