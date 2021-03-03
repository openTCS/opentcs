/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.drawing.figures;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import static java.awt.image.ImageObserver.ABORT;
import static java.awt.image.ImageObserver.ALLBITS;
import static java.awt.image.ImageObserver.FRAMEBITS;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.jhotdraw.draw.AbstractAttributedDecoratedFigure;
import org.opentcs.guing.components.drawing.ZoomPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Figure displaying a bitmap.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class BitmapFigure
    extends AbstractAttributedDecoratedFigure
    implements ImageObserver {

  /**
   * This class's logger.
   */
  private static final Logger log
      = LoggerFactory.getLogger(BitmapFigure.class);
  /**
   * The image to be displayed.
   */
  private BufferedImage image;
  /**
   * The enclosing rectangle.
   */
  private Rectangle fDisplayBox;
  /**
   * The exact position of the figure's center.
   */
  private ZoomPoint fZoomPoint;
  /**
   * Flag, if this figures has been removed from the drawing due to
   * an other view is active or if it visible.
   */
  private boolean temporarilyRemoved = false;
  /**
   * Path of the image.
   */
  private String imagePath;

  public BitmapFigure(File file) {
    try {
      image = ImageIO.read(file);
      imagePath = file.getPath();
      if (image == null) {
        log.error("Couldn't open image file at" + file.getPath());
        fDisplayBox = new Rectangle(0, 0, 0, 0);
        fZoomPoint = new ZoomPoint(0, 0);
        requestRemove();
        return;
      }
      fDisplayBox = new Rectangle(image.getWidth(), image.getHeight());
      fZoomPoint = new ZoomPoint(0.5 * image.getWidth(), 0.5 * image.getHeight());
    }
    catch (IOException ex) {
      log.error("", ex);
      requestRemove();
    }
  }

  public String getImagePath() {
    return imagePath;
  }

  public boolean isTemporarilyRemoved() {
    return temporarilyRemoved;
  }

  public void setTemporarilyRemoved(boolean temporarilyRemoved) {
    this.temporarilyRemoved = temporarilyRemoved;
  }

  public Rectangle displayBox() {
    return new Rectangle(fDisplayBox.x, fDisplayBox.y,
                         fDisplayBox.width, fDisplayBox.height);
  }

  public void setDisplayBox(Rectangle displayBox) {
    fDisplayBox = displayBox;
  }

  @Override // AbstractFigure
  public void setBounds(Point2D.Double anchor, Point2D.Double lead) {
    //resize
    if (lead != null) {
      //anchor is upper left, lead lower right
      fDisplayBox.width = (int) (lead.x - anchor.x);
      fDisplayBox.height = (int) (lead.y - anchor.y);
    }
    else {
      fZoomPoint.setX(anchor.x);
      fZoomPoint.setY(anchor.y);
      fDisplayBox.x = (int) (anchor.x - 0.5 * fDisplayBox.width);
      fDisplayBox.y = (int) (anchor.y - 0.5 * fDisplayBox.height);
    }
  }

  @Override // ImageObserver
  public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
    if ((infoflags & (FRAMEBITS | ALLBITS)) != 0) {
      invalidate();
    }

    return (infoflags & (ALLBITS | ABORT)) == 0;
  }

  @Override
  protected boolean figureContains(Point2D.Double p) {
    return fDisplayBox.contains(p);
  }

  @Override
  protected void drawFill(Graphics2D g) {
    if (image != null) {
      Rectangle r = displayBox();
      g.drawImage(image, r.x, r.y, r.width, r.height, this);
    }
  }

  @Override
  protected void drawStroke(Graphics2D g) {
    Rectangle r = displayBox();
    g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
  }

  @Override
  public Rectangle2D.Double getBounds() {
    Rectangle2D r2 = fDisplayBox.getBounds2D();
    Rectangle2D.Double r2d = new Rectangle2D.Double();
    r2d.setRect(r2);

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
    fZoomPoint.setX(r.x + 0.5 * r.width);
    fZoomPoint.setY(r.y + 0.5 * r.height);
  }

  @Override
  public void transform(AffineTransform tx) {
    Point2D center = fZoomPoint.getPixelLocationExactly();
    setBounds((Point2D.Double) tx.transform(center, center), null);
  }

  public void setScaleFactor(double oldValue, double newValue) {
    fDisplayBox.width /= oldValue;
    fDisplayBox.width *= newValue;
    fDisplayBox.height /= oldValue;
    fDisplayBox.height *= newValue;
  }
}
