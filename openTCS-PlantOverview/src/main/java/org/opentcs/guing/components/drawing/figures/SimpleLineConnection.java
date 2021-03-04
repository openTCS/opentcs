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
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.EventObject;
import static java.util.Objects.requireNonNull;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.LineConnectionFigure;
import org.jhotdraw.draw.connector.Connector;
import org.jhotdraw.draw.decoration.ArrowTip;
import org.jhotdraw.geom.BezierPath;
import org.opentcs.guing.components.drawing.course.OriginChangeListener;
import org.opentcs.guing.components.properties.SelectionPropertiesComponent;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.components.tree.TreeViewManager;
import org.opentcs.guing.model.FigureComponent;
import org.opentcs.guing.model.elements.AbstractConnection;
import org.opentcs.guing.model.elements.PointModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public abstract class SimpleLineConnection
    extends LineConnectionFigure
    implements AttributesChangeListener,
               OriginChangeListener {

  protected static final AttributeKey<Color> FILL_COLOR
      = new AttributeKey<>("FillColor", Color.class);
  protected static final AttributeKey<Color> STROKE_COLOR
      = new AttributeKey<>("StrokeColor", Color.class);
  // Pfeil fï¿½r Vorwï¿½rtsfahrt: gefï¿½llt mit Stroke Color
  protected static final ArrowTip ARROW_FORWARD
      = new ArrowTip(0.35, 12.0, 11.3, true, true, true);
  // Pfeil fï¿½r Rï¿½ckwï¿½rtsfahrt: gefï¿½llt mit Fill Color
  protected static final ArrowTip ARROW_BACKWARD
      = new ArrowTip(0.35, 12.0, 11.3, true, true, false);
  private static final Logger logger
      = LoggerFactory.getLogger(SimpleLineConnection.class);

  /**
   * The manager for the components tree view.
   */
  private final TreeViewManager componentsTreeManager;
  /**
   * Displays properties of the currently selected model component(s).
   */
  private final SelectionPropertiesComponent propertiesComponent;

  /**
   * Creates a new instance.
   *
   * @param componentsTreeManager The manager for the components tree view.
   * @param propertiesComponent Displays properties of the currently selected
   * model component(s).
   * @param model The model corresponding to this graphical object.
   */
  public SimpleLineConnection(TreeViewManager componentsTreeManager,
                              SelectionPropertiesComponent propertiesComponent,
                              AbstractConnection model) {
    this.componentsTreeManager = requireNonNull(componentsTreeManager,
                                                "componentsTreeManager");
    this.propertiesComponent = requireNonNull(propertiesComponent,
                                              "propertiesComponent");

    set(FigureConstants.MODEL, model);
    initConnectionFigure();
  }

  /**
   * Wird aufgerufen, wenn das Figure gerade durch Klonen erzeugt wurde und
   * erlaubt das Durchfï¿½hren von Initialisierungen.
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
      logger.warn("", ex);
      return path.clone();
    }
  }

  /**
   * Aktualisiert die Eigenschaften des Models. Wird z.B. aufgerufen, wenn sich
   * der Maï¿½stab des Layout geï¿½ndert hat
   */
  public abstract void updateModel();

  /**
   * Scales the model coodinates accodring to changes to the layout scale.
   *
   * @param event The event containing the layout scale change.
   */
  public abstract void scaleModel(EventObject event);

  /**
   * Calculates the euclid distance between the start position and the end position.
   *
   * @param startPosX The x coordiante of the start position.
   * @param startPosY The y coordiante of the start position.
   * @param endPosX The x coordinate of the end position.
   * @param endPosY The y coordinate of the end position.
   * @return the euclid distance between start and end point rounded to the next integer.
   */
  protected double distance(double startPosX, double startPosY, double endPosX, double endPosY) {
    double dX = startPosX - endPosX;
    double dY = startPosY - endPosY;
    double dist = Math.sqrt(dX * dX + dY * dY);
    dist = Math.floor(dist + 0.5);  // round to an integer value.

    return dist;
  }

  public void updateDecorations() {
  }

  /**
   * Prï¿½ft, ob zwei Figures durch eine Strecke miteinander verbunden werden
   * kï¿½nnen. Verbunden werden dï¿½rfen nur Meldepunkte mit Stï¿½tzknoten in
   * beliebiger Kombination, nicht jedoch Meldepunkte mit Arbeitsstationen.
   *
   * @param start
   * @param end
   * @return
   * <code>true</code>, wenn eine Verbindung mï¿½glich ist, ansonsten
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
    // 1. Das zugehï¿½rige Objekt im Tree markieren
    // 2. Die Eigenschaften dieses Objekts im Property Panel anzeigen
    AbstractConnection model = getModel();
    componentsTreeManager.selectItem(model);
    propertiesComponent.setModel(model);

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
    // ist fï¿½r Strecken uninteressant
  }

  @Override // OriginChangeListener
  public void originScaleChanged(EventObject event) {
    scaleModel(event);
  }

  @Override
  public SimpleLineConnection clone() {
    try {
      SimpleLineConnection clone = (SimpleLineConnection) super.clone();
      clone.set(FigureConstants.MODEL, getModel().clone());

      return clone;
    }
    catch (CloneNotSupportedException exc) {
      // XXX Do something.
      throw new IllegalStateException("Unexpected exception encountered", exc);
    }
  }
}
