/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle.management;

import java.util.List;
import javax.annotation.Nonnull;
import org.opentcs.components.Lifecycle;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.drivers.vehicle.VehicleCommAdapterDescription;

/**
 * Provides comm adapter specific panels used for interaction and displaying information.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface VehicleCommAdapterPanelFactory
    extends Lifecycle {

  /**
   * Returns a list of {@link VehicleCommAdapterPanel}s.
   *
   * @param description The description to create panels for.
   * @param vehicle The vehicle to create panels for.
   * @param processModel The current state of the process model a panel may want to initialize its
   * components with.
   * @return A list of comm adapter panels, or an empty list, if this factory cannot provide panels
   * for the given description.
   */
  List<VehicleCommAdapterPanel> getPanelsFor(@Nonnull VehicleCommAdapterDescription description,
                                             @Nonnull TCSObjectReference<Vehicle> vehicle,
                                             @Nonnull VehicleProcessModelTO processModel);
}
