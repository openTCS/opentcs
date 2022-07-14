/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import org.jhotdraw.draw.DefaultDrawing;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.Figure;
import org.opentcs.data.model.ModelConstants;
import org.opentcs.data.model.visualization.Layer;
import org.opentcs.data.model.visualization.LayerGroup;
import org.opentcs.guing.base.components.layer.LayerWrapper;
import org.opentcs.guing.base.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.base.components.properties.type.StringProperty;
import org.opentcs.guing.base.model.CompositeModelComponent;
import org.opentcs.guing.base.model.ModelComponent;
import static org.opentcs.guing.base.model.ModelComponent.MISCELLANEOUS;
import static org.opentcs.guing.base.model.ModelComponent.NAME;
import org.opentcs.guing.base.model.SimpleFolder;
import org.opentcs.guing.base.model.elements.BlockModel;
import org.opentcs.guing.base.model.elements.LayoutModel;
import org.opentcs.guing.base.model.elements.LinkModel;
import org.opentcs.guing.base.model.elements.LocationModel;
import org.opentcs.guing.base.model.elements.LocationTypeModel;
import org.opentcs.guing.base.model.elements.OtherGraphicalElement;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.components.drawing.course.CoordinateBasedDrawingMethod;
import org.opentcs.guing.common.components.drawing.course.DrawingMethod;
import org.opentcs.guing.common.util.I18nPlantOverview;
import org.opentcs.guing.common.util.ModelComponentFactory;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;

