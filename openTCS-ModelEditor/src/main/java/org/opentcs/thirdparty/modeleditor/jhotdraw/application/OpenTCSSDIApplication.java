/**
 * (c): IML, JHotDraw.
 *
 */
package org.opentcs.thirdparty.modeleditor.jhotdraw.application;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import static java.util.Objects.requireNonNull;
import java.util.ResourceBundle;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.JFrame;
import org.jhotdraw.app.SDIApplication;
import org.jhotdraw.app.View;
import org.opentcs.common.PortalManager;
import static org.opentcs.common.PortalManager.ConnectionState.CONNECTED;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.customizations.plantoverview.ApplicationFrame;
import org.opentcs.guing.common.event.ModelNameChangeEvent;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.modeleditor.application.OpenTCSView;
import org.opentcs.modeleditor.application.menus.menubar.ApplicationMenuBar;
import org.opentcs.modeleditor.util.I18nPlantOverviewModeling;
import org.opentcs.thirdparty.modeleditor.jhotdraw.application.action.file.CloseFileAction;
import org.opentcs.util.event.EventHandler;
import org.opentcs.util.event.EventSource;
import org.opentcs.util.gui.Icons;

/**
 * The enclosing SDI application.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class OpenTCSSDIApplication
    extends SDIApplication
    implements EventHandler {

  private static final ResourceBundle BUNDLE
      = ResourceBundle.getBundle(I18nPlantOverviewModeling.SYSTEM_PATH);
  /**
   * Whether the GUI window should be maximized on startup.
   */
  private static final boolean FRAME_MAXIMIZED = false;
  /**
   * The GUI window's x-coordinate on screen in pixels.
   */
  private static final int FRAME_BOUNDS_X = 0;
  /**
   * The GUI window's y-coordinate on screen in pixels.
   */
  private static final int FRAME_BOUNDS_Y = 0;
  /**
   * The GUI window's height in pixels.
   */
  private static final int FRAME_BOUNDS_HEIGHT = 768;
  /**
   * The GUI window's width in pixels.
   */
  private static final int FRAME_BOUNDS_WIDTH = 1024;
  /**
   * The JFrame in which the OpenTCSView is shown.
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
   * Where we register for application events.
   */
  private final EventSource eventSource;
  /**
   * The portal manager.
   */
  private final PortalManager portalManager;

  /**
   * Creates a new instance.
   *
   * @param frame The frame in which the OpenTCSView is to be shown.
   * @param menuBarProvider Provides the main application menu bar.
   * @param modelManager Provides the current system model.
   * @param eventSource Where this instance registers for application events.
   * @param portalManager The portal manager.
   */
  @Inject
  public OpenTCSSDIApplication(@ApplicationFrame JFrame frame,
                               Provider<ApplicationMenuBar> menuBarProvider,
                               ModelManager modelManager,
                               @ApplicationEventBus EventSource eventSource,
                               PortalManager portalManager) {
    this.contentFrame = requireNonNull(frame, "frame");
    this.menuBarProvider = requireNonNull(menuBarProvider, "menuBarProvider");
    this.modelManager = requireNonNull(modelManager, "modelManager");
    this.eventSource = requireNonNull(eventSource, "eventSource");
    this.portalManager = requireNonNull(portalManager, "portalManager");
  }

  @Override
  public void show(final View view) {
    requireNonNull(view, "view");

    if (view.isShowing()) {
      return;
    }
    view.setShowing(true);

    eventSource.subscribe(this);

    final OpenTCSView opentcsView = (OpenTCSView) view;

    setupContentFrame(opentcsView);

    TitleUpdater titleUpdater = new TitleUpdater(opentcsView);
    opentcsView.addPropertyChangeListener(titleUpdater);
    eventSource.subscribe(titleUpdater);

    updateViewTitle(view, contentFrame);

    // The frame should be shown only after the view has been initialized.
    opentcsView.start();
    contentFrame.setVisible(true);
  }

  @Override
  protected void updateViewTitle(View view, JFrame frame) {
    ((OpenTCSView) view).updateModelName();
  }

  @Override
  public void onEvent(Object event) {
    if (event instanceof ModelNameChangeEvent) {
      ModelNameChangeEvent modelNameChangeEvent = (ModelNameChangeEvent) event;
      updateViewTitle((OpenTCSView) modelNameChangeEvent.getSource(), contentFrame);
    }
  }

  private void updateViewTitle(OpenTCSView view, JFrame frame) {
    String modelName = modelManager.getModel().getName();
    if (view.hasUnsavedChanges()) {
      modelName += "*";
    }

    if (frame != null) {
      frame.setTitle(OpenTCSView.NAME + " - \""
          + modelName + "\" - "
          + BUNDLE.getString("openTcsSdiApplication.frameTitle_connectedTo.text") + portalManager.getDescription()
          + " (" + portalManager.getHost() + ":" + portalManager.getPort() + ")");
    }
  }

  private void setupContentFrame(OpenTCSView opentcsView) {
    contentFrame.setJMenuBar(menuBarProvider.get());

    contentFrame.setIconImages(Icons.getOpenTCSIcons());
    contentFrame.setSize(1024, 768);

    // Restore the window's dimensions from the configuration.
    contentFrame.setExtendedState(FRAME_MAXIMIZED ? Frame.MAXIMIZED_BOTH : Frame.NORMAL);

    if (contentFrame.getExtendedState() != Frame.MAXIMIZED_BOTH) {
      contentFrame.setBounds(FRAME_BOUNDS_X,
                             FRAME_BOUNDS_Y,
                             FRAME_BOUNDS_WIDTH,
                             FRAME_BOUNDS_HEIGHT);
    }

    contentFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    contentFrame.addWindowListener(new WindowStatusUpdater(opentcsView));
  }

  private class TitleUpdater
      implements PropertyChangeListener,
                 EventHandler {

    private final OpenTCSView opentcsView;

    public TitleUpdater(OpenTCSView opentcsView) {
      this.opentcsView = requireNonNull(opentcsView, "opentcsView");
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      String name = evt.getPropertyName();

      if (name.equals(View.HAS_UNSAVED_CHANGES_PROPERTY)) {
        updateViewTitle(opentcsView, contentFrame);
      }
    }

    @Override
    public void onEvent(Object event) {
      if (event instanceof PortalManager.ConnectionState) {
        PortalManager.ConnectionState connectionState = (PortalManager.ConnectionState) event;
        switch (connectionState) {
          case CONNECTED:
            break;
          case DISCONNECTED:
            break;
          default:
        }
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
