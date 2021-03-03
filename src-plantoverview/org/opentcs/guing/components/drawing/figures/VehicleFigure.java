/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.components.drawing.figures;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import static java.awt.image.ImageObserver.ABORT;
import static java.awt.image.ImageObserver.ALLBITS;
import static java.awt.image.ImageObserver.FRAMEBITS;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.JPopupMenu;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.handle.Handle;
import org.jhotdraw.geom.BezierPath;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Triple;
import org.opentcs.data.model.Vehicle;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.application.action.course.VehicleAction;
import org.opentcs.guing.components.drawing.OpenTCSDrawingView;
import org.opentcs.guing.components.drawing.ZoomPoint;
import org.opentcs.guing.components.drawing.figures.decoration.VehicleOutlineHandle;
import org.opentcs.guing.components.drawing.figures.liner.BezierLiner;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.components.properties.type.AngleProperty;
import org.opentcs.guing.components.properties.type.PercentProperty;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.exchange.DefaultKernelProxyManager;
import org.opentcs.guing.model.FigureComponent;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SimpleFolder;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.AbstractConnection;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.util.ConfigConstants;
import org.opentcs.guing.util.DefaultVehicleThemeManager;
import org.opentcs.guing.util.VehicleThemeManager;
import org.opentcs.util.configuration.ConfigurationStore;
import org.opentcs.util.gui.plugins.VehicleTheme;