/**
 * Base implementation for a SystemModel.
 * Holds the vehicles and the layout of the model. The SystemModel has a map of base components
 * for each component type (e.g. points, locations, vehicles, ...).
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class StandardSystemModel
    extends CompositeModelComponent
    implements SystemModel {

  /**
   * A Map that represents the main folder of the system model.
   * Maps a folder key to the main model component for that folder.
   */
  private final Map<FolderKey, ModelComponent> fMainFolders = new HashMap<>();
  /**
   * Maps a model component class to a main folder component.
   * Practically determines which folder a model component belongs to.
   */
  private final Map<Class<?>, ModelComponent> fParentFolders = new HashMap<>();
  /**
   * Maps model components to the corresponding figures.
   */
  private final Map<ModelComponent, Figure> figuresMap = new HashMap<>();
  /**
   * The drawing.
   */
  private final Drawing fDrawing = new DefaultDrawing();
  /**
   * The used drawing method.
   */
  private final DrawingMethod fDrawingMethod = new CoordinateBasedDrawingMethod();

  private final ModelComponentFactory modelComponentFactory;

  /**
   * Creates a new instance with a default drawing method.
   */
  @Inject
  public StandardSystemModel(ModelComponentFactory modelComponentFactory) {
    super("Model");
    this.modelComponentFactory = requireNonNull(modelComponentFactory, "modelComponentFactory");

    createMainFolders();
    setupParentFolders();
    createProperties();
  }

  @Override
  public KeyValueSetProperty getPropertyMiscellaneous() {
    return (KeyValueSetProperty) getProperty(MISCELLANEOUS);
  }

  @Override // SystemModel
  public void addMainFolder(FolderKey key, ModelComponent component) {
    fMainFolders.put(key, component);
  }

  @Override // SystemModel
  public ModelComponent getMainFolder(FolderKey key) {
    return fMainFolders.get(key);
  }

  @Override // SystemModel
  public ModelComponent getFolder(ModelComponent item) {
    if (item == null) {
      return null;
    }

    for (Class<?> c : fParentFolders.keySet()) {
      if (item.getClass() == c || c.isInstance(item)) {
        return fParentFolders.get(c);
      }
    }

    return null;
  }

  @Override // SystemModel
  public <T> List<T> getAll(FolderKey foldername, Class<T> classType) {
    List<T> items = new ArrayList<>();
    for (ModelComponent o : getMainFolder(foldername).getChildComponents()) {
      if (classType.isInstance(o)) {
        items.add(classType.cast(o));
      }
    }

    return items;
  }

  @Override
  public List<ModelComponent> getAll() {
    List<ModelComponent> items = new ArrayList<>();
    for (ModelComponent o : fMainFolders.values()) { //Iterate over folders
      if (o instanceof CompositeModelComponent) {
        items.addAll(getAll((CompositeModelComponent) o));
      }
      else {
        items.add(o);
      }
    }
    return items;
  }
  
  @Override
  public void onRestorationComplete(){
  }

  @Override
  public void registerFigure(ModelComponent component, Figure figure) {
    figuresMap.put(component, figure);
  }

  @Override
  public Figure getFigure(ModelComponent component) {
    return figuresMap.get(component);
  }

  @Override // SystemModel
  public Drawing getDrawing() {
    return fDrawing;
  }

  @Override // SystemModel
  public DrawingMethod getDrawingMethod() {
    return fDrawingMethod;
  }

  @Override
  public ModelComponent getModelComponent(String name) {
    for (ModelComponent folder : fMainFolders.values()) {
      ModelComponent component = getModelComponent(name, folder);
      if (component != null) {
        return component;
      }
    }

    return null;
  }

  @Override // SystemModel
  public List<VehicleModel> getVehicleModels() {
    return getAll(FolderKey.VEHICLES, VehicleModel.class);
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
  public LayoutModel getLayoutModel() {
    return (LayoutModel) getMainFolder(FolderKey.LAYOUT);
  }

  @Override // SystemModel
  public List<PointModel> getPointModels() {
    return getAll(FolderKey.POINTS, PointModel.class);
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
    return getAll(FolderKey.LOCATIONS, LocationModel.class);
  }

  @Override // SystemModel
  public List<LocationModel> getLocationModels(LocationTypeModel type) {
    List<LocationModel> items = new ArrayList<>();
    for (LocationModel location : getLocationModels()) {
      if (location.getLocationType() == type) {
        items.add(location);
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
    return getAll(FolderKey.PATHS, PathModel.class);
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
    return getAll(FolderKey.LINKS, LinkModel.class);
  }

  @Override // SystemModel
  public List<LinkModel> getLinkModels(LocationTypeModel locationType) {
    List<LinkModel> items = new ArrayList<>();
    for (LinkModel ref : getLinkModels()) {
      if (ref.getLocation().getLocationType() == locationType) {
        items.add(ref);
      }
    }

    return items;
  }

  @Override // SystemModel
  public List<LocationTypeModel> getLocationTypeModels() {
    return getAll(FolderKey.LOCATION_TYPES, LocationTypeModel.class);
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

  @Override
  public BlockModel getBlockModel(String name) {
    for (BlockModel block : getBlockModels()) {
      if (block.getName().equals(name)) {
        return block;
      }
    }

    return null;
  }

  @Override // SystemModel
  public List<BlockModel> getBlockModels() {
    return getAll(FolderKey.BLOCKS, BlockModel.class);
  }

  @Override // SystemModel
  public List<OtherGraphicalElement> getOtherGraphicalElements() {
    return getAll(FolderKey.OTHER_GRAPHICAL_ELEMENTS, OtherGraphicalElement.class);
  }

  /**
   * Return all model components in a folder.
   *
   * @param folder The folder of which to get the components from.
   * @return A list of all model components in that folder.
   */
  private List<ModelComponent> getAll(CompositeModelComponent folder) {
    List<ModelComponent> result = new LinkedList<>();
    for (ModelComponent component : folder.getChildComponents()) {
      if (component instanceof CompositeModelComponent) {
        result.addAll(getAll((CompositeModelComponent) component));
      }
      else {
        result.add(component);
      }
    }
    return result;
  }

  private ModelComponent getModelComponent(String name, ModelComponent root) {
    if (root instanceof CompositeModelComponent) {
      for (ModelComponent subComponent : root.getChildComponents()) {
        ModelComponent result = getModelComponent(name, subComponent);
        if (result != null) {
          return result;
        }
      }
    }
    else if (Objects.equals(name, root.getName())) {
      return root;
    }
    return null;
  }

  /**
   * Creates the main folder in the system model.
   */
  private void createMainFolders() {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle(I18nPlantOverview.TREEVIEW_PATH);
    createMainFolder(this, FolderKey.VEHICLES,
                     new SimpleFolder(bundle.getString("standardSystemModel.folder_vehicles.name")));

    createMainFolder(this, FolderKey.LAYOUT, createDefaultLayoutModel());

    createMainFolder(getMainFolder(FolderKey.LAYOUT), FolderKey.POINTS,
                     new SimpleFolder(bundle.getString("standardSystemModel.folder_points.name")));
    createMainFolder(getMainFolder(FolderKey.LAYOUT), FolderKey.PATHS,
                     new SimpleFolder(bundle.getString("standardSystemModel.folder_paths.name")));
    createMainFolder(getMainFolder(FolderKey.LAYOUT), FolderKey.LOCATIONS,
                     new SimpleFolder(bundle.getString("standardSystemModel.folder_locations.name")));
    createMainFolder(getMainFolder(FolderKey.LAYOUT), FolderKey.LOCATION_TYPES,
                     new SimpleFolder(bundle.getString("standardSystemModel.folder_locationTypes.name")));
    createMainFolder(getMainFolder(FolderKey.LAYOUT), FolderKey.LINKS,
                     new SimpleFolder(bundle.getString("standardSystemModel.folder_links.name")));
    createMainFolder(getMainFolder(FolderKey.LAYOUT), FolderKey.BLOCKS,
                     new SimpleFolder(bundle.getString("standardSystemModel.folder_blocks.name")));
    createMainFolder(getMainFolder(FolderKey.LAYOUT), FolderKey.OTHER_GRAPHICAL_ELEMENTS,
                     new SimpleFolder(bundle.getString("standardSystemModel.folder_otherGraphicalElements.name")));
  }

  private LayoutModel createDefaultLayoutModel() {
    LayoutModel layoutModel = modelComponentFactory.createLayoutModel();
    layoutModel.setName(ModelConstants.DEFAULT_VISUAL_LAYOUT_NAME);
    LayerGroup defaultGroup = new LayerGroup(ModelConstants.DEFAULT_LAYER_GROUP_ID,
                                             ModelConstants.DEFAULT_LAYER_GROUP_NAME,
                                             true);
    layoutModel.getPropertyLayerGroups().getValue().put(defaultGroup.getId(), defaultGroup);
    Layer defaultLayer = new Layer(ModelConstants.DEFAULT_LAYER_ID,
                                   ModelConstants.DEFAULT_LAYER_ORDINAL,
                                   true,
                                   ModelConstants.DEFAULT_LAYER_NAME,
                                   ModelConstants.DEFAULT_LAYER_GROUP_ID);
    layoutModel.getPropertyLayerWrappers().getValue()
        .put(defaultLayer.getId(), new LayerWrapper(defaultLayer, defaultGroup));

    return layoutModel;
  }

  private void createProperties() {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle(I18nPlantOverview.TREEVIEW_PATH);

    StringProperty pName = new StringProperty(this);
    pName.setDescription(bundle.getString("standardSystemModel.property_name.description"));
    pName.setHelptext(bundle.getString("standardSystemModel.property_name.helptext"));
    setProperty(NAME, pName);

    KeyValueSetProperty pMiscellaneous = new KeyValueSetProperty(this);
    pMiscellaneous.setDescription("Miscellaneous properties");
    pMiscellaneous.setHelptext("Miscellaneous properties");
    setProperty(MISCELLANEOUS, pMiscellaneous);
  }

  /**
   * Creates a folder for the system model and the TreeView.
   */
  private void createMainFolder(ModelComponent parentFolder,
                                FolderKey key,
                                ModelComponent newFolder) {
    addMainFolder(key, newFolder);
    parentFolder.add(newFolder);
  }

  /**
   * Initialises the association between a main folder and a model component class.
   */
  private void setupParentFolders() {
    fParentFolders.put(VehicleModel.class, getMainFolder(FolderKey.VEHICLES));
    fParentFolders.put(LayoutModel.class, getMainFolder(FolderKey.LAYOUT));
    fParentFolders.put(PointModel.class, getMainFolder(FolderKey.POINTS));
    fParentFolders.put(PathModel.class, getMainFolder(FolderKey.PATHS));
    fParentFolders.put(LocationModel.class, getMainFolder(FolderKey.LOCATIONS));
    fParentFolders.put(LocationTypeModel.class, getMainFolder(FolderKey.LOCATION_TYPES));
    fParentFolders.put(LinkModel.class, getMainFolder(FolderKey.LINKS));
    fParentFolders.put(BlockModel.class, getMainFolder(FolderKey.BLOCKS));
    fParentFolders.put(OtherGraphicalElement.class, getMainFolder(FolderKey.OTHER_GRAPHICAL_ELEMENTS));
  }
}
