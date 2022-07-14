/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.persistence;

import com.google.common.base.Strings;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.jhotdraw.draw.Figure;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.KernelServicePortal;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.customizations.ApplicationHome;
import org.opentcs.data.ObjectPropConstants;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.Location;
import org.opentcs.data.model.Location.Link;
import org.opentcs.data.model.LocationType;
import org.opentcs.data.model.ModelConstants;
import org.opentcs.data.model.Path;
import org.opentcs.data.model.Point;
import org.opentcs.data.model.Vehicle;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.guing.base.components.properties.event.NullAttributesChangeListener;
import org.opentcs.guing.base.components.properties.type.KeyValueProperty;
import org.opentcs.guing.base.components.properties.type.LengthProperty;
import org.opentcs.guing.base.model.FigureDecorationDetails;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.elements.BlockModel;
import org.opentcs.guing.base.model.elements.LayoutModel;
import org.opentcs.guing.base.model.elements.LinkModel;
import org.opentcs.guing.base.model.elements.LocationModel;
import org.opentcs.guing.base.model.elements.LocationTypeModel;
import org.opentcs.guing.base.model.elements.PathModel;
import org.opentcs.guing.base.model.elements.PointModel;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.application.ModelRestorationProgressStatus;
import org.opentcs.guing.common.application.ProgressIndicator;
import org.opentcs.guing.common.application.StatusPanel;
import org.opentcs.guing.common.components.drawing.course.Origin;
import org.opentcs.guing.common.components.drawing.course.OriginChangeListener;
import org.opentcs.guing.common.components.drawing.figures.FigureConstants;
import org.opentcs.guing.common.components.drawing.figures.LabeledLocationFigure;
import org.opentcs.guing.common.components.drawing.figures.LabeledPointFigure;
import org.opentcs.guing.common.components.drawing.figures.LinkConnection;
import org.opentcs.guing.common.components.drawing.figures.LocationFigure;
import org.opentcs.guing.common.components.drawing.figures.PathConnection;
import org.opentcs.guing.common.components.drawing.figures.PointFigure;
import org.opentcs.guing.common.components.drawing.figures.TCSLabelFigure;
import org.opentcs.guing.common.exchange.adapter.ProcessAdapterUtil;
import org.opentcs.guing.common.model.SystemModel;
import org.opentcs.guing.common.util.CourseObjectFactory;
import static org.opentcs.guing.common.util.I18nPlantOverview.STATUS_PATH;
import org.opentcs.guing.common.util.ModelComponentFactory;
import org.opentcs.guing.common.util.SynchronizedFileChooser;
import org.opentcs.thirdparty.guing.common.jhotdraw.util.ResourceBundleUtil;
import static org.opentcs.util.Assertions.checkState;
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
   * The model component factory to be used.
   */
  private final ModelComponentFactory modelComponentFactory;
  /**
   * Provides new instances of SystemModel.
   */
  private final Provider<SystemModel> systemModelProvider;
  /**
   * A utility class for process adapters.
   */
  private final ProcessAdapterUtil procAdapterUtil;
  /**
   * A file chooser for selecting model files to be saved.
   */
  private final JFileChooser modelPersistorFileChooser;
  /**
   * The file filters for different model persistors that can be used to save models to a file.
   */
  private final ModelFilePersistor modelPersistor;
  /**
   * Converts model data on export.
   */
  private final ModelExportAdapter modelExportAdapter;
  /**
   * The progress indicator to be used.
   */
  private final ProgressIndicator progressIndicator;
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
   * @param modelComponentFactory The model component factory to be used.
   * @param procAdapterUtil A utility class for process adapters.
   * @param systemModelProvider Provides instances of SystemModel.
   * @param statusPanel StatusPanel to log messages.
   * @param homeDir The application's home directory.
   * @param modelPersistor The model persistor.
   * @param modelExportAdapter Converts model data on export.
   * @param progressIndicator The progress indicator to be used.
   */
  @Inject
  public OpenTCSModelManager(CourseObjectFactory crsObjFactory,
                             ModelComponentFactory modelComponentFactory,
                             ProcessAdapterUtil procAdapterUtil,
                             Provider<SystemModel> systemModelProvider,
                             StatusPanel statusPanel,
                             @ApplicationHome File homeDir,
                             ModelFilePersistor modelPersistor,
                             ModelExportAdapter modelExportAdapter,
                             ProgressIndicator progressIndicator) {
    this.crsObjFactory = requireNonNull(crsObjFactory, "crsObjFactory");
    this.modelComponentFactory = requireNonNull(modelComponentFactory, "modelComponentFactory");
    this.procAdapterUtil = requireNonNull(procAdapterUtil, "procAdapterUtil");
    this.systemModelProvider = requireNonNull(systemModelProvider, "systemModelProvider");
    this.statusPanel = requireNonNull(statusPanel, "statusPanel");
    requireNonNull(homeDir, "homeDir");

    this.modelPersistor = requireNonNull(modelPersistor, "modelPersistor");
    this.modelPersistorFileChooser = new SynchronizedFileChooser(new File(homeDir, "data"));
    this.modelPersistorFileChooser.setAcceptAllFileFilterUsed(false);
    this.modelPersistorFileChooser.setFileFilter(modelPersistor.getDialogFileFilter());

    this.modelExportAdapter = requireNonNull(modelExportAdapter, "modelExportAdapter");
    this.progressIndicator = progressIndicator;

    this.systemModel = systemModelProvider.get();
    initializeSystemModel(systemModel);
  }

  @Override
  public SystemModel getModel() {
    return systemModel;
  }

  @Override
  public boolean saveModelToFile(boolean chooseName) {
    fModelName = systemModel.getName();
    if (chooseName
        || currentModelFile == null
        || fModelName == null
        || fModelName.isEmpty()
        || fModelName.equals(Kernel.DEFAULT_MODEL_NAME)) {
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
                               Instant.now().truncatedTo(ChronoUnit.SECONDS).toString())
      );

      return persistModel(systemModel,
                          currentModelFile,
                          modelPersistor);

    }
    catch (IOException e) {
      statusPanel.setLogMessage(Level.SEVERE,
                                ResourceBundleUtil.getBundle(STATUS_PATH)
                                    .getString("openTcsModelManager.message_notSaved.text"));
      LOG.warn("Exception persisting model", e);
      return false;
    }
    catch (IllegalArgumentException e) {
      JOptionPane.showConfirmDialog(statusPanel, e.getMessage());
      return true;
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
                               ModelFilePersistor persistor)
      throws IOException, KernelRuntimeException {
    requireNonNull(systemModel, "systemModel");
    requireNonNull(persistor, "persistor");

    if (!persistor.serialize(modelExportAdapter.convert(systemModel), file)) {
      return false;
    }

    systemModel.setName(fModelName);
    return true;
  }

  @Override
  public void restoreModel() {
    fModelName = systemModel.getName();

    LayoutModel layoutModel = (LayoutModel) systemModel.getMainFolder(SystemModel.FolderKey.LAYOUT);
    double scaleX = layoutModel.getPropertyScaleX().getValueByUnit(LengthProperty.Unit.MM);
    double scaleY = layoutModel.getPropertyScaleY().getValueByUnit(LengthProperty.Unit.MM);

    Origin origin = systemModel.getDrawingMethod().getOrigin();

    // Create figures and process adapters
    progressIndicator.setProgress(ModelRestorationProgressStatus.LOADING_POINTS);
    List<Figure> points = restorePointsInModel(systemModel.getPointModels(), scaleX, scaleY);
    systemModel.getDrawing().addAll(associateFiguresWithOrigin(points, origin));

    progressIndicator.setProgress(ModelRestorationProgressStatus.LOADING_PATHS);
    List<Figure> paths = restorePathsInModel(systemModel.getPathModels(), systemModel);
    systemModel.getDrawing().addAll(associateFiguresWithOrigin(paths, origin));

    progressIndicator.setProgress(ModelRestorationProgressStatus.LOADING_LOCATIONS);
    List<Figure> locations = restoreLocationsInModel(systemModel.getLocationModels(),
                                                     scaleX,
                                                     scaleY,
                                                     systemModel);
    systemModel.getDrawing().addAll(associateFiguresWithOrigin(locations, origin));

    progressIndicator.setProgress(ModelRestorationProgressStatus.LOADING_BLOCKS);
    List<Figure> blocks = restoreBlocksInModel(systemModel.getBlockModels(), systemModel);
    systemModel.getDrawing().addAll(associateFiguresWithOrigin(blocks, origin));
    restoreBlockComponentDecorationDetails(systemModel);
  }

  private List<Figure> associateFiguresWithOrigin(List<Figure> figures, Origin origin) {
    for (Figure figure : figures) {
      if (figure instanceof OriginChangeListener) {
        origin.addListener((OriginChangeListener) figure);
        figure.set(FigureConstants.ORIGIN, origin);
      }
    }
    return figures;
  }

  @Override
  @SuppressWarnings("deprecation")
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
    checkState(allVisualLayouts.size() == 1,
               "There has to be one, and only one, visual layout. Number of visual layouts: %d",
               allVisualLayouts.size());
    Set<Vehicle> allVehicles = objectService.fetchObjects(Vehicle.class);
    Set<Point> allPoints = objectService.fetchObjects(Point.class);
    Set<LocationType> allLocationTypes = objectService.fetchObjects(LocationType.class);
    Set<Location> allLocations = objectService.fetchObjects(Location.class);
    Set<Path> allPaths = objectService.fetchObjects(Path.class);
    Set<Block> allBlocks = objectService.fetchObjects(Block.class);

    List<Figure> restoredFigures = new ArrayList<>();

    Origin origin = systemModel.getDrawingMethod().getOrigin();

    VisualLayout visualLayout = allVisualLayouts.iterator().next();
    if (visualLayout.getScaleX() != 0.0 && visualLayout.getScaleY() != 0.0) {
      origin.setScale(visualLayout.getScaleX(), visualLayout.getScaleY());
    }

    LayoutModel layoutModel
        = (LayoutModel) systemModel.getMainFolder(SystemModel.FolderKey.LAYOUT);

    prepareLayout(layoutModel, systemModel, origin, objectService, visualLayout);

    progressIndicator.setProgress(ModelRestorationProgressStatus.LOADING_POINTS);
    restoreModelPoints(allPoints, systemModel, origin, restoredFigures, objectService);
    progressIndicator.setProgress(ModelRestorationProgressStatus.LOADING_PATHS);
    restoreModelPaths(allPaths, systemModel, origin, restoredFigures, objectService);
    progressIndicator.setProgress(ModelRestorationProgressStatus.LOADING_LOCATIONS);
    restoreModelLocationTypes(allLocationTypes, systemModel, objectService);
    restoreModelLocations(allLocations, systemModel, origin, restoredFigures, objectService);
    progressIndicator.setProgress(ModelRestorationProgressStatus.LOADING_VEHICLES);
    restoreModelVehicles(allVehicles, systemModel, objectService);
    progressIndicator.setProgress(ModelRestorationProgressStatus.LOADING_BLOCKS);
    restoreModelBlocks(allBlocks, systemModel, objectService);
    restoreBlockComponentDecorationDetails(systemModel);

    systemModel.getDrawing().addAll(restoredFigures);
    systemModel.onRestorationComplete();
  }

  private void restoreBlockComponentDecorationDetails(SystemModel systemModel) {
    for (BlockModel model : systemModel.getBlockModels()) {
      for (ModelComponent blockElement : model.getChildComponents()) {
        if (!(blockElement instanceof FigureDecorationDetails)) {
          continue;
        }

        ((FigureDecorationDetails) blockElement).addBlockModel(model);
      }
    }
  }

  private void prepareLayout(LayoutModel layoutModel,
                             SystemModel systemModel,
                             Origin origin,
                             TCSObjectService objectService,
                             @Nullable VisualLayout layout) {
    layoutModel.getPropertyName().setText(ModelConstants.DEFAULT_VISUAL_LAYOUT_NAME);
    layoutModel.getPropertyScaleX().setValueAndUnit(origin.getScaleX(),
                                                    LengthProperty.Unit.MM);
    layoutModel.getPropertyScaleY().setValueAndUnit(origin.getScaleY(),
                                                    LengthProperty.Unit.MM);

    if (layout != null) {
      procAdapterUtil.processAdapterFor(layoutModel)
          .updateModelProperties(layout, layoutModel, systemModel, objectService);
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
      BlockModel blockModel = modelComponentFactory.createBlockModel();
      procAdapterUtil.processAdapterFor(blockModel)
          .updateModelProperties(block, blockModel, systemModel, objectService);

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

      systemModel.registerFigure(locationModel, llf);
      locationModel.addAttributesChangeListener(llf);

      String locationTypeName = (String) locationModel.getPropertyType().getValue();
      locationModel.setLocationType(getLocationTypeComponent(systemModel, locationTypeName));

      for (LinkModel linkModel : getAttachedLinks(systemModel, locationModel)) {
        LinkConnection linkConnection = createLinkFigure(linkModel, llf);

        systemModel.registerFigure(linkModel, linkConnection);
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

    int labelPositionX;
    int labelPositionY;
    if (labelOffsetX != null && labelOffsetY != null) {
      try {
        labelPositionX = Integer.parseInt(labelOffsetX);
        labelPositionY = Integer.parseInt(labelOffsetY);
      }
      catch (NumberFormatException ex) {
        // XXX This does not look right.
        labelPositionX = -20;
        labelPositionY = -20;
      }

      label.setOffset(labelPositionX, labelPositionY);
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
    LabeledPointFigure lpf = (LabeledPointFigure) systemModel.getFigure(pointModel);
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
      procAdapterUtil.processAdapterFor(locationModel)
          .updateModelProperties(location, locationModel, systemModel, objectService);

      LabeledLocationFigure llf = createLocationFigure(locationModel,
                                                       origin.getScaleX(),
                                                       origin.getScaleY());

      systemModel.registerFigure(locationModel, llf);
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
        linkModel.getPropertyLayerWrapper()
            .setValue(locationModel.getPropertyLayerWrapper().getValue());

        LinkConnection linkConnection = createLinkFigure(linkModel, llf);

        systemModel.registerFigure(linkModel, linkConnection);
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
      LocationTypeModel locationTypeModel = modelComponentFactory.createLocationTypeModel();
      procAdapterUtil.processAdapterFor(locationTypeModel)
          .updateModelProperties(locationType, locationTypeModel, systemModel, objectService);
      systemModel.getMainFolder(SystemModel.FolderKey.LOCATION_TYPES).add(locationTypeModel);
    }
  }

  private void restoreModelVehicles(Set<Vehicle> allVehicles,
                                    SystemModel systemModel,
                                    TCSObjectService objectService) {
    for (Vehicle vehicle : allVehicles) {
      VehicleModel vehicleModel = modelComponentFactory.createVehicleModel();
      vehicleModel.setVehicle(vehicle);
      procAdapterUtil.processAdapterFor(vehicleModel)
          .updateModelProperties(vehicle, vehicleModel, systemModel, objectService);

      systemModel.getMainFolder(SystemModel.FolderKey.VEHICLES).add(vehicleModel);
      // VehicleFigures will be created in OpenTCSDrawingView.setVehicles().
    }
  }

  private List<Figure> restorePathsInModel(List<PathModel> paths, SystemModel systemModel) {
    List<Figure> restoredFigures = new ArrayList<>(paths.size());

    for (PathModel pathModel : paths) {
      PathConnection pathFigure = createPathFigure(pathModel, systemModel);

      systemModel.registerFigure(pathModel, pathFigure);
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
      LabeledPointFigure startFigure = (LabeledPointFigure) systemModel.getFigure(startPointModel);
      LabeledPointFigure endFigure = (LabeledPointFigure) systemModel.getFigure(endPointModel);
      pathFigure.connect(startFigure, endFigure);
    }

    PathModel.Type connectionType
        = (PathModel.Type) pathModel.getPropertyPathConnType().getValue();

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
      procAdapterUtil.processAdapterFor(pathModel)
          .updateModelProperties(path, pathModel, systemModel, objectService);

      PathConnection pathFigure = createPathFigure(pathModel, systemModel);

      systemModel.registerFigure(pathModel, pathFigure);
      pathModel.addAttributesChangeListener(pathFigure);
      systemModel.getMainFolder(SystemModel.FolderKey.PATHS).add(pathModel);
      restoredFigures.add(pathFigure);
      origin.addListener(pathFigure);
      pathFigure.set(FigureConstants.ORIGIN, origin);
    }
  }

  private void initPathControlPoints(PathModel.Type connectionType,
                                     String sControlPoints,
                                     PathConnection pathFigure) {
    if (connectionType != PathModel.Type.BEZIER
        && connectionType != PathModel.Type.BEZIER_3) {
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

      systemModel.registerFigure(pointModel, lpf);
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

    int labelPositionX;
    int labelPositionY;
    if (labelOffsetX != null && labelOffsetY != null) {
      try {
        labelPositionX = Integer.parseInt(labelOffsetX);
        labelPositionY = Integer.parseInt(labelOffsetY);
      }
      catch (NumberFormatException ex) {
        // XXX This does not look right.
        labelPositionX = -20;
        labelPositionY = -20;
      }

      label.setOffset(labelPositionX, labelPositionY);
    }
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
      procAdapterUtil.processAdapterFor(pointModel)
          .updateModelProperties(point, pointModel, systemModel, objectService);

      LabeledPointFigure lpf = createPointFigure(pointModel,
                                                 origin.getScaleX(),
                                                 origin.getScaleY());

      systemModel.registerFigure(pointModel, lpf);
      pointModel.addAttributesChangeListener(lpf);
      systemModel.getMainFolder(SystemModel.FolderKey.POINTS).add(pointModel);
      restoredFigures.add(lpf);

      origin.addListener(lpf);
      lpf.set(FigureConstants.ORIGIN, origin);
    }
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

  protected void initializeSystemModel(SystemModel systemModel) {
    LayoutModel layoutModel = systemModel.getLayoutModel();

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

  protected void setCurrentModelFile(File currentModelFile) {
    this.currentModelFile = currentModelFile;
  }

  protected StatusPanel getStatusPanel() {
    return statusPanel;
  }

  protected ModelExportAdapter getModelExportAdapter() {
    return modelExportAdapter;
  }

  protected void setModel(SystemModel systemModel) {
    this.systemModel = systemModel;
  }

  protected String getModelName() {
    return fModelName;
  }

  protected void setModelName(String modelName) {
    this.fModelName = modelName;
  }
}
