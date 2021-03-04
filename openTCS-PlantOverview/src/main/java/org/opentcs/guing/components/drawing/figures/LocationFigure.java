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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.jhotdraw.draw.ConnectionFigure;
import org.jhotdraw.draw.connector.ChopEllipseConnector;
import org.jhotdraw.draw.connector.Connector;
import org.jhotdraw.geom.Geom;
import org.opentcs.components.plantoverview.LocationTheme;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.guing.components.drawing.ZoomPoint;
import org.opentcs.guing.components.properties.SelectionPropertiesComponent;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.type.SymbolProperty;
import org.opentcs.guing.components.tree.ComponentsTreeViewManager;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.LocationTypeModel;

/**
 * Ein Figure fï¿½r Stationen (ï¿½bergabestationen, Batterieladestationen) und
 * Gerï¿½te (Aufzï¿½ge, Drehteller und so weiter).
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LocationFigure
    extends TCSFigure
    implements ImageObserver {

  /**
   * The image representing the location.
   */
  private transient Image fImage;
  private int fWidth;
  private int fHeight;
  private final LocationTheme locationTheme;

  /**
   * Creates a new instance.
   *
   * @param componentsTreeManager The manager for the components tree view.
   * @param propertiesComponent Displays properties of the currently selected
   * model component(s).
   * @param locationTheme The location theme to be used.
   * @param model The model corresponding to this graphical object.
   */
  @Inject
  public LocationFigure(ComponentsTreeViewManager componentsTreeManager,
                        SelectionPropertiesComponent propertiesComponent,
                        LocationTheme locationTheme,
                        @Assisted LocationModel model) {
    super(componentsTreeManager, propertiesComponent, model);
    this.locationTheme = requireNonNull(locationTheme, "locationTheme");

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
    // TODO: Beim Draggen soll der Zoompoint immer auf das Raster des Gridconstrainers einrasten
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
    // Location Figure hat nur einen Connector in der Mitte der Figur (?)
    return new ChopEllipseConnector(this);
  }

  @Override // AbstractFigure
  public Connector findCompatibleConnector(Connector c, boolean isStartConnector) {
    // Location Figure hat nur einen Connector in der Mitte der Figur (?)
    return new ChopEllipseConnector(this);
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

  /**
   * Wird aus LabeledLocationFigure.propertiesChanged() aufgerufen wenn das
   * Symbol fï¿½r die Station geï¿½ndert wird.
   *
   * @param e
   */
  public void propertiesChanged(AttributesChangeEvent e) {
    LocationTypeModel locationType = getModel().getLocationType();

    if (locationType != null) {
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

    if (fImage != null) {
      fWidth = Math.max(fImage.getWidth(this) + 10, 30);
      fHeight = Math.max(fImage.getHeight(this) + 10, 30);
      fDisplayBox.setSize(fWidth, fHeight);
    }
  }

  @Override  // ImageObserver
  public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
    if ((infoflags & (FRAMEBITS | ALLBITS)) != 0) {
      invalidate();
    }

    return (infoflags & (ALLBITS | ABORT)) == 0;
  }
}
