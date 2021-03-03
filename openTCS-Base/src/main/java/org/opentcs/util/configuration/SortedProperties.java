/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.util.configuration;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

/**
 * A subclass of <code>Properties</code> for storing properties in
 * lexicographical order.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class SortedProperties
extends Properties {

  /**
   * Creates a new SortedProperties.
   */
  public SortedProperties() {
    // Do nada.
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Enumeration<Object> keys() {
    Vector v = new Vector(keySet());
    Collections.sort(v);
    return v.elements();
  }
}
