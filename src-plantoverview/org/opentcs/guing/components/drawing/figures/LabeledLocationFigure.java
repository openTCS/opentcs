/**
 * (c): IML, IFAK, JHotDraw.
 *
 */
package org.opentcs.guing.components.drawing.figures;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.Action;
import org.jhotdraw.xml.DOMInput;
import org.jhotdraw.xml.DOMOutput;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.guing.application.action.edit.CopyAction;
import org.opentcs.guing.application.action.edit.CutAction;
import org.opentcs.guing.application.action.edit.DuplicateAction;
import org.opentcs.guing.application.action.edit.PasteAction;
import org.opentcs.guing.components.drawing.ZoomPoint;
import org.opentcs.guing.components.drawing.course.Origin;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.type.CoordinateProperty;
import org.opentcs.guing.components.properties.type.KeyValueProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.ModelAttribute;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.model.AbstractFigureComponent;
import org.opentcs.guing.model.FigureComponent;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.LocationModel;

/**
 * LabeledLocationFigure: LocationFigure mit zugehörigem Label, das mit der
 * Figur bewegt wird.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class LabeledLocationFigure
    extends LabeledFigure {

  /**
   * DOM support
   */
  public LabeledLocationFigure() {
    LocationFigure lf = new LocationFigure(new LocationModel());
    setPresentationFigure(lf);
  }

  /**
   * @param figure
   */
  public LabeledLocationFigure(LocationFigure figure) {
    setPresentationFigure(figure);
  }

  /**
   * Liefert das Figure-Objekt.
   *
   * @return
   */
  public LocationFigure getLocationFigure() {
    return (LocationFigure) getPresentationFigure();
  }

  @Override	// AbstractFigure
  public String getToolTipText(Point2D.Double p) {
    LocationFigure lf = (LocationFigure) getPresentationFigure();
    StringBuilder sb = new StringBuilder("<html>Location ");
    sb.append("<b>").append(lf.getModel().getName()).append("</b>");
    // Show miscellaneous properties in tooltip
    KeyValueSetProperty property = (KeyValueSetProperty) lf.getModel().getProperty(ModelComponent.MISCELLANEOUS);
    Iterator<KeyValueProperty> items = property.getItems().iterator();

    while (items.hasNext()) {
      KeyValueProperty next = items.next();
      String key = next.getKey();
      String value = next.getValue();
      sb.append("<br>").append(key).append(": ").append(value);
    }

    sb.append("</html");

    return sb.toString();
  }

  @Override	// GraphicalCompositeFigure
  public LabeledLocationFigure clone() {
    // Do NOT clone the label here.
    LabeledLocationFigure that = (LabeledLocationFigure) super.clone();

    if (that.getChildCount() > 0) {
      that.removeChild(0);
    }

    that.fLabel = null;
    LocationFigure thatPresentationFigure = (LocationFigure) that.getPresentationFigure();
    thatPresentationFigure.addFigureListener(that.eventHandler);
    // Force loading of the symbol bitmap
    thatPresentationFigure.propertiesChanged(null);

    return that;
  }

  @Override	// GraphicalCompositeFigure
  public void read(DOMInput in) throws IOException {
    double x = in.getAttribute("x", 0d);
    double y = in.getAttribute("y", 0d);
    setBounds(new Point2D.Double(x, y), new Point2D.Double(x, y));
  }

  @Override	// GraphicalCompositeFigure
  public void write(DOMOutput out) throws IOException {
    LocationFigure lf = (LocationFigure) getPresentationFigure();
    out.addAttribute("x", lf.getZoomPoint().getX());
    out.addAttribute("y", lf.getZoomPoint().getY());
    out.addAttribute("name", get(FigureConstants.MODEL).getName());
  }

  @Override // AbstractFigure
  public Collection<Action> getActions(Point2D.Double p) {
    LinkedList<Action> editOptions = new LinkedList<>();
//    editOptions.add(new CutAction());
//    editOptions.add(new CopyAction());
//    editOptions.add(new PasteAction());
//    editOptions.add(new DuplicateAction());

    return editOptions;
  }

  @Override	// AttributesChangeListener
  public void propertiesChanged(AttributesChangeEvent event) {
    if (event.getInitiator().equals(this)) {
      return;
    }

    // Move the figure if the model coordinates have been changed in the
    // Properties panel
    Origin origin = (Origin) get(FigureConstants.ORIGIN);

    if (origin != null) {
      LocationFigure lf = (LocationFigure) getPresentationFigure();
      StringProperty xLayout = (StringProperty) lf.getModel().getProperty(ElementPropKeys.LOC_POS_X);
      StringProperty yLayout = (StringProperty) lf.getModel().getProperty(ElementPropKeys.LOC_POS_Y);

      if (xLayout.hasChanged() || yLayout.hasChanged()) {
        getLabel().willChange();
        Point2D exact = origin.calculatePixelPositionExactly(xLayout, yLayout);
        double scale = lf.getZoomPoint().scale();
        double xNew = exact.getX() / scale;
        double yNew = exact.getY() / scale;
        Point2D.Double anchor = new Point2D.Double(xNew, yNew);
        setBounds(anchor, anchor);
        getLabel().changed();
      }
    }

    // Update the image of the actual Location type
    ((LocationFigure) getPresentationFigure()).propertiesChanged(event);
    // Auch das Label aktualisieren
    fireFigureChanged();
  }

  @Override // LabeledFigure
  public void updateModel() {
    Origin origin = (Origin) get(FigureConstants.ORIGIN);
    LocationFigure lf = (LocationFigure) getPresentationFigure();
    FigureComponent model = lf.getModel();
    CoordinateProperty cpx = (CoordinateProperty) model.getProperty(LocationModel.MODEL_X_POSITION);
    CoordinateProperty cpy = (CoordinateProperty) model.getProperty(LocationModel.MODEL_Y_POSITION);
    // Schreibt die aktuellen Modell-Koordinaten in die Properties
    if ((double) cpx.getValue() == 0.0 && (double) cpy.getValue() == 0.0) {
      // Koordinaten nur einmal beim Erzeugen aus Layout übernehmen
      origin.calculateRealPosition(lf.center(), cpx, cpy);
      cpx.markChanged();
      cpy.markChanged();
    }
    // Schreibt die aktuellen Layout-Koordinaten in die Properties
    ZoomPoint zoomPoint = lf.getZoomPoint();
    // Wenn die Figure gerade gelöscht wurde, kann der Origin schon null sein
    if (zoomPoint != null && origin != null) {
      StringProperty sp = (StringProperty) model.getProperty(ElementPropKeys.LOC_POS_X);
      String sValue = sp.getText();
      int oldValue;

      if (sValue == null || sValue.isEmpty()) {
        oldValue = 0;
      }
      else {
        oldValue = (int) Double.parseDouble(sp.getText());
      }

      int newValue = (int) (zoomPoint.getX() * origin.getScaleX());

      if (newValue != oldValue) {
        sp.setText(String.format("%d", newValue));
        sp.markChanged();
      }

      sp = (StringProperty) model.getProperty(ElementPropKeys.LOC_POS_Y);

      if (sValue == null || sValue.isEmpty()) {
        oldValue = 0;
      }
      else {
        oldValue = (int) Double.parseDouble(sp.getText());
      }

      newValue = (int) (-zoomPoint.getY() * origin.getScaleY());	// Vorzeichen!

      if (newValue != oldValue) {
        sp.setText(String.format("%d", newValue));
        sp.markChanged();
      }
    }

    model.propertiesChanged(this);
    // Auch das Label aktualisieren
    fireFigureChanged();
  }
}
