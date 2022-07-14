/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.components.drawing.figures;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import org.jhotdraw.draw.event.FigureEvent;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.components.drawing.figures.LabeledFigure;
import org.opentcs.guing.common.components.drawing.figures.TCSFigure;
import org.opentcs.guing.common.components.drawing.figures.TCSLabelFigure;

/**
 *
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class VehicleLabelFigure
    extends TCSLabelFigure {

  private static final Color COLOR_BACKGROUND = new Color(0xFFFFF0);  // beige
  private static final int MARGIN = 4;

  public VehicleLabelFigure(String vehicleName) {
    super(vehicleName);
  }

  @Override
  protected void drawFill(Graphics2D g) {
    if (getText() != null) {
      TextLayout layout = getTextLayout();
      Rectangle2D bounds = layout.getBounds();
      RoundRectangle2D.Double rr = new RoundRectangle2D.Double(
          bounds.getX() + origin.x - MARGIN,
          bounds.getY() + origin.y + layout.getAscent() - MARGIN,
          bounds.getWidth() + 2 * MARGIN,
          bounds.getHeight() + 2 + MARGIN,
          MARGIN, MARGIN);
      g.setPaint(COLOR_BACKGROUND);
      g.fill(rr);
    }
  }

  @Override
  protected void drawStroke(Graphics2D g) {
  }

  @Override
  protected void drawText(Graphics2D g) {
    if (getText() != null || isEditable()) {
      TextLayout layout = getTextLayout();
      g.setPaint(Color.BLUE.darker());
      layout.draw(g, (float) origin.x, (float) (origin.y + layout.getAscent()));
    }
  }

  @Override
  public void figureChanged(FigureEvent event) {
    if (event.getFigure() instanceof LabeledFigure) {
      LabeledFigure lf = (LabeledFigure) event.getFigure();
      TCSFigure figure = lf.getPresentationFigure();
      VehicleModel model = (VehicleModel) figure.getModel();
      String name = model.getName();

      if (model.getPoint() != null) {
        name += "@" + model.getPoint().getName();
      }

      setText(name);
      invalidate();
      validate();
    }
  }
}
