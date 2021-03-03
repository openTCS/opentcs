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
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

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
  
  @Override
  public Enumeration<Object> keys() {
    Set<String> keyStrings = new TreeSet<>();
    for (Object key : keySet()) {
      keyStrings.add((String) key);
    }
    return Collections.enumeration(new LinkedList<>(keyStrings));
  }
}
