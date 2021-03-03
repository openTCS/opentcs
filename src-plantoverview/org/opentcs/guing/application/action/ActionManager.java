/**
 * (c): IML.
 *
 */
package org.opentcs.guing.application.action;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Locale;
import static java.util.Objects.requireNonNull;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import org.jhotdraw.app.Application;
import org.jhotdraw.app.action.ActionUtil;
import org.jhotdraw.app.action.view.ToggleViewPropertyAction;
import org.jhotdraw.app.action.view.ViewPropertyAction;
import org.jhotdraw.draw.DrawingView;
import static org.jhotdraw.draw.DrawingView.CONSTRAINER_VISIBLE_PROPERTY;
import org.jhotdraw.draw.Figure;
import org.opentcs.access.Kernel;
import org.opentcs.guing.application.GuiManager.OperationMode;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.application.action.actions.CreateBlockAction;
import org.opentcs.guing.application.action.actions.CreateGroupAction;
import org.opentcs.guing.application.action.actions.CreateLocationTypeAction;
import org.opentcs.guing.application.action.actions.CreateStaticRouteAction;
import org.opentcs.guing.application.action.actions.CreateTransportOrderAction;
import org.opentcs.guing.application.action.actions.CreateVehicleAction;
import org.opentcs.guing.application.action.actions.ShowVehiclesAction;
import org.opentcs.guing.application.action.app.AboutAction;
import org.opentcs.guing.application.action.edit.ClearSelectionAction;
import org.opentcs.guing.application.action.edit.CopyAction;
import org.opentcs.guing.application.action.edit.CutAction;
import org.opentcs.guing.application.action.edit.DeleteAction;
import org.opentcs.guing.application.action.edit.DuplicateAction;
import org.opentcs.guing.application.action.edit.PasteAction;
import org.opentcs.guing.application.action.edit.SelectAllAction;
import org.opentcs.guing.application.action.edit.UndoRedoManager;
import org.opentcs.guing.application.action.file.CloseFileAction;
import org.opentcs.guing.application.action.file.LoadModelAction;
import org.opentcs.guing.application.action.file.NewModelAction;
import org.opentcs.guing.application.action.file.SaveModelAction;
import org.opentcs.guing.application.action.file.SaveModelAsAction;
import org.opentcs.guing.application.action.view.AddBitmapAction;
import org.opentcs.guing.application.action.view.AddDrawingViewAction;
import org.opentcs.guing.application.action.view.AddPluginPanelAction;
import org.opentcs.guing.application.action.view.AddTransportOrderSequenceView;
import org.opentcs.guing.application.action.view.AddTransportOrderView;
import org.opentcs.guing.application.action.view.FindVehicleAction;
import org.opentcs.guing.application.action.view.LoadViewBookmarkAction;
import org.opentcs.guing.application.action.view.LocationThemeAction;
import org.opentcs.guing.application.action.view.PauseAllVehiclesAction;
import org.opentcs.guing.application.action.view.RestoreDockingLayoutAction;
import org.opentcs.guing.application.action.view.SaveViewBookmarkAction;
import org.opentcs.guing.application.action.view.VehicleThemeAction;
import org.opentcs.guing.application.action.view.ZoomViewToWindowAction;
import org.opentcs.guing.components.drawing.OpenTCSDrawingView;
import static org.opentcs.guing.components.drawing.OpenTCSDrawingView.BLOCKS_VISIBLE_PROPERTY;
import static org.opentcs.guing.components.drawing.OpenTCSDrawingView.LABELS_VISIBLE_PROPERTY;
import static org.opentcs.guing.components.drawing.OpenTCSDrawingView.STATIC_ROUTES_VISIBLE_PROPERTY;
import org.opentcs.guing.components.drawing.figures.LabeledPointFigure;
import org.opentcs.guing.exchange.KernelProxyManager;
import org.opentcs.guing.util.ConfigConstants;
import org.opentcs.guing.util.CourseObjectFactory;
import org.opentcs.guing.util.Cursors;
import org.opentcs.guing.util.DefaultLocationThemeManager;
import org.opentcs.guing.util.DefaultVehicleThemeManager;
import org.opentcs.guing.util.LocationThemeManager;
import org.opentcs.guing.util.PanelRegistry;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.guing.util.VehicleThemeManager;
import org.opentcs.util.configuration.ConfigurationStore;
import org.opentcs.util.gui.plugins.LocationTheme;
import org.opentcs.util.gui.plugins.PanelFactory;
import org.opentcs.util.gui.plugins.VehicleTheme;

