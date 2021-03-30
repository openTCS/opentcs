/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.guing.components.drawing.figures.decoration;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.handle.BoundsOutlineHandle;
import org.opentcs.guing.components.drawing.figures.PointFigure;

/**
 *
 * 
 * @author Heinz Huber (Fraunhofer IML)
 */
public class PointOutlineHandle
    extends BoundsOutlineHandle {

  public PointOutlineHandle(Figure owner) {
    super(owner);
  }

  @Override
  public void draw(Graphics2D g) {
    PointFigure pf = (PointFigure) getOwner();
    Shape bounds = pf.getShape();

    if (getOwner().get(AttributeKeys.TRANSFORM) != null) {
      bounds = getOwner().get(AttributeKeys.TRANSFORM).createTransformedShape(bounds);
    }

    if (view != null) {
      bounds = view.getDrawingToViewTransform().createTransformedShape(bounds);
      Rectangle2D bounds2D = bounds.getBounds2D();
      float centerX = (float) bounds2D.getCenterX();
      float centerY = (float) bounds2D.getCenterY();
      Point2D center = new Point2D.Float(centerX, centerY);
      float radius = 10.0f;
      float[] dist = {0.1f, 0.9f};
      Color[] colors = {Color.CYAN, Color.BLUE};

      RadialGradientPaint radialGradientPaint = new RadialGradientPaint(center, radius, dist, colors);
      Paint oldPaint = g.getPaint();
      g.setPaint(radialGradientPaint);

      g.fill(bounds);
      g.setPaint(oldPaint);
    }
  }
}
