/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.exchange.adapter;

import com.google.inject.assistedinject.Assisted;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.TCSObject;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.guing.components.properties.type.ColorProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.exchange.EventDispatcher;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.BlockModel;
import org.opentcs.guing.model.elements.LocationModel;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.storage.PlantModelCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.util.Objects.requireNonNull;

/**
 * An adapter for blocks.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class BlockAdapter
    extends AbstractProcessAdapter {

  /**
   * This class's logger.
   */
  private static final Logger log
      = LoggerFactory.getLogger(BlockAdapter.class);

  /**
   * Creates a new instance.
   *
   * @param model The corresponding model component.
   * @param eventDispatcher The event dispatcher.
   */
  @Inject
  public BlockAdapter(@Assisted BlockModel model,
                      @Assisted EventDispatcher eventDispatcher) {
    super(model, eventDispatcher);
  }

  @Override
  public BlockModel getModel() {
    return (BlockModel) super.getModel();
  }

  @Override  // OpenTCSProcessAdapter
  public void updateModelProperties(Kernel kernel,
                                    TCSObject<?> tcsObject,
                                    @Nullable ModelLayoutElement layoutElement) {
    Block block = requireNonNull((Block) tcsObject, "tcsObject");
    try {
      StringProperty name
          = (StringProperty) getModel().getProperty(ModelComponent.NAME);
      name.setText(block.getName());

      getModel().removeAllCourseElements();

      for (TCSResourceReference<?> resRef : block.getMembers()) {
        ProcessAdapter adapter = getEventDispatcher().findProcessAdapter(resRef);
        getModel().addCourseElement(adapter.getModel());
      }

      updateMiscModelProperties(block);
    }
    catch (CredentialsException e) {
      log.warn("", e);
    }
  }

  @Override  // OpenTCSProcessAdapter
  public void updateProcessProperties(Kernel kernel, PlantModelCache plantModel) {
    requireNonNull(kernel, "kernel");

    Block block = kernel.createBlock();
    TCSObjectReference<Block> reference = block.getReference();

    StringProperty pName
        = (StringProperty) getModel().getProperty(ModelComponent.NAME);
    String name = pName.getText();

    try {
      kernel.renameTCSObject(reference, name);

      updateProcessBlock(kernel, block, plantModel);

      updateMiscProcessProperties(kernel, reference);
    }
    catch (KernelRuntimeException e) {
      log.warn("", e);
    }
  }

  private void updateProcessBlock(Kernel kernel, Block block, PlantModelCache plantModel)
      throws KernelRuntimeException {

    for (TCSResourceReference<?> resRef : block.getMembers()) {
      kernel.removeBlockMember(block.getReference(), resRef);
    }

    for (ModelComponent model : getModel().getChildComponents()) {
      TCSResourceReference<?> memberRef;
      if (model instanceof PointModel) {
        memberRef = plantModel.getPoints().get(model.getName()).getReference();
      }
      else if (model instanceof PathModel) {
        memberRef = plantModel.getPaths().get(model.getName()).getReference();
      }
      else if (model instanceof LocationModel) {
        memberRef = plantModel.getLocations().get(model.getName()).getReference();
      }
      else {
        throw new IllegalArgumentException("Unhandled model type "
            + model.getClass().getName());
      }

      ProcessAdapter adapter = getEventDispatcher().findProcessAdapter(model);

      if (adapter != null) {
        kernel.addBlockMember(block.getReference(), memberRef);
      }
    }
    // Write the block color into the model layout element
    for (VisualLayout layout : plantModel.getVisualLayouts()) {
      updateLayoutElement(layout, block.getReference());
    }
  }

  /**
   * Refreshes the properties of the layout element and saves it in the kernel.
   *
   * @param layout The VisualLayout.
   */
  private void updateLayoutElement(VisualLayout layout,
                                   TCSObjectReference<?> ref) {
    ModelLayoutElement layoutElement = new ModelLayoutElement(ref);

    ColorProperty pColor
        = (ColorProperty) getModel().getProperty(ElementPropKeys.BLOCK_COLOR);
    int rgb = pColor.getColor().getRGB() & 0x00FFFFFF;  // mask alpha bits
    layoutElement.getProperties().put(ElementPropKeys.BLOCK_COLOR, String.format("#%06X", rgb));

    layout.getLayoutElements().add(layoutElement);
  }
}
