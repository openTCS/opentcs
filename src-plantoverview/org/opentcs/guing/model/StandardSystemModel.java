/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.Drawing;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.visualization.LayoutElement;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.guing.components.drawing.course.CoordinateBasedDrawingMethod;
import org.opentcs.guing.components.drawing.course.DrawingMethod;
import org.opentcs.guing.exchange.EventDispatcher;
import org.opentcs.guing.exchange.adapter.BlockAdapter;
import org.opentcs.guing.exchange.adapter.GroupAdapter;
import org.opentcs.guing.exchange.adapter.LayoutAdapter;
import org.opentcs.guing.exchange.adapter.LinkAdapter;
import org.opentcs.guing.exchange.adapter.LocationAdapter;
import org.opentcs.guing.exchange.adapter.LocationTypeAdapter;
import org.opentcs.guing.exchange.adapter.PathAdapter;
import org.opentcs.guing.exchange.adapter.PointAdapter;
import org.opentcs.guing.exchange.adapter.ProcessAdapterFactory;
import org.opentcs.guing.exchange.adapter.StaticRouteAdapter;
import org.opentcs.guing.exchange.adapter.VehicleAdapter;
import org.opentcs.guing.model.elements.BlockModel;
import org.opentcs.guing.model.elements.GroupModel;
import org.opentcs.guing.model.elements.LayoutModel;
import org.opentcs.guing.model.elements.LinkModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.LocationTypeModel;
import org.opentcs.guing.model.elements.OtherGraphicalElement;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.model.elements.StaticRouteModel;
import org.opentcs.guing.model.elements.VehicleModel;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Standardimplementierung des Datenmodells des gesamten modellierten Systems.
 * Besteht aus den Batterien, den Transportgütern, den Fahrzeugtypen mit ihren
 * Fahrzeugen und dem Fahrkurslayout. Das Systemmodell verwaltet zusätzlich zu
 * seinen Kindelementen eine Hastable mit den Komposita-Komponenten, die
 * unbedingt vorhanden sein müssen. Die Applikation als Klient fragt das
 * Systemmodell dann nach einer bestimmten Komponente (z.B. der für die
 * Transportgüter), um herauszufinden, wo ein neu erzeugtes Transportgut
 * abgelegt werden kann.
 * <p>
 * <b>Entwurfsmuster:</b> Kompositum.
 * StandardSystemModel ist ein konkretes Kompositum.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
