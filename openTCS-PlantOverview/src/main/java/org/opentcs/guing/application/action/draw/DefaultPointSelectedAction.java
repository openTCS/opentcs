/*
 * openTCS copyright information:
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action.draw;

import java.awt.event.ActionEvent;
import static java.util.Objects.requireNonNull;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.action.AbstractSelectedAction;
import org.jhotdraw.draw.tool.CreationTool;
import org.jhotdraw.draw.tool.Tool;
import org.jhotdraw.gui.JPopupButton;
import org.opentcs.guing.components.drawing.figures.LabeledPointFigure;
import org.opentcs.guing.components.drawing.figures.PointFigure;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * This action manages the behaviour when the user selects the point button.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DefaultPointSelectedAction
    extends AbstractSelectedAction {

  /**
   * The SelectionProperty contains all point types in the model.
   */
  private PointModel.PointType pointType;

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
  public DefaultPointSelectedAction(DrawingEditor editor,
                                    Tool tool,
                                    JPopupButton popupButton,
                                    ButtonGroup group) {
    super(editor);
    this.tool = requireNonNull(tool);
    this.popupButton = requireNonNull(popupButton);
    this.group = requireNonNull(group);
  }

  /**
   * Constructor for a button inside a drop down menu of another button.
   *
   * @param editor The drawing editor
   * @param tool The tool
   * @param pointType The point type
   * @param popupButton The popup button
   * @param group The button group
   */
  public DefaultPointSelectedAction(DrawingEditor editor,
                                    Tool tool,
                                    PointModel.PointType pointType,
                                    JPopupButton popupButton,
                                    ButtonGroup group) {
    super(editor);
    this.pointType = pointType;
    this.tool = requireNonNull(tool);
    this.popupButton = requireNonNull(popupButton);
    this.group = requireNonNull(group);

    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    putValue(AbstractAction.NAME, bundle.getString("point.type." + pointType.name() + ".text"));
    putValue(AbstractAction.SHORT_DESCRIPTION, bundle.getString("point.type." + pointType.name() + ".toolTipText"));
    ImageIcon icon = new ImageIcon(getClass().getResource(bundle.getString("point.type." + pointType.name() + ".popupIcon")));
    putValue(AbstractAction.SMALL_ICON, icon);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (pointType != null) {
      CreationTool creationTool = (CreationTool) tool;
      LabeledPointFigure lpf = (LabeledPointFigure) creationTool.getPrototype();
      PointFigure pointFigure = lpf.getPresentationFigure();
      pointFigure.getModel().getPropertyType().setValue(pointType);

      ResourceBundleUtil.getBundle().configureNamelessButton(popupButton, "point.type." + pointType.name());
    }

    getEditor().setTool(tool);
    group.setSelected(popupButton.getModel(), true);
  }

  @Override
  protected void updateEnabledState() {
    setEnabled(getView() != null && getView().isEnabled());
  }
}
