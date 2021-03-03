/**
 * (c): IML, IFAK, JHotDraw.
 *
 */
package org.opentcs.guing.application;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.event.CFocusListener;
import bibliothek.gui.dock.common.event.CVetoClosingEvent;
import bibliothek.gui.dock.common.event.CVetoClosingListener;
import bibliothek.gui.dock.common.intern.CDockable;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;
import org.jhotdraw.app.AbstractView;
import org.jhotdraw.app.View;
import org.jhotdraw.app.action.window.ToggleVisibleAction;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingView;
import static org.jhotdraw.draw.DrawingView.CONSTRAINER_VISIBLE_PROPERTY;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.QuadTreeDrawing;
import org.jhotdraw.draw.connector.Connector;
import org.jhotdraw.draw.io.InputFormat;
import org.jhotdraw.draw.io.OutputFormat;
import org.jhotdraw.gui.URIChooser;
import org.jhotdraw.util.ReversedList;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.UnsupportedKernelOpException;
import org.opentcs.access.rmi.KernelUnavailableException;
import org.opentcs.access.rmi.RemoteKernelConnection;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.message.Message;
import org.opentcs.data.model.Group;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.ViewBookmark;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.drivers.messages.LimitSpeed;
import org.opentcs.guing.application.action.ActionManager;
import org.opentcs.guing.application.action.edit.UndoRedoManager;
import org.opentcs.guing.application.action.file.CloseFileAction;
import org.opentcs.guing.application.toolbar.PaletteToolBarBorder;
import org.opentcs.guing.components.dialogs.AllVehiclesPanel;
import org.opentcs.guing.components.dialogs.BookmarkSelectionPanel;
import org.opentcs.guing.components.dialogs.CloseDialog;
import org.opentcs.guing.components.dialogs.FindVehiclePanel;
import org.opentcs.guing.components.dialogs.StandardContentDialog;
import org.opentcs.guing.components.dialogs.StandardDialog;
import org.opentcs.guing.components.dialogs.VehiclesPanel;
import org.opentcs.guing.components.dockable.DockingManager;
import org.opentcs.guing.components.drawing.OpenTCSDockableUtil;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.components.drawing.OpenTCSDrawingView;
import static org.opentcs.guing.components.drawing.OpenTCSDrawingView.BLOCKS_VISIBLE_PROPERTY;
import static org.opentcs.guing.components.drawing.OpenTCSDrawingView.LABELS_VISIBLE_PROPERTY;
import static org.opentcs.guing.components.drawing.OpenTCSDrawingView.STATIC_ROUTES_VISIBLE_PROPERTY;
import org.opentcs.guing.components.drawing.Ruler;
import org.opentcs.guing.components.drawing.course.Origin;
import org.opentcs.guing.components.drawing.course.OriginChangeListener;
import org.opentcs.guing.components.drawing.figures.BitmapFigure;
import org.opentcs.guing.components.drawing.figures.FigureConstants;
import org.opentcs.guing.components.drawing.figures.LabeledFigure;
import org.opentcs.guing.components.drawing.figures.LabeledLocationFigure;
import org.opentcs.guing.components.drawing.figures.LabeledPointFigure;
import org.opentcs.guing.components.drawing.figures.PathConnection;
import org.opentcs.guing.components.drawing.figures.SimpleLineConnection;
import org.opentcs.guing.components.drawing.figures.TCSFigure;
import org.opentcs.guing.components.drawing.figures.VehicleFigure;
import org.opentcs.guing.components.properties.AttributesComponent;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.components.properties.panel.PropertiesTableContent;
import org.opentcs.guing.components.properties.type.ColorProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.Property;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.components.tree.StandardTreeViewPanel;
import org.opentcs.guing.components.tree.TreeViewManager;
import org.opentcs.guing.components.tree.elements.UserObject;
import org.opentcs.guing.components.tree.elements.VehicleUserObject;
import org.opentcs.guing.event.BlockChangeEvent;
import org.opentcs.guing.event.BlockChangeListener;
import org.opentcs.guing.event.DrawingEditorEvent;
import org.opentcs.guing.event.DrawingEditorListener;
import org.opentcs.guing.event.StaticRouteChangeEvent;
import org.opentcs.guing.event.StaticRouteChangeListener;
import org.opentcs.guing.exchange.KernelProxyManager;
import org.opentcs.guing.exchange.OpenTCSEventDispatcher;
import org.opentcs.guing.exchange.adapter.GroupAdapter;
import org.opentcs.guing.exchange.adapter.LinkAdapter;
import org.opentcs.guing.exchange.adapter.OpenTCSProcessAdapter;
import org.opentcs.guing.exchange.adapter.PathAdapter;
import org.opentcs.guing.exchange.adapter.ProcessAdapter;
import org.opentcs.guing.exchange.adapter.ProcessAdapterFactory;
import org.opentcs.guing.model.FigureComponent;
import org.opentcs.guing.model.FiguresFolder;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.PropertiesCollection;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.AbstractConnection;
import org.opentcs.guing.model.elements.BlockModel;
import org.opentcs.guing.model.elements.GroupModel;
import org.opentcs.guing.model.elements.LayoutModel;
import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.LocationTypeModel;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.StaticRouteModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.storage.OpenTCSFactory;
import org.opentcs.guing.storage.OpenTCSModelManager;
import org.opentcs.guing.storage.ViewBookmarkNameChooser;
import org.opentcs.guing.transport.CreateTransportOrderPanel;
import org.opentcs.guing.transport.OrderSequencesContainerPanel;
import org.opentcs.guing.transport.TransportOrdersContainerPanel;
import org.opentcs.guing.util.Colors;
import org.opentcs.guing.util.ConfigConstants;
import org.opentcs.guing.util.CourseObjectFactory;
import org.opentcs.guing.util.Cursors;
import org.opentcs.guing.util.OpenTCSDOMStorableInputOutputFormat;
import org.opentcs.guing.util.PanelRegistry;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.util.configuration.ConfigurationStore;
import org.opentcs.util.gui.plugins.PanelFactory;
import org.opentcs.util.gui.plugins.PluggablePanel;

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
               AttributesChangeListener,
               BlockChangeListener,
               DrawingEditorListener,
               StaticRouteChangeListener {

  /**
   * The name/title of this application.
   */
  public static final String NAME
      = ResourceBundleUtil.getBundle().getString("OpenTCSView.name");
  /**
   * Copyright information.
   */
  public static final String COPYRIGHT = "Copyright (c) 2012 - 2014 by Fraunhofer IML";
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
  public static final String toolBarActionsProperty = "toolBarActions";
  /**
   * This class' configuration store.
   */
  private static final ConfigurationStore configStore
      = ConfigurationStore.getStore(OpenTCSView.class.getName());
  /**
   * This class's logger.
   */
  private static final Logger log
      = Logger.getLogger(OpenTCSView.class.getName());
  private static final String NUMBER_OF_DRAWING_VIEWS
      = "NUMBER_OF_DRAWING_VIEWS";
  private static final String NUMBER_OF_TRANSPORT_ORDER_VIEWS
      = "NUMBER_OF_TRANSPORT_ORDER_VIEWS";
  private static final String NUMBER_OF_ORDER_SEQUENCE_VIEWS
      = "NUMBER_OF_ORDER_SEQUENCE_VIEWS";
  /**
   * A single central instance (singleton) of this class.
   * XXX This should go away.
   */
  private static OpenTCSView instance;
  /**
   * The manager for all actions.
   */
  private final ActionManager fActionManager;
  private String sModelName = "";
  private String sOperationMode = "";
  private List<JToolBar> fToolBars;
  private JMenuBar fMenuBar;
  /**
   * Allows for undoing and redoing actions.
   */
  private final UndoRedoManager fUndoRedoManager = new UndoRedoManager();
  /**
   * Depending on the type of an application, there may be one editor per view,
   * or a single shared editor for all views.
   */
  private final OpenTCSDrawingEditor fDrawingEditor;
  // Koordinaten für das Mess-Werkzeug
  private Point2D.Double fCurrentMousePoint = new Point2D.Double();
  private Point2D.Double fMouseEndPoint = new Point2D.Double();
  private Point2D.Double fMouseStartPoint = new Point2D.Double();
  /**
   * The JFrame.
   */
  private final JFrame fFrame;
  /**
   * Utility to manage the views.
   */
  private final ViewManager viewManager;
  // === openTCS ===
  // Tree for Vehicles and Layout components: Points, Paths, etc.
  private final StandardTreeViewPanel fComponentsTreeView = new StandardTreeViewPanel(fUndoRedoManager);
  // Der TreeViewManager, der Anfragen an den TreeView weiterleitet
  private final TreeViewManager fComponentsTreeManager;
  // Tree for the blocks
  private final StandardTreeViewPanel fBlocksTreeView = new StandardTreeViewPanel(fUndoRedoManager);
  private final TreeViewManager fBlocksTreeManager;
  // Tree for point groups
  private final StandardTreeViewPanel fGroupsTreeView = new StandardTreeViewPanel(fUndoRedoManager, StandardTreeViewPanel.GROUP_VIEW);
  private final TreeViewManager fGroupsTreeManager;
  // Die Ansicht der Properties
  private final AttributesComponent fPropertiesComponent = new AttributesComponent(fUndoRedoManager);
  // Verwaltet verschiedene Fahrkursmodelle
  private final OpenTCSModelManager fModelManager;
  // Splash - wird nur für Stand-Alone Applikation angezeigt
  private final ProgressIndicator progressIndicator;
  // Das gesamte Datenmodell
  private SystemModel fSystemModel;
  // Der Dialog zum Anzeigen der Eigenschaften eines Fahrkurselements.
  private JDialog fPropertiesDialog;
  private final DockingManager dockingManager = new DockingManager();
  /**
   * Registry for plugin panels.
   */
  private final PanelRegistry panelRegistry;
  /**
   * The proxy/connection manager to be used.
   */
  private final KernelProxyManager kernelProxyManager;
  /**
   * Whether the system/the vehicles are currently paused or not.
   */
  private boolean vehiclesPaused;
  private final KernelStatusPanel statusScrollPane = new KernelStatusPanel();
  private final StatusPanel statusPanel;
  private final VehiclesPanel vehiclesPanel = new VehiclesPanel();
  private final CourseObjectFactory crsObjFactory;
  /**
   * The process adapter factory to be used.
   */
  private final ProcessAdapterFactory procAdapterFactory;

  /**
   * Creates a new view.
   *
   * @param fFrame The <code>JFrame</code> this view is wrapped in.
   * @param progressIndicator The progress indicator to be used.
   * @param kernelProxyManager The proxy/connection manager to be used.
   * @param viewManager The view manager to be used.
   * @param tcsDrawingEditor The drawing editor to be used.
   * @param modelManager The model manager to be used.
   * @param statusPanel The status panel to be used.
   * @param panelRegistry The plugin panel registry to be used.
   * @param crsObjFactory The course object factory to be used.
   * @param procAdapterFactory The process adapter factory to be used.
   * @param systemModel The system model to be used.
   */
  @Inject
  public OpenTCSView(@ApplicationFrame JFrame fFrame,
                     ProgressIndicator progressIndicator,
                     KernelProxyManager kernelProxyManager,
                     ViewManager viewManager,
                     OpenTCSDrawingEditor tcsDrawingEditor,
                     OpenTCSModelManager modelManager,
                     StatusPanel statusPanel,
                     PanelRegistry panelRegistry,
                     CourseObjectFactory crsObjFactory,
                     ProcessAdapterFactory procAdapterFactory,
                     SystemModel systemModel) {
    this.progressIndicator = requireNonNull(progressIndicator,
                                            "progressIndicator");
    this.kernelProxyManager = requireNonNull(kernelProxyManager,
                                             "kernelProxyManager");
    this.viewManager = requireNonNull(viewManager, "viewManager");
    this.fDrawingEditor = requireNonNull(tcsDrawingEditor, "tcsDrawingEditor");
    this.fModelManager = requireNonNull(modelManager, "modelManager");
    this.statusPanel = requireNonNull(statusPanel, "statusPanel");
    this.panelRegistry = requireNonNull(panelRegistry, "panelRegistry");
    this.crsObjFactory = requireNonNull(crsObjFactory, "crsObjFactory");
    this.procAdapterFactory = requireNonNull(procAdapterFactory,
                                             "procAdapterFactory");
    this.fFrame = fFrame;
    
    this.fSystemModel = requireNonNull(systemModel, "systemModel is null");

    progressIndicator.setProgress(10, "openTCS view initialized");

    fDrawingEditor.setDrawingEditorListener(this);

    fActionManager = new ActionManager(this, kernelProxyManager, panelRegistry,
                                       crsObjFactory);

    // --- Tree View im Panel links oben ---
    fComponentsTreeManager = new TreeViewManager(fComponentsTreeView);
    fComponentsTreeView.getTree().getSelectionModel().setSelectionMode(
        TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    fComponentsTreeManager.setHideBlocks(true);

    fBlocksTreeManager = new TreeViewManager(fBlocksTreeView);

    fGroupsTreeManager = new TreeViewManager(fGroupsTreeView);

    progressIndicator.setProgress(15, "Initialize system model");
    setSystemModel(fSystemModel);

    // --- Properties View im Panel links unten ---
    fPropertiesComponent.setPropertiesContent(new PropertiesTableContent(this, this));
    fPropertiesComponent.setMinimumSize(new Dimension(100, 300));

    // --- MouseListener --- 
    VehicleDragHandler listener
        = new VehicleDragHandler(Cursors.getDragVehicleCursor());
    fComponentsTreeView.getTree().addMouseListener(listener);
    fComponentsTreeView.getTree().addMouseMotionListener(listener);

    createActions();
    initializeFrame();
  }

  public static OpenTCSView instance() {
    return instance;
  }

  public static void setInstance(OpenTCSView view) {
    instance = view;
  }

  /**
   * Shows or hides the specific <code>PanelFactory</code>.
   *
   * @param factory The factory resp. panel that shall be shown / hidden.
   * @param visible True to set it visible, false otherwise.
   */
  public void showPluginPanel(PanelFactory factory, boolean visible) {
    String id = factory.getClass().getName();
    SingleCDockable dockable
        = dockingManager.getCControl().getSingleDockable(id);
    if (dockable != null) {
      // dockable is not null at this point when the user hides the plugin 
      // panel by clicking on its menu entry
      PluggablePanel panel = (PluggablePanel) dockable.getFocusComponent();
      panel.plugOut();
      if (!dockingManager.getCControl().removeDockable(dockable)) {
        log.warning("Couldn't remove dockable for plugin panel '"
            + factory.getPanelDescription() + "'");
        return;
      }
    }
    if (!visible) {
      return;
    }
    final PluggablePanel panel = factory.createPanel();
    if (factory.providesPanel(kernel().getState())) {
      final DefaultSingleCDockable factoryDockable = dockingManager.createFloatingDockable(
          factory.getClass().getName(),
          factory.getPanelDescription(),
          panel);
      factoryDockable.addVetoClosingListener(new CVetoClosingListener() {

        @Override
        public void closing(CVetoClosingEvent event) {
        }

        @Override
        public void closed(CVetoClosingEvent event) {
          panel.plugOut();
          dockingManager.getCControl().removeDockable(factoryDockable);
        }
      });
      panel.plugIn();
    }
  }

  /**
   * Returns the DockingManager.
   *
   * @return The DockingManager.
   */
  public DockingManager getDockingManager() {
    return dockingManager;
  }

  /**
   * Adds a new drawing view to the tabbed wrappingPanel.
   *
   * @return The newly created Dockable.
   */
  public DefaultSingleCDockable addDrawingView() {
    JScrollPane newScrollPane = viewManager.getNewDrawingView(fSystemModel);

    int drawingViewIndex = viewManager.getNextDrawingViewIndex();

    String title = ResourceBundleUtil.getBundle().getString(
        "OpenTCSView.tab.drivingCourse") + " " + drawingViewIndex;
    DefaultSingleCDockable newDockable
        = dockingManager.createDockable("drivingCourse" + drawingViewIndex,
                                        title,
                                        newScrollPane,
                                        true);
    OpenTCSDrawingView drawingView
        = viewManager.putDrawingView(newDockable, newScrollPane);
    // Panel mit Buttons für Zoom-Faktor und Grid on/off wird links unten 
    // neben dem Scrollbalken der ScrollPane gezeichnetdrawingView
    JPanel placardPanel = fActionManager.createPlacardPanel(drawingView);
    newScrollPane.add(placardPanel, JScrollPane.LOWER_LEFT_CORNER);

    // Add to group pop ups
    if (drawingViewIndex > 0) { //don't add to modelling view
      ModelComponent groups = getSystemModel().getMainFolder(SystemModel.GROUPS);

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
    newDockable.addFocusListener(new DockableFocusListener());

    drawingView.handleFocusGained();
    firePropertyChange(OpenTCSDrawingView.FOCUS_GAINED, null, newDockable);

    return newDockable;
  }

  /**
   * Adds a new transport order view.
   *
   * @param shouldInitView If the newly created view should initialize itself
   * with current transport orders. It should be false when there isn't set up
   * an event dispatcher yet.
   */
  public void addTransportOrderView(boolean shouldInitView) {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    int biggestIndex = viewManager.getNextTransportOrderViewIndex();
    DefaultSingleCDockable lastTOView = viewManager.getLastTransportOrderView();
    TransportOrdersContainerPanel panel
        = new TransportOrdersContainerPanel(this);
    DefaultSingleCDockable newDockable
        = dockingManager.createDockable("transportOrders" + biggestIndex,
                                        bundle.getString("OpenTCSView.tab.transportOrders")
            + " " + biggestIndex, panel, true);
    viewManager.putTransportOrderView(newDockable, panel);

    if (shouldInitView) {
      panel.initView();
      OpenTCSEventDispatcher eventDispatcher
          = (OpenTCSEventDispatcher) getSystemModel().getEventDispatcher();

      if (eventDispatcher != null) {
        eventDispatcher.getTransportOrderDispatcher().addListener(panel);
      }
    }

    newDockable.addVetoClosingListener(new TransportOrderClosingListener(newDockable));

    final int indexToInsert;

    if (lastTOView != null) {
      indexToInsert = dockingManager.getTabPane(DockingManager.COURSE_TAB_PANE_ID).
          getStation().indexOf(lastTOView.intern()) + 1;
    }
    else {
      indexToInsert = viewManager.getDrawingViewMap().size();
    }

    dockingManager.addTabTo(newDockable, DockingManager.COURSE_TAB_PANE_ID, indexToInsert);
  }

  /**
   * Adds a new order sequence view.
   *
   * @param shouldInitView If the newly created view should initialize itself
   * with current transport orders. It should be false when there isn't set up
   * an event dispatcher yet.
   */
  public void addTransportOrderSequenceView(boolean shouldInitView) {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    int biggestIndex = viewManager.getNextOrderSequenceViewIndex();
    DefaultSingleCDockable lastOSView = viewManager.getLastOrderSequenceView();

    OrderSequencesContainerPanel panel = new OrderSequencesContainerPanel(this);
    DefaultSingleCDockable newDockable
        = dockingManager.createDockable("orderSequences" + biggestIndex,
                                        bundle.getString("OpenTCSView.tab.transportOrderSequences") + " " + biggestIndex, panel, true);
    viewManager.putOrderSequenceView(newDockable, panel);

    if (shouldInitView) {
      panel.initView();

      OpenTCSEventDispatcher eventDispatcher
          = (OpenTCSEventDispatcher) getSystemModel().getEventDispatcher();
      if (eventDispatcher != null) {
        eventDispatcher.getOrderSequenceDispatcher().addListener(panel);
      }
    }

    newDockable.addVetoClosingListener(new OrderSequenceClosingListener(newDockable));

    final int indexToInsert;
    if (lastOSView != null) {
      indexToInsert = dockingManager.getTabPane(DockingManager.COURSE_TAB_PANE_ID).
          getStation().indexOf(lastOSView.intern()) + 1;
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
  public void restoreDockingDefaultLayout() {
    List<DefaultSingleCDockable> drawingViewKeys
        = new ArrayList<>(viewManager.getDrawingViewMap().keySet());
    for (DefaultSingleCDockable dock : drawingViewKeys) {
      removeDrawingView(dock);
    }
    closeOpenedPluginPanels();
    dockingManager.removeDockable(dockingManager.getCControl().
        getSingleDockable(DockingManager.VEHICLES_DOCKABLE_ID));
    viewManager.reset();
    configStore.removeItem(NUMBER_OF_DRAWING_VIEWS);
    configStore.removeItem(NUMBER_OF_TRANSPORT_ORDER_VIEWS);
    configStore.removeItem(NUMBER_OF_ORDER_SEQUENCE_VIEWS);

    initializeFrame();

    // Depending on the current kernel state there may exist panels, now, that
    // shouldn't be visible.
    Thread t = new Thread(new Runnable() {

      @Override
      public void run() {
        setKernelState(kernel().getState());
      }
    });
    t.start();
  }

  /**
   * Creates a new group of elements.
   * The elements are selected from a CreateGroupPanel or from the context menu
   * of a Point / Location / Path
   *
   * @param components The components that will form that group.
   */
  public void createGroup(Set<ModelComponent> components) {
    Objects.requireNonNull(components, "components is null");
    if (components.isEmpty()) {
      return;
    }
    GroupAdapter groupAdapter
        = (GroupAdapter) procAdapterFactory.createAdapter(GroupModel.class);

    if (groupAdapter == null) {
      return;
    }

    try {
      groupAdapter.setEventDispatcher(getSystemModel().getEventDispatcher());
      GroupModel groupModel = new GroupModel();
      groupAdapter.setModel(groupModel);
      Group group = groupAdapter.createProcessObject();
      fSystemModel.getEventDispatcher().addProcessAdapter(groupAdapter);

      groupModel.createUserObject();

      for (String name : getDrawingViewNames()) {
        groupModel.setDrawingViewVisible(name, true);
      }

      ModelComponent folder = getSystemModel().getMainFolder(SystemModel.GROUPS);
      folder.add(groupModel);
      fGroupsTreeManager.addItem(folder, groupModel);

      for (ModelComponent component : components) {
        ProcessAdapter adapter = fSystemModel.getEventDispatcher().findProcessAdapter(component);
        TCSObjectReference<?> ref = (TCSObjectReference<?>) adapter.getProcessObject();
        kernel().addGroupMember(group.getReference(), ref);
        groupModel.add(component);
        fGroupsTreeManager.addItem(groupModel, component);
      }

      ((StandardTreeViewPanel) fGroupsTreeManager.getTreeView()).sortChildren();
      setHasUnsavedChanges(true);
    }
    catch (KernelRuntimeException ex) {
      log.log(Level.SEVERE, null, ex);
    }
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
    removeModelComponent(getSystemModel().getMainFolder(SystemModel.GROUPS), gm);
  }

  /**
   * Removes members from a group.
   *
   * @param userObjects The items that should be removed.
   */
  public void removeGroupMembers(Set<UserObject> userObjects) {
    if (!userObjects.isEmpty()) {
      List<ModelComponent> items = new ArrayList<>();
      Iterator<UserObject> it = userObjects.iterator();

      while (it.hasNext()) {
        UserObject next = it.next();
        ModelComponent dataObject = next.getModelComponent();
        items.add(dataObject);
        Group group = kernel().getTCSObject(Group.class, next.getParent().getName());

        if (group != null) {
          ProcessAdapter adapter;
          fGroupsTreeManager.removeItem(next);
          adapter = fSystemModel.getEventDispatcher().findProcessAdapter(dataObject);

          if (adapter != null) {
            TCSObjectReference<?> ref
                = (TCSObjectReference<?>) adapter.getProcessObject();
            kernel().removeGroupMember(group.getReference(), ref);
          }
        }
      }

      for (OpenTCSDrawingView drawingView : getDrawingViews()) {
        drawingView.setGroupVisible(new ArrayList<>(items), true);
      }

      setHasUnsavedChanges(true);
    }
  }

  /**
   * Adds the currently selected items on the drawing view to the given group.
   *
   * @param groupModel GroupFolder referencing the group.
   */
  public void addSelectedItemsToGroup(GroupModel groupModel) {
    Set<Figure> selectedFigures = fDrawingEditor.getActiveView().getSelectedFigures();
    Group group = kernel().getTCSObject(Group.class, groupModel.getName());

    if (group != null) {
      for (Figure next : selectedFigures) {
        FigureComponent model = next.get(FigureConstants.MODEL);

        if (model instanceof LinkModel) {
          // LinkModels are automatically set in/visible
          continue;
        }

        if (groupModel.contains(model)) {
          continue;
        }

        groupModel.add(model);
        fGroupsTreeManager.addItem(groupModel, model);
        ProcessAdapter adapter = fSystemModel.getEventDispatcher().findProcessAdapter(model);
        TCSObjectReference<?> ref
            = (TCSObjectReference<?>) adapter.getProcessObject();
        kernel().addGroupMember(group.getReference(), ref);
      }

      ((StandardTreeViewPanel) fGroupsTreeManager.getTreeView()).sortChildren();
      setHasUnsavedChanges(true);
    }
    else {
      log.log(Level.WARNING, "Group not found: {0}", groupModel.getName());
    }
  }

  /**
   * Toggles the visibility of the group members.
   *
   * @param gm The folder that contains the elements of the group.
   * @param visible Visible or not.
   */
  public void setGroupVisibilityInAllDrawingViews(GroupModel gm, boolean visible) {
    gm.setGroupVisible(visible);

    for (OpenTCSDrawingView drawingView : getDrawingViews()) {
      drawingView.setGroupVisible(gm.getChildComponents(), gm.isGroupVisible());
    }
  }

  /**
   * Toggles the visibility of the group members for a specific
   * <code>OpenTCSDrawingView</code>.
   *
   * @param title The title of the drawing view.
   * @param sf The group folder containing the elements to hide.
   * @param visible Visible or not.
   */
  public void setGroupVisibilityInDrawingView(String title, GroupModel sf, boolean visible) {
    sf.setDrawingViewVisible(title, visible);
    viewManager.setGroupVisibilityInDrawingView(title, sf, visible);
  }

  public JMenuBar getMenuBar() {
    return fMenuBar;
  }

  /**
   * Returns the horizontal ruler of the active view.
   *
   * @return A Ruler.
   */
  public Ruler getHorizontalRuler() {
    return viewManager.getHorizontalRuler();
  }

  /**
   * Returns the vertical ruler of the active view.
   *
   * @return A Ruler.
   */
  public Ruler getVerticalRuler() {
    return viewManager.getVerticalRuler();
  }

  /**
   * Creates a path that exists in the kernel but not in the plant overview, yet.
   * This method is currently needed for the plugin-panel
   * <code>PointConnectorFactory</code>.
   *
   * @param start A reference to the start point.
   * @param end A reference to the end point.
   */
  public void insertPath(TCSObjectReference<org.opentcs.data.model.Point> start,
                         TCSObjectReference<org.opentcs.data.model.Point> end) {
    if (hasOperationMode(GuiManager.OperationMode.MODELLING)) {
      OpenTCSProcessAdapter adapterP1
          = (OpenTCSProcessAdapter) getSystemModel().getEventDispatcher().findProcessAdapter(start);
      OpenTCSProcessAdapter adapterP2
          = (OpenTCSProcessAdapter) getSystemModel().getEventDispatcher().findProcessAdapter(end);
      PointModel modelP1 = (PointModel) adapterP1.getModel();
      PointModel modelP2 = (PointModel) adapterP2.getModel();
      LabeledPointFigure figureP1 = modelP1.getFigure();
      LabeledPointFigure figureP2 = modelP2.getFigure();

      for (Figure figure : getDrawingView().getDrawing().getFiguresFrontToBack()) {
        if (figure instanceof PathConnection) {
          PathConnection pc = (PathConnection) figure;
          if (pc.getStartFigure() == figureP1 && pc.getEndFigure() == figureP2) {
            return;
          }
        }
      }

      PathConnection pc = new PathConnection();
      pc.connect(figureP1, figureP2);
      getDrawingView().getDrawing().add(pc);
    }
  }

  public AttributesComponent getPropertiesComponent() {
    return fPropertiesComponent;
  }

  public TreeViewManager getTreeViewManager() {
    return fComponentsTreeManager;
  }

  public UndoRedoManager getUndoRedoManager() {
    return fUndoRedoManager;
  }

  /**
   * Informs all locations the theme has changed and they have to repaint.
   */
  public void updateLocationThemes() {
    for (DrawingView drawView : fDrawingEditor.getDrawingViews()) {
      for (Figure figure : drawView.getDrawing().getChildren()) {
        if (figure instanceof LabeledLocationFigure) {
          LabeledLocationFigure locFigure = (LabeledLocationFigure) figure;
          locFigure.propertiesChanged(new AttributesChangeEvent(this, locFigure.getLocationFigure().getModel()));
        }
      }
    }
  }

  /**
   * Informs all vehicles the theme has changed and they have to repaint.
   */
  public void updateVehicleThemes() {
    for (DrawingView drawView : fDrawingEditor.getDrawingViews()) {
      for (Figure figure : drawView.getDrawing().getChildren()) {
        if (figure instanceof VehicleFigure) {
          VehicleFigure vehicleFigure = (VehicleFigure) figure;
          vehicleFigure.propertiesChanged(new AttributesChangeEvent(this, vehicleFigure.getModel()));
        }
      }
    }
    vehiclesPanel.repaint();
  }

  /**
   * Logs a message to the status text area.
   *
   * @param message The message to log.
   */
  public void log(Message message) {
    statusScrollPane.log(message);
  }

  /**
   * Loads the current kernel model.
   */
  public void loadCurrentKernelModel() {
    progressIndicator.initialize();
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    progressIndicator.setProgress(0, bundle.getString("loadCurrentKernelModel.cleanup"));
    // Delete all figure of the old model
    for (DrawingView drawView : fDrawingEditor.getDrawingViews()) {
      ((OpenTCSDrawingView) drawView).removeAll();
    }
    // If there was a model before unregister the EventDispatcher
    try {
      getSystemModel().getEventDispatcher().release();
    }
    catch (Exception e) {
      log.log(Level.WARNING, "Could not release model: {0}", getSystemModel().getName());
    }
    vehiclesPanel.clearVehicles();

    // Start loading the new model
    progressIndicator.setProgress(50, bundle.getString("loadCurrentKernelModel.startLoading"));
    SystemModel restoredModel = fModelManager.restoreModel();

    if (restoredModel == null) {
      if (kernel().getState() == Kernel.State.MODELLING) {
        createEmptyModel();
      }
    }
    else {
      setSystemModel(restoredModel);
      // Sort the tree
      Enumeration<TreeNode> eTreeNodes
          = ((TreeNode) fComponentsTreeView.getTree().getModel().getRoot()).children();

      while (eTreeNodes.hasMoreElements()) {
        TreeNode node = eTreeNodes.nextElement();
        fComponentsTreeView.sortItems(node);
      }

      progressIndicator.setProgress(90, bundle.getString("loadCurrentKernelModel.setUpWorkingArea"));
      ModelComponent layoutComponent = fSystemModel.getMainFolder(SystemModel.LAYOUT);
      layoutComponent.addAttributesChangeListener(this);

      // Show model name in title
      if (fModelManager.getModelName().isEmpty()) {
        setModelNameProperty(Kernel.DEFAULT_MODEL_NAME);
      }
      else {
        setModelNameProperty(fModelManager.getModelName());
      }

      setupEventDispatcher(getSystemModel());
      OpenTCSEventDispatcher eventDispatcher = (OpenTCSEventDispatcher) getSystemModel().getEventDispatcher();

      for (TransportOrdersContainerPanel panel : viewManager.getTransportOrderMap().values()) {
        eventDispatcher.getTransportOrderDispatcher().addListener(panel);
      }

      for (OrderSequencesContainerPanel panel : viewManager.getOrderSequenceMap().values()) {
        eventDispatcher.getOrderSequenceDispatcher().addListener(panel);
      }

      eventDispatcher.updateModelProperties();
      eventDispatcher.register();

      // --- Operating mode ---
      // Initialize vehicles
      for (DrawingView drawView : fDrawingEditor.getDrawingViews()) {
        ((OpenTCSDrawingView) drawView).setVehicles(fSystemModel);
      }
      // Initialize transport order and -sequences panels
      for (TransportOrdersContainerPanel panel : viewManager.getTransportOrderMap().values()) {
        panel.initView();
      }

      for (OrderSequencesContainerPanel panel : viewManager.getOrderSequenceMap().values()) {
        panel.initView();
      }

      showStatus(Level.INFO, "Layout \"" + fModelManager.getModelName() + "\" loaded from kernel");

      if (restoredModel.getPointModels().isEmpty()) {
        for (DrawingView drawView : fDrawingEditor.getDrawingViews()) {
          ((OpenTCSDrawingView) drawView).initializeOffsetFigures();
        }
      }

      setHasUnsavedChanges(false);

      getUndoRedoManager().discardAllEdits();
    }
    progressIndicator.terminate();
  }

  /**
   *
   * @param oldState
   * @param newState
   */
  public void switchKernelState(Kernel.State oldState, Kernel.State newState) {
    if (newState != oldState) {
      if (newState == Kernel.State.SHUTDOWN) {
        stop();
      }
      else {
        setKernelState(newState);
        closeOpenedPluginPanels();
      }
    }
  }

  public void switchKernelState(RemoteKernelConnection.State newState) {
    if (newState == RemoteKernelConnection.State.DISCONNECTED) {
      stop();
    }
  }

  public void setKernelStateProperty(String kernelState) {
    String oldKernelState = sOperationMode;
    sOperationMode = kernelState;
    firePropertyChange(OPERATIONMODE_PROPERTY, oldKernelState, kernelState);
  }

  public void setModelNameProperty(String modelName) {
    String oldModelName = sModelName;
    sModelName = modelName;
    firePropertyChange(MODELNAME_PROPERTY, oldModelName, modelName);
  }

  public void updateModelName() {
    // Model has changed -> add "*" to the model's name
    if (hasUnsavedChanges()) {
      if (!sModelName.endsWith("*")) {
        sModelName += "*";
      }
    }
    // Changes have been saved -> remove "*"
    else {
      if (sModelName.endsWith("*")) {
        sModelName = sModelName.substring(0, sModelName.length() - 1);
      }
    }

    fComponentsTreeView.updateText(sModelName + " - " + sOperationMode);
  }

  /**
   * Adds a background image to the currently active drawing view.
   *
   * @param file The file with the image.
   */
  public void addBackgroundBitmap(File file) {
    if (hasOperationMode(GuiManager.OperationMode.MODELLING)) {
      viewManager.setBitmapToModellingView(file);
    }
    else {
      getDrawingView().addBackgroundBitmap(file);
    }
  }

  public String getModelName() {
    return sModelName;
  }

  public void setCurrentMousePoint(Point2D.Double currentPoint) {
    fCurrentMousePoint = currentPoint;
  }

  public void setMouseEndPoint(Point2D.Double endPoint) {
    fMouseEndPoint = endPoint;
  }

  public void setMouseStartPoint(Point2D.Double startPoint) {
    fMouseStartPoint = startPoint;
  }

  /**
   * Anzeige der aktuellen Mausposition im Statusfeld unten rechts
   *
   * @param showXY
   * @param showWH
   */
  public final void showPosition(boolean showXY, boolean showWH) {
    if (showXY) {
      double x = fCurrentMousePoint.x;
      double y = -fCurrentMousePoint.y;

      if (showWH) {
        double w = Math.abs(fMouseEndPoint.x - fMouseStartPoint.x);
        double h = Math.abs(fMouseEndPoint.y - fMouseStartPoint.y);
        statusPanel.setPositionText(
            String.format("X %.0f Y %.0f W %.0f H %.0f", x, y, w, h));
      }
      else {
        // TODO
        Iterator<LayoutModel> iLayouts = fSystemModel.getLayoutModels().iterator();

        if (iLayouts.hasNext()) {
          LayoutModel layout = iLayouts.next();
          LengthProperty lpx
              = (LengthProperty) layout.getProperty(LayoutModel.SCALE_X);
          LengthProperty lpy
              = (LengthProperty) layout.getProperty(LayoutModel.SCALE_Y);
          double scaleX = (double) lpx.getValue();
          double scaleY = (double) lpy.getValue();
          double xmm = x * scaleX;
          double ymm = y * scaleY;
          statusPanel.setPositionText(
              String.format("X %.0f (%.0fmm) Y %.0f (%.0fmm)", x, xmm, y, ymm));
        }
        else {
          statusPanel.setPositionText(String.format("X %.0f Y %.0f", x, y));
        }
      }
    }
    else {
      statusPanel.setPositionText("");
    }
  }

  /**
   * get und set für DrawingView property "scaleFactor" - beeinflusst den
   * Zoom-Faktor
   *
   * @return
   */
  public double getScaleFactor() {
    OpenTCSDrawingView drawingView = fDrawingEditor.getActiveView();
    return drawingView == null ? 1.0 : drawingView.getScaleFactor();
  }

  public void setScaleFactor(double newValue) {
    double oldValue = fDrawingEditor.getActiveView().getScaleFactor();
    fDrawingEditor.getActiveView().setScaleFactor(newValue);
    firePropertyChange("scaleFactor", oldValue, newValue);
  }

  public void scaleAndScrollTo(OpenTCSDrawingView drawView,
                               double newScale,
                               int xCenter,
                               int yCenter) {
    double oldValue = fDrawingEditor.getActiveView().getScaleFactor();
    drawView.scaleAndScrollTo(newScale, xCenter, yCenter);
    firePropertyChange("scaleFactor", oldValue, newScale);
  }

  /**
   * Setter- und Getter-Methoden, die implizit aus ToggleViewPropertyAction
   * aufgerufen werden.
   *
   * @return
   */
  public boolean isConstrainerVisible() {
    OpenTCSDrawingView drawingView = fDrawingEditor.getActiveView();
    return drawingView == null ? true : drawingView.isConstrainerVisible();
  }

  public void setConstrainerVisible(boolean newValue) {
    boolean oldValue = fDrawingEditor.getActiveView().isConstrainerVisible();
    fDrawingEditor.getActiveView().setConstrainerVisible(newValue);
    firePropertyChange(CONSTRAINER_VISIBLE_PROPERTY, oldValue, newValue);
  }

  public boolean isRulersVisible() {
    OpenTCSDrawingView drawingView = fDrawingEditor.getActiveView();
    return drawingView == null ? true : drawingView.isRulersVisible();
  }

  public void setRulersVisible(boolean newValue) {
    boolean oldValue = fDrawingEditor.getActiveView().isRulersVisible();
    fDrawingEditor.getActiveView().setRulersVisible(newValue);
    firePropertyChange(OpenTCSDrawingView.RULERS_VISIBLE_PROPERTY,
                       oldValue,
                       newValue);
  }

  public boolean isLabelsVisible() {
    OpenTCSDrawingView drawingView = fDrawingEditor.getActiveView();
    return drawingView == null ? true : drawingView.isLabelsVisible();
  }

  public void setLabelsVisible(boolean newValue) {
    boolean oldValue = fDrawingEditor.getActiveView().isLabelsVisible();
    fDrawingEditor.getActiveView().setLabelsVisible(newValue);
    firePropertyChange(LABELS_VISIBLE_PROPERTY, oldValue, newValue);
  }

  public boolean isBlocksVisible() {
    OpenTCSDrawingView drawingView = fDrawingEditor.getActiveView();
    return drawingView == null ? true : drawingView.isBlocksVisible();
  }

  public void setBlocksVisible(boolean newValue) {
    boolean oldValue = fDrawingEditor.getActiveView().isBlocksVisible();
    fDrawingEditor.getActiveView().setBlocksVisible(newValue);
    firePropertyChange(BLOCKS_VISIBLE_PROPERTY, oldValue, newValue);
  }

  public boolean isStaticRoutesVisible() {
    OpenTCSDrawingView drawingView = fDrawingEditor.getActiveView();
    return drawingView == null ? true : drawingView.isStaticRoutesVisible();
  }

  public void setStaticRoutesVisible(boolean newValue) {
    boolean oldValue = fDrawingEditor.getActiveView().isStaticRoutesVisible();
    fDrawingEditor.getActiveView().setStaticRoutesVisible(newValue);
    firePropertyChange(STATIC_ROUTES_VISIBLE_PROPERTY, oldValue, newValue);
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
      ModelComponent folder = getSystemModel().getFolder(modelComponent);

      if (folder == null) {
        // Workaround: Eigentlich sollten im Tree keine Folder selektiert sein!
        return null;
      }

      if (folder.contains(modelComponent)) {
        try {
          // Paste after Copy: Create clones of tree components (and figures)
          if (modelComponent instanceof FigureComponent) {
            Figure figure = ((FigureComponent) modelComponent).getFigure();

            if (figure instanceof LabeledFigure) {
              // Point, Location
              // Create new Figure with a "cloned" model
              final LabeledFigure clonedFigure = (LabeledFigure) figure.clone();
              // Place the figure relative to the position of the prototype
              AffineTransform tx = new AffineTransform();
              // TODO: Abstand des Duplikats konfigurierbar!
              // TODO: Bei mehrfachem Aufruf von Paste den Offset jeweils relativ zum Vorgänger, nicht zum Original
              tx.translate(50, 50);
              clonedFigure.transform(tx);
              getDrawingView().getDrawing().add(clonedFigure);
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
            // LocationType, Block, StaticRoute, Group
            modelComponent = modelComponent.clone();
          }
        }
        catch (CloneNotSupportedException ex) {
          log.log(Level.WARNING, "clone() not supported for {0}", modelComponent.getName());
        }
      }

      if (modelComponent instanceof FigureComponent) {
        if (!getDrawingView().getDrawing().contains(((FigureComponent) modelComponent).getFigure())) {
          getDrawingView().getDrawing().add(((FigureComponent) modelComponent).getFigure());
        }
      }

      userObject = addModelComponent(folder, modelComponent);
      restoredUserObjects.add(userObject);
    }

    return restoredUserObjects;
  }

  /**
   * @param figuresToClone
   * @return
   */
  public List<Figure> cloneFigures(List<Figure> figuresToClone) {
    // Buffer for Links and Paths associated with the cloned Points and Locations
    TreeSet<AbstractConnection> bufferedConnections = new TreeSet<>();
    // References the prototype Points and Locations to their clones
    Map<FigureComponent, FigureComponent> mClones = new HashMap<>();
    List<Figure> clonedFigures = new ArrayList<>();

    for (Figure figure : figuresToClone) {
      if (figure instanceof LabeledFigure) {
        // Location or Point
        FigureComponent model = figure.get(FigureConstants.MODEL);

        if (model != null) {
          bufferedConnections.addAll(model.getConnections());
        }

        LabeledFigure clonedFigure = (LabeledFigure) figure.clone();
        FigureComponent clonedModel = clonedFigure.get(FigureConstants.MODEL);

        if (model != null) {
          mClones.put(model, clonedModel);
        }
        // Paste cloned figure to the drawing
        AffineTransform tx = new AffineTransform();
        // TODO: Abstand des Duplikats konfigurierbar!
        // TODO: Bei mehrfachem Aufruf von Paste den Offset jeweils relativ zum Vorgänger, nicht zum Original
        tx.translate(50, 50);
        clonedFigure.transform(tx);
        getDrawingView().getDrawing().add(clonedFigure);
        // The new tree component will be created by "figureAdded()"
        clonedFigures.add(clonedFigure);
      }
      else if (figure instanceof BitmapFigure) {
        BitmapFigure clonedFigure = new BitmapFigure(
            new File(((BitmapFigure) figure).getImagePath()));
        AffineTransform tx = new AffineTransform();
        // TODO: Abstand des Duplikats konfigurierbar!
        // TODO: Bei mehrfachem Aufruf von Paste den Offset jeweils relativ zum Vorgänger, nicht zum Original
        tx.translate(50, 50);
        clonedFigure.transform(tx);
        getDrawingView().addBackgroundBitmap(clonedFigure);
      }
    }

    for (Figure figure : figuresToClone) {
      if (figure instanceof SimpleLineConnection) {
        // Link or Path
        SimpleLineConnection clonedFigure = (SimpleLineConnection) figure.clone();
        AbstractConnection model = (AbstractConnection) figure.get(FigureConstants.MODEL);
        AbstractConnection clonedModel = (AbstractConnection) clonedFigure.get(FigureConstants.MODEL);

        if (bufferedConnections.contains(model)) {
          if (model != null) {
            FigureComponent sourcePoint = (FigureComponent) model.getStartComponent();
            FigureComponent clonedSource = mClones.get(sourcePoint);  // The clone of the original source point
            Iterator<Connector> iConnectors = clonedSource.getFigure().getConnectors(null).iterator();
            clonedFigure.setStartConnector(iConnectors.next());

            FigureComponent destinationPoint = (FigureComponent) model.getEndComponent();
            FigureComponent clonedDestination = mClones.get(destinationPoint);
            iConnectors = clonedDestination.getFigure().getConnectors(null).iterator();
            clonedFigure.setEndConnector(iConnectors.next());

            clonedModel.setConnectedComponents(clonedSource, clonedDestination);
          }
        }

        getDrawingView().getDrawing().add(clonedFigure);
        // The new tree component will be created by "figureAdded()"
        clonedFigures.add(clonedFigure);
      }
    }

    return clonedFigures;
  }

  @Override	// View
  public void write(URI f, URIChooser chooser) throws IOException {
    log.severe("method entry");
    Drawing drawing = fDrawingEditor.getDrawing();
    OutputFormat outputFormat = drawing.getOutputFormats().get(0);
    outputFormat.write(f, drawing);
  }

  @Override	// View
  public void read(URI f, URIChooser chooser) throws IOException {
    log.severe("method entry");
    try {
      final Drawing drawing = createDrawing();
      InputFormat inputFormat = drawing.getInputFormats().get(0);
      inputFormat.read(f, drawing, true);

      SwingUtilities.invokeAndWait(new Runnable() {
        @Override
        public void run() {
          for (DrawingView drawView : fDrawingEditor.getDrawingViews()) {
            OpenTCSDrawingView tcsDrawView = (OpenTCSDrawingView) drawView;
            tcsDrawView.getDrawing().removeUndoableEditListener(fUndoRedoManager);
            tcsDrawView.setDrawing(drawing);
            tcsDrawView.getDrawing().addUndoableEditListener(fUndoRedoManager);
          }
          fUndoRedoManager.discardAllEdits();
        }
      });
    }
    catch (InterruptedException | InvocationTargetException e) {
      InternalError error = new InternalError();
      e.initCause(e);
      throw error;
    }
  }

  /**
   * Clears the view, for example by emptying the contents of the view, or by
   * reading a template contents from a file. By convention this method is never
   * invoked on the AWT Event Dispatcher Thread. Das darf es eben nicht! HH
   * 31.8.2012
   */
  @Override	// AbstractView
  public void clear() {
//		final Drawing newDrawing = createDrawing();
//		try {
//			SwingUtilities.invokeAndWait(new Runnable() {
//
//				@Override
//				public void run() {
//					drawingView.getDrawing().removeUndoableEditListener(undo);
//					drawingView.setDrawing(newDrawing);
//					drawingView.getDrawing().addUndoableEditListener(undo);
//					undo.discardAllEdits();
//				}
//			});
//		}
//		catch (InvocationTargetException | InterruptedException ex) {
//			Main.logger.log(Level.SEVERE, "Exception in clear():\n" + ex);
//		}
  }

  @Override	// AbstractView
  public void start() {
    // Load current model from the kernel
    progressIndicator.setProgress(40, "Load system model from openTCS kernel");

    if (kernel() == null) {
      showStatus(Level.WARNING, "Kernel not connected");
    }
    else {
      setKernelState(kernel().getState());
      loadCurrentKernelModel();

      for (OpenTCSDockableUtil util : viewManager.getDrawingViewMap().values()) {
        util.getDrawingView().handleFocusGained();
      }

      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          vehiclesPanel.setVehicleModels(fSystemModel.getVehicleModels());
        }
      });
    }

    progressIndicator.setProgress(100, "Model loaded from openTCS kernel.");
  }

  /**
   * Informs the view that a model component has changed.
   */
  public void modelComponentChanged() {
    setHasUnsavedChanges(true);
    firePropertyChange(View.HAS_UNSAVED_CHANGES_PROPERTY, null, null);
  }

  @Override	// AbstractView
  public void stop() {
    log.info("GUI terminating...");
    // don't save the modelling view
    configStore.setInt(NUMBER_OF_DRAWING_VIEWS, viewManager.getDrawingViewMap().size() - 1);
    configStore.setInt(NUMBER_OF_TRANSPORT_ORDER_VIEWS,
                       viewManager.getTransportOrderMap().size());
    configStore.setInt(NUMBER_OF_ORDER_SEQUENCE_VIEWS,
                       viewManager.getOrderSequenceMap().size());
    dockingManager.saveLayout();

    System.exit(0);
  }

  @Override	// AbstractView
  public boolean canSaveTo(URI file) {
    return new File(file).getName().endsWith(".xml");
  }

  @Override	// AbstractView
  public URI getURI() {
    String modelName = fModelManager.getModelName();

    try {
      uri = new URI(modelName);
    }
    catch (URISyntaxException ex) {
      log.log(Level.WARNING,
              "URISyntaxException in getURI({0}):\n{1}",
              new Object[] {modelName, ex});
    }

    return uri;
  }

  @Override	// GuiManager
  public OpenTCSDrawingView getDrawingView() {
    return fDrawingEditor.getActiveView();
  }

  /**
   * Returns all drawing views (including the modelling view)
   *
   * @return List with all known <code>OpenTCSDrawingViews</code>.
   */
  public List<OpenTCSDrawingView> getDrawingViews() {
    List<OpenTCSDrawingView> views = new ArrayList<>();

    for (OpenTCSDockableUtil util : viewManager.getDrawingViewMap().values()) {
      views.add(util.getDrawingView());
    }

    return views;
  }

  /**
   * Returns all drawing views (excluding the modelling view)
   *
   * @return List with all known <code>OpenTCSDrawingViews</code>, but not
   * the modelling view.
   */
  public List<OpenTCSDrawingView> getOperatingDrawingViews() {
    return viewManager.getOperatingDrawingViews();
  }

  public List<String> getDrawingViewNames() {
    return viewManager.getDrawingViewNames();
  }

  @Override	// GuiManager
  public OpenTCSDrawingEditor getEditor() {
    return fDrawingEditor;
  }

  @Override	// GuiManager
  public GuiManager.OperationMode getOperationMode() {
    return fActionManager.getOperationMode();
  }

  @Override// GuiManager
  public boolean hasOperationMode(GuiManager.OperationMode mode) {
    return getOperationMode().equals(mode);
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
    else {
      Set<ModelComponent> comps = new HashSet<>(1);
      comps.add(modelComponent);
      editingOptions(comps);
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
      // Wieder alle ursprünglich "angeklickten" Objekte im Tree markieren
      fComponentsTreeManager.selectItems(components);
    }
    else {  // Im Operating Mode kann nur eine Komponente ausgewählt werden
      selectModelComponent(modelComponent);
    }
  }

  @Override// GuiManager
  public boolean treeComponentRemoved(ModelComponent model) {
    boolean componentRemoved = false;
    boolean componentRemovedFromFolder = false;
    // Das Löschen ist nur im MODELLING Mode erlaubt
    if (kernel().getState() == Kernel.State.MODELLING) {
      // Point: zugehörige PointFigure löschen
      // Location: zugehörige LocationFigure löschen
      if (model instanceof PointModel || model instanceof LocationModel) {
        LabeledFigure lf = (LabeledFigure) ((FigureComponent) model).getFigure();
        // Die Drawing löscht auch ggf. mit dem Point verbundene PathConnections
        // bzw. eine mit der Location verbundenen LinkConnection
        getEditor().getActiveView().getDrawing().remove(lf);
        componentRemoved = true;
      }
      else if ((model instanceof LinkModel || model instanceof PathModel)
          && !(model.getParent() instanceof BlockModel)) {
        // Link: zugehörige LinkConnection löschen
        // Path: zugehörige PathConnection löschen
        SimpleLineConnection figure = (SimpleLineConnection) ((FigureComponent) model).getFigure();
        getEditor().getActiveView().getDrawing().remove(figure);
        componentRemoved = true;
      }
      else if (model instanceof VehicleModel) {
        VehicleFigure vf = (VehicleFigure) ((FigureComponent) model).getFigure();
        // Zugehörige VehicleFigure löschen
        getEditor().getActiveView().getDrawing().remove(vf);
        componentRemoved = true;
      }
      else if (model instanceof LocationTypeModel) {
        // Search if any Locations of this type exist
        for (LocationModel lm : fSystemModel.getLocationModels()) {
          if (lm.getLocationType() == model) {
            JOptionPane.showMessageDialog(
                this,
                ResourceBundleUtil.getBundle().getString("message.cannotDeleteLocationType.text"),
                ResourceBundleUtil.getBundle().getString("message.cannotDeleteLocationType.title"),
                JOptionPane.ERROR_MESSAGE);

            return false;
          }
        }

        componentRemoved = true;
      }

      ModelComponent folder = getSystemModel().getFolder(model);

      if (folder != null) {
        componentRemovedFromFolder = removeModelComponent(folder, model);
      }
    }

    return componentRemoved || componentRemovedFromFolder;
  }

  @Override	// GuiManager
  public void figureSelected(ModelComponent modelComponent) {
    modelComponent.addAttributesChangeListener(this);
    fPropertiesComponent.setModel(modelComponent);

    Figure figure = findFigure(modelComponent);
    OpenTCSDrawingView drawingView = fDrawingEditor.getActiveView();

    if (figure != null) {
      drawingView.clearSelection();
      drawingView.addToSelection(figure);
      // Ansicht zu dieser Figur scrollen
      drawingView.scrollTo(figure);
    }
  }

  @Override	// GuiManager
  public void showPropertiesDialog(ModelComponent modelComponent) {
    AttributesComponent dialogComponent
        = new AttributesComponent(fUndoRedoManager);
    dialogComponent.setPropertiesContent(new PropertiesTableContent(this, this));
    dialogComponent.setModel(modelComponent);

    if (fPropertiesDialog == null) {
      fPropertiesDialog = createPropertiesDialog(dialogComponent);
    }

    if (!fPropertiesDialog.isVisible()) {
      fPropertiesDialog.setVisible(true);
    }
  }

  @Override	// GuiManager
  public void blockSelected(FiguresFolder blockFiguresFolder) {
    Rectangle2D r = null;
    Iterator<Figure> figureIter = blockFiguresFolder.figures();

    while (figureIter.hasNext()) {
      Rectangle2D displayBox = figureIter.next().getDrawingArea(); // displayBox();

      if (r == null) {
        r = displayBox;
      }
      else {
        r.add(displayBox);
      }
    }

    OpenTCSDrawingView drawingView = fDrawingEditor.getActiveView();

    if (r != null) {
      drawingView.clearSelection();
      figureIter = blockFiguresFolder.figures();

      while (figureIter.hasNext()) {
        drawingView.addToSelection(figureIter.next());
      }

      //	drawingView.scrollTo(Geom.center(r));
      drawingView.updateBlock(blockFiguresFolder);
    }
  }

  @Override	// GuiManager
  public void createEmptyModel() {
    CloseFileAction action
        = (CloseFileAction) getActionMap().get(CloseFileAction.ID);
    action.actionPerformed(new ActionEvent(this,
                                           ActionEvent.ACTION_PERFORMED,
                                           CloseFileAction.ID_MODEL_CLOSING));

    if (action.getFileSavedStatus() == JOptionPane.CANCEL_OPTION) {
      return;
    }

    fPropertiesComponent.getPropertiesContent().reset();

    try {
      log.fine("Creating new driving course model...");
      kernel().createModel(Kernel.DEFAULT_MODEL_NAME);
    }
    catch (CredentialsException exc) {
      throw new IllegalStateException(exc);
    }
  }

  @Override	// GuiManager
  public void loadModel() {
    CloseFileAction action = (CloseFileAction) getActionMap().get(CloseFileAction.ID);
    action.actionPerformed(new ActionEvent(this,
                                           ActionEvent.ACTION_PERFORMED,
                                           CloseFileAction.ID_MODEL_CLOSING));

    switch (action.getFileSavedStatus()) {
      case JOptionPane.YES_OPTION:
        super.setHasUnsavedChanges(false);
        break;

      case JOptionPane.NO_OPTION:
        break;

      case JOptionPane.CANCEL_OPTION:
        return;
    }

    if (fModelManager.selectModel()) {
      if (fModelManager.loadModel()) {
        loadCurrentKernelModel();
      }
    }
  }

  @Override
  public boolean saveModel() {
    if (fModelManager.saveModel(false)) {
      setHasUnsavedChanges(false);
      setModelNameProperty(fModelManager.getModelName());
      return true;
    }

    return false;
  }

  @Override	// GuiManager
  public void saveModelAs() {
    if (fModelManager.saveModel(true)) {
      setHasUnsavedChanges(false);
      setModelNameProperty(fModelManager.getModelName());
    }
  }

  @Override	// GuiManager
  public void zoomViewToWindow() {
    fDrawingEditor.getActiveView().zoomViewToWindow();
  }

  @Override
  public void loadViewBookmark() {
    // Das Kernel-Objekt für das aktuelle VisualLayout
    // Beim starten der Visualisierung im Operating-Mode wird kein VisualLayout erzeugt!
    Set<VisualLayout> sLayouts = kernel().getTCSObjects(VisualLayout.class);

    if (sLayouts
        != null && sLayouts.size()
        > 0) {
      Iterator<VisualLayout> iLayouts = sLayouts.iterator();

      if (iLayouts.hasNext()) {
        VisualLayout layout = iLayouts.next();	// Es sollte genau 1 Layout geben!

        if (layout != null) {
          OpenTCSDrawingView currentView = fDrawingEditor.getActiveView();

          BookmarkSelectionPanel contentPanel
              = new BookmarkSelectionPanel(layout.getViewBookmarks(), false);
          StandardContentDialog dialog = new StandardContentDialog(this,
                                                                   contentPanel);
          ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
          dialog.setTitle(bundle.getString("view.loadViewBookmark.text"));
          dialog.setVisible(true);

          if (dialog.getReturnStatus() == StandardContentDialog.RET_OK) {
            ViewBookmark selectedBookmark = contentPanel.getSelectedItem();

            if (selectedBookmark != null) {
              String label = selectedBookmark.getLabel();
              int centerX = selectedBookmark.getCenterX();
              int centerY = selectedBookmark.getCenterY();
//						int viewRotation = selectedBookmark.getViewRotation();
              double viewScaleX = selectedBookmark.getViewScaleX();
//						double viewScaleY = selectedBookmark.getViewScaleY();
              // Ansicht verschieben und zoomen...
              currentView.scaleAndScrollTo(viewScaleX, centerX, centerY);
              // TODO: String from ResourceBundle
              showStatus(Level.INFO, "Ansicht \"" + label + "\" geladen.");
            }
          }
        }
      }
    }
    else {
      // TODO: String from ResourceBundle
      JOptionPane.showMessageDialog(this, "Es gibt noch kein VisualLayout!\nBitte das Modell im aktuellen Format speichern.");
    }
  }

  @Override	// GuiManager
  public void saveViewBookmark() {
    // Das Kernel-Objekt für das aktuelle VisualLayout
    // Beim starten der Visualisierung im Operating-Mode wird kein VisualLayout erzeugt!
    Set<VisualLayout> sLayouts = kernel().getTCSObjects(VisualLayout.class);

    if (sLayouts != null && sLayouts.size() > 0) {
      Iterator<VisualLayout> iLayouts = sLayouts.iterator();

      if (iLayouts.hasNext()) {
        VisualLayout layout = iLayouts.next();	// Es sollte genau 1 Layout geben!

        if (layout != null) {
          // Die View Bookmarks für dieses Layout
          List<ViewBookmark> bookmarks = new ArrayList<>();
          Iterator<ViewBookmark> iBookmarks = layout.getViewBookmarks().iterator();

          while (iBookmarks.hasNext()) {
            ViewBookmark next = iBookmarks.next();
            bookmarks.add(next);
          }

          ViewBookmarkNameChooser content
              = new ViewBookmarkNameChooser(bookmarks);
          ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
          String title = bundle.getString("view.saveViewBookmark.text");
          StandardDialog dialog = new StandardDialog(this, true, content, title);
          dialog.setLocationRelativeTo(this);
          dialog.setVisible(true);

          if (dialog.getReturnStatus() == StandardDialog.RET_OK) {
            String name = content.getChosenName();
            String nameInvalid = bundle.getString("message.nameExists");

            if (name.isEmpty()) {
              String enterName = bundle.getString("message.enterName");
              JOptionPane.showMessageDialog(this,
                                            enterName,
                                            nameInvalid,
                                            JOptionPane.ERROR_MESSAGE);
              return;
            }
            else {
              String nameAlreadyInUse
                  = bundle.getString("message.bookmark.nameExists");
              ViewBookmark bookmarkToRemove = null;

              for (ViewBookmark bm : bookmarks) {
                if (bm.getLabel().equals(name)) {
                  int result
                      = JOptionPane.showConfirmDialog(this,
                                                      nameAlreadyInUse,
                                                      nameInvalid,
                                                      JOptionPane.ERROR_MESSAGE);

                  if (result == JOptionPane.YES_OPTION) {
                    bookmarkToRemove = bm;
                  }
                  else {
                    return;
                  }
                }
              }

              bookmarks.remove(bookmarkToRemove);
              ViewBookmark bookmark = new ViewBookmark();
              bookmark.setLabel(name);
              OpenTCSDrawingView currentView = fDrawingEditor.getActiveView();
              // Aktuell sichtbarer Bildauschnitt in Pixel-Koordinaten
              Rectangle visibleRect = currentView.getVisibleRect();
              // Umrechnung in Zeichnungs-Koordinaten
              Rectangle2D.Double visibleViewRect = currentView.viewToDrawing(visibleRect);
              int centerX = (int) visibleViewRect.getCenterX();
              int centerY = (int) -visibleViewRect.getCenterY();	// Vorzeichen!
              bookmark.setCenterX(centerX);
              bookmark.setCenterY(centerY);
              double zoomFactor = currentView.getScaleFactor();
              bookmark.setViewScaleX(zoomFactor);
              bookmark.setViewScaleY(zoomFactor);	// TODO: x/y unterscheiden
              bookmark.setViewRotation(0);	// TODO
              // Die neue Bookmark der Liste zufügen
              bookmarks.add(bookmark);

              try {
                kernel().setVisualLayoutViewBookmarks(layout.getReference(),
                                                      bookmarks);
                // TODO: String from ResourceBundle
                showStatus(Level.INFO, "Ansicht \"" + name + "\" gespeichert.");
                setHasUnsavedChanges(true);
              }
              catch (UnsupportedKernelOpException ex) {
                // TODO: String from ResourceBundle
                showStatus(Level.WARNING,
                           "Operation \"Ansicht speichern\" nicht erlaubt!");
              }
            }
          }
        }
      }
    }
    else {
      // TODO: String from ResourceBundle
      JOptionPane.showMessageDialog(this, "Es gibt noch kein VisualLayout!\nBitte das Modell im aktuellen Format speichern.");
    }
  }

  @Override	// GuiManager
  public ModelComponent createModelComponent(
      Class<? extends ModelComponent> clazz) {
    requireNonNull(clazz, "clazz");

    ModelComponent model;
    if (clazz == VehicleModel.class) {
      model = crsObjFactory.createVehicleModel();
    }
    else if (clazz == LocationTypeModel.class) {
      model = crsObjFactory.createLocationTypeModel();
    }
    else if (clazz == BlockModel.class) {
      model = crsObjFactory.createBlockModel();
      List<BlockModel> blocks = getSystemModel().getBlockModels();

      ColorProperty p = (ColorProperty) model.getProperty(ElementPropKeys.BLOCK_COLOR);
      p.setColor(Colors.unusedBlockColor(blocks));
    }
    else if (clazz == StaticRouteModel.class) {
      model = crsObjFactory.createStaticRouteModel();
    }
    else {
      throw new IllegalArgumentException("Unhandled component class: " + clazz);
    }

    addModelComponent(getSystemModel().getFolder(model), model);

    return model;
  }

  /**
   * Checks if the selected tree object can be edited.
   *
   * @param dataObjects The data objects that need editing.
   */
  public void editingOptions(Set<ModelComponent> dataObjects) {
    if (dataObjects == null || dataObjects.isEmpty()) {
      if (kernel().getState() == Kernel.State.MODELLING
          && (getDrawingView().hasBufferedFigures()
              || fComponentsTreeView.hasBufferedObjects())) {
        fActionManager.changeEditMenuEnablePaste();
      }
      else {
        fActionManager.changeEditMenu(false);
      }
    }
    else {
      fActionManager.changeEditMenu(false);
      Iterator<ModelComponent> it = dataObjects.iterator();
      while (it.hasNext()) {
        ModelComponent dataObject = it.next();
        if (dataObject instanceof PointModel
            || dataObject instanceof LocationModel
            || dataObject instanceof LocationTypeModel
            || dataObject instanceof VehicleModel) {
          fActionManager.changeEditMenu(true);
        }
      }
    }
  }

  @Override	// GuiManager
  public void createTransportOrder() {
    CreateTransportOrderPanel contentPanel = new CreateTransportOrderPanel(this);
    StandardContentDialog dialog = new StandardContentDialog(this, contentPanel);
    dialog.setTitle(ResourceBundleUtil.getBundle().getString("TransportOrdersContainerPanel.newTransportOrder"));
    dialog.setVisible(true);

    if (dialog.getReturnStatus() == StandardContentDialog.RET_OK) {
      OpenTCSEventDispatcher ed
          = (OpenTCSEventDispatcher) fSystemModel.getEventDispatcher();
      ed.getTransportOrderDispatcher().createTransportOrder(
          contentPanel.getLocations(),
          contentPanel.getActions(),
          contentPanel.getSelectedDeadline(),
          contentPanel.getSelectedVehicle());
    }
  }

  @Override	// GuiManager
  public void resetSelectionTool() {
    ResourceBundleUtil drawLabels = ResourceBundleUtil.getBundle();

    for (JToolBar bar : fToolBars) {
      if (bar.getName() != null
          && bar.getName().equals(drawLabels.getString("toolBarCreation.title"))) {
        // At index 1 should be the default selection tool
        JToggleButton button = (JToggleButton) bar.getComponentAtIndex(1);
        button.setSelected(true);
        return;
      }
    }
  }

  @Override	// GuiManager
  public void findVehicle() {
    List<VehicleModel> vehicles = new ArrayList<>(fSystemModel.getVehicleModels());

    if (!vehicles.isEmpty()) {
      Collections.sort(vehicles);
      FindVehiclePanel content = new FindVehiclePanel(vehicles, fDrawingEditor.getActiveView());
      String title = ResourceBundleUtil.getBundle().getString("findVehiclePanel.title");
      CloseDialog dialog = new CloseDialog(this, true, content, title);
      dialog.setLocationRelativeTo(this);
      dialog.setVisible(true);
    }
  }

  @Override	// GuiManager
  public void showVehicles() {
    AllVehiclesPanel fContent = new AllVehiclesPanel();
    StandardContentDialog fDialog
        = new StandardContentDialog(this, fContent, false, StandardContentDialog.CLOSE);
    fDialog.setTitle(ResourceBundleUtil.getBundle().getString("actions.showVehicles.text"));
    fDialog.setVisible(true);
  }

  @Override	// AttributesChangeListener
  public void propertiesChanged(AttributesChangeEvent event) {
    if (event.getInitiator() == this) {
      return;
    }

    ModelComponent model = event.getModel();
    Property pName = model.getProperty(ModelComponent.NAME);

    if (pName != null && pName.hasChanged()) {
      fComponentsTreeManager.itemChanged(model);
    }

    if (model instanceof LayoutModel) {
      // Maßstabsänderung behandeln
      LengthProperty pScaleX
          = (LengthProperty) model.getProperty(LayoutModel.SCALE_X);
      LengthProperty pScaleY
          = (LengthProperty) model.getProperty(LayoutModel.SCALE_Y);
      updateLocationThemes();

      if (pScaleX.hasChanged() || pScaleY.hasChanged()) {
        double scaleX = (double) pScaleX.getValue();
        double scaleY = (double) pScaleY.getValue();

        if (scaleX != 0.0 && scaleY != 0.0) {
          fSystemModel.getDrawingMethod().getOrigin().setScale(scaleX, scaleY);
//					fModelManager.restoreModel();	// ???
        }
      }
    }

    if (model instanceof LocationModel) {
      if (model.getProperty(LocationModel.TYPE).hasChanged()) {
        SelectionProperty p
            = (SelectionProperty) model.getProperty(LocationModel.TYPE);
        LocationTypeModel type
            = fSystemModel.getLocationTypeModel((String) p.getValue());
        ((LocationModel) model).setLocationType(type);
        if (!(model == event.getInitiator())) {
          model.propertiesChanged(this);
        }
      }
    }

    if (model instanceof LocationTypeModel) {
      for (LocationModel locModel : fSystemModel.getLocationModels()) {
        locModel.updateTypeProperty(fSystemModel.getLocationTypeModels());
      }
    }

//	resetSelectionTool(); ???
  }

  @Override	// BlockChangeListener
  public void courseElementsChanged(BlockChangeEvent event) {
    BlockModel block = (BlockModel) event.getSource();
    // Remove all children from the block and re-add those that are still there.
    fBlocksTreeManager.removeChildren(block);
    for (ModelComponent component : block.getChildComponents()) {
      fBlocksTreeManager.addItem(block, component);
    }

    setHasUnsavedChanges(true);
    firePropertyChange(View.HAS_UNSAVED_CHANGES_PROPERTY, null, null);
  }

  @Override
  public void colorChanged(BlockChangeEvent event) {
  }

  @Override	// BlockChangeListener
  public void blockRemoved(BlockChangeEvent event) {
  }

  @Override	// StaticRouteChangeListener
  public void pointsChanged(StaticRouteChangeEvent event) {
    StaticRouteModel staticRoute = (StaticRouteModel) event.getSource();
    // Remove all elements from the static route and re-add the ones left.
    fComponentsTreeManager.removeChildren(staticRoute);
    for (ModelComponent component : staticRoute.getChildComponents()) {
      fComponentsTreeManager.addItem(staticRoute, component);
    }

    setHasUnsavedChanges(true);
    firePropertyChange(View.HAS_UNSAVED_CHANGES_PROPERTY, null, null);
  }

  @Override// DrawingEditorListener
  public void colorChanged(StaticRouteChangeEvent event) {
  }

  @Override
  public void staticRouteRemoved(StaticRouteChangeEvent event) {
  }

  @Override	// DrawingEditorListener
  public ModelComponent figureAdded(DrawingEditorEvent event) {
    Figure figure = event.getFigure();
    FigureComponent model = figure.get(FigureConstants.MODEL);

    if (model == null) {
      return null;
    }

    if (figure instanceof AttributesChangeListener) {
      model.addAttributesChangeListener((AttributesChangeListener) figure);
    }

    // Die zugefügte Figur soll auf Maßstabsänderungen des Layout reagieren
    if (figure instanceof OriginChangeListener) {
      Origin ref = fSystemModel.getDrawingMethod().getOrigin();

      if (ref != null) {
        ref.addListener((OriginChangeListener) figure);
        figure.set(FigureConstants.ORIGIN, ref);
      }
    }

    if (model instanceof FigureComponent) {
      model.setFigure(figure);
    }

    ModelComponent folder = getSystemModel().getFolder(model);
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

    FigureComponent model = figure.get(FigureConstants.MODEL);

    if (model == null) {
      return null;
    }

    synchronized (model) {
      // Die gelöschte Figur soll nicht mehr auf Verschieben des Referenzpunktes reagieren
      if (figure instanceof OriginChangeListener) {
        Origin ref = figure.get(FigureConstants.ORIGIN);

        if (ref != null) {
          ref.removeListener((OriginChangeListener) figure);
          figure.set(FigureConstants.ORIGIN, null);
        }
      }
      // Zunächst die Zuordnung zu Blocks, Groups, Static Routes im Kernel aufheben
      removeFromAllBlocks(model);
      removeFromAllStaticRoutes(model);
      removeFromAllGroups(model);
      // ... dann das Objekt selbst im Kernel löschen
      ModelComponent folder = getSystemModel().getFolder(model);

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
      // Einzelne Figur selektiert
      Figure figure = event.getFigure();

      if (figure != null) {
        FigureComponent model = figure.get(FigureConstants.MODEL);

        if (model != null) {
          model.addAttributesChangeListener(this);
          fPropertiesComponent.setModel(model);
          fComponentsTreeManager.selectItem(model);
          fBlocksTreeManager.selectItem(model);
          fGroupsTreeManager.selectItem(model);
        }
      }
    }
    else {
      // Mehrere Figuren selektiert
      List<ModelComponent> models = new LinkedList<>();
      Set<ModelComponent> components = new HashSet<>();

      for (Figure figure : event.getFigures()) {
        FigureComponent model = figure.get(FigureConstants.MODEL);
        if (model != null) {
          models.add(model);
          components.add(model);
        }
      }

      // Gemeinsame Properties der selektierten Figuren anzeigen
      ModelComponent model = new PropertiesCollection(models);
      fComponentsTreeManager.selectItems(components);
      fBlocksTreeManager.selectItems(components);
      fGroupsTreeManager.selectItems(components);
      fPropertiesComponent.setModel(model);
    }
  }

  /**
   * Sends messages to pauseVehicles() to start or pause vehicles.
   */
  public void pauseAllVehicles() {
    if (!vehiclesPaused) {
      pauseVehicles(0);
      vehiclesPaused = true;
    }
    else {
      pauseVehicles(100);
      vehiclesPaused = false;
    }
  }

  /**
   * Sends messages to the kernel to pause all vehicles.
   *
   * @param speed
   */
  private void pauseVehicles(int speed) {
    ModelComponent folder = getSystemModel().getMainFolder(SystemModel.VEHICLES);

    for (ModelComponent component : folder.getChildComponents()) {
      VehicleModel vModel = (VehicleModel) component;
      kernel().sendCommAdapterMessage(vModel.getReference(),
                                      new LimitSpeed(LimitSpeed.Type.ABSOLUTE,
                                                     speed));
    }
  }

  /**
   * Toggles all vehicle figures to ignore precise position or not.
   *
   * @param selected
   */
  public void ignorePrecisePosition(boolean selected) {
    configStore.setBoolean(ConfigConstants.IGNORE_VEHICLE_PRECISE_POSITION, selected);

    for (Figure figure : fDrawingEditor.getDrawing().getChildren()) {
      if (figure instanceof VehicleFigure) {
        ((VehicleFigure) figure).setIgnorePrecisePosition(selected);
      }
    }
  }

  /**
   * Toggles all vehicle figures to ingore orientation angle or not.
   *
   * @param selected
   */
  public void ignoreOrientationAngle(boolean selected) {
    configStore.setBoolean(ConfigConstants.IGNORE_VEHICLE_ORIENTATION_ANGLE, selected);

    for (Figure figure : fDrawingEditor.getDrawing().getChildren()) {
      if (figure instanceof VehicleFigure) {
        ((VehicleFigure) figure).setIgnoreOrientationAngle(selected);
      }
    }
  }

  @Override	// GuiManager
  protected void setHasUnsavedChanges(boolean newValue) {
    super.setHasUnsavedChanges(newValue);
//////    fUndoRedoManager.setHasSignificantEdits(newValue);
  }

  private void dockableGainedFocus(CDockable dockable) {
    OpenTCSDockableUtil util = viewManager.getDrawingViewMap().get(dockable);

    if (util == null) {
      return;
    }

    OpenTCSDrawingView drawView = util.getDrawingView();
    fDrawingEditor.setActiveView(drawView);
    // XXX Looks suspicious: Why are the same values set again here?
    drawView.setConstrainerVisible(drawView.isConstrainerVisible());
    drawView.setLabelsVisible(drawView.isLabelsVisible());
    drawView.setRulersVisible(drawView.isRulersVisible());
    drawView.setStaticRoutesVisible(drawView.isStaticRoutesVisible());
    drawView.setBlocksVisible(drawView.isBlocksVisible());
    drawView.handleFocusGained();
    firePropertyChange(OpenTCSDrawingView.FOCUS_GAINED, null, dockable);
  }

  /**
   * Removes a DrawingView.
   */
  private void removeDrawingView(DefaultSingleCDockable dock) {
    if (viewManager.getDrawingViewMap().containsKey(dock)) {
      // Remove from group pop ups
      ModelComponent groups = getSystemModel().getMainFolder(SystemModel.GROUPS);
      for (Object o : groups.getChildComponents()) {
        if (o instanceof GroupModel) {
          GroupModel gf = (GroupModel) o;
          gf.removeDrawingView(dock.getTitleText());
        }
      }
      fDrawingEditor.remove(viewManager.getDrawingViewMap().get(dock).getDrawingView());
      viewManager.getDrawingViewMap().remove(dock);
      dockingManager.removeDockable(dock);
    }
  }

  /**
   * Hier wird aus dem OpenTCSView-Panel und dem Panel für die ToolBars ein
   * neues Panel zusammengesetzt.
   *
   * @return
   */
  private JPanel wrapViewComponent() {
    // Add a dummy toolbar for dragging.
    // (Preview to see how the tool bar would look like after dragging?)
    final JToolBar toolBar = new JToolBar();
    // A wholeComponentPanel for toolbars above the OpenTCSView wholeComponentPanel.
    final JPanel toolBarPanel = new JPanel();
    toolBarPanel.setLayout(new BoxLayout(toolBarPanel, BoxLayout.LINE_AXIS));
    toolBar.setBorder(new PaletteToolBarBorder());

    final List<JPanel> toolBarPanels
        = Collections.synchronizedList(new ArrayList<JPanel>());
    final List<JToolBar> lToolBars = new LinkedList<>();

    // The new wholeComponentPanel for the whole component.
    JPanel wholeComponentPanel = new JPanel(new BorderLayout());
    wholeComponentPanel.add(toolBarPanel, BorderLayout.NORTH);
    wholeComponentPanel.add(getComponent());
    toolBarPanels.add(toolBarPanel);
    lToolBars.add(toolBar);

    JPanel viewComponent = wholeComponentPanel;

    LinkedList<Action> toolBarActions = new LinkedList<>();

    // XXX Why is this list iterated in *reverse* order?
    for (JToolBar curToolBar : new ReversedList<>(fToolBars)) {
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

      toolBarPanels.add(curToolBarPanel);
      lToolBars.add(curToolBar);
      viewComponent = wrappingPanel;
      toolBarActions.addFirst(new ToggleVisibleAction(curToolBar, curToolBar.getName()));
    }

    for (JToolBar bar : lToolBars) {
      configureToolBarButtons(bar);
    }

    getComponent().putClientProperty(toolBarActionsProperty, toolBarActions);

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

  /**
   * Teilt dem EventDispatcher das Leitsteuerungsobjekt und das
   * Hauptanwendungsobjekt mit. Ist beispielsweise nach dem Laden eines
   * Fahrkursmodells notwendig, da diese beiden Objekte nicht mit abgespeichert
   * werden.
   */
  private void setupEventDispatcher(SystemModel systemModel) {
    OpenTCSEventDispatcher dispatcher
        = (OpenTCSEventDispatcher) systemModel.getEventDispatcher();
    dispatcher.setKernel(kernel());
    dispatcher.setView(this);
  }

  private void closeOpenedPluginPanels() {
    for (PanelFactory factory : panelRegistry.getFactories()) {
      showPluginPanel(factory, false);
    }
  }

  /**
   * Sets the kernel state.
   *
   * @param state The new state.
   */
  private void setKernelState(final Kernel.State state) {
    try {
      SwingUtilities.invokeAndWait(new Runnable() {

        @Override
        public void run() {
          String kernelState = "?";
          ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();

          switch (state) {
            case MODELLING:
              viewManager.setKernelStateModelling(dockingManager);

              kernelState = bundle.getString("kernel.stateModelling");
              fActionManager.setOperationMode(GuiManager.OperationMode.MODELLING);
              break;

            case OPERATING:
              viewManager.setKernelStateOperating(dockingManager);
              vehiclesPanel.setVehicleModels(fSystemModel.getVehicleModels());

              kernelState = bundle.getString("kernel.stateOperating");
              fActionManager.setOperationMode(GuiManager.OperationMode.OPERATING);
              break;
          }
          // Show new state in the title
          setKernelStateProperty(kernelState);
        }
      });
    }
    catch (CredentialsException | IllegalArgumentException |
        KernelUnavailableException | InterruptedException |
        InvocationTargetException ex) {
      showStatus(Level.SEVERE,
                 "Exception in Kernel.setState(): " + ex);
      log.log(Level.SEVERE, "Unexpected exception", ex);
    }

    // Auf Selection Tool umschalten
    resetSelectionTool();
    // Properties-Tabelle zurücksetzen
    fPropertiesComponent.reset();
  }

  public String getKernelState() {
    return sOperationMode;
  }

  /**
   * Text-Anzeige in der Statuszeile (am unteren Bildrand).
   *
   * @param level Log-Level, bestimmt die Textfarbe
   * @param text Angezeigter Text
   */
  private void showStatus(Level level, String text) {
    statusPanel.setLogMessage(level, text);
  }

  /**
   * Returns a reference to the remote kernel.
   *
   * @return A reference to the remote kernel.
   */
  private Kernel kernel() {
    return kernelProxyManager.kernel();
  }

  /**
   * Creates a new Drawing for this view.
   */
  private Drawing createDrawing() {
    QuadTreeDrawing drawing = new QuadTreeDrawing();
    // TODO: Drawing nicht direkt in XML Datei speichern, sondern über Kernel
    OpenTCSDOMStorableInputOutputFormat ioFormat
        = new OpenTCSDOMStorableInputOutputFormat(new OpenTCSFactory());
    drawing.addInputFormat(ioFormat);
    drawing.addOutputFormat(ioFormat);

    return drawing;
  }

  /**
   * Hookmethode zur Erzeugung des ProcessAdapters. Zunächst wird geprüft, ob
   * nicht doch schon ein Adapter für das Model vorhanden ist. Dies kann während
   * der Synchronisation mit der Leitsteuerung der Fall sein.
   *
   * @param model
   * @return
   * @throws Exception
   */
  private ProcessAdapter createProcessAdapter(ModelComponent model) throws Exception {
    OpenTCSProcessAdapter adapter = (OpenTCSProcessAdapter) getSystemModel().getEventDispatcher().findProcessAdapter(model);

    if (adapter != null) {
      adapter.updateProcessProperties(true);
      return adapter;
    }

    adapter = (OpenTCSProcessAdapter) procAdapterFactory.createAdapter(model.getClass());

    if (adapter != null) {
      adapter.setModel(model);
      adapter.setEventDispatcher(getSystemModel().getEventDispatcher());
      adapter.createProcessObject();
      fSystemModel.getEventDispatcher().addProcessAdapter(adapter);

      if (adapter instanceof PathAdapter) {
        // Handle paths explicitly, because they need to inform
        // also the two associated points
        PathAdapter pathAdapter = (PathAdapter) adapter;
        pathAdapter.connectionChanged(null);
      }
      else if (adapter instanceof LinkAdapter) {
        // Handle links explicitly, because they need to inform
        // also the associated point and location
        LinkAdapter linkAdapter = (LinkAdapter) adapter;
        linkAdapter.connectionChanged(null);
      }
    }

    return adapter;
  }

  /**
   * Entfernt einen Knoten (oder eine Strecke?) aus allen Blockbereichen.
   *
   * @param model Das gelöschte Objekt
   */
  private void removeFromAllBlocks(ModelComponent model) {
    // The (invisible?) root folder of the "Blocks" tree...
    ModelComponent mainFolder = getSystemModel().getMainFolder(SystemModel.BLOCKS);

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
   * Removes a Point/Path/... from all Groups
   *
   * @param model The deleted object
   */
  private void removeFromAllGroups(ModelComponent model) {
    // The (invisible?) root folder of the "Groups" tree...
    ModelComponent mainFolder = getSystemModel().getMainFolder(SystemModel.GROUPS);

    synchronized (mainFolder) {
      // ... contains one folder for each Group
      for (ModelComponent groupFolder : mainFolder.getChildComponents()) {
        Group group = kernel().getTCSObject(Group.class, groupFolder.getName());

        if (group != null) {
          ProcessAdapter adapter = fSystemModel.getEventDispatcher().findProcessAdapter(model);

          if (adapter != null) {
            TCSObjectReference<?> ref
                = (TCSObjectReference<?>) adapter.getProcessObject();

            if (ref != null) {
              kernel().removeGroupMember(group.getReference(), ref);
              groupFolder.remove(model);
              fGroupsTreeManager.removeItem(model);
            }
          }
        }
      }
    }
  }

  /**
   * Entfernt einen Knoten aus allen statischen Routen.
   *
   * @param models die Knotenobjekte
   */
  private void removeFromAllStaticRoutes(ModelComponent model) {
    // The "Static Routes" folder of the "Components" tree...
    ModelComponent mainFolder = getSystemModel().getMainFolder(SystemModel.STATIC_ROUTES);

    synchronized (mainFolder) {
      // ... contains one folder for each StaticRoute

      for (ModelComponent routeFolderComp : mainFolder.getChildComponents()) {
        StaticRouteModel staticRoute = (StaticRouteModel) routeFolderComp;
        List<ModelComponent> elementsToRemove = new ArrayList<>();
        // All child components (Points, Paths) of one Block
        for (ModelComponent routeChildComp : staticRoute.getChildComponents()) {
          if (model == routeChildComp) {
            elementsToRemove.add(routeChildComp);
          }
        }

        if (!elementsToRemove.isEmpty()) {
          // At least one component found
          for (ModelComponent mc : elementsToRemove) {
            if (mc instanceof PointModel) {
              staticRoute.removePoint((PointModel) mc);
            }
          }
          staticRoute.pointsChanged();
        }
      }
    }
  }

  /**
   * Erzeugt den Dialog zum Anzeigen der Eigenschaften eines Fahrkurselements.
   * <p>
   * Entwurfsmuster: Fabrikmethode
   *
   * @param attributesComponent die Komponente zur Darstellung der Eigenschaften
   * eines Fahrkurselementes
   * @return der Dialog
   */
  private JDialog createPropertiesDialog(AttributesComponent attributesComponent) {
    String title
        = ResourceBundleUtil.getBundle().getString("dialog.properties.title");
    CloseDialog dialog
        = new CloseDialog(this, false, attributesComponent, title);
    dialog.setSize(250, 300);
    dialog.setLocationRelativeTo(this);
    dialog.setAlwaysOnTop(true);

    return dialog;
  }

  /**
   * Fügt dem übergebenen Elternobjekt ein Kindelement hinzu. Diese Methode wird
   * von Befehlsobjekten aufgerufen, die Undo/Redo verwenden. Daher hier keinen
   * Befehl für Undo/Redo aufrufen.
   *
   * @param folder
   * @param modelComponent
   */
  private UserObject addModelComponent(ModelComponent folder, ModelComponent modelComponent) {
    if (folder.contains(modelComponent)) {
      return modelComponent.getUserObject();
    }

    if (modelComponent instanceof LocationModel) {
      LocationModel location = (LocationModel) modelComponent;
      // Zu einer neu erzeugten Location zunächst den Default-Typ zuweisen
      if (location.getLocationType() == null) {
        List<LocationTypeModel> types = getSystemModel().getLocationTypeModels();
        LocationTypeModel type;

        if (types.isEmpty()) {
          type = (LocationTypeModel) createModelComponent(LocationTypeModel.class);
        }
        else {
          type = types.get(0);
        }

        location.setLocationType(type);
        location.updateTypeProperty(getSystemModel().getLocationTypeModels());
      }
    }

    folder.add(modelComponent);

    // Erst mit dem ProcessAdapter wird das ProcessObject erzeugt und vom Kernel ein Name generiert
    try {
      createProcessAdapter(modelComponent);
    }
    catch (Exception e) {
      log.log(Level.WARNING, null, e);
    }
    // Knoten "Modell"
    fComponentsTreeManager.addItem(folder, modelComponent);
    modelComponent.addAttributesChangeListener(this);
    // Neuer LocationType: allen existierenden Locations bekannt machen
    if (modelComponent instanceof LocationTypeModel) {
      List<LocationTypeModel> types = fSystemModel.getLocationTypeModels();

      for (LocationModel location : fSystemModel.getLocationModels()) {
        location.updateTypeProperty(types);
      }
    }

    if (modelComponent instanceof BlockModel) {
      fBlocksTreeManager.addItem(folder, modelComponent);
      ((BlockModel) modelComponent).addBlockChangeListener(this);

      for (DrawingView drawView : fDrawingEditor.getDrawingViews()) {
        ((OpenTCSDrawingView) drawView).blockAdded((BlockModel) modelComponent);
      }
    }
    else if (modelComponent instanceof GroupModel) {
      fGroupsTreeManager.addItem(folder, modelComponent);
      Group group = kernel().getTCSObject(Group.class, modelComponent.getName());
      for (ModelComponent member : modelComponent.getChildComponents()) {
        fGroupsTreeManager.addItem(modelComponent, member);
        ProcessAdapter adapter = fSystemModel.getEventDispatcher().findProcessAdapter(member);
        TCSObjectReference<?> ref
            = (TCSObjectReference<?>) adapter.getProcessObject();
        kernel().addGroupMember(group.getReference(), ref);
      }
    }
    else if (modelComponent instanceof StaticRouteModel) {
      ((StaticRouteModel) modelComponent).addStaticRouteChangeListener(this);
      // Add hops (Points) to StaticRoute
      if (modelComponent instanceof StaticRouteModel) {
        pointsChanged(new StaticRouteChangeEvent((StaticRouteModel) modelComponent));
      }
    }
    else if (modelComponent instanceof VehicleModel) {
      for (OpenTCSDrawingView drawingView : getDrawingViews()) {
        drawingView.addVehicle((VehicleModel) modelComponent);
      }
    }

    selectModelComponent(modelComponent);
    // Objekt zugefügt -> Änderung im Modell anzeigen
    setHasUnsavedChanges(true);
    firePropertyChange(View.HAS_UNSAVED_CHANGES_PROPERTY, null, null);

    return modelComponent.getUserObject();
  }

  /**
   * Entfernt das übergebene Model. Achtung: diese Methode wird von
   * Befehlsobjekten aufgerufen, die Undo/Redo verwenden. Daher hier keinen
   * Befehl für Undo/Redo aufrufen.
   *
   * @param parent
   * @param model
   */
  private boolean removeModelComponent(ModelComponent folder, ModelComponent model) {
    if (!folder.contains(model)) {
      return false;
    }

    boolean componentRemoved = false;

    synchronized (model) {
      if (!BlockModel.class.isInstance(folder)
          && !StaticRouteModel.class.isInstance(folder)) {
        // don't delete objects from a Blocks or StaticRoutes folder
        synchronized (folder) {
          folder.remove(model);
        }

        model.removeAttributesChangeListener(this);
        componentRemoved = true;
      }

      fPropertiesComponent.reset();

      if (model instanceof BlockModel) {
        // Remove Blocks from the Blocks tree
        fBlocksTreeManager.removeItem(model);
        ((BlockModel) model).blockRemoved();
        ((BlockModel) model).removeBlockChangeListener(this);
      }
      else if (model instanceof GroupModel) {
        fGroupsTreeManager.removeItem(model);
      }
      else if (componentRemoved) {
        fComponentsTreeManager.removeItem(model);
      }

      if (model instanceof StaticRouteModel) {
        ((StaticRouteModel) model).removeStaticRouteChangeListener(this);
      }
      else if (model instanceof LocationTypeModel) {
        for (LocationModel location : fSystemModel.getLocationModels()) {
          location.updateTypeProperty(fSystemModel.getLocationTypeModels());
        }
      }

      if (!BlockModel.class.isInstance(folder)
          && !StaticRouteModel.class.isInstance(folder)) {
        ProcessAdapter adapter = fSystemModel.getEventDispatcher().findProcessAdapter(model);

        if (adapter != null) {
          adapter.releaseProcessObject();
        }
      }

      // Objekt gelöscht -> Änderung im Modell anzeigen
      setHasUnsavedChanges(true);
      firePropertyChange(View.HAS_UNSAVED_CHANGES_PROPERTY, null, null);
    }

    return componentRemoved;
  }

  /**
   * Findet zu der Modellkomponente das passende Figure.
   *
   * @param model
   * @return
   */
  private Figure findFigure(ModelComponent model) {
    if (model instanceof FigureComponent) {
      FigureComponent figureComponent = (FigureComponent) model;

      if (figureComponent.getFigure() != null) {
        return figureComponent.getFigure();
      }
    }

////		FigureEnumeration e = ((StandardDrawingEditor) drawingEditor()).drawing().figures();
////
////		while (e.hasMoreElements()) {
////			Figure figure = e.nextFigure();
////
////			if (figure.getAttribute(FigureConstants.MODEL) == model) {
////				return figure;
////			}
////		}
    return null;
  }

  private void setSystemModel(SystemModel systemModel) {
    fSystemModel = Objects.requireNonNull(systemModel, "systemModel is null");
    getEditor().setSystemModel(systemModel);
    // Dies erzeugt eine neue Drawing in der DrawingView
    // --- Undo, Redo, Clipboard ---
    Drawing drawing = fDrawingEditor.getDrawing();
    drawing.addUndoableEditListener(fUndoRedoManager);
    // TODO: Drawing nicht direkt in XML Datei speichern, sondern über Kernel
    OpenTCSDOMStorableInputOutputFormat ioFormat
        = new OpenTCSDOMStorableInputOutputFormat(new OpenTCSFactory());
    drawing.addInputFormat(ioFormat);
    drawing.addOutputFormat(ioFormat);

    fComponentsTreeManager.restoreTreeView(systemModel);
    fBlocksTreeManager.restoreTreeView(systemModel.getMainFolder(SystemModel.BLOCKS));
    fBlocksTreeManager.getTreeView().sortRoot();
    fGroupsTreeManager.restoreTreeView(systemModel.getMainFolder(SystemModel.GROUPS));
    fGroupsTreeManager.getTreeView().sortRoot();
    fGroupsTreeManager.getTreeView().sortChildren();

    // Add Attribute Change Listeners to all objects
    for (VehicleModel vehicle : fSystemModel.getVehicleModels()) {
      vehicle.addAttributesChangeListener(this);
    }

    for (LayoutModel layout : fSystemModel.getLayoutModels()) {
      layout.addAttributesChangeListener(this);
    }

    for (PointModel point : fSystemModel.getPointModels()) {
      point.addAttributesChangeListener(this);
    }

    for (PathModel path : fSystemModel.getPathModels()) {
      path.addAttributesChangeListener(this);
    }

    for (LocationTypeModel locationType : fSystemModel.getLocationTypeModels()) {
      locationType.addAttributesChangeListener(this);
    }

    for (LocationModel location : fSystemModel.getLocationModels()) {
      location.addAttributesChangeListener(this);
    }

    for (LinkModel link : fSystemModel.getLinkModels()) {
      link.addAttributesChangeListener(this);
    }

    for (BlockModel block : fSystemModel.getBlockModels()) {
      block.addAttributesChangeListener(this);
      block.addBlockChangeListener(this);
    }

    for (StaticRouteModel staticRoute : fSystemModel.getStaticRouteModels()) {
      staticRoute.addStaticRouteChangeListener(this);
    }
  }

  @Override	// GuiManager
  public SystemModel getSystemModel() {
    return fSystemModel;
  }

  /**
   * Actions können erst initialisiert werden, nachdem die View mit einer
   * JHotDraw "Application" verknüpft wurde.
   *
   */
  private void createActions() {
    setActionMap(fActionManager.createActionMap());
    fToolBars = fActionManager.createToolBars();
    wrapViewComponent();
    fMenuBar = fActionManager.createMenuBar();
  }

  /**
   * Initiales the frame with the toolbars and the dockable elements.
   */
  private void initializeFrame() {
    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        fFrame.getContentPane().removeAll();
        dockingManager.initializeDockables(fFrame,
                                           vehiclesPanel,
                                           fComponentsTreeView,
                                           fBlocksTreeView,
                                           fGroupsTreeView,
                                           fPropertiesComponent,
                                           statusScrollPane);
        // Frame
        fFrame.setLayout(new BorderLayout());
        fFrame.add(wrapViewComponent(), BorderLayout.NORTH);
        fFrame.add(dockingManager.getCControl().getContentArea());
        fFrame.add(statusPanel, BorderLayout.SOUTH);
        restoreSavedDockables();
      }
    });
  }

  /**
   * Restores the dockables that were saved after the last exit.
   */
  private void restoreSavedDockables() {
    // --- DrawingView for modelling ---
    DefaultSingleCDockable modellingDockable = addDrawingView();
    viewManager.initModellingDockable(modellingDockable,
                                      ResourceBundleUtil.getBundle().getString("modellingDrawingView.name"));
    int n = configStore.getInt(NUMBER_OF_DRAWING_VIEWS, 1);
    for (int i = 0; i < n; i++) {
      addDrawingView();
    }

    n = configStore.getInt(NUMBER_OF_TRANSPORT_ORDER_VIEWS, 1);
    boolean shouldInit = getSystemModel().getEventDispatcher() != null;
    for (int i = 0; i < n; i++) {
      addTransportOrderView(shouldInit);
    }

    n = configStore.getInt(NUMBER_OF_ORDER_SEQUENCE_VIEWS, 1);
    for (int i = 0; i < n; i++) {
      addTransportOrderSequenceView(shouldInit);
    }

    dockingManager.getTabPane(DockingManager.COURSE_TAB_PANE_ID)
        .getStation().setFrontDockable(viewManager.evaluateFrontDockable());
    dockingManager.loadLayout();
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
      this.dragCursor = Objects.requireNonNull(dragCursor);
    }

    @Override
    public void mousePressed(MouseEvent e) {
      UserObject object = fComponentsTreeView.getDraggedUserObject(e);

      if (object instanceof VehicleUserObject
          && kernel().getState() == Kernel.State.OPERATING) {
        VehicleUserObject vehicleUserObject = (VehicleUserObject) object;
        vehicleModel = vehicleUserObject.getModelComponent();
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

      fComponentsTreeView.setCursor(dragCursor);
      setCursor(dragCursor);
    }

    @Override
    public void mouseReleased(MouseEvent event) {
      // Reset cursors to the default ones.
      fComponentsTreeView.setCursor(Cursor.getDefaultCursor());
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
      OpenTCSEventDispatcher ed
          = (OpenTCSEventDispatcher) fSystemModel.getEventDispatcher();
      ed.getTransportOrderDispatcher().createTransportOrder(model, vehicleModel);
    }
  }

  private class OrderSequenceClosingListener
      implements CVetoClosingListener {

    private final DefaultSingleCDockable newDockable;

    public OrderSequenceClosingListener(DefaultSingleCDockable newDockable) {
      this.newDockable = newDockable;
    }

    @Override
    public void closing(CVetoClosingEvent event) {
    }

    @Override
    public void closed(CVetoClosingEvent event) {
      if (event.isExpected()) {
        dockingManager.getCControl().removeDockable(newDockable);
        viewManager.getOrderSequenceMap().remove(newDockable);
      }
    }
  }

  private class TransportOrderClosingListener
      implements CVetoClosingListener {

    private final DefaultSingleCDockable newDockable;

    public TransportOrderClosingListener(DefaultSingleCDockable newDockable) {
      this.newDockable = newDockable;
    }

    @Override
    public void closing(CVetoClosingEvent event) {
    }

    @Override
    public void closed(CVetoClosingEvent event) {
      if (event.isExpected()) {
        dockingManager.getCControl().removeDockable(newDockable);
        viewManager.getTransportOrderMap().remove(newDockable);
      }
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

  private class DockableFocusListener
      implements CFocusListener {

    public DockableFocusListener() {
      // Do nada.
    }

    @Override
    public void focusGained(CDockable dockable) {
      dockableGainedFocus(dockable);
    }

    @Override
    public void focusLost(CDockable dockable) {
    }
  }
}
