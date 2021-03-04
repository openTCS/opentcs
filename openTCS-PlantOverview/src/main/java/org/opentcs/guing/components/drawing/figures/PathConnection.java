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

import com.google.inject.assistedinject.Assisted;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.Collection;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.connector.ChopEllipseConnector;
import org.jhotdraw.draw.connector.Connector;
import org.jhotdraw.draw.handle.BezierOutlineHandle;
import org.jhotdraw.draw.handle.Handle;
import org.jhotdraw.draw.liner.ElbowLiner;
import org.jhotdraw.draw.liner.Liner;
import org.jhotdraw.draw.liner.SlantedLiner;
import org.jhotdraw.geom.BezierPath;
import org.jhotdraw.xml.DOMInput;
import org.jhotdraw.xml.DOMOutput;
import org.opentcs.guing.components.drawing.course.Origin;
import org.opentcs.guing.components.drawing.figures.liner.BezierLinerControlPointHandle;
import org.opentcs.guing.components.drawing.figures.liner.TripleBezierLiner;
import org.opentcs.guing.components.drawing.figures.liner.TupelBezierLiner;
import org.opentcs.guing.components.properties.SelectionPropertiesComponent;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.type.AbstractProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.SpeedProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.tree.ComponentsTreeViewManager;
import org.opentcs.guing.model.FigureComponent;
import org.opentcs.guing.model.elements.PathModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A connection between two points.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class PathConnection
    extends SimpleLineConnection {

  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(PathConnection.class);
  /**
   * The dash pattern for locked paths.
   */
  private static final double[] LOCKED_DASH = {6.0, 4.0};
  /**
   * The dash pattern for unlocked paths.
   */
  private static final double[] UNLOCKED_DASH = {10.0, 0.0};
  /**
   * The tool tip text generator.
   */
  private final ToolTipTextGenerator textGenerator;
  /**
   * Control point 1.
   */
  private Point2D.Double cp1;
  /**
   * Control point 2.
   */
  private Point2D.Double cp2;
  /**
   * Control point 3.
   */
  private Point2D.Double cp3;
  /**
   * Control point 4.
   */
  private Point2D.Double cp4;
  /**
   * Control point 5.
   */
  private Point2D.Double cp5;

  private Origin previousOrigin;

  /**
   * Creates a new instance.
   *
   * @param componentsTreeManager The manager for the components tree view.
   * @param propertiesComponent Displays properties of the currently selected model component(s).
   * @param model The model corresponding to this graphical object.
   * @param textGenerator The tool tip text generator.
   */
  @Inject
  public PathConnection(ComponentsTreeViewManager componentsTreeManager,
                        SelectionPropertiesComponent propertiesComponent,
                        @Assisted PathModel model,
                        ToolTipTextGenerator textGenerator) {
    super(componentsTreeManager, propertiesComponent, model);
    this.textGenerator = requireNonNull(textGenerator, "textGenerator");
    resetPath();
  }

  @Override
  public PathModel getModel() {
    return (PathModel) get(FigureConstants.MODEL);
  }

  @Override
  public void updateConnection() {
    super.updateConnection();
    initializePreviousOrigin();
    updateControlPoints();
  }

  /**
   * Resets control points and connects start and end point with a straight line.
   */
  private void resetPath() {
    Point2D.Double sp = path.get(0, BezierPath.C0_MASK);
    Point2D.Double ep;
    int size = path.size();

    switch (size) {
      case 2: // DIRECT Liner: 2 Punkte (Start / Ende)
        ep = path.get(1, BezierPath.C0_MASK);
        break;
      case 3: //2 Bezier control points
        ep = path.get(2, BezierPath.C0_MASK);
        break;
      case 4: // ELBOW/Slanted: zusï¿½tzlich 2 Stï¿½tzpunkte
        ep = path.get(3, BezierPath.C0_MASK);
        break;
      case 7: // 3 Bezier control points
        ep = path.get(6, BezierPath.C0_MASK);
        break;

      default:
        LOG.warn("Path has {} points", size);
        return;
    }

    path.clear();
    path.add(new BezierPath.Node(sp));
    path.add(new BezierPath.Node(ep));
    cp1 = cp2 = cp3 = cp4 = cp5 = null;
    getModel().getPropertyPathControlPoints().markChanged();
  }

  /**
   * Bei Umwandlung von DIRECT/ELBOW/SLANTED in BEZIER-Kurve:
   * Initiale Kontrollpunkte bei 1/n, 2/n, ... der Strecke setzen.
   *
   * @param type the type of the curve
   */
  private void initControlPoints(PathModel.LinerType type) {
    Point2D.Double sp = path.get(0, BezierPath.C0_MASK);
    Point2D.Double ep;
    int size = path.size();

    switch (size) {
      case 2: // DIRECT Liner: 2 Punkte (Start / Ende)
        ep = path.get(1, BezierPath.C0_MASK);
        break;
      case 3: //2 or 3 Bezier control points
        ep = path.get(2, BezierPath.C0_MASK);
        break;
      case 4: // ELBOW/Slanted: zusï¿½tzlich 2 Stï¿½tzpunkte
        ep = path.get(3, BezierPath.C0_MASK);
        break;

      default:
        LOG.warn("Path has {} points", size);
        return;
    }

    if (sp.x != ep.x || sp.y != ep.y) {
      path.clear();
      if (type == PathModel.LinerType.BEZIER_3) { //BEZIER curve with 3 control points);
        //Add the scaled vector between start and endpoint to the startpoint
        cp1 = new Point2D.Double(sp.x + (ep.x - sp.x) * 1 / 6, sp.y + (ep.y - sp.y) * 1 / 6); //point at 1/6
        cp2 = new Point2D.Double(sp.x + (ep.x - sp.x) * 2 / 6, sp.y + (ep.y - sp.y) * 2 / 6); //point at 2/6
        cp3 = new Point2D.Double(sp.x + (ep.x - sp.x) * 3 / 6, sp.y + (ep.y - sp.y) * 3 / 6); //point at 3/6
        cp4 = new Point2D.Double(sp.x + (ep.x - sp.x) * 4 / 6, sp.y + (ep.y - sp.y) * 4 / 6); //point at 4/6
        cp5 = new Point2D.Double(sp.x + (ep.x - sp.x) * 5 / 6, sp.y + (ep.y - sp.y) * 5 / 6); //point at 5/6
        path.add(new BezierPath.Node(BezierPath.C2_MASK,
                                     sp.x, sp.y, //Current point
                                     sp.x, sp.y, //Previous point - not in use because of C2_MASK
                                     cp1.x, cp1.y)); //Next point
        //Use cp1 and cp2 to draw between sp and cp3
        path.add(new BezierPath.Node(BezierPath.C1C2_MASK,
                                     cp3.x, cp3.y, //Current point
                                     cp2.x, cp2.y, //Previous point
                                     cp4.x, cp4.y)); //Next point
        //Use cp4 and cp5 to draw between cp3 and ep
        path.add(new BezierPath.Node(BezierPath.C1_MASK,
                                     ep.x, ep.y, //Current point
                                     cp5.x, cp5.y, //Previous point
                                     ep.x, ep.y)); //Next point - not in use because of C1_MASK
      }
      else {
        cp1 = new Point2D.Double(sp.x + (ep.x - sp.x) / 3, sp.y + (ep.y - sp.y) / 3); //point at 1/3
        cp2 = new Point2D.Double(ep.x - (ep.x - sp.x) / 3, ep.y - (ep.y - sp.y) / 3); //point at 2/3
        cp3 = null;
        cp4 = null;
        cp5 = null;
        path.add(new BezierPath.Node(BezierPath.C2_MASK,
                                     sp.x, sp.y, //Current point
                                     sp.x, sp.y, //Previous point - not in use because of C2_MASK
                                     cp1.x, cp1.y)); //Next point
        path.add(new BezierPath.Node(BezierPath.C1_MASK,
                                     ep.x, ep.y, //Current point
                                     cp2.x, cp2.y, //Previous point
                                     ep.x, ep.y)); //Next point - not in use because of C1_MASK
      }

      getModel().getPropertyPathControlPoints().markChanged();
      path.invalidatePath();
    }
  }

  /**
   * Im "alten" Modell wird zu quadratischen Kurven ein Kontrollpunkt
   * gespeichert, zu kubischen Kurven zwei.
   *
   * @param cp1
   * @param cp2 Identisch mit cp1 bei quadratischen Kurven
   */
  public void addControlPoints(Point2D.Double cp1, Point2D.Double cp2) {
    this.cp1 = cp1;
    this.cp2 = cp2;
    this.cp3 = null;
    Point2D.Double sp = path.get(0, BezierPath.C0_MASK);
    Point2D.Double ep = path.get(1, BezierPath.C0_MASK);
    path.clear();
    path.add(new BezierPath.Node(BezierPath.C2_MASK,
                                 sp.x, sp.y, //Current point
                                 sp.x, sp.y, //Previous point
                                 cp1.x, cp1.y)); //Next point
    path.add(new BezierPath.Node(BezierPath.C1_MASK,
                                 ep.x, ep.y, //Current point
                                 cp2.x, cp2.y, //Previous point
                                 ep.x, ep.y)); //Next point
    //getModel().getProperty(ElementPropKeys.PATH_CONTROL_POINTS).markChanged();
  }

  /**
   * A bezier curve with three control points.
   *
   * @param cp1 Control point 1
   * @param cp2 Control point 2
   * @param cp3 Control point 3
   * @param cp4 Control point 4
   * @param cp5 Control point 5
   */
  public void addControlPoints(Point2D.Double cp1,
                               Point2D.Double cp2,
                               Point2D.Double cp3,
                               Point2D.Double cp4,
                               Point2D.Double cp5) {
    this.cp1 = cp1;
    this.cp2 = cp2;
    this.cp3 = cp3;
    this.cp4 = cp4;
    this.cp5 = cp5;
    Point2D.Double sp = path.get(0, BezierPath.C0_MASK);
    Point2D.Double ep = path.get(path.size() - 1, BezierPath.C0_MASK);
    path.clear();
    path.add(new BezierPath.Node(BezierPath.C2_MASK,
                                 sp.x, sp.y, //Current point
                                 sp.x, sp.y, //Previous point
                                 cp1.x, cp1.y)); //Next point
    //Use cp1 and cp2 to draw between sp and cp3
    path.add(new BezierPath.Node(BezierPath.C1C2_MASK,
                                 cp3.x, cp3.y, //Current point
                                 cp2.x, cp2.y, //Previous point
                                 cp4.x, cp4.y)); //Next point
    //Use cp4 and cp5 to draw between cp3 and ep
    path.add(new BezierPath.Node(BezierPath.C1_MASK,
                                 ep.x, ep.y, //Current point
                                 cp5.x, cp5.y, //Previous point
                                 cp4.x, cp4.y)); //Next point
    StringProperty sProp = getModel().getPropertyPathControlPoints();
    sProp.setText(String.format("%d,%d;%d,%d;%d,%d;%d,%d;%d,%d;",
                                (int) cp1.x, (int) cp1.y,
                                (int) cp2.x, (int) cp2.y,
                                (int) cp3.x, (int) cp3.y,
                                (int) cp4.x, (int) cp4.y,
                                (int) cp5.x, (int) cp5.y));
    sProp.markChanged();
    getModel().propertiesChanged(this);
  }

  public Point2D.Double getCp1() {
    return cp1;
  }

  public Point2D.Double getCp2() {
    return cp2;
  }

  public Point2D.Double getCp3() {
    return cp3;
  }

  public Point2D.Double getCp4() {
    return cp4;
  }

  public Point2D.Double getCp5() {
    return cp5;
  }

  @Override
  public Point2D.Double getCenter() {
    // Computes the center of the curve.
    // Approximation: Center of the control points.
    Point2D.Double p1;
    Point2D.Double p2;
    Point2D.Double pc;

    p1 = (cp1 == null ? path.get(0, BezierPath.C0_MASK) : cp1);
    p2 = (cp2 == null ? path.get(1, BezierPath.C0_MASK) : cp2);
    if (cp3 == null) {
      pc = new Point2D.Double((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
    }
    else {
      //Use cp3 for 3-bezier as center because the curve goes through it at 50%
      pc = (cp3 == null ? path.get(3, BezierPath.C0_MASK) : cp3);
    }

    return pc;
  }

  /**
   * Initializes the previous origin which is used to scale the control points of this path.
   */
  private void initializePreviousOrigin() {
    if (previousOrigin == null) {
      Origin origin = get(FigureConstants.ORIGIN);
      previousOrigin = new Origin();
      previousOrigin.setScale(origin.getScaleX(), origin.getScaleY());
    }
  }

  /**
   * Die BEZIER-Kontrollpunkte aktualisieren
   */
  public void updateControlPoints() {
    StringProperty sProp = getModel().getPropertyPathControlPoints();
    if (cp1 != null && cp2 != null) {
      if (cp3 != null) {
        cp1 = path.get(0, BezierPath.C2_MASK);
        cp2 = path.get(1, BezierPath.C1_MASK);
        cp3 = path.get(1, BezierPath.C0_MASK);
        cp4 = path.get(1, BezierPath.C2_MASK);
        cp5 = path.get(2, BezierPath.C1_MASK);
      }
      else {
        cp1 = path.get(0, BezierPath.C2_MASK);
        cp2 = path.get(1, BezierPath.C1_MASK);
      }
    }

    String sControlPoints = "";
    if (cp1 != null) {
      if (cp2 != null) {
        if (cp3 != null) {
          // Format: x1,y1;x2,y2;x3,y3;x4,y4;x5,y5
          sControlPoints = String.format("%d,%d;%d,%d;%d,%d;%d,%d;%d,%d",
                                         (int) (cp1.x),
                                         (int) (cp1.y),
                                         (int) (cp2.x),
                                         (int) (cp2.y),
                                         (int) (cp3.x),
                                         (int) (cp3.y),
                                         (int) (cp4.x),
                                         (int) (cp4.y),
                                         (int) (cp5.x),
                                         (int) (cp5.y));
        }
        else {
          // Format: x1,y1;x2,y2
          sControlPoints = String.format("%d,%d;%d,%d", (int) (cp1.x),
                                         (int) (cp1.y), (int) (cp2.x),
                                         (int) (cp2.y));
        }
      }
      else {
        // Format: x1,y1
        sControlPoints = String.format("%d,%d", (int) (cp1.x), (int) (cp1.y));
      }
    }
    sProp.setText(sControlPoints);
    invalidate();
    sProp.markChanged();
    getModel().propertiesChanged(this);
  }

  /**
   * Connects two figures with this connection.
   *
   * @param start The first figure.
   * @param end The second figure.
   */
  public void connect(LabeledPointFigure start, LabeledPointFigure end) {
    // ChopEllipseConnector zeichnet die Linienenden/Pfeilspitzen auf
    // die direkte Verbindungsgerade von Start nach End.
    // TODO: Auf Tangenten zu Controlpoints ausrichten?
    Connector compConnector = new ChopEllipseConnector();
    Connector startConnector = start.findCompatibleConnector(compConnector, true);
    Connector endConnector = end.findCompatibleConnector(compConnector, true);

    if (!canConnect(startConnector, endConnector)) {
      return;
    }

    setStartConnector(startConnector);
    setEndConnector(endConnector);

    getModel().setConnectedComponents(start.get(FigureConstants.MODEL),
                                      end.get(FigureConstants.MODEL));
  }

  /**
   * Returns the type of this path.
   *
   * @return The type of this path
   */
  public PathModel.LinerType getLinerType() {
    return (PathModel.LinerType) getModel().getPropertyPathConnType().getValue();
  }

  public void setLinerByType(PathModel.LinerType type) {
    switch (type) {
      case DIRECT:
        resetPath();
        updateLiner(null);
        break;

      case ELBOW:
        if (!(getLiner() instanceof ElbowLiner)) {
          resetPath();
          updateLiner(new ElbowLiner());
        }

        break;

      case SLANTED:
        if (!(getLiner() instanceof SlantedLiner)) {
          resetPath();
          updateLiner(new SlantedLiner());
        }

        break;

      case BEZIER:
        if (!(getLiner() instanceof TupelBezierLiner)) {
          initControlPoints(type);
          updateLiner(new TupelBezierLiner());
        }
        break;
      case BEZIER_3:
        if (!(getLiner() instanceof TripleBezierLiner)) {
          initControlPoints(type);
          updateLiner(new TripleBezierLiner());
        }
        break;
      default:
        setLiner(null);
    }
  }

  private void updateLiner(Liner newLiner) {
    setLiner(newLiner);
    fireFigureHandlesChanged();
    fireAreaInvalidated();
    updateControlPoints();
    invalidate();
    getModel().propertiesChanged(this);
  }

  private LengthProperty calculateLength() {
    try {
      LengthProperty property = getModel().getPropertyLength();

      if (property != null) {
        double length = (double) property.getValue();
        // Tbd: Wann soll die Lï¿½nge aus dem Abstand der verbundenen Punkte neu berechnet werden?
        if (length <= 0.0) {
          PointFigure start = ((LabeledPointFigure) getStartFigure()).getPresentationFigure();
          PointFigure end = ((LabeledPointFigure) getEndFigure()).getPresentationFigure();
          double startPosX = start.getModel().getPropertyModelPositionX().getValueByUnit(LengthProperty.Unit.MM);
          double startPosY = start.getModel().getPropertyModelPositionY().getValueByUnit(LengthProperty.Unit.MM);
          double endPosX = end.getModel().getPropertyModelPositionX().getValueByUnit(LengthProperty.Unit.MM);
          double endPosY = end.getModel().getPropertyModelPositionY().getValueByUnit(LengthProperty.Unit.MM);
          length = distance(startPosX, startPosY, endPosX, endPosY);
          property.setValueAndUnit(length, LengthProperty.Unit.MM);
          property.markChanged();
        }
      }

      return property;
    }
    catch (IllegalArgumentException ex) {
      LOG.error("calculateLength()", ex);
      return null;
    }
  }

  @Override
  public String getToolTipText(Point2D.Double p) {
    return textGenerator.getToolTipText(getModel());
  }

  @Override
  public Collection<Handle> createHandles(int detailLevel) {
    List<Handle> handles = new LinkedList<>();
    // see BezierFigure
    switch (detailLevel % 2) {
      case -1: // Mouse hover handles
        handles.add(new BezierOutlineHandle(this, true));
        break;

      case 0:  // Mouse clicked
        if (cp1 != null) {
          // Startpunkt: Handle nach CP2
          handles.add(new BezierLinerControlPointHandle(this, 0, BezierPath.C2_MASK));
          if (cp2 != null) {
            // Endpunkt: Handle für CP3
            handles.add(new BezierLinerControlPointHandle(this, 1, BezierPath.C1_MASK));
            if (cp3 != null) {
              // Endpunkt: Handle nach EP
              handles.add(new BezierLinerControlPointHandle(this, 2, BezierPath.C1_MASK));
            }
          }
        }

        break;

      case 1:  // double click
        // Rechteckiger Rahmen + Drehpunkt
//      TransformHandleKit.addTransformHandles(this, handles);
        handles.add(new BezierOutlineHandle(this));
        break;

      default:
    }

    return handles;
  }

  @Override
  public void lineout() {
    if (getLiner() == null) {
      path.invalidatePath();
    }
    else {
      getLiner().lineout(this);
    }
  }

  @Override
  public void write(DOMOutput out) {
    // Das bringt nichts, da bei Paste ein neues PathModel mit den Default-Parametern erzeugt wird
//    SelectionProperty pLinerType = (SelectionProperty) getModel().getProperty(ElementPropKeys.PATH_CONN_TYPE);
//    PathModel.LinerType value = (PathModel.LinerType) pLinerType.getValue();
//    out.addAttribute("liner", value.name());
    FigureComponent startModel = requireNonNull(getStartFigure().get(FigureConstants.MODEL));
    FigureComponent endModel = requireNonNull(getEndFigure().get(FigureConstants.MODEL));
    out.addAttribute("sourceName", startModel.getName());
    out.addAttribute("destName", endModel.getName());
  }

  @Override
  public void read(DOMInput in) {
    // Don't do anything, as a new PathModel with default values is created on paste.
  }

  @Override
  public void propertiesChanged(AttributesChangeEvent e) {
    if (!e.getInitiator().equals(this)) {
      setLinerByType((PathModel.LinerType) getModel().getPropertyPathConnType().getValue());
      calculateLength();
      lineout();
    }

    super.propertiesChanged(e);
  }

  @Override
  public void updateDecorations() {
    if (getModel() == null) {
      return;
    }

    set(AttributeKeys.START_DECORATION, navigableBackward() ? ARROW_BACKWARD : null);
    set(AttributeKeys.END_DECORATION, navigableForward() ? ARROW_FORWARD : null);

    // Mark locked path.
    if (Boolean.TRUE.equals(getModel().getPropertyLocked().getValue())) {
      set(AttributeKeys.STROKE_COLOR, Color.red);
      set(AttributeKeys.STROKE_DASHES, LOCKED_DASH);
    }
    else {
      set(AttributeKeys.STROKE_COLOR, Color.black);
      set(AttributeKeys.STROKE_DASHES, UNLOCKED_DASH);
    }
  }

  @Override
  public <T> void set(AttributeKey<T> key, T newValue) {
    super.set(key, newValue);
    // if the ModelComponent is set we update the decorations, because
    // properties like maxReverseVelocity could have changed
    if (key.equals(FigureConstants.MODEL)) {
      updateDecorations();
    }
  }

  @Override
  public void updateModel() {
    if (calculateLength() == null) {
      return;
    }

    getModel().getPropertyMaxVelocity().markChanged();
    getModel().getPropertyMaxReverseVelocity().markChanged();

    getModel().propertiesChanged(this);
  }

  @Override
  public void scaleModel(EventObject event) {
    if (!(event.getSource() instanceof Origin)) {
      return;
    }

    Origin origin = (Origin) event.getSource();
    if (previousOrigin.getScaleX() == origin.getScaleX()
        && previousOrigin.getScaleY() == origin.getScaleY()) {
      return;
    }

    if (isTupelBezier()) { // BEZIER
      Point2D.Double scaledControlPoint = scaleControlPoint(cp1, origin);
      path.set(0, BezierPath.C2_MASK, scaledControlPoint);
      scaledControlPoint = scaleControlPoint(cp2, origin);
      path.set(1, BezierPath.C1_MASK, scaledControlPoint);
    }
    else if (isTripleBezier()) { // BEZIER_3
      Point2D.Double scaledControlPoint = scaleControlPoint(cp1, origin);
      path.set(0, BezierPath.C2_MASK, scaledControlPoint);
      scaledControlPoint = scaleControlPoint(cp2, origin);
      path.set(1, BezierPath.C1_MASK, scaledControlPoint);
      scaledControlPoint = scaleControlPoint(cp3, origin);
      path.set(1, BezierPath.C0_MASK, scaledControlPoint);
      scaledControlPoint = scaleControlPoint(cp4, origin);
      path.set(1, BezierPath.C2_MASK, scaledControlPoint);
      path.set(2, BezierPath.C2_MASK, scaledControlPoint);
      scaledControlPoint = scaleControlPoint(cp5, origin);
      path.set(2, BezierPath.C1_MASK, scaledControlPoint);
    }

    // Remember the new scale
    previousOrigin.setScale(origin.getScaleX(), origin.getScaleY());
    updateControlPoints();
  }

  private boolean navigableForward() {
    return getModel().getPropertyMaxVelocity().getValueByUnit(SpeedProperty.Unit.MM_S) > 0.0;
  }

  private boolean navigableBackward() {
    return getModel().getPropertyMaxReverseVelocity().getValueByUnit(SpeedProperty.Unit.MM_S) > 0.0;
  }

  private Point2D.Double scaleControlPoint(Point2D.Double p, Origin newScale) {
    return new Double((p.x * previousOrigin.getScaleX()) / newScale.getScaleX(),
                      (p.y * previousOrigin.getScaleY()) / newScale.getScaleY());
  }

  private boolean isTupelBezier() {
    return cp1 != null && cp2 != null && cp3 == null && cp4 == null && cp5 == null;
  }

  private boolean isTripleBezier() {
    return cp1 != null && cp2 != null && cp3 != null && cp4 != null && cp5 != null;
  }

  @Override // LineConnectionFigure
  public PathConnection clone() {
    PathConnection clone = (PathConnection) super.clone();

    AbstractProperty pConnType = (AbstractProperty) clone.getModel().getPropertyPathConnType();
    if (getLiner() instanceof TupelBezierLiner) {
      pConnType.setValue(PathModel.LinerType.BEZIER);
    }
    else if (getLiner() instanceof TripleBezierLiner) {
      pConnType.setValue(PathModel.LinerType.BEZIER_3);
    }
    else if (getLiner() instanceof ElbowLiner) {
      pConnType.setValue(PathModel.LinerType.ELBOW);
    }
    else if (getLiner() instanceof SlantedLiner) {
      pConnType.setValue(PathModel.LinerType.SLANTED);
    }

    clone.getModel().setFigure(clone);

    return clone;
  }
}
