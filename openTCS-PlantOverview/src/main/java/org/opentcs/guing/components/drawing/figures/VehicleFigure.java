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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.handle.Handle;
import org.jhotdraw.geom.BezierPath;
import org.opentcs.components.plantoverview.VehicleTheme;
import org.opentcs.data.model.Triple;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.guing.application.ApplicationState;
import org.opentcs.guing.application.OperationMode;
import org.opentcs.guing.application.menus.MenuFactory;
import org.opentcs.guing.application.menus.VehiclePopupMenu;
import org.opentcs.guing.components.drawing.OpenTCSDrawingView;
import org.opentcs.guing.components.drawing.ZoomPoint;
import org.opentcs.guing.components.drawing.figures.decoration.VehicleOutlineHandle;
import org.opentcs.guing.components.drawing.figures.liner.TripleBezierLiner;
import org.opentcs.guing.components.drawing.figures.liner.TupelBezierLiner;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.components.properties.type.AngleProperty;
import org.opentcs.guing.model.SimpleFolder;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.AbstractConnection;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.persistence.ModelManager;
import org.opentcs.guing.util.PlantOverviewApplicationConfiguration;

/**
 * The graphical representation of a vehicle.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
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
   * The figure's length in drawing units.
   */
  private static final double LENGTH = 30.0;
  /**
   * The figure's width in drawing units.
   */
  private static final double WIDTH = 20.0;
  /**
   * The vehicle theme to be used.
   */
  private final VehicleTheme vehicleTheme;
  /**
   * A factory for popup menus.
   */
  private final MenuFactory menuFactory;
  /**
   * The tool tip text generator.
   */
  private final ToolTipTextGenerator textGenerator;
  /**
   * The model manager.
   */
  private final ModelManager modelManager;
  /**
   * The application's current state.
   */
  private final ApplicationState applicationState;
  /**
   * The angle at which the image is to be drawn.
   */
  private double fAngle;
  /**
   * The image.
   */
  protected transient Image fImage;
  /**
   * Whether to ignore the vehicle's precise position or not.
   */
  private boolean ignorePrecisePosition;
  /**
   * Whether to ignore the vehicle's orientation angle or not.
   */
  private boolean ignoreOrientationAngle;
  /**
   * Indicates whether figure details changed.
   */
  private boolean figureDetailsChanged = false;

  /**
   * Creates a new instance.
   *
   * @param vehicleTheme The vehicle theme to be used.
   * @param menuFactory A factory for popup menus.
   * @param appConfig The application's configuration.
   * @param model The model corresponding to this graphical object.
   * @param textGenerator The tool tip text generator.
   * @param modelManager The model manager.
   * @param applicationState The application's current state.
   */
  @Inject
  public VehicleFigure(VehicleTheme vehicleTheme,
                       MenuFactory menuFactory,
                       PlantOverviewApplicationConfiguration appConfig,
                       @Assisted VehicleModel model,
                       ToolTipTextGenerator textGenerator,
                       ModelManager modelManager,
                       ApplicationState applicationState) {
    super(model);
    this.vehicleTheme = requireNonNull(vehicleTheme, "vehicleTheme");
    this.menuFactory = requireNonNull(menuFactory, "menuFactory");
    this.textGenerator = requireNonNull(textGenerator, "textGenerator");
    this.modelManager = requireNonNull(modelManager, "modelManager");
    this.applicationState = requireNonNull(applicationState, "applicationState");

    fDisplayBox = new Rectangle((int) LENGTH, (int) WIDTH);
    fZoomPoint = new ZoomPoint(0.5 * LENGTH, 0.5 * WIDTH);

    setIgnorePrecisePosition(appConfig.ignoreVehiclePrecisePosition());
    setIgnoreOrientationAngle(appConfig.ignoreVehicleOrientationAngle());

    fImage = vehicleTheme.statelessImage(model.getVehicle());
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
    return textGenerator.getToolTipText(getModel());
  }

  @Override
  public boolean isSelectable() {
    return super.isSelectable() && applicationState.hasOperationMode(OperationMode.OPERATING);
  }

  /**
   * Sets the ignore flag for the vehicle's reported orientation angle.
   *
   * @param doIgnore Whether to ignore the reported orientation angle.
   */
  public final void setIgnoreOrientationAngle(boolean doIgnore) {
    ignoreOrientationAngle = doIgnore;
    PointModel point = getModel().getPoint();

    if (point == null) {
      // Vehicle nur zeichnen, wenn Point bekannt ist oder wenn Precise Position
      // bekannt ist und nicht ignoriert werden soll.
      setVisible(!ignorePrecisePosition);
    }
    else {
      Figure pointFigure = modelManager.getModel().getFigure(point);
      Rectangle2D.Double r = pointFigure.getBounds();
      Point2D.Double pCenter = new Point2D.Double(r.getCenterX(), r.getCenterY());
      setBounds(pCenter, null);
      fireFigureChanged();
    }
  }

  /**
   * Sets the ignore flag for the vehicle's precise position.
   *
   * @param doIgnore Whether to ignore the reported precise position of the
   * vehicle.
   */
  public final void setIgnorePrecisePosition(boolean doIgnore) {
    ignorePrecisePosition = doIgnore;
    PointModel point = getModel().getPoint();

    if (point == null) {
      // Vehicle nur zeichnen, wenn Point bekannt ist oder wenn Precise Position
      // bekannt ist und nicht ignoriert werden soll.
      setVisible(!ignorePrecisePosition);
    }
    else {
      Figure pointFigure = modelManager.getModel().getFigure(point);
      Rectangle2D.Double r = pointFigure.getBounds();
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
    fDisplayBox.x = (int) (anchor.x - 0.5 * LENGTH);
    fDisplayBox.y = (int) (anchor.y - 0.5 * WIDTH);
    firePropertyChange(POSITION_CHANGED, oldBounds, getBounds());

    updateVehicleOrientation();
  }

  private void updateVehicleOrientation() {
    VehicleModel model = getModel();
    // Winkelausrichtung:
    // 1. Exakte Pose vom Fahrzeugtreiber gemeldet oder
    // 2. Property des aktuellen Punktes oder
    // 3. Zielrichtung zum nï¿½chsten Punkt oder
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
    else if (currentPoint != null) {
      // Use orientation from current point.
      AngleProperty ap = currentPoint.getPropertyVehicleOrientationAngle();
      angle = (double) ap.getValue();

      if (!Double.isNaN(angle)) {
        fAngle = angle;
      }
      else {
        // Wenn fï¿½r diesen Punkt keine Winkelausrichtung spezifiziert ist,
        // Winkel zum nï¿½chsten Zielpunkt bestimmen
        alignVehicleToNextPoint();
      }
    }
  }

  private void alignVehicleToNextPoint() {
    VehicleModel model = getModel();
    PointModel nextPoint = model.getNextPoint();
    PointModel currentPoint = model.getPoint();

    AbstractConnection connection;
    if (model.getDriveOrderState() == TransportOrder.State.BEING_PROCESSED) {
      if (model.getDriveOrderComponents() == null) {
        connection = null;
      }
      else {
        connection = (PathModel) model.getDriveOrderComponents().stream()
            .filter(component -> component instanceof PathModel)
            .findFirst()
            .orElse(null);
      }
    }
    else {
      if (nextPoint != null) {
        connection = currentPoint.getConnectionTo(nextPoint);
      }
      else {
        // Wenn es keinen Zielpunkt gibt, einen beliebigen (?) Nachbarpunkt zum aktuellen Punkt suchen
        connection = currentPoint.getConnections().stream()
            .filter(con -> con instanceof PathModel)
            .filter(con -> Objects.equals(con.getStartComponent(), currentPoint))
            .findFirst()
            .orElse(null);
      }
    }

    if (connection != null) {
      fAngle = calculateAngle(connection);
    }
  }

  private double calculateAngle(AbstractConnection connection) {
    PointModel currentPoint = (PointModel) connection.getStartComponent();
    PointModel nextPoint = (PointModel) connection.getEndComponent();

    PathConnection pathFigure
        = (PathConnection) modelManager.getModel().getFigure(connection);
    LabeledPointFigure clpf
        = (LabeledPointFigure) modelManager.getModel().getFigure(currentPoint);
    PointFigure cpf = clpf.getPresentationFigure();

    if (pathFigure.getLiner() instanceof TupelBezierLiner
        || pathFigure.getLiner() instanceof TripleBezierLiner) {
      BezierPath bezierPath = pathFigure.getBezierPath();
      Point2D.Double cp = bezierPath.get(0, BezierPath.C2_MASK);
      double dx = cp.getX() - cpf.getZoomPoint().getX();
      double dy = cp.getY() - cpf.getZoomPoint().getY();
      // An die Tangente der Verbindungskurve ausrichten
      return Math.toDegrees(Math.atan2(-dy, dx));
    }
    else {
      LabeledPointFigure nlpf
          = (LabeledPointFigure) modelManager.getModel().getFigure(nextPoint);
      PointFigure npf = nlpf.getPresentationFigure();
      double dx = npf.getZoomPoint().getX() - cpf.getZoomPoint().getX();
      double dy = npf.getZoomPoint().getY() - cpf.getZoomPoint().getY();
      // Nach dem direkten Winkel ausrichten
      return Math.toDegrees(Math.atan2(-dy, dx));
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

    int dx;
    int dy;
    Rectangle r = displayBox();

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

      case 0:  // Mouse clicked
//      handles.add(new VehicleOutlineHandle(this));
        break;

      case 1:  // Double-Click
//      handles.add(new VehicleOutlineHandle(this));
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
    // This gets executed on a double click AND a right click on the figure
    VehicleModel model = getModel();
    VehiclePopupMenu menu = menuFactory.createVehiclePopupMenu(Arrays.asList(model));
    menu.show((OpenTCSDrawingView) drawingView, evt.getX(), evt.getY());

    return false;
  }

  @Override
  public void propertiesChanged(AttributesChangeEvent e) {
    if (e.getInitiator().equals(this)
        || e.getModel() == null) {
      return;
    }

    updateFigureDetails((VehicleModel) e.getModel());

    if (isFigureDetailsChanged()) {
      SwingUtilities.invokeLater(() -> {
        // Only call if the figure is visible - will cause NPE in BoundsOutlineHandle otherwise.
        if (isVisible()) {
          fireFigureChanged();
        }
      });

      setFigureDetailsChanged(false);
    }
  }

  /**
   * Updates the figure details based on the given vehicle model.
   * <p>
   * If figure details do change, call {@link #setFigureDetailsChanged(boolean)} to set the
   * corresponding flag to {@code true}.
   * When overriding this method, always remember to call the super-implementation.
   * </p>
   *
   * @param model The updated vehicle model.
   */
  protected void updateFigureDetails(VehicleModel model) {
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
   * Returns the vehicle theme.
   *
   * @return The vehicle theme.
   */
  protected VehicleTheme getVehicleTheme() {
    return vehicleTheme;
  }

  public boolean isFigureDetailsChanged() {
    return figureDetailsChanged;
  }

  public void setFigureDetailsChanged(boolean figureDetailsChanged) {
    this.figureDetailsChanged = figureDetailsChanged;
  }

  public boolean isIgnorePrecisePosition() {
    return ignorePrecisePosition;
  }

  public ModelManager getModelManager() {
    return modelManager;
  }
}
