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
import javax.inject.Provider;
import javax.swing.JFrame;
import net.engio.mbassy.listener.Handler;
import org.jhotdraw.app.SDIApplication;
import org.jhotdraw.app.View;
import org.opentcs.guing.application.action.file.CloseFileAction;
import org.opentcs.guing.application.menus.menubar.ApplicationMenuBar;
import org.opentcs.guing.event.ModelNameChangeEvent;
import org.opentcs.guing.model.ModelManager;
import org.opentcs.guing.util.PlantOverviewApplicationConfiguration;
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
   * A provider for the menu bar.
   */
  private final Provider<ApplicationMenuBar> menuBarProvider;
  /**
   * Provides the current system model.
   */
  private final ModelManager modelManager;
  /**
   * The application's configuration.
   */
  private final PlantOverviewApplicationConfiguration appConfig;
  /**
   * Provides the application's current state.
   */
  private final ApplicationState appState;

  /**
   * Creates a new instance.
   *
   * @param frame The frame in which the OpenTCSView is to be shown.
   * @param menuBarProvider Provides the main application menu bar.
   * @param modelManager Provides the current system model.
   * @param appConfig The application's configuration.
   * @param appState Provides the application's current state.
   */
  @Inject
  public OpenTCSSDIApplication(@ApplicationFrame JFrame frame,
                               Provider<ApplicationMenuBar> menuBarProvider,
                               ModelManager modelManager,
                               PlantOverviewApplicationConfiguration appConfig,
                               ApplicationState appState) {
    this.contentFrame = requireNonNull(frame, "frame");
    this.menuBarProvider = requireNonNull(menuBarProvider, "menuBarProvider");
    this.modelManager = requireNonNull(modelManager, "modelManager");
    this.appConfig = requireNonNull(appConfig, "appConfig");
    this.appState = requireNonNull(appState, "appState");
  }

  @Override
  public void show(final View view) {
    requireNonNull(view, "view");

    if (view.isShowing()) {
      return;
    }
    view.setShowing(true);

    final OpenTCSView opentcsView = (OpenTCSView) view;

    setupContentFrame(opentcsView);

    opentcsView.addPropertyChangeListener(new TitleUpdater(opentcsView));
    updateViewTitle(view, contentFrame);

    // The frame should be shown only after the view has been initialized.
    opentcsView.start();
    contentFrame.setVisible(true);
  }

  @Override
  protected void updateViewTitle(View view, JFrame frame) {
    requireNonNull(view, "view");
    requireNonNull(frame, "frame");

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
    ApplicationMenuBar menuBar = menuBarProvider.get();
    menuBar.setOperationMode(appState.getOperationMode());
    contentFrame.setJMenuBar(menuBar);

    contentFrame.setIconImages(Icons.getOpenTCSIcons());
    contentFrame.setSize(1024, 768);

    // Restore the window's dimensions from the configuration.
    contentFrame.setExtendedState(appConfig.frameMaximized() ? Frame.MAXIMIZED_BOTH : Frame.NORMAL);

    if (contentFrame.getExtendedState() != Frame.MAXIMIZED_BOTH) {
      contentFrame.setBounds(appConfig.frameBoundsX(),
                             appConfig.frameBoundsY(),
                             appConfig.frameBoundsWidth(),
                             appConfig.frameBoundsHeight());
    }

    contentFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
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
      // Check if changes to the model still need to be saved.
      getAction(opentcsView, CloseFileAction.ID).actionPerformed(
          new ActionEvent(contentFrame,
                          ActionEvent.ACTION_PERFORMED,
                          CloseFileAction.ID_WINDOW_CLOSING));
    }

    @Override
    public void windowClosed(WindowEvent e) {
      opentcsView.stop();
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {
      setActiveView(opentcsView);
    }
  }
}
