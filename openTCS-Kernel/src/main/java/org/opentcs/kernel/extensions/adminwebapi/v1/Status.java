/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.kernel.extensions.adminwebapi.v1;

/**
 * Describes the kernel process's current status.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class Status {

  private String heapSize = String.valueOf(Runtime.getRuntime().totalMemory());

  private String maxHeapSize = String.valueOf(Runtime.getRuntime().maxMemory());

  private String freeInHeap = String.valueOf(Runtime.getRuntime().freeMemory());

  public Status() {
  }

  public String getHeapSize() {
    return heapSize;
  }

  public void setHeapSize(String heapSize) {
    this.heapSize = heapSize;
  }

  public String getMaxHeapSize() {
    return maxHeapSize;
  }

  public void setMaxHeapSize(String maxHeapSize) {
    this.maxHeapSize = maxHeapSize;
  }

  public String getFreeInHeap() {
    return freeInHeap;
  }

  public void setFreeInHeap(String freeInHeap) {
    this.freeInHeap = freeInHeap;
  }

}
