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
import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import net.engio.mbassy.bus.MBassador;
import org.opentcs.access.Kernel;
import org.opentcs.access.SharedKernelProvider;
import org.opentcs.data.model.visualization.ViewBookmark;
import org.opentcs.data.model.visualization.VisualLayout;
import org.opentcs.guing.application.ApplicationFrame;
import org.opentcs.guing.components.dialogs.StandardDialog;
import org.opentcs.guing.components.drawing.OpenTCSDrawingEditor;
import org.opentcs.guing.event.ModelModificationEvent;
import org.opentcs.guing.storage.ViewBookmarkNameChooser;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 *
 * @author Heinz Huber (Fraunhofer IML)
 * @author Stefan Walter (Fraunhofer IML)
 */
public class SaveViewBookmarkAction
    extends AbstractAction {

  /**
   * This action's ID.
   */
  public final static String ID = "view.saveViewBookmark";
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
   * The application's event bus for notifying listeners about changes to the
   * system model.
   */
  private final MBassador<Object> eventBus;

  /**
   * Creates a new instance.
   *
   * @param drawingEditor The drawing editor.
   * @param dialogParent The parent component for dialogs shown by this action.
   * @param kernelProvider Provides access to a kernel.
   * @param eventBus The application's event bus for notifying listeners about
   * changes to the system model.
   */
  @Inject
  public SaveViewBookmarkAction(OpenTCSDrawingEditor drawingEditor,
                                @ApplicationFrame Component dialogParent,
                                SharedKernelProvider kernelProvider,
                                MBassador<Object> eventBus) {
    this.drawingEditor = requireNonNull(drawingEditor, "drawingEditor");
    this.dialogParent = requireNonNull(dialogParent, "dialogParent");
    this.kernelProvider = requireNonNull(kernelProvider, "kernelProvider");
    this.eventBus = requireNonNull(eventBus, "eventBus");

    ResourceBundleUtil.getBundle().configureAction(this, ID);
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    final Object kernelClient = new Object();
    try {
      kernelProvider.register(kernelClient);

      saveViewBookmark(kernelProvider.getKernel());
    }
    finally {
      kernelProvider.unregister(kernelClient);
    }
  }

  private void saveViewBookmark(Kernel kernel) {
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

    // Die View Bookmarks für dieses Layout
    List<ViewBookmark> bookmarks = new ArrayList<>(layout.getViewBookmarks());

    ViewBookmarkNameChooser content
        = new ViewBookmarkNameChooser(bookmarks);
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    String title = bundle.getString("view.saveViewBookmark.text");
    StandardDialog dialog = new StandardDialog(dialogParent, true, content, title);
    dialog.setLocationRelativeTo(dialogParent);
    dialog.setVisible(true);

    if (dialog.getReturnStatus() != StandardDialog.RET_OK) {
      return;
    }

    String name = content.getChosenName();
    String nameInvalid = bundle.getString("message.nameExists");

    if (name.isEmpty()) {
      String enterName = bundle.getString("message.enterName");
      JOptionPane.showMessageDialog(dialogParent,
                                    enterName,
                                    nameInvalid,
                                    JOptionPane.ERROR_MESSAGE);
      return;
    }

    for (ViewBookmark bm : bookmarks) {
      if (bm.getLabel().equals(name)) {
        int result
            = JOptionPane.showConfirmDialog(dialogParent,
                                            bundle.getString("message.bookmark.nameExists"),
                                            nameInvalid,
                                            JOptionPane.ERROR_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
          bookmarks.remove(bm);
        }
        else {
          return;
        }
      }
    }

    ViewBookmark bookmark = drawingEditor.getActiveView().bookmark();
    bookmark.setLabel(name);

    bookmarks.add(bookmark);

    kernel.setVisualLayoutViewBookmarks(layout.getReference(), bookmarks);

    eventBus.publish(new ModelModificationEvent(this));
  }
}
