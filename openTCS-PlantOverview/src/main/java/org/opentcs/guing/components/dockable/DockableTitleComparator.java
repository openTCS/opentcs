/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.guing.components.dockable;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import java.util.Comparator;

/**
 * Compares two <code>DefaultSingleCDockable</code> instances by their titles.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DockableTitleComparator
    implements Comparator<DefaultSingleCDockable> {

  @Override
  public int compare(DefaultSingleCDockable o1, DefaultSingleCDockable o2) {
    return o1.getTitleText().compareTo(o2.getTitleText());
  }
}
