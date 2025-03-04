// SPDX-FileCopyrightText: The openTCS Authors
// SPDX-License-Identifier: MIT
package org.opentcs.operationsdesk.components.drawing.figures.decoration;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.handle.BoundsOutlineHandle;
import org.opentcs.operationsdesk.components.drawing.figures.VehicleFigure;

/**
 */
public class VehicleOutlineHandle
    extends
      BoundsOutlineHandle {

  public VehicleOutlineHandle(Figure owner) {
    super(owner);
  }

  @Override
  public void draw(Graphics2D g) {
    VehicleFigure vf = (VehicleFigure) getOwner();
    Rectangle2D bounds = vf.getBounds();

    if (view != null) {
      AffineTransform at = view.getDrawingToViewTransform();
      at.translate(bounds.getCenterX(), bounds.getCenterY());
      at.rotate(-Math.toRadians(vf.getAngle()));
      at.translate(-bounds.getCenterX(), -bounds.getCenterY());
      Path2D shape = (Path2D) at.createTransformedShape(bounds);
      g.setClip(shape);
      g.setStroke(new BasicStroke(2.0f));
      g.draw(shape);
      Color c = new Color(127, 0, 127, 127);
      g.setPaint(c);
      g.fill(shape);
    }
  }
}
