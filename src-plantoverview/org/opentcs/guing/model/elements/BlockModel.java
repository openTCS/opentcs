/**
 * (c): IML, IFAK.
 *
 */
package org.opentcs.guing.model.elements;

import com.google.common.collect.Lists;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.opentcs.data.model.visualization.ElementPropKeys;
import org.opentcs.guing.components.properties.event.AttributesChangeListener;
import org.opentcs.guing.components.properties.type.ColorProperty;
import org.opentcs.guing.components.properties.type.KeyValueSetProperty;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.components.tree.elements.BlockUserObject;
import org.opentcs.guing.event.BlockChangeEvent;
import org.opentcs.guing.event.BlockChangeListener;
import org.opentcs.guing.model.FigureComponent;
import org.opentcs.guing.model.FiguresFolder;
import org.opentcs.guing.model.ModelComponent;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Eine Blockstrecke. Eine Blockstrecke besitzt eine Menge von
 * FigureComponent-Objekten. Ein FigureComponent-Objekt verweist jeweils auf
 * genau ein Figure-Objekt. Somit besitzt eine Blockstrecke eine Menge von
 * Figures (allerdings handelt es sich nur um Verweise auf Figures; die
 * Verwaltung erfolgt in einem FiguresFolder bzw. in einem Drawing.
 * <p>
 * <b>Entwurfsmuster:</b> Kompositum. Blockline ist ein Kompositum.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 * @see BlocklineUserObject
 * @see FigureComponent
 */
public class BlockModel
    extends FiguresFolder {

  /**
   * A list of change listeners for this object.
   */
  private List<BlockChangeListener> fListeners = new LinkedList<>();

  /**
   * Creates a new instance.
   */
  public BlockModel() {
    this("");
  }

  /**
   * Creates a new instance.
   *
   * @param name The name of the block.
   */
  public BlockModel(String name) {
    super(name);
    createProperties();
  }

  @Override	// FiguresFolder
  public BlockUserObject createUserObject() {
    fUserObject = new BlockUserObject(this);

    return (BlockUserObject) fUserObject;
  }

  @Override	// AbstractModelComponent
  public String getTreeViewName() {
    String treeViewName = getDescription() + " " + getName();

    return treeViewName;
  }

  @Override	// AbstractModelComponent
  public String getDescription() {
    return ResourceBundleUtil.getBundle().getString("block.description");
  }

  @Override	// AbstractModelComponent
  public void propertiesChanged(AttributesChangeListener listener) {
    if (getProperty(ElementPropKeys.BLOCK_COLOR).hasChanged()) {
      colorChanged();
    }

    super.propertiesChanged(listener);
  }

  /**
   * Liefert die Farbe des Blockbereichs.
   *
   * @return
   */
  public Color getColor() {
    ColorProperty property = (ColorProperty) getProperty(ElementPropKeys.BLOCK_COLOR);

    return property.getColor();
  }

  /**
   * Fügt dem Blockbereich ein Fahrkurselement hinzu.
   *
   * @param model das hinzuzufügende Fahrkurselement
   */
  public void addCourseElement(ModelComponent model) {
    if (!contains(model)) {
      getChildComponents().add(model);
      // _nicht_ den parent ändern!
    }
  }

  /**
   * Entfernt ein Fahrkurselement aus dem Blockbereich.
   *
   * @param model das zu entfernende Fahrkurselement
   */
  public void removeCourseElement(ModelComponent model) {
    if (contains(model)) {
      remove(model);
    }
  }

  /**
   * Entfernt alle Fahrkurselemente aus dem Blockbereich.
   */
  public void removeAllCourseElements() {
    for (Object o : new ArrayList<>(Lists.reverse(getChildComponents()))) {
      remove((ModelComponent) o);
    }
  }

  /**
   * Registriert einen Listener, der fortan informiert wird, wenn sich die
   * Fahrkurselemente ändern.
   *
   * @param listener
   */
  public void addBlockChangeListener(BlockChangeListener listener) {
    if (fListeners == null) {
      fListeners = new ArrayList<>();
    }

    if (!fListeners.contains(listener)) {
      fListeners.add(listener);
    }
  }

  /**
   * Entfernt einen Listener, der ab sofort nicht mehr informiert wird, wenn es
   * Änderungen an den Fahrkurslementen gibt.
   *
   * @param listener
   */
  public void removeBlockChangeListener(BlockChangeListener listener) {
    fListeners.remove(listener);
  }

  /**
   * Benachrichtigt alle registrierten Listener, dass sich bei den
   * Fahrkurselementen etwas geändert hat. Wird von einem Klienten aufgerufen,
   * der Änderungen an den Fahrkurselementen vorgenommen hat.
   */
  public void courseElementsChanged() {
    for (BlockChangeListener listener : fListeners) {
      listener.courseElementsChanged(new BlockChangeEvent(this));
    }
  }

  /**
   * Benachrichtigt alle registrierten Listener, dass sich die Farbe des
   * Blockbereichs geändert hat.
   */
  public void colorChanged() {
    for (BlockChangeListener listener : fListeners) {
      listener.colorChanged(new BlockChangeEvent(this));
    }
  }

  /**
   * Benachrichtigt alle registrierten Listener, dass der Blockbereich entfernt
   * wurde. Wird von einem Klienten aufgerufen, der den Blockbereich entfernt
   * hat.
   */
  public void blockRemoved() {
    for (BlockChangeListener listener : new ArrayList<>(fListeners)) {
      listener.blockRemoved(new BlockChangeEvent(this));
    }
  }

  private void createProperties() {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    // Name
    StringProperty pName = new StringProperty(this);
    pName.setDescription(bundle.getString("block.name.text"));
    pName.setHelptext(bundle.getString("block.name.helptext"));
    setProperty(NAME, pName);
    // Color
    ColorProperty pColor = new ColorProperty(this, Color.red);
    pColor.setDescription(bundle.getString("element.blockColor.text"));
    pColor.setHelptext(bundle.getString("element.blockColor.helptext"));
    setProperty(ElementPropKeys.BLOCK_COLOR, pColor);
    // Miscellaneous
    KeyValueSetProperty pMiscellaneous = new KeyValueSetProperty(this);
    pMiscellaneous.setDescription(bundle.getString("block.miscellaneous.text"));
    pMiscellaneous.setHelptext(bundle.getString("block.miscellaneous.helptext"));
    setProperty(MISCELLANEOUS, pMiscellaneous);
  }
}
