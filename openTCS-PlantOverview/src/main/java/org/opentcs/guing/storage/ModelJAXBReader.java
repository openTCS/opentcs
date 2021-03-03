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
import java.io.IOException;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.opentcs.guing.components.properties.type.LengthProperty;
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

/**
 * Implementation of <code>ModelReader</code> to deserialize a
 * <code>SystemModel</code>.
 *
 * @author Philipp Seifert (Philipp.Seifert@iml.fraunhofer.de)
 */
public class ModelJAXBReader
    implements ModelReader {

  /**
   * The <code>SystemModel</code> that will contain the read model.
   */
  private final SystemModel systemModel;
  /**
   * Converts JAXB classes to ModelComponents.
   */
  private final ModelComponentConverter modelConverter;

  public ModelJAXBReader(SystemModel systemModel) {
    this.systemModel = requireNonNull(systemModel, "systemModel is null");
    modelConverter = new ModelComponentConverter();
  }

  @Override
  public SystemModel deserialize(File file)
      throws IOException, IllegalArgumentException {
    requireNonNull(file, "file is null");
    String modelName
        = file.getName().replaceFirst("[.][^.]+$", ""); //remove extension;
    if (modelName != null && !modelName.isEmpty()) {
      systemModel.setName(modelName);
    }
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(CourseModel.class);

      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      CourseModel courseModel = (CourseModel) jaxbUnmarshaller.unmarshal(file);
      for (CourseElement courseElement : courseModel.getCourseElements()) {
        ModelComponent model = modelConverter.revertCourseElement(courseElement);
        addToSystemModel(model);
      }
    }
    catch (JAXBException e) {
      throw new IOException(e);
    }
    catch (IllegalArgumentException e) {
      throw e;
    }

    return systemModel;
  }

  /**
   * Add the given model to the system model.
   */
  private void addToSystemModel(ModelComponent model) {
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
      double scaleX = (double)
          ((LengthProperty) layoutComponent.getProperty(LayoutModel.SCALE_X)).getValue();
      double scaleY = (double)
          ((LengthProperty) layoutComponent.getProperty(LayoutModel.SCALE_Y)).getValue();
      systemModel.getDrawingMethod().getOrigin().setScale(
                scaleX, scaleY);
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
}
