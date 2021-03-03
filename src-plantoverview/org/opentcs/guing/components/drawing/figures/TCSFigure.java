/**
 * (c): IML, IFAK, JHotDraw.
 *
 */
package org.opentcs.guing.components.drawing.figures;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;
import org.jhotdraw.draw.AbstractAttributedDecoratedFigure;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.geom.Geom;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.components.drawing.OpenTCSDrawingView;
import org.opentcs.guing.components.drawing.ZoomPoint;
import org.opentcs.guing.model.FigureComponent;
import org.opentcs.guing.model.ModelComponent;

/**
 * Basisimplementierung für Figures, die mit den Standardfunktionen von JHotDraw
 * nicht auskommen.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public abstract class TCSFigure
    extends AbstractAttributedDecoratedFigure {

  /**
   * The enclosing rectangle.
   */
  protected Rectangle fDisplayBox;
  /**
   * Enthält die exakte Position des Mittelpunkts der Figur
   */
  protected ZoomPoint fZoomPoint;

  /**
   * Creates a new instance.
   *
   * @param modelComponent Das Modell dieses grafischen Objekts
   */
  public TCSFigure(FigureComponent modelComponent) {
    super();
    set(FigureConstants.MODEL, modelComponent);
  }

  /**
   * @return den Zoompunkt
   */
  public ZoomPoint getZoomPoint() {
    return fZoomPoint;
  }

  /**
   * Setzt den ZoomPunkt.
   *
   * @param zoomPoint Der Zoompunkt	*
   */
  public void setZoomPoint(ZoomPoint zoomPoint) {
    fZoomPoint = zoomPoint;
  }

  /**
   * Wird beim Erzeugen eines neuen Grafik-Objekts mit dem Creation Tool
   * aufgerufen. Dabei wird auch das zugehörige Modell ge-"cloned".
   *
   * @return
   */
  @Override	// AbstractAttributedDecoratedFigure
  public TCSFigure clone() {
    try {
      TCSFigure that = (TCSFigure) super.clone();
      that.fDisplayBox = new Rectangle(fDisplayBox);
      that.setModel((FigureComponent) getModel().clone());

      return that;
    }
    catch (CloneNotSupportedException ex) {
      throw new Error("Cannot clone() unexpectedly", ex);
    }
  }

  /**
   * Returns the model object for this figure.
   *
   * @return The model object for this figure.
   */
  public FigureComponent getModel() {
    return get(FigureConstants.MODEL);
  }

  public void setModel(FigureComponent model) {
    set(FigureConstants.MODEL, model);
  }

  /**
   * Returns the enclosing rectangle.
   *
   * @return The enclosing rectangle.
   */
  public Rectangle displayBox() {
    return new Rectangle(fDisplayBox);
  }

  @Override
  public boolean figureContains(Point2D.Double p) {
    Rectangle2D.Double r2d = getBounds();
    // Grow for connectors
    Geom.grow(r2d, 10d, 10d);

    return (r2d.contains(p));
  }

  @Override
  public boolean handleMouseClick(Double p, MouseEvent evt, DrawingView drawingView) {
    // Bei Doppelclick auf eine Figur:
    // 1. Das zugehörige Objekt im Tree markieren
    // 2. Die Eigenschaften dieses Objekts im Property Panel anzeigen
    ModelComponent model = getModel();
    OpenTCSView tcsView = ((OpenTCSDrawingView) drawingView).getTCSView();
    tcsView.getTreeViewManager().selectItem(model);
    tcsView.getPropertiesComponent().setModel(model);
    // 3. Wenn <Ctrl> gedrückt, zusätzlich Popup-Dialog für Eigenschaften
    if ((evt.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) > 0) {
      tcsView.showPropertiesDialog(model);
    }

    return false;
  }
}
