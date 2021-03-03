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
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.Collection;
import java.util.LinkedList;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import net.engio.mbassy.bus.MBassador;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.ConnectionFigure;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.connector.ChopEllipseConnector;
import org.jhotdraw.draw.connector.Connector;
import org.jhotdraw.draw.decoration.LineDecoration;
import org.jhotdraw.draw.handle.BezierOutlineHandle;
import org.jhotdraw.draw.handle.Handle;
import org.jhotdraw.draw.liner.ElbowLiner;
import org.jhotdraw.draw.liner.SlantedLiner;
import org.jhotdraw.geom.BezierPath;
import org.jhotdraw.xml.DOMInput;
import org.jhotdraw.xml.DOMOutput;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.guing.components.drawing.figures.liner.BezierLiner;
import org.opentcs.guing.components.drawing.figures.liner.BezierLinerControlPointHandle;
import org.opentcs.guing.components.properties.SelectionPropertiesComponent;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.type.BooleanProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.components.properties.type.SpeedProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.tree.ComponentsTreeViewManager;
import org.opentcs.guing.event.PathLockedEvent;
import org.opentcs.guing.model.FigureComponent;
import org.opentcs.guing.model.elements.PathModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Eine Verbindung zwischen zwei Punkten.
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
   * The application's event bus.
   */
  private final MBassador<Object> eventBus;
  /**
   * Control point 1.
   */
  private Point2D.Double cp1;
  /**
   * Control point 2.
   */
  private Point2D.Double cp2;

  /**
   * Creates a new instance.
   *
   * @param componentsTreeManager The manager for the components tree view.
   * @param propertiesComponent Displays properties of the currently selected
   * model component(s).
   * @param eventBus The application's event bus.
   * @param model The model corresponding to this graphical object.
   */
  @Inject
  public PathConnection(ComponentsTreeViewManager componentsTreeManager,
                        SelectionPropertiesComponent propertiesComponent,
                        MBassador<Object> eventBus,
                        @Assisted PathModel model) {
    super(componentsTreeManager, propertiesComponent, model);
    this.eventBus = requireNonNull(eventBus, "eventBus");
    resetPath();
  }

  @Override
  public PathModel getModel() {
    return (PathModel) get(FigureConstants.MODEL);
  }

  /**
   * Kontrollpunkte lï¿½schen; Anfang und Ende durch eine Gerade verbinden.
   */
  private void resetPath() {
    Point2D.Double sp = path.get(0, BezierPath.C0_MASK);
    Point2D.Double ep;
    int size = path.size();

    switch (size) {
      case 2: // DIRECT Liner: 2 Punkte (Start / Ende)
        ep = path.get(1, BezierPath.C0_MASK);
        break;

      case 4: // ELBOW/Slanted: zusï¿½tzlich 2 Stï¿½tzpunkte
        ep = path.get(3, BezierPath.C0_MASK);
        break;

      default:
        LOG.warn("Path has {} points", size);
        return;
    }

    path.clear();
    path.add(new BezierPath.Node(sp));
    path.add(new BezierPath.Node(ep));
    cp1 = cp2 = null;
    getModel().getProperty(ElementPropKeys.PATH_CONTROL_POINTS).markChanged();
  }

  @Override
  public void updateConnection() {
    super.updateConnection();
    updateControlPoints();
  }

  /**
   * Bei Umwandlung von DIRECT/ELBOW/SLANTED in BEZIER-Kurve:
   * Initiale Kontrollpunkte bei 1/3 und 2/3 der Strecke setzen.
   */
  private void initControlPoints() {
    Point2D.Double sp = path.get(0, BezierPath.C0_MASK);
    Point2D.Double ep;
    int size = path.size();

    switch (size) {
      case 2: // DIRECT Liner: 2 Punkte (Start / Ende)
        ep = path.get(1, BezierPath.C0_MASK);
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
      cp1 = new Point2D.Double(sp.x + (ep.x - sp.x) / 3, sp.y + (ep.y - sp.y) / 3);
      cp2 = new Point2D.Double(ep.x - (ep.x - sp.x) / 3, ep.y - (ep.y - sp.y) / 3);
      path.add(new BezierPath.Node(BezierPath.C2_MASK, sp.x, sp.y, sp.x, sp.y, cp1.x, cp1.y));
      path.add(new BezierPath.Node(BezierPath.C1_MASK, ep.x, ep.y, cp2.x, cp2.y, ep.x, ep.y));
      getModel().getProperty(ElementPropKeys.PATH_CONTROL_POINTS).markChanged();
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
    Point2D.Double sp = path.get(0, BezierPath.C0_MASK);
    Point2D.Double ep = path.get(1, BezierPath.C0_MASK);
    path.clear();
    path.add(new BezierPath.Node(BezierPath.C2_MASK, sp.x, sp.y, sp.x, sp.y, cp1.x, cp1.y));
    path.add(new BezierPath.Node(BezierPath.C1_MASK, ep.x, ep.y, cp2.x, cp2.y, ep.x, ep.y));
    getModel().getProperty(ElementPropKeys.PATH_CONTROL_POINTS).markChanged();
  }

  public Point2D.Double getCp1() {
    return cp1;
  }

  public Point2D.Double getCp2() {
    return cp2;
  }

  @Override // BezierFigure
  public Point2D.Double getCenter() {
    // Computes the center of the curve.
    // Approximation: Center of the control points.
    Point2D.Double p1;
    Point2D.Double p2;
    Point2D.Double pc;

    p1 = (cp1 == null ? path.get(0, BezierPath.C0_MASK) : cp1);
    p2 = (cp2 == null ? path.get(1, BezierPath.C0_MASK) : cp2);
    pc = new Point2D.Double((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);

    return pc;
  }

  /**
   * Die BEZIER-Kontrollpunkte aktualisieren
   */
  public void updateControlPoints() {
    if (cp1 != null) {
      cp1 = path.get(0, BezierPath.C2_MASK);
    }

    if (cp2 != null) {
      cp2 = path.get(1, BezierPath.C1_MASK);
    }

    String sControlPoints = "";
    if (cp1 != null) {
      if (cp2 != null) {
        // Format: x1,y1;x2,y2
        sControlPoints = String.format("%d,%d;%d,%d", (int) (cp1.x),
                                       (int) (cp1.y), (int) (cp2.x),
                                       (int) (cp2.y));
      }
      else {
        // Format: x1,y1
        sControlPoints = String.format("%d,%d", (int) (cp1.x), (int) (cp1.y));
      }
    }

    StringProperty sProp
        = (StringProperty) getModel().getProperty(ElementPropKeys.PATH_CONTROL_POINTS);
    sProp.setText(sControlPoints);
    sProp.markChanged();
  }

  /**
   * Verknï¿½pft zwei Figure-Objekte durch diese Verbindung.
   *
   * @param start das erste Figure-Objekt
   * @param end das zweite Figure-Objekt
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
   *
   * @param name
   */
  public void setLinerByName(String name) {
    PathModel.LinerType type = PathModel.LinerType.valueOf(name);
    setLinerByType(type);
  }

  /**
   *
   * @param type
   */
  public void setLinerByType(PathModel.LinerType type) {
    switch (type) {
      case DIRECT:
        setLiner(null);
        resetPath();
        break;

      case ELBOW:
        if (!(getLiner() instanceof ElbowLiner)) {
          setLiner(new ElbowLiner());
          resetPath();
        }

        break;

      case SLANTED:
        if (!(getLiner() instanceof SlantedLiner)) {
          setLiner(new SlantedLiner());
          resetPath();
        }

        break;

      case BEZIER:
        if (!(getLiner() instanceof BezierLiner)) {
          setLiner(new BezierLiner());
        }

        if (cp1 == null) {
          initControlPoints();
        }

        break;

      default:
        setLiner(null);
    }
  }

  /**
   *
   * @return
   */
  private LengthProperty calculateLength() {
    try {
      LengthProperty property = (LengthProperty) getModel().getProperty(PathModel.LENGTH);

      if (property != null) {
        double length = (double) property.getValue();
        // Tbd: Wann soll die Lï¿½nge aus dem Abstand der verbundenen Punkte neu berechnet werden?
        if (length <= 0.0) {
          PointFigure start = ((LabeledPointFigure) getStartFigure()).getPresentationFigure();
          PointFigure end = ((LabeledPointFigure) getEndFigure()).getPresentationFigure();
          length = distance(start.getZoomPoint(), end.getZoomPoint());
          LengthProperty.Unit unit = property.getUnit();
          property.setValueAndUnit(length, LengthProperty.Unit.MM);
          property.convertTo(unit);
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

  @Override  // AbstractFigure
  public String getToolTipText(Point2D.Double p) {
    StringBuilder sb = new StringBuilder("<html>Path ");
    sb.append("<b>").append(getModel().getName()).append("</b>");
    sb.append("</html>");

    return sb.toString();
  }

  @Override // BezierFigure
  public Connector findConnector(Double p, ConnectionFigure prototype) {
    Connector connector = super.findConnector(p, prototype);

    return connector;
  }

  @Override  // LineConnectionFigure
  public Collection<Handle> createHandles(int detailLevel) {
    LinkedList<Handle> handles = new LinkedList<>();
    // see BezierFigure
    switch (detailLevel % 2) {
      case -1: // Mouse hover handles
        handles.add(new BezierOutlineHandle(this, true));
        break;

      case 0:  // Mouse clicked
//      handles.add(new BezierLinerHandle(this));

        if (cp1 != null) {
          // Startpunkt: Handle nach CP2
          handles.add(new BezierLinerControlPointHandle(this, 0, 2));

          if (cp2 != null) {
            // Endpunkt: Handle nach CP1
            handles.add(new BezierLinerControlPointHandle(this, 1, 1));
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

  @Override  // LineConnectionFigure
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
    // Das bringt nichts, da bei Paste ein neues PathModel mit den Default-Parametern erzeugt wird
//    String sLinerType = PathModel.LinerType.DIRECT.name();
//    sLinerType = in.getAttribute("liner", sLinerType);
//    setLinerByName(sLinerType);
  }

  @Override  // SimpleLineConnection
  public boolean handleMouseClick(Point2D.Double p, MouseEvent evt, DrawingView drawingView) {
    boolean ret = super.handleMouseClick(p, evt, drawingView);

    return ret;
  }

  @Override  // SimpleLineConnection
  public void propertiesChanged(AttributesChangeEvent e) {
    if (!e.getInitiator().equals(this)) {
      SelectionProperty pType = (SelectionProperty) getModel().getProperty(ElementPropKeys.PATH_CONN_TYPE);
      PathModel.LinerType type = (PathModel.LinerType) pType.getValue();
      setLinerByType(type);
      // Lï¿½nge neu berechnen
      calculateLength();
      lineout();
    }

    super.propertiesChanged(e);
  }

  /**
   * Updates the arrows.
   */
  @Override  // SimpleLineConnection
  public void updateDecorations() {
    final double[] lockedDash = {6.0, 4.0};
    final double[] unlockedDash = {10.0, 0.0};

    if (getModel() != null) {
      LineDecoration startDecoration = null;
      LineDecoration endDecoration = null;
      SpeedProperty pSpeed = (SpeedProperty) getModel().getProperty(PathModel.MAX_VELOCITY);

      if ((double) pSpeed.getValue() > 0.0) {
        endDecoration = ARROW_FORWARD;
      }

      pSpeed = (SpeedProperty) getModel().getProperty(PathModel.MAX_REVERSE_VELOCITY);

      if ((double) pSpeed.getValue() > 0.0) {
        startDecoration = ARROW_BACKWARD;
      }

      set(AttributeKeys.START_DECORATION, startDecoration);
      set(AttributeKeys.END_DECORATION, endDecoration);
      // Gesperrte Strecken markieren
      BooleanProperty pLocked = (BooleanProperty) getModel().getProperty(PathModel.LOCKED);
      if (pLocked.hasChanged()) {
        eventBus.publish(new PathLockedEvent(this));
      }

      if (pLocked.getValue() instanceof Boolean) {
        boolean locked = (boolean) (pLocked).getValue();

        if (locked) {
          set(AttributeKeys.STROKE_COLOR, Color.red);
          set(AttributeKeys.STROKE_DASHES, lockedDash);
        }
        else {
          set(AttributeKeys.STROKE_COLOR, Color.black);
          set(AttributeKeys.STROKE_DASHES, unlockedDash);
        }
      }
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

  @Override  // SimpleLineConnection
  public void updateModel() {
    if (calculateLength() == null) {
      return;
    }

    SpeedProperty pSpeed = (SpeedProperty) getModel().getProperty(PathModel.MAX_VELOCITY);
    pSpeed.markChanged();
    pSpeed = (SpeedProperty) getModel().getProperty(PathModel.MAX_REVERSE_VELOCITY);
    pSpeed.markChanged();

    getModel().propertiesChanged(this);
  }

  @Override // LineConnectionFigure
  public PathConnection clone() {
    PathConnection clone = (PathConnection) super.clone();
    SelectionProperty pConnType = (SelectionProperty) clone.getModel().getProperty(ElementPropKeys.PATH_CONN_TYPE);

    if (getLiner() instanceof BezierLiner) {
      pConnType.setValue(PathModel.LinerType.BEZIER);
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
