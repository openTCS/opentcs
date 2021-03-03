/*
 *
 * Created on 17.08.2012 13:01:41
 */
package org.opentcs.guing.components.drawing;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * A ruler.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class Ruler
		extends JComponent
		implements PropertyChangeListener {

	/**
	 * Size of the rulers( height of the horizontal ruler, width of the vertical).
	 */
	public static final int SIZE = 25;

	/**
	 * Orientation of this ruler.
	 */
	public enum Orientation {

		HORIZONTAL,
		VERTICAL
	}
	public Orientation orientation;
	/**
	 * The translation of the drawing view.
	 */
	private Point translation = new Point(0, 0);
	/**
	 * The DrawingView.
	 */
	private final OpenTCSDrawingView drawingView;
	/**
	 * The current scale factor.
	 */
	private double scaleFactor = 1.0;

	/**
	 * Creates a new ruler.
	 *
	 * @param orientation The orientation (0 - horizontal; 1 - vertical).
	 * @param drawingView The DrawingView.
	 */
	public Ruler(Orientation orientation, OpenTCSDrawingView drawingView) {
		this.orientation = orientation;
		Objects.requireNonNull(drawingView);
		this.drawingView = drawingView;
	}

	/**
	 * Sets a new height of the ruler and repaints it.
	 *
	 * @param preferredHeight The new height.
	 */
	public void setPreferredHeight(int preferredHeight) {
		setPreferredSize(new Dimension(SIZE, preferredHeight));
		repaint();
	}

	/**
	 * Sets a new width of the ruler and repaints it.
	 *
	 * @param preferredWidth The new width.
	 */
	public void setPreferredWidth(int preferredWidth) {
		setPreferredSize(new Dimension(preferredWidth, SIZE));
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Rectangle drawHere = g.getClipBounds();
		translation = (Point) drawingView.getTranslation().clone();

		Graphics2D g2d = (Graphics2D) g;
		g2d.setFont(new Font("Arial", Font.PLAIN, 10));
		// i translated
		int translated;
		// i normalized to decimal
		int draw;
		int drawOld = 0;
		// draw translated
		int drawTranslated;
		String translatedAsString;
		String lastIndex;

		// horizontal
		if (orientation == Orientation.HORIZONTAL) {
			// base line
			g2d.drawLine(
					0, SIZE - 1,
					getWidth(), SIZE - 1);

			for (int i = drawHere.x; i < getWidth(); i += 10) {
				translated = translateValue(i);
				translatedAsString = Integer.toString(translated);
				lastIndex = translatedAsString.substring(translatedAsString.length() - 1);

				int decimal = Integer.parseInt(lastIndex);
				{
					// These steps are neccessary to guarantee lines are drawn
					// at every pixel. It always rounds i to a decimal, so the modulo
					// operators work
					draw = i;

					if (translated < 0) {
						draw += decimal;
					}
					else {
						draw -= decimal;
					}

					drawTranslated = translateValue(draw);
					// draw has to be incremented by 1, otherwise the drawn lines
					// are wrong by 1 pixel
					draw++;
				}

				if (drawTranslated % (10 * scaleFactor) == 0) {
					g2d.drawLine(draw, SIZE - 1, draw, SIZE - 4);
				}

				if (drawTranslated % (50 * scaleFactor) == 0) {
					g2d.drawLine(draw, SIZE - 1, draw, SIZE - 7);
				}

				if (drawTranslated % (100 * scaleFactor) == 0) {
					g2d.drawLine(draw, SIZE - 1, draw, SIZE - 11);
					int value = (int) (drawTranslated / scaleFactor);
					String textValue = Integer.toString(value);

					if (scaleFactor < 0.06) {
						if (value % 5000 == 0) {
							g2d.drawString(textValue, value == 0 ? draw - 2 : draw - 8, 9);
						}
					}
					else if ((draw - drawOld) < 31) {
						if (value % 500 == 0) {
							g2d.drawString(textValue, value == 0 ? draw - 2 : draw - 8, 9);
						}
					}
					else {
						g2d.drawString(textValue, value == 0 ? draw - 2 : draw - 8, 9);
					}

					drawOld = draw;
				}
			}
		}

		drawOld = 0;

		if (orientation == Orientation.VERTICAL) {
			// base line
			g2d.drawLine(
					SIZE - 1, 0,
					SIZE - 1, getHeight());

			// Rotate the font for vertical axis
			AffineTransform fontAT = new AffineTransform();
			fontAT.rotate(270 * java.lang.Math.PI / 180);
			Font font = g2d.getFont().deriveFont(fontAT);
			g2d.setFont(font);

			for (int i = drawHere.y; i < getHeight(); i += 10) {
				translated = translateValue(i);
				translatedAsString = Integer.toString(translated);
				lastIndex = translatedAsString.substring(translatedAsString.length() - 1);
				int decimal = Integer.parseInt(lastIndex);

				{
					// These steps are neccessary to guarantee lines are drawn
					// at every pixel. It always rounds i to a decimal, so the modulo
					// operators work
					draw = i;

					if (translated < 0) {
						draw += decimal;
					}
					else {
						draw -= decimal;
					}

					drawTranslated = translateValue(draw);
					draw++;
				}

				if (drawTranslated % (10 * scaleFactor) == 0) {
					g2d.drawLine(SIZE - 1, draw, SIZE - 4, draw);
				}

				if (drawTranslated % (50 * scaleFactor) == 0) {
					g2d.drawLine(SIZE - 1, draw, SIZE - 7, draw);
				}

				if (drawTranslated % (100 * scaleFactor) == 0) {
					g2d.drawLine(SIZE - 1, draw, SIZE - 11, draw);
					int value = -(int) (drawTranslated / scaleFactor);
					String textValue = Integer.toString(value);
				
					if (scaleFactor < 0.06) {
						if (value % 5000 == 0) {
							g2d.drawString(textValue, 9, value == 0 ? draw + 2 : draw + 8);
						}
					}
					else if ((draw - drawOld) < 31) {
						if (value % 500 == 0) {
							g2d.drawString(textValue, 9, value == 0 ? draw + 2 : draw + 8);
						}
					}
					else {
						g2d.drawString(textValue, 9, value == 0 ? draw + 2 : draw + 8);
					}

					drawOld = draw;
				}
			}
		}
	}

	/**
	 * Returns a translated value, considering current translation of the view.
	 *
	 * @param i The value.
	 * @return The translated value.
	 */
	private int translateValue(int i) {
		if (orientation == Orientation.HORIZONTAL) {
			if (translation.x < 0) {
				return i + translation.x;
			}
			else {
				return i;
			}
		}
		else if (translation.y < 0) {
			return i + translation.y;
		}
		else {
			return i;
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("scaleFactor")) {
			scaleFactor = (double) evt.getNewValue();
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					switch (orientation) {
					case HORIZONTAL:
						setPreferredWidth(drawingView.getWidth());
						break;

					case VERTICAL:
						setPreferredHeight(drawingView.getHeight());
						break;
					}
				}
			});
		}
	}
}
