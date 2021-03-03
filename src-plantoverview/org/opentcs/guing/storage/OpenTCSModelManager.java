/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.storage;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.Figure;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.ObjectPropConstants;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Group;
import org.opentcs.data.model.Layout;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Location.Link;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.StaticRoute;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.LocationRepresentation;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.guing.application.OpenTCSView;
import org.opentcs.guing.application.StatusPanel;
import org.opentcs.guing.components.dialogs.StandardDialog;
import org.opentcs.guing.components.drawing.course.Origin;
import org.opentcs.guing.components.drawing.figures.FigureConstants;
import org.opentcs.guing.components.drawing.figures.LabeledLocationFigure;
import org.opentcs.guing.components.drawing.figures.LabeledPointFigure;
import org.opentcs.guing.components.drawing.figures.LinkConnection;
import org.opentcs.guing.components.drawing.figures.LocationFigure;
import org.opentcs.guing.components.drawing.figures.PathConnection;
import org.opentcs.guing.components.drawing.figures.PointFigure;
import org.opentcs.guing.components.drawing.figures.TCSLabelFigure;
import org.opentcs.guing.components.properties.event.NullAttributesChangeListener;
import org.opentcs.guing.components.properties.type.ColorProperty;
import org.opentcs.guing.components.properties.type.KeyValueProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.properties.type.StringSetProperty;
import org.opentcs.guing.components.properties.type.SymbolProperty;
import org.opentcs.guing.exchange.EventDispatcher;
import org.opentcs.guing.exchange.KernelProxyManager;
import org.opentcs.guing.exchange.OpenTCSEventDispatcher;
import org.opentcs.guing.exchange.adapter.LayoutAdapter;
import org.opentcs.guing.exchange.adapter.LinkAdapter;
import org.opentcs.guing.exchange.adapter.LocationAdapter;
import org.opentcs.guing.exchange.adapter.OpenTCSProcessAdapter;
import org.opentcs.guing.exchange.adapter.PathAdapter;
import org.opentcs.guing.exchange.adapter.PointAdapter;
import org.opentcs.guing.exchange.adapter.ProcessAdapter;
import org.opentcs.guing.exchange.adapter.ProcessAdapterFactory;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SystemModel;
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
import org.opentcs.guing.util.Colors;
import org.opentcs.guing.util.CourseObjectFactory;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.util.ObjectListCycler;

