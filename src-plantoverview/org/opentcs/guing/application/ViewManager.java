package org.opentcs.guing.application;

import bibliothek.gui.Dockable;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.intern.DefaultCommonDockable;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.BorderFactory;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import org.jhotdraw.gui.PlacardScrollPaneLayout;
import org.opentcs.guing.components.dockable.CStackDockStation;
import org.opentcs.guing.components.dockable.DockableTitleComparator;
import org.opentcs.guing.components.dockable.DockingManager;
import org.opentcs.guing.components.drawing.OpenTCSDockableUtil;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.components.drawing.OpenTCSDrawingView;
import org.opentcs.guing.components.drawing.Ruler;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.GroupModel;
import org.opentcs.guing.transport.OrderSequencesContainerPanel;
import org.opentcs.guing.transport.TransportOrdersContainerPanel;

/**
 * Manages the mapping of dockables to drawing views, transport order views and
 * order sequence views.
 * 
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class ViewManager {

  /**
   * Map for Dockable -> DrawingView + Rulers.
   */
  private final Map<DefaultSingleCDockable, OpenTCSDockableUtil> drawingViewMap;
  /**
   * Map for transport order dockable -> transport order container panel.
   */
  private final Map<DefaultSingleCDockable, TransportOrdersContainerPanel> transportOrderViews;
  /**
   * Map for order sequences dockable -> order sequences container panel.
   */
  private final Map<DefaultSingleCDockable, OrderSequencesContainerPanel> orderSequenceViews;
  /**
   * A factory for drawing views.
   */
  private final Provider<OpenTCSDrawingView> drawingViewProvider;
  /**
   * Depending on the type of an application, there may be one editor per view,
   * or a single shared editor for all views.
   */
  private final OpenTCSDrawingEditor fDrawingEditor;
  /**
   * The default modelling dockable.
   */
  private DefaultSingleCDockable drawingViewModellingDockable;
  
  /**
   * Creates a new instance.
   *
   * @param fDrawingEditor The drawing editor to be used.
   * @param drawingViewProvider A provider for creating drawing views on demand.
   */
  @Inject
  public ViewManager(OpenTCSDrawingEditor fDrawingEditor,
                     Provider<OpenTCSDrawingView> drawingViewProvider) {
    drawingViewMap = new TreeMap<>(new DockableTitleComparator());
    transportOrderViews = new TreeMap<>(new DockableTitleComparator());
    orderSequenceViews = new TreeMap<>(new DockableTitleComparator());
    this.fDrawingEditor = requireNonNull(fDrawingEditor, "fDrawingEditor");
    this.drawingViewProvider = requireNonNull(drawingViewProvider,
                                              "drawingViewProvider");
    
    for (OpenTCSDockableUtil util : drawingViewMap.values()) {
      fDrawingEditor.add(util.getDrawingView());
    }
  }

  /**
   * Resets all components.
   */
  public void reset() {
    drawingViewModellingDockable = null;
    drawingViewMap.clear();
    transportOrderViews.clear();
    orderSequenceViews.clear();
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
    for (DefaultSingleCDockable dock : drawingViewMap.keySet()) {
      if (dock.getTitleText().equals(title)) {
        drawingViewMap.get(dock).getDrawingView().setGroupVisible(sf.getChildComponents(), visible);
      }
    }
  }
  
  public Map<DefaultSingleCDockable, OpenTCSDockableUtil> getDrawingViewMap() {
    return drawingViewMap;
  }
  
  public Map<DefaultSingleCDockable, TransportOrdersContainerPanel> getTransportOrderMap() {
    return transportOrderViews;
  }
  
  public Map<DefaultSingleCDockable, OrderSequencesContainerPanel> getOrderSequenceMap() {
    return orderSequenceViews;
  }

  /**
   * Sets visibility states of all dockables to modelling.
   *
   * @param dockingManager The DockingManager.
   */
  public void setKernelStateModelling(DockingManager dockingManager) {
    CStackDockStation station
        = dockingManager.getTabPane(DockingManager.COURSE_TAB_PANE_ID).getStation();
    List<DefaultSingleCDockable> drawingViews = new ArrayList<>(drawingViewMap.keySet());
    for (int i = 0; i < drawingViews.size(); i++) {
      DefaultSingleCDockable dock = drawingViews.get(i);
      if (dock != drawingViewModellingDockable) {
        // Setting it to closeable = false, so the ClosingListener
        // doesn't remove the dockable when it's closed
        dock.setCloseable(false);
        dockingManager.setDockableVisibility(dock.getUniqueId(), false);
      }
    }
    
    for (DefaultSingleCDockable dock : new ArrayList<>(transportOrderViews.keySet())) {
      dockingManager.hideDockable(station, dock);
      transportOrderViews.get(dock).clearTransportOrders();
    }
    
    for (DefaultSingleCDockable dock : new ArrayList<>(orderSequenceViews.keySet())) {
      dockingManager.hideDockable(station, dock);
      orderSequenceViews.get(dock).clearOrderSequences();
    }
    
    dockingManager.setDockableVisibility(DockingManager.VEHICLES_DOCKABLE_ID, false);
    dockingManager.showDockable(station, drawingViewModellingDockable, 0);
    drawingViewMap.get(drawingViewModellingDockable).getDrawingView().handleFocusGained();
  }

  /**
   * Sets visibility states of all dockables to operating.
   *
   * @param dockingManager The DockingManager.
   */
  public void setKernelStateOperating(DockingManager dockingManager) {
    CStackDockStation station
        = dockingManager.getTabPane(DockingManager.COURSE_TAB_PANE_ID).getStation();
    Dockable frontDock = station.getFrontDockable();
    dockingManager.hideDockable(station, drawingViewModellingDockable);
    int i = 0;
    for (DefaultSingleCDockable dock : new ArrayList<>(drawingViewMap.keySet())) {
      if (dock != drawingViewModellingDockable) {
        // Restore to default
        dock.setCloseable(true);
        dockingManager.showDockable(station, dock, i);
        i++;
      }
    }
    i = drawingViewMap.size();
    for (DefaultSingleCDockable dock : new ArrayList<>(drawingViewMap.keySet())) {
      // OpenTCSDrawingViews can be undocked when switching states, so
      // we make sure they aren't counted as docked
      if (dockingManager.isDockableDocked(station, dock)) {
        i--;
      }
    }
    
    int dockedDrawingViews = i;
    
    for (DefaultSingleCDockable dock : new ArrayList<>(transportOrderViews.keySet())) {
      dockingManager.showDockable(station, dock, i);
      i++;
    }
    
    i = dockedDrawingViews + transportOrderViews.size();
    
    for (DefaultSingleCDockable dock : new ArrayList<>(orderSequenceViews.keySet())) {
      dockingManager.showDockable(station, dock, i);
      i++;
    }
    
    if (frontDock != null && frontDock.isDockableShowing()) {
      station.setFrontDockable(frontDock);
    }
    else {
      station.setFrontDockable(station.getDockable(0));
    }
    dockingManager.setDockableVisibility(DockingManager.VEHICLES_DOCKABLE_ID, true);
  }

  /**
   * Returns all drawing views (excluding the modelling view)
   *
   * @return List with all known <code>OpenTCSDrawingViews</code>, but not
   * the modelling view.
   */
  public List<OpenTCSDrawingView> getOperatingDrawingViews() {
    List<OpenTCSDrawingView> views = new ArrayList<>();
    
    for (OpenTCSDockableUtil util : drawingViewMap.values()) {
      if (drawingViewMap.get(drawingViewModellingDockable) != util) {
        views.add(util.getDrawingView());
      }
    }
    
    return views;
  }
  
  /**
   * Returns the title texts of all drawing views.
   * 
   * @return List of strings containing the names.
   */
  public List<String> getDrawingViewNames() {
    List<String> names = new ArrayList<>();
    for (DefaultSingleCDockable dock : drawingViewMap.keySet()) {
      if (dock != drawingViewModellingDockable) {
        names.add(dock.getTitleText());
      }
    }
    Collections.sort(names);
    
    return names;
  }
  
  /**
   * Initializes the unique modelling dockable.
   * 
   * @param dockable The dockable that will be the modelling dockable.
   * @param title The title of this dockable.
   */
  public void initModellingDockable(DefaultSingleCDockable dockable, String title) {
    drawingViewModellingDockable = Objects.requireNonNull(dockable, "dockable is null");
    drawingViewModellingDockable.setTitleText(Objects.requireNonNull(title, "title is null"));
    drawingViewModellingDockable.setCloseable(false);
  }
  
  public void setBitmapToModellingView(File file) {
    drawingViewMap.get(drawingViewModellingDockable).getDrawingView().addBackgroundBitmap(file);
  }
  
  public int getNextDrawingViewIndex() {
    return (drawingViewMap.isEmpty() && drawingViewModellingDockable == null)
        ? 0 : nextAvailableIndex(drawingViewMap.keySet());
  }
  
  public int getNextTransportOrderViewIndex() {
    return nextAvailableIndex(transportOrderViews.keySet());
  }
  
  public int getNextOrderSequenceViewIndex() {
    return nextAvailableIndex(orderSequenceViews.keySet());
  }
  
  public DefaultSingleCDockable getLastTransportOrderView() {
    int biggestIndex = getNextTransportOrderViewIndex();
    DefaultSingleCDockable lastTOView = null;
    Iterator<DefaultSingleCDockable> tranportOrderViewIterator
        = transportOrderViews.keySet().iterator();
    for (int i = 0; i < biggestIndex; i++) {
      if (tranportOrderViewIterator.hasNext()) {
        lastTOView = tranportOrderViewIterator.next();
      }
    }
    
    return lastTOView;
  }
  
  public DefaultSingleCDockable getLastOrderSequenceView() {
    int biggestIndex = getNextOrderSequenceViewIndex();
    DefaultSingleCDockable lastOSView = null;
    Iterator<DefaultSingleCDockable> orderSequencesViewIterator
        = orderSequenceViews.keySet().iterator();
    for (int i = 0; i < biggestIndex; i++) {
      if (orderSequencesViewIterator.hasNext()) {
        lastOSView = orderSequencesViewIterator.next();
      }
    }
    
    return lastOSView;
  }

  /**
   * Creates a new <code>OpenTCSDrawingView</code>, wraps it in a
   * <code>JScrollPane</code> and return this.
   *
   * @param fSystemModel The SystemModel.
   * @return A ScrollPane containing a new drawing view.
   */
  public JScrollPane getNewDrawingView(SystemModel fSystemModel) {
    Objects.requireNonNull(fSystemModel, "fSystemModel is null");
    
    OpenTCSDrawingView newDrawingView = drawingViewProvider.get();
    fDrawingEditor.add(newDrawingView);
    fDrawingEditor.setActiveView(newDrawingView);
    newDrawingView.setBlocks(fSystemModel.getMainFolder(SystemModel.BLOCKS));
    newDrawingView.setStaticRoutes(fSystemModel.getMainFolder(SystemModel.STATIC_ROUTES));
    newDrawingView.setVehicles(fSystemModel);
    JScrollPane newScrollPane = new JScrollPane();
    newScrollPane.setViewport(new JViewport());
    newScrollPane.getViewport().setView(newDrawingView);
    // Ruler
    newScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    newScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    newScrollPane.setViewportBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
    newScrollPane.setHorizontalScrollBar(new PlacardScrollbar());
    newScrollPane.setLayout(new PlacardScrollPaneLayout());
    newScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
    
    Ruler newHorizontalRuler = new Ruler(Ruler.Orientation.HORIZONTAL, newDrawingView);
    newDrawingView.addPropertyChangeListener(newHorizontalRuler);
    newHorizontalRuler.setPreferredWidth(newDrawingView.getWidth());
    Ruler newVerticalRuler = new Ruler(Ruler.Orientation.VERTICAL, newDrawingView);
    newDrawingView.addPropertyChangeListener(newVerticalRuler);
    newVerticalRuler.setPreferredHeight(newDrawingView.getHeight());
    newScrollPane.setColumnHeaderView(newHorizontalRuler);
    newScrollPane.setRowHeaderView(newVerticalRuler);
    
    return newScrollPane;
  }

  /**
   * Returns the horizontal ruler of the active view.
   *
   * @return A Ruler.
   */
  public Ruler getHorizontalRuler() {
    OpenTCSDrawingView activeView = fDrawingEditor.getActiveView();
    
    for (OpenTCSDockableUtil util : drawingViewMap.values()) {
      if (activeView == util.getDrawingView()) {
        return util.getHorizontalRuler();
      }
    }
    
    return null;
  }

  /**
   * Returns the vertical ruler of the active view.
   *
   * @return A Ruler.
   */
  public Ruler getVerticalRuler() {
    OpenTCSDrawingView activeView = fDrawingEditor.getActiveView();
    
    for (OpenTCSDockableUtil util : drawingViewMap.values()) {
      if (activeView == util.getDrawingView()) {
        return util.getVerticalRuler();
      }
    }
    
    return null;
  }

  /**
   * Puts a scroll pane with a key dockable into the drawing view map.
   * The scroll pane has to contain the drawing view and both rulers.
   *
   * @param dockable The dockable the scrollPane is wrapped into. Used as the key.
   * @param scrollPane The scroll pane containing the drawing view and rulers.
   * @return The extracted <code>OpenTCSDrawingView</code>.
   */
  public OpenTCSDrawingView putDrawingView(DefaultSingleCDockable dockable,
                                           JScrollPane scrollPane) {
    Objects.requireNonNull(dockable, "dockable is null");
    Objects.requireNonNull(scrollPane, "scrollPane is null");
    if (!(scrollPane.getViewport().getView() instanceof OpenTCSDrawingView)
        || !(scrollPane.getColumnHeader().getView() instanceof Ruler)
        || !(scrollPane.getRowHeader().getView() instanceof Ruler)) {
      return null;
    }
    OpenTCSDrawingView drawingView = (OpenTCSDrawingView) scrollPane.getViewport().getView();
    Ruler horizontalRuler = (Ruler) scrollPane.getColumnHeader().getView();
    Ruler verticalRuler = (Ruler) scrollPane.getRowHeader().getView();
    drawingViewMap.put(dockable, new OpenTCSDockableUtil(drawingView, horizontalRuler, verticalRuler));
    
    return drawingView;
  }

  /**
   * Puts a <code>TransportOrdersContainerPanel</code> with a key dockable
   * into the transport order view map.
   *
   * @param dockable The dockable the panel is wrapped into. Used as the key.
   * @param panel The panel.
   */
  public void putTransportOrderView(DefaultSingleCDockable dockable,
                                    TransportOrdersContainerPanel panel) {
    transportOrderViews.put(dockable, panel);
  }

  /**
   * Puts a <code>OrderSequencesContainerPanel</code> with a key dockable
   * into the order sequence view map.
   *
   * @param dockable The dockable the panel is wrapped into. Used as the key.
   * @param panel The panel.
   */
  public void putOrderSequenceView(DefaultSingleCDockable dockable,
                                   OrderSequencesContainerPanel panel) {
    orderSequenceViews.put(dockable, panel);
  }

  /**
   * Evaluates which dockable should be the front dockable.
   *
   * @return The dockable that should be the front dockable. <code>null</code>
   * if no dockables exist.
   */
  public DefaultCommonDockable evaluateFrontDockable() {
    if (!drawingViewMap.isEmpty()) {
      return drawingViewMap.keySet().iterator().next().intern();
    }
    if (!transportOrderViews.isEmpty()) {
      return transportOrderViews.keySet().iterator().next().intern();
    }
    if (!orderSequenceViews.isEmpty()) {
      return orderSequenceViews.keySet().iterator().next().intern();
    }
    return null;
  }

  /**
   * Returns the next available index of a set of dockables.
   * E.g. if "Dock 0" and "Dock 2" are being used, 1 would be returned.
   *
   * @param setToIterate The set to iterate.
   * @return The next available index.
   */
  private int nextAvailableIndex(Set<DefaultSingleCDockable> setToIterate) {
    // Name
    Pattern p = Pattern.compile("\\d");
    Matcher m;
    int biggestIndex = 0;
    
    for (DefaultSingleCDockable dock : setToIterate) {
      m = p.matcher(dock.getTitleText());
      
      if (m.find()) {
        int index = Integer.parseInt(m.group(0));
        
        if (index > biggestIndex) {
          biggestIndex = index;
        }
      }
    }
    
    return biggestIndex + 1;
  }
  
  private class PlacardScrollbar
      extends JScrollBar {
    
    public PlacardScrollbar() {
      super(JScrollBar.HORIZONTAL);
      setPreferredSize(new Dimension(100, 18));
    }
  }
}
