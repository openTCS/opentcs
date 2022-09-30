/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.servicewebapi.v1.order.binding;

import java.util.List;
import org.opentcs.kernel.extensions.servicewebapi.v1.status.binding.PeripheralOperationDescription;
import org.opentcs.kernel.extensions.servicewebapi.v1.status.binding.Property;

/**
 * A peripheral job to be processed by the kernel.
 *
 * @author Leonard Schüngel (Fraunhofer IML)
 */
public class Job {

  private boolean incompleteName;

  private String reservationToken;

  private String relatedVehicle;

  private String relatedTransportOrder;

  private PeripheralOperationDescription peripheralOperation;

  private List<Property> properties;

  public Job() {
  }

  public boolean isIncompleteName() {
    return incompleteName;
  }

  public void setIncompleteName(boolean incompleteName) {
    this.incompleteName = incompleteName;
  }

  public String getReservationToken() {
    return reservationToken;
  }

  public void setReservationToken(String reservationToken) {
    this.reservationToken = reservationToken;
  }

  public String getRelatedVehicle() {
    return relatedVehicle;
  }

  public void setRelatedVehicle(String relatedVehicle) {
    this.relatedVehicle = relatedVehicle;
  }

  public String getRelatedTransportOrder() {
    return relatedTransportOrder;
  }

  public void setRelatedTransportOrder(String relatedTransportOrder) {
    this.relatedTransportOrder = relatedTransportOrder;
  }

  public PeripheralOperationDescription getPeripheralOperation() {
    return peripheralOperation;
  }

  public void setPeripheralOperation(PeripheralOperationDescription peripheralOperation) {
    this.peripheralOperation = peripheralOperation;
  }

  public List<Property> getProperties() {
    return properties;
  }

  public void setProperties(List<Property> properties) {
    this.properties = properties;
  }

}