/**
 * Speichert die Fahrkursmodelle über die Leitsteuerung und damit auf dem
 * Server.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class OpenTCSModelManager {

  /**
   * Identifier für das Layout-Objekt, welches das standardmäßige Fahrkursmodell
   * enthält.
   */
  public static final String DEFAULT_LAYOUT = "Default";
  /**
   * This class's logger.
   */
  private static final Logger log
      = Logger.getLogger(OpenTCSModelManager.class.getName());
  /**
   * The proxy/connection manager to be used.
   */
  private final KernelProxyManager kernelProxyManager;
  /**
   * A panel for status messages to be used.
   */
  private final StatusPanel statusPanel;
  /**
   * A course object factory to be used.
   */
  private final CourseObjectFactory crsObjFactory;
  /**
   * The process adapter factory to be used.
   */
  private final ProcessAdapterFactory procAdapterFactory;
  /**
   * Der Name des aktuellen Fahrkursmodells. Wurde durch den Benutzer noch kein
   * Name angegeben, so ist dieser
   * <code>null</code>.
   */
  private String fModelName;
  /**
   * Provides new instances of SystemModel.
   */
  private final Provider<SystemModel> systemModelProvider;

  /**
   * Creates a new instance.
   *
   * @param kernelProxyManager The The proxy/connection manager to be used.
   * @param statusPanel A panel for status messages.
   * @param crsObjFactory A course object factory to be used.
   * @param procAdapterFactory The process adapter factory to be used.
   * @param systemModelProvider Provides an instance of SystemModel.
   */
  @Inject
  public OpenTCSModelManager(KernelProxyManager kernelProxyManager,
                             StatusPanel statusPanel,
                             CourseObjectFactory crsObjFactory,
                             ProcessAdapterFactory procAdapterFactory,
                             Provider<SystemModel> systemModelProvider) {
    this.kernelProxyManager = requireNonNull(kernelProxyManager,
                                             "kernelProxyManager");
    this.statusPanel = requireNonNull(statusPanel, "statusPanel");
    this.crsObjFactory = requireNonNull(crsObjFactory, "crsObjFactory");
    this.procAdapterFactory = requireNonNull(procAdapterFactory,
                                             "procAdapterFactory");
    this.systemModelProvider = requireNonNull(systemModelProvider, "systemModelProvider");
  }

  /**
   * Shows a dialog to select a model.
   *
   * @return True, if the user pressed "OK", false otherwise.
   */
  public boolean selectModel() {
    return showOpenDialog(createDialogContent());
  }

  /**
   * Lädt ein zuvor gespeichertes Fahrkursmodell ohne vorher einen Auswahldialog
   * anzuzeigen.
   *
   * @return
   */
  public boolean loadModel() {
    try {
      // Neues Modell laden, Name steht in fModelName
      kernel().loadModel(fModelName);	// Darf nur in MODELLING Mode aufgerufen werden!
      return true;
    }
    catch (CredentialsException | IOException e) {
      JOptionPane.showMessageDialog(OpenTCSView.instance(),
                                    "Could not load kernel model " + fModelName,
                                    "Could not load kernel model",
                                    JOptionPane.ERROR_MESSAGE);
      log.log(Level.SEVERE, "Exception loading model " + fModelName, e);
      getModelName();	// Modell-Namen aktualisieren
    }

    return false;
  }

  /**
   * Saves the current driving course model.
   *
   * @param chooseName If <code>true</code>, this method shows a dialog to enter
   * a name for the model.
   * @return Whether the model was actually saved.
   */
  public boolean saveModel(boolean chooseName) {
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    String name = getModelName();

    try {
      if (chooseName
          || name == null
          || name.isEmpty()
          || name.equals(Kernel.DEFAULT_MODEL_NAME)
          || name.equals(labels.getString("file.newModel.text"))) {
        showSaveDialog(createDialogContent());
      }

      return save();
    }
    catch (CredentialsException e) {
      log.log(Level.WARNING, "", e);
    }

    return false;
  }

  /**
   * Liefert den Namen des aktuellen Fahrkursmodells.
   *
   * @return
   */
  public String getModelName() {
    fModelName = kernel().getCurrentModelName();

    return fModelName;
  }

  /**
   * Lädt alle Objekte des aktuellen Kernel-Modells und erzeugt die zugehörigen
   * Figuren für die Visualisierung/Modellierung.
   *
   * @return
   */
  public SystemModel restoreModel() {
    fModelName = kernel().getCurrentModelName();

    if (fModelName.isEmpty()) {
      return null;
    }

    SystemModel systemModel = systemModelProvider.get();
    systemModel.setEventDispatcher(createEventDispatcher());

    // Die im Kernel gespeicherten Layouts
    Set<VisualLayout> allVisualLayouts = kernel().getTCSObjects(VisualLayout.class);
    Set<Vehicle> allVehicles = kernel().getTCSObjects(Vehicle.class);
    Set<Point> allPoints = kernel().getTCSObjects(Point.class);
    Set<LocationType> allLocationTypes = kernel().getTCSObjects(LocationType.class);
    Set<Location> allLocations = kernel().getTCSObjects(Location.class);
    Set<Path> allPaths = kernel().getTCSObjects(Path.class);
    Set<Block> allBlocks = kernel().getTCSObjects(Block.class);
    Set<StaticRoute> allStaticRoutes = kernel().getTCSObjects(StaticRoute.class);
    Set<Group> allGroups = kernel().getTCSObjects(Group.class);

    // Wenn das Modell (noch) kein VisualLayout enthält, das Layout aus der
    // Datei "layout_Default.xml" laden ("altes" Modell)
    try {
      Layout layout = getLayout(DEFAULT_LAYOUT);

      if (layout != null) {
        byte[] data = layout.getData();

        if (data.length > 0) {
          if (kernel().getState() != Kernel.State.MODELLING) {
            JOptionPane.showMessageDialog(null, ResourceBundleUtil.getBundle().getString("oldModel.wrongState"));
            System.exit(0);
          }

          XMLStorageFormat storageFormat = new XMLStorageFormat(crsObjFactory);
          storageFormat.restore(new ByteArrayInputStream(data), systemModel, this);
          return systemModel;
        }
      }
    }
    catch (Exception ex) {
      log.log(Level.SEVERE, "Error in restoreModel():\n", ex);
      return null;
    }

    Set<OpenTCSProcessAdapter> createdAdapters = new HashSet<>();
    List<Figure> restoredFigures = new ArrayList<>();

    double scaleX = Origin.DEFAULT_SCALE;
    double scaleY = Origin.DEFAULT_SCALE;
    Origin origin = systemModel.getDrawingMethod().getOrigin();

    // "Neues" Modell: Layout ist im Visual-Layout Objekt gespeichert
    VisualLayout visualLayout = null;
    for (VisualLayout visLayout : allVisualLayouts) {
      visualLayout = visLayout;	// Es sollte genau ein Layout geben
      systemModel.createLayoutMap(visualLayout, allPoints, allPaths, allLocations, allBlocks);
      scaleX = visualLayout.getScaleX();
      scaleY = visualLayout.getScaleY();

      if (scaleX != 0.0 && scaleY != 0.0) {
        origin.setScale(scaleX, scaleY);
      }
    }

    ModelComponent layoutComponent = systemModel.getMainFolder(SystemModel.LAYOUT);
    // Leeres Modell: Default-Layout erzeugen
    OpenTCSProcessAdapter adapter;
    if (visualLayout == null) {
      StringProperty name = (StringProperty) layoutComponent.getProperty(LayoutModel.NAME);
      name.setText("VLayout");

      try {
        LengthProperty scale = (LengthProperty) layoutComponent.getProperty(LayoutModel.SCALE_X);
        scale.setValueAndUnit(scaleX, LengthProperty.Unit.MM);
        scale = (LengthProperty) layoutComponent.getProperty(LayoutModel.SCALE_Y);
        scale.setValueAndUnit(scaleY, LengthProperty.Unit.MM);
      }
      catch (IllegalArgumentException ex) {
        log.log(Level.WARNING, "Exception in setValueAndUnit():\n{0}", ex);
      }

      adapter = createLayoutAdapter(systemModel, layoutComponent);
      adapter.setInTransition(true);
      createdAdapters.add(adapter);
    }
    else {
      // TODO: Bei mehreren Layouts muss jedem SystemFolder der Name des Layouts zugewiesen werden
      adapter = addProcessAdapter(systemModel, visualLayout.getReference(), layoutComponent);
      adapter.updateModelProperties();
    }

    restoreModelPoints(allPoints, systemModel, origin, scaleX, scaleY, createdAdapters, restoredFigures);
    restoreModelPaths(allPaths, systemModel, origin, createdAdapters, restoredFigures);
    restoreModelVehicles(allVehicles, systemModel, createdAdapters);
    restoreModelLocationTypes(allLocationTypes, systemModel, createdAdapters);
    restoreModelLocations(allLocations, systemModel, origin, scaleX, scaleY, createdAdapters, restoredFigures);
    restoreModelBlocks(allBlocks, systemModel, createdAdapters);
    restoreModelStaticRoutes(allStaticRoutes, systemModel, createdAdapters);
    restoreModelGroups(allGroups, systemModel, createdAdapters);

    // --- Groups --- 
    Drawing drawing = systemModel.getDrawing();

    for (Figure figure : restoredFigures) {
      drawing.add(figure);
    }

    // Allen Adaptern mitteilen, dass die Transition beendet ist
    for (OpenTCSProcessAdapter currentAdapter : createdAdapters) {
      currentAdapter.setInTransition(false);
    }

    return systemModel;
  }

  private void restoreModelGroups(Set<Group> allGroups, SystemModel systemModel,
                                  Set<OpenTCSProcessAdapter> createdAdapters) {
    // --- Groups ---
    for (Group group : allGroups) {
      GroupModel groupModel = new GroupModel(group.getName());
      OpenTCSProcessAdapter adapter = addProcessAdapter(systemModel,
                                                        group.getReference(),
                                                        groupModel);
      adapter.setInTransition(true);
      createdAdapters.add(adapter);
      adapter.updateModelProperties();

      groupModel.createUserObject();
      systemModel.getMainFolder(SystemModel.GROUPS).add(groupModel);
      Set<TCSObjectReference<?>> refs = group.getMembers();

      for (TCSObjectReference ref : refs) {
        if (ref.getReferentClass() == Point.class) {
          Point point = kernel().getTCSObject(Point.class, ref);

          if (point != null) {
            groupModel.add(systemModel.getPointModel(point.getName()));
          }
        }
        else if (ref.getReferentClass() == Location.class) {
          Location location = kernel().getTCSObject(Location.class, ref);

          if (location != null) {
            groupModel.add(systemModel.getLocationModel(location.getName()));
          }
        }
        else if (ref.getReferentClass() == Path.class) {
          Path path = kernel().getTCSObject(Path.class, ref);

          if (path != null) {
            groupModel.add(systemModel.getPathModel(path.getName()));
          }
        }
      }
    }
  }

  private void restoreModelStaticRoutes(Set<StaticRoute> allStaticRoutes,
                                        SystemModel systemModel,
                                        Set<OpenTCSProcessAdapter> createdAdapters) {
    // --- Static Routes ---
    ObjectListCycler<Color> routeColorCycler
        = new ObjectListCycler<>(Colors.defaultColors());
    for (StaticRoute staticRoute : allStaticRoutes) {
      StaticRouteModel staticRouteModel = crsObjFactory.createStaticRouteModel();
      OpenTCSProcessAdapter adapter = addProcessAdapter(systemModel, staticRoute.getReference(), staticRouteModel);
      adapter.setInTransition(true);
      createdAdapters.add(adapter);
      adapter.updateModelProperties();

      // Neue Farbe suchen für StaticRoutes, die min. 1 Hop haben
      if (!staticRoute.getHops().isEmpty()) {
        ((ColorProperty) staticRouteModel
         .getProperty(ElementPropKeys.BLOCK_COLOR))
            .setColor(routeColorCycler.next());
      }
      // Das zugehörige Model Layout Element suchen
      ModelLayoutElement element = systemModel.getLayoutMap().get(staticRoute.getReference());

      if (element != null) {
        Map<String, String> properties = element.getProperties();
        // Im Layout Element gespeicherte Farbe überschreibt den Default-Wert
        String sColor = properties.get(ElementPropKeys.BLOCK_COLOR);
        String srgb = sColor.substring(1);	// delete trailing "#"
        int rgb = Integer.parseInt(srgb, 16);
        Color color = new Color(rgb);
        ColorProperty cp = (ColorProperty) staticRouteModel.getProperty(ElementPropKeys.BLOCK_COLOR);
        cp.setColor(color);
      }

      systemModel.getMainFolder(SystemModel.STATIC_ROUTES).add(staticRouteModel);
    }
  }

  private void restoreModelBlocks(Set<Block> allBlocks, SystemModel systemModel,
                                  Set<OpenTCSProcessAdapter> createdAdapters) {
    // --- Alle Blocks, die der Kernel kennt ---
    ObjectListCycler<Color> blockColorCycler
        = new ObjectListCycler<>(Colors.defaultColors());
    for (Block block : allBlocks) {
      BlockModel blockModel = crsObjFactory.createBlockModel();
      OpenTCSProcessAdapter adapter = addProcessAdapter(systemModel, block.getReference(), blockModel);
      adapter.setInTransition(true);
      createdAdapters.add(adapter);
      adapter.updateModelProperties();

//			Iterator iMembers = block.getMembers().iterator();
//
//			while (iMembers.hasNext()) {
//				ModelComponent modelComponent = getModelComponent(systemModel, (TCSObjectReference) iMembers.next());
//				blockModel.addCourseElement(modelComponent);
//			}
      // Neue Farbe suchen für Blocks, die mindestens ein Member haben
      if (!block.getMembers().isEmpty()) {
        ((ColorProperty) blockModel
         .getProperty(ElementPropKeys.BLOCK_COLOR))
            .setColor(blockColorCycler.next());
      }
      // Das zugehörige Model Layout Element suchen
      ModelLayoutElement element = systemModel.getLayoutMap().get(block.getReference());

      if (element != null) {
        Map<String, String> properties = element.getProperties();
        // Im Layout Element gespeicherte Farbe überschreibt den Default-Wert
        String sColor = properties.get(ElementPropKeys.BLOCK_COLOR);
        String srgb = sColor.substring(1);	// delete trailing "#"
        Color color = new Color(Integer.parseInt(srgb, 16));
        ((ColorProperty) blockModel.getProperty(ElementPropKeys.BLOCK_COLOR))
            .setColor(color);
      }

      systemModel.getMainFolder(SystemModel.BLOCKS).add(blockModel);
    }
  }

  private void restoreModelLocations(Set<Location> allLocations,
                                     SystemModel systemModel,
                                     Origin origin, double scaleX, double scaleY,
                                     Set<OpenTCSProcessAdapter> createdAdapters,
                                     List<Figure> restoredFigures) {
    // --- Alle Locations, die der Kernel kennt ---
    for (Location location : allLocations) {
      // Neues Figure-Objekt
      LocationFigure locationFigure = crsObjFactory.createLocationFigure();
      // Das zugehörige Modell
      LocationModel locationModel = locationFigure.getModel();
      // Adapter zur Verknüpfung des Kernel-Objekts mit der Figur
      OpenTCSProcessAdapter adapter = addProcessAdapter(systemModel, location.getReference(), locationModel);
      adapter.setInTransition(true);
      createdAdapters.add(adapter);
      // Das zugehörige Model Layout Element suchen und mit dem Adapter verknüpfen
      ModelLayoutElement layoutElement = systemModel.getLayoutMap().get(location.getReference());

      if (layoutElement != null) {
        ((LocationAdapter) adapter).setLayoutElement(layoutElement);
      }
      // Setze Typ, Koordinaten, ... aus dem Kernel-Modell
      adapter.updateModelProperties();
      // Default-Position für den Fall, dass kein Layout-Element zu dieser Location gefunden wurde
      double figurePositionX = location.getPosition().getX();
      double figurePositionY = location.getPosition().getY();
      // Die zugehörige Beschriftung:
      TCSLabelFigure label = new TCSLabelFigure(location.getName());

      Point2D.Double labelPosition;
      if (layoutElement != null) {
        Map<String, String> layoutProperties = layoutElement.getProperties();
        String locPosX = layoutProperties.get(ElementPropKeys.LOC_POS_X);
        String locPosY = layoutProperties.get(ElementPropKeys.LOC_POS_Y);
        // Die in den Properties gespeicherte Position überschreibt die im Kernel-Objekt gespeicherten Werte
        // TO DO: Auswahl, z.B. über Parameter?
        if (locPosX != null && locPosY != null) {
          try {
            figurePositionX = Integer.parseInt(locPosX);
            figurePositionY = Integer.parseInt(locPosY);
          }
          catch (NumberFormatException ex) {
            figurePositionX = figurePositionY = 0;
          }
        }

        String labelOffsetX = layoutProperties.get(ElementPropKeys.LOC_LABEL_OFFSET_X);
        String labelOffsetY = layoutProperties.get(ElementPropKeys.LOC_LABEL_OFFSET_Y);
        // TODO: labelOrientationAngle auswerten
//			String labelOrientationAngle = properties.get(ElementPropKeys.LOC_LABEL_ORIENTATION_ANGLE);

        double labelPositionX;
        double labelPositionY;
        if (labelOffsetX != null && labelOffsetY != null) {
          try {
            labelPositionX = Integer.parseInt(labelOffsetX);
            labelPositionY = Integer.parseInt(labelOffsetY);
          }
          catch (NumberFormatException ex) {
            labelPositionX = labelPositionY = -20;
          }

          labelPosition = new Point2D.Double(labelPositionX, labelPositionY);
          label.setOffset(labelPosition);
        }
      }
      // Figur auf diese Position verschieben
      Point2D.Double figurePosition = new Point2D.Double(figurePositionX / scaleX, -figurePositionY / scaleY);	// Vorzeichen!
      locationFigure.setBounds(figurePosition, figurePosition);

      LabeledLocationFigure llf = new LabeledLocationFigure(locationFigure);
      labelPosition = locationFigure.getStartPoint();
      labelPosition.x += label.getOffset().x;
      labelPosition.y += label.getOffset().y;
      label.setBounds(labelPosition, labelPosition);
      llf.setLabel(label);

      locationModel.setFigure(llf);
      locationModel.addAttributesChangeListener(llf);
      systemModel.getMainFolder(SystemModel.LOCATIONS).add(locationModel);
      restoredFigures.add(llf);
      // Den Stationstyp zuweisen
      // Der Typ der Station
      LocationTypeModel type = (LocationTypeModel) getModelComponent(systemModel, location.getType());
      locationModel.setLocationType(type);
      locationModel.updateTypeProperty(systemModel.getLocationTypeModels());
      locationModel.propertiesChanged(new NullAttributesChangeListener());
      // XXX Why clearing the objects properties? pseifert @ 25.04.14
      //kernel().clearTCSObjectProperties(location.getReference());
      KeyValueSetProperty misc = (KeyValueSetProperty) locationModel.getProperty(ModelComponent.MISCELLANEOUS);

      if (misc != null) {
        for (String key : location.getProperties().keySet()) {
          misc.addItem(new KeyValueProperty(locationModel, key, location.getProperties().get(key)));
        }
        // Datei für Default-Symbol
        SymbolProperty symbol = (SymbolProperty) locationModel.getProperty(ObjectPropConstants.LOC_DEFAULT_REPRESENTATION);

        if (symbol.getLocationRepresentation() != null) {
          LocationRepresentation symbolName = symbol.getLocationRepresentation();
          KeyValueProperty pr = new KeyValueProperty(locationModel, ObjectPropConstants.LOC_DEFAULT_REPRESENTATION, symbolName.name());
          misc.addItem(pr);

          Iterator<KeyValueProperty> e = misc.getItems().iterator();

          while (e.hasNext()) {
            pr = e.next();
            kernel().setTCSObjectProperty(location.getReference(), pr.getKey(), pr.getValue());
          }
        }
      }

      // Die zugehörigen Links
      Set<Link> attachedLinks = location.getAttachedLinks();

      for (Link link : attachedLinks) {
        // Der mit dem Link verbundene Point
        TCSResourceReference<Point> refPoint = link.getPoint();
        Point point = kernel().getTCSObject(Point.class, refPoint);
        PointModel pointModel = (PointModel) getModelComponent(systemModel, point.getReference());
        LabeledPointFigure lpf = pointModel.getFigure();
        // Eine Figure zur Darstellung des Links...
        LinkConnection linkConnection = crsObjFactory.createLinkConnection();
        // ...verbindet Point und Location
        linkConnection.connect(lpf, llf);

        // Das zur Figure gehörige Datenmodell in der GUI
        LinkModel linkModel = linkConnection.getModel();
        // Speziell für diesen Link erlaubte Operation
        ArrayList<String> operationItems = new ArrayList<>();

        for (String operation : link.getAllowedOperations()) {
          operationItems.add(operation);
        }

        StringSetProperty pOperations = (StringSetProperty) linkModel.getProperty(LinkModel.ALLOWED_OPERATIONS);
        pOperations.setItems(operationItems);

        adapter = addProcessAdapter(systemModel, linkModel, pointModel, locationModel);
        adapter.setInTransition(true);
        createdAdapters.add(adapter);
        linkModel.setFigure(linkConnection);
        linkModel.addAttributesChangeListener(linkConnection);
        systemModel.getMainFolder(SystemModel.LINKS).add(linkModel);
        restoredFigures.add(linkConnection);
      }

      // Koordinaten der Location ändern sich, wenn der Maßstab verändert wird
      origin.addListener(llf);
      llf.set(FigureConstants.ORIGIN, origin);
    }
  }

  private void restoreModelLocationTypes(Set<LocationType> allLocationTypes,
                                         SystemModel systemModel,
                                         Set<OpenTCSProcessAdapter> createdAdapters) {
    // --- Alle Location-Types, die der Kernel kennt ---
    for (LocationType locationType : allLocationTypes) {
      LocationTypeModel locationTypeModel = crsObjFactory.createLocationTypeModel();
      OpenTCSProcessAdapter adapter = addProcessAdapter(systemModel,
                                                        locationType.getReference(),
                                                        locationTypeModel);
      adapter.setInTransition(true);
      createdAdapters.add(adapter);
      adapter.updateModelProperties();
      systemModel.getMainFolder(SystemModel.LOCATION_TYPES).add(locationTypeModel);
    }
  }

  private void restoreModelVehicles(Set<Vehicle> allVehicles,
                                    SystemModel systemModel,
                                    Set<OpenTCSProcessAdapter> createdAdapters) {
    // --- Alle Fahrzeuge, die der Kernel kennt ---
    for (Vehicle vehicle : allVehicles) {
      VehicleModel vehicleModel = crsObjFactory.createVehicleModel();
      vehicleModel.setReference(vehicle.getReference());
      // Adapter zur Verknüpfung des Kernel-Objekts mit der Figur
      OpenTCSProcessAdapter adapter = addProcessAdapter(systemModel,
                                                        vehicle.getReference(),
                                                        vehicleModel);
      adapter.setInTransition(true);
      createdAdapters.add(adapter);
      // Setze Typ, Koordinaten, ... aus dem Kernel-Modell
      adapter.updateModelProperties();
      systemModel.getMainFolder(SystemModel.VEHICLES).add(vehicleModel);
      // Die VehicleFigures werden erst in OpenTCSDrawingView.setVehicles() erzeugt
    }
  }

  private void restoreModelPaths(Set<Path> allPaths, SystemModel systemModel,
                                 Origin origin,
                                 Set<OpenTCSProcessAdapter> createdAdapters,
                                 List<Figure> restoredFigures) {
    // --- Alle Pfade, die der Kernel kennt ---
    for (Path path : allPaths) {
      // Neues Figure-Objekt
      PathConnection pathFigure = crsObjFactory.createPathConnection();
      // Das zugehörige Modell
      PathModel pathModel = pathFigure.getModel();
      // Anfangs- und Endpunkte
      PointModel startPointModel = (PointModel) getModelComponent(systemModel, path.getSourcePoint());
      PointModel endPointModel = (PointModel) getModelComponent(systemModel, path.getDestinationPoint());
      pathFigure.connect(startPointModel.getFigure(), endPointModel.getFigure());
      // Adapter zur Verknüpfung des Kernel-Objekts mit der Figur
      OpenTCSProcessAdapter adapter = addProcessAdapter(systemModel, path.getReference(), pathModel);
      adapter.setInTransition(true);
      createdAdapters.add(adapter);
      // Das zugehörige Model Layout Element suchen und mit dem Adapter verknüpfen
      ModelLayoutElement layoutElement = systemModel.getLayoutMap().get(path.getReference());

      if (layoutElement != null) {
        ((PathAdapter) adapter).setLayoutElement(layoutElement);
      }

      // Setze Typ, Koordinaten, ... aus dem Kernel-Modell
      adapter.updateModelProperties();

      pathFigure.updateDecorations();

      if (layoutElement != null) {
        Map<String, String> layoutProperties = layoutElement.getProperties();
        SelectionProperty property = (SelectionProperty) pathModel.getProperty(ElementPropKeys.PATH_CONN_TYPE);
        String sConnectionType = layoutProperties.get(ElementPropKeys.PATH_CONN_TYPE);

        if (sConnectionType != null && !sConnectionType.isEmpty()) {
          PathModel.LinerType connectionType
              = PathModel.LinerType.valueOfNormalized(sConnectionType);
          property.setValue(connectionType);
          pathFigure.setLinerByType(connectionType);

          if (connectionType.equals(PathModel.LinerType.BEZIER)) {
            String sControlPoints = layoutProperties.get(ElementPropKeys.PATH_CONTROL_POINTS);
            // Format: x1,y1 or x1,y1;x2,y2
            if (sControlPoints != null && !sControlPoints.isEmpty()) {
              String[] values = sControlPoints.split("[,;]");

              try {
                if (values.length >= 2) {
                  int xcp1 = Integer.parseInt(values[0]);
                  int ycp1 = Integer.parseInt(values[1]);
                  Point2D.Double cp1 = new Point2D.Double(xcp1, ycp1);

                  if (values.length >= 4) {
                    int xcp2 = Integer.parseInt(values[2]);
                    int ycp2 = Integer.parseInt(values[3]);
                    Point2D.Double cp2 = new Point2D.Double(xcp2, ycp2);
                    pathFigure.addControlPoints(cp1, cp2);	// Cubic curve
                  }
                  else {
                    pathFigure.addControlPoints(cp1, cp1);	// Quadratic curve
                  }
                }
              }
              catch (NumberFormatException nfex) {
              }
            }
          }
        }

        // HH 2014-02-14: Verschieben der Pfeilspitze ist in der Figure noch nicht
        // implementiert, daher dieses Property auch nicht auswerten
//			String arrowPosition = layoutProperties.get(ElementPropKeys.PATH_ARROW_POSITION);
      }

      pathModel.setFigure(pathFigure);
      pathModel.addAttributesChangeListener(pathFigure);
      systemModel.getMainFolder(SystemModel.PATHS).add(pathModel);
      restoredFigures.add(pathFigure);
      // Koordinaten der Kontrollpunkte ändern sich, wenn der Maßstab verändert wird
      origin.addListener(pathFigure);
      pathFigure.set(FigureConstants.ORIGIN, origin);
    }
  }

  private void restoreModelPoints(Set<Point> allPoints, SystemModel systemModel,
                                  Origin origin, double scaleX, double scaleY,
                                  Set<OpenTCSProcessAdapter> adapters,
                                  List<Figure> restoredFigures) {
    // --- Alle Punkte, die der Kernel kennt ---
    for (Point point : allPoints) {
      // Neues Figure-Objekt
      PointFigure pointFigure = crsObjFactory.createPointFigure();
      // Das zugehörige Modell
      PointModel pointModel = pointFigure.getModel();
      // Adapter zur Verknüpfung des Kernel-Objekts mit der Figur
      PointAdapter adapter = (PointAdapter) addProcessAdapter(systemModel,
                                                              point.getReference(),
                                                              pointModel);
      // Das zugehörige Model Layout Element suchen und mit dem Adapter verknüpfen
      ModelLayoutElement layoutElement = systemModel.getLayoutMap().get(point.getReference());
      if (layoutElement != null) {
        adapter.setLayoutElement(layoutElement);
      }
      // Setze Typ, Koordinaten, ... aus dem Kernel-Modell
      adapter.updateModelProperties();
      adapter.setInTransition(true);
      adapters.add(adapter);
      // Die im Kernel gespeicherte Position
      double figurePositionX = point.getPosition().getX();
      double figurePositionY = point.getPosition().getY();
      // TODO: positionZ = point.getPosition().getZ();	// immer 0
      // Die zugehörige Beschriftung:
      TCSLabelFigure label = new TCSLabelFigure(point.getName());

      Point2D.Double labelPosition;
      Point2D.Double figurePosition;
      if (layoutElement != null) {
        Map<String, String> layoutProperties = layoutElement.getProperties();
        String pointPosX = layoutProperties.get(ElementPropKeys.POINT_POS_X);
        String pointPosY = layoutProperties.get(ElementPropKeys.POINT_POS_Y);
        // Die in den Properties gespeicherte Position überschreibt die im Kernel-Objekt gespeicherten Werte
        // TO DO: Auswahl, z.B. über Parameter?
        if (pointPosX != null && pointPosY != null) {
          try {
            figurePositionX = Integer.parseInt(pointPosX);
            figurePositionY = Integer.parseInt(pointPosY);
          }
          catch (NumberFormatException ex) {
          }
        }

        // Label
        String labelOffsetX = layoutProperties.get(ElementPropKeys.POINT_LABEL_OFFSET_X);
        String labelOffsetY = layoutProperties.get(ElementPropKeys.POINT_LABEL_OFFSET_Y);
        // TODO: labelOrientationAngle auswerten
//			String labelOrientationAngle = layoutProperties.get(ElementPropKeys.POINT_LABEL_ORIENTATION_ANGLE);

        double labelPositionX;
        double labelPositionY;
        if (labelOffsetX != null && labelOffsetY != null) {
          try {
            labelPositionX = Integer.parseInt(labelOffsetX);
            labelPositionY = Integer.parseInt(labelOffsetY);
          }
          catch (NumberFormatException ex) {
            // XXX This does not look right.
            labelPositionX = labelPositionY = -20;
          }

          labelPosition = new Point2D.Double(labelPositionX, labelPositionY);
          label.setOffset(labelPosition);
        }
      }
      // Figur auf diese Position verschieben
      figurePosition = new Point2D.Double(figurePositionX / scaleX, -figurePositionY / scaleY);	// Vorzeichen!
      pointFigure.setBounds(figurePosition, figurePosition);

      LabeledPointFigure lpf = new LabeledPointFigure(pointFigure);
      labelPosition = pointFigure.getStartPoint();
      labelPosition.x += label.getOffset().x;
      labelPosition.y += label.getOffset().y;
      label.setBounds(labelPosition, null);
      lpf.setLabel(label);

      pointModel.setFigure(lpf);
      pointModel.addAttributesChangeListener(lpf);
      systemModel.getMainFolder(SystemModel.POINTS).add(pointModel);
      restoredFigures.add(lpf);

      // Koordinaten der Punkte ändern sich, wenn der Maßstab verändert wird
      origin.addListener(lpf);
      lpf.set(FigureConstants.ORIGIN, origin);
    }
  }

  /**
   * Wird zum Erzeugen des VisualLayout aufgerufen
   *
   * @param systemModel
   * @param model
   * @return
   */
  public LayoutAdapter createLayoutAdapter(SystemModel systemModel,
                                           ModelComponent model) {

    LayoutAdapter adapter = (LayoutAdapter) systemModel.getEventDispatcher()
        .createProcessAdapter(model.getClass());
    adapter.setModel(model);
    adapter.setEventDispatcher(systemModel.getEventDispatcher());
    adapter.register();

    try {
      adapter.createProcessObject();
      systemModel.getEventDispatcher().addProcessAdapter(adapter);
    }
    catch (KernelRuntimeException ex) {
      log.log(Level.SEVERE, "Exception in creating process object", ex);
    }

    return adapter;
  }

  /**
   * Saves the current kernel model.
   *
   * @return Whether the model was actually saved.
   */
  private boolean save() {
    if (fModelName == null || fModelName.equals(Kernel.DEFAULT_MODEL_NAME)) {
      return false;
    }

    try {
      kernel().saveModel(fModelName, true);
      return true;
    }
    catch (IOException | CredentialsException | ObjectUnknownException e) {
      log.log(Level.WARNING, "Exception saving model " + fModelName, e);
      statusPanel.setLogMessage(Level.WARNING, e.getMessage());
    }

    return false;
  }

  /**
   * Liefert die Leitsteuerung.
   *
   * @return die Leitsteuerung
   */
  private Kernel kernel() {
    return kernelProxyManager.kernel();
  }

  /**
   * Erzeugt einen Dialog zur Auswahl eines Fahrkursmodells.
   *
   * @param content der Dialoginhalt
   * @param title Der Titel des Dialogs.
   * @return den Dialog
   */
  private StandardDialog createModelChooserDialog(String title, JComponent content) {
    return new StandardDialog(OpenTCSView.instance(), true, content, title);
  }

  /**
   * Erzeugt den Inhalt des Dialogs zur Auswahl eines Fahrkursmodells.
   *
   * @return den Dialoginhalt
   * @throws CredentialsException wenn die Benutzerrechte es nicht erlauben, die
   * Namen der vorhandenen Models abzurufen
   */
  private SystemModelNameChooser createDialogContent() throws CredentialsException {
    ArrayList<String> list = new ArrayList<>();
    Set<String> modelNames = kernel().getModelNames();
    Iterator<String> i = modelNames.iterator();

    while (i.hasNext()) {
      list.add(i.next());
    }

    return new SystemModelNameChooser(list);
  }

  /**
   * Liefert das Layout mit dem übergebenen Namen. Existiert das Layout nicht,
   * wird es zuvor erzeugt.
   *
   * @param name der Name des gesuchten Layouts
   * @return das gefundene Layout oder
   * <code> null </code>, wenn kein Layout-Objekt gefunden wurde
   * @throws CredentialsException wenn die Benutzerrechte nicht zum Lesen von
   * Layouts ausreichen
   */
  private Layout getLayout(String name) throws CredentialsException {
    Set<Layout> layouts = kernel().getTCSObjects(Layout.class);

    for (Layout layout : layouts) {
      if (layout.getName().equals(name)) {
        return layout;
      }
    }

    return null;
  }

  /**
   * Zeigt einen Dialog zum Öffnen eines Fahrkursmodells.
   *
   * @return true, wenn der Anwender den Ok-Button gedrückt hat
   * @param content der Dialoginhalt
   */
  private boolean showOpenDialog(SystemModelNameChooser content) {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    String title = bundle.getString("systemModelNameChooser.title.open");
    StandardDialog dialog = createModelChooserDialog(title, content);
    dialog.setLocationRelativeTo(OpenTCSView.instance());
    dialog.setVisible(true);

    if (dialog.getReturnStatus() == StandardDialog.RET_OK) {
      String choosenName = content.getChoosenName();

      if (choosenName.isEmpty()) {
        String nameInvalid = bundle.getString("message.nameInvalid");
        String enterName = bundle.getString("message.enterName");
        JOptionPane.showMessageDialog(OpenTCSView.instance(), enterName, nameInvalid, JOptionPane.ERROR_MESSAGE);

        return false;
      }
      else {
        fModelName = choosenName;
      }

      return true;
    }
    else {
      return false;
    }
  }

  /**
   * Zeigt einen Dialog zum Speichern eines Fahrkursmodells.
   *
   * @return true, wenn der Anwender den Ok-Button gedrückt hat
   * @param content der Dialoginhalt
   */
  private boolean showSaveDialog(SystemModelNameChooser content) {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    String title = bundle.getString("systemModelNameChooser.title.save");
    StandardDialog dialog = createModelChooserDialog(title, content);
    dialog.setLocationRelativeTo(OpenTCSView.instance());
    dialog.setVisible(true);

    if (dialog.getReturnStatus() == StandardDialog.RET_OK) {
      // Warnung, wenn der Name eines bereits existierenden Modells ausgewählt wurde
      // ... außer dem Namen des aktuellen Modells
      String dialogTitle = bundle.getString("message.nameExists");
      String dialogText = bundle.getString("message.model.nameExists");

      if (content.nameExists() && !(content.getChoosenName().equals(fModelName))) {
        int result = JOptionPane.showConfirmDialog(OpenTCSView.instance(), dialogText, dialogTitle, JOptionPane.ERROR_MESSAGE);

        if (result == JOptionPane.NO_OPTION) {
          return showSaveDialog(content);
        }
        else if (result == JOptionPane.CANCEL_OPTION) {
          return false;
        }
      }

      fModelName = content.getChoosenName();

      if (fModelName.isEmpty()) {
        JOptionPane.showMessageDialog(OpenTCSView.instance(), dialogText, dialogTitle, JOptionPane.ERROR_MESSAGE);
        fModelName = Kernel.DEFAULT_MODEL_NAME;
        return showSaveDialog(content);
      }

      return true;
    }
    else {
      fModelName = Kernel.DEFAULT_MODEL_NAME;
      return false;
    }
  }

  /**
   * Fabrikmethode zur Erzeugung des EventDispatchers.
   *
   * @return den neu erzeugten EventDispatcher
   */
  private EventDispatcher createEventDispatcher() {
    OpenTCSEventDispatcher dispatcher
        = new OpenTCSEventDispatcher(procAdapterFactory);
    dispatcher.setKernel(kernel());
    return dispatcher;
  }

  /**
   * Erzeugt zu einem ModelComponent-Objekt und einem TCSObject einen passenden
   * ProcessAdapter und fügt ihn dem EventDispatcher hinzu.
   *
   * @param systemModel
   * @param ref das Objekte in der Leitsteuerung
   * @param model das ModelComponent-Objekt
   * @return den erzeugten ProcessAdapter
   */
  private OpenTCSProcessAdapter addProcessAdapter(
      SystemModel systemModel, TCSObjectReference<?> ref, ModelComponent model) {

    ProcessAdapter adapter = systemModel.getEventDispatcher().createProcessAdapter(model.getClass());
    adapter.setModel(model);
    adapter.setEventDispatcher(systemModel.getEventDispatcher());
    adapter.setProcessObject(ref);
    adapter.register();

    return (OpenTCSProcessAdapter) adapter;
  }

  /**
   * Erzeugt zu einer gelesenen Verknüpfung einen passenden ProcessAdapter und
   * fügt ihn dem EventDispatcher hinzu.
   *
   * @param systemModel
   * @param link Die Verknüpfung
   * @param point Der Meldepunkt
   * @param location Die Station
   * @return Der erzeugte ProcessAdapter
   */
  private OpenTCSProcessAdapter addProcessAdapter(SystemModel systemModel,
                                                  LinkModel link,
                                                  PointModel point,
                                                  LocationModel location) {

    LinkAdapter linkAdapter = (LinkAdapter) systemModel.getEventDispatcher().createProcessAdapter(link.getClass());
    linkAdapter.setModel(link);
    linkAdapter.setEventDispatcher(systemModel.getEventDispatcher());
    PointAdapter pointAdapter = (PointAdapter) systemModel.getEventDispatcher().findProcessAdapter(point);
    LocationAdapter locationAdapter = (LocationAdapter) systemModel.getEventDispatcher().findProcessAdapter(location);
    linkAdapter.setConnectedProcessObjects(pointAdapter.getProcessObject(),
                                           locationAdapter.getProcessObject());
    linkAdapter.register();

    return linkAdapter;
  }

  /**
   * Findet zu einem TCSObject das passende Objekt in der Modellierung.
   *
   * @param systemModel
   * @param ref die Referenz auf das TCSObject
   * @return das Objekt in der Modellierung
   */
  private ModelComponent getModelComponent(SystemModel systemModel,
                                           TCSObjectReference<?> ref) {
    ProcessAdapter adapter = systemModel.getEventDispatcher().findProcessAdapter(ref);

    if (adapter == null) {
      return null;
    }

    return adapter.getModel();
  }
}
