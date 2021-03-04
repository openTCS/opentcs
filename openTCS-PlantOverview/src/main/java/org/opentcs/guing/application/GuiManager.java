/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application;

import org.opentcs.components.plantoverview.PlantModelExporter;
import org.opentcs.components.plantoverview.PlantModelImporter;
import org.opentcs.guing.model.FiguresFolder;
import org.opentcs.guing.model.ModelComponent;

/**
 * Provides some central services for various parts of the plant overview application.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public interface GuiManager {

  /**
   * Called when an object was selected in the tree view.
   *
   * @param modelComponent The selected object.
   */
  void selectModelComponent(ModelComponent modelComponent);

  /**
   * Called when an additional object was selected in the tree view.
   *
   * @param modelComponent The selected object.
   */
  void addSelectedModelComponent(ModelComponent modelComponent);

  /**
   * Called when an object was removed from the tree view (by user interaction).
   *
   * @param fDataObject The object to be removed.
   * @return Indicates whether the object was really removed from the model.
   */
  boolean treeComponentRemoved(ModelComponent fDataObject);

  /**
   * Notifies about a figure object being selected.
   *
   * @param modelComponent The selected object.
   */
  void figureSelected(ModelComponent modelComponent);

  /**
   * Called when a block was selected in the tree view.
   * Should select all figures in the drawing view belonging to the block.
   *
   * @param blockFiguresFolder
   */
  void blockSelected(FiguresFolder blockFiguresFolder);

  /**
   * Creates a new, empty model and initializes it.
   */
  void createEmptyModel();

  /**
   * Loads a plant model.
   */
  void loadModel();
  
  /**
   * Imports a plant model using the given importer.
   *
   * @param importer The importer.
   */
  void importModel(PlantModelImporter importer);

  /**
   * @return
   */
  boolean saveModel();

  /**
   *
   * @return
   */
  boolean saveModelAs();
  
  /**
   * Exports a plant model using the given exporter.
   *
   * @param exporter The exporter.
   * @return 
   */
  void exportModel(PlantModelExporter exporter);

  /**
   * Creates a new model component instance that does not have a corresponding figure.
   * (Like a block or a location type.)
   *
   * @param clazz The type of object to be created.
   * @return The created object.
   */
  ModelComponent createModelComponent(Class<? extends ModelComponent> clazz);
}
