/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.operationsdesk.components.drawing;

import java.util.List;
import static java.util.Objects.requireNonNull;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import org.opentcs.guing.base.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.base.components.properties.event.NullAttributesChangeListener;
import org.opentcs.guing.base.model.elements.VehicleModel;
import org.opentcs.guing.common.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.common.components.drawing.OpenTCSDrawingView;
import org.opentcs.guing.common.event.SystemModelTransitionEvent;
import org.opentcs.guing.common.persistence.ModelManager;
import org.opentcs.operationsdesk.components.drawing.figures.NamedVehicleFigure;
import org.opentcs.operationsdesk.util.VehicleCourseObjectFactory;

/**
 * The <code>DrawingEditor</code> coordinates <code>DrawingViews</code>
 * and the <code>Drawing</code>.
 * It also offers methods to add specific unique figures to the
 * <code>Drawing</code>.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class OpenTCSDrawingEditorOperating
    extends OpenTCSDrawingEditor {

  /**
   * Provides the current system model.
   */
  private final ModelManager modelManager;
  /**
   * A factory for course objects.
   */
  private final VehicleCourseObjectFactory courseObjectFactory;

  /**
   * Creates a new instance.
   *
   * @param courseObjectFactory A factory for course objects.
   * @param modelManager Provides the current system model.
   */
  @Inject
  public OpenTCSDrawingEditorOperating(VehicleCourseObjectFactory courseObjectFactory,
                                       ModelManager modelManager) {
    super(courseObjectFactory);
    this.courseObjectFactory = requireNonNull(courseObjectFactory, "courseObjectFactory");
    this.modelManager = requireNonNull(modelManager, "modelManager");
  }

  @Override
  public void onEvent(Object event) {
    if (event instanceof SystemModelTransitionEvent) {
      handleSystemModelTransition((SystemModelTransitionEvent) event);
    }
  }

  private void handleSystemModelTransition(SystemModelTransitionEvent evt) {
    switch (evt.getStage()) {
      case UNLOADING:
        // XXX Remove vehicles?
        break;
      case LOADED:
        setVehicles(modelManager.getModel().getVehicleModels());
//        initializeOffsetFigures();
        break;
      default:
      // Do nada.
    }
  }

  /**
   * Adds the given vehicles to the drawing.
   *
   * @param vehicleModels The <code>VehicleModels</code> to add.
   */
  public void setVehicles(List<VehicleModel> vehicleModels) {
    for (VehicleModel vehicleComp : vehicleModels) {
      addVehicle(vehicleComp);
    }
  }

  /**
   * Adds a vehicle to the drawing.
   *
   * @param vehicleModel The vehicle model to add.
   */
  public void addVehicle(VehicleModel vehicleModel) {
    NamedVehicleFigure vehicleFigure
        = courseObjectFactory.createNamedVehicleFigure(vehicleModel);

    SwingUtilities.invokeLater(() -> getDrawing().add(vehicleFigure));

    vehicleModel.addAttributesChangeListener(vehicleFigure);
    modelManager.getModel().registerFigure(vehicleModel, vehicleFigure);

    vehicleModel.setDisplayDriveOrders(true);
    for (OpenTCSDrawingView view : getAllViews()) {
      view.displayDriveOrders(vehicleModel, true);
    }
    vehicleFigure.propertiesChanged(new AttributesChangeEvent(new NullAttributesChangeListener(), vehicleModel));
  }
}
