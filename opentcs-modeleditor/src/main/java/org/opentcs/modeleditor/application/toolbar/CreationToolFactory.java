// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.modeleditor.application.toolbar;

import org.jhotdraw.draw.ConnectionFigure;
import org.jhotdraw.draw.Figure;
import org.opentcs.thirdparty.modeleditor.jhotdraw.application.toolbar.OpenTCSConnectionTool;

/**
 * A factory for tools concerned with the creation of figures/model elements.
 */
public interface CreationToolFactory {

  OpenTCSCreationTool createCreationTool(Figure prototype);

  OpenTCSConnectionTool createConnectionTool(ConnectionFigure prototype);
}
