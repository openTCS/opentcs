package org.opentcs.kernel.extensions.servicewebapi.v1;

import org.opentcs.drivers.vehicle.LoadHandlingDevice;

public class LoadHandlingDeviceTO {

  private String label;
  private boolean full;

  public LoadHandlingDeviceTO() {
  }

  public LoadHandlingDeviceTO(String label, boolean full) {
    this.label = label;
    this.full = full;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public boolean isFull() {
    return full;
  }

  public void setFull(boolean full) {
    this.full = full;
  }

  public static LoadHandlingDeviceTO fromLoadHandlingDevice(LoadHandlingDevice device) {
    return new LoadHandlingDeviceTO(device.getLabel(), device.isFull());
  }

  @Override
  public String toString() {
    return "LoadHandlingDeviceTO{" +
        "label='" + label + '\'' +
        ", full=" + full +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LoadHandlingDeviceTO that = (LoadHandlingDeviceTO) o;
    return full == that.full &&
        java.util.Objects.equals(label, that.label);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(label, full);
  }
}
