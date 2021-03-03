/**
 * (c): IML, IFAK, JHotDraw.
 *
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

	private final Color COLOR_BACKGROUND = new Color(0xFFFFF0);	// beige
	private final int margin = 4;

	public VehicleLabelFigure(String vehicleName) {
		super(vehicleName);
	}

	@Override	// TextFigure
	protected void drawFill(Graphics2D g) {
		if (getText() != null) {
			TextLayout layout = getTextLayout();
			Rectangle2D bounds = layout.getBounds();
			RoundRectangle2D.Double rr = new RoundRectangle2D.Double(
					bounds.getX() + origin.x - margin,
					bounds.getY() + origin.y + layout.getAscent() - margin,
					bounds.getWidth() + 2 * margin,
					bounds.getHeight() + 2 + margin,
					margin, margin);
			g.setPaint(COLOR_BACKGROUND);
			g.fill(rr);
		}
	}

	@Override	// TextFigure
	protected void drawStroke(Graphics2D g) {
	}

	@Override	// TextFigure
	protected void drawText(Graphics2D g) {
		if (getText() != null || isEditable()) {
			TextLayout layout = getTextLayout();
			g.setPaint(Color.BLUE.darker());
			layout.draw(g, (float) origin.x, (float) (origin.y + layout.getAscent()));
		}
	}

	@Override	// LabelFigure
	public void figureChanged(FigureEvent event) {
		if (event.getFigure() instanceof LabeledFigure) {
			LabeledFigure lf = (LabeledFigure) event.getFigure();
			TCSFigure figure = (TCSFigure) lf.getPresentationFigure();
			VehicleModel model = (VehicleModel) figure.getModel();
			String name = model.getName();
			
			if (model.getPoint() != null) {
				name += "@" + model.getPoint().getName();
			}

			// TODO: Mehrzeilig/HTML? - So geht's nicht:
//			String name = "<html>" + model.getName() + "</html>";
//			if (model.getPoint() != null) {
//				name = "<html>" + model.getName() + "</br>" + model.getPoint().getName() + "</html>";
//			}
			
			setText(name);
			// Label neu zeichnen
			invalidate();
			validate();
		}
	}
}
