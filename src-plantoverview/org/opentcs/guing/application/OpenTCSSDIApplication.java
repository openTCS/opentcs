/**
 * (c): IML, JHotDraw.
 *
 */
package org.opentcs.guing.application;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.JFrame;
import org.jhotdraw.app.SDIApplication;
import org.jhotdraw.app.View;
import org.opentcs.access.Kernel;
import org.opentcs.guing.application.action.file.CloseFileAction;
import org.opentcs.guing.components.drawing.OpenTCSDrawingView;
import org.opentcs.guing.exchange.KernelProxyManager;
import org.opentcs.util.configuration.ConfigurationStore;
import org.opentcs.util.gui.Icons;

/**
 * The enclosing SDI application.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class OpenTCSSDIApplication
    extends SDIApplication {

  /**
   * This classes configuration store.
   */
  private static final ConfigurationStore configStore
      = ConfigurationStore.getStore(OpenTCSView.class.getName());
  /**
   * The JFrame in which the OpenTCSView is shown. May be null.
   */
  private final JFrame contentFrame;
  /**
   * The proxy/connection manager to be used.
   */
  private final KernelProxyManager kernelProxyManager;

  /**
   * Creates a new instance.
   *
   * @param frame The frame in which the OpenTCSView is to be shown.
   * @param kernelProxyManager The proxy/connection manager to be used.
   */
  @Inject
  public OpenTCSSDIApplication(@ApplicationFrame JFrame frame,
                               KernelProxyManager kernelProxyManager) {
    this.contentFrame = requireNonNull(frame, "frame");
    this.kernelProxyManager = requireNonNull(kernelProxyManager,
                                             "kernelProxyManager");
  }

  @Override // SDIApplication
  public void show(final View view) {
    requireNonNull(view, "view is null");

    final OpenTCSView opentcsView = (OpenTCSView) view;
    if (opentcsView.isShowing()) {
      return;
    }
    opentcsView.setShowing(true);

    if (contentFrame != null) {
      setupContentFrame(opentcsView);
    }
    // Bei Änderungen am Modell wird in der View HAS_UNSAVED_CHANGES_PROPERTY gesetzt.
    // Beim Laden eines neuen Modells wird in der View MODELNAME_PROPERTY gesetzt.
    // Beim Umschalten der Kernel-Betriebsart wird in der View OPERATIONMODE_PROPERTY gesetzt.
    // Damit wird die Titelzeile aktualisiert
    opentcsView.addPropertyChangeListener(new TitleUpdater(opentcsView));

    // The frame should be shown only after the view has been initialized.
    opentcsView.start();
    if (contentFrame != null) {
      contentFrame.setVisible(true);
    }
  }

  @Override
  protected void updateViewTitle(View view, JFrame frame) {
    requireNonNull(view, "view is null");
    requireNonNull(frame, "frame is null");

    OpenTCSView opentcsView = (OpenTCSView) view;
    opentcsView.updateModelName();
    if (frame != null) {
      frame.setTitle(OpenTCSView.NAME + " - "
          + opentcsView.getKernelState() + " - \""
          + opentcsView.getModelName() + "\"");
    }
  }

  private void setupContentFrame(OpenTCSView opentcsView) {
    // Das OpenTCSView-Panel in den Frame zeichnen
    // Menu erzeugen...
    // ... und im Frame anzeigen
    contentFrame.setJMenuBar(opentcsView.getMenuBar());
    // Ein Icon für den Frame
    contentFrame.setIconImages(Icons.getOpenTCSIcons());
    // Größe des Frames
    contentFrame.setSize(1024, 768);	// Default size

    // Fenster-Dimensionen aus openTCS Configuration laden
    int extendedState = configStore.getInt("FRAME_EXTENDED_STATE",
                                           contentFrame.getExtendedState());
    contentFrame.setExtendedState(extendedState);

    if (contentFrame.getExtendedState() != Frame.MAXIMIZED_BOTH) {
      int xPos = configStore.getInt("FRAME_X_POS", contentFrame.getBounds().x);
      int yPos = configStore.getInt("FRAME_Y_POS", contentFrame.getBounds().y);
      int width = configStore.getInt("FRAME_WIDTH", contentFrame.getBounds().width);
      int height = configStore.getInt("FRAME_HEIGHT", contentFrame.getBounds().height);

      contentFrame.setBounds(xPos, yPos, width, height);
    }

    final Kernel kernel = kernelProxyManager.kernel();
    if (kernel.getCurrentModelName().equals(configStore.getString("LAST_MODEL", ""))) {
      int n = 1;
      for (OpenTCSDrawingView drawView : opentcsView.getDrawingViews()) {
        int centerX = configStore.getInt("VIEW_X_" + n, 0);
        int centerY = configStore.getInt("VIEW_Y_" + n, 0);
        double scaleFactor = configStore.getDouble("VIEW_SCALEFACTOR_" + n,
                                                   drawView.getScaleFactor());
        opentcsView.scaleAndScrollTo(drawView, scaleFactor, centerX, centerY);
        n++;
      }
    }

    // Action "Frame schließen" abfangen
    contentFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    // Fenster-Dimensionen beim Schließen in openTCS Configuration speichern
    contentFrame.addWindowListener(new WindowStatusUpdater(opentcsView, kernel));
  }

  private class TitleUpdater
      implements PropertyChangeListener {

    private final OpenTCSView opentcsView;

    public TitleUpdater(OpenTCSView opentcsView) {
      this.opentcsView = requireNonNull(opentcsView, "opentcsView");
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      String name = evt.getPropertyName();

      if (name.equals(View.HAS_UNSAVED_CHANGES_PROPERTY)
          || name.equals(OpenTCSView.MODELNAME_PROPERTY)
          || name.equals(OpenTCSView.OPERATIONMODE_PROPERTY)) {
        updateViewTitle(opentcsView, contentFrame);
      }
    }
  }

  private class WindowStatusUpdater
      extends WindowAdapter {

    private final OpenTCSView opentcsView;
    private final Kernel kernel;

    public WindowStatusUpdater(OpenTCSView opentcsView, Kernel kernel) {
      this.opentcsView = requireNonNull(opentcsView, "opentcsView");
      this.kernel = requireNonNull(kernel, "kernel");
    }

    @Override
    public void windowClosing(WindowEvent e) {
      // Called when the window is being closed.
      // Check if changes to the model still need to be saved.
      getAction(opentcsView, CloseFileAction.ID).actionPerformed(
          new ActionEvent(contentFrame,
                          ActionEvent.ACTION_PERFORMED,
                          CloseFileAction.ID_WINDOW_CLOSING));
    }

    @Override
    public void windowClosed(WindowEvent e) {
      configStore.setInt("FRAME_EXTENDED_STATE", contentFrame.getExtendedState());
      configStore.setInt("FRAME_X_POS", contentFrame.getBounds().x);
      configStore.setInt("FRAME_Y_POS", contentFrame.getBounds().y);
      configStore.setInt("FRAME_WIDTH", contentFrame.getBounds().width);
      configStore.setInt("FRAME_HEIGHT", contentFrame.getBounds().height);
      // Convert coordinates in the drawing
      configStore.setString("LAST_MODEL", kernel.getCurrentModelName());
      int n = 1;
      for (OpenTCSDrawingView drawView : opentcsView.getOperatingDrawingViews()) {
        Rectangle2D.Double visibleViewRect
            = drawView.viewToDrawing(drawView.getVisibleRect());
        int centerX = (int) visibleViewRect.getCenterX();
        int centerY = (int) -visibleViewRect.getCenterY();
        configStore.setInt("VIEW_X_" + n, centerX);
        configStore.setInt("VIEW_Y_" + n, centerY);
        configStore.setDouble("VIEW_SCALEFACTOR_" + n, drawView.getScaleFactor());
        n++;
      }

      opentcsView.stop();
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {
      setActiveView(opentcsView);
    }
  }
}
