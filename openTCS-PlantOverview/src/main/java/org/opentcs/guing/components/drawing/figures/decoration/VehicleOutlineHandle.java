/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.guing.components.drawing.figures.decoration;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.handle.BoundsOutlineHandle;
import org.opentcs.guing.components.drawing.figures.VehicleFigure;

/**
 *
 * 
 * @author Heinz Huber (Fraunhofer IML)
 */
public class VehicleOutlineHandle
    extends BoundsOutlineHandle {

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
      // Rahmen um die Figur
      g.setClip(shape);
      g.setStroke(new BasicStroke(2.0f));
      g.draw(shape);
      // Transparente Füllung über die Figur
      Color c = new Color(127, 0, 127, 127);
      g.setPaint(c);
      g.fill(shape);
    }
  }
}
