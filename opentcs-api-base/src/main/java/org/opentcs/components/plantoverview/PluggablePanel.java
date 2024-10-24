// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.components.plantoverview;

import javax.swing.JPanel;
import org.opentcs.components.Lifecycle;

/**
 * Declares methods that a pluggable panel should provide for the enclosing
 * application.
 */
public abstract class PluggablePanel
    extends
      JPanel
    implements
      Lifecycle {

}
