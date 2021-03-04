/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.filechooser.FileFilter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Unmarshaller.Listener;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.opentcs.guing.application.StatusPanel;
import org.opentcs.guing.components.properties.type.Property;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.SystemModel.FolderKey;
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
import org.opentcs.guing.persistence.CourseElement;
import org.opentcs.guing.persistence.CourseModel;
import org.opentcs.guing.persistence.ModelComponentConverter;
import org.opentcs.guing.util.JOptionPaneUtil;
import org.opentcs.guing.util.ResourceBundleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of <code>ModelReader</code> to deserialize a <code>SystemModel</code>.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class ModelJAXBReader
    implements ModelReader {

  /**
   * This class' logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ModelValidator.class);
  /**
   * The <code>SystemModel</code> that will contain the read model.
   */
  private final Provider<SystemModel> systemModelProvider;
  /**
   * Validates model components and the system model.
   */
  private final ModelValidator validator;
  /**
   * The status panel of the plant overview.
   */
  private final StatusPanel statusPanel;

  /**
   * Creates a new instance.
   *
   * @param systemModelProvider the system model
   * @param validator the validator
   * @param statusPanel the status panel
   */
  @Inject
  public ModelJAXBReader(Provider<SystemModel> systemModelProvider,
                         ModelValidator validator,
                         StatusPanel statusPanel) {
    this.systemModelProvider = requireNonNull(systemModelProvider, "systemModelProvider");
    this.validator = requireNonNull(validator, "validator");
    this.statusPanel = requireNonNull(statusPanel, "statusPanel");
  }

  @Override
  public Optional<SystemModel> deserialize(File file)
      throws IOException, IllegalArgumentException {
    requireNonNull(file, "file");
    SystemModel systemModel = systemModelProvider.get();
    String modelName = file.getName().replaceFirst("[.][^.]+$", ""); //remove extension;
    if (modelName != null && !modelName.isEmpty()) {
      systemModel.setName(modelName);
    }
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(CourseModel.class);

      //Create XMLStreamReader for the given file
      XMLInputFactory xif = XMLInputFactory.newFactory();
      FileInputStream xml = new FileInputStream(file);
      XMLStreamReader xsr = xif.createXMLStreamReader(xml);

      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      //Register a listener to store the beginning row/column for every object
      LocationListener listener = new LocationListener(xsr);
      jaxbUnmarshaller.setListener(listener);

      CourseModel courseModel = (CourseModel) jaxbUnmarshaller.unmarshal(xsr);
      Set<String> errors = new HashSet<>();
      ModelComponentConverter modelConverter = new ModelComponentConverter();
      for (CourseElement courseElement : courseModel.getCourseElements()) {
        ModelComponent model = modelConverter.revertCourseElement(courseElement);
        if (validator.isValidWith(systemModel, model)) {
          addToSystemModel(model, systemModel);
        }
        else {
          //Gather log information and log/store it in the errors
          Location loc = listener.getLocation(courseElement);
          String validationErrors = validator.getErrors().stream()
              .collect(Collectors.joining("\n  "));
          String message = ResourceBundleUtil.getBundle()
              .getFormatted("ModelJAXBReader.error.header",
                            loc.getLineNumber(),
                            loc.getColumnNumber(),
                            model.getClass().getSimpleName(),
                            validationErrors);
          LOG.warn(message);
          errors.add(message);
          validator.resetErrors();
        }
      }
      //if any errors occurred, show the dialog with all errors listed
      if (!errors.isEmpty()) {
        ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
        JOptionPaneUtil.showDialogWithTextArea(statusPanel,
                                               bundle.getString("ValidationWarning.title"),
                                               bundle.getString("ValidationWarning.descriptionLoading"),
                                               errors);
      }
    }
    catch (JAXBException e) {
      throw new IOException(e);
    }
    catch (IllegalArgumentException e) {
      throw e;
    }
    catch (XMLStreamException e) {
      LOG.warn("Exception while reading model file.", e);
    }

    return Optional.of(systemModel);
  }

  @Override
  public FileFilter getDialogFileFilter() {
    return ModelJAXBConstants.DIALOG_FILE_FILTER;
  }

  /**
   * Add the given model to the system model.
   */
  private void addToSystemModel(ModelComponent model, SystemModel systemModel) {
    if (model instanceof BlockModel) {
      systemModel.getMainFolder(FolderKey.BLOCKS).add(model);
    }
    if (model instanceof GroupModel) {
      systemModel.getMainFolder(FolderKey.GROUPS).add(model);
    }
    if (model instanceof LayoutModel) {
      // SystemModel already contains a LayoutModel, just copy the properties
      ModelComponent layoutComponent = systemModel.getMainFolder(FolderKey.LAYOUT);
      for (Map.Entry<String, Property> property
               : model.getProperties().entrySet()) {
        layoutComponent.setProperty(property.getKey(), property.getValue());
      }
    }
    if (model instanceof LinkModel) {
      systemModel.getMainFolder(FolderKey.LINKS).add(model);
    }
    if (model instanceof LocationModel) {
      systemModel.getMainFolder(FolderKey.LOCATIONS).add(model);
    }
    if (model instanceof LocationTypeModel) {
      systemModel.getMainFolder(FolderKey.LOCATION_TYPES).add(model);
    }
    if (model instanceof OtherGraphicalElement) {
      systemModel.getMainFolder(FolderKey.OTHER_GRAPHICAL_ELEMENTS).add(model);
    }
    if (model instanceof PathModel) {
      systemModel.getMainFolder(FolderKey.PATHS).add(model);
    }
    if (model instanceof PointModel) {
      systemModel.getMainFolder(FolderKey.POINTS).add(model);
    }
    if (model instanceof StaticRouteModel) {
      systemModel.getMainFolder(FolderKey.STATIC_ROUTES).add(model);
    }
    if (model instanceof VehicleModel) {
      systemModel.getMainFolder(FolderKey.VEHICLES).add(model);
    }
  }

  private static class LocationListener
      extends Listener {

    private final XMLStreamReader xsr;
    private final Map<Object, Location> locations;

    public LocationListener(XMLStreamReader xsr) {
      this.xsr = xsr;
      this.locations = new HashMap<>();
    }

    @Override
    public void beforeUnmarshal(Object target, Object parent) {
      locations.put(target, xsr.getLocation());
    }

    public Location getLocation(Object o) {
      return locations.get(o);
    }
  }
}