class StandardSystemModel
    extends CompositeModelComponent
    implements SystemModel {

  /**
   * Die Hashtable mit Zuordnungen zwischen Strings und den Hauptkomponenten des
   * Modells.
   */
  private final Map<String, ModelComponent> fMainFolders = new HashMap<>();
  /**
   * Enthält Zuordnungen zwischen den Hauptordnern des Modells und
   * Class-Objekten von ModelComponent-Objekten. Hierdurch ist praktisch
   * konfigurierbar, welche ModelComponent-Objekte in welchen Ordner gehören.
   */
  private final Map<Class, ModelComponent> fParentFolders = new HashMap<>();
  /**
   * Die Zeichnung.
   */
  private final Drawing fDrawing = new DefaultDrawing();
  /**
   * Die Map mit der Zuordnung der Model-Layout-Elemente zu den Kernel-Objekten
   */
  private final Map<TCSObjectReference<?>, ModelLayoutElement> fLayoutMap
      = new HashMap<>();
  /**
   * Die für das Modell verwendete Zeichenmethode.
   */
  private final DrawingMethod fDrawingMethod;
  /**
   * Die Tabelle mit den Zuordnungen zwischen Modellkomponente und
   * Leitsteuerungsobjekt.
   */
  private EventDispatcher fEventDispatcher;
  /**
   * The process adapter factory to be used.
   */
  private final ProcessAdapterFactory procAdapterFactory;

  /**
   * Creates a new instance.
   *
   * @param drawingMethod The drawing method.
   * @param procAdapterFactory The process adapter factory to be used.
   */
  public StandardSystemModel(DrawingMethod drawingMethod,
                             ProcessAdapterFactory procAdapterFactory) {
    super("Model");
    this.fDrawingMethod = requireNonNull(drawingMethod, "drawingMethod");
    this.procAdapterFactory = requireNonNull(procAdapterFactory,
                                             "procAdapterFactory");

    createMainFolders();
    setupParentFolders();
    initProcessAdapterFactory();
  }

  /**
   * Creates a new instance with a default drawing method.
   *
   * @param procAdapterFactory The process adapter factory to be used.
   */
  @Inject
  public StandardSystemModel(ProcessAdapterFactory procAdapterFactory) {
    this(new CoordinateBasedDrawingMethod(), procAdapterFactory);
  }

  @Override // SystemModel
  public void addMainFolder(String key, ModelComponent component) {
    fMainFolders.put(key, component);
  }

  @Override // SystemModel
  public ModelComponent getMainFolder(String key) {
    return fMainFolders.get(key);
  }

  @Override // SystemModel
  public ModelComponent getFolder(ModelComponent item) {
    if (item == null) {
      return null;
    }

    for (Class c : fParentFolders.keySet()) {
      if (item.getClass() == c || c.isInstance(item)) {
        return fParentFolders.get(c);
      }
    }

    return null;
  }

  @Override // SystemModel
  public <T> List<T> getAll(String foldername, Class<T> classType) {
    List<T> items = new ArrayList<>();
    for (ModelComponent o : getMainFolder(foldername).getChildComponents()) {
      if (classType.isInstance(o)) {
        items.add(classType.cast(o));
      }
    }

    return items;
  }

  @Override // SystemModel
  public EventDispatcher getEventDispatcher() {
    return fEventDispatcher;
  }

  @Override // SystemModel
  public void setEventDispatcher(EventDispatcher eventDispatcher) {
    fEventDispatcher = eventDispatcher;
  }

  @Override // SystemModel
  public Drawing createDrawing() {
    return new DefaultDrawing();
  }

  @Override // SystemModel
  public Drawing getDrawing() {
    return fDrawing;
  }

  @Override // SystemModel
  public DrawingMethod getDrawingMethod() {
    return fDrawingMethod;
  }

  @Override // SystemModel
  public List<VehicleModel> getVehicleModels() {
    List<VehicleModel> vehicles = new ArrayList<>();

    for (ModelComponent vComp : getMainFolder(VEHICLES).getChildComponents()) {
      vehicles.add((VehicleModel) vComp);
    }

    return vehicles;
  }

  @Override // SystemModel
  public VehicleModel getVehicleModel(String name) {
    for (VehicleModel vehicle : getVehicleModels()) {
      if (vehicle.getName().equals(name)) {
        return vehicle;
      }
    }

    return null;
  }

  @Override // SystemModel
  public List<LayoutModel> getLayoutModels() {
    List<LayoutModel> layouts = new ArrayList<>();
    // TODO: Erweitern, wenn es mal mehrere Visual Layouts zu einem Kernel-Modell geben sollte
    LayoutModel item = (LayoutModel) getMainFolder(LAYOUT);
    layouts.add(item);

    return layouts;
  }

  @Override // SystemModel
  public LayoutModel getLayoutModel(String name) {
    // TODO: Erweitern, wenn es mal mehrere Visual Layouts zu einem Kernel-Modell geben sollte
    return null;
  }

  @Override // SystemModel
  public List<PointModel> getPointModels() {
    return getAll(POINTS, PointModel.class);
  }

  @Override // SystemModel
  public PointModel getPointModel(String name) {
    for (PointModel point : getPointModels()) {
      if (point.getName().equals(name)) {
        return point;
      }
    }

    return null;
  }

  @Override // SystemModel
  public List<LocationModel> getLocationModels() {
    List<LocationModel> items = new ArrayList<>();

    for (ModelComponent item : getMainFolder(LOCATIONS).getChildComponents()) {
      if (item instanceof LocationModel) {
        items.add((LocationModel) item);
      }
    }

    return items;
  }

  @Override // SystemModel
  public List<LocationModel> getLocationModels(LocationTypeModel type) {
    List<LocationModel> items = new ArrayList<>();
    Iterator<LocationModel> e = getLocationModels().iterator();

    while (e.hasNext()) {
      LocationModel s = e.next();

      if (s.getLocationType() == type) {
        items.add(s);
      }
    }

    return items;
  }

  @Override // SystemModel
  public LocationModel getLocationModel(String name) {
    for (LocationModel location : getLocationModels()) {
      if (location.getName().equals(name)) {
        return location;
      }
    }

    return null;
  }

  @Override // SystemModel
  public List<PathModel> getPathModels() {
    List<PathModel> items = new ArrayList<>();

    for (ModelComponent item : getMainFolder(PATHS).getChildComponents()) {
      // XXX Why is this here? LinkModel is not a subclass of PathModel...
      if (item instanceof LinkModel) {
        continue;
      }
      if (item instanceof PathModel) {
        items.add((PathModel) item);
      }
    }

    return items;
  }

  @Override
  public PathModel getPathModel(String name) {
    for (PathModel path : getPathModels()) {
      if (path.getName().equals(name)) {
        return path;
      }
    }

    return null;
  }

  @Override // SystemModel
  public List<LinkModel> getLinkModels() {
    List<LinkModel> items = new ArrayList<>();

    for (ModelComponent item : getMainFolder(LINKS).getChildComponents()) {
      if (item instanceof LinkModel) {
        items.add((LinkModel) item);
      }
    }

    return items;
  }

  @Override // SystemModel
  public List<LinkModel> getLinkModels(LocationTypeModel locationType) {
    List<LinkModel> items = new ArrayList<>();

    for (LinkModel ref : getAll(LINKS, LinkModel.class)) {
      if (ref.getLocation().getLocationType() == locationType) {
        items.add(ref);
      }
    }

    return items;
  }

  @Override // SystemModel
  public List<LocationTypeModel> getLocationTypeModels() {
    List<LocationTypeModel> result = new ArrayList<>();
    for (ModelComponent component
         : getMainFolder(LOCATION_TYPES).getChildComponents()) {
      result.add((LocationTypeModel) component);
    }
    return result;
  }

  @Override // SystemModel
  public LocationTypeModel getLocationTypeModel(String name) {
    for (LocationTypeModel t : getLocationTypeModels()) {
      if (t.getName().equals(name)) {
        return t;
      }
    }

    return null;
  }

  @Override // SystemModel
  public List<BlockModel> getBlockModels() {
    List<BlockModel> result = new ArrayList<>();
    for (ModelComponent component : getMainFolder(BLOCKS).getChildComponents()) {
      result.add((BlockModel) component);
    }
    return result;
  }

  @Override // SystemModel
  public List<StaticRouteModel> getStaticRouteModels() {
    List<StaticRouteModel> result = new ArrayList<>();
    for (ModelComponent component
         : getMainFolder(STATIC_ROUTES).getChildComponents()) {
      result.add((StaticRouteModel) component);
    }
    return result;
  }

  @Override // SystemModel
  public List<OtherGraphicalElement> getOtherGraphicalElements() {
    List<OtherGraphicalElement> result = new ArrayList<>();
    for (ModelComponent component
         : getMainFolder(OTHER_GRAPHICAL_ELEMENTS).getChildComponents()) {
      result.add((OtherGraphicalElement) component);
    }
    return result;
  }

  @Override
  public void createLayoutMap(VisualLayout layout,
                              Set<Point> points,
                              Set<Path> paths,
                              Set<Location> locations,
                              Set<Block> blocks) {

    Set<LayoutElement> elements = layout.getLayoutElements();

    // Points
    for (Point point : points) {
      for (LayoutElement element : elements) {
        ModelLayoutElement mle = (ModelLayoutElement) element;

        if (mle.getVisualizedObject().equals(point.getReference())) {
          fLayoutMap.put(point.getReference(), mle);
          break;
        }
      }
    }

    // Paths
    for (Path path : paths) {
      for (LayoutElement element : elements) {
        ModelLayoutElement mle = (ModelLayoutElement) element;

        if (mle.getVisualizedObject().equals(path.getReference())) {
          fLayoutMap.put(path.getReference(), mle);
          break;
        }
      }
    }

    // Locations
    for (Location location : locations) {
      for (LayoutElement element : elements) {
        ModelLayoutElement mle = (ModelLayoutElement) element;

        if (mle.getVisualizedObject().equals(location.getReference())) {
          fLayoutMap.put(location.getReference(), mle);
        }
      }
    }

    // Blocks
    for (Block block : blocks) {
      for (LayoutElement element : elements) {
        ModelLayoutElement mle = (ModelLayoutElement) element;

        if (mle.getVisualizedObject().equals(block.getReference())) {
          fLayoutMap.put(block.getReference(), mle);
        }
      }
    }
  }

  @Override
  public Map<TCSObjectReference<?>, ModelLayoutElement> getLayoutMap() {
    return fLayoutMap;
  }

  /**
   * Initialisiert die Fabrik zur Erzeugung von passenden
   * ProcessAdapter-Objekten.
   */
  private void initProcessAdapterFactory() {
    procAdapterFactory.add(VehicleModel.class, new VehicleAdapter());
    procAdapterFactory.add(LayoutModel.class, new LayoutAdapter());
    procAdapterFactory.add(PointModel.class, new PointAdapter());
    procAdapterFactory.add(PathModel.class, new PathAdapter());
    procAdapterFactory.add(LocationModel.class, new LocationAdapter());
    procAdapterFactory.add(LocationTypeModel.class, new LocationTypeAdapter());
    procAdapterFactory.add(LinkModel.class, new LinkAdapter());
    procAdapterFactory.add(BlockModel.class, new BlockAdapter());
    procAdapterFactory.add(GroupModel.class, new GroupAdapter());
    procAdapterFactory.add(StaticRouteModel.class, new StaticRouteAdapter());
//  factory.add(OtherGraphicalElement.class, new OtherGraphicalElementAdapter());  // TODO ???
  }

  /**
   * Erstellt die unveränderlichen Hauptordner des TreeViews. Hauptordner
   * existieren immer, auch wenn es sonst keine Komponenten im Systemmodell
   * gibt. Hauptordner sollen allein durch Nutzereingaben nicht gelöscht werden
   * können.
   */
  private void createMainFolders() {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    createMainFolder(this, VEHICLES,
                     new SimpleFolder(bundle.getString("tree.vehicles.text")));
    createMainFolder(this, LAYOUT,
                     new LayoutModel(bundle.getString("tree.layout.text")));
    createMainFolder(getMainFolder(LAYOUT), POINTS,
                     new SimpleFolder(bundle.getString("tree.points.text")));
    createMainFolder(getMainFolder(LAYOUT), PATHS,
                     new SimpleFolder(bundle.getString("tree.paths.text")));
    createMainFolder(getMainFolder(LAYOUT), LOCATIONS,
                     new SimpleFolder(bundle.getString("tree.locations.text")));
    createMainFolder(getMainFolder(LAYOUT), LOCATION_TYPES,
                     new SimpleFolder(bundle.getString("tree.locationTypes.text")));
    createMainFolder(getMainFolder(LAYOUT), LINKS,
                     new SimpleFolder(bundle.getString("tree.links.text")));
    createMainFolder(getMainFolder(LAYOUT), BLOCKS,
                     new SimpleFolder(bundle.getString("tree.blocks.text")));
    createMainFolder(getMainFolder(LAYOUT), GROUPS,
                     new SimpleFolder(bundle.getString("tree.groups.text")));
    createMainFolder(getMainFolder(LAYOUT), STATIC_ROUTES,
                     new SimpleFolder(bundle.getString("tree.staticRoutes.text")));
    createMainFolder(getMainFolder(LAYOUT), OTHER_GRAPHICAL_ELEMENTS,
                     new SimpleFolder(bundle.getString("tree.otherGraphicals.text")));
  }

  /**
   * Erzeugt einen Hauptordner, der sowohl dem TreeView als auch dem
   * Systemmodell hinzugefügt wird.
   */
  private void createMainFolder(ModelComponent parentFolder,
                                String key,
                                ModelComponent newFolder) {
    addMainFolder(key, newFolder);
    parentFolder.add(newFolder);
  }

  /**
   * Initialisiert die Zuordnungen zwischen ModelComponent-Ordnern und
   * ModelComponent-Inhalten anhand von Class-Objekten.
   */
  private void setupParentFolders() {
    fParentFolders.put(VehicleModel.class, getMainFolder(VEHICLES));
    fParentFolders.put(LayoutModel.class, getMainFolder(LAYOUT));
    fParentFolders.put(PointModel.class, getMainFolder(POINTS));
    fParentFolders.put(PathModel.class, getMainFolder(PATHS));
    fParentFolders.put(LocationModel.class, getMainFolder(LOCATIONS));
    fParentFolders.put(LocationTypeModel.class, getMainFolder(LOCATION_TYPES));
    fParentFolders.put(LinkModel.class, getMainFolder(LINKS));
    fParentFolders.put(BlockModel.class, getMainFolder(BLOCKS));
    fParentFolders.put(GroupModel.class, getMainFolder(GROUPS));
    fParentFolders.put(StaticRouteModel.class, getMainFolder(STATIC_ROUTES));
    fParentFolders.put(OtherGraphicalElement.class, getMainFolder(OTHER_GRAPHICAL_ELEMENTS));
  }
}
