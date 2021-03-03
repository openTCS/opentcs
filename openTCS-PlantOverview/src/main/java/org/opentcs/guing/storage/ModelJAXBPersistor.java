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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.HashSet;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.swing.filechooser.FileFilter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.opentcs.guing.application.StatusPanel;
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
import org.opentcs.guing.persistence.CourseElement;
import org.opentcs.guing.persistence.CourseModel;
import org.opentcs.guing.persistence.ModelComponentConverter;
import org.opentcs.guing.util.Comparators;
import org.opentcs.guing.util.JOptionPaneUtil;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Synchronizes data kept in <code>ModelComponents</code> to a xml file.
 *
 * @author Tobias Marquardt (Fraunhofer IML)
 */
public class ModelJAXBPersistor
    implements ModelFilePersistor {

  /**
   * The status panel for logging error messages.
   */
  private final StatusPanel statusPanel;
  /**
   * Provides new instances to validate a system model.
   */
  private final Provider<ModelValidator> validatorProvider;

  /**
   * Create a new instance.
   *
   * @param statusPanel A status panel for logging error messages.
   * @param validatorProvider Provides validator instances.
   */
  @Inject
  public ModelJAXBPersistor(StatusPanel statusPanel,
                            Provider<ModelValidator> validatorProvider) {
    this.statusPanel = requireNonNull(statusPanel, "statusPanel");
    this.validatorProvider = requireNonNull(validatorProvider, "validatorProvider");
  }

  @Override
  public boolean serialize(SystemModel systemModel, String modelName, File file, boolean ignoreError)
      throws IOException {
    requireNonNull(systemModel, "systemModel");
    requireNonNull(file, "file");

    CourseModel courseModel = new CourseModel();
    ModelComponentConverter modelConverter = new ModelComponentConverter();
    ModelValidator validator = validatorProvider.get();

    boolean valid = true;
    for (ModelComponent component : systemModel.getAll()) {
      valid &= validator.isValidWith(systemModel, component);
    }
    //Report possible duplicates if we persist to the kernel
    if (!valid) {
      //Use a hash set to avoid duplicate errors
      Set<String> errors = new HashSet<>(validator.getErrors());
      ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
      JOptionPaneUtil.showDialogWithTextArea(
          statusPanel,
          bundle.getString("ValidationWarning.title"),
          bundle.getString("ValidationWarning.descriptionSavingKernel"),
          errors);
      if (!ignoreError) {
        return false;
      }
    }

    SortedSet<LayoutModel> layoutModels = new TreeSet<>(Comparators.modelComponentsByName());
    layoutModels.addAll(systemModel.getLayoutModels());
    for (LayoutModel model : layoutModels) {
      persist(model, courseModel, modelConverter);
    }
    SortedSet<PointModel> pointModels = new TreeSet<>(Comparators.modelComponentsByName());
    pointModels.addAll(systemModel.getPointModels());
    for (PointModel model : pointModels) {
      persist(model, courseModel, modelConverter);
    }
    SortedSet<PathModel> pathModels = new TreeSet<>(Comparators.modelComponentsByName());
    pathModels.addAll(systemModel.getPathModels());
    for (PathModel model : pathModels) {
      // XXX PathAdapter needs to be changed: connectionChanged() should not do
      // anything while modelling. establishPath() should only be called in
      // updateProcessProperties(), or even better, moved to createProcessObject().
      // XXX Registering/Unregistering of ConnectionChangeListener in PathAdapter still correct?
      persist(model, courseModel, modelConverter);
    }
    SortedSet<LocationTypeModel> locationTypeModels = new TreeSet<>(Comparators.modelComponentsByName());
    locationTypeModels.addAll(systemModel.getLocationTypeModels());
    for (LocationTypeModel model : locationTypeModels) {
      persist(model, courseModel, modelConverter);
    }
    SortedSet<LocationModel> locationModels = new TreeSet<>(Comparators.modelComponentsByName());
    locationModels.addAll(systemModel.getLocationModels());
    for (LocationModel model : locationModels) {
      persist(model, courseModel, modelConverter);
    }
    SortedSet<LinkModel> linkModels = new TreeSet<>(Comparators.modelComponentsByName());
    linkModels.addAll(systemModel.getLinkModels());
    for (LinkModel model : linkModels) {
      // XXX LinkAdapter needs to be changed: connectionChanged() should not do
      // anything while modelling. establishLink() should only be called in
      // updateProcessProperties(), or even better, moved to createProcessObject().
      // XXX Registering/Unregistering of ConnectionChangeListener in LinkAdapter still correct?
      persist(model, courseModel, modelConverter);
    }
    SortedSet<BlockModel> blockModels = new TreeSet<>(Comparators.modelComponentsByName());
    blockModels.addAll(systemModel.getBlockModels());
    for (BlockModel model : blockModels) {
      persist(model, courseModel, modelConverter);
    }
    SortedSet<GroupModel> groupModels = new TreeSet<>(Comparators.modelComponentsByName());
    groupModels.addAll(systemModel.getGroupModels());
    for (GroupModel model : groupModels) {
      persist(model, courseModel, modelConverter);
    }
    SortedSet<StaticRouteModel> staticRouteModels = new TreeSet<>(Comparators.modelComponentsByName());
    staticRouteModels.addAll(systemModel.getStaticRouteModels());
    for (StaticRouteModel model : staticRouteModels) {
      persist(model, courseModel, modelConverter);
    }
    SortedSet<VehicleModel> vehicleModels = new TreeSet<>(Comparators.modelComponentsByName());
    vehicleModels.addAll(systemModel.getVehicleModels());
    for (VehicleModel model : vehicleModels) {
      persist(model, courseModel, modelConverter);
    }

    close(courseModel, file);

    return true;
  }

  @Override
  public FileFilter getDialogFileFilter() {
    return ModelJAXBConstants.DIALOG_FILE_FILTER;
  }

  private void persist(ModelComponent model,
                       CourseModel courseModel,
                       ModelComponentConverter modelConverter) {
    CourseElement element = modelConverter.convertModel(model);
    courseModel.add(element);
  }

  private void close(CourseModel courseModel, File file)
      throws IOException {
    if (!file.getName().endsWith(ModelJAXBConstants.FILE_ENDING_OPENTCS)) {
      file = new File(file.getParentFile(),
                      file.getName() + "." + ModelJAXBConstants.FILE_ENDING_OPENTCS);
    }

    try (OutputStream outStream = new FileOutputStream(file)) {
      JAXBContext jc = JAXBContext.newInstance(CourseModel.class);
      Marshaller marshaller = jc.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      StringWriter stringWriter = new StringWriter();
      marshaller.marshal(courseModel, stringWriter);

      outStream.write(stringWriter.toString().getBytes(Charset.forName("UTF-8")));
      outStream.flush();
    }
    catch (JAXBException e) {
      throw new IOException(e);
    }
  }
}
