/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.event.CVetoClosingEvent;
import bibliothek.gui.dock.common.event.CVetoClosingListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import org.jhotdraw.app.AbstractView;
import org.jhotdraw.app.action.window.ToggleVisibleAction;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.gui.URIChooser;
import org.jhotdraw.util.ReversedList;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.access.SharedKernelServicePortal;
import org.opentcs.access.SharedKernelServicePortalProvider;
import org.opentcs.components.kernel.services.ServiceUnavailableException;
import org.opentcs.components.plantoverview.PlantModelExporter;
import org.opentcs.components.plantoverview.PlantModelImporter;
import org.opentcs.components.plantoverview.PluggablePanel;
import org.opentcs.components.plantoverview.PluggablePanelFactory;
import org.opentcs.customizations.ApplicationEventBus;
import org.opentcs.customizations.plantoverview.ApplicationFrame;
import org.opentcs.data.notification.UserNotification;
import org.opentcs.guing.application.action.ToolBarManager;
import org.opentcs.guing.application.action.ViewActionMap;
import org.opentcs.guing.application.action.edit.UndoRedoManager;
import org.opentcs.guing.application.action.file.CloseFileAction;
import org.opentcs.guing.application.toolbar.PaletteToolBarBorder;
import org.opentcs.guing.components.dialogs.VehiclesPanel;
import org.opentcs.guing.components.dockable.DockableHandlerFactory;
import org.opentcs.guing.components.dockable.DockingManager;
import org.opentcs.guing.components.dockable.DrawingViewFocusHandler;
import org.opentcs.guing.components.drawing.DrawingViewFactory;
import org.opentcs.guing.components.drawing.DrawingViewScrollPane;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.components.drawing.OpenTCSDrawingView;
import org.opentcs.guing.components.drawing.course.Origin;
import org.opentcs.guing.components.drawing.course.OriginChangeListener;
import org.opentcs.guing.components.drawing.figures.FigureConstants;
import org.opentcs.guing.components.drawing.figures.LabeledFigure;
import org.opentcs.guing.components.drawing.figures.LabeledPointFigure;
import org.opentcs.guing.components.drawing.figures.SimpleLineConnection;
import org.opentcs.guing.components.drawing.figures.TCSFigure;
import org.opentcs.guing.components.drawing.figures.VehicleFigure;
import org.opentcs.guing.components.properties.SelectionPropertiesComponent;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.components.properties.panel.PropertiesPanelFactory;
import org.opentcs.guing.components.properties.type.AbstractProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.tree.BlocksTreeViewManager;
import org.opentcs.guing.components.tree.ComponentsTreeViewManager;
import org.opentcs.guing.components.tree.GroupsTreeViewManager;
import org.opentcs.guing.components.tree.TreeViewManager;
import org.opentcs.guing.components.tree.elements.ContextObject;
import org.opentcs.guing.components.tree.elements.UserObject;
import org.opentcs.guing.components.tree.elements.UserObjectContext;
import org.opentcs.guing.components.tree.elements.UserObjectContext.ContextType;
import org.opentcs.guing.components.tree.elements.UserObjectUtil;
import org.opentcs.guing.components.tree.elements.VehicleUserObject;
import org.opentcs.guing.event.BlockChangeEvent;
import org.opentcs.guing.event.BlockChangeListener;
import org.opentcs.guing.event.DrawingEditorEvent;
import org.opentcs.guing.event.DrawingEditorListener;
import org.opentcs.guing.event.KernelStateChangeEvent;
import org.opentcs.guing.event.ModelNameChangeEvent;
import org.opentcs.guing.event.OperationModeChangeEvent;
import org.opentcs.guing.event.ResetInteractionToolCommand;
import org.opentcs.guing.event.SystemModelTransitionEvent;
import org.opentcs.guing.exchange.TransportOrderUtil;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.PropertiesCollection;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.BlockModel;
import org.opentcs.guing.model.elements.GroupModel;
import org.opentcs.guing.model.elements.LayoutModel;
import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.LocationTypeModel;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.persistence.ModelManager;
import org.opentcs.guing.transport.OrderSequencesContainerPanel;
import org.opentcs.guing.transport.TransportOrdersContainerPanel;
import org.opentcs.guing.util.Colors;
import org.opentcs.guing.util.CourseObjectFactory;
import org.opentcs.guing.util.Cursors;
import org.opentcs.guing.util.I18nPlantOverview;
import org.opentcs.guing.util.PanelRegistry;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.guing.util.UniqueNameGenerator;
import org.opentcs.guing.util.UserMessageHelper;
import org.opentcs.util.event.EventBus;
import org.opentcs.util.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Visualizes the driving course and other kernel objects as well as messages
 * received by the kernel.
 * (Contains everything underneath the tool bars.)
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class OpenTCSView
    extends AbstractView
    implements GuiManager,
               EventHandler {

  /**
   * The name/title of this application.
   */
  public static final String NAME
      = ResourceBundleUtil.getBundle(I18nPlantOverview.MISC_PATH).getString("openTcsView.applicationName.text");
  /**
   * Property key for the kernel's current mode of operation.
   */
  public static final String OPERATIONMODE_PROPERTY = "operationMode";
  /**
   * Property key for the currently loaded driving course model.
   * The corresponding value contains a "*" if the model has been modified.
   */
  public static final String MODELNAME_PROPERTY = "modelName";
  /**
   *
   */
  public static final String TOOLBAR_ACTIONS_PROPERTY = "toolBarActions";
  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(OpenTCSView.class);
  /**
   * A single central instance (singleton) of this class.
   * XXX This should be removed as soon as instance() isn't called by other
   * classes any more and the singleton is only injected via Guice.
   */
  private static OpenTCSView instance;
  /**
   * This instance's resource bundle.
   */
  private final ResourceBundleUtil bundle
      = ResourceBundleUtil.getBundle(I18nPlantOverview.MISC_PATH);
  /**
   * Provides/manages the application's current state.
   */
  private final ApplicationState appState;
  /**
   * Allows for undoing and redoing actions.
   */
  private final UndoRedoManager fUndoRedoManager;
  /**
   * The drawing editor.
   */
  private final OpenTCSDrawingEditor fDrawingEditor;
  /**
   * The JFrame.
   */
  private final JFrame fFrame;
  /**
   * Utility to manage the views.
   */
  private final ViewManager viewManager;
  /**
   * A manager for the components tree view.
   */
  private final TreeViewManager fComponentsTreeManager;
  /**
   * A manager for the blocks tree view.
   */
  private final TreeViewManager fBlocksTreeManager;
  /**
   * A manager for the groups tree view.
   */
  private final TreeViewManager fGroupsTreeManager;
  /**
   * Displays properties of the currently selected model component(s).
   */
  private final SelectionPropertiesComponent fPropertiesComponent;
  /**
   * Manages driving course models.
   */
  private final ModelManager fModelManager;
  /**
   * Indicates the progress for lengthy operations.
   */
  private final ProgressIndicator progressIndicator;
  /**
   * Manages docking frames.
   */
  private final DockingManager dockingManager;
  /**
   * Registry for plugin panels.
   */
  private final PanelRegistry panelRegistry;
  /**
   * Provides access to a portal.
   */
  private final SharedKernelServicePortalProvider portalProvider;
  /**
   * The shared portal to be used.
   */
  private SharedKernelServicePortal sharedPortal;
  /**
   * A panel that displays kernel messages.
   */
  private final KernelStatusPanel kernelStatusPanel;
  /**
   * A panel for mouse position/status.
   */
  private final StatusPanel statusPanel;
  /**
   * A panel showing all vehicles in the system model.
   */
  private final VehiclesPanel vehiclesPanel;
  /**
   * A factory for system model objects.
   */
  private final CourseObjectFactory crsObjFactory;
  /**
   * Shows messages to the user.
   */
  private final UserMessageHelper userMessageHelper;
  /**
   * A factory for drawing views.
   */
  private final DrawingViewFactory drawingViewFactory;
  /**
   * Handles block events.
   */
  private final BlockChangeListener blockEventHandler = new BlockEventHandler();
  /**
   * Handles events for changes of properties.
   */
  private final AttributesChangeListener attributesEventHandler = new AttributesEventHandler();
  /**
   * Generates names for model objects.
   */
  private final UniqueNameGenerator modelCompNameGen;
  /**
   * A factory for UserObject instances.
   */
  private final UserObjectUtil userObjectUtil;
  /**
   * A provider for ActionMaps.
   */
  private final Provider<ViewActionMap> actionMapProvider;
  /**
   * A provider for the tool bar manager.
   */
  private final Provider<ToolBarManager> toolBarManagerProvider;
  /**
   * A factory for properties-related panels.
   */
  private final PropertiesPanelFactory propertiesPanelFactory;
  /**
   * A provider for panels showing the transport orders.
   */
  private final Provider<TransportOrdersContainerPanel> toContainerPanelProvider;
  /**
   * A provider for panels showing the order sequences.
   */
  private final Provider<OrderSequencesContainerPanel> osContainerPanelProvider;
  /**
   * A helper for creating transport orders with the kernel.
   */
  private final TransportOrderUtil orderUtil;
  /**
   * The application's event bus.
   */
  private final EventBus eventBus;
  /**
   * Handles focussing of dockables.
   */
  private final DrawingViewFocusHandler drawingViewFocusHandler;
  /**
   * A factory for handlers related to dockables.
   */
  private final DockableHandlerFactory dockableHandlerFactory;
  /**
   * Provides the application's tool bars.
   */
  private ToolBarManager toolBarManager;
  /**
   * A string representation for the current mode of operation.
   */
  private String sOperationMode = "";

  public static OpenTCSView instance() {
    return instance;
  }

  public static void setInstance(OpenTCSView view) {
    instance = view;
  }

  /**
   * Creates a new instance.
   *
   * @param appState Provides/manages the application's current state.
   * @param appFrame The <code>JFrame</code> this view is wrapped in.
   * @param progressIndicator The progress indicator to be used.
   * @param portalProvider Provides a access to a portal.
   * @param viewManager The view manager to be used.
   * @param tcsDrawingEditor The drawing editor to be used.
   * @param modelManager The model manager to be used.
   * @param statusPanel The status panel to be used.
   * @param panelRegistry The plugin panel registry to be used.
   * @param crsObjFactory The course object factory to be used.
   * @param userMessageHelper An UserMessageHelper
   * @param drawingViewFactory A factory for drawing views.
   * @param modelCompNameGen A generator for model components' names.
   * @param undoRedoManager Allows for undoing and redoing actions.
   * @param componentsTreeManager Manages the components tree view.
   * @param blocksTreeManager Manages the blocks tree view.
   * @param groupsTreeManager Manages the groups tree view.
   * @param kernelStatusPanel A panel that displays kernel messages.
   * @param propertiesComponent Displays properties of the currently selected model component(s).
   * @param vehiclesPanel A panel showing all the vehicles in the system model.
   * @param userObjectUtil A factory for UserObject instances.
   * @param actionMapProvider A provider for ActionMaps.
   * @param toolBarManagerProvider A provider for the tool bar manager.
   * @param propertiesPanelFactory A factory for properties-related panels.
   * @param toContainerPanelProvider A provider for panels showing the transport orders.
   * @param osContainerPanelProvider A provider for panels showing the order sequences.
   * @param orderUtil A helper for creating transport orders with the kernel.
   * @param eventBus The application's event bus.
   * @param dockingManager Manages docking frames.
   * @param drawingViewFocusHandler Handles focussing of dockables.
   * @param dockableHandlerFactory A factory for handlers related to dockables.
   */
  @Inject
  public OpenTCSView(ApplicationState appState,
                     @ApplicationFrame JFrame appFrame,
                     ProgressIndicator progressIndicator,
                     SharedKernelServicePortalProvider portalProvider,
                     ViewManager viewManager,
                     OpenTCSDrawingEditor tcsDrawingEditor,
                     ModelManager modelManager,
                     StatusPanel statusPanel,
                     PanelRegistry panelRegistry,
                     CourseObjectFactory crsObjFactory,
                     UserMessageHelper userMessageHelper,
                     DrawingViewFactory drawingViewFactory,
                     UniqueNameGenerator modelCompNameGen,
                     UndoRedoManager undoRedoManager,
                     ComponentsTreeViewManager componentsTreeManager,
                     BlocksTreeViewManager blocksTreeManager,
                     GroupsTreeViewManager groupsTreeManager,
                     KernelStatusPanel kernelStatusPanel,
                     SelectionPropertiesComponent propertiesComponent,
                     VehiclesPanel vehiclesPanel,
                     UserObjectUtil userObjectUtil,
                     Provider<ViewActionMap> actionMapProvider,
                     Provider<ToolBarManager> toolBarManagerProvider,
                     PropertiesPanelFactory propertiesPanelFactory,
                     Provider<TransportOrdersContainerPanel> toContainerPanelProvider,
                     Provider<OrderSequencesContainerPanel> osContainerPanelProvider,
                     TransportOrderUtil orderUtil,
                     @ApplicationEventBus EventBus eventBus,
                     DockingManager dockingManager,
                     DrawingViewFocusHandler drawingViewFocusHandler,
                     DockableHandlerFactory dockableHandlerFactory) {
    this.appState = requireNonNull(appState, "appState");
    this.fFrame = requireNonNull(appFrame, "appFrame");
    this.progressIndicator = requireNonNull(progressIndicator, "progressIndicator");
    this.portalProvider = requireNonNull(portalProvider, "portalProvider");
    this.viewManager = requireNonNull(viewManager, "viewManager");
    this.fDrawingEditor = requireNonNull(tcsDrawingEditor, "tcsDrawingEditor");
    this.fModelManager = requireNonNull(modelManager, "modelManager");
    this.statusPanel = requireNonNull(statusPanel, "statusPanel");
    this.panelRegistry = requireNonNull(panelRegistry, "panelRegistry");
    this.crsObjFactory = requireNonNull(crsObjFactory, "crsObjFactory");
    this.userMessageHelper = requireNonNull(userMessageHelper, "userMessageHelper");
    this.drawingViewFactory = requireNonNull(drawingViewFactory, "drawingViewFactory");
    this.modelCompNameGen = requireNonNull(modelCompNameGen, "modelCompNameGen");
    this.fUndoRedoManager = requireNonNull(undoRedoManager, "undoRedoManager");
    this.fComponentsTreeManager = requireNonNull(componentsTreeManager, "componentsTreeManager");
    this.fBlocksTreeManager = requireNonNull(blocksTreeManager, "blocksTreeManager");
    this.fGroupsTreeManager = requireNonNull(groupsTreeManager, "groupsTreeManager");
    this.kernelStatusPanel = requireNonNull(kernelStatusPanel, "kernelStatusPanel");
    this.fPropertiesComponent = requireNonNull(propertiesComponent, "propertiesComponent");
    this.vehiclesPanel = requireNonNull(vehiclesPanel, "vehiclesPanel");
    this.userObjectUtil = requireNonNull(userObjectUtil, "userObjectUtil");
    this.actionMapProvider = requireNonNull(actionMapProvider, "actionMapProvider");
    this.toolBarManagerProvider = requireNonNull(toolBarManagerProvider, "toolBarManagerProvider");
    this.propertiesPanelFactory = requireNonNull(propertiesPanelFactory, "propertiesPanelFactory");
    this.toContainerPanelProvider = requireNonNull(toContainerPanelProvider,
                                                   "toContainerPanelProvider");
    this.osContainerPanelProvider = requireNonNull(osContainerPanelProvider,
                                                   "osContainerPanelProvider");
    this.orderUtil = requireNonNull(orderUtil, "orderUtil");
    this.eventBus = requireNonNull(eventBus, "eventBus");
    this.dockingManager = requireNonNull(dockingManager, "dockingManager");
    this.drawingViewFocusHandler = requireNonNull(drawingViewFocusHandler,
                                                  "drawingViewFocusHandler");
    this.dockableHandlerFactory = requireNonNull(dockableHandlerFactory, "dockableHandlerFactory");
  }

  @Override // AbstractView
  public void init() {
    eventBus.subscribe(this);

    progressIndicator.setProgress(StartupProgressStatus.INITIALIZED);

    fDrawingEditor.setDrawingEditorListener(new DrawingEditorEventHandler(fModelManager));

    eventBus.subscribe(fComponentsTreeManager);

    progressIndicator.setProgress(StartupProgressStatus.INITIALIZE_MODEL);
    setSystemModel(fModelManager.getModel());

    // Properties view (lower left corner)
    fPropertiesComponent.setPropertiesContent(
        propertiesPanelFactory.createPropertiesTableContent(this));
    fPropertiesComponent.setMinimumSize(new Dimension(100, 300));

    // Register a listener for dragging vehicles around.
    VehicleDragHandler listener = new VehicleDragHandler(Cursors.getDragVehicleCursor());
    fComponentsTreeManager.addMouseListener(listener);
    fComponentsTreeManager.addMouseMotionListener(listener);

    setActionMap(actionMapProvider.get());
    this.toolBarManager = toolBarManagerProvider.get();
    eventBus.subscribe(toolBarManager);

    eventBus.subscribe(fPropertiesComponent);
    eventBus.subscribe(vehiclesPanel);
    eventBus.subscribe(viewManager);
    eventBus.subscribe(fUndoRedoManager);
    eventBus.subscribe(fDrawingEditor);

    initializeFrame();
    createEmptyModel();
  }

  @Override // AbstractView
  public void stop() {
    LOG.info("GUI terminating...");
    eventBus.unsubscribe(this);
    System.exit(0);
  }

  @Override  // AbstractView
  public void clear() {
  }

  @Override
  public void onEvent(Object event) {
    if (event instanceof SystemModelTransitionEvent) {
      handleSystemModelTransition((SystemModelTransitionEvent) event);
    }
    if (event instanceof KernelStateChangeEvent) {
      handleKernelStateChangeEvent((KernelStateChangeEvent) event);
    }
  }

  private void handleSystemModelTransition(SystemModelTransitionEvent evt) {
    switch (evt.getStage()) {
      case LOADED:
        setHasUnsavedChanges(false);
        // Update model name in title
        setModelNameProperty(fModelManager.getModel().getName());
        break;
      default:
      // Do nada.
    }
  }

  /**
   * Shows or hides the specific <code>PanelFactory</code>.
   *
   * @param factory The factory resp. panel that shall be shown / hidden.
   * @param visible True to set it visible, false otherwise.
   */
  public void showPluginPanel(PluggablePanelFactory factory, boolean visible) {
    String id = factory.getClass().getName();
    SingleCDockable dockable = dockingManager.getCControl().getSingleDockable(id);
    if (dockable != null) {
      // dockable is not null at this point when the user hides the plugin 
      // panel by clicking on its menu entry
      PluggablePanel panel = (PluggablePanel) dockable.getFocusComponent();
      panel.terminate();
      if (!dockingManager.getCControl().removeDockable(dockable)) {
        LOG.warn("Couldn't remove dockable for plugin panel factory '{}'", factory);
        return;
      }
    }
    if (!visible) {
      return;
    }
    if (factory.providesPanel(OperationMode.equivalent(appState.getOperationMode()))) {
      PluggablePanel panel
          = factory.createPanel(OperationMode.equivalent(appState.getOperationMode()));
      DefaultSingleCDockable factoryDockable = dockingManager.createFloatingDockable(
          factory.getClass().getName(),
          factory.getPanelDescription(),
          panel);
      factoryDockable.addVetoClosingListener(new CVetoClosingListener() {

        @Override
        public void closing(CVetoClosingEvent event) {
        }

        @Override
        public void closed(CVetoClosingEvent event) {
          panel.terminate();
          dockingManager.getCControl().removeDockable(factoryDockable);
        }
      });
      panel.initialize();
    }
  }

  /**
   * Adds a new drawing view to the tabbed wrappingPanel.
   *
   * @return The newly created Dockable.
   */
  public DefaultSingleCDockable addDrawingView() {
    DrawingViewScrollPane newScrollPane
        = drawingViewFactory.createDrawingView(fModelManager.getModel(),
                                               toolBarManager.getSelectionToolButton(),
                                               toolBarManager.getDragToolButton(),
                                               toolBarManager.getButtonCreateLink(),
                                               toolBarManager.getButtonCreatePath());

    int drawingViewIndex = viewManager.getNextDrawingViewIndex();

    String title = bundle.getString("openTcsView.panel_operatingDrawingView.title") + " " + drawingViewIndex;
    DefaultSingleCDockable newDockable
        = dockingManager.createDockable("drivingCourse" + drawingViewIndex,
                                        title,
                                        newScrollPane,
                                        true);
    viewManager.addDrawingView(newDockable, newScrollPane);

    // Add to group pop ups
    if (drawingViewIndex > 0) { //don't add to modelling view
      ModelComponent groups = fModelManager.getModel().getMainFolder(SystemModel.FolderKey.GROUPS);

      for (Object o : groups.getChildComponents()) {
        if (o instanceof GroupModel) {
          GroupModel gf = (GroupModel) o;
          gf.setDrawingViewVisible(title, true);
        }
      }
    }

    int lastIndex = Math.max(0, drawingViewIndex - 1);
    dockingManager.addTabTo(newDockable, DockingManager.COURSE_TAB_PANE_ID, lastIndex);

    newDockable.addVetoClosingListener(new DrawingViewClosingListener(newDockable));
    newDockable.addFocusListener(drawingViewFocusHandler);

    newScrollPane.getDrawingView().dispatchEvent(new FocusEvent(this, FocusEvent.FOCUS_GAINED));
    firePropertyChange(OpenTCSDrawingView.FOCUS_GAINED, null, newDockable);

    return newDockable;
  }

  /**
   * Adds a new transport order view.
   */
  public void addTransportOrderView() {
    int biggestIndex = viewManager.getNextTransportOrderViewIndex();
    DefaultSingleCDockable lastTOView = viewManager.getLastTransportOrderView();
    TransportOrdersContainerPanel panel = toContainerPanelProvider.get();
    DefaultSingleCDockable newDockable
        = dockingManager.createDockable("transportOrders" + biggestIndex,
                                        bundle.getString(
                                            "openTcsView.panel_operatingTransportOrdersView.title")
                                        + " " + biggestIndex, panel, true);
    viewManager.addTransportOrderView(newDockable, panel);

    panel.initView();

    newDockable.addVetoClosingListener(
        dockableHandlerFactory.createDockableClosingHandler(newDockable));

    final int indexToInsert;

    if (lastTOView != null) {
      indexToInsert = dockingManager
          .getTabPane(DockingManager.COURSE_TAB_PANE_ID)
          .getStation()
          .indexOf(lastTOView.intern()) + 1;
    }
    else {
      indexToInsert = viewManager.getDrawingViewMap().size();
    }

    dockingManager.addTabTo(newDockable, DockingManager.COURSE_TAB_PANE_ID, indexToInsert);
  }

  /**
   * Adds a new order sequence view.
   */
  public void addTransportOrderSequenceView() {
    int biggestIndex = viewManager.getNextOrderSequenceViewIndex();
    DefaultSingleCDockable lastOSView = viewManager.getLastOrderSequenceView();

    OrderSequencesContainerPanel panel = osContainerPanelProvider.get();
    DefaultSingleCDockable newDockable
        = dockingManager.createDockable("orderSequences" + biggestIndex,
                                        bundle.getString(
                                            "openTcsView.panel_operatingOrderSequencesView.title")
                                        + " " + biggestIndex,
                                        panel, true);
    viewManager.addOrderSequenceView(newDockable, panel);

    panel.initView();

    newDockable.addVetoClosingListener(
        dockableHandlerFactory.createDockableClosingHandler(newDockable));

    final int indexToInsert;
    if (lastOSView != null) {
      indexToInsert = dockingManager
          .getTabPane(DockingManager.COURSE_TAB_PANE_ID)
          .getStation()
          .indexOf(lastOSView.intern()) + 1;
    }
    else {
      indexToInsert = viewManager.getTransportOrderMap().size()
          + viewManager.getDrawingViewMap().size();
    }
    dockingManager.addTabTo(newDockable, DockingManager.COURSE_TAB_PANE_ID, indexToInsert);
  }

  /**
   * Restores the layout to default.
   */
  public void resetWindowArrangement() {
    for (DefaultSingleCDockable dock : new ArrayList<>(viewManager.getDrawingViewMap().keySet())) {
      removeDrawingView(dock);
    }
    for (DefaultSingleCDockable dock : new ArrayList<>(viewManager.getTransportOrderMap().keySet())) {
      dockingManager.removeDockable(dock);
    }
    for (DefaultSingleCDockable dock : new ArrayList<>(viewManager.getOrderSequenceMap().keySet())) {
      dockingManager.removeDockable(dock);
    }
    dockingManager.reset();
    closeOpenedPluginPanels();
    viewManager.reset();

    initializeFrame();

    // Depending on the current kernel state there may exist panels, now, that shouldn't be visible.
    new Thread(() -> setPlantOverviewState(appState.getOperationMode())).start();
  }

  /**
   * Creates a new group of elements.
   * The elements are selected from a CreateGroupPanel or from the context menu
   * of a Point / Location / Path
   *
   * @param members The components that will form that group.
   */
  public void createGroup(Set<ModelComponent> members) {
    requireNonNull(members, "members");

    if (members.isEmpty()) {
      return;
    }

    GroupModel groupModel = crsObjFactory.createGroupModel();
    for (ModelComponent member : members) {
      groupModel.add(member);
    }

    for (String name : viewManager.getDrawingViewNames()) {
      groupModel.setDrawingViewVisible(name, true);
    }

    addModelComponent(fModelManager.getModel().getMainFolder(SystemModel.FolderKey.GROUPS),
                      groupModel);
  }

  /**
   * Deletes a group.
   *
   * @param gm The folder that contains the elements of the group.
   */
  public void deleteGroup(GroupModel gm) {
    for (OpenTCSDrawingView drawingView : getDrawingViews()) {
      drawingView.setGroupVisible(gm.getChildComponents(), true);
    }
    removeModelComponent(fModelManager.getModel().getMainFolder(SystemModel.FolderKey.GROUPS), gm);
  }

  /**
   * Removes members from a group.
   *
   * @param userObjects The items that should be removed.
   * @return <code>true</code> if the UserObject was successfully removed.
   */
  public boolean removeGroupMembers(Set<UserObject> userObjects) {
    requireNonNull(userObjects, "userObjects");

    if (userObjects.isEmpty()) {
      return false;
    }

    List<ModelComponent> items = new ArrayList<>();
    for (UserObject userObject : userObjects) {
      items.add(userObject.getModelComponent());
      fGroupsTreeManager.removeItem(userObject);
    }

    for (OpenTCSDrawingView drawingView : getDrawingViews()) {
      drawingView.setGroupVisible(new ArrayList<>(items), true);
    }

    setHasUnsavedChanges(true);
    return true;
  }

  /**
   * Adds the currently selected items on the drawing view to the given group.
   *
   * @param groupModel GroupFolder referencing the group.
   */
  public void addSelectedItemsToGroup(GroupModel groupModel) {
    requireNonNull(groupModel, "groupModel");

    for (Figure figure : fDrawingEditor.getActiveView().getSelectedFigures()) {
      ModelComponent model = figure.get(FigureConstants.MODEL);

      if (model instanceof LinkModel) {
        // LinkModels are automatically set in/visible
        continue;
      }

      if (groupModel.contains(model)) {
        continue;
      }

      groupModel.add(model);
      fGroupsTreeManager.addItem(groupModel, model);
    }

    fGroupsTreeManager.getTreeView().sortChildren();
    setHasUnsavedChanges(true);
  }

  /**
   * Logs a message to the status text area.
   *
   * @param message The message to log.
   */
  private void log(UserNotification message) {
    kernelStatusPanel.display(message);
  }

  @Override  // GuiManager
  public void createEmptyModel() {
    CloseFileAction action = (CloseFileAction) getActionMap().get(CloseFileAction.ID);
    if (action != null) {
      action.actionPerformed(new ActionEvent(this,
                                             ActionEvent.ACTION_PERFORMED,
                                             CloseFileAction.ID_MODEL_CLOSING));
      if (action.getFileSavedStatus() == JOptionPane.CANCEL_OPTION) {
        return;
      }
    }

    // Clean up first...
    eventBus.onEvent(new SystemModelTransitionEvent(this,
                                                    SystemModelTransitionEvent.Stage.UNLOADING));

    eventBus.onEvent(new SystemModelTransitionEvent(this,
                                                    SystemModelTransitionEvent.Stage.UNLOADED));

    // Create the new, empty model.
    LOG.debug("Creating new driving course model...");
    fModelManager.createEmptyModel();

    eventBus.onEvent(new SystemModelTransitionEvent(this,
                                                    SystemModelTransitionEvent.Stage.LOADING));

    // Now let components set themselves up for the new model.
    setSystemModel(fModelManager.getModel());

    eventBus.onEvent(new SystemModelTransitionEvent(this,
                                                    SystemModelTransitionEvent.Stage.LOADED));

    // makes sure the origin is on the lower left side and the ruler
    // are correctly drawn
    fDrawingEditor.initializeViewport();
  }

  public void loadCurrentKernelModel() {
    try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {
      loadCurrentKernelModel(sharedPortal.getPortal());
    }
    catch (ServiceUnavailableException exc) {
      LOG.info("Kernel unavailable, aborting.", exc);
    }
  }

  /**
   * Loads the current kernel model.
   */
  private void loadCurrentKernelModel(KernelServicePortal portal) {
    if (hasUnsavedChanges()) {
      if (!showUnsavedChangesDialog()) {
        return;
      }
    }
    if (appState.hasOperationMode(OperationMode.OPERATING)
        && portal.getState() == Kernel.State.MODELLING) {
      handleKernelInModellingMode();
      return;
    }
    restoreModel(portal);
  }

  /**
   * Initializes the model stored in the kernel or in the model manager.
   *
   * @param portal If not null, the model from the given kernel will be loaded, else the model from
   * the model manager
   */
  private void restoreModel(@Nullable KernelServicePortal portal) {
    progressIndicator.initialize();

    progressIndicator.setProgress(ModelRestorationProgressStatus.CLEANUP);
    eventBus.onEvent(new SystemModelTransitionEvent(this,
                                                    SystemModelTransitionEvent.Stage.UNLOADING));

    progressIndicator.setProgress(ModelRestorationProgressStatus.START_LOADING_MODEL);
    eventBus.onEvent(new SystemModelTransitionEvent(this,
                                                    SystemModelTransitionEvent.Stage.UNLOADED));

    if (portal == null) {
      fModelManager.restoreModel();
    }
    else {
      fModelManager.restoreModel(portal);
      statusPanel.setLogMessage(Level.INFO,
                                bundle.getFormatted("openTcsView.message_modelLoaded.text",
                                                    fModelManager.getModel().getName()));
    }

    progressIndicator.setProgress(ModelRestorationProgressStatus.SET_UP_MODEL_VIEW);
    eventBus.onEvent(new SystemModelTransitionEvent(this,
                                                    SystemModelTransitionEvent.Stage.LOADING));

    setSystemModel(fModelManager.getModel());

    progressIndicator.setProgress(ModelRestorationProgressStatus.SET_UP_DIRECTORY_TREE);

    progressIndicator.setProgress(ModelRestorationProgressStatus.SET_UP_WORKING_AREA);

    ModelComponent layoutComponent
        = fModelManager.getModel().getMainFolder(SystemModel.FolderKey.LAYOUT);
    layoutComponent.addAttributesChangeListener(attributesEventHandler);

    eventBus.onEvent(new SystemModelTransitionEvent(this, SystemModelTransitionEvent.Stage.LOADED));
    updateModelName();

    progressIndicator.terminate();
  }

  /**
   * Shows a dialog telling the user the plant overview state can't be switched
   * as long as it has unsaved changes.
   *
   * @return <code>true</code> if the user saved the model or
   * discarded the changes (the program can
   * continue normally), <code>false</code> if the user pressed cancel.
   */
  private boolean showSwitchStateUnsavedChangesDialog() {
    String title = bundle.getString("openTcsView.dialog_unsavedChanges.title");
    String text = bundle.getString("openTcsView.dialog_unsavedChanges.message");
    String[] options = {bundle.getString("openTcsView.dialog_unsavedChanges.option_persist.text"),
                        bundle.getString("openTcsView.dialog_unsavedChanges.option_discard.text"),
                        bundle.getString("openTcsView.dialog_unsavedChanges.option_cancel.text")};
    switch (userMessageHelper.showOptionsDialog(title,
                                                text,
                                                UserMessageHelper.Type.ERROR,
                                                options)) {
      case 0:
        return persistModel();
      case 1:
        setHasUnsavedChanges(false);
        return true;
      default:
        return false;
    }
  }

  private void handleKernelStateChangeEvent(KernelStateChangeEvent event) {
    closeOpenedPluginPanels();
    switch (event.getNewState()) {
      case MODELLING:
        if (appState.hasOperationMode(OperationMode.OPERATING)) {
          handleKernelInModellingMode();
        }
        break;
      case OPERATING:
        if (appState.hasOperationMode(OperationMode.OPERATING)) {
          if (hasUnsavedChanges()) {
            if (!showSwitchStateUnsavedChangesDialog()) {
              return;
            }
          }
          SwingUtilities.invokeLater(() -> loadCurrentKernelModel());
        }
        break;
      case DISCONNECTED:
        if (appState.hasOperationMode(OperationMode.OPERATING)) {
          setPlantOverviewState(OperationMode.MODELLING);
          if (sharedPortal != null) {
            sharedPortal.close();
            sharedPortal = null;
          }
        }
        break;
      case SHUTDOWN:
        if (appState.hasOperationMode(OperationMode.OPERATING)) {
          setPlantOverviewState(OperationMode.MODELLING);
          if (sharedPortal != null) {
            sharedPortal.close();
            sharedPortal = null;
          }
          String text = bundle.getFormatted("openTcsView.message_kernelConnectionLost.text",
                                            bundle.getString("openTcsView.state_modelling.text"));
          log(new UserNotification(text, UserNotification.Level.NOTEWORTHY));
        }
        break;
      default:
    }
  }

  /**
   * Switches the plant overview state.
   *
   * @param newMode The mode to switch to.
   */
  public void switchPlantOverviewState(OperationMode newMode) {
    if (!appState.hasOperationMode(newMode)) {
      closeOpenedPluginPanels();
    }
    switch (newMode) {
      case MODELLING:
        setPlantOverviewState(newMode);
        if (sharedPortal != null) {
          sharedPortal.close();
          sharedPortal = null;
        }
        break;
      case OPERATING:
        if (hasUnsavedChanges()) {
          if (!showSwitchStateUnsavedChangesDialog()) {
            return;
          }
        }
        try {
          sharedPortal = portalProvider.register();
          if (sharedPortal.getPortal().getState() != Kernel.State.OPERATING) {
            handleKernelInModellingMode();
          }
          setPlantOverviewState(newMode);
          SwingUtilities.invokeLater(() -> loadCurrentKernelModel());
        }
        catch (ServiceUnavailableException exc) {
          // If a kernel is not available, switch (back) to modelling mode.
          switchPlantOverviewState(OperationMode.MODELLING);
        }
        break;
      default:
      // Do nada.
    }
  }

  private void handleKernelInModellingMode() {
    createEmptyModel();
    String text = bundle.getFormatted("openTcsView.message_kernelInModelling.text",
                                      bundle.getString("openTcsView.state_modelling.text"),
                                      bundle.getString("openTcsView.state_operating.text"));
    log(new UserNotification(text, UserNotification.Level.INFORMATIONAL));
  }

  private void setPlantOverviewStateProperty(String kernelState) {
    String oldKernelState = sOperationMode;
    sOperationMode = kernelState;
    firePropertyChange(OPERATIONMODE_PROPERTY, oldKernelState, kernelState);
  }

  private void setModelNameProperty(String modelName) {
    fModelManager.getModel().setName(modelName);
    eventBus.onEvent(new ModelNameChangeEvent(this, modelName));
  }

  public void updateModelName() {
    String newName = fModelManager.getModel().getName();
    eventBus.onEvent(new ModelNameChangeEvent(this, newName));
  }

  /**
   * Adds a background image to the currently active drawing view.
   *
   * @param file The file with the image.
   */
  public void addBackgroundBitmap(File file) {
    if (appState.hasOperationMode(OperationMode.MODELLING)) {
      viewManager.setBitmapToModellingView(file);
    }
    else {
      getActiveDrawingView().addBackgroundBitmap(file);
    }
  }

  /**
   * Adds the given model components to the data model. E.g. when pasting.
   *
   * @param userObjects
   * @return
   */
  public List<UserObject> restoreModelComponents(List<UserObject> userObjects) {
    List<UserObject> restoredUserObjects = new ArrayList<>();

    for (UserObject userObject : userObjects) {
      ModelComponent modelComponent = userObject.getModelComponent();
      ModelComponent folder = fModelManager.getModel().getFolder(modelComponent);

      if (folder == null) {
        // Workaround: Eigentlich sollten im Tree keine Folder selektiert sein!
        return null;
      }

      if (folder.contains(modelComponent)) {
        try {
          // Paste after Copy: Create clones of tree components (and figures)
          Figure figure = fModelManager.getModel().getFigure(modelComponent);
          if (figure != null) {
            if (figure instanceof LabeledFigure) {
              // Point, Location
              // Create new Figure with a "cloned" model
              final LabeledFigure clonedFigure = (LabeledFigure) figure.clone();
              // Place the figure relative to the position of the prototype
              AffineTransform tx = new AffineTransform();
              // TODO: Make the duplicate's distance configurable.
              // TODO: With multiple pastes, place the inserted figure relative
              // to the predecessor, not the original.
              tx.translate(50, 50);
              clonedFigure.transform(tx);
              getActiveDrawingView().getDrawing().add(clonedFigure);
              // The new tree component will be created by "figureAdded()"
              modelComponent = clonedFigure.get(FigureConstants.MODEL);
            }
            else if (figure instanceof TCSFigure) {
              // Vehicle, ...
              TCSFigure clonedFigure = (TCSFigure) figure.clone();
              modelComponent = clonedFigure.getModel();
            }
          }
          else {
            // LocationType, Block, Group
            modelComponent = modelComponent.clone();
          }
        }
        catch (CloneNotSupportedException ex) {
          LOG.warn("clone() not supported for {}", modelComponent.getName());
        }
      }

      Figure figure = fModelManager.getModel().getFigure(modelComponent);
      if (figure != null
          && !getActiveDrawingView().getDrawing().contains(figure)) {
        getActiveDrawingView().getDrawing().add(figure);
      }

      addModelComponent(folder, modelComponent);
      ContextType type = null;
      if (userObject instanceof ContextObject) {
        ContextObject co = (ContextObject) userObject;
        type = co.getContextType();
      }
      UserObjectContext context = userObjectUtil.createContext(type);
      restoredUserObjects.add(userObjectUtil.createUserObject(modelComponent, context));
    }

    return restoredUserObjects;
  }

  @Override  // View
  public void write(URI f, URIChooser chooser)
      throws IOException {
  }

  @Override  // View
  public void read(URI f, URIChooser chooser)
      throws IOException {
  }

  @Override  // AbstractView
  public boolean canSaveTo(URI file) {
    return new File(file).getName().endsWith(".xml");
  }

  @Override  // AbstractView
  public URI getURI() {
    String modelName = fModelManager.getModel().getName();

    try {
      uri = new URI(modelName);
    }
    catch (URISyntaxException ex) {
      LOG.warn("URISyntaxException in getURI({})", modelName, ex);
    }

    return uri;
  }

  /**
   * Returns all drawing views (including the modelling view)
   *
   * @return List with all known <code>OpenTCSDrawingViews</code>.
   */
  private List<OpenTCSDrawingView> getDrawingViews() {
    List<OpenTCSDrawingView> views = new ArrayList<>();

    for (DrawingViewScrollPane scrollPane : viewManager.getDrawingViewMap().values()) {
      views.add(scrollPane.getDrawingView());
    }

    return views;
  }

  @Override
  public void selectModelComponent(ModelComponent modelComponent) {
    fPropertiesComponent.setModel(modelComponent);
    DrawingView drawingView = fDrawingEditor.getActiveView();
    drawingView.clearSelection();
    Figure figure = findFigure(modelComponent);
    // LocationType hat keine Figur
    if (figure != null) {
      drawingView.toggleSelection(figure);
    }
  }

  @Override// GuiManager
  public void addSelectedModelComponent(ModelComponent modelComponent) {
    Set<ModelComponent> components = fComponentsTreeManager.getSelectedItems();

    if (components.size() > 1) {
      components.add(modelComponent);

      DrawingView drawingView = fDrawingEditor.getActiveView();
      drawingView.clearSelection();

      Collection<Figure> figures = new LinkedList<>();
      for (ModelComponent comp : components) {
        Figure figure = findFigure(comp);

        // At least LocationTypes do not have a Figure!
        if (figure != null) {
          figures.add(figure);
        }
      }
      drawingView.addToSelection(figures);

      fPropertiesComponent.setModel(new PropertiesCollection(components));
      // Re-select all originally selected objects in the tree.
      fComponentsTreeManager.selectItems(components);
    }
    else {
      // In operating mode, only one component can be selected.
      selectModelComponent(modelComponent);
    }
  }

  @Override// GuiManager
  public boolean treeComponentRemoved(ModelComponent model) {
    boolean componentRemoved = false;
    boolean componentRemovedFromFolder = false;
    // Removal is only allowed in modelling mode.
    if (appState.hasOperationMode(OperationMode.MODELLING)) {
      // Point/Location: Remove corresponding Figure.
      if (model instanceof PointModel || model instanceof LocationModel) {
        LabeledFigure lf = (LabeledFigure) fModelManager.getModel().getFigure(model);
        // Die Drawing l�scht auch ggf. mit dem Point verbundene PathConnections
        // bzw. eine mit der Location verbundenen LinkConnection
        fDrawingEditor.getActiveView().getDrawing().remove(lf);
        componentRemoved = true;
      }
      // Link/Path: Remove corresponding Figure.
      else if ((model instanceof LinkModel || model instanceof PathModel)
          && !(model.getParent() instanceof BlockModel)) {
        SimpleLineConnection figure = (SimpleLineConnection) fModelManager.getModel().getFigure(model);
        fDrawingEditor.getActiveView().getDrawing().remove(figure);
        componentRemoved = true;
      }
      // Vehicle: Remove corresponding Figure.
      else if (model instanceof VehicleModel) {
        VehicleFigure vf = (VehicleFigure) fModelManager.getModel().getFigure(model);
        fDrawingEditor.getActiveView().getDrawing().remove(vf);
        componentRemoved = true;
      }
      else if (model instanceof LocationTypeModel) {
        // Search if any Locations of this type exist
        for (LocationModel lm : fModelManager.getModel().getLocationModels()) {
          if (lm.getLocationType() == model) {
            JOptionPane.showMessageDialog(
                this,
                bundle.getString("openTcsView.optionPane_cannotDeleteLocationType.message"),
                bundle.getString("openTcsView.optionPane_cannotDeleteLocationType.title"),
                JOptionPane.ERROR_MESSAGE);

            return false;
          }
        }

        componentRemoved = true;
      }

      ModelComponent folder = fModelManager.getModel().getFolder(model);

      if (folder != null) {
        componentRemovedFromFolder = removeModelComponent(folder, model);
      }
    }

    return componentRemoved || componentRemovedFromFolder;
  }

  @Override  // GuiManager
  public void figureSelected(ModelComponent modelComponent) {
    modelComponent.addAttributesChangeListener(attributesEventHandler);
    fPropertiesComponent.setModel(modelComponent);

    Figure figure = findFigure(modelComponent);
    OpenTCSDrawingView drawingView = fDrawingEditor.getActiveView();

    if (figure != null) {
      drawingView.clearSelection();
      drawingView.addToSelection(figure);
      // Scroll view to this figure.
      drawingView.scrollTo(figure);
    }
  }

  @Override  // GuiManager
  public void loadModel() {
    if (hasUnsavedChanges()) {
      if (!showUnsavedChangesDialog()) {
        return;
      }
    }

    if (!fModelManager.loadModel(null)) {
      return;
    }
    restoreModel(null);

    setHasUnsavedChanges(false);
  }

  @Override
  public void importModel(PlantModelImporter importer) {
    requireNonNull(importer, "importer");

    if (hasUnsavedChanges()) {
      if (!showUnsavedChangesDialog()) {
        return;
      }
    }

    if (!fModelManager.importModel(importer)) {
      return;
    }
    restoreModel(null);

    setHasUnsavedChanges(false);
  }

  /**
   * Shows a dialog to save unsaved changes.
   *
   * @return <code>true</code> if the user pressed yes or no, <code>false</code>
   * if the user pressed cancel.
   */
  private boolean showUnsavedChangesDialog() {
    CloseFileAction action = (CloseFileAction) getActionMap().get(CloseFileAction.ID);
    action.actionPerformed(new ActionEvent(this,
                                           ActionEvent.ACTION_PERFORMED,
                                           CloseFileAction.ID_MODEL_CLOSING));
    switch (action.getFileSavedStatus()) {
      case JOptionPane.YES_OPTION:
        super.setHasUnsavedChanges(false);
        return true;
      case JOptionPane.NO_OPTION:
        return true;
      case JOptionPane.CANCEL_OPTION:
        return false;
      default:
        return false;
    }
  }

  /**
   * Persists the current (local) model in the kernel.
   *
   * @return Whether the model was actually saved.
   */
  public boolean persistModel() {
    try (SharedKernelServicePortal sharedPortal = portalProvider.register()) {
      return persistModel(sharedPortal.getPortal());
    }
    catch (ServiceUnavailableException exc) {
      LOG.warn("Exception persisting model", exc);
      return false;
    }
  }

  private boolean persistModel(KernelServicePortal portal) {
    if (hasUnsavedChanges()) {
      JOptionPane.showMessageDialog(null, bundle.getString("openTcsView.optionPane_saveModelBeforeKernelPersist.message"));
      if (fModelManager.persistModel(true)) {
        setHasUnsavedChanges(false);
        String modelName = fModelManager.getModel().getName();
        setModelNameProperty(modelName);
        return persistModel(portal);
      }
      return false;
    }
    try {
      if (portal.getState() != Kernel.State.OPERATING) {
        if (userMessageHelper.showConfirmDialog(
            bundle.getString("openTcsView.dialog_saveModelConfirmation.title"),
            bundle.getString("openTcsView.dialog_saveModelConfirmation.message"),
            UserMessageHelper.Type.QUESTION)
            != UserMessageHelper.ReturnType.OK) {
          return false;
        }
      }
      boolean didSave = fModelManager.persistModel(portal);
      if (didSave) {
        String modelName = fModelManager.getModel().getName();
        setModelNameProperty(modelName);
        setHasUnsavedChanges(false);
        String persistMsg = bundle.getFormatted("openTcsView.message_modelSaved.text", modelName);
        statusPanel.setLogMessage(Level.INFO, persistMsg);
      }
      return didSave;
    }
    catch (KernelRuntimeException e) {
      LOG.warn("Exception persisting model {}", fModelManager.getModel().getName(), e);
      statusPanel.setLogMessage(Level.WARNING, e.getMessage());
      return false;
    }
  }

  @Override
  public boolean saveModel() {
    boolean saved = fModelManager.persistModel(false);
    if (saved) {
      String modelName = fModelManager.getModel().getName();
      setModelNameProperty(modelName);
      setHasUnsavedChanges(false);
    }
    return saved;
  }

  @Override  // GuiManager
  public boolean saveModelAs() {
    boolean saved = fModelManager.persistModel(true);
    if (saved) {
      String modelName = fModelManager.getModel().getName();
      setModelNameProperty(modelName);
      setHasUnsavedChanges(false);
    }
    return saved;
  }

  @Override
  public void exportModel(PlantModelExporter exporter) {
    fModelManager.exportModel(exporter);
  }

  @Override  // GuiManager
  public ModelComponent createModelComponent(Class<? extends ModelComponent> clazz) {
    requireNonNull(clazz, "clazz");

    ModelComponent model;
    if (clazz == VehicleModel.class) {
      VehicleModel vehicleModel = crsObjFactory.createVehicleModel();
      vehicleModel.getPropertyRouteColor()
          .setColor(Colors.unusedVehicleColor(fModelManager.getModel().getVehicleModels()));
      model = vehicleModel;
    }
    else if (clazz == LocationTypeModel.class) {
      model = crsObjFactory.createLocationTypeModel();
    }
    else if (clazz == BlockModel.class) {
      BlockModel blockModel = crsObjFactory.createBlockModel();
      blockModel.getPropertyColor()
          .setColor(Colors.unusedBlockColor(fModelManager.getModel().getBlockModels()));
      model = blockModel;
    }
    else {
      throw new IllegalArgumentException("Unhandled component class: " + clazz);
    }

    addModelComponent(fModelManager.getModel().getFolder(model), model);

    return model;
  }

  private OpenTCSDrawingView getActiveDrawingView() {
    return fDrawingEditor.getActiveView();
  }

  private void removeDrawingView(DefaultSingleCDockable dock) {
    if (!viewManager.getDrawingViewMap().containsKey(dock)) {
      return;
    }

    // Remove from group pop ups
    ModelComponent groups = fModelManager.getModel().getMainFolder(SystemModel.FolderKey.GROUPS);
    for (Object o : groups.getChildComponents()) {
      if (o instanceof GroupModel) {
        GroupModel gf = (GroupModel) o;
        gf.removeDrawingView(dock.getTitleText());
      }
    }
    fDrawingEditor.remove(viewManager.getDrawingViewMap().get(dock).getDrawingView());
    viewManager.removeDockable(dock);
    dockingManager.removeDockable(dock);
  }

  /**
   * Combines the OpenTCSView panel and the panel for the tool bars to a new
   * panel.
   *
   * @return The resulting panel.
   */
  private JPanel wrapViewComponent() {
    // Add a dummy toolbar for dragging.
    // (Preview to see how the tool bar would look like after dragging?)
    final JToolBar toolBar = new JToolBar();
    // A wholeComponentPanel for toolbars above the OpenTCSView wholeComponentPanel.
    final JPanel toolBarPanel = new JPanel();
    toolBarPanel.setLayout(new BoxLayout(toolBarPanel, BoxLayout.LINE_AXIS));
    toolBar.setBorder(new PaletteToolBarBorder());

    final List<JToolBar> lToolBars = new LinkedList<>();

    // The new wholeComponentPanel for the whole component.
    JPanel wholeComponentPanel = new JPanel(new BorderLayout());
    wholeComponentPanel.add(toolBarPanel, BorderLayout.NORTH);
    wholeComponentPanel.add(getComponent());
    lToolBars.add(toolBar);

    JPanel viewComponent = wholeComponentPanel;

    LinkedList<Action> toolBarActions = new LinkedList<>();

    // XXX Why is this list iterated in *reverse* order?
    for (JToolBar curToolBar : new ReversedList<>(toolBarManager.getToolBars())) {
      // A panel that wraps the toolbar.
      final JPanel curToolBarPanel = new JPanel();
      curToolBarPanel.setLayout(new BoxLayout(curToolBarPanel, BoxLayout.LINE_AXIS));
      // A panel that wraps the (wrapped) toolbar and the previous component
      // (the whole view and the nested/wrapped toolbars).
      JPanel wrappingPanel = new JPanel(new BorderLayout());
      curToolBar.setBorder(new PaletteToolBarBorder());

      curToolBarPanel.add(curToolBar);
      wrappingPanel.add(curToolBarPanel, BorderLayout.NORTH);
      wrappingPanel.add(viewComponent);

      lToolBars.add(curToolBar);
      viewComponent = wrappingPanel;
      toolBarActions.addFirst(new ToggleVisibleAction(curToolBar, curToolBar.getName()));
    }

    for (JToolBar bar : lToolBars) {
      configureToolBarButtons(bar);
    }

    getComponent().putClientProperty(TOOLBAR_ACTIONS_PROPERTY, toolBarActions);

    return viewComponent;
  }

  private void configureToolBarButtons(JToolBar bar) {
    final Dimension dimButton = new Dimension(32, 34);
    for (Component comp : bar.getComponents()) {
      if (comp instanceof JButton || comp instanceof JToggleButton) {
        JComponent tbButton = (JComponent) comp;
        tbButton.setMaximumSize(dimButton);
        tbButton.setPreferredSize(dimButton);
        tbButton.setBorder(new EtchedBorder());
      }
    }
  }

  private void closeOpenedPluginPanels() {
    for (PluggablePanelFactory factory : panelRegistry.getFactories()) {
      showPluginPanel(factory, false);
    }
  }

  /**
   * Sets the plant overview state.
   *
   * @param newMode The new state.
   */
  private void setPlantOverviewState(final OperationMode newMode) {
    final OperationMode oldMode = appState.getOperationMode();
    appState.setOperationMode(newMode);
    Runnable run = new Runnable() {

      @Override
      public void run() {
        String plantOverviewState;

        switch (newMode) {
          case MODELLING:
            plantOverviewState = bundle.getString("openTcsView.state_modelling.text");
            break;
          case OPERATING:
            plantOverviewState = bundle.getString("openTcsView.state_operating.text");
            break;
          default:
            plantOverviewState = "?";
        }
        // XXX The event should probably be emitted in ApplicationState now.
        eventBus.onEvent(new OperationModeChangeEvent(this, oldMode, newMode));
        // Show new state in the title
        setPlantOverviewStateProperty(plantOverviewState);
      }
    };

    if (SwingUtilities.isEventDispatchThread()) {
      // Called from File -> Mode
      SwingUtilities.invokeLater(run);
    }
    else {
      try {
        // Called from Main.connectKernel()
        SwingUtilities.invokeAndWait(run);
      }
      catch (InterruptedException | InvocationTargetException ex) {
        LOG.error("Unexpected exception ", ex);
      }
    }

    // Switch to selection tool.
    eventBus.onEvent(new ResetInteractionToolCommand(this));
  }

  public String getPlantOverviewState() {
    return sOperationMode;
  }

  /**
   * Adds the given model component to the given folder.
   *
   * @param folder The folder.
   * @param modelComponent The model component to be added.
   */
  private void addModelComponent(ModelComponent folder, ModelComponent modelComponent) {
    if (folder.contains(modelComponent)) {
      return;
    }

    // This method is being called by command objects that use undo/redo, so
    // avoid calling commands via undo/redo here.
    if (requiresName(modelComponent) && modelComponent.getName().isEmpty()) {
      String name = modelCompNameGen.getUniqueString(modelComponent.getClass());
      modelComponent.setName(name);
      modelCompNameGen.addString(name);
    }

    if (modelComponent instanceof LocationModel) {
      LocationModel location = (LocationModel) modelComponent;
      // Zu einer neu erzeugten Location zun�chst den Default-Typ zuweisen
      if (location.getLocationType() == null) {
        List<LocationTypeModel> types = fModelManager.getModel().getLocationTypeModels();
        LocationTypeModel type;

        if (types.isEmpty()) {
          type = (LocationTypeModel) createModelComponent(LocationTypeModel.class);
        }
        else {
          type = types.get(0);
        }

        location.setLocationType(type);
        location.updateTypeProperty(fModelManager.getModel().getLocationTypeModels());
      }
    }

    folder.add(modelComponent);

//    procAdapterUtil.registerProcessAdapter(modelComponent,
//                                           fModelManager.getModel().getProcessAdapterPool());
    // Knoten "Modell"
    fComponentsTreeManager.addItem(folder, modelComponent);
    modelComponent.addAttributesChangeListener(attributesEventHandler);
    // Neuer LocationType: allen existierenden Locations bekannt machen
    if (modelComponent instanceof LocationTypeModel) {
      List<LocationTypeModel> types = fModelManager.getModel().getLocationTypeModels();

      for (LocationModel location : fModelManager.getModel().getLocationModels()) {
        location.updateTypeProperty(types);
      }
    }

    if (modelComponent instanceof BlockModel) {
      BlockModel blockModel = (BlockModel) modelComponent;
      fBlocksTreeManager.addItem(folder, modelComponent);
      blockModel.addBlockChangeListener(blockEventHandler);

      for (DrawingView drawView : fDrawingEditor.getDrawingViews()) {
        ((OpenTCSDrawingView) drawView).blockAdded(blockModel);
      }
    }
    else if (modelComponent instanceof GroupModel) {
      fGroupsTreeManager.addItem(folder, modelComponent);
      for (ModelComponent member : modelComponent.getChildComponents()) {
        fGroupsTreeManager.addItem(modelComponent, member);
      }
      fGroupsTreeManager.getTreeView().sortChildren();
    }
    else if (modelComponent instanceof VehicleModel) {
      fDrawingEditor.addVehicle((VehicleModel) modelComponent);
    }

    selectModelComponent(modelComponent);

    setHasUnsavedChanges(true);
  }

  /**
   * Checks whether the given model component should get a (generated) name if
   * it doesn't have any, yet.
   *
   * @param model The model component to be checked.
   * @return <code>true</code> if, and only if, a name should be generated for
   * the given component.
   */
  private boolean requiresName(ModelComponent model) {
    if (model instanceof PointModel
        || model instanceof PathModel
        || model instanceof LocationTypeModel
        || model instanceof LocationModel
        || model instanceof BlockModel
        || model instanceof GroupModel
        || model instanceof LayoutModel
        || model instanceof VehicleModel) {
      return true;
    }
    return false;
  }

  /**
   * Removes the given model component from the given folder.
   *
   * @param folder The folder.
   * @param model The component to be removed.
   */
  private boolean removeModelComponent(ModelComponent folder,
                                       ModelComponent model) {
    if (!folder.contains(model)) {
      return false;
    }

    // This method is being called by command objects that use undo/redo, so
    // avoid calling commands via undo/redo here.
    boolean componentRemoved = false;

    synchronized (model) {
      if (!BlockModel.class.isInstance(folder)) {
        // don't delete objects from a Blocks folder
        synchronized (folder) {
          folder.remove(model);
        }

        model.removeAttributesChangeListener(attributesEventHandler);
        componentRemoved = true;
      }

      fPropertiesComponent.reset();

      if (model instanceof BlockModel) {
        BlockModel blockModel = (BlockModel) model;
        // Remove Blocks from the Blocks tree
        fBlocksTreeManager.removeItem(blockModel);
        blockModel.blockRemoved();
        blockModel.removeBlockChangeListener(blockEventHandler);
      }
      else if (model instanceof GroupModel) {
        fGroupsTreeManager.removeItem(model);
      }
      else if (componentRemoved) {
        fComponentsTreeManager.removeItem(model);
      }

      if (model instanceof LocationTypeModel) {
        for (LocationModel location : fModelManager.getModel().getLocationModels()) {
          location.updateTypeProperty(fModelManager.getModel().getLocationTypeModels());
        }
      }

      modelCompNameGen.removeString(model.getName());

      setHasUnsavedChanges(true);
    }

    return componentRemoved;
  }

  /**
   * Returns the figure that belongs to the given model component.
   *
   * @param model The model component.
   * @return The figure that belongs to the given model component, or
   * <code>null</code>, if there isn't any.
   */
  private Figure findFigure(ModelComponent model) {
    return fModelManager.getModel().getFigure(model);
  }

  private void setSystemModel(SystemModel systemModel) {
    requireNonNull(systemModel, "systemModel");

    long timeBefore = System.currentTimeMillis();

    // Notify the view's scroll panes about the new systemModel and therefore about the new/changed
    // origin. This way they can handle changes made to the origin's scale.
    for (DrawingViewScrollPane scrollPane : viewManager.getDrawingViewMap().values()) {
      scrollPane.originChanged(systemModel.getDrawingMethod().getOrigin());
    }

    // Clear the name generator
    modelCompNameGen.clear();
    fDrawingEditor.setSystemModel(systemModel);

    // --- Undo, Redo, Clipboard ---
    Drawing drawing = fDrawingEditor.getDrawing();
    drawing.addUndoableEditListener(fUndoRedoManager);

    fComponentsTreeManager.restoreTreeView(systemModel);
    fComponentsTreeManager.sortItems();
    fComponentsTreeManager.getTreeView().getTree().scrollRowToVisible(0);
    fBlocksTreeManager.restoreTreeView(systemModel.getMainFolder(SystemModel.FolderKey.BLOCKS));
    fBlocksTreeManager.getTreeView().sortRoot();
    fBlocksTreeManager.getTreeView().getTree().scrollRowToVisible(0);
    fGroupsTreeManager.restoreTreeView(systemModel.getMainFolder(SystemModel.FolderKey.GROUPS));
    fGroupsTreeManager.getTreeView().sortRoot();
    fGroupsTreeManager.getTreeView().sortChildren();
    fGroupsTreeManager.getTreeView().getTree().scrollRowToVisible(0);

    // Add Attribute Change Listeners to all objects
    for (VehicleModel vehicle : systemModel.getVehicleModels()) {
      vehicle.addAttributesChangeListener(attributesEventHandler);
      modelCompNameGen.addString(vehicle.getName());
    }

    for (LayoutModel layout : systemModel.getLayoutModels()) {
      layout.addAttributesChangeListener(attributesEventHandler);
      modelCompNameGen.addString(layout.getName());
    }

    for (PointModel point : systemModel.getPointModels()) {
      point.addAttributesChangeListener(attributesEventHandler);
      modelCompNameGen.addString(point.getName());
    }

    for (PathModel path : systemModel.getPathModels()) {
      path.addAttributesChangeListener(attributesEventHandler);
      modelCompNameGen.addString(path.getName());
    }

    for (LocationTypeModel locationType : systemModel.getLocationTypeModels()) {
      locationType.addAttributesChangeListener(attributesEventHandler);
      modelCompNameGen.addString(locationType.getName());
    }

    for (LocationModel location : systemModel.getLocationModels()) {
      location.addAttributesChangeListener(attributesEventHandler);
      modelCompNameGen.addString(location.getName());
    }

    for (LinkModel link : systemModel.getLinkModels()) {
      link.addAttributesChangeListener(attributesEventHandler);
      modelCompNameGen.addString(link.getName());
    }

    for (BlockModel block : systemModel.getBlockModels()) {
      block.addAttributesChangeListener(attributesEventHandler);
      block.addBlockChangeListener(blockEventHandler);
      modelCompNameGen.addString(block.getName());
    }

    LOG.debug("setSystemModel() took {} ms.", System.currentTimeMillis() - timeBefore);
  }

  /**
   * Initializes the frame with the toolbars and the dockable elements.
   */
  private void initializeFrame() {
    if (!SwingUtilities.isEventDispatchThread()) {
      try {
        SwingUtilities.invokeAndWait(() -> initializeFrame());
      }
      catch (InterruptedException | InvocationTargetException e) {
        LOG.warn("Exception initializing frame", e);
      }
      return;
    }

    fFrame.getContentPane().removeAll();
    dockingManager.initializeDockables(fFrame,
                                       vehiclesPanel,
                                       fComponentsTreeManager.getTreeView(),
                                       fBlocksTreeManager.getTreeView(),
                                       fGroupsTreeManager.getTreeView(),
                                       fPropertiesComponent,
                                       kernelStatusPanel);
    // Frame
    fFrame.setLayout(new BorderLayout());
    fFrame.add(wrapViewComponent(), BorderLayout.NORTH);
    fFrame.add(dockingManager.getCControl().getContentArea());
    fFrame.add(statusPanel, BorderLayout.SOUTH);
    restoreDockables();
    // Ensure that, after initialization, the selection tool is active.
    // This needs to be done after the initial drawing views have been set
    // up so they reflect the behaviour of the selected tool.
    // XXX Maybe there is a better way to ensure this...
    toolBarManager.getDragToolButton().doClick();
    toolBarManager.getSelectionToolButton().doClick();
  }

  private void restoreDockables() {
    // --- DrawingView for modelling ---
    DefaultSingleCDockable modellingDockable = addDrawingView();
    viewManager.initModellingDockable(
        modellingDockable,
        bundle.getString("openTcsView.panel_modellingDrawingView.title"));

    addDrawingView();

    addTransportOrderView();
    addTransportOrderSequenceView();

    dockingManager.getTabPane(DockingManager.COURSE_TAB_PANE_ID)
        .getStation()
        .setFrontDockable(viewManager.evaluateFrontDockable());
  }

  private class AttributesEventHandler
      implements AttributesChangeListener {

    /**
     * Creates a new instance.
     */
    public AttributesEventHandler() {
    }

    @Override  // AttributesChangeListener
    public void propertiesChanged(AttributesChangeEvent event) {
      if (event.getInitiator() == this) {
        return;
      }

      ModelComponent model = event.getModel();

      // If a model component's name changed, update the blocks this component is a member of
      if (model.getPropertyName() != null && model.getPropertyName().hasChanged()) {
        fComponentsTreeManager.itemChanged(model);

        fModelManager.getModel().getBlockModels().stream()
            .filter(block -> blockAffectedByNameChange(block, model))
            .forEach(block -> updateBlockMembers(block));
      }

      if (model instanceof LayoutModel) {
        // Handle scale changes.
        LengthProperty pScaleX = (LengthProperty) model.getProperty(LayoutModel.SCALE_X);
        LengthProperty pScaleY = (LengthProperty) model.getProperty(LayoutModel.SCALE_Y);

        if (pScaleX.hasChanged() || pScaleY.hasChanged()) {
          double scaleX = (double) pScaleX.getValue();
          double scaleY = (double) pScaleY.getValue();

          if (scaleX != 0.0 && scaleY != 0.0) {
            fModelManager.getModel().getDrawingMethod().getOrigin().setScale(scaleX, scaleY);
          }
        }
      }

      if (model instanceof LocationModel) {
        if (model.getProperty(LocationModel.TYPE).hasChanged()) {
          AbstractProperty p = (AbstractProperty) model.getProperty(LocationModel.TYPE);
          LocationTypeModel type
              = fModelManager.getModel().getLocationTypeModel((String) p.getValue());
          ((LocationModel) model).setLocationType(type);
          if (model != event.getInitiator()) {
            model.propertiesChanged(this);
          }
        }
      }

      if (model instanceof LocationTypeModel) {
        for (LocationModel locModel : fModelManager.getModel().getLocationModels()) {
          locModel.updateTypeProperty(fModelManager.getModel().getLocationTypeModels());
        }
      }
    }

    private boolean blockAffectedByNameChange(BlockModel block, ModelComponent model) {
      return block.getChildComponents().stream().anyMatch(member -> member.equals(model));
    }

    private void updateBlockMembers(BlockModel block) {
      List<String> members = new ArrayList<>();
      for (ModelComponent component : block.getChildComponents()) {
        members.add(component.getName());
      }
      block.getPropertyElements().setItems(members);
    }
  }

  /**
   * Handles events emitted for changes of blocks.
   */
  private class BlockEventHandler
      implements BlockChangeListener {

    /**
     * Creates a new instance.
     */
    public BlockEventHandler() {
    }

    @Override  // BlockChangeListener
    public void courseElementsChanged(BlockChangeEvent event) {
      BlockModel block = (BlockModel) event.getSource();
      // Remove all children from the block and re-add those that are still there.
      fBlocksTreeManager.removeChildren(block);
      for (ModelComponent component : block.getChildComponents()) {
        fBlocksTreeManager.addItem(block, component);
      }

      setHasUnsavedChanges(true);
    }

    @Override
    public void colorChanged(BlockChangeEvent event) {
    }

    @Override  // BlockChangeListener
    public void blockRemoved(BlockChangeEvent event) {
    }
  }

  /**
   * Handles events emitted by the drawing editor.
   */
  private class DrawingEditorEventHandler
      implements DrawingEditorListener {

    /**
     * Provides access to the current system model.
     */
    private final ModelManager modelManager;

    /**
     * Creates a new instance.
     *
     * @param modelManager Provides access to the current system model.
     */
    public DrawingEditorEventHandler(ModelManager modelManager) {
      this.modelManager = requireNonNull(modelManager, "modelManager");
    }

    @Override  // DrawingEditorListener
    public ModelComponent figureAdded(DrawingEditorEvent event) {
      Figure figure = event.getFigure();
      ModelComponent model = figure.get(FigureConstants.MODEL);

      // Some figures do not have a model - OriginFigure, for instance.
      // XXX Check if we can't unify all figures to have a model.
      if (model == null) {
        return null;
      }

      if (figure instanceof AttributesChangeListener) {
        model.addAttributesChangeListener((AttributesChangeListener) figure);
      }

      // The added figure shall react on changes of the layout's scale.
      if (figure instanceof OriginChangeListener) {
        Origin ref = modelManager.getModel().getDrawingMethod().getOrigin();

        if (ref != null) {
          ref.addListener((OriginChangeListener) figure);
          figure.set(FigureConstants.ORIGIN, ref);
        }
      }

      fModelManager.getModel().registerFigure(model, figure);

      ModelComponent folder = modelManager.getModel().getFolder(model);
      addModelComponent(folder, model);

      if (figure instanceof LabeledFigure) {
        ((LabeledFigure) figure).updateModel();
      }

      return model;
    }

    @Override// DrawingEditorListener
    public ModelComponent figureRemoved(DrawingEditorEvent e) {
      Figure figure = e.getFigure();

      if (figure == null) {
        return null;
      }

      ModelComponent model = figure.get(FigureConstants.MODEL);

      if (model == null) {
        return null;
      }

      synchronized (model) {
        // The removed figure shouldn't react on changes of the origin any more.
        if (figure instanceof OriginChangeListener) {
          Origin ref = figure.get(FigureConstants.ORIGIN);

          if (ref != null) {
            ref.removeListener((OriginChangeListener) figure);
            figure.set(FigureConstants.ORIGIN, null);
          }
        }
        // Disassociate from blocks, static routes and groups...
        removeFromAllBlocks(model);
        removeFromAllGroups(model);
        // ...and remove the object itself.
        ModelComponent folder = modelManager.getModel().getFolder(model);
        synchronized (folder) {
          removeModelComponent(folder, model);
        }

        return model;
      }
    }

    @Override
    public void figureSelected(DrawingEditorEvent event) {
      if (event.getCount() == 0) {
        fComponentsTreeManager.selectItems(null);
        fBlocksTreeManager.selectItems(null);
        fGroupsTreeManager.selectItems(null);
      }
      else if (event.getCount() == 1) {
        // Single figure selected.
        Figure figure = event.getFigure();

        if (figure != null) {
          ModelComponent model = figure.get(FigureConstants.MODEL);

          if (model != null) {
            model.addAttributesChangeListener(attributesEventHandler);
            fPropertiesComponent.setModel(model);
            fComponentsTreeManager.selectItem(model);
            fBlocksTreeManager.selectItem(model);
            fGroupsTreeManager.selectItem(model);
          }
        }
      }
      else {
        // Multiple figures selected.
        List<ModelComponent> models = new LinkedList<>();
        Set<ModelComponent> components = new HashSet<>();

        for (Figure figure : event.getFigures()) {
          ModelComponent model = figure.get(FigureConstants.MODEL);
          if (model != null) {
            models.add(model);
            components.add(model);
          }
        }

        // Display shared properties of the selected figures.
        ModelComponent model = new PropertiesCollection(models);
        fComponentsTreeManager.selectItems(components);
        fBlocksTreeManager.selectItems(components);
        fGroupsTreeManager.selectItems(components);
        fPropertiesComponent.setModel(model);
      }
    }

    /**
     * Removes a component from all blocks in the model.
     *
     * @param model The component to be removed.
     */
    private void removeFromAllBlocks(ModelComponent model) {
      // The (invisible?) root folder of the "Blocks" tree...
      ModelComponent mainFolder
          = modelManager.getModel().getMainFolder(SystemModel.FolderKey.BLOCKS);

      synchronized (mainFolder) {
        // ... contains one folder for each Block

        for (ModelComponent blockModelComp : mainFolder.getChildComponents()) {
          BlockModel block = (BlockModel) blockModelComp;

          List<ModelComponent> elementsToRemove = new ArrayList<>();
          // All child components (Points, Paths) of one Block
          for (ModelComponent blockChildComp : block.getChildComponents()) {
            if (model == blockChildComp) {
              elementsToRemove.add(blockChildComp);
            }
          }

          if (!elementsToRemove.isEmpty()) {
            // At least one component found
            for (ModelComponent mc : elementsToRemove) {
              block.removeCourseElement(mc);
            }

            block.courseElementsChanged();
          }
        }
      }
    }

    /**
     * Removes a component from all groups in the model.
     *
     * @param model The component to be removed.
     */
    private void removeFromAllGroups(ModelComponent model) {
      // The (invisible?) root folder of the "Groups" tree...
      ModelComponent mainFolder
          = modelManager.getModel().getMainFolder(SystemModel.FolderKey.GROUPS);

      synchronized (mainFolder) {
        // ... contains one folder for each Group
        for (ModelComponent groupFolder : mainFolder.getChildComponents()) {
          groupFolder.remove(model);
          fGroupsTreeManager.removeItem(model);
        }
      }
    }
  }

  /**
   * MouseListener for vehicle dragging events in the tree view.
   */
  private class VehicleDragHandler
      extends MouseAdapter {

    /**
     * The cursor to be used when a vehicle is dragged.
     */
    private final Cursor dragCursor;
    /**
     * The currently selected/dragged vehicle model.
     */
    private VehicleModel vehicleModel;

    /**
     * Creates a new instance.
     *
     * @param dragCursor The cursor to be used when a vehicle is dragged.
     */
    public VehicleDragHandler(Cursor dragCursor) {
      this.dragCursor = requireNonNull(dragCursor, "dragCursor");
    }

    @Override
    public void mousePressed(MouseEvent e) {
      UserObject object = fComponentsTreeManager.getDraggedUserObject(e);

      if (object instanceof VehicleUserObject
          && appState.hasOperationMode(OperationMode.OPERATING)) {
        vehicleModel = ((VehicleUserObject) object).getModelComponent();
      }
      else {
        vehicleModel = null;
      }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
      if (vehicleModel == null) {
        return;
      }
      Point eOnScreen = e.getLocationOnScreen();
      for (OpenTCSDrawingView drawView : getDrawingViews()) {
        if (drawView.isShowing()) {
          if (drawView.containsPointOnScreen(eOnScreen)) {
            drawView.setCursor(dragCursor);
          }
        }
      }

      fComponentsTreeManager.setCursor(dragCursor);
      setCursor(dragCursor);
    }

    @Override
    public void mouseReleased(MouseEvent event) {
      // Reset cursors to the default ones.
      fComponentsTreeManager.setCursor(Cursor.getDefaultCursor());
      setCursor(Cursor.getDefaultCursor());
      vehicleModel = null;

      if (vehicleModel != null && vehicleModel.getDriveOrderComponents() == null) {
        createOrderToPointOnScreen(event.getLocationOnScreen());
      }
    }

    // Class-specific methods start here.
    private void createOrderToPointOnScreen(Point locationOnScreen) {
      for (OpenTCSDrawingView drawView : getDrawingViews()) {
        drawView.setCursor(Cursor.getDefaultCursor());

        if (drawView.isShowing()
            && drawView.containsPointOnScreen(locationOnScreen)) {
          Figure figure = getFigureAtPointInView(locationOnScreen, drawView);
          if (figure instanceof LabeledPointFigure) {
            createOrderToPointFigure((LabeledPointFigure) figure);
          }
        }
      }
    }

    private Figure getFigureAtPointInView(Point locationOnScreen,
                                          OpenTCSDrawingView drawView) {
      Point drawingViewOnScreen = drawView.getLocationOnScreen();
      Point drawingViewPoint
          = new Point(locationOnScreen.x - drawingViewOnScreen.x,
                      locationOnScreen.y - drawingViewOnScreen.y);
      return drawView.findFigure(drawingViewPoint);
    }

    private void createOrderToPointFigure(LabeledPointFigure figure) {
      PointModel model = (PointModel) figure.get(FigureConstants.MODEL);
      orderUtil.createTransportOrder(model, vehicleModel);
    }
  }

  private class DrawingViewClosingListener
      implements CVetoClosingListener {

    private final DefaultSingleCDockable newDockable;

    public DrawingViewClosingListener(DefaultSingleCDockable newDockable) {
      this.newDockable = newDockable;
    }

    @Override
    public void closing(CVetoClosingEvent event) {
    }

    @Override
    public void closed(CVetoClosingEvent event) {
      // A dockable is closeable by default. It isn't closeable
      // when switching kernel states and we want to hide additional views
      if (newDockable.isCloseable()) {
        removeDrawingView(newDockable);
      }
    }
  }
}