/**
 * Creates and handles the Actions which are activated in OpenTCSView by menus,
 * toolbars or buttons.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class ActionManager {

  /**
   * This classes configuration store.
   */
  private static final ConfigurationStore configStore
      = ConfigurationStore.getStore(OpenTCSView.class.getName());
  private static final double[] scaleFactors = {
    5.00, 4.00, 3.00, 2.00, 1.50, 1.25, 1.00, 0.75, 0.50, 0.25, 0.10};
  // Action IDs
  private static final String toggleGridActionID = "view.toggleGrid";
  private static final String toggleRulersActionID = "view.toggleRulers";
  private static final String toggleLabelsActionID = "view.toggleLabels";
  private static final String toggleBlocksActionID = "view.toggleBlocks";
  private static final String toggleStaticRoutesActionID = "view.toggleStaticRoutes";
  private final OpenTCSView view;
  private final PanelRegistry panelRegistry;
  private OperationMode operationMode = OperationMode.UNDEFINED;
  // Menu entries (depend on Kernel mode)
  private JMenu menuFile;
  private JMenu menuEdit;
  private JMenu menuActions;
  private JMenu menuView;
  private JMenu menuViewToolBars;
  private JMenu menuVehicleTheme;
  private JMenu menuLocationTheme;
  private JMenu menuPluginPanels;
  private JMenuItem menuItemNewModel;
  private JMenuItem menuItemLoadModel;
  private JMenuItem menuItemSaveModel;
  private JMenuItem menuItemSaveModelAs;
  private JMenuItem menuItemCreateTransportOrder;
  private JMenuItem menuItemFindVehicle;
  private JMenuItem menuItemShowVehicles;
  private JMenuItem menuItemCopy;
  private JMenuItem menuItemCut;
  private JMenuItem menuItemDuplicate;
  private JMenuItem menuItemPaste;
  private JMenuItem menuAddDrawingView;
  private JMenuItem menuTransportOrderView;
  private JMenuItem menuOrderSequenceView;
  private JMenuItem menuAddBitmap;
  private JCheckBoxMenuItem cbiIgnorePrecisePosition;
  private JCheckBoxMenuItem cbiIgnoreOrientationAngle;
  private JMenuItem cbiAlignLayoutWithModel;
  private JMenuItem cbiAlignModelWithLayout;
  /**
   * A manager for the tool bars.
   */
  private ToolBarManager toolBarManager;
  /**
   * Components in the placard panel.
   */
  private JComboBox<ZoomItem> fZoomComboBox;
  /**
   * A manager for vehicle themes.
   */
  private final VehicleThemeManager vehicleThemeManager;
  /**
   * A manager for location themes.
   */
  private final LocationThemeManager locationThemeManager;
  /**
   * The proxy/connection manager to be used.
   */
  private final KernelProxyManager kernelProxyManager;
  /**
   * The course object factory to be used.
   */
  private final CourseObjectFactory crsObjFactory;

  /**
   * Creates a new instance.
   *
   * @param view The view we work with.
   * @param kernelProxyManager The proxy/connection manager to be used.
   * @param panelRegistry The plugin panel registry to be used.
   * @param crsObjFactory The course object factory to be used.
   */
  public ActionManager(OpenTCSView view,
                       KernelProxyManager kernelProxyManager,
                       PanelRegistry panelRegistry,
                       CourseObjectFactory crsObjFactory) {
    this.view = requireNonNull(view, "view");
    this.kernelProxyManager = requireNonNull(kernelProxyManager,
                                             "kernelProxyManager");
    this.panelRegistry = requireNonNull(panelRegistry, "panelRegistry");
    this.crsObjFactory = requireNonNull(crsObjFactory, "crsObjFactory");
    // XXX This should actually be injected.
    this.vehicleThemeManager = DefaultVehicleThemeManager.getInstance();
    // XXX This should actually be injected.
    this.locationThemeManager = DefaultLocationThemeManager.getInstance();
  }

  /**
   * All Actions for OpenTCSView are registered here; createMenuBar() and
   * createToolBars() link the control elements to these Actions.
   *
   * @return
   */
  public ActionMap createActionMap() {
    ActionMap actionMap = new ActionMap();
    Application app = view.getApplication();
    Action action;

    // --- Menu File ---
    actionMap.put(NewModelAction.ID, new NewModelAction(view));
    actionMap.put(LoadModelAction.ID, new LoadModelAction(view));
    actionMap.put(SaveModelAction.ID, new SaveModelAction(view));
    actionMap.put(SaveModelAsAction.ID, new SaveModelAsAction(view));
    actionMap.put(CloseFileAction.ID, new CloseFileAction(view));

    // --- Menu Edit ---
    // Undo, Redo
    actionMap.put(UndoRedoManager.UNDO_ACTION_ID, view.getUndoRedoManager().getUndoAction());
    actionMap.put(UndoRedoManager.REDO_ACTION_ID, view.getUndoRedoManager().getRedoAction());
    // Cut, Copy, Paste, Duplicate, Delete
    actionMap.put(CutAction.ID, new CutAction());
    actionMap.put(CopyAction.ID, new CopyAction());
    actionMap.put(PasteAction.ID, new PasteAction());
    actionMap.put(DuplicateAction.ID, new DuplicateAction());
    actionMap.put(DeleteAction.ID, new DeleteAction());
    // Select all, Clear selection
    actionMap.put(SelectAllAction.ID, new SelectAllAction());
    actionMap.put(ClearSelectionAction.ID, new ClearSelectionAction());

    // --- Menu Actions ---
    // Menu item Actions -> Create ...
    actionMap.put(CreateLocationTypeAction.ID, new CreateLocationTypeAction(view));
    actionMap.put(CreateVehicleAction.ID, new CreateVehicleAction(view));
    actionMap.put(CreateBlockAction.ID, new CreateBlockAction(view));
    actionMap.put(CreateStaticRouteAction.ID, new CreateStaticRouteAction(view));
    actionMap.put(CreateTransportOrderAction.ID, new CreateTransportOrderAction(view));

    // --- Menu View ---
    // Menu View -> Add drawing view
    action = new AddDrawingViewAction(view);
    actionMap.put(AddDrawingViewAction.ID, action);
    action.putValue(Action.NAME, AddDrawingViewAction.ID);

    // Menu View -> Add transport order view
    action = new AddTransportOrderView(view);
    actionMap.put(AddTransportOrderView.ID, action);
    action.putValue(Action.NAME, AddTransportOrderView.ID);

    // Menu View -> Add transport order sequence view
    action = new AddTransportOrderSequenceView(view);
    actionMap.put(AddTransportOrderSequenceView.ID, action);
    action.putValue(Action.NAME, AddTransportOrderSequenceView.ID);

    // Popup menu View -> Zoom: Create one Action for each factor
    for (double sf : scaleFactors) {
      String id = (int) (sf * 100) + " %";
      action = new ViewPropertyAction(app,
                                      view,
                                      DrawingView.SCALE_FACTOR_PROPERTY,
                                      Double.TYPE,
                                      new Double(sf));
      actionMap.put(id, action);
      action.putValue(Action.NAME, id);
    }

    // Menu View -> Location theme
    action = new LocationThemeAction(view, null);
    actionMap.put(LocationThemeAction.UNDEFINED, action);
    action.putValue(Action.NAME, LocationThemeAction.UNDEFINED);

    for (LocationTheme curTheme : locationThemeManager.getThemes()) {
      String id = curTheme.getName();
      action = new LocationThemeAction(view, curTheme);
      actionMap.put(id, action);
      action.putValue(Action.NAME, id);
    }

    // Menu View -> Vehicle theme
    action = new VehicleThemeAction(view, null);
    actionMap.put(VehicleThemeAction.UNDEFINED, action);
    action.putValue(Action.NAME, VehicleThemeAction.UNDEFINED);

    for (VehicleTheme curTheme : vehicleThemeManager.getThemes()) {
      String id = curTheme.getName();
      action = new VehicleThemeAction(view, curTheme);
      actionMap.put(id, action);
      action.putValue(Action.NAME, id);
    }

    actionMap.put(ZoomViewToWindowAction.ID, new ZoomViewToWindowAction(view));
    actionMap.put(LoadViewBookmarkAction.ID, new LoadViewBookmarkAction(view));
    actionMap.put(SaveViewBookmarkAction.ID, new SaveViewBookmarkAction(view));
    actionMap.put(AddBitmapAction.ID, new AddBitmapAction(view));
    actionMap.put(RestoreDockingLayoutAction.ID, new RestoreDockingLayoutAction(view));

    actionMap.put(toggleGridActionID, new ToggleViewPropertyAction(app, view, OpenTCSDrawingView.CONSTRAINER_VISIBLE_PROPERTY));
    actionMap.put(toggleRulersActionID, new ToggleViewPropertyAction(app, view, OpenTCSDrawingView.RULERS_VISIBLE_PROPERTY));
    actionMap.put(toggleLabelsActionID, new ToggleViewPropertyAction(app, view, OpenTCSDrawingView.LABELS_VISIBLE_PROPERTY));
    actionMap.put(toggleBlocksActionID, new ToggleViewPropertyAction(app, view, OpenTCSDrawingView.BLOCKS_VISIBLE_PROPERTY));
    actionMap.put(toggleStaticRoutesActionID, new ToggleViewPropertyAction(app, view, OpenTCSDrawingView.STATIC_ROUTES_VISIBLE_PROPERTY));

    actionMap.put(FindVehicleAction.ID, new FindVehicleAction(view));
    actionMap.put(ShowVehiclesAction.ID, new ShowVehiclesAction(view));
    actionMap.put(PauseAllVehiclesAction.ID, new PauseAllVehiclesAction(view));
    actionMap.put(CreateGroupAction.ID, new CreateGroupAction(view));

    // --- Menu Help ---
    actionMap.put(AboutAction.ID, new AboutAction(view));

    return actionMap;
  }

  /**
   * Creates the toolbars.
   *
   * @return A list containing the created toolbars.
   */
  public List<JToolBar> createToolBars() {
    toolBarManager = new ToolBarManager(view, crsObjFactory);
    return toolBarManager.getToolBars();
  }

  /**
   * This method enables or disables the Editing menu Items for a specific Model
   * Component.
   *
   * @param shouldEdit: It tells the action manager whether to disable edit
   * options for a certain Model Component or not.
   */
  public void changeEditMenu(boolean shouldEdit) {
//    menuItemCopy.setEnabled(shouldEdit);
//    menuItemCut.setEnabled(shouldEdit);
//    menuItemDuplicate.setEnabled(shouldEdit);
//    menuItemPaste.setEnabled(shouldEdit);
  }

  /**
   * Disables the editing menu except the paste menu item.
   */
  public void changeEditMenuEnablePaste() {
//    menuItemCopy.setEnabled(false);
//    menuItemCut.setEnabled(false);
//    menuItemDuplicate.setEnabled(false);
//    menuItemPaste.setEnabled(true);
  }

  /**
   * Creates the menu bar.
   *
   * @return The menu bar.
   */
  public JMenuBar createMenuBar() {
    final JMenuBar menuBar = new JMenuBar();
    final ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    final ActionMap actionMap = view.getActionMap();
    JMenuItem menuItem;
    Action action;

    // --- Menu File ---
    menuFile = new JMenu();
    labels.configureMenu(menuFile, "file");

    // Menu item File -> New Model (nur in Mode Modelling)
    menuItemNewModel = new JMenuItem(actionMap.get(NewModelAction.ID));
    labels.configureMenu(menuItemNewModel, NewModelAction.ID);
    menuFile.add(menuItemNewModel);

    // Menu item File -> Load Model (nur in Mode Modelling)
    menuItemLoadModel = new JMenuItem(actionMap.get(LoadModelAction.ID));
    labels.configureMenu(menuItemLoadModel, LoadModelAction.ID);
    menuFile.add(menuItemLoadModel);

    // Menu item File -> Save Model
    menuItemSaveModel = new JMenuItem(actionMap.get(SaveModelAction.ID));
    labels.configureMenu(menuItemSaveModel, SaveModelAction.ID);
    menuFile.add(menuItemSaveModel);

    // Menu item File -> Save Model As
    menuItemSaveModelAs = new JMenuItem(actionMap.get(SaveModelAsAction.ID));
    labels.configureMenu(menuItemSaveModelAs, SaveModelAsAction.ID);
    menuFile.add(menuItemSaveModelAs);

    menuFile.addSeparator();
    // Menu item File -> Close
    menuItem = new JMenuItem(actionMap.get(CloseFileAction.ID));
    labels.configureMenu(menuItem, CloseFileAction.ID);
    menuFile.add(menuItem);	// TODO: Nur bei "Stand-Alone" Frame

    menuBar.add(menuFile);

    // --- Menu Edit ---
    menuEdit = new JMenu();
    labels.configureMenu(menuEdit, "edit");
    // Undo, Redo
    menuEdit.add(actionMap.get(UndoRedoManager.UNDO_ACTION_ID));
    menuEdit.add(actionMap.get(UndoRedoManager.REDO_ACTION_ID));
    menuEdit.addSeparator();
    // Cut, Copy, Paste, Duplicate
//    menuItemCut = menuEdit.add(actionMap.get(CutAction.ID));
//    menuItemCopy = menuEdit.add(actionMap.get(CopyAction.ID));
//    menuItemPaste = menuEdit.add(actionMap.get(PasteAction.ID));
//    menuItemDuplicate = menuEdit.add(actionMap.get(DuplicateAction.ID));
    // Delete
    menuEdit.add(actionMap.get(DeleteAction.ID));
    menuEdit.addSeparator();
    // Select all, Clear selection
    menuEdit.add(actionMap.get(SelectAllAction.ID));
    menuEdit.add(actionMap.get(ClearSelectionAction.ID));

    menuBar.add(menuEdit);

    // --- Menu Actions ---
    menuActions = new JMenu();
    labels.configureMenu(menuActions, "actions");

    // Menu item Actions -> Create Transport Order
    menuItemCreateTransportOrder = new JMenuItem(actionMap.get(CreateTransportOrderAction.ID));
    labels.configureMenu(menuItemCreateTransportOrder, CreateTransportOrderAction.ID);
    menuActions.add(menuItemCreateTransportOrder);
    menuActions.addSeparator();

    // Menu item Actions -> Find Vehicle
    menuItemFindVehicle = new JMenuItem(actionMap.get(FindVehicleAction.ID));
    labels.configureMenu(menuItemFindVehicle, FindVehicleAction.ID);
    menuActions.add(menuItemFindVehicle);

    // Menu item Actions -> Show Vehicles
    menuItemShowVehicles = new JMenuItem(actionMap.get(ShowVehiclesAction.ID));
    labels.configureMenu(menuItemShowVehicles, ShowVehiclesAction.ID);
    menuActions.add(menuItemShowVehicles);

    // Menu item Actions -> Ignore precise position
    cbiIgnorePrecisePosition = new JCheckBoxMenuItem("actions.ignorePrecisePosition");
    labels.configureMenu(cbiIgnorePrecisePosition, "actions.ignorePrecisePosition");
    menuActions.add(cbiIgnorePrecisePosition);
    cbiIgnorePrecisePosition.setSelected(configStore.getBoolean(ConfigConstants.IGNORE_VEHICLE_PRECISE_POSITION, false));
    cbiIgnorePrecisePosition.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        view.ignorePrecisePosition(cbiIgnorePrecisePosition.isSelected());
      }
    });

    // Menu item Actions -> Ignore orientation angle
    cbiIgnoreOrientationAngle = new JCheckBoxMenuItem(actionMap.get("actions.ignoreOrientationAngle"));
    labels.configureMenu(cbiIgnoreOrientationAngle, "actions.ignoreOrientationAngle");
    menuActions.add(cbiIgnoreOrientationAngle);
    cbiIgnoreOrientationAngle.setSelected(configStore.getBoolean(ConfigConstants.IGNORE_VEHICLE_ORIENTATION_ANGLE, false));
    cbiIgnoreOrientationAngle.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        view.ignoreOrientationAngle(cbiIgnoreOrientationAngle.isSelected());
      }
    });

    // Menu item Actions -> Copy model to layout
    cbiAlignModelWithLayout = new ModelToLayoutMenuItem(view.getEditor(), true);
    menuActions.add(cbiAlignModelWithLayout);

    // Menu item Actions -> Copy layout to model
    cbiAlignLayoutWithModel = new LayoutToModelMenuItem(view.getEditor(), true);
    menuActions.add(cbiAlignLayoutWithModel);

    menuBar.add(menuActions);

    // --- Menu View ---
    menuView = new JMenu();

    labels.configureMenu(menuView, "view");
    // Menu item View -> Load View Bookmark
    menuItem = new JMenuItem(actionMap.get(LoadViewBookmarkAction.ID));
    labels.configureMenu(menuItem, LoadViewBookmarkAction.ID);
    menuView.add(menuItem);
    // Menu item View -> Save View Bookmark
    menuItem = new JMenuItem(actionMap.get(SaveViewBookmarkAction.ID));
    labels.configureMenu(menuItem, SaveViewBookmarkAction.ID);
    menuView.add(menuItem);
    // Menu item View -> Add Background Image
    menuAddBitmap = new JMenuItem(actionMap.get(AddBitmapAction.ID));
    labels.configureMenu(menuAddBitmap, "view.addBitmap");
    menuView.add(menuAddBitmap);

    menuView.addSeparator();

    // Menu item View -> Add course view
    menuAddDrawingView = new JMenuItem(actionMap.get(AddDrawingViewAction.ID));
    labels.configureMenu(menuAddDrawingView, "view.drawingView");
    menuView.add(menuAddDrawingView);

    // Menu item View -> Add transport order view
    menuTransportOrderView = new JMenuItem(actionMap.get(AddTransportOrderView.ID));
    labels.configureMenu(menuTransportOrderView, "view.transportOrderView");
    menuView.add(menuTransportOrderView);

    // Menu item View -> Add transport order sequence view
    menuOrderSequenceView = new JMenuItem(actionMap.get(AddTransportOrderSequenceView.ID));
    labels.configureMenu(menuOrderSequenceView, "view.orderSequenceView");
    menuView.add(menuOrderSequenceView);

    menuView.addSeparator();

    // Popup menu View -> Toolbars: Show/hide single toolbars
    // The ToolBarActions are set in OpenTCSView.wrapViewComponent().
    // Therefore createToolBars() has to be called() first.
    Object object = view.getComponent().getClientProperty(OpenTCSView.toolBarActionsProperty);
    List<Action> viewActions = (List<Action>) object;
    menuViewToolBars = null;
    JCheckBoxMenuItem miToolbar;

    if (viewActions != null && !viewActions.isEmpty()) {
      menuViewToolBars = (viewActions.size() == 1) ? menuView : new JMenu(labels.getString("view.toolBars.text"));

      for (Action a : viewActions) {
        miToolbar = new JCheckBoxMenuItem(a);
        ActionUtil.configureJCheckBoxMenuItem(miToolbar, a);
        menuViewToolBars.add(miToolbar);

        if (miToolbar.getText().equals(labels.getString("toolBarCreation.title"))) {
          miToolbar.setEnabled(false);	// "Draw"-Toolbar musn't be disabled.
        }
      }

      if (menuViewToolBars != menuActions) {
        menuView.add(menuViewToolBars);
      }
    }

    // Menu item Actions -> Location theme
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    menuLocationTheme = new JMenu(bundle.getString("view.locationTheme.text"));
    final ButtonGroup themeGroup = new ButtonGroup();

    LocationTheme defaultTheme = locationThemeManager.getDefaultConfigStoreTheme();
    action = actionMap.get(LocationThemeAction.UNDEFINED);
    miToolbar = new JCheckBoxMenuItem(action);

    themeGroup.add(miToolbar);

    ActionUtil.configureJCheckBoxMenuItem(miToolbar, action);

    menuLocationTheme.add(miToolbar);

    if (defaultTheme == null) {
      miToolbar.setSelected(true);
    }

    for (LocationTheme curTheme : locationThemeManager.getThemes()) {
      String id = curTheme.getName();
      action = actionMap.get(id);
      miToolbar = new JCheckBoxMenuItem(action);
      themeGroup.add(miToolbar);
      ActionUtil.configureJCheckBoxMenuItem(miToolbar, action);
      menuLocationTheme.add(miToolbar);

      if (defaultTheme == curTheme) {
        miToolbar.setSelected(true);
      }
    }

    menuView.add(menuLocationTheme);

    // Menu item Actions -> Vehicle theme
    menuVehicleTheme = new JMenu(bundle.getString("view.vehicleTheme.text"));
    final ButtonGroup bgVehicleTheme = new ButtonGroup();

    VehicleTheme defaultVehicleTheme
        = vehicleThemeManager.getDefaultConfigStoreTheme();
    action = actionMap.get(VehicleThemeAction.UNDEFINED);
    miToolbar = new JCheckBoxMenuItem(action);

    bgVehicleTheme.add(miToolbar);

    ActionUtil.configureJCheckBoxMenuItem(miToolbar, action);

    menuVehicleTheme.add(miToolbar);

    if (defaultVehicleTheme == null) {
      miToolbar.setSelected(true);
    }

    for (VehicleTheme curTheme : vehicleThemeManager.getThemes()) {
      String id = curTheme.getName();
      action = actionMap.get(id);
      miToolbar = new JCheckBoxMenuItem(action);
      bgVehicleTheme.add(miToolbar);
      ActionUtil.configureJCheckBoxMenuItem(miToolbar, action);
      menuVehicleTheme.add(miToolbar);

      if (defaultVehicleTheme == curTheme) {
        miToolbar.setSelected(true);
      }
    }

    menuView.add(menuVehicleTheme);

    // Menu item View -> Plugins
    menuPluginPanels = new JMenu(bundle.getString("view.pluginPanels.text"));

    menuView.add(menuPluginPanels);

    // Menu item View -> Restore docking layout
    menuItem = new JMenuItem(actionMap.get(RestoreDockingLayoutAction.ID));

    menuItem.setText(bundle.getString("view.restoreDockingLayout.text"));
    menuView.add(menuItem);

    // Menu item View -> Language
    menuView.addSeparator();

    JMenu menuLanguage = new JMenu();
    labels.configureMenu(menuLanguage, "view.language");
    final ButtonGroup bgLanguage = new ButtonGroup();

    final JCheckBoxMenuItem cbiLanguageDE = new JCheckBoxMenuItem(labels.getString("language.german"));
    menuLanguage.add(cbiLanguageDE);

    if (Locale.getDefault().equals(Locale.GERMAN)) {
      cbiLanguageDE.setSelected(true);
    }

    bgLanguage.add(cbiLanguageDE);

    cbiLanguageDE.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            configStore.setString("LANGUAGE", "GERMAN");
            cbiLanguageDE.setSelected(true);
            JOptionPane.showMessageDialog(view.getDrawingView().getComponent(),
                                          labels.getString("message.restart"));
          }
        }
    );

    final JCheckBoxMenuItem cbiLanguageEN = new JCheckBoxMenuItem(labels.getString("language.english"));
    menuLanguage.add(cbiLanguageEN);

    if (Locale.getDefault().equals(Locale.ENGLISH)) {
      cbiLanguageEN.setSelected(true);
    }

    bgLanguage.add(cbiLanguageEN);

    cbiLanguageEN.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            configStore.setString("LANGUAGE", "ENGLISH");
            cbiLanguageEN.setSelected(true);
            JOptionPane.showMessageDialog(view.getDrawingView().getComponent(),
                                          labels.getString("message.restart"));
          }
        }
    );

    menuView.add(menuLanguage);

    menuBar.add(menuView);

    // --- Menu Help ---
    JMenu menuHelp = new JMenu();
    labels.configureMenu(menuHelp, "help");
    menuHelp.add(actionMap.get(AboutAction.ID));
    menuBar.add(menuHelp);

    return menuBar;
  }

  /**
   * Creates a panel with buttons for a DrawingView that is shown
   * at the bottom of the drawing.
   *
   * @param drawingView The DrawingView this panel will be added to.
   * @return The created panel.
   */
  public JPanel createPlacardPanel(OpenTCSDrawingView drawingView) {
    JPanel placardPanel = new JPanel();
    placardPanel.setLayout(new BoxLayout(placardPanel, BoxLayout.X_AXIS));

    fZoomComboBox = zoomComboBox(drawingView);
    placardPanel.add(fZoomComboBox);
    placardPanel.add(zoomViewToWindowButton());

    // --- Listens to draggings in the drawing ---
    ViewDragScrollListener dragScrollListener = new ViewDragScrollListener(drawingView);
    drawingView.addMouseListener(dragScrollListener);
    drawingView.addMouseMotionListener(dragScrollListener);
    drawingView.addMouseWheelListener(dragScrollListener);

    // Show/hide grid
    AbstractButton toggleConstrainerButton = toggleConstrainerButton(drawingView);
    toggleConstrainerButton.setSelected(drawingView.isConstrainerVisible());
    placardPanel.add(toggleConstrainerButton);

    // Show/hide rulers
    AbstractButton toggleRulersButton = toggleRulersButton(drawingView);
    toggleRulersButton.setSelected(drawingView.isRulersVisible());
    placardPanel.add(toggleRulersButton);

    // Show/hide leabels
    AbstractButton toggleLabelsButton = toggleLabelsButton(drawingView);
    toggleLabelsButton.setSelected(drawingView.isLabelsVisible());
    placardPanel.add(toggleLabelsButton);

    // Show/hide blocks
    AbstractButton toggleBlocksButton = toggleBlocksButton(drawingView);
    toggleBlocksButton.setSelected(drawingView.isBlocksVisible());
    placardPanel.add(toggleBlocksButton);

    // Show/hide static routes
    AbstractButton toggleStaticRoutesButton = toggleStaticRoutesButton(drawingView);
    toggleStaticRoutesButton.setSelected(drawingView.isStaticRoutesVisible());
    placardPanel.add(toggleStaticRoutesButton);

    return placardPanel;
  }

  /**
   * Creates the combo box with different zoom factors.
   *
   * @param drawingView The DrawingView this combo box will belong to.
   * @return The created combo box.
   */
  private JComboBox<ZoomItem> zoomComboBox(final OpenTCSDrawingView drawingView) {
    final JComboBox<ZoomItem> comboBox = new JComboBox<>();
    comboBox.setEditable(true);
    comboBox.setFocusable(true);

    for (int i = 0; i < scaleFactors.length; i++) {
      comboBox.addItem(new ZoomItem(scaleFactors[i]));

      if (scaleFactors[i] == 1.0) {
        comboBox.setSelectedIndex(i);
      }
    }

    comboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final double scaleFactor;

        if (comboBox.getSelectedItem() instanceof ZoomItem) {
          // A zoom step of the array scaleFactors[]
          ZoomItem item = (ZoomItem) comboBox.getSelectedItem();
          scaleFactor = item.getScaleFactor();
        }
        else {
          try {
            // Text input in the combo box
            String text = (String) comboBox.getSelectedItem();
            double factor = Double.parseDouble(text.split(" ")[0]);
            scaleFactor = factor * 0.01;	// Eingabe in %
            comboBox.setSelectedItem((int) (factor + 0.5) + " %");
          }
          catch (NumberFormatException ex) {
            comboBox.setSelectedIndex(0);
            return;
          }
        }

        if (view.getDrawingView() == drawingView) {
          view.setScaleFactor(scaleFactor);
        }
      }
    });

    drawingView.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        // String constants are interned
        if ("scaleFactor".equals(evt.getPropertyName())) {
          double scaleFactor = (double) evt.getNewValue();

          for (int i = 0; i < comboBox.getItemCount(); i++) {
            // One of the predefined scale factors was selected
            if (scaleFactor == comboBox.getItemAt(i).getScaleFactor()) {
              comboBox.setSelectedIndex(i);
              break;
            }

            if (i + 1 < comboBox.getItemCount()
                && scaleFactor < comboBox.getItemAt(i).getScaleFactor()
                && scaleFactor > comboBox.getItemAt(i + 1).getScaleFactor()) {
              // Insert the new scale factor between the next smaller / larger entries
              ZoomItem newItem = new ZoomItem(scaleFactor);
              comboBox.insertItemAt(newItem, i + 1);
              comboBox.setSelectedItem(newItem);
            }
            else if (scaleFactor > comboBox.getItemAt(0).getScaleFactor()) {
              // Insert new item for scale factor larger than the largest predefined factor
              ZoomItem newItem = new ZoomItem(scaleFactor);
              comboBox.insertItemAt(newItem, 0);
              comboBox.setSelectedItem(newItem);
            }
            else if (scaleFactor < comboBox.getItemAt(comboBox.getItemCount() - 1).getScaleFactor()) {
              // Insert new item for scale factor larger than the largest predefined factor
              ZoomItem newItem = new ZoomItem(scaleFactor);
              comboBox.insertItemAt(newItem, comboBox.getItemCount());
              comboBox.setSelectedItem(newItem);
            }
          }
        }
      }
    });

    return comboBox;
  }

  /**
   * Creates a button that zooms the drawing to a scale factor so that
   * it fits the window size.
   *
   * @return The created button.
   */
  private JButton zoomViewToWindowButton() {
    final JButton button = new JButton();
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    labels.configureToolBarButton(button, "view.zoomViewToWindow");
    button.setMargin(new Insets(0, 0, 0, 0));
    button.setFocusable(false);

    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        view.zoomViewToWindow();
      }
    });

    return button;
  }

  /**
   * Creates a button to toggle the grid in the drawing.
   *
   * @param view The DrawingView the button will belong to.
   * @return The created button.
   */
  private AbstractButton toggleConstrainerButton(final OpenTCSDrawingView drawingView) {
    final JToggleButton toggleButton = new JToggleButton();
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    labels.configureToolBarButton(toggleButton, toggleGridActionID);
    toggleButton.setMargin(new Insets(0, 0, 0, 0));
    toggleButton.setFocusable(false);

    toggleButton.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent event) {
        if (view.getDrawingView() == drawingView) {
          drawingView.setConstrainerVisible(toggleButton.isSelected());
          view.firePropertyChange(CONSTRAINER_VISIBLE_PROPERTY,
                                  !toggleButton.isSelected(),
                                  toggleButton.isSelected());
        }
      }
    });

    drawingView.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(DrawingView.CONSTRAINER_VISIBLE_PROPERTY)) {
          if (view.getDrawingView() == drawingView) {
            toggleButton.setSelected(view.getDrawingView().isConstrainerVisible());
          }
        }
      }
    });

    return toggleButton;
  }

  /**
   * Creates a button to toggle the rulers in the drawing.
   *
   * @param view The DrawingView the button will belong to.
   * @return The created button.
   */
  private AbstractButton toggleRulersButton(final OpenTCSDrawingView drawingView) {
    final JToggleButton toggleButton = new JToggleButton();
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    labels.configureToolBarButton(toggleButton, toggleRulersActionID);
    toggleButton.setMargin(new Insets(0, 0, 0, 0));
    toggleButton.setFocusable(false);

    toggleButton.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent event) {
        if (view.getDrawingView() == drawingView) {
          drawingView.setRulersVisible(toggleButton.isSelected());
          view.firePropertyChange(OpenTCSDrawingView.RULERS_VISIBLE_PROPERTY,
                                  !toggleButton.isSelected(),
                                  toggleButton.isSelected());
        }
      }
    });

    drawingView.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(OpenTCSDrawingView.RULERS_VISIBLE_PROPERTY)) {
          if (view.getDrawingView() == drawingView) {
            toggleButton.setSelected(view.getDrawingView().isRulersVisible());
          }
        }
      }
    });

    return toggleButton;
  }

  /**
   * Creates a button to toglle the labels.
   *
   * @param view The DrawingView the button will belong to.
   * @return The created button.
   */
  private AbstractButton toggleLabelsButton(final OpenTCSDrawingView drawingView) {
    final JToggleButton toggleButton = new JToggleButton();
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    labels.configureToolBarButton(toggleButton, toggleLabelsActionID);
    toggleButton.setMargin(new Insets(0, 0, 0, 0));
    toggleButton.setFocusable(false);

    toggleButton.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent event) {
        if (view.getDrawingView() == drawingView) {
          drawingView.setLabelsVisible(toggleButton.isSelected());
          view.firePropertyChange(LABELS_VISIBLE_PROPERTY,
                                  !toggleButton.isSelected(),
                                  toggleButton.isSelected());
        }
      }
    });

    drawingView.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(OpenTCSDrawingView.LABELS_VISIBLE_PROPERTY)) {
          if (view.getDrawingView() == drawingView) {
            toggleButton.setSelected(view.getDrawingView().isLabelsVisible());
          }
        }
      }
    });

    return toggleButton;
  }

  /**
   * Creates a button to toggle the blocks in the drawing.
   *
   * @param view The DrawingView the button will belong to.
   * @return The created button.
   */
  private AbstractButton toggleBlocksButton(final OpenTCSDrawingView drawingView) {
    final JToggleButton toggleButton = new JToggleButton();
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    labels.configureToolBarButton(toggleButton, toggleBlocksActionID);
    toggleButton.setMargin(new Insets(0, 0, 0, 0));
    toggleButton.setFocusable(false);

    toggleButton.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent event) {
        if (view.getDrawingView() == drawingView) {
          drawingView.setBlocksVisible(toggleButton.isSelected());
          view.firePropertyChange(BLOCKS_VISIBLE_PROPERTY,
                                  !toggleButton.isSelected(),
                                  toggleButton.isSelected());
        }
      }
    });

    drawingView.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(OpenTCSDrawingView.BLOCKS_VISIBLE_PROPERTY)) {
          if (view.getDrawingView() == drawingView) {
            toggleButton.setSelected(view.getDrawingView().isBlocksVisible());
          }
        }
      }
    });

    return toggleButton;
  }

  /**
   * Creates a button to toggle the static routes in the drawing.
   *
   * @param view The DrawingView the button will belong to.
   * @return The created button.
   */
  private AbstractButton toggleStaticRoutesButton(final OpenTCSDrawingView drawingView) {
    final JToggleButton toggleButton = new JToggleButton();
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    labels.configureToolBarButton(toggleButton, toggleStaticRoutesActionID);
    toggleButton.setMargin(new Insets(0, 0, 0, 0));
    toggleButton.setFocusable(false);

    toggleButton.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent event) {
        if (view.getDrawingView() == drawingView) {
          drawingView.setStaticRoutesVisible(toggleButton.isSelected());
          view.firePropertyChange(STATIC_ROUTES_VISIBLE_PROPERTY,
                                  !toggleButton.isSelected(),
                                  toggleButton.isSelected());
        }
      }
    });

    drawingView.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(OpenTCSDrawingView.STATIC_ROUTES_VISIBLE_PROPERTY)) {
          if (view.getDrawingView() == drawingView) {
            toggleButton.setSelected(view.getDrawingView().isStaticRoutesVisible());
          }
        }
      }
    });

    return toggleButton;
  }

  /**
   * Disable tools for editing and changing attributes in OPERATING mode.
   *
   * @param mode
   */
  public void setOperationMode(OperationMode mode) {
    menuItemNewModel.setEnabled(mode == OperationMode.MODELLING);
    menuItemLoadModel.setEnabled(mode == OperationMode.MODELLING);
    // Saving is also allowed in OPERATING mode, e.g. locking of Pathes
    menuItemSaveModel.setEnabled(true);
    menuItemSaveModelAs.setEnabled(true);

    menuItemFindVehicle.setEnabled(mode == OperationMode.OPERATING);
    menuItemShowVehicles.setEnabled(mode == OperationMode.OPERATING);
    cbiIgnorePrecisePosition.setEnabled(mode == OperationMode.OPERATING);
    cbiIgnoreOrientationAngle.setEnabled(mode == OperationMode.OPERATING);
    cbiAlignLayoutWithModel.setEnabled(mode == OperationMode.MODELLING);
    cbiAlignModelWithLayout.setEnabled(mode == OperationMode.MODELLING);
    menuOrderSequenceView.setEnabled(mode == OperationMode.OPERATING);
    menuTransportOrderView.setEnabled(mode == OperationMode.OPERATING);
    menuAddDrawingView.setEnabled(mode == OperationMode.OPERATING);
    menuAddBitmap.setEnabled(mode == OperationMode.MODELLING);

    menuEdit.setEnabled(mode == OperationMode.MODELLING);
    menuItemCreateTransportOrder.setEnabled(mode == OperationMode.OPERATING);
    menuViewToolBars.setEnabled(mode == OperationMode.MODELLING);

    toolBarManager.setOperationMode(mode);

    evaluatePluginPanels(mode);

    operationMode = mode;
  }

  public OperationMode getOperationMode() {
    return operationMode;
  }

  /**
   * Removes/adds plugin panels depending on the <code>OperationMode</code>.
   *
   * @param operationMode The operation mode.
   */
  private void evaluatePluginPanels(OperationMode operationMode) {
    Kernel.State equivalentState = null;
    switch (operationMode) {
      case MODELLING:
        equivalentState = Kernel.State.MODELLING;
        break;
      case OPERATING:
        equivalentState = Kernel.State.OPERATING;
        break;
      default:
        break;
    }

    if (equivalentState == null) {
      return;
    }

    menuPluginPanels.removeAll();

    for (final PanelFactory factory : panelRegistry.getFactories()) {
      if (kernelProxyManager.isConnected()
          && factory.providesPanel(kernelProxyManager.kernel().getState())) {
        String title = factory.getPanelDescription();
        final JCheckBoxMenuItem utilMenuItem = new JCheckBoxMenuItem();
        utilMenuItem.setAction(new AddPluginPanelAction(view, factory));
        utilMenuItem.setText(title);
        view.getDockingManager().addPropertyChangeListener(
            new PluginPanelPropertyHandler(utilMenuItem));
        menuPluginPanels.add(utilMenuItem);
      }
    }
    // If the menu is empty, add a single disabled menu item to it that explains
    // to the user that no plugin panels are available.
    if (menuPluginPanels.getMenuComponentCount() == 0) {
      final ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
      JMenuItem dummyItem = new JMenuItem(labels.getString("view.pluginPanels.noneAvailable.text"));
      dummyItem.setEnabled(false);
      menuPluginPanels.add(dummyItem);
    }
  }

  /**
   * A listener that affords dragging of the drawingView and single objects past
   * the bounds of the current view.
   */
  private class ViewDragScrollListener
      extends MouseAdapter
      implements MouseWheelListener {

    private final Cursor defaultCursor;
    private final Point startPoint = new Point();
    private JToggleButton selectionTool;
    private JToggleButton dragTool;
    private final OpenTCSDrawingView drawingView;

    /**
     * Creates a new ComponentDragScrollListener.
     */
    public ViewDragScrollListener(OpenTCSDrawingView drawingView) {
      this.drawingView = drawingView;
      defaultCursor = drawingView.getCursor();
    }

    @Override
    public void mouseDragged(final MouseEvent evt) {
      if (drawingView.vehicleDragged()) {
        drawingView.setCursor(Cursors.getDragVehicleCursor());
      }

      Container c = drawingView.getParent();

      if (c instanceof JViewport) {
        final JViewport viewport = (JViewport) drawingView.getParent();
        Point cp = SwingUtilities.convertPoint(drawingView,
                                               evt.getPoint(),
                                               viewport);
        int dx = startPoint.x - cp.x;
        int dy = startPoint.y - cp.y;
        Point vp = viewport.getViewPosition();
        vp.translate(dx, dy);

        if (dragTool.isSelected()) {
          final int oldWidth = drawingView.getWidth();
          final int oldHeight = drawingView.getHeight();
          drawingView.scrollRectToVisible(new Rectangle(vp, viewport.getSize()));
          drawingView.updateOffsetFigures(dx, dy);

          // only repaint the ruler if the bounds have changed
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              int newWidth = drawingView.getWidth();
              int newHeight = drawingView.getHeight();

              if (oldWidth != newWidth) {
                view.getHorizontalRuler().setPreferredWidth(drawingView.getWidth());
              }

              if (oldHeight != newHeight) {
                view.getVerticalRuler().setPreferredHeight(drawingView.getHeight());
              }
            }
          });
        }
        else {
          final int oldWidth = drawingView.getWidth();
          final int oldHeight = drawingView.getHeight();
          drawingView.scrollRectToVisible(new Rectangle(evt.getX(),
                                                        evt.getY(),
                                                        1,
                                                        1));
          Figure figure = drawingView.findFigure(evt.getPoint());

          if (figure != null) {
            drawingView.validateOffsets(figure);
          }
          // only repaint the ruler if the bounds have changed
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              int newWidth = drawingView.getWidth();
              int newHeight = drawingView.getHeight();

              if (oldWidth != newWidth) {
                view.getHorizontalRuler().setPreferredWidth(drawingView.getWidth());
              }

              if (oldHeight != newHeight) {
                view.getVerticalRuler().setPreferredHeight(drawingView.getHeight());
              }
            }
          });
        }

        view.setCurrentMousePoint(drawingView.viewToDrawing(evt.getPoint()));
        view.showPosition(true, false);
        startPoint.setLocation(cp);
        drawingView.revalidate();
        drawingView.repaint();
      }
    }

    @Override
    public void mousePressed(MouseEvent evt) {
      if (dragIsSelected()) {
        drawingView.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
      }

      Container c = drawingView.getParent();

      if (c instanceof JViewport) {
        JViewport viewPort = (JViewport) c;
        Point cp = SwingUtilities.convertPoint(drawingView, evt.getPoint(), viewPort);
        startPoint.setLocation(cp);
      }

      view.setCurrentMousePoint(drawingView.viewToDrawing(evt.getPoint()));
      view.setMouseStartPoint(drawingView.viewToDrawing(evt.getPoint()));
      view.showPosition(true, false);
    }

    @Override
    public void mouseReleased(MouseEvent evt) {
      if (!dragIsSelected()) {
        Figure fig = drawingView.findFigure(evt.getPoint());
        if (fig instanceof LabeledPointFigure) {
          drawingView.createPossibleTransportOrder(fig);
        }
        view.setMouseEndPoint(drawingView.viewToDrawing(evt.getPoint()));

        if (evt.getButton() != 2) {
          view.showPosition(true, true);
        }
        else {
          view.showPosition(true, false);
        }
      }
    }

    @Override
    public void mouseExited(MouseEvent evt) {
      dragIsSelected();
      view.showPosition(false, false);
    }

    @Override
    public void mouseEntered(MouseEvent evt) {
      dragIsSelected();
    }

    @Override
    public void mouseMoved(MouseEvent evt) {
      view.setCurrentMousePoint(drawingView.viewToDrawing(evt.getPoint()));
      view.showPosition(true, false);
    }

    @Override
    public void mouseClicked(MouseEvent evt) {
      if (evt.getButton() == 2) {
        if (dragTool.isSelected()) {
          selectionTool.setSelected(true);
        }
        else if (selectionTool.isSelected()) {
          dragTool.setSelected(true);
        }
        // Sets the correct cursor
        dragIsSelected();
      }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
      if (e.isControlDown()) {
        int zoomLevel = fZoomComboBox.getSelectedIndex();
        int notches = e.getWheelRotation();

        if (zoomLevel != -1) {
          if (notches < 0) {
            if (zoomLevel > 0) {
              zoomLevel--;
              fZoomComboBox.setSelectedIndex(zoomLevel);
            }
          }
          else {
            if (zoomLevel < fZoomComboBox.getItemCount() - 1) {
              zoomLevel++;
              fZoomComboBox.setSelectedIndex(zoomLevel);
            }
          }
        }
      }
    }

    /**
     * Checks whether the drag tool is selected.
     *
     * @return true if the drag tool is selected, false otherwise.
     */
    private boolean dragIsSelected() {
      if (selectionTool == null || dragTool == null) {
        selectionTool = toolBarManager.getSelectionToolButton();
        selectionTool.setSelected(true);
        dragTool = toolBarManager.getDragToolButton();
      }

      if (!selectionTool.isSelected() && dragTool.isSelected()) {
        drawingView.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return true;
      }
      else if (selectionTool.isSelected() && !dragTool.isSelected()) {
        drawingView.setCursor(defaultCursor);
        return false;
      }
      else {
        return false;
      }
    }
  }
}