/**
 * Die graphische Repräsentation eines Fahrzeugs.
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class VehicleFigure
    extends TCSFigure
    implements AttributesChangeListener,
               ImageObserver {

  /**
   * When the position of the vehicle changed.
   */
  public static final String POSITION_CHANGED = "position_changed";
  /**
   * This class's logger.
   */
  private static final Logger log
      = Logger.getLogger(LocationFigure.class.getName());
  /**
   * The figure's length in drawing units.
   */
  private static final double fLength = 30.0;
  /**
   * The figure's width in drawing units.
   */
  private static final double fWidth = 20.0;
  /**
   * A manager vor vehicle themes.
   */
  private final VehicleThemeManager vehicleThemeManager;
  /**
   * The kernel object associated with this Figure.
   */
  private final Vehicle fVehicle;
  /**
   * The angle at which the image is to be drawn.
   */
  private double fAngle;
  /**
   * The image's file name.
   */
  private String fImageFileName;
  /**
   * The image.
   */
  private transient Image fImage;
  /**
   * Whether to ignore the vehicle's precise position or not.
   */
  private boolean ignorePrecisePosition;
  /**
   * Whether to ignore the vehicle's orientation angle or not.
   */
  private boolean ignoreOrientationAngle;

  /**
   * Creates a new instance.
   *
   * @param model The model.
   */
  public VehicleFigure(FigureComponent model) {
    super(model);
    // XXX Should be injected instead.
    this.vehicleThemeManager = DefaultVehicleThemeManager.getInstance();
    fDisplayBox = new Rectangle((int) fLength, (int) fWidth);
    fZoomPoint = new ZoomPoint(0.5 * fLength, 0.5 * fWidth);

    ConfigurationStore configStore = ConfigurationStore.getStore(OpenTCSView.class.getName());
    setIgnorePrecisePosition(configStore.getBoolean(ConfigConstants.IGNORE_VEHICLE_PRECISE_POSITION, false));
    setIgnoreOrientationAngle(configStore.getBoolean(ConfigConstants.IGNORE_VEHICLE_ORIENTATION_ANGLE, false));

    VehicleTheme theme = vehicleThemeManager.getDefaultTheme();
    TCSObjectReference<Vehicle> reference = ((VehicleModel) model).getReference();

    if (reference != null) {
      fVehicle = DefaultKernelProxyManager.instance().kernel().getTCSObject(Vehicle.class, reference);

      if (fImageFileName == null || !fImageFileName.equals(theme.getImagePathFor(fVehicle))) {
        fImageFileName = theme.getImagePathFor(fVehicle);
        fImage = loadImage(fImageFileName);
      }
    }
    else {
      fVehicle = null;
      log.log(Level.SEVERE, "Reference to Vehicle is null!");
    }
  }

  @Override
  public VehicleModel getModel() {
    return (VehicleModel) get(FigureConstants.MODEL);
  }

  public void setAngle(double angle) {
    fAngle = angle;
  }

  public double getAngle() {
    return fAngle;
  }

  @Override
  public Rectangle2D.Double getBounds() {
    Rectangle2D.Double r2d = new Rectangle2D.Double();
    r2d.setRect(fDisplayBox.getBounds2D());

    return r2d;
  }

  @Override
  public Object getTransformRestoreData() {
    return fDisplayBox.clone();
  }

  @Override
  public void restoreTransformTo(Object restoreData) {
    Rectangle r = (Rectangle) restoreData;
    fDisplayBox.x = r.x;
    fDisplayBox.y = r.y;
    fDisplayBox.width = r.width;
    fDisplayBox.height = r.height;
    fZoomPoint.setX(r.getCenterX());
    fZoomPoint.setY(r.getCenterY());
  }

  @Override
  public void transform(AffineTransform tx) {
    Point2D center = fZoomPoint.getPixelLocationExactly();
    setBounds((Point2D.Double) tx.transform(center, center), null);
  }

  @Override
  public String getToolTipText(Point2D.Double p) {
    VehicleModel model = getModel();
    StringBuilder sb = new StringBuilder("<html>Vehicle ");
    sb.append("<b>").append(model.getName()).append("</b>");
    sb.append("<br>Position: ").append(model.getPoint() != null ? model.getPoint().getName() : "?");
    sb.append("<br>Next Position: ").append(model.getNextPoint() != null ? model.getNextPoint().getName() : "?");
    SelectionProperty sp = (SelectionProperty) model.getProperty(VehicleModel.STATE);
    sb.append("<br>State: ").append(sp.getValue().toString());
    sp = (SelectionProperty) model.getProperty(VehicleModel.PROC_STATE);
    sb.append("<br>Proc State: ").append(sp.getValue().toString());
    String sColor = "black";
    SelectionProperty pEnergyState = (SelectionProperty) model.getProperty(VehicleModel.ENERGY_STATE);
    VehicleModel.EnergyState state = (VehicleModel.EnergyState) pEnergyState.getValue();

    switch (state) {
      case CRITICAL:
        sColor = "red";
        break;

      case DEGRADED:
        sColor = "orange";
        break;

      case GOOD:
        sColor = "green";
        break;
    }

    sb.append("<br>Energy: <font color=").append(sColor).append(">").append(((PercentProperty) model.getProperty(VehicleModel.ENERGY_LEVEL)).getValue()).append("%</font>");
    sb.append("</html>");

    return sb.toString();
  }

  /**
   * Toggles the ignorance of the orientation angle.
   *
   * @param selected
   */
  public final void setIgnoreOrientationAngle(boolean selected) {
    ignoreOrientationAngle = selected;
    PointModel point = getModel().getPoint();

    if (point == null) {
      // Vehicle nur zeichnen, wenn Point bekannt ist oder wenn Precise Position
      // bekannt ist und nicht ignoriert werden soll.
      setVisible(!ignorePrecisePosition);
    }
    else {
      Rectangle2D.Double r = point.getFigure().getBounds();
      Point2D.Double pCenter = new Point2D.Double(r.getCenterX(), r.getCenterY());
      setBounds(pCenter, null);
      fireFigureChanged();
    }
  }

  /**
   * Toggles the ignorance of the precise position.
   *
   * @param selected
   */
  public void setIgnorePrecisePosition(boolean selected) {
    ignorePrecisePosition = selected;
    PointModel point = getModel().getPoint();

    if (point == null) {
      // Vehicle nur zeichnen, wenn Point bekannt ist oder wenn Precise Position
      // bekannt ist und nicht ignoriert werden soll.
      setVisible(!ignorePrecisePosition);
    }
    else {
      Rectangle2D.Double r = point.getFigure().getBounds();
      Point2D.Double pCenter = new Point2D.Double(r.getCenterX(), r.getCenterY());
      setBounds(pCenter, null);
      fireFigureChanged();
    }
  }

  /**
   * Draws the center of the figure at <code>anchor</code>; the size does not
   * change.
   *
   * @param anchor Center of the figure
   * @param lead Not used
   */
  @Override
  public void setBounds(Point2D.Double anchor, Point2D.Double lead) {
    VehicleModel model = getModel();
    Rectangle2D.Double oldBounds = getBounds();
    setVisible(false);

    Triple precisePosition = model.getPrecisePosition();

    if (!ignorePrecisePosition) {
      if (precisePosition != null) {
        setVisible(true);
        // Tree-Folder "Vehicles"
        SimpleFolder folder = (SimpleFolder) model.getParent();
        SystemModel systemModel = (SystemModel) folder.getParent();
        double scaleX = systemModel.getDrawingMethod().getOrigin().getScaleX();
        double scaleY = systemModel.getDrawingMethod().getOrigin().getScaleY();

        if (scaleX != 0.0 && scaleY != 0.0) {
          anchor.x = precisePosition.getX() / scaleX;
          anchor.y = -precisePosition.getY() / scaleY;
        }
      }
    }

    fZoomPoint.setX(anchor.x);
    fZoomPoint.setY(anchor.y);
    fDisplayBox.x = (int) (anchor.x - 0.5 * fLength);
    fDisplayBox.y = (int) (anchor.y - 0.5 * fWidth);
    firePropertyChange(POSITION_CHANGED, oldBounds, getBounds());

    // Winkelausrichtung:
    // 1. Exakte Pose vom Fahrzeugtreiber gemeldet oder
    // 2. Property des aktuellen Punktes oder
    // 3. Zielrichtung zum nächsten Punkt oder
    // 4. letzte Ausrichtung beibehalten
    // ... oder beliebigen Nachbarpunkt suchen???
    double angle = model.getOrientationAngle();
    PointModel currentPoint = model.getPoint();

    if (currentPoint != null) {
      setVisible(true);
    }

    if (!Double.isNaN(angle) && !ignoreOrientationAngle) {
      fAngle = angle;
    }
    else {
      if (currentPoint != null) {
        // Winkelausrichtung aus Property des aktuellen Punktes bestimmen
        AngleProperty ap = (AngleProperty) currentPoint.getProperty(PointModel.VEHICLE_ORIENTATION_ANGLE);

        if (ap != null) {
          angle = (double) ap.getValue();

          if (!Double.isNaN(angle)) {
            fAngle = angle;
          }
          else {
            // Wenn für diesen Punkt keine Winkelausrichtung spezifiziert ist,
            // Winkel zum nächsten Zielpunkt bestimmen
            PointModel nextPoint = model.getNextPoint();
            AbstractConnection connection;

            if (nextPoint == null) {
              // Wenn es keinen Zielpunkt gibt, einen beliebigen (?) Nachbarpunkt zum aktuellen Punkt suchen
              Iterator<AbstractConnection> iConnections = currentPoint.getConnections().iterator();

              while (iConnections.hasNext()) {
                connection = iConnections.next();

                if (connection.getStartComponent().equals(currentPoint)) {
                  ModelComponent destinationPoint = connection.getEndComponent();
                  // Die Links (zu Locations) gehören auch zu den Connections
                  if (destinationPoint instanceof PointModel) {
                    nextPoint = (PointModel) connection.getEndComponent();
                    break;
                  }
                }
              }
            }

            if (nextPoint != null) {
              connection = currentPoint.getConnectionTo(nextPoint);

              if (connection == null) {
                return;
              }

              PathConnection pathFigure = (PathConnection) connection.getFigure();
              PointFigure cpf
                  = (PointFigure) currentPoint.getFigure().getPresentationFigure();

              if (pathFigure.getLiner() instanceof BezierLiner) {
                BezierPath bezierPath = pathFigure.getBezierPath();
                Point2D.Double cp = bezierPath.get(0, BezierPath.C2_MASK);
                double dx = cp.getX() - cpf.getZoomPoint().getX();
                double dy = cp.getY() - cpf.getZoomPoint().getY();
                // An die Tangente der Verbindungskurve ausrichten
                fAngle = Math.toDegrees(Math.atan2(-dy, dx));
              }
              else {
                PointFigure npf
                    = (PointFigure) nextPoint.getFigure().getPresentationFigure();
                double dx = npf.getZoomPoint().getX() - cpf.getZoomPoint().getX();
                double dy = npf.getZoomPoint().getY() - cpf.getZoomPoint().getY();
                // Nach dem direkten Winkel ausrichten
                fAngle = Math.toDegrees(Math.atan2(-dy, dx));
              }
            }
          }
        }
      }
    }
  }

  /**
   * Beim Aufruf des Dialogs SingleVehicleView Fahrzeug unbedingt zeichnen.
   *
   * @param g2d
   */
  public void forcedDraw(Graphics2D g2d) {
    drawFill(g2d);
  }

  @Override
  protected void drawFigure(Graphics2D g2d) {
    VehicleModel model = getModel();
    PointModel currentPoint = model.getPoint();
    Triple precisePosition = model.getPrecisePosition();
    // Fahrzeug nur zeichnen, wenn es einem Punkt zugewiesen ist oder eine exakte
    // Position gesetzt ist
    if (currentPoint != null || precisePosition != null) {
      drawFill(g2d);
    }
  }

  @Override
  protected void drawFill(Graphics2D g2d) {
    if (g2d == null) {
      return;
    }

    int dx, dy;
    Rectangle r = displayBox();

    if (fImage == null) {
      if (fImageFileName != null) {
        fImage = loadImage(fImageFileName);
      }
    }

    if (fImage != null) {
      dx = (r.width - fImage.getWidth(this)) / 2;
      dy = (r.height - fImage.getHeight(this)) / 2;
      int x = r.x + dx;
      int y = r.y + dy;
      AffineTransform oldAF = g2d.getTransform();
      g2d.translate(r.getCenterX(), r.getCenterY());
      g2d.rotate(-Math.toRadians(fAngle));
      g2d.translate(-r.getCenterX(), -r.getCenterY());
      g2d.drawImage(fImage, x, y, null);
      g2d.setTransform(oldAF);
    }
    else {
      // TODO: Rechteck als Umriss zeichnen
    }

    // Text
    String name = getModel().getName();
    Pattern p = Pattern.compile("\\d+");	// Ziffern suchen
    Matcher m = p.matcher(name);

    if (m.find()) {	// Wenn es mindestens eine Ziffer gibt...
      String number = m.group();
      g2d.setPaint(Color.BLUE);
      g2d.drawString(number, (int) r.getCenterX() - 5, (int) r.getCenterY() + 6);
    }
  }

  @Override
  protected void drawStroke(Graphics2D g2d) {
    // Nothing to do here - Vehicle Figure is completely drawn in drawFill()
  }

  @Override
  public Collection<Handle> createHandles(int detailLevel) {
    LinkedList<Handle> handles = new LinkedList<>();

    switch (detailLevel) {
      case -1: // Mouse Moved
        handles.add(new VehicleOutlineHandle(this));
        break;

      case 0:	// Mouse clicked
//			handles.add(new VehicleOutlineHandle(this));
        break;

      case 1:	// Double-Click
//			handles.add(new VehicleOutlineHandle(this));
        break;

      default:
        break;
    }

    return handles;
  }

  @Override
  public boolean handleMouseClick(Point2D.Double p,
                                  MouseEvent evt,
                                  DrawingView drawingView) {
    VehicleModel model = getModel();
    OpenTCSView.instance().getTreeViewManager().selectItem(model);
    OpenTCSView.instance().getPropertiesComponent().setModel(model);

    JPopupMenu menu = VehicleAction.createVehicleMenu(model);
    menu.show((OpenTCSDrawingView) drawingView, evt.getX(), evt.getY());

    return false;
  }

  @Override
  public void propertiesChanged(AttributesChangeEvent e) {
    if (e.getInitiator().equals(this)) {
      return;
    }
    VehicleModel model = (VehicleModel) e.getModel();

    if (model == null || fVehicle == null) {
      return;
    }

    VehicleTheme theme = vehicleThemeManager.getDefaultTheme();

    if (fImageFileName == null || !fImageFileName.equals(theme.getImagePathFor(fVehicle))) {
      fImageFileName = theme.getImagePathFor(fVehicle);
      fImage = loadImage(fImageFileName);
    }

    PointModel point = model.getPoint();
    Triple precisePosition = model.getPrecisePosition();

    if (point == null && precisePosition == null) {
      // Wenn weder Punkt noch exakte Position bekannt: Figur nicht zeichnen
      setVisible(false);
    }
    else {
      // Wenn eine exakte Position existiert, wird diese in setBounds() gesetzt,
      // benötigt also keine anderen Koordinaten
      if (precisePosition != null && !ignorePrecisePosition) {
        setVisible(true);
        setBounds(new Point2D.Double(), null);
        // Nur aufrufen, wenn Figure sichtbar - sonst gibt es in BoundsOutlineHandle eine NP-Exception!
        fireFigureChanged();
      }
      else if (point != null) {
        setVisible(true);
        Rectangle2D.Double r = point.getFigure().getBounds();
        Point2D.Double pCenter = new Point2D.Double(r.getCenterX(), r.getCenterY());
        // Figur an der Mitte des Knotens zeichnen.
        // Die Winkelausrichtung wird in setBounds() bestimmt
        setBounds(pCenter, null);
        // Nur aufrufen, wenn Figure sichtbar - sonst gibt es in BoundsOutlineHandle eine NP-Exception!
        fireFigureChanged();
      }
      else {
        setVisible(false);
      }
    }
  }

  @Override
  public boolean imageUpdate(Image img, int infoflags,
                             int x, int y,
                             int width, int height) {
    if ((infoflags & (FRAMEBITS | ALLBITS)) != 0) {
      invalidate();
    }

    return (infoflags & (ALLBITS | ABORT)) == 0;
  }

  /**
   * Loads an image from the file with the given name.
   *
   * @param fileName The name of the file from which to load the image.
   * @return The image, or <code>null</code>, if it could not be loaded.
   */
  private Image loadImage(String fileName) {
    if (fileName == null) {
      return null;
    }
    URL url = getClass().getResource(fileName);
    if (url == null) {
      log.warning("Invalid image file name " + fileName);
      return null;
    }
    try {
      return ImageIO.read(url);
    }
    catch (IOException exc) {
      log.log(Level.WARNING, "Exception loading image", exc);
      return null;
    }
  }
}
