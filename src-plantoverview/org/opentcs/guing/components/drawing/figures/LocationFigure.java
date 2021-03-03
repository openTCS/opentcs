/**
 * (c): IML, IFAK, JHotDraw.
 *
 */
package org.opentcs.guing.components.drawing.figures;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Timer;
import org.jhotdraw.draw.ConnectionFigure;
import org.jhotdraw.draw.connector.ChopEllipseConnector;
import org.jhotdraw.draw.connector.Connector;
import org.jhotdraw.geom.Geom;
import org.opentcs.data.ObjectPropConstants;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.guing.components.drawing.ZoomPoint;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.type.SymbolProperty;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.LocationTypeModel;
import org.opentcs.guing.util.DefaultLocationThemeManager;
import org.opentcs.guing.util.LocationThemeManager;
import org.opentcs.util.gui.plugins.LocationTheme;

/**
 * Ein Figure für Stationen (Übergabestationen, Batterieladestationen) und
 * Geräte (Aufzüge, Drehteller und so weiter).
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class LocationFigure
    extends TCSFigure
    implements ImageObserver {

  /**
   * This class's logger.
   */
  private static final Logger log
      = Logger.getLogger(LocationFigure.class.getName());
  // Der Dateiname des Bildes
  private String fImageFileName;
  // Der Dateiname des alternativen Bildes zur Darstellung von "Blinken"
  private String fFlashImageFileName;
  // Das Bild
  private transient Image fImage;
  // Alternatives Bild zur Darstellung von "Blinken"
  private transient Image fFlashImage;
  // Die Ausdehnung der Figur
  private int fWidth, fHeight;
  // Timer zur Darstellung eines "blinkenden" Bildes
  private boolean showFlashImage;
  private Timer flashTimer;
