/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.guing.components.drawing.figures;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import org.jhotdraw.draw.event.FigureEvent;
import org.opentcs.guing.model.elements.VehicleModel;

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

  @Override  // TextFigure
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

  @Override  // TextFigure
  protected void drawStroke(Graphics2D g) {
  }

  @Override  // TextFigure
  protected void drawText(Graphics2D g) {
    if (getText() != null || isEditable()) {
      TextLayout layout = getTextLayout();
      g.setPaint(Color.BLUE.darker());
      layout.draw(g, (float) origin.x, (float) (origin.y + layout.getAscent()));
    }
  }

  @Override  // LabelFigure
  public void figureChanged(FigureEvent event) {
    if (event.getFigure() instanceof LabeledFigure) {
      LabeledFigure lf = (LabeledFigure) event.getFigure();
      TCSFigure figure = lf.getPresentationFigure();
      VehicleModel model = (VehicleModel) figure.getModel();
      String name = model.getName();
      
      if (model.getPoint() != null) {
        name += "@" + model.getPoint().getName();
      }

      // TODO: Mehrzeilig/HTML? - So geht's nicht:
//      String name = "<html>" + model.getName() + "</html>";
//      if (model.getPoint() != null) {
//        name = "<html>" + model.getName() + "</br>" + model.getPoint().getName() + "</html>";
//      }
      
      setText(name);
      // Label neu zeichnen
      invalidate();
      validate();
    }
  }
}
