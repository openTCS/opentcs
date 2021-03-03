/**
 * (c): Fraunhofer IML.
 *
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
