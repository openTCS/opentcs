/**
 * (c): IML, JHotDraw.
 *
 */
package org.opentcs.guing.application;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.JFrame;
import net.engio.mbassy.listener.Handler;
import org.jhotdraw.app.SDIApplication;
import org.jhotdraw.app.View;
import org.opentcs.access.Kernel;
import org.opentcs.access.SharedKernelProvider;
import org.opentcs.guing.application.action.file.CloseFileAction;
import org.opentcs.guing.components.drawing.OpenTCSDrawingView;
import org.opentcs.guing.event.ModelNameChangeEvent;
import org.opentcs.guing.model.ModelManager;
import org.opentcs.guing.util.ApplicationConfiguration;
import org.opentcs.util.gui.Icons;

/**
 * The enclosing SDI application.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class OpenTCSSDIApplication
    extends SDIApplication {

  /**
   * The JFrame in which the OpenTCSView is shown. May be null.
   */
  private final JFrame contentFrame;
  /**
   * Provides access to a kernel.
   */
  private final SharedKernelProvider kernelProvider;
  /**
   * Provides the current system model.
   */
  private final ModelManager modelManager;
  /**
   * The application's configuration.
   */
  private final ApplicationConfiguration appConfig;

  /**
   * Creates a new instance.
   *
   * @param frame The frame in which the OpenTCSView is to be shown.
   * @param kernelProvider Provides a access to a kernel.
   * @param modelManager Provides the current system model.
   * @param appConfig The application's configuration.
   */
  @Inject
  public OpenTCSSDIApplication(@ApplicationFrame JFrame frame,
                               SharedKernelProvider kernelProvider,
                               ModelManager modelManager,
                               ApplicationConfiguration appConfig) {
    this.contentFrame = requireNonNull(frame, "frame");
    this.kernelProvider = requireNonNull(kernelProvider, "kernelProvider");
    this.modelManager = requireNonNull(modelManager, "modelManager");
    this.appConfig = requireNonNull(appConfig, "appConfig");
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
    updateViewTitle(view, contentFrame);

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

    String modelName = modelManager.getModel().getName();
    if (opentcsView.hasUnsavedChanges()) {
      modelName += "*";
    }

    if (frame != null) {
      frame.setTitle(OpenTCSView.NAME + " - "
          + opentcsView.getPlantOverviewState() + " - \""
          + modelName + "\"");
    }
  }

  @Handler
  public void modelNameChange(ModelNameChangeEvent event) {
    OpenTCSView opentcsView = (OpenTCSView) event.getSource();
    String modelName = modelManager.getModel().getName();
    if (opentcsView.hasUnsavedChanges()) {
      modelName += "*";
    }

    if (contentFrame != null) {
      contentFrame.setTitle(OpenTCSView.NAME + " - "
          + opentcsView.getPlantOverviewState() + " - \""
          + modelName + "\"");
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
    contentFrame.setSize(1024, 768); // Default size

    // Restore the window's dimensions from the configuration.
    contentFrame.setExtendedState(appConfig.getFrameExtendedState());

    if (contentFrame.getExtendedState() != Frame.MAXIMIZED_BOTH) {
      contentFrame.setBounds(appConfig.getFrameBounds());
    }

    if (kernelProvider.kernelShared()) {
      final Kernel kernel = kernelProvider.getKernel();
      if (kernel.getLoadedModelName()
          .equals(appConfig.getLastLoadedModelName())) {
        int n = 1;
        for (OpenTCSDrawingView drawView : opentcsView
            .getOperatingDrawingViews()) {
          opentcsView.scaleAndScrollTo(drawView, appConfig
                                       .getDrawingViewBookmark(n));
          n++;
        }
      }

    }
    // Action "Frame schließen" abfangen
    contentFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    // Fenster-Dimensionen beim Schließen in openTCS Configuration speichern
    contentFrame.addWindowListener(new WindowStatusUpdater(opentcsView));
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
          || name.equals(OpenTCSView.OPERATIONMODE_PROPERTY)) {
        updateViewTitle(opentcsView, contentFrame);
      }
    }
  }

  private class WindowStatusUpdater
      extends WindowAdapter {

    private final OpenTCSView opentcsView;

    public WindowStatusUpdater(OpenTCSView opentcsView) {
      this.opentcsView = requireNonNull(opentcsView, "opentcsView");
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
      appConfig.setFrameExtendedState(contentFrame.getExtendedState());
      appConfig.setFrameBounds(contentFrame.getBounds());
      appConfig.setLastLoadedModelName(modelManager.getModel().getName());
      int n = 1;
      for (OpenTCSDrawingView drawView : opentcsView.getOperatingDrawingViews()) {
        appConfig.setDrawingViewBookmark(n, drawView.bookmark());
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
