/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.virtualvehicle;

import org.opentcs.components.kernel.services.VehicleService;

/**
 * A factory for creating various comm adapter panel specific instances.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface AdapterPanelComponentsFactory {

  /**
   * Creates a {@link LoopbackCommAdapterPanel} representing the given process model's content.
   *
   * @param processModel The process model to represent.
   * @param vehicleService The vehicle service used for interaction with the comm adapter.
   * @return The comm adapter panel.
   */
  LoopbackCommAdapterPanel createPanel(LoopbackVehicleModelTO processModel,
                                       VehicleService vehicleService);
}
