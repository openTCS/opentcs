/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.model.elements;

import org.opentcs.guing.model.AbstractFigureComponent;

/**
 * Eine grafische Komponente mit illustrierender Wirkung aber ohne direkte
 * Beziehung zum Fahrkurs. Hierzu gehören beispielsweise Text, Linie, Rechteck,
 * Kreis und Bild.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class OtherGraphicalElement
    extends AbstractFigureComponent {

  /**
   * Creates a new instance of OtherGraphicalElement.
   */
  public OtherGraphicalElement() {
    super();
  }

  @Override
  public String getDescription() {

////		if (getFigure() instanceof TransformableDecorator) {
////			TransformableDecorator decorator = (TransformableDecorator) getFigure();
////
////			if (decorator.getDecoratedFigure() instanceof RectangleFigure) {
////				return "Rechteck";
////			}
////			if (decorator.getDecoratedFigure() instanceof EllipseFigure) {
////				return "Ellipse";
////			}
////			if (decorator.getDecoratedFigure() instanceof LineFigure) {
////				return "Linie";
////			}
////		}
////
////		if (getFigure() instanceof TextFigure) {
////			return "Text";
////		}
    return "Grafisches Objekt";
  }

  @Override
  public int compareTo(AbstractFigureComponent o) {
    return 0;
  }
}
