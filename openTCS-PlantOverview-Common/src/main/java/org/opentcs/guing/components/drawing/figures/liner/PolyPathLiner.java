/*
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.drawing.figures.liner;

import java.util.Collection;
import java.util.Collections;
import org.jhotdraw.draw.ConnectionFigure;
import org.jhotdraw.draw.LineConnectionFigure;
import org.jhotdraw.draw.handle.Handle;
import org.jhotdraw.draw.liner.Liner;
import org.jhotdraw.geom.BezierPath;

/**
 *
 * @author Leonard Schuengel (Fraunhofer IML)
 */
public class PolyPathLiner
    implements org.jhotdraw.draw.liner.Liner {

  public PolyPathLiner() {
  }

  @Override
  public void lineout(ConnectionFigure figure) {
    BezierPath path = ((LineConnectionFigure) figure).getBezierPath();

    if (path != null) {
      path.invalidatePath();
    }

  }

  @Override
  public Collection<Handle> createHandles(BezierPath path) {
    return Collections.emptyList();
  }

  @Override // Object
  public Liner clone() {
    try {
      return (Liner) super.clone();
    }
    catch (CloneNotSupportedException ex) {
      InternalError error = new InternalError(ex.getMessage());
      error.initCause(ex);
      throw error;
    }
  }

}
