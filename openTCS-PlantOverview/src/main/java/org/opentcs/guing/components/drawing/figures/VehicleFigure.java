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
import java.util.Collection;
import java.util.LinkedList;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.handle.Handle;
import org.jhotdraw.geom.BezierPath;
import org.opentcs.components.plantoverview.VehicleTheme;
import org.opentcs.data.model.Triple;
import org.opentcs.guing.application.menus.MenuFactory;
import org.opentcs.guing.application.menus.VehiclePopupMenu;
import org.opentcs.guing.components.drawing.OpenTCSDrawingView;
import org.opentcs.guing.components.drawing.ZoomPoint;
import org.opentcs.guing.components.drawing.figures.decoration.VehicleOutlineHandle;
import org.opentcs.guing.components.drawing.figures.liner.TripleBezierLiner;
import org.opentcs.guing.components.drawing.figures.liner.TupelBezierLiner;
import org.opentcs.guing.components.properties.SelectionPropertiesComponent;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.components.properties.type.AngleProperty;
import org.opentcs.guing.components.tree.ComponentsTreeViewManager;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SimpleFolder;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.AbstractConnection;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.VehicleModel;
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
   * The angle at which the image is to be drawn.
   */
  private double fAngle;
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
   * @param componentsTreeManager The manager for the components tree view.
   * @param propertiesComponent Displays properties of the currently selected model component(s).
   * @param vehicleTheme The vehicle theme to be used.
   * @param menuFactory A factory for popup menus.
   * @param appConfig The application's configuration.
   * @param model The model corresponding to this graphical object.
   * @param textGenerator The tool tip text generator.
   */
  @Inject
  public VehicleFigure(ComponentsTreeViewManager componentsTreeManager,
                       SelectionPropertiesComponent propertiesComponent,
                       VehicleTheme vehicleTheme,
                       MenuFactory menuFactory,
                       PlantOverviewApplicationConfiguration appConfig,
                       @Assisted VehicleModel model,
                       ToolTipTextGenerator textGenerator) {
    super(componentsTreeManager, propertiesComponent, model);
    this.vehicleTheme = requireNonNull(vehicleTheme, "vehicleTheme");
    this.menuFactory = requireNonNull(menuFactory, "menuFactory");
    this.textGenerator = requireNonNull(textGenerator, "textGenerator");

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
      Rectangle2D.Double r = point.getFigure().getBounds();
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
    fDisplayBox.x = (int) (anchor.x - 0.5 * LENGTH);
    fDisplayBox.y = (int) (anchor.y - 0.5 * WIDTH);
    firePropertyChange(POSITION_CHANGED, oldBounds, getBounds());

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

      if (ap != null) {
        angle = (double) ap.getValue();

        if (!Double.isNaN(angle)) {
          fAngle = angle;
        }
        else {
          // Wenn fï¿½r diesen Punkt keine Winkelausrichtung spezifiziert ist,
          // Winkel zum nï¿½chsten Zielpunkt bestimmen
          PointModel nextPoint = model.getNextPoint();

          if (nextPoint == null) {
            // Wenn es keinen Zielpunkt gibt, einen beliebigen (?) Nachbarpunkt zum aktuellen Punkt suchen
            for (AbstractConnection connection : currentPoint.getConnections()) {
              if (connection.getStartComponent().equals(currentPoint)) {
                ModelComponent destinationPoint = connection.getEndComponent();
                // Die Links (zu Locations) gehï¿½ren auch zu den Connections
                if (destinationPoint instanceof PointModel) {
                  nextPoint = (PointModel) connection.getEndComponent();
                  break;
                }
              }
            }
          }

          if (nextPoint != null) {
            AbstractConnection connection = currentPoint.getConnectionTo(nextPoint);

            if (connection == null) {
              return;
            }

            PathConnection pathFigure = (PathConnection) connection.getFigure();
            PointFigure cpf = currentPoint.getFigure().getPresentationFigure();

            if (pathFigure.getLiner() instanceof TupelBezierLiner
                || pathFigure.getLiner() instanceof TripleBezierLiner) {
              BezierPath bezierPath = pathFigure.getBezierPath();
              Point2D.Double cp = bezierPath.get(0, BezierPath.C2_MASK);
              double dx = cp.getX() - cpf.getZoomPoint().getX();
              double dy = cp.getY() - cpf.getZoomPoint().getY();
              // An die Tangente der Verbindungskurve ausrichten
              fAngle = Math.toDegrees(Math.atan2(-dy, dx));
            }
            else {
              PointFigure npf = nextPoint.getFigure().getPresentationFigure();
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
    VehicleModel model = getModel();
    getComponentsTreeManager().selectItem(model);
    getPropertiesComponent().setModel(model);

    VehiclePopupMenu menu = menuFactory.createVehiclePopupMenu(model);
    menu.show((OpenTCSDrawingView) drawingView, evt.getX(), evt.getY());

    return false;
  }

  @Override
  public void propertiesChanged(AttributesChangeEvent e) {
    if (e.getInitiator().equals(this)) {
      return;
    }
    VehicleModel model = (VehicleModel) e.getModel();

    if (model == null) {
      return;
    }

    fImage = vehicleTheme.statefulImage(model.getVehicle());

    PointModel point = model.getPoint();
    Triple precisePosition = model.getPrecisePosition();

    if (point == null && precisePosition == null) {
      // If neither the point nor the precise position is known, don't draw the figure.
      SwingUtilities.invokeLater(() -> setVisible(false));
    }
    else if (precisePosition != null && !ignorePrecisePosition) {
      // If a precise position exists, it is set in setBounds(), so it doesn't need any coordinates.
      SwingUtilities.invokeLater(() -> {
        setVisible(true);
        setBounds(new Point2D.Double(), null);
        // Only call if the figure is visible - will cause NPE in BoundsOutlineHandle otherwise.
        fireFigureChanged();
      });
    }
    else if (point != null) {
      SwingUtilities.invokeLater(() -> {
        setVisible(true);
        Rectangle2D.Double r = point.getFigure().getBounds();
        Point2D.Double pCenter = new Point2D.Double(r.getCenterX(), r.getCenterY());
        // Draw figure in the center of the node.
        // Angle is set in setBounds().
        setBounds(pCenter, null);
        // Only call if the figure is visible - will cause NPE in BoundsOutlineHandle otherwise.
        fireFigureChanged();
      });
    }
    else {
      SwingUtilities.invokeLater(() -> setVisible(false));
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
   * Returns the vehicle theme.
   *
   * @return The vehicle theme.
   */
  protected VehicleTheme getVehicleTheme() {
    return vehicleTheme;
  }
}
