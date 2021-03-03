/*
 * openTCS copyright information:
 * Copyright (c) 2006 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.data.model;

import java.io.Serializable;
import java.util.Arrays;
import org.opentcs.data.TCSObject;

/**
 * Stores presentation data for the modelling and visualization client(s).
 * The actual presentation data is merely stored but not interpreted by the
 * kernel side of the system.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public final class Layout
extends TCSObject<Layout>
implements Serializable, Cloneable {
  /**
   * The client's actual layout data.
   */
  private byte[] data;
  
  /**
   * Creates a new Layout.
   *
   * @param objectID The new layout's object ID.
   * @param name The new layout's name.
   * @param layoutData The new layout's actual layout data.
   */
  public Layout(int objectID, String name, byte[] layoutData) {
    super(objectID, name);
    if (layoutData == null) {
      throw new NullPointerException("layoutData is null");
    }
    data = layoutData;
  }
  
  /**
   * Returns the client's actual layout data.
   *
   * @return The client's actual layout data.
   */
  public byte[] getData() {
    return data;
  }
  
  /**
   * Sets the client's actual layout data.
   *
   * @param newData The new layout data.
   */
  public void setData(byte[] newData) {
    if (newData == null) {
      throw new NullPointerException("newData is null");
    }
    data = newData;
  }
  
  @Override
  public Layout clone() {
    Layout clone = (Layout) super.clone();
    clone.data = Arrays.copyOf(data, data.length);
    return clone;
  }
}
