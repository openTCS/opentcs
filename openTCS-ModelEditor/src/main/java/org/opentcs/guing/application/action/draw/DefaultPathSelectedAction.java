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
import org.opentcs.guing.util.ImageDirectory;

/**
 * This action manages the behaviour when the user selects the path button.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class DefaultPathSelectedAction
    extends org.jhotdraw.draw.action.AbstractSelectedAction {

  private final PathModel.Type pathType;
  private final Tool tool;
  /**
   * The button this action belongs to.
   */
  private final JPopupButton popupButton;
  /**
   * The Icon the popup button uses when this action is selected.
   */
  private final ImageIcon largeIcon;
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
    this.tool = Objects.requireNonNull(tool);
    this.popupButton = Objects.requireNonNull(popupButton);
    this.group = Objects.requireNonNull(group);
    this.pathType = null;
    this.largeIcon = null;
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
      PathModel.Type pathType,
      JPopupButton popupButton,
      ButtonGroup group) {

    super(editor);
    this.tool = Objects.requireNonNull(tool);
    this.popupButton = Objects.requireNonNull(popupButton);
    this.group = Objects.requireNonNull(group);

    this.pathType = Objects.requireNonNull(pathType);
    this.largeIcon = getLargeImageIconByType(pathType);

    putValue(AbstractAction.NAME, pathType.getDescription());
    putValue(AbstractAction.SHORT_DESCRIPTION, pathType.getHelptext());
    putValue(AbstractAction.SMALL_ICON, getImageIconByType(pathType));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (pathType != null) {
      OpenTCSConnectionTool connectionTool = (OpenTCSConnectionTool) tool;
      PathConnection pathConnection = (PathConnection) connectionTool.getPrototype();
      // Typ explizit setzen, sodass die ausgewählte Kurve grafisch dargestellt wird
      // Im Property muss die Kurve auch noch geändert werden
      pathConnection.getModel().getPropertyPathConnType().setValue(pathType);

      popupButton.setText(null);
      popupButton.setToolTipText(pathType.getHelptext());
      popupButton.setIcon(largeIcon);
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

  private ImageIcon getImageIconByType(PathModel.Type pathType) {
    switch (pathType) {
      case DIRECT:
        return ImageDirectory.getImageIcon("/toolbar/path-direct.22.png");
      case ELBOW:
        return ImageDirectory.getImageIcon("/toolbar/path-elbow.22.png");
      case SLANTED:
        return ImageDirectory.getImageIcon("/toolbar/path-slanted.22.png");
      case BEZIER:
        return ImageDirectory.getImageIcon("/toolbar/path-bezier.22.png");
      case BEZIER_3:
        return ImageDirectory.getImageIcon("/toolbar/path-bezier.22.png");
      case POLYPATH:
        return ImageDirectory.getImageIcon("/toolbar/path-polypath.22.png");
      default:
        return null;
    }
  }

  private ImageIcon getLargeImageIconByType(PathModel.Type pathType) {
    switch (pathType) {
      case DIRECT:
        return ImageDirectory.getImageIcon("/toolbar/path-direct-arrow.22.png");
      case ELBOW:
        return ImageDirectory.getImageIcon("/toolbar/path-elbow-arrow.22.png");
      case SLANTED:
        return ImageDirectory.getImageIcon("/toolbar/path-slanted-arrow.22.png");
      case BEZIER:
        return ImageDirectory.getImageIcon("/toolbar/path-bezier-arrow.22.png");
      case BEZIER_3:
        return ImageDirectory.getImageIcon("/toolbar/path-bezier-arrow.22.png");
      case POLYPATH:
        return ImageDirectory.getImageIcon("/toolbar/path-polypath-arrow.22.png");
      default:
        return null;
    }
  }
}
