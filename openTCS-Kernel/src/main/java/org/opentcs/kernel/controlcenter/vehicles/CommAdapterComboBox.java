/**
 * Copyright (c) 2016 Fraunhofer IML
 */
package org.opentcs.kernel.controlcenter.vehicles;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import org.opentcs.drivers.vehicle.VehicleCommAdapterFactory;
import org.opentcs.util.gui.WideComboBox;

/**
 * A wide combobox which sets the selected item when receiving an update event from a
 * {@link VehicleCommAdapterFactory}.
 *
 * @author Mats Wilhelm (Fraunhofer IML)
 * @param <E> The type of this combo box's elements.
 */
public class CommAdapterComboBox<E>
    extends WideComboBox<E>
    implements PropertyChangeListener {

  /**
   * Creates a new instance.
   */
  public CommAdapterComboBox() {
    super();
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (Objects.equals(evt.getPropertyName(), VehicleEntry.Attribute.COMM_ADAPTER_FACTORY)) {
      getModel().setSelectedItem(evt.getNewValue());
    }
  }
}
