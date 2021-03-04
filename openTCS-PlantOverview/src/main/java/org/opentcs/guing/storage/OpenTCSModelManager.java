/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.storage;

import com.google.common.base.Strings;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import org.jhotdraw.draw.Figure;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.components.plantoverview.PlantModelExporter;
import org.opentcs.components.plantoverview.PlantModelImporter;
import org.opentcs.customizations.ApplicationHome;
import org.opentcs.data.ObjectPropConstants;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Group;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Location.Link;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.guing.application.StatusPanel;
import org.opentcs.guing.components.drawing.course.Origin;
import org.opentcs.guing.components.drawing.course.OriginChangeListener;
import org.opentcs.guing.components.drawing.figures.FigureConstants;
import org.opentcs.guing.components.drawing.figures.LabeledLocationFigure;
import org.opentcs.guing.components.drawing.figures.LabeledPointFigure;
import org.opentcs.guing.components.drawing.figures.LinkConnection;
import org.opentcs.guing.components.drawing.figures.LocationFigure;
import org.opentcs.guing.components.drawing.figures.PathConnection;
import org.opentcs.guing.components.drawing.figures.PointFigure;
import org.opentcs.guing.components.drawing.figures.TCSLabelFigure;
import org.opentcs.guing.components.properties.event.NullAttributesChangeListener;
import org.opentcs.guing.components.properties.type.KeyValueProperty;
import org.opentcs.guing.components.properties.type.LengthProperty;
import org.opentcs.guing.exchange.adapter.ProcessAdapterUtil;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.ModelManager;
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
import org.opentcs.guing.util.CourseObjectFactory;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.opentcs.guing.util.SynchronizedFileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages (loads, persists and keeps) the driving course model.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class OpenTCSModelManager
    implements ModelManager {

  /**
   * Identifier for the default layout object.
   */
  public static final String DEFAULT_LAYOUT = "Default";
  /**
   * Directory where models will be persisted.
   */
  public static final String MODEL_DIRECTORY = "data/";
  /**
   * This class's logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(OpenTCSModelManager.class);
  /**
   * The StatusPanel at the bottom to log messages.
   */
  private final StatusPanel statusPanel;
  /**
   * A course object factory to be used.
   */
  private final CourseObjectFactory crsObjFactory;
  /**
   * Provides new instances of SystemModel.
   */
  private final Provider<SystemModel> systemModelProvider;
  /**
   * A utility class for process adapters.
   */
  private final ProcessAdapterUtil procAdapterUtil;
  /**
   * A file chooser for selecting model files to be opened.
   */
  private final JFileChooser modelReaderFileChooser;
  /**
   * A file chooser for selecting model files to be saved.
   */
  private final JFileChooser modelPersistorFileChooser;
  /**
   * Persists a model to a kernel.
   */
  private final ModelKernelPersistor kernelPersistor;
  /**
   * The file filters for different model readers that can be used to load models from a file.
   */
  private final Map<FileFilter, ModelReader> modelReaderFilter = new HashMap<>();
  /**
   * The file filters for different model persistors that can be used to save models to a file.
   */
  private final Map<FileFilter, ModelFilePersistor> modelPersistorFilter = new HashMap<>();
  /**
   * Converts model data on import.
   */
  private final ModelImportAdapter modelImportAdapter;
  /**
   * Converts model data on export.
   */
  private final ModelExportAdapter modelExportAdapter;
  /**
   * The model currently loaded.
   */
  private SystemModel systemModel;
  /**
   * The current system model's name.
   */
  private String fModelName = "";
  /**
   * The file which the current model is loaded from/written to.
   */
  private File currentModelFile;

  /**
   * Creates a new instance.
   *
   * @param crsObjFactory A course object factory to be used.
   * @param procAdapterUtil A utility class for process adapters.
   * @param systemModelProvider Provides instances of SystemModel.
   * @param statusPanel StatusPanel to log messages.
   * @param homeDir The application's home directory.
   * @param kernelPersistor Persists a model to a kernel.
   * @param modelReaders The set of model readers
   * @param modelPersistors The set of model persistors
   * @param modelImportAdapter Converts model data on import.
   * @param modelExportAdapter Converts model data on export.
   */
  @Inject
  public OpenTCSModelManager(CourseObjectFactory crsObjFactory,
                             ProcessAdapterUtil procAdapterUtil,
                             Provider<SystemModel> systemModelProvider,
                             StatusPanel statusPanel,
                             @ApplicationHome File homeDir,
                             ModelKernelPersistor kernelPersistor,
                             Set<ModelReader> modelReaders,
                             Set<ModelFilePersistor> modelPersistors,
                             ModelImportAdapter modelImportAdapter,
                             ModelExportAdapter modelExportAdapter) {
    this.crsObjFactory = requireNonNull(crsObjFactory, "crsObjFactory");
    this.procAdapterUtil = requireNonNull(procAdapterUtil, "procAdapterUtil");
    this.systemModelProvider = requireNonNull(systemModelProvider, "systemModelProvider");
    this.statusPanel = requireNonNull(statusPanel, "statusPanel");
    requireNonNull(homeDir, "homeDir");
    this.kernelPersistor = requireNonNull(kernelPersistor, "kernelPersistor");
    this.modelReaderFileChooser = new SynchronizedFileChooser(new File(homeDir, "data"));
    this.modelReaderFileChooser.setAcceptAllFileFilterUsed(false);
    modelReaders.stream().forEach(o -> {
      FileFilter filter = o.getDialogFileFilter();
      if (o instanceof UnifiedModelReader) {
        this.modelReaderFileChooser.setFileFilter(filter);
      }
      else {
        this.modelReaderFileChooser.addChoosableFileFilter(filter);
      }
      modelReaderFilter.put(filter, o);
    });
    this.modelPersistorFileChooser = new SynchronizedFileChooser(new File(homeDir, "data"));
    this.modelPersistorFileChooser.setAcceptAllFileFilterUsed(false);
    modelPersistors.stream().forEach(o -> {
      FileFilter filter = o.getDialogFileFilter();
      if (o instanceof UnifiedModelPersistor) {
        this.modelPersistorFileChooser.setFileFilter(filter);
      }
      else {
        this.modelPersistorFileChooser.addChoosableFileFilter(filter);
      }
      modelPersistorFilter.put(filter, o);
    });
    this.modelImportAdapter = requireNonNull(modelImportAdapter, "modelImportAdapter");
    this.modelExportAdapter = requireNonNull(modelExportAdapter, "modelExportAdapter");

    this.systemModel = systemModelProvider.get();
    initializeSystemModel(systemModel);
  }

  @Override
  public SystemModel getModel() {
    return systemModel;
  }

  @Override
  public boolean loadModel(@Nullable File modelFile) {
    File file = modelFile != null ? modelFile : showOpenDialog();
    if (file == null) {
      return false;
    }

    FileFilter chosenFileFilter = modelReaderFileChooser.getFileFilter();
    return loadModel(file, modelReaderFilter.get(chosenFileFilter));
  }

  @Override
  public boolean loadModel(@Nullable File modelFile, ModelReader reader) {
    requireNonNull(reader, "reader");
    File file = modelFile != null ? modelFile : showOpenDialog();
    if (file == null) {
      return false;
    }

    try {
      Optional<SystemModel> opt = reader.deserialize(file);
      if (!opt.isPresent()) {
        LOG.debug("Loading model canceled.");
        return false;
      }
      systemModel = opt.get();
      currentModelFile = file;
      initializeSystemModel(systemModel);
      return true;
    }
    catch (IOException | IllegalArgumentException ex) {
      statusPanel.setLogMessage(Level.SEVERE,
                                ResourceBundleUtil.getBundle()
                                    .getFormatted("modelManager.persistence.notLoaded",
                                                  file.getName()));
      LOG.info("Error reading file", ex);
    }
    return false;
  }

  @Override
  public boolean importModel(PlantModelImporter importer) {
    requireNonNull(importer, "importer");

    try {
      Optional<PlantModelCreationTO> opt = importer.importPlantModel();
      if (!opt.isPresent()) {
        LOG.debug("Model import cancelled.");
        return false;
      }
      SystemModel newSystemModel = modelImportAdapter.convert(opt.get());
      systemModel = newSystemModel;
      currentModelFile = null;
      initializeSystemModel(systemModel);
      return true;
    }
    catch (IOException | IllegalArgumentException ex) {
      statusPanel.setLogMessage(Level.SEVERE,
                                ResourceBundleUtil.getBundle()
                                    .getFormatted("modelManager.persistence.notImported"));
      LOG.warn("Exception importing model", ex);
      return false;
    }
  }

  @Override
  public boolean persistModel(KernelServicePortal portal) {
    try {
      fModelName = systemModel.getName();
      statusPanel.clear();
      return persistModel(systemModel, portal, kernelPersistor, false);
    }
    catch (IllegalStateException | CredentialsException e) {
      statusPanel.setLogMessage(Level.SEVERE,
                                ResourceBundleUtil.getBundle()
                                    .getString("modelManager.persistence.notSaved"));
      LOG.warn("Exception persisting model", e);
      return false;
    }
    catch (IllegalArgumentException e) {
      statusPanel.setLogMessage(Level.SEVERE,
                                e.getMessage());
      LOG.warn("Exception persisting model", e);
      return false;
    }
  }

  @Override
  public boolean persistModel(boolean chooseName) {
    fModelName = systemModel.getName();
    if (chooseName
        || currentModelFile == null
        || fModelName == null
        || fModelName.isEmpty()
        || fModelName.equals(Kernel.DEFAULT_MODEL_NAME)
        || fModelName.equals(
            ResourceBundleUtil.getBundle().getString("file.newModel.text"))) {
      File selectedFile = showSaveDialog();
      if (selectedFile == null) {
        return false;
      }
      currentModelFile = selectedFile;
    }
    try {
      statusPanel.clear();

      // Set the last-modified time stamp of the model to right now, as we're saving the model file.
      systemModel.getPropertyMiscellaneous().addItem(
          new KeyValueProperty(systemModel,
                               ObjectPropConstants.MODEL_FILE_LAST_MODIFIED,
                               Instant.now().toString())
      );

      return persistModel(systemModel,
                          currentModelFile,
                          modelPersistorFilter.get(modelPersistorFileChooser.getFileFilter()),
                          true);

    }
    catch (IOException e) {
      statusPanel.setLogMessage(Level.SEVERE,
                                ResourceBundleUtil.getBundle()
                                    .getString("modelManager.persistence.notSaved"));
      LOG.warn("Exception persisting model", e);
      return false;
    }
    catch (IllegalArgumentException e) {
      JOptionPane.showConfirmDialog(statusPanel, e.getMessage());
      return true;
    }
  }

  @Override
  public boolean exportModel(PlantModelExporter exporter) {
    requireNonNull(exporter, "exporter");

    try {
      exporter.exportPlantModel(modelExportAdapter.convert(systemModel));
      return true;
    }
    catch (IOException | IllegalArgumentException ex) {
      statusPanel.setLogMessage(Level.SEVERE,
                                ResourceBundleUtil.getBundle()
                                    .getString("modelManager.persistence.notExported"));
      LOG.warn("Exception exporting model", ex);
      return false;
    }
  }

  @Override
  public void createEmptyModel() {
    systemModel = systemModelProvider.get();
    initializeSystemModel(systemModel);
    systemModel.setName(Kernel.DEFAULT_MODEL_NAME);
    fModelName = systemModel.getName();
  }

  /**
   * Persist model with the persistor.
   *
   * @param systemModel The system model to be persisted.
   * @param persistor The persistor to be used.
   * @param ignoreError whether the model should be persisted when duplicates exist
   * @return Whether the model was actually saved.
   */
  private boolean persistModel(SystemModel systemModel,
                               File file,
                               ModelFilePersistor persistor,
                               boolean ignoreError)
      throws IOException, KernelRuntimeException {
    requireNonNull(systemModel, "systemModel");
    requireNonNull(persistor, "persistor");

    if (!persistor.serialize(systemModel, fModelName, file, ignoreError)) {
      return false;
    }

    systemModel.setName(fModelName);
    return true;
  }

  /**
   * Persist model with the persistor.
   *
   * @param systemModel The system model to be persisted.
   * @param persistor The persistor to be used.
   * @param ignoreError whether the model should be persisted when duplicates exist
   * @return Whether the model was actually saved.
   * @throws IllegalStateException If there was a problem persisting the model
   */
  private boolean persistModel(SystemModel systemModel,
                               KernelServicePortal portal,
                               ModelKernelPersistor persistor,
                               boolean ignoreError)
      throws IllegalStateException, KernelRuntimeException {
    requireNonNull(systemModel, "systemModel");
    requireNonNull(persistor, "persistor");

    if (!persistor.persist(systemModel, portal, ignoreError)) {
      return false;
    }

    systemModel.setName(fModelName);
    return true;
  }

  @Override
  public void restoreModel() {
    fModelName = systemModel.getName();
    List<Figure> restoredFigures = new ArrayList<>();

    LayoutModel layoutModel = (LayoutModel) systemModel.getMainFolder(SystemModel.FolderKey.LAYOUT);
    double scaleX = layoutModel.getPropertyScaleX().getValueByUnit(LengthProperty.Unit.MM);
    double scaleY = layoutModel.getPropertyScaleY().getValueByUnit(LengthProperty.Unit.MM);

    // Create figures and process adapters
    restoredFigures.addAll(restorePointsInModel(systemModel.getPointModels(), scaleX, scaleY));
    restoredFigures.addAll(restorePathsInModel(systemModel.getPathModels(), systemModel));
    restoredFigures.addAll(restoreLocationsInModel(systemModel.getLocationModels(),
                                                   scaleX,
                                                   scaleY,
                                                   systemModel));
    restoredFigures.addAll(restoreBlocksInModel(systemModel.getBlockModels(), systemModel));
    restoredFigures.addAll(restoreStaticRoutesInModel(systemModel.getStaticRouteModels(), systemModel));
    restoredFigures.addAll(restoreGroupsInModel(systemModel.getGroupModels(), systemModel));

    // Associate all created figures with the origin.
    Origin origin = systemModel.getDrawingMethod().getOrigin();
    for (Figure figure : restoredFigures) {
      if (figure instanceof OriginChangeListener) {
        origin.addListener((OriginChangeListener) figure);
        figure.set(FigureConstants.ORIGIN, origin);
      }
    }

    long timeBefore = System.currentTimeMillis();
    systemModel.getDrawing().addAll(restoredFigures);
    LOG.debug("Adding figures to drawing took {} ms.", System.currentTimeMillis() - timeBefore);
  }

  @Override
  public void restoreModel(KernelServicePortal portal) {
    requireNonNull(portal, "portal");

    createEmptyModel();

    fModelName = portal.getPlantModelService().getModelName();
    systemModel.getPropertyName().setText(fModelName);
    portal.getPlantModelService().getModelProperties().entrySet().stream()
        .forEach(
            entry -> systemModel.getPropertyMiscellaneous().addItem(
                new KeyValueProperty(systemModel, entry.getKey(), entry.getValue())
            )
        );

    TCSObjectService objectService = (TCSObjectService) portal.getPlantModelService();

    Set<VisualLayout> allVisualLayouts = objectService.fetchObjects(VisualLayout.class);
    Set<Vehicle> allVehicles = objectService.fetchObjects(Vehicle.class);
    Set<Point> allPoints = objectService.fetchObjects(Point.class);
    Set<LocationType> allLocationTypes = objectService.fetchObjects(LocationType.class);
    Set<Location> allLocations = objectService.fetchObjects(Location.class);
    Set<Path> allPaths = objectService.fetchObjects(Path.class);
    Set<Block> allBlocks = objectService.fetchObjects(Block.class);
    @SuppressWarnings("deprecation")
    Set<org.opentcs.data.model.StaticRoute> allStaticRoutes
        = objectService.fetchObjects(org.opentcs.data.model.StaticRoute.class);
    Set<Group> allGroups = objectService.fetchObjects(Group.class);

    List<Figure> restoredFigures = new ArrayList<>();

    Origin origin = systemModel.getDrawingMethod().getOrigin();

    VisualLayout visualLayout = null;
    for (VisualLayout visLayout : allVisualLayouts) {
      visualLayout = visLayout;  // Es sollte genau ein Layout geben
      systemModel.createLayoutMap(visualLayout,
                                  allPoints,
                                  allPaths,
                                  allLocations,
                                  allBlocks,
                                  allVehicles);
      if (visualLayout.getScaleX() != 0.0 && visualLayout.getScaleY() != 0.0) {
        origin.setScale(visualLayout.getScaleX(), visualLayout.getScaleY());
      }
    }

    LayoutModel layoutModel
        = (LayoutModel) systemModel.getMainFolder(SystemModel.FolderKey.LAYOUT);

    prepareLayout(layoutModel, systemModel, origin, objectService, visualLayout);

    restoreModelPoints(allPoints, systemModel, origin, restoredFigures, objectService);
    restoreModelPaths(allPaths, systemModel, origin, restoredFigures, objectService);
    restoreModelVehicles(allVehicles, systemModel, objectService);
    restoreModelLocationTypes(allLocationTypes, systemModel, objectService);
    restoreModelLocations(allLocations, systemModel, origin, restoredFigures, objectService);
    restoreModelBlocks(allBlocks, systemModel, objectService);
    restoreModelStaticRoutes(allStaticRoutes, systemModel, objectService);
    restoreModelGroups(allGroups, systemModel, objectService);

    systemModel.getDrawing().addAll(restoredFigures);
  }

  private void prepareLayout(LayoutModel layoutModel,
                             SystemModel systemModel,
                             Origin origin,
                             TCSObjectService objectService,
                             @Nullable VisualLayout layout) {
    layoutModel.getPropertyName().setText("VLayout");
    layoutModel.getPropertyScaleX().setValueAndUnit(origin.getScaleX(),
                                                    LengthProperty.Unit.MM);
    layoutModel.getPropertyScaleY().setValueAndUnit(origin.getScaleY(),
                                                    LengthProperty.Unit.MM);

    if (layout != null) {
      procAdapterUtil.processAdapterFor(layoutModel)
          .updateModelProperties(layout, layoutModel, systemModel, objectService, null);
    }
  }

  private List<Figure> restoreGroupsInModel(List<GroupModel> groupModels,
                                            SystemModel systemModel) {
    for (GroupModel groupModel : groupModels) {
      // XXX This should probably be done when the group model is created, not here.
      for (String elementName : groupModel.getPropertyElements().getItems()) {
        ModelComponent modelComponent = getGroupMember(systemModel, elementName);
        if (modelComponent != null) {
          groupModel.add(modelComponent);
        }
      }
    }

    return new ArrayList<>();
  }

  private void restoreModelGroups(Set<Group> allGroups, SystemModel systemModel,
                                  TCSObjectService objectService) {
    for (Group group : allGroups) {
      GroupModel groupModel = new GroupModel(group.getName());

      procAdapterUtil.processAdapterFor(groupModel)
          .updateModelProperties(group, groupModel, systemModel, objectService, null);

      for (TCSObjectReference<?> ref : group.getMembers()) {
        ModelComponent component = systemModel.getModelComponent(ref.getName());
        if (component != null) {
          groupModel.add(component);
        }
      }

      systemModel.getMainFolder(SystemModel.FolderKey.GROUPS).add(groupModel);
    }
  }

  private List<Figure> restoreStaticRoutesInModel(List<StaticRouteModel> staticRouteModels,
                                                  SystemModel systemModel) {
    for (StaticRouteModel staticRouteModel : staticRouteModels) {
      // XXX This should probably be done when the route model is created, not here.
      for (String elementName : staticRouteModel.getPropertyElements().getItems()) {
        PointModel modelComponent = getPointComponent(systemModel, elementName);
        if (modelComponent != null) {
          staticRouteModel.addPoint(modelComponent);
        }
      }
    }

    return new ArrayList<>();
  }

  @SuppressWarnings("deprecation")
  private void restoreModelStaticRoutes(Set<org.opentcs.data.model.StaticRoute> allStaticRoutes,
                                        SystemModel systemModel,
                                        TCSObjectService objectService) {
    for (org.opentcs.data.model.StaticRoute staticRoute : allStaticRoutes) {
      StaticRouteModel staticRouteModel = crsObjFactory.createStaticRouteModel();

      ModelLayoutElement element = systemModel.getLayoutMap().get(staticRoute.getReference());
      procAdapterUtil.processAdapterFor(staticRouteModel)
          .updateModelProperties(staticRoute, staticRouteModel, systemModel, objectService, element);

      systemModel.getMainFolder(SystemModel.FolderKey.STATIC_ROUTES).add(staticRouteModel);
    }
  }

  private List<Figure> restoreBlocksInModel(List<BlockModel> blockModels, SystemModel systemModel) {
    for (BlockModel blockModel : blockModels) {
      // XXX This should probably be done when the block model is created, not here.
      for (String elementName : blockModel.getPropertyElements().getItems()) {
        ModelComponent modelComponent = getBlockMember(systemModel, elementName);
        if (modelComponent != null) {
          blockModel.addCourseElement(modelComponent);
        }
      }
    }

    return new ArrayList<>();
  }

  private void restoreModelBlocks(Set<Block> allBlocks, SystemModel systemModel,
                                  TCSObjectService objectService) {
    for (Block block : allBlocks) {
      BlockModel blockModel = crsObjFactory.createBlockModel();

      // Das zugeh�rige Model Layout Element suchen
      ModelLayoutElement element = systemModel.getLayoutMap().get(block.getReference());
      procAdapterUtil.processAdapterFor(blockModel)
          .updateModelProperties(block, blockModel, systemModel, objectService, element);

      systemModel.getMainFolder(SystemModel.FolderKey.BLOCKS).add(blockModel);
    }
  }

  private List<Figure> restoreLocationsInModel(List<LocationModel> locationModels,
                                               double scaleX,
                                               double scaleY,
                                               SystemModel systemModel) {
    List<Figure> restoredFigures = new ArrayList<>(locationModels.size());

    for (LocationModel locationModel : locationModels) {
      LabeledLocationFigure llf = createLocationFigure(locationModel, scaleX, scaleY);

      locationModel.setFigure(llf);
      locationModel.addAttributesChangeListener(llf);

      String locationTypeName = (String) locationModel.getPropertyType().getValue();
      locationModel.setLocationType(getLocationTypeComponent(systemModel, locationTypeName));

      for (LinkModel linkModel : getAttachedLinks(systemModel, locationModel)) {
        LinkConnection linkConnection = createLinkFigure(linkModel, llf);

        linkModel.setFigure(linkConnection);
        linkModel.addAttributesChangeListener(linkConnection);
        restoredFigures.add(linkConnection);
      }

      locationModel.propertiesChanged(new NullAttributesChangeListener());
      restoredFigures.add(llf);
    }

    return restoredFigures;
  }

  private LabeledLocationFigure createLocationFigure(LocationModel locationModel,
                                                     double scaleX,
                                                     double scaleY) {
    LabeledLocationFigure llf = crsObjFactory.createLocationFigure();
    LocationFigure locationFigure = llf.getPresentationFigure();
    locationFigure.set(FigureConstants.MODEL, locationModel);

    // The corresponding label
    TCSLabelFigure label = new TCSLabelFigure(locationModel.getName());

    Point2D.Double labelPosition;
    Point2D.Double figurePosition;
    double figurePositionX = 0;
    double figurePositionY = 0;
    String locPosX = locationModel.getPropertyLayoutPositionX().getText();
    String locPosY = locationModel.getPropertyLayoutPositionY().getText();
    if (locPosX != null && locPosY != null) {
      try {
        figurePositionX = Integer.parseInt(locPosX);
        figurePositionY = Integer.parseInt(locPosY);
      }
      catch (NumberFormatException ex) {
      }
    }

    // Label
    String labelOffsetX = locationModel.getPropertyLabelOffsetX().getText();
    String labelOffsetY = locationModel.getPropertyLabelOffsetY().getText();
    // TODO: labelOrientationAngle auswerten
//      String labelOrientationAngle = layoutProperties.get(ElementPropKeys.POINT_LABEL_ORIENTATION_ANGLE);

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
    figurePosition = new Point2D.Double(figurePositionX / scaleX, -figurePositionY / scaleY);  // Vorzeichen!
    locationFigure.setBounds(figurePosition, figurePosition);

    labelPosition = locationFigure.getStartPoint();
    labelPosition.x += label.getOffset().x;
    labelPosition.y += label.getOffset().y;
    label.setBounds(labelPosition, null);
    llf.setLabel(label);

    return llf;
  }

  private LinkConnection createLinkFigure(LinkModel linkModel, LabeledLocationFigure llf) {
    PointModel pointModel = linkModel.getPoint();
    LabeledPointFigure lpf = pointModel.getFigure();
    LinkConnection linkConnection = crsObjFactory.createLinkConnection();
    linkConnection.set(FigureConstants.MODEL, linkModel);
    linkConnection.connect(lpf, llf);
    linkConnection.getModel().updateName();

    return linkConnection;
  }

  private void restoreModelLocations(Set<Location> allLocations,
                                     SystemModel systemModel,
                                     Origin origin,
                                     List<Figure> restoredFigures,
                                     TCSObjectService objectService) {
    for (Location location : allLocations) {
      LocationModel locationModel = new LocationModel();

      ModelLayoutElement layoutElement = systemModel.getLayoutMap().get(location.getReference());
      procAdapterUtil.processAdapterFor(locationModel)
          .updateModelProperties(location, locationModel, systemModel, objectService, layoutElement);

      LabeledLocationFigure llf = createLocationFigure(locationModel,
                                                       origin.getScaleX(),
                                                       origin.getScaleY());

      locationModel.setFigure(llf);
      locationModel.addAttributesChangeListener(llf);
      systemModel.getMainFolder(SystemModel.FolderKey.LOCATIONS).add(locationModel);
      restoredFigures.add(llf);

      LocationTypeModel type = getLocationTypeComponent(systemModel, location.getType().getName());
      locationModel.setLocationType(type);
      locationModel.updateTypeProperty(systemModel.getLocationTypeModels());
      locationModel.propertiesChanged(new NullAttributesChangeListener());

      for (Link link : location.getAttachedLinks()) {
        PointModel pointModel = getPointComponent(systemModel, link.getPoint().getName());

        LinkModel linkModel = new LinkModel();

        linkModel.setConnectedComponents(pointModel, locationModel);
        linkModel.updateName();
        linkModel.getPropertyStartComponent().setText(pointModel.getName());
        linkModel.getPropertyEndComponent().setText(locationModel.getName());
        linkModel.getPropertyAllowedOperations()
            .setItems(new ArrayList<>(link.getAllowedOperations()));

        LinkConnection linkConnection = createLinkFigure(linkModel, llf);

        linkModel.setFigure(linkConnection);
        linkModel.addAttributesChangeListener(linkConnection);
        systemModel.getMainFolder(SystemModel.FolderKey.LINKS).add(linkModel);
        restoredFigures.add(linkConnection);
      }

      origin.addListener(llf);
      llf.set(FigureConstants.ORIGIN, origin);
    }
  }

  private void restoreModelLocationTypes(Set<LocationType> allLocationTypes,
                                         SystemModel systemModel,
                                         TCSObjectService objectService) {
    for (LocationType locationType : allLocationTypes) {
      LocationTypeModel locationTypeModel = crsObjFactory.createLocationTypeModel();

      procAdapterUtil.processAdapterFor(locationTypeModel)
          .updateModelProperties(locationType, locationTypeModel, systemModel, objectService, null);
      systemModel.getMainFolder(SystemModel.FolderKey.LOCATION_TYPES).add(locationTypeModel);
    }
  }

  private void restoreModelVehicles(Set<Vehicle> allVehicles,
                                    SystemModel systemModel,
                                    TCSObjectService objectService) {
    for (Vehicle vehicle : allVehicles) {
      VehicleModel vehicleModel = crsObjFactory.createVehicleModel();
      vehicleModel.setVehicle(vehicle);

      ModelLayoutElement element = systemModel.getLayoutMap().get(vehicle.getReference());
      procAdapterUtil.processAdapterFor(vehicleModel)
          .updateModelProperties(vehicle, vehicleModel, systemModel, objectService, element);

      systemModel.getMainFolder(SystemModel.FolderKey.VEHICLES).add(vehicleModel);
      // VehicleFigures will be created in OpenTCSDrawingView.setVehicles().
    }
  }

  private List<Figure> restorePathsInModel(List<PathModel> paths, SystemModel systemModel) {
    List<Figure> restoredFigures = new ArrayList<>(paths.size());

    for (PathModel pathModel : paths) {
      PathConnection pathFigure = createPathFigure(pathModel, systemModel);

      pathModel.setFigure(pathFigure);
      pathModel.addAttributesChangeListener(pathFigure);

      restoredFigures.add(pathFigure);
    }

    return restoredFigures;
  }

  private PathConnection createPathFigure(PathModel pathModel, SystemModel systemModel) {
    PathConnection pathFigure = crsObjFactory.createPathConnection();

    pathFigure.set(FigureConstants.MODEL, pathModel);
    PointModel startPointModel = getPointComponent(systemModel,
                                                   pathModel.getPropertyStartComponent().getText());
    PointModel endPointModel = getPointComponent(systemModel,
                                                 pathModel.getPropertyEndComponent().getText());
    if (startPointModel != null && endPointModel != null) {
      pathFigure.connect(startPointModel.getFigure(), endPointModel.getFigure());
    }

    PathModel.LinerType connectionType
        = (PathModel.LinerType) pathModel.getPropertyPathConnType().getValue();

    if (connectionType != null) {
      initPathControlPoints(connectionType,
                            pathModel.getPropertyPathControlPoints().getText(),
                            pathFigure);
      pathFigure.setLinerByType(connectionType);
    }

    pathFigure.updateDecorations();

    return pathFigure;
  }

  private void restoreModelPaths(Set<Path> allPaths, SystemModel systemModel,
                                 Origin origin,
                                 List<Figure> restoredFigures,
                                 TCSObjectService objectService) {
    for (Path path : allPaths) {
      PathModel pathModel = new PathModel();

      ModelLayoutElement layoutElement = systemModel.getLayoutMap().get(path.getReference());
      procAdapterUtil.processAdapterFor(pathModel)
          .updateModelProperties(path, pathModel, systemModel, objectService, layoutElement);

      PathConnection pathFigure = createPathFigure(pathModel, systemModel);

      pathModel.setFigure(pathFigure);
      pathModel.addAttributesChangeListener(pathFigure);
      systemModel.getMainFolder(SystemModel.FolderKey.PATHS).add(pathModel);
      restoredFigures.add(pathFigure);
      // Koordinaten der Kontrollpunkte �ndern sich, wenn der Ma�stab ver�ndert wird
      origin.addListener(pathFigure);
      pathFigure.set(FigureConstants.ORIGIN, origin);
    }
  }

  private void initPathControlPoints(PathModel.LinerType connectionType,
                                     String sControlPoints,
                                     PathConnection pathFigure) {
    if (connectionType != PathModel.LinerType.BEZIER
        && connectionType != PathModel.LinerType.BEZIER_3) {
      return;
    }
    if (Strings.isNullOrEmpty(sControlPoints)) {
      return;
    }

    // Format: x1,y1 or x1,y1;x2,y2
    String[] values = sControlPoints.split("[,;]");

    try {
      if (values.length >= 2) {
        int xcp1 = (int) Double.parseDouble(values[0]);
        int ycp1 = (int) Double.parseDouble(values[1]);
        Point2D.Double cp1 = new Point2D.Double(xcp1, ycp1);

        if (values.length >= 4) {
          int xcp2 = (int) Double.parseDouble(values[2]);
          int ycp2 = (int) Double.parseDouble(values[3]);
          Point2D.Double cp2 = new Point2D.Double(xcp2, ycp2);

          if (values.length >= 10) {
            int xcp3 = (int) Double.parseDouble(values[4]);
            int ycp3 = (int) Double.parseDouble(values[5]);
            int xcp4 = (int) Double.parseDouble(values[6]);
            int ycp4 = (int) Double.parseDouble(values[7]);
            int xcp5 = (int) Double.parseDouble(values[8]);
            int ycp5 = (int) Double.parseDouble(values[9]);
            Point2D.Double cp3 = new Point2D.Double(xcp3, ycp3);
            Point2D.Double cp4 = new Point2D.Double(xcp4, ycp4);
            Point2D.Double cp5 = new Point2D.Double(xcp5, ycp5);
            pathFigure.addControlPoints(cp1, cp2, cp3, cp4, cp5);
          }
          else {
            pathFigure.addControlPoints(cp1, cp2);  // Cubic curve
          }
        }
        else {
          pathFigure.addControlPoints(cp1, cp1);  // Quadratic curve
        }
      }
    }
    catch (NumberFormatException nfex) {
      LOG.info("Error while parsing bezier control points.", nfex);
    }
  }

  private List<Figure> restorePointsInModel(List<PointModel> points,
                                            double scaleX,
                                            double scaleY) {
    List<Figure> restoredFigures = new ArrayList<>(points.size());

    for (PointModel pointModel : points) {
      LabeledPointFigure lpf = createPointFigure(pointModel, scaleX, scaleY);

      pointModel.setFigure(lpf);
      pointModel.addAttributesChangeListener(lpf);
      restoredFigures.add(lpf);
    }

    return restoredFigures;
  }

  private LabeledPointFigure createPointFigure(PointModel pointModel,
                                               double scaleX,
                                               double scaleY) {
    LabeledPointFigure lpf = crsObjFactory.createPointFigure();
    PointFigure pointFigure = lpf.getPresentationFigure();
    pointFigure.setModel(pointModel);

    // The corresponding label
    TCSLabelFigure label = new TCSLabelFigure(pointModel.getName());

    Point2D.Double labelPosition;
    Point2D.Double figurePosition;
    double figurePositionX = 0;
    double figurePositionY = 0;
    String pointPosX = pointModel.getPropertyLayoutPosX().getText();
    String pointPosY = pointModel.getPropertyLayoutPosY().getText();
    if (pointPosX != null && pointPosY != null) {
      try {
        figurePositionX = Integer.parseInt(pointPosX);
        figurePositionY = Integer.parseInt(pointPosY);
      }
      catch (NumberFormatException ex) {
      }
    }

    // Label
    String labelOffsetX = pointModel.getPropertyPointLabelOffsetX().getText();
    String labelOffsetY = pointModel.getPropertyPointLabelOffsetY().getText();
    // TODO: labelOrientationAngle auswerten
//      String labelOrientationAngle = layoutProperties.get(ElementPropKeys.POINT_LABEL_ORIENTATION_ANGLE);

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
    // Figur auf diese Position verschieben
    figurePosition = new Point2D.Double(figurePositionX / scaleX, -figurePositionY / scaleY);  // Vorzeichen!
    pointFigure.setBounds(figurePosition, figurePosition);

    labelPosition = pointFigure.getStartPoint();
    labelPosition.x += label.getOffset().x;
    labelPosition.y += label.getOffset().y;
    label.setBounds(labelPosition, null);
    lpf.setLabel(label);

    return lpf;
  }

  private void restoreModelPoints(Set<Point> allPoints, SystemModel systemModel,
                                  Origin origin,
                                  List<Figure> restoredFigures,
                                  TCSObjectService objectService) {
    for (Point point : allPoints) {
      PointModel pointModel = new PointModel();

      // Das zugeh�rige Model Layout Element suchen und mit dem Adapter verkn�pfen
      ModelLayoutElement layoutElement = systemModel.getLayoutMap().get(point.getReference());

      // Setze Typ, Koordinaten, ... aus dem Kernel-Modell
      procAdapterUtil.processAdapterFor(pointModel)
          .updateModelProperties(point, pointModel, systemModel, objectService, layoutElement);

      LabeledPointFigure lpf = createPointFigure(pointModel,
                                                 origin.getScaleX(),
                                                 origin.getScaleY());

      pointModel.setFigure(lpf);
      pointModel.addAttributesChangeListener(lpf);
      systemModel.getMainFolder(SystemModel.FolderKey.POINTS).add(pointModel);
      restoredFigures.add(lpf);

      // Koordinaten der Punkte �ndern sich, wenn der Ma�stab ver�ndert wird
      origin.addListener(lpf);
      lpf.set(FigureConstants.ORIGIN, origin);
    }
  }

  /**
   * Shows a dialog to select a model to load.
   *
   * @return The selected file or <code>null</code>, if nothing was selected.
   */
  private File showOpenDialog() {
    if (!modelReaderFileChooser.getCurrentDirectory().isDirectory()) {
      modelReaderFileChooser.getCurrentDirectory().mkdir();
    }
    if (modelReaderFileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
      return null;
    }
    return modelReaderFileChooser.getSelectedFile();
  }

  /**
   * Shows a dialog to save a model locally.
   *
   * @return The selected file or <code>null</code>, if nothing was selected.
   */
  private File showSaveDialog() {
    if (!modelPersistorFileChooser.getCurrentDirectory().isDirectory()) {
      modelPersistorFileChooser.getCurrentDirectory().mkdir();
    }
    if (modelPersistorFileChooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
      fModelName = Kernel.DEFAULT_MODEL_NAME;
      return null;
    }

    File selectedFile = modelPersistorFileChooser.getSelectedFile();

    // Extract the model name from the file name, but without the extension.
    fModelName = selectedFile.getName().replaceFirst("[.][^.]+$", "");
    if (fModelName.isEmpty()) {
      fModelName = Kernel.DEFAULT_MODEL_NAME;
      return null;
    }

    return selectedFile;
  }

  /**
   * Return the point model component with the given name from the
   * system model.
   *
   * @param name The name of the point to return.
   * @return The PointModel that matches the given name.
   */
  private PointModel getPointComponent(SystemModel systemModel, String name) {
    for (PointModel modelComponent : systemModel.getPointModels()) {
      if (modelComponent.getName().equals(name)) {
        return modelComponent;
      }
    }
    return null;
  }

  /**
   * Returns the location type model component with the given name from the
   * system model.
   *
   * @param name The name of the location type to return.
   * @return The LocationModel that matches the given name.
   */
  private LocationTypeModel getLocationTypeComponent(SystemModel systemModel, String name) {
    for (LocationTypeModel modelComponent : systemModel.getLocationTypeModels()) {
      if (modelComponent.getName().equals(name)) {
        return modelComponent;
      }
    }
    return null;
  }

  /**
   * Returns a <code>ModelComponent</code> with the given name that is
   * a member of a block.
   *
   * @param name The name of the ModelComponent to return.
   * @return The ModelComponent.
   */
  private ModelComponent getBlockMember(SystemModel systemModel, String name) {
    for (PointModel pModel : systemModel.getPointModels()) {
      if (name.equals(pModel.getName())) {
        return pModel;
      }
    }
    for (PathModel pModel : systemModel.getPathModels()) {
      if (name.equals(pModel.getName())) {
        return pModel;
      }
    }
    for (LocationModel lModel : systemModel.getLocationModels()) {
      if (name.equals(lModel.getName())) {
        return lModel;
      }
    }
    return null;
  }

  /**
   * Returns a <code>ModelComponent</code> with the given name that is
   * a member of a group.
   *
   * @param name The name of the ModelComponent to return.
   * @return The ModelComponent.
   */
  private ModelComponent getGroupMember(SystemModel systemModel, String name) {
    return getBlockMember(systemModel, name);
  }

  /**
   * Returns the attached links to the given location model. After persisting
   * the LinkModels in the system model contain the names of the
   * connected components in the specific properties. The components are
   * searched here and are set as the connected components in the link.
   *
   * @param locationModel The LocationModel for which we need the connected
   * links.
   * @return A list with the connected links.
   */
  private List<LinkModel> getAttachedLinks(SystemModel systemModel, LocationModel locationModel) {
    List<LinkModel> links = new ArrayList<>();
    String locationName = locationModel.getName();
    for (LinkModel link : systemModel.getLinkModels()) {
      if (link.getPropertyStartComponent().getText().equals(locationName)) {
        PointModel pointModel = getPointComponent(systemModel,
                                                  link.getPropertyEndComponent().getText());
        link.setConnectedComponents(pointModel, locationModel);
        link.updateName();
        links.add(link);
      }
      else if (link.getPropertyEndComponent().getText().equals(locationName)) {
        PointModel pointModel = getPointComponent(systemModel,
                                                  link.getPropertyStartComponent().getText());
        link.setConnectedComponents(pointModel, locationModel);
        link.updateName();
        links.add(link);
      }
    }

    return links;
  }

  private void initializeSystemModel(SystemModel systemModel) {
    List<LayoutModel> layoutModels = systemModel.getLayoutModels();

    if (!layoutModels.isEmpty()) {
      LayoutModel layoutModel = layoutModels.get(0);

      LengthProperty pScaleX = layoutModel.getPropertyScaleX();
      if (pScaleX.getValueByUnit(LengthProperty.Unit.MM) == 0) {
        pScaleX.setValueAndUnit(Origin.DEFAULT_SCALE, LengthProperty.Unit.MM);
      }

      LengthProperty pScaleY = layoutModel.getPropertyScaleY();
      if (pScaleY.getValueByUnit(LengthProperty.Unit.MM) == 0) {
        pScaleY.setValueAndUnit(Origin.DEFAULT_SCALE, LengthProperty.Unit.MM);
      }

      systemModel.getDrawingMethod().getOrigin()
          .setScale(pScaleX.getValueByUnit(LengthProperty.Unit.MM),
                    pScaleY.getValueByUnit(LengthProperty.Unit.MM));
    }
  }
}
