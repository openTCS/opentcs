/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.modeleditor.application.toolbar;

import org.jhotdraw.draw.ConnectionFigure;
import org.jhotdraw.draw.Figure;
import org.opentcs.thirdparty.modeleditor.jhotdraw.application.toolbar.OpenTCSConnectionTool;

/**
 * A factory for tools concerned with the creation of figures/model elements.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public interface CreationToolFactory {

  OpenTCSCreationTool createCreationTool(Figure prototype);

  OpenTCSConnectionTool createConnectionTool(ConnectionFigure prototype);
}
