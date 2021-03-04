/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.guing.components.drawing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import javax.inject.Inject;
import org.opentcs.guing.application.ApplicationState;
import org.opentcs.guing.application.OpenTCSView;
import static org.opentcs.guing.components.drawing.OpenTCSDrawingView.setViewRenderingHints;
import org.opentcs.guing.exchange.TransportOrderUtil;
import org.opentcs.guing.model.ModelManager;

/**
 * Draws the drawing using a BufferedImage.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class OpenTCSDrawingViewBuffered
    extends OpenTCSDrawingView {

  /**
   * The drawingBuffer holds a rendered image of the drawing (in view
   * coordinates).
   */
  private BufferedImage drawingBuffer;

  /**
   * Creates new instance.
   *
   * @param appState Stores the application's current state.
   * @param opentcsView The view to be used.
   * @param modelManager Provides the current system model.
   * @param orderUtil A helper for creating transport orders with the kernel.
   */
  @Inject
  public OpenTCSDrawingViewBuffered(ApplicationState appState,
                                    OpenTCSView opentcsView,
                                    ModelManager modelManager,
                                    TransportOrderUtil orderUtil) {
    super(appState, opentcsView, modelManager, orderUtil);
  }

  @Override
  protected void drawDrawingImpl(Graphics2D g2d) {
    Rectangle vr = getVisibleRect();
    Point shift = new Point(0, 0);

    if (bufferedArea.contains(vr)
        || bufferedArea.width >= vr.width && bufferedArea.height >= vr.height) {
      // The visible rect fits into the buffered area, but may be shifted; shift the buffered area.
      shift.x = bufferedArea.x - vr.x;
      shift.y = bufferedArea.y - vr.y;

      if (shift.x > 0) {
        dirtyArea.add(new Rectangle(bufferedArea.x - shift.x,
                                    vr.y,
                                    shift.x + bufferedArea.width - vr.width,
                                    bufferedArea.height));
      }
      else if (shift.x < 0) {
        dirtyArea.add(new Rectangle(bufferedArea.x + vr.width,
                                    vr.y,
                                    -shift.x + bufferedArea.width - vr.width,
                                    bufferedArea.height));
      }

      if (shift.y > 0) {
        dirtyArea.add(new Rectangle(vr.x,
                                    bufferedArea.y - shift.y,
                                    bufferedArea.width,
                                    shift.y + bufferedArea.height - vr.height));
      }
      else if (shift.y < 0) {
        dirtyArea.add(new Rectangle(vr.x,
                                    bufferedArea.y + vr.height,
                                    bufferedArea.width,
                                    -shift.y + bufferedArea.height - vr.height));
      }

      bufferedArea.x = vr.x;
      bufferedArea.y = vr.y;
    }
    else {
      // The buffered drawing area does not match the visible rect;
      // resize it, and mark everything as dirty.
      bufferedArea.setBounds(vr);
      dirtyArea.setBounds(vr);

      if (drawingBuffer != null
          && (drawingBuffer.getWidth() != vr.width
              || drawingBuffer.getHeight() != vr.height)) {
        // The dimension of the drawing buffer does not fit into the visible rect;
        // throw the buffer away.
        drawingBuffer.flush();
        drawingBuffer = null;
      }
    }
    // Update the contents of the buffer if necessary

    int valid = (drawingBuffer == null)
        ? VolatileImage.IMAGE_INCOMPATIBLE
        : VolatileImage.IMAGE_OK;

    switch (valid) {
      case VolatileImage.IMAGE_INCOMPATIBLE:
        // old buffer doesn't work with new GraphicsConfig; (re-)create it
        try {
          drawingBuffer = getGraphicsConfiguration().createCompatibleImage(vr.width, vr.height, Transparency.TRANSLUCENT);
        }
        catch (OutOfMemoryError e) {
          drawingBuffer = null;
        }

        dirtyArea.setBounds(bufferedArea);
        break;
        
      default:
    }

    if (drawingBuffer == null) {
      // There is not enough memory available for a drawing buffer;
      // draw without buffering.
      drawDrawing(g2d);
      return;
    }

    if (!dirtyArea.isEmpty()) {
      // An area of the drawing buffer is dirty; repaint it
      Graphics2D gBuf = drawingBuffer.createGraphics();
      setViewRenderingHints(gBuf);

      // For shifting and cleaning, we need to erase everything underneath
      gBuf.setComposite(AlphaComposite.Src);

      // Perform shifting if needed
      if (shift.x != 0 || shift.y != 0) {
        gBuf.copyArea(Math.max(0, -shift.x),
                      Math.max(0, -shift.y),
                      drawingBuffer.getWidth() - Math.abs(shift.x),
                      drawingBuffer.getHeight() - Math.abs(shift.y),
                      shift.x,
                      shift.y);
        shift.x = shift.y = 0;
      }

      // Clip the dirty area
      gBuf.translate(-bufferedArea.x, -bufferedArea.y);
      gBuf.clip(dirtyArea);

      // Clear the dirty area
      gBuf.setBackground(new Color(0x0, true));
      gBuf.clearRect(dirtyArea.x, dirtyArea.y, dirtyArea.width, dirtyArea.height);
      gBuf.setComposite(AlphaComposite.SrcOver);

      // Repaint the dirty area
      drawDrawing(gBuf);
      gBuf.dispose();
    }

    g2d.drawImage(drawingBuffer, bufferedArea.x, bufferedArea.y, null);

    dirtyArea.setSize(-1, -1);
  }

  @Override
  public void removeNotify() {
    super.removeNotify();

    if (drawingBuffer != null) {
      drawingBuffer.flush();
      drawingBuffer = null;
    }
  }
}
