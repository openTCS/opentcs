/**
 * (c): IML.
 *
 */
package org.opentcs.guing.application.action.file;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.opentcs.guing.application.GuiManager;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 *
 * @author Heinz Huber (Fraunhofer IML)
 */
public class SaveModelAction
    extends AbstractAction {

  public final static String ID = "file.saveModel";
  private final GuiManager view;

  /**
   * Creates a new instance.
   *
   * @param view
   */
  public SaveModelAction(GuiManager view) {
    this.view = view;
    ResourceBundleUtil.getBundle().configureAction(this, ID);
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    view.saveModel();
  }
}
