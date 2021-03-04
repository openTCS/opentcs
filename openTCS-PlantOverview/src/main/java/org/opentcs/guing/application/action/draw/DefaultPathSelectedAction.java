/**
 * (c): IML, JHotDraw.
 *
 */
package org.opentcs.guing.application.action.draw;

import java.awt.event.ActionEvent;
import java.util.Objects;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.tool.Tool;
import org.jhotdraw.gui.JPopupButton;
import org.opentcs.guing.application.toolbar.OpenTCSConnectionTool;
import org.opentcs.guing.components.drawing.figures.PathConnection;
import org.opentcs.guing.model.elements.PathModel;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * This action manages the behaviour when the user selects the path button.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class DefaultPathSelectedAction
    extends org.jhotdraw.draw.action.AbstractSelectedAction {

  private PathModel.LinerType pathType;
  private final Tool tool;
  /**
   * The button this action belongs to.
   */
  private final JPopupButton popupButton;
  /**
   * The ButtonGroup the popupButton belongs to. It is necessary to know it,
   * because
   * <code>DrawingEditor.setTool()</code> doesn't select or deselect the
   * popupButton, so we have to do it manually.
   */
  private final ButtonGroup group;

  /**
   * Constructor for an action of a button in the toolbar.
   *
   * @param editor The drawing editor
   * @param tool The tool
   * @param popupButton The popup button
   * @param group The button group
   */
  public DefaultPathSelectedAction(
      DrawingEditor editor,
      Tool tool,
      JPopupButton popupButton,
      ButtonGroup group) {

    super(editor);
    Objects.requireNonNull(tool);
    this.tool = tool;
    Objects.requireNonNull(popupButton);
    this.popupButton = popupButton;
    Objects.requireNonNull(group);
    this.group = group;
  }

  /**
   * Constructor for a button inside a drop down menu of another button.
   *
   * @param editor The drawing editor
   * @param tool The tool
   * @param pathType The path tzpe
   * @param popupButton The popup button
   * @param group The button group
   */
  public DefaultPathSelectedAction(
      DrawingEditor editor,
      Tool tool,
      PathModel.LinerType pathType,
      JPopupButton popupButton,
      ButtonGroup group) {

    super(editor);
    this.pathType = pathType;
    Objects.requireNonNull(tool);
    this.tool = tool;
    Objects.requireNonNull(popupButton);
    this.popupButton = popupButton;
    Objects.requireNonNull(group);
    this.group = group;

    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    putValue(AbstractAction.NAME, bundle.getString("path.type." + pathType.name() + ".text"));
    putValue(AbstractAction.SHORT_DESCRIPTION, bundle.getString("path.type." + pathType.name() + ".toolTipText"));
    ImageIcon icon = new ImageIcon(getClass().getResource(bundle.getString("path.type." + pathType.name() + ".popupIcon")));
    putValue(AbstractAction.SMALL_ICON, icon);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (pathType != null) {
      OpenTCSConnectionTool connectionTool = (OpenTCSConnectionTool) tool;
      PathConnection pathConnection = (PathConnection) connectionTool.getPrototype();
      // Typ explizit setzen, sodass die ausgewählte Kurve grafisch dargestellt wird
      // Im Property muss die Kurve auch noch geändert werden
      pathConnection.getModel().getPropertyPathConnType().setValue(pathType);

      ResourceBundleUtil.getBundle().configureNamelessButton(popupButton, "path.type." + pathType.name());
    }

    getEditor().setTool(tool);
    group.setSelected(popupButton.getModel(), true);
  }

  @Override
  protected void updateEnabledState() {
    if (getView() != null) {
      setEnabled(getView().isEnabled());
    }
    else {
      setEnabled(false);
    }
  }
}
