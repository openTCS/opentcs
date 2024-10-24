// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.kernel.extensions.servicewebapi.v1.binding;

import java.util.List;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.PeripheralOperationDescription;
import org.opentcs.kernel.extensions.servicewebapi.v1.binding.shared.Property;

/**
 * A peripheral job to be processed by the kernel.
 */
public class PostPeripheralJobRequestTO {

  private boolean incompleteName;

  private String reservationToken;

  private String relatedVehicle;

  private String relatedTransportOrder;

  private PeripheralOperationDescription peripheralOperation;

  private List<Property> properties;

  public PostPeripheralJobRequestTO() {
  }

  public boolean isIncompleteName() {
    return incompleteName;
  }

  public PostPeripheralJobRequestTO setIncompleteName(boolean incompleteName) {
    this.incompleteName = incompleteName;
    return this;
  }

  public String getReservationToken() {
    return reservationToken;
  }

  public PostPeripheralJobRequestTO setReservationToken(String reservationToken) {
    this.reservationToken = reservationToken;
    return this;
  }

  public String getRelatedVehicle() {
    return relatedVehicle;
  }

  public PostPeripheralJobRequestTO setRelatedVehicle(String relatedVehicle) {
    this.relatedVehicle = relatedVehicle;
    return this;
  }

  public String getRelatedTransportOrder() {
    return relatedTransportOrder;
  }

  public PostPeripheralJobRequestTO setRelatedTransportOrder(String relatedTransportOrder) {
    this.relatedTransportOrder = relatedTransportOrder;
    return this;
  }

  public PeripheralOperationDescription getPeripheralOperation() {
    return peripheralOperation;
  }

  public PostPeripheralJobRequestTO setPeripheralOperation(
      PeripheralOperationDescription peripheralOperation
  ) {
    this.peripheralOperation = peripheralOperation;
    return this;
  }

  public List<Property> getProperties() {
    return properties;
  }

  public PostPeripheralJobRequestTO setProperties(List<Property> properties) {
    this.properties = properties;
    return this;
  }

}