////	// Label
////	protected String fLabel;
  private final LocationThemeManager locationThemeManager;

  /**
   * Creates a new instance.
   *
   * @param model The corresponding model object.
   */
  public LocationFigure(LocationModel model) {
    super(model);
    fWidth = fHeight = 30;
    fDisplayBox = new Rectangle(fWidth, fHeight);
    fZoomPoint = new ZoomPoint(0.5 * fWidth, 0.5 * fHeight);
    locationThemeManager = DefaultLocationThemeManager.getInstance();
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

  @Override	// Figure
  public Object getTransformRestoreData() {
    return fDisplayBox.clone();
  }

  @Override	// Figure
  public void restoreTransformTo(Object restoreData) {
    Rectangle r = (Rectangle) restoreData;
    fDisplayBox.x = r.x;
    fDisplayBox.y = r.y;
    fDisplayBox.width = r.width;
    fDisplayBox.height = r.height;
    fZoomPoint.setX(r.x + 0.5 * r.width);
    fZoomPoint.setY(r.y + 0.5 * r.height);
  }

  @Override	// Figure
  public void transform(AffineTransform tx) {
    // TODO: Beim Draggen soll der Zoompoint immer auf das Raster des Gridconstrainers einrasten
    Point2D center = getZoomPoint().getPixelLocationExactly();
    setBounds((Point2D.Double) tx.transform(center, center), null);
  }

  @Override	// AbstractFigure
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

  @Override	// AbstractAttributedFigure
  protected void drawFill(Graphics2D g) {
    int dx, dy;
    Rectangle r = displayBox();
    g.fillRect(r.x, r.y, r.width, r.height);

    if (fImage == null) {
      if (fImageFileName != null) {
        fImage = loadImage(fImageFileName);
      }
    }
    // Alternatives Bild
    if (fFlashImage == null) {
      if (fFlashImageFileName != null) {
        fFlashImage = loadImage(fFlashImageFileName);
      }
    }

    if (fImage != null) {
      if (showFlashImage && fFlashImage != null) {
        dx = (r.width - fFlashImage.getWidth(this)) / 2;
        dy = (r.height - fFlashImage.getHeight(this)) / 2;
        g.drawImage(fFlashImage, r.x + dx, r.y + dy, this);
      }
      else {
        dx = (r.width - fImage.getWidth(this)) / 2;
        dy = (r.height - fImage.getHeight(this)) / 2;
        g.drawImage(fImage, r.x + dx, r.y + dy, this);
      }
    }

////		// Text
////		g.setPaint(new Color(1.0f, 0.2f, 0.4f, 0.7f));
////		Font font = new Font("Dialog", Font.PLAIN, 16);
////		FontMetrics fontMetrics = g.getFontMetrics(font);
////		dx = -fontMetrics.stringWidth(fLabel) / 2 - 1;
////		dy = fontMetrics.getHeight() / 4;
////		g.setFont(font);
////		g.drawString(fLabel, (int) (r.getCenterX() + dx), (int) (r.getCenterY() + dy));
  }

  @Override	// AbstractAttributedFigure
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
   * Symbol für die Station geändert wird.
   *
   * @param e
   */
  public void propertiesChanged(AttributesChangeEvent e) {
    LocationTypeModel locationType = getModel().getLocationType();
    LocationTheme theme = locationThemeManager.getDefaultTheme();
//	// Ein Text, der im Bild dargestellt wird
//	StringProperty pLabel = (StringProperty) model.getProperty(LocationModel.LABEL);
//	fLabel = pLabel.getText();

    if (locationType != null) {
      // Ein Symbol für diese Location 
      SymbolProperty pSymbol = (SymbolProperty) getModel().getProperty(ObjectPropConstants.LOC_DEFAULT_REPRESENTATION);
      LocationRepresentation locationRepresentation = pSymbol.getLocationRepresentation();
      // Wenn für diese Location kein eigenes Symbol spezifiziert ist, ...
      if (locationRepresentation == null) {
        // ... das Default-Symbol des zugehörigen LocationTypes verwenden
        pSymbol = (SymbolProperty) locationType.getProperty(ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION);
        locationRepresentation = pSymbol.getLocationRepresentation();
        fImageFileName = theme.getImagePathFor(locationRepresentation);
        fImage = loadImage(fImageFileName);
      }
      else {
        // ... sonst das eigene Symbol verwenden
        fImageFileName = theme.getImagePathFor(locationRepresentation);
        fImage = loadImage(fImageFileName);
        // Wenn das Symbol blinken soll: Abwechselnd das eigene Symbol ... 
        if (theme.getImagePathFor(locationRepresentation) != null
            && (theme.getImagePathFor(locationRepresentation)).contains("flash")) {
          fImageFileName = theme.getImagePathFor(locationRepresentation);
          fImage = loadImage(fImageFileName);
          // ... und das des LocationTypes zeichnen
          pSymbol = (SymbolProperty) locationType.getProperty(ObjectPropConstants.LOCTYPE_DEFAULT_REPRESENTATION);
          locationRepresentation = pSymbol.getLocationRepresentation();
          fFlashImageFileName = theme.getImagePathFor(locationRepresentation);
          fFlashImage = loadImage(fFlashImageFileName);
          initTimer();
        }
        else {
          fFlashImage = null;
          fFlashImageFileName = null;
          stopTimer();
        }
      }
    }

    if (fImage != null) {
      fWidth = Math.max(fImage.getWidth(this) + 10, 30);
      fHeight = Math.max(fImage.getHeight(this) + 10, 30);
      fDisplayBox.setSize(fWidth, fHeight);
    }
  }

  @Override	// ImageObserver
  public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
    if ((infoflags & (FRAMEBITS | ALLBITS)) != 0) {
      invalidate();
    }

    return (infoflags & (ALLBITS | ABORT)) == 0;
  }

  private void initTimer() {
    if (flashTimer == null) {
      flashTimer = new Timer(1000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          showFlashImage = !showFlashImage;
          fireFigureChanged();
        }
      });
    }

    flashTimer.restart();
  }

  private void stopTimer() {
    if (flashTimer != null) {
      flashTimer.stop();
    }

    showFlashImage = false;
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
