// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.guing.common.components.dockable;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import java.util.Comparator;

/**
 * Compares two <code>DefaultSingleCDockable</code> instances by their titles.
 */
public class DockableTitleComparator
    implements
      Comparator<DefaultSingleCDockable> {

  /**
   * Creates a new instance.
   */
  public DockableTitleComparator() {
  }

  @Override
  public int compare(DefaultSingleCDockable o1, DefaultSingleCDockable o2) {
    return o1.getTitleText().compareTo(o2.getTitleText());
  }
}
