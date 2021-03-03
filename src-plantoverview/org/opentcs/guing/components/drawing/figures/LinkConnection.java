/**
 * (c): IML, IFAK, JHotDraw.
 *
 */
package org.opentcs.guing.components.drawing.figures;

import java.awt.BasicStroke;
import java.awt.geom.Point2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.connector.ChopEllipseConnector;
import org.jhotdraw.draw.connector.Connector;
import org.jhotdraw.xml.DOMInput;
import org.jhotdraw.xml.DOMOutput;
import org.opentcs.guing.model.FigureComponent;
import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.PointModel;

/**
 * Eine gestrichelte Linie, die einen Meldepunkt mit einer Übergabestation oder
 * einer Batterieladestation verbindet. Eine SimpleLineConnection besitzt keine
 * Pfeilspitzen.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class LinkConnection
    extends SimpleLineConnection {

  /**
   * Creates a new instance.
   *
   * @param model The corresponding model object.
   */
  public LinkConnection(LinkModel model) {
    super(model);
    double dash[] = {5.0, 5.0};
    set(AttributeKeys.START_DECORATION, null);
    set(AttributeKeys.END_DECORATION, null);
    set(AttributeKeys.STROKE_WIDTH, 1.0);
    set(AttributeKeys.STROKE_CAP, BasicStroke.CAP_BUTT);
    set(AttributeKeys.STROKE_JOIN, BasicStroke.JOIN_MITER);
    set(AttributeKeys.STROKE_MITER_LIMIT, 1.0);
    set(AttributeKeys.STROKE_DASHES, dash);
    set(AttributeKeys.STROKE_DASH_PHASE, 0.0);
  }

  public LinkConnection() {
    this(new LinkModel());
  }

  @Override
  public LinkModel getModel() {
    return (LinkModel) get(FigureConstants.MODEL);
  }

  /**
   * Verknüpft zwei Figure-Objekte durch diese Verbindung.
   *
   * @param point das erste Figure-Objekt
   * @param location das zweite Figure-Objekt
   */
  public void connect(LabeledPointFigure point, LabeledLocationFigure location) {
    Connector compConnector = new ChopEllipseConnector();
    Connector startConnector = point.findCompatibleConnector(compConnector, true);
    Connector endConnector = location.findCompatibleConnector(compConnector, true);

    if (!canConnect(startConnector, endConnector)) {
      return;
    }

    setStartConnector(startConnector);
    setEndConnector(endConnector);
  }

  @Override // AbstractFigure
  public String getToolTipText(Point2D.Double p) {
    StringBuilder sb = new StringBuilder("<html>Link ");
    sb.append("<b>").append(getModel().getName()).append("</b>");
    sb.append("</html>");

    return sb.toString();
  }

  @Override // SimpleLineConnection
  public boolean canConnect(Connector start, Connector end) {
    FigureComponent modelStart = start.getOwner().get(FigureConstants.MODEL);
    FigureComponent modelEnd = end.getOwner().get(FigureConstants.MODEL);

    if (modelStart == null || modelEnd == null) {
      return false;
    }

    if ((modelStart instanceof PointModel) && modelEnd instanceof LocationModel) {
      LocationModel location = (LocationModel) modelEnd;
      PointModel point = (PointModel) modelStart;

      return !location.hasConnectionTo(point);
    }

    if (modelStart instanceof LocationModel && (modelEnd instanceof PointModel)) {
      LocationModel location = (LocationModel) modelStart;
      PointModel point = (PointModel) modelEnd;

      return !location.hasConnectionTo(point);
    }

    return false;
  }

  @Override
  public void write(DOMOutput out) {
    out.addAttribute("sourceName", getStartFigure().get(FigureConstants.MODEL).getName());
    out.addAttribute("destName", getEndFigure().get(FigureConstants.MODEL).getName());
  }

  @Override
  public void read(DOMInput in) {
  }

  @Override // SimpleLineConnection
  public void updateModel() {
    // Bei Link Connection ist nichts zu tun
  }

  @Override // LineConnectionFigure
  public void updateConnection() {
    super.updateConnection();
  }

  @Override // LineConnectionFigure
  public LinkConnection clone() {
    try {
      LinkModel linkModel = (LinkModel) getModel().clone();
      LinkConnection clone = new LinkConnection(linkModel);
      clone.initConnectionFigure();
      
      return clone;
    }
    catch (CloneNotSupportedException ex) {
      Logger.getLogger(LinkConnection.class.getName()).log(Level.SEVERE, "Clone not supported: {0}", getModel().getName());
      return null;
    }
  }
}
