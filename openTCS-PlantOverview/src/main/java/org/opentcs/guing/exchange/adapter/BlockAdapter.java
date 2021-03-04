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
import java.util.HashSet;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.Kernel;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.access.to.model.BlockCreationTO;
import org.opentcs.access.to.model.ModelLayoutElementCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.guing.components.properties.type.ColorProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.exchange.EventDispatcher;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.BlockModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private static final Logger LOG = LoggerFactory.getLogger(BlockAdapter.class);

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
      LOG.warn("", e);
    }
  }

  @Override  // OpenTCSProcessAdapter
  public void storeToPlantModel(PlantModelCreationTO plantModel) {
    try {
      plantModel.getBlocks().add(new BlockCreationTO(getModel().getName())
          .setMemberNames(getMemberNames())
          .setProperties(getKernelProperties()));

      // Write the block color into the model layout element
      for (VisualLayoutCreationTO layout : plantModel.getVisualLayouts()) {
        updateLayoutElement(layout);
      }

    }
    catch (KernelRuntimeException e) {
      LOG.warn("", e);
    }
  }

  private Set<String> getMemberNames() {
    Set<String> result = new HashSet<>();
    for (ModelComponent model : getModel().getChildComponents()) {
      if (getEventDispatcher().findProcessAdapter(model) != null) {
        result.add(model.getName());
      }
    }

    return result;
  }

  /**
   * Refreshes the properties of the layout element and saves it in the kernel.
   *
   * @param layout The VisualLayout.
   */
  private void updateLayoutElement(VisualLayoutCreationTO layout) {
    ColorProperty pColor = (ColorProperty) getModel().getProperty(ElementPropKeys.BLOCK_COLOR);
    int rgb = pColor.getColor().getRGB() & 0x00FFFFFF;  // mask alpha bits

    layout.getModelElements().add(
        new ModelLayoutElementCreationTO(getModel().getName())
            .setProperty(ElementPropKeys.BLOCK_COLOR, String.format("#%06X", rgb))
    );
  }

}
