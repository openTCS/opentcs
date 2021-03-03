/*
 * (c): IML.
 * 
 *
 * Created on 15.08.2012 10:07:03
 */
package org.opentcs.guing.application.action.draw;

import java.awt.event.ActionEvent;
import java.util.Objects;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.tool.CreationTool;
import org.jhotdraw.draw.tool.Tool;
import org.jhotdraw.gui.JPopupButton;
import org.opentcs.guing.components.drawing.figures.LabeledPointFigure;
import org.opentcs.guing.components.drawing.figures.PointFigure;
import org.opentcs.guing.components.properties.type.Property;
import org.opentcs.guing.components.properties.type.SelectionProperty;
import org.opentcs.guing.model.elements.PointModel;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * This action manages the behaviour when the user selects the point button.
 *
 * @author Philipp Seifert (Fraunhofer IML)
 */
public class DefaultPointSelectedAction
		extends org.jhotdraw.draw.action.AbstractSelectedAction {

	/**
	 * The SelectionProperty contains all point types in the model.
	 */
  private PointModel.PointType pointType;
//	private SelectionProperty types;
	/**
	 * The point this action is representing.
	 */
//	private Object type;
//	private String strType;
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
	 * @param editor
	 * @param tool
	 * @param popupButton
	 * @param group
	 */
	public DefaultPointSelectedAction(
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
	 * @param editor
	 * @param tool
	 * @param pointType
	 * @param popupButton
	 * @param group
	 */
	public DefaultPointSelectedAction(
			DrawingEditor editor,
			Tool tool,
      PointModel.PointType pointType,
			JPopupButton popupButton,
			ButtonGroup group) {

		super(editor);
		this.pointType = pointType;
		Objects.requireNonNull(tool);
		this.tool = tool;
		Objects.requireNonNull(popupButton);
		this.popupButton = popupButton;
		Objects.requireNonNull(group);
		this.group = group;

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
      PointFigure pointFigure = (PointFigure) lpf.getPresentationFigure();
      SelectionProperty pType = (SelectionProperty) pointFigure.getModel().getProperty(PointModel.TYPE);
      pType.setValue(pointType);

      ResourceBundleUtil.getBundle().configureNamelessButton(popupButton, "point.type." + pointType.name());
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
