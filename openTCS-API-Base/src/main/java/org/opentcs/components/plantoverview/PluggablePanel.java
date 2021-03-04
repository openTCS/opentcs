/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.components.plantoverview;

import javax.swing.JPanel;
import org.opentcs.components.Lifecycle;

/**
 * Declares methods that a pluggable panel should provide for the enclosing
 * application.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class PluggablePanel
    extends JPanel
    implements Lifecycle {

}
