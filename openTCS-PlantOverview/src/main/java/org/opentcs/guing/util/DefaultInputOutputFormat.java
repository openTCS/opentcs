/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.util;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.io.DOMStorableInputOutputFormat;
import org.jhotdraw.xml.DOMFactory;
import org.jhotdraw.xml.NanoXMLDOMInput;
import org.opentcs.guing.components.drawing.figures.LabeledLocationFigure;
import org.opentcs.guing.components.drawing.figures.LabeledPointFigure;
import org.opentcs.guing.components.drawing.figures.LinkConnection;
import org.opentcs.guing.components.drawing.figures.PathConnection;

/**
 * OpenTCS' version of JHotDraw's <code>DOMStorableInputOutputFormat</code>.
 * Only the read()-method was changed, so if a path is copied with its two
 * associated points the copied path can be connected to the newly copied points.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class DefaultInputOutputFormat
    extends DOMStorableInputOutputFormat {

  private final DOMFactory factory;
  private final String description;
  private final String mimeType;

  /**
   * Creates a new instance with format name "Drawing", file extension "xml"
   * and mime type "image/x-jhotdraw".
   *
   * @param factory
   */
  public DefaultInputOutputFormat(DOMFactory factory) {
    this(factory, "Drawing", "xml", "image/x-jhotdraw");
  }

  /**
   * Creates a new instance using the specified parameters.
   *
   * @param factory The factory for creating Figures from XML elements.
   * @param description The format description to be used for the file filter.
   * @param fileExtension The file extension to be used for file filter.
   * @param mimeType The Mime Type is used for clipboard access.
   */
  public DefaultInputOutputFormat(
      DOMFactory factory,
      String description, String fileExtension, String mimeType) {
    super(factory, description, fileExtension, mimeType);
    this.factory = factory;
    this.description = description;
    this.mimeType = mimeType;
  }

  @Override
  public void read(Transferable t, Drawing drawing, boolean replace)
      throws UnsupportedFlavorException, IOException {
    LinkedList<Figure> figures = new LinkedList<>();
    InputStream in = (InputStream) t.getTransferData(new DataFlavor(mimeType, description));
    NanoXMLDOMInput domi = new NanoXMLDOMInput(factory, in);
    domi.openElement("Drawing-Clip");

    for (int i = 0, n = domi.getElementCount(); i < n; i++) {
      Figure f = (Figure) domi.readObject(i);
      figures.add(f);
    }

    domi.closeElement();

    // --- OpenTCS code starts here ---
    in = (InputStream) t.getTransferData(new DataFlavor(mimeType, description));
    domi = new NanoXMLDOMInput(factory, in);
    domi.openElement("Drawing-Clip");

    for (int i = 0, n = domi.getElementCount(); i < n; i++) {
      // search the duplicated figures for a path connection
      Figure baseFigure = figures.get(i);
      domi.openElement(i);

      if (baseFigure instanceof PathConnection) {
        String srcPointName = domi.getAttribute("sourceName", "");
        String destPointName = domi.getAttribute("destName", "");

        if (!srcPointName.isEmpty() && !destPointName.isEmpty()) {
          Figure srcPointFigure = null;
          Figure destPointFigure = null;
          InputStream in2 = (InputStream) t.getTransferData(new DataFlavor(mimeType, description));
          NanoXMLDOMInput domi2 = new NanoXMLDOMInput(factory, in2);
          domi2.openElement("Drawing-Clip");

          for (int j = 0, m = domi2.getElementCount(); j < m; j++) {
            // search the duplicated figures for the points, that
            // belong to the path
            Figure searchFigure = figures.get(j);
            domi2.openElement(j);

            if (domi2.getAttribute("name", "").equals(srcPointName)) {
              srcPointFigure = searchFigure;
            }

            if (domi2.getAttribute("name", "").equals(destPointName)) {
              destPointFigure = searchFigure;
            }

            domi2.closeElement();
          }

          domi2.closeElement();

          if (srcPointFigure != null && destPointFigure != null) {
            PathConnection path = (PathConnection) baseFigure;
            path.connect((LabeledPointFigure) srcPointFigure, (LabeledPointFigure) destPointFigure);
          }
        }
      }
      else if (baseFigure instanceof LinkConnection) {
        // search the duplicated figures for a LinkConnection
        String srcPointName = domi.getAttribute("sourceName", "");
        String destLocationName = domi.getAttribute("destName", "");

        if (!srcPointName.isEmpty() && !destLocationName.isEmpty()) {
          Figure srcPointFigure = null;
          Figure destPointFigure = null;
          InputStream in2 = (InputStream) t.getTransferData(new DataFlavor(mimeType, description));
          NanoXMLDOMInput domi2 = new NanoXMLDOMInput(factory, in2);
          domi2.openElement("Drawing-Clip");

          for (int j = 0, m = domi2.getElementCount(); j < m; j++) {
            // search the duplicated figures for the point and location, that
            // belong to the link
            Figure searchFigure = figures.get(j);
            domi2.openElement(j);

            if (domi2.getAttribute("name", "").equals(srcPointName)) {
              srcPointFigure = searchFigure;
            }

            String x = domi2.getAttribute("name", "");

            if (x.equals(destLocationName)) {
              destPointFigure = searchFigure;
            }

            domi2.closeElement();
          }

          domi2.closeElement();

          if (srcPointFigure != null && destPointFigure != null) {
            LinkConnection link = (LinkConnection) baseFigure;
            link.connect((LabeledPointFigure) srcPointFigure, (LabeledLocationFigure) destPointFigure);
            link.getModel().updateName();
          }
        }
      }

      domi.closeElement();
    }

    domi.closeElement();
    // --- OpenTCS code ends here ---

    if (replace) {
      drawing.removeAllChildren();
    }

    drawing.addAll(figures);
  }
}
