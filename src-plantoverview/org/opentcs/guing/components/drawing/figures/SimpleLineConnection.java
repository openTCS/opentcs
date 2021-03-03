/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.drawing.figures;

import java.awt.Color;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.connector.Connector;
import org.jhotdraw.draw.decoration.ArrowTip;
import org.jhotdraw.geom.BezierPath;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.components.drawing.OpenTCSDrawingView;
import org.opentcs.guing.components.drawing.ZoomPoint;
import org.opentcs.guing.components.drawing.course.OriginChangeListener;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.model.FigureComponent;
import org.opentcs.guing.model.elements.AbstractConnection;
import org.opentcs.guing.model.elements.PointModel;

/**
 * 
 * @author Heinz Huber (Fraunhofer IML)
 */
public abstract class SimpleLineConnection
    extends org.jhotdraw.draw.LineConnectionFigure
    implements AttributesChangeListener, OriginChangeListener {

  private static final Logger logger
      = Logger.getLogger(SimpleLineConnection.class.getName());
  /**
   * The attributes of a figure. Each figure can have an open ended set of
   * attributes. Attributes are identified by name.
   *
   * @see #getAttribute
   * @see #setAttribute
   */
  protected final static AttributeKey<Color> FILL_COLOR
      = new AttributeKey<>("FillColor", Color.class);
  protected final static AttributeKey<Color> STROKE_COLOR
      = new AttributeKey<>("StrokeColor", Color.class);
  // Pfeil für Vorwärtsfahrt: gefüllt mit Stroke Color
  protected final static ArrowTip ARROW_FORWARD
      = new ArrowTip(0.35, 12.0, 11.3, true, true, true);
  // Pfeil für Rückwärtsfahrt: gefüllt mit Fill Color
  protected final static ArrowTip ARROW_BACKWARD
      = new ArrowTip(0.35, 12.0, 11.3, true, true, false);

  /**
   * Creates a new instance.
   *
   * @param figureComponent The model object.
   */
  public SimpleLineConnection(FigureComponent figureComponent) {
    set(FigureConstants.MODEL, figureComponent);
    initConnectionFigure();
  }
  
  /**
   * Creates a new instance.
   */
  public SimpleLineConnection() {
  }

  /**
   * Wird aufgerufen, wenn das Figure gerade durch Klonen erzeugt wurde und
   * erlaubt das Durchführen von Initialisierungen.
   */
  protected final void initConnectionFigure() {
    updateDecorations();
  }

  /**
   * Liefert das Datenmodell zu diesem Figure.
   *
   * @return das Datenmodell
   */
  public AbstractConnection getModel() {
    return (AbstractConnection) get(FigureConstants.MODEL);
  }

  /**
   *
   * @return
   */
  public Shape getShape() {
    return path;
  }

  @Override // BezierFigure
  protected BezierPath getCappedPath() {
    // Workaround wegen NullPointerException in BezierFigure.getCappedPath()
    try {
      return super.getCappedPath();
    }
    catch (NullPointerException ex) {
      logger.log(Level.WARNING, null, ex);
      return path.clone();
    }
  }

  /**
   * Aktualisiert die Eigenschaften des Models. Wird z.B. aufgerufen, wenn sich
   * der Maßstab des Layout geändert hat
   */
  public abstract void updateModel();

  /**
   *
   * @param p1
   * @param p2
   * @return
   */
  protected double distance(ZoomPoint p1, ZoomPoint p2) {
    double dX = p1.getX() - p2.getX();
    double dY = p1.getY() - p2.getY();
    double dist = Math.sqrt(dX * dX + dY * dY);
    // TODO: ScaleX, ScaleY berücksichtigen

//		path.validatePath();
//		dist = path.getLengthOfPath(1.5);	// TEST! Welcher Faktor?
    dist = Math.floor(dist + 0.5);	// Auf ganze Zahl runden

    return dist;
  }

  /**
   * Is overridden in PathConnection.
   */
  public void updateDecorations() {
  }

  /**
   * Prüft, ob zwei Figures durch eine Strecke miteinander verbunden werden
   * können. Verbunden werden dürfen nur Meldepunkte mit Stützknoten in
   * beliebiger Kombination, nicht jedoch Meldepunkte mit Arbeitsstationen.
   *
   * @param start
   * @param end
   * @return
   * <code>true</code>, wenn eine Verbindung möglich ist, ansonsten
   * <code>false</code>
   */
  @Override // LineConnectionFigure
  public boolean canConnect(Connector start, Connector end) {
    FigureComponent modelStart = start.getOwner().get(FigureConstants.MODEL);
    FigureComponent modelEnd = end.getOwner().get(FigureConstants.MODEL);

    if (modelStart == null || modelEnd == null || modelEnd == modelStart) {
      return false;
    }

    if ((modelStart instanceof PointModel) && (modelEnd instanceof PointModel)) {
      PointModel startPoint = (PointModel) modelStart;
      PointModel endPoint = (PointModel) modelEnd;
      return !startPoint.hasConnectionTo(endPoint);
    }
    else {
      return false;
    }
  }

  @Override // LineConnectionFigure
  protected void handleConnect(Connector start, Connector end) {
    if (start != null && end != null) {
      FigureComponent startModel = start.getOwner().get(FigureConstants.MODEL);
      FigureComponent endModel = end.getOwner().get(FigureConstants.MODEL);
      getModel().setConnectedComponents(startModel, endModel);
      updateModel();
    }
  }

  @Override // LineConnectionFigure
  protected void handleDisconnect(Connector start, Connector end) {
    super.handleDisconnect(start, end);

    getModel().removingConnection();
  }

  @Override // LineConnectionFigure
  public boolean handleMouseClick(Point2D.Double p, MouseEvent evt, DrawingView drawingView) {
    // Bei Doppelclick auf eine Strecke:
    // 1. Das zugehörige Objekt im Tree markieren
    // 2. Die Eigenschaften dieses Objekts im Property Panel anzeigen
    AbstractConnection model = getModel();
    OpenTCSView tcsView = ((OpenTCSDrawingView) drawingView).getTCSView();
    tcsView.getTreeViewManager().selectItem(model);
    tcsView.getPropertiesComponent().setModel(model);
    // Wenn <Ctrl> gedrückt, 3. zusätzlich Popup-Dialog für Eigenschaften
    if ((evt.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) > 0) {
      tcsView.showPropertiesDialog(model);
    }

    return false;
  }

  @Override // AttributesChangeListener
  public void propertiesChanged(AttributesChangeEvent e) {
    if (!e.getInitiator().equals(this)) {
      updateDecorations();
      fireFigureChanged(getDrawingArea());
    }
  }

  @Override // OriginChangeListener
  public void originLocationChanged(EventObject event) {
    // ist für Strecken uninteressant
  }

  @Override // OriginChangeListener
  public void originScaleChanged(EventObject event) {
    updateModel();
  }
}
