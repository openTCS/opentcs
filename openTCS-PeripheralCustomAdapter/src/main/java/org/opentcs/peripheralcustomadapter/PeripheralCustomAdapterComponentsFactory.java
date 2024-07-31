package org.opentcs.peripheralcustomadapter;

import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;

/**
 * A factory for creating various comm adapter panel-specific instances.
 */
public interface PeripheralCustomAdapterComponentsFactory {

  /**
   * Creates a new Peripheral communication adapter for the given location.
   *
   * @param location The location.
   * @return A Peripheral communication adapter instance.
   */

  PeripheralCommunicationAdapter createPeripheralCustomCommAdapter(
      TCSResourceReference<Location> location
  );
}
