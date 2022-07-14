/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.components.drawing.figures;

import com.google.inject.assistedinject.Assisted;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.ConnectionFigure;
import org.jhotdraw.draw.connector.ChopEllipseConnector;
import org.jhotdraw.draw.connector.Connector;
import org.jhotdraw.geom.Geom;
import org.opentcs.components.plantoverview.LocationTheme;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.data.order.TransportOrder;
import org.opentcs.guing.base.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.base.components.properties.type.SymbolProperty;
import org.opentcs.guing.base.model.elements.BlockModel;
import org.opentcs.guing.base.model.elements.LocationModel;
import org.opentcs.guing.base.model.elements.LocationTypeModel;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.components.drawing.DrawingOptions;
import org.opentcs.guing.common.components.drawing.Strokes;
import org.opentcs.guing.common.components.drawing.ZoomPoint;

/**
 * A figure for locations.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LocationFigure
    extends TCSFigure
    implements ImageObserver {

  /**
   * The fill color for locked locations.
   */
  private static final Color LOCKED_COLOR = new Color(255, 50, 50);
  /**
   * The image representing the location.
   */
  private transient Image fImage;
  private int fWidth;
  private int fHeight;
  private final LocationTheme locationTheme;
  private final DrawingOptions drawingOptions;

  /**
   * Creates a new instance.
   *
   * @param locationTheme The location theme to be used.
   * @param model The model corresponding to this graphical object.
   * @param drawingOptions The drawing options.
   */
  @Inject
  public LocationFigure(LocationTheme locationTheme,
                        @Assisted LocationModel model,
                        DrawingOptions drawingOptions) {
    super(model);
    this.locationTheme = requireNonNull(locationTheme, "locationTheme");
    this.drawingOptions = requireNonNull(drawingOptions, "drawingOptions");

    fWidth = 30;
    fHeight = 30;
    fDisplayBox = new Rectangle(fWidth, fHeight);
    fZoomPoint = new ZoomPoint(0.5 * fWidth, 0.5 * fHeight);
  }

  @Override
  public LocationModel getModel() {
    return (LocationModel) get(FigureConstants.MODEL);
  }

  public Point center() {
    return Geom.center(fDisplayBox);
  }

  @Override // Figure
  public Rectangle2D.Double getBounds() {
    Rectangle2D r2 = fDisplayBox.getBounds2D();
    Rectangle2D.Double r2d = new Rectangle2D.Double();
    r2d.setRect(r2);

    return r2d;
  }

  @Override  // Figure
  public Object getTransformRestoreData() {
    return fDisplayBox.clone();
  }

  @Override  // Figure
  public void restoreTransformTo(Object restoreData) {
    Rectangle r = (Rectangle) restoreData;
    fDisplayBox.x = r.x;
    fDisplayBox.y = r.y;
    fDisplayBox.width = r.width;
    fDisplayBox.height = r.height;
    fZoomPoint.setX(r.x + 0.5 * r.width);
    fZoomPoint.setY(r.y + 0.5 * r.height);
  }

  @Override  // Figure
  public void transform(AffineTransform tx) {
    Point2D center = getZoomPoint().getPixelLocationExactly();
    setBounds((Point2D.Double) tx.transform(center, center), null);
  }

  @Override  // AbstractFigure
  public void setBounds(Point2D.Double anchor, Point2D.Double lead) {
    fZoomPoint.setX(anchor.x);
    fZoomPoint.setY(anchor.y);
    fDisplayBox.x = (int) (anchor.x - 0.5 * fDisplayBox.width);
    fDisplayBox.y = (int) (anchor.y - 0.5 * fDisplayBox.height);
  }

  @Override // AbstractFigure
  public Connector findConnector(Point2D.Double p, ConnectionFigure prototype) {
    return new ChopEllipseConnector(this);
  }

  @Override // AbstractFigure
  public Connector findCompatibleConnector(Connector c, boolean isStartConnector) {
    return new ChopEllipseConnector(this);
  }

  @Override
  public int getLayer() {
    return getModel().getPropertyLayerWrapper().getValue().getLayer().getOrdinal();
  }

  @Override
  public boolean isVisible() {
    return super.isVisible()
        && getModel().getPropertyLayerWrapper().getValue().getLayer().isVisible()
        && getModel().getPropertyLayerWrapper().getValue().getLayerGroup().isVisible();
  }

  @Override
  protected void drawFigure(Graphics2D g) {
    if (drawingOptions.isBlocksVisible()) {
      drawBlockDecoration(g);
    }
    drawRouteDecoration(g);

    super.drawFigure(g);
  }

  private void drawRouteDecoration(Graphics2D g) {
    for (VehicleModel vehicleModel : getModel().getVehicleModels()) {
      Stroke stroke = Strokes.PATH_ON_ROUTE;
      Color color = transparentColor(vehicleModel.getDriveOrderColor(), 192);
      if (vehicleModel.getDriveOrderState() == TransportOrder.State.WITHDRAWN) {
        stroke = Strokes.PATH_ON_WITHDRAWN_ROUTE;
        color = Color.GRAY;
      }

      drawDecoration(g, stroke, color);
    }
  }

  private void drawBlockDecoration(Graphics2D g) {
    for (BlockModel blockModel : getModel().getBlockModels()) {
      drawDecoration(g, Strokes.BLOCK_ELEMENT, transparentColor(blockModel.getColor(), 192));
    }
  }

  private Color transparentColor(Color color, int alpha) {
    return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
  }

  private void drawDecoration(Graphics2D g, Stroke stroke, Color color) {
    g.setStroke(stroke);
    g.setColor(color);
    g.draw(this.getDrawingArea());
  }

  @Override  // AbstractAttributedFigure
  protected void drawFill(Graphics2D g) {
    int dx;
    int dy;
    Rectangle r = displayBox();
    g.fillRect(r.x, r.y, r.width, r.height);

    if (fImage != null) {
      dx = (r.width - fImage.getWidth(this)) / 2;
      dy = (r.height - fImage.getHeight(this)) / 2;
      g.drawImage(fImage, r.x + dx, r.y + dy, this);
    }
  }

  @Override  // AbstractAttributedFigure
  protected void drawStroke(Graphics2D g) {
    Rectangle r = displayBox();
    g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
  }

  @Override
  public LocationFigure clone() {
    LocationFigure thatFigure = (LocationFigure) super.clone();
    thatFigure.setZoomPoint(new ZoomPoint(fZoomPoint.getX(), fZoomPoint.getY()));

    return thatFigure;
  }

  @Override  // ImageObserver
  public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
    if ((infoflags & (FRAMEBITS | ALLBITS)) != 0) {
      invalidate();
    }

    return (infoflags & (ALLBITS | ABORT)) == 0;
  }

  public void propertiesChanged(AttributesChangeEvent e) {
    handleLocationTypeChanged();
    handleLocationLockChanged();
  }

  private void handleLocationTypeChanged() {
    LocationTypeModel locationType = getModel().getLocationType();

    if (locationType == null) {
      return;
    }
    if (getModel().getLocation() != null && locationType.getLocationType() != null) {
      fImage = locationTheme.getImageFor(getModel().getLocation(), locationType.getLocationType());
    }
    else {
      SymbolProperty pSymbol = getModel().getPropertyDefaultRepresentation();
      LocationRepresentation locationRepresentation = pSymbol.getLocationRepresentation();
      if (locationRepresentation == null
          || locationRepresentation == LocationRepresentation.DEFAULT) {
        pSymbol = locationType.getPropertyDefaultRepresentation();
        locationRepresentation = pSymbol.getLocationRepresentation();
        fImage = locationTheme.getImageFor(locationRepresentation);
      }
      else {
        fImage = locationTheme.getImageFor(locationRepresentation);
      }
    }
    fWidth = Math.max(fImage.getWidth(this) + 10, 30);
    fHeight = Math.max(fImage.getHeight(this) + 10, 30);
    fDisplayBox.setSize(fWidth, fHeight);
  }

  private void handleLocationLockChanged() {
    set(AttributeKeys.FILL_COLOR,
        (Boolean) getModel().getPropertyLocked().getValue() ? LOCKED_COLOR : Color.WHITE);
  }
}
