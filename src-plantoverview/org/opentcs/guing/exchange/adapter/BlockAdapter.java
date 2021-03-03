/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.exchange.adapter;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opentcs.access.CredentialsException;
import org.opentcs.access.KernelRuntimeException;
import org.opentcs.data.ObjectExistsException;
import org.opentcs.data.ObjectUnknownException;
import org.opentcs.data.TCSObjectReference;
import org.opentcs.data.model.Block;
import org.opentcs.data.model.TCSResourceReference;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.data.model.visualization.LayoutElement;
import org.opentcs.data.model.visualization.ModelLayoutElement;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.guing.components.properties.event.AttributesChangeEvent;
import org.opentcs.guing.components.properties.type.ColorProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.event.BlockChangeEvent;
import org.opentcs.guing.event.BlockChangeListener;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.model.elements.BlockModel;

/**
 * An adapter for blocks.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class BlockAdapter
    extends OpenTCSProcessAdapter
    implements BlockChangeListener {

  /**
   * This class's logger.
   */
  private static final Logger log
      = Logger.getLogger(BlockAdapter.class.getName());

  /**
   * Creates a new instance.
   */
  public BlockAdapter() {
    super();
  }

  @Override
  public BlockModel getModel() {
    return (BlockModel) super.getModel();
  }

  @Override
  public void setModel(ModelComponent model) {
    if (!BlockModel.class.isInstance(model)) {
      throw new IllegalArgumentException(model + " is not a BlockModel");
    }
    super.setModel(model);
  }

  @Override
  @SuppressWarnings("unchecked")
  public TCSObjectReference<Block> getProcessObject() {
    return (TCSObjectReference<Block>) super.getProcessObject();
  }

  @Override	// AbstractProcessAdapter
  public Block createProcessObject() throws KernelRuntimeException {
    if (!hasModelingState()) {
      return null;
    }

    Block block = kernel().createBlock();
    setProcessObject(block.getReference());
    // Nur den Namen des Kernel-Objekts als Property übernehmen
    nameToModel(block);
    register();

    return block;
  }

  @Override	// AbstractProcessAdapter
  public void register() {
    super.register();
    getModel().addBlockChangeListener(this);
  }

  @Override	// AbstractProcessAdapter
  public void releaseProcessObject() {
    try {
      releaseLayoutElement();
      kernel().removeTCSObject(getProcessObject());
      super.releaseProcessObject();	// ???
    }
    catch (KernelRuntimeException e) {
      log.log(Level.WARNING, null, e);
    }
  }

  @Override	// OpenTCSProcessAdapter
  public void propertiesChanged(AttributesChangeEvent event) {
    if (event.getInitiator() != this) {
      updateProcessProperties(false);
    }
  }

  @Override	// OpenTCSProcessAdapter
  public void updateModelProperties() {
    TCSObjectReference<Block> reference = getProcessObject();

    synchronized (reference) {
      try {
        Block block = kernel().getTCSObject(Block.class, reference);
        if (block == null) {
          return;
        }

        StringProperty name = (StringProperty) getModel().getProperty(ModelComponent.NAME);
        name.setText(block.getName());

        getModel().removeAllCourseElements();

        for (TCSResourceReference<?> resRef : block.getMembers()) {
          ProcessAdapter adapter = getEventDispatcher().findProcessAdapter(resRef);
          getModel().addCourseElement(adapter.getModel());
        }

        updateMiscModelProperties(block);
      }
      catch (CredentialsException e) {
        log.log(Level.WARNING, null, e);
      }
    }
  }

  @Override	// OpenTCSProcessAdapter
  public void updateProcessProperties(boolean updateAllProperties) {
    super.updateProcessProperties(updateAllProperties);
    TCSObjectReference<Block> reference = getProcessObject();

    if (isInTransition()) {
      return;
    }

    synchronized (reference) {
      StringProperty pName = (StringProperty) getModel().getProperty(ModelComponent.NAME);
      String name = pName.getText();

      try {
        if (updateAllProperties || pName.hasChanged()) {
          kernel().renameTCSObject(reference, name);
        }

        Block block = kernel().getTCSObject(Block.class, reference);
        updateProcessBlock(block, reference);

        updateMiscProcessProperties(updateAllProperties);
      }
      catch (ObjectExistsException e) {
        undo(name, e);
        // TODO: Show message in the status bar ("Object with this name already exists".)
        // Also in all other adapters.
      }
      catch (CredentialsException | ObjectUnknownException e) {
        log.log(Level.WARNING, null, e);
      }
    }
  }

  private void updateProcessBlock(Block block,
                                  TCSObjectReference<Block> reference)
      throws ObjectUnknownException, CredentialsException {
    if (block == null) {
      return;
    }
    for (TCSResourceReference<?> resRef : block.getMembers()) {
      kernel().removeBlockMember(reference, resRef);
    }

    for (ModelComponent model : getModel().getChildComponents()) {
      ProcessAdapter adapter = getEventDispatcher().findProcessAdapter(model);

      if (adapter != null && adapter.getProcessObject() != null) {
        kernel().addBlockMember(
            reference, (TCSResourceReference<?>) adapter.getProcessObject());
      }
    }
    // Write the block color into the model layout element
    for (VisualLayout layout : kernel().getTCSObjects(VisualLayout.class)) {
      updateLayoutElement(layout);
    }
  }

  /**
   * Refreshes the properties of the layout element and saves it in the kernel.
   *
   * @param layout The VisualLayout.
   */
  private void updateLayoutElement(VisualLayout layout) {
    if (fLayoutElement == null) {
      fLayoutElement = new ModelLayoutElement(getProcessObject());
    }

    Map<String, String> layoutProperties = fLayoutElement.getProperties();

    ColorProperty pColor = (ColorProperty) getModel().getProperty(ElementPropKeys.BLOCK_COLOR);
    int rgb = pColor.getColor().getRGB() & 0x00FFFFFF;	// mask alpha bits
    layoutProperties.put(ElementPropKeys.BLOCK_COLOR, String.format("#%06X", rgb));
    fLayoutElement.setProperties(layoutProperties);

    Set<LayoutElement> layoutElements = layout.getLayoutElements();
    for (LayoutElement element : layoutElements) {
      ModelLayoutElement mle = (ModelLayoutElement) element;

      if (mle.getVisualizedObject().getId() == fLayoutElement.getVisualizedObject().getId()) {
        layoutElements.remove(element);
        break;
      }
    }

    layoutElements.add(fLayoutElement);
    kernel().setVisualLayoutElements(layout.getReference(), layoutElements);
  }

  @Override	// BlockChangeListener
  public void courseElementsChanged(BlockChangeEvent event) {
    updateProcessProperties(false);
  }

  @Override	// BlockChangeListener
  public void colorChanged(BlockChangeEvent e) {
  }

  @Override	// BlockChangeListener
  public void blockRemoved(BlockChangeEvent e) {
  }
}
