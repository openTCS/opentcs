/*
 * openTCS copyright information:
 * Copyright (c) 2013 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.application.action.view;

import java.awt.Component;
import java.awt.event.ActionEvent;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.AbstractAction;
import org.opentcs.access.Kernel;
import org.opentcs.access.SharedKernelProvider;
import org.opentcs.data.model.visualization.ViewBookmark;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.guing.application.ApplicationFrame;
import org.opentcs.guing.components.dialogs.BookmarkSelectionPanel;
import org.opentcs.guing.components.dialogs.StandardContentDialog;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * An action to load a view bookmark.
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class LoadViewBookmarkAction
    extends AbstractAction {

  /**
   * This action's ID.
   */
  public final static String ID = "view.loadViewBookmark";
  /**
   * The drawing editor.
   */
  private final OpenTCSDrawingEditor drawingEditor;
  /**
   * The parent component for dialogs shown by this action.
   */
  private final Component dialogParent;
  /**
   * Provides access to a kernel.
   */
  private final SharedKernelProvider kernelProvider;

  /**
   * Creates a new instance.
   *
   * @param drawingEditor The drawing editor.
   * @param dialogParent The parent component for dialogs shown by this action.
   * @param kernelProvider Provides access to a kernel.
   */
  @Inject
  public LoadViewBookmarkAction(OpenTCSDrawingEditor drawingEditor,
                                @ApplicationFrame Component dialogParent,
                                SharedKernelProvider kernelProvider) {
    this.drawingEditor = requireNonNull(drawingEditor, "drawingEditor");
    this.dialogParent = requireNonNull(dialogParent, "dialogParent");
    this.kernelProvider = requireNonNull(kernelProvider, "kernelProvider");

    ResourceBundleUtil.getBundle().configureAction(this, ID);
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    final Object kernelClient = new Object();
    try {
      kernelProvider.register(kernelClient);

      loadViewBookmark(kernelProvider.getKernel());
    }
    finally {
      kernelProvider.unregister(kernelClient);
    }
  }

  private void loadViewBookmark(Kernel kernel) {
    if (kernel == null) {
      return;
    }

    // Das Kernel-Objekt für das aktuelle VisualLayout
    // Beim starten der Visualisierung im Operating-Mode wird kein VisualLayout erzeugt!
    Set<VisualLayout> sLayouts = kernel.getTCSObjects(VisualLayout.class);

    // Currently, there should be exactly one layout.
    if (sLayouts == null || sLayouts.isEmpty()) {
      throw new IllegalStateException("There is no layout");
    }

    VisualLayout layout = sLayouts.iterator().next();

    BookmarkSelectionPanel contentPanel
        = new BookmarkSelectionPanel(layout.getViewBookmarks(), false);
    StandardContentDialog dialog = new StandardContentDialog(dialogParent,
                                                             contentPanel);
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    dialog.setTitle(bundle.getString("view.loadViewBookmark.text"));
    dialog.setVisible(true);

    if (dialog.getReturnStatus() != StandardContentDialog.RET_OK) {
      return;
    }
    ViewBookmark selectedBookmark = contentPanel.getSelectedItem();
    if (selectedBookmark == null) {
      return;
    }

    // Adjust view
    drawingEditor.getActiveView().scaleAndScrollTo(selectedBookmark);
  }
}
