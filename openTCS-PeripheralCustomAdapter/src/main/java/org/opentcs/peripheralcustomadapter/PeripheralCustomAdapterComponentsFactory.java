package org.opentcs.peripheralcustomadapter;

import org.opentcs.data.model.Location;
import org.opentcs.data.model.TCSResourceReference;

public interface PeripheralCustomAdapterComponentsFactory {

  PeripheralCommunicationAdapter createPeripheralCustomCommAdapter(
      TCSResourceReference<Location> location
  );
}
