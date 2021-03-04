/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.drivers.vehicle.management;

import java.io.Serializable;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import org.opentcs.drivers.vehicle.VehicleProcessModel;

/**
 * Instances of this class represent events emitted by/for changes on {@link VehicleProcessModel}s.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class ProcessModelEvent
    extends CommAdapterEvent
    implements Serializable {

  /**
   * The attribute's name that changed in the process model.
   */
  private final String attributeChanged;
  /**
   * A serializable representation of the corresponding process model.
   */
  private final VehicleProcessModelTO updatedProcessModel;

  /**
   * Creates a new instance.
   *
   * @param attributeChanged The attribute's name that changed.
   * @param updatedProcessModel A serializable representation of the corresponding process model.
   */
  public ProcessModelEvent(@Nonnull String attributeChanged,
                           @Nonnull VehicleProcessModelTO updatedProcessModel) {
    this.attributeChanged = requireNonNull(attributeChanged, "attributeChanged");
    this.updatedProcessModel = requireNonNull(updatedProcessModel, "updatedProcessModel");
  }

  /**
   * Returns the attribute's name that changed in the process model.
   *
   * @return The attribute's name that changed in the process model.
   */
  public String getAttributeChanged() {
    return attributeChanged;
  }

  /**
   * Returns a serializable representation of the corresponding process model.
   *
   * @return A serializable representation of the corresponding process model.
   */
  public VehicleProcessModelTO getUpdatedProcessModel() {
    return updatedProcessModel;
  }
}
