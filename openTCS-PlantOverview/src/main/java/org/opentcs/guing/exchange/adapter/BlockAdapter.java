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

import java.util.HashSet;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.annotation.Nullable;
import org.opentcs.access.to.model.BlockCreationTO;
import org.opentcs.access.to.model.ModelLayoutElementCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.access.to.model.VisualLayoutCreationTO;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.SystemModel;
import org.opentcs.guing.model.elements.BlockModel;
import org.opentcs.util.Colors;

/**
 * An adapter for blocks.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class BlockAdapter
    extends AbstractProcessAdapter {

  @Override  // OpenTCSProcessAdapter
  public void updateModelProperties(TCSObject<?> tcsObject,
                                    ModelComponent modelComponent,
                                    SystemModel systemModel,
                                    TCSObjectService objectService,
                                    @Nullable ModelLayoutElement layoutElement) {
    Block block = requireNonNull((Block) tcsObject, "tcsObject");
    BlockModel model = (BlockModel) modelComponent;

    model.getPropertyName().setText(block.getName());
    model.removeAllCourseElements();

    updateModelType(model, block);

    for (TCSResourceReference<?> resRef : block.getMembers()) {
      ModelComponent blockMember = systemModel.getModelComponent(resRef.getName());
      model.addCourseElement(blockMember);
    }

    updateMiscModelProperties(model, block);

    if (layoutElement != null) {
      updateModelLayoutProperties(model, layoutElement);
    }
  }

  @Override
  public PlantModelCreationTO storeToPlantModel(ModelComponent modelComponent,
                                                SystemModel systemModel,
                                                PlantModelCreationTO plantModel) {
    return plantModel
        .withBlock(new BlockCreationTO(modelComponent.getName())
            .withType(getKernelBlockType((BlockModel) modelComponent))
            .withMemberNames(getMemberNames((BlockModel) modelComponent))
            .withProperties(getKernelProperties(modelComponent))
        )
        .withVisualLayouts(updatedLayouts(modelComponent, plantModel.getVisualLayouts(), systemModel));
  }

  private void updateModelLayoutProperties(BlockModel model, ModelLayoutElement layoutElement) {
    String sBlockColor = layoutElement.getProperties().get(ElementPropKeys.BLOCK_COLOR);
    if (sBlockColor != null) {
      model.getPropertyColor().setColor(Colors.decodeFromHexRGB(sBlockColor));
    }
  }

  private Block.Type getKernelBlockType(BlockModel model) {
    return convertBlockType((BlockModel.BlockType) model.getPropertyType().getValue());
  }

  private Set<String> getMemberNames(BlockModel blockModel) {
    Set<String> result = new HashSet<>();
    for (ModelComponent model : blockModel.getChildComponents()) {
      result.add(model.getName());
    }

    return result;
  }

  private void updateModelType(BlockModel model, Block block) {
    BlockModel.BlockType value;

    switch (block.getType()) {
      case SAME_DIRECTION_ONLY:
        value = BlockModel.BlockType.SAME_DIRECTION_ONLY;
        break;
      case SINGLE_VEHICLE_ONLY:
      default:
        value = BlockModel.BlockType.SINGLE_VEHICLE_ONLY;
    }

    model.getPropertyType().setValue(value);
  }

  private Block.Type convertBlockType(BlockModel.BlockType type) {
    requireNonNull(type, "type");
    switch (type) {
      case SINGLE_VEHICLE_ONLY:
        return Block.Type.SINGLE_VEHICLE_ONLY;
      case SAME_DIRECTION_ONLY:
        return Block.Type.SAME_DIRECTION_ONLY;
      default:
        throw new IllegalArgumentException("Unhandled block type: " + type);
    }
  }

  @Override
  protected VisualLayoutCreationTO updatedLayout(ModelComponent model,
                                                 VisualLayoutCreationTO layout,
                                                 SystemModel systemModel) {
    BlockModel blockModel = (BlockModel) model;

    return layout.withModelElement(
        new ModelLayoutElementCreationTO(blockModel.getName())
            .withProperty(ElementPropKeys.BLOCK_COLOR,
                          Colors.encodeToHexRGB(blockModel.getPropertyColor().getColor()))
    );
  }

}
