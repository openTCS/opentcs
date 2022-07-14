/**
 * Copyright (c) The openTCS Authors.
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.common.exchange.adapter;

import java.util.HashSet;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import org.opentcs.access.to.model.BlockCreationTO;
import org.opentcs.access.to.model.PlantModelCreationTO;
import org.opentcs.components.kernel.services.TCSObjectService;
import org.opentcs.data.TCSObject;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.guing.base.model.ModelComponent;
import org.opentcs.guing.base.model.elements.BlockModel;
import org.opentcs.guing.common.model.SystemModel;

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
                                    TCSObjectService objectService) {
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
    updateModelLayoutProperties(model, block);
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
            .withLayout(getLayout((BlockModel) modelComponent))
        );
  }

  private void updateModelLayoutProperties(BlockModel model, Block block) {
    model.getPropertyColor().setColor(block.getLayout().getColor());
  }

  private Block.Type getKernelBlockType(BlockModel model) {
    return convertBlockType((BlockModel.Type) model.getPropertyType().getValue());
  }

  private Set<String> getMemberNames(BlockModel blockModel) {
    Set<String> result = new HashSet<>();
    for (ModelComponent model : blockModel.getChildComponents()) {
      result.add(model.getName());
    }

    return result;
  }

  private BlockCreationTO.Layout getLayout(BlockModel model) {
    return new BlockCreationTO.Layout(model.getPropertyColor().getColor());
  }

  private void updateModelType(BlockModel model, Block block) {
    BlockModel.Type value;

    switch (block.getType()) {
      case SAME_DIRECTION_ONLY:
        value = BlockModel.Type.SAME_DIRECTION_ONLY;
        break;
      case SINGLE_VEHICLE_ONLY:
      default:
        value = BlockModel.Type.SINGLE_VEHICLE_ONLY;
    }

    model.getPropertyType().setValue(value);
  }

  private Block.Type convertBlockType(BlockModel.Type type) {
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
}
