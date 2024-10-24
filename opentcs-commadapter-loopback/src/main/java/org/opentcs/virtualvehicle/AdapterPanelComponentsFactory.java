// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.virtualvehicle;

import org.opentcs.components.kernel.services.VehicleService;

/**
 * A factory for creating various comm adapter panel specific instances.
 */
public interface AdapterPanelComponentsFactory {

  /**
   * Creates a {@link LoopbackCommAdapterPanel} representing the given process model's content.
   *
   * @param processModel The process model to represent.
   * @param vehicleService The vehicle service used for interaction with the comm adapter.
   * @return The comm adapter panel.
   */
  LoopbackCommAdapterPanel createPanel(
      LoopbackVehicleModelTO processModel,
      VehicleService vehicleService
  );
}
