/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.drawing;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import static java.util.Objects.requireNonNull;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * A placard panel for drawing views.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DrawingViewPlacardPanel
    extends JPanel {

  private static final String toggleGridActionID = "view.toggleGrid";
  private static final String toggleRulersActionID = "view.toggleRulers";
  private static final String toggleLabelsActionID = "view.toggleLabels";
  private static final String toggleBlocksActionID = "view.toggleBlocks";
  private static final String toggleStaticRoutesActionID = "view.toggleStaticRoutes";
  /**
   * The drawing view.
   */
  private final OpenTCSDrawingView drawingView;
  /**
   * A combo box for selecting the zoom level.
   */
  private final JComboBox<ZoomItem> zoomComboBox;
  /**
   * A toggle button for turning rulers on/off.
   */
  private final JToggleButton toggleRulersButton;

  /**
   * Creates a new instance.
   *
   * @param drawingView The drawing view.
   */
  public DrawingViewPlacardPanel(OpenTCSDrawingView drawingView) {
    this.drawingView = requireNonNull(drawingView, "drawingView");

    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

    this.zoomComboBox = zoomComboBox(drawingView);
    this.add(zoomComboBox);

    this.add(zoomViewToWindowButton(drawingView));

    // Show/hide grid
    JToggleButton toggleConstrainerButton = toggleConstrainerButton(drawingView);
    toggleConstrainerButton.setSelected(drawingView.isConstrainerVisible());
    this.add(toggleConstrainerButton);

    // Show/hide rulers
    toggleRulersButton = toggleRulersButton();
    this.add(toggleRulersButton);

    // Show/hide leabels
    JToggleButton toggleLabelsButton = toggleLabelsButton(drawingView);
    toggleLabelsButton.setSelected(drawingView.isLabelsVisible());
    this.add(toggleLabelsButton);

    // Show/hide blocks
    JToggleButton toggleBlocksButton = toggleBlocksButton(drawingView);
    toggleBlocksButton.setSelected(drawingView.isBlocksVisible());
    this.add(toggleBlocksButton);

    // Show/hide static routes
    JToggleButton toggleStaticRoutesButton = toggleStaticRoutesButton(drawingView);
    toggleStaticRoutesButton.setSelected(drawingView.isStaticRoutesVisible());
    this.add(toggleStaticRoutesButton);

  }

  public JComboBox<ZoomItem> getZoomComboBox() {
    return zoomComboBox;
  }

  public JToggleButton getToggleRulersButton() {
    return toggleRulersButton;
  }

  /**
   * Creates the combo box with different zoom factors.
   *
   * @param drawingView The DrawingView this combo box will belong to.
   * @return The created combo box.
   */
  private JComboBox<ZoomItem> zoomComboBox(final OpenTCSDrawingView drawingView) {
    final JComboBox<ZoomItem> comboBox = new JComboBox<>();
    comboBox.setEditable(true);
    comboBox.setFocusable(true);

    final double[] scaleFactors = {
      5.00, 4.00, 3.00, 2.00, 1.50, 1.25, 1.00, 0.75, 0.50, 0.25, 0.10
    };
    for (int i = 0; i < scaleFactors.length; i++) {
      comboBox.addItem(new ZoomItem(scaleFactors[i]));

      if (scaleFactors[i] == 1.0) {
        comboBox.setSelectedIndex(i);
      }
    }

    comboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final double scaleFactor;

        if (comboBox.getSelectedItem() instanceof ZoomItem) {
          // A zoom step of the array scaleFactors[]
          ZoomItem item = (ZoomItem) comboBox.getSelectedItem();
          scaleFactor = item.getScaleFactor();
        }
        else {
          try {
            // Text input in the combo box
            String text = (String) comboBox.getSelectedItem();
            double factor = Double.parseDouble(text.split(" ")[0]);
            scaleFactor = factor * 0.01;  // Eingabe in %
            comboBox.setSelectedItem((int) (factor + 0.5) + " %");
          }
          catch (NumberFormatException ex) {
            comboBox.setSelectedIndex(0);
            return;
          }
        }

        drawingView.setScaleFactor(scaleFactor);
      }
    });

    drawingView.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        // String constants are interned
        if ("scaleFactor".equals(evt.getPropertyName())) {
          double scaleFactor = (double) evt.getNewValue();

          for (int i = 0; i < comboBox.getItemCount(); i++) {
            // One of the predefined scale factors was selected
            if (scaleFactor == comboBox.getItemAt(i).getScaleFactor()) {
              comboBox.setSelectedIndex(i);
              break;
            }

            if (i + 1 < comboBox.getItemCount()
                && scaleFactor < comboBox.getItemAt(i).getScaleFactor()
                && scaleFactor > comboBox.getItemAt(i + 1).getScaleFactor()) {
              // Insert the new scale factor between the next smaller / larger entries
              ZoomItem newItem = new ZoomItem(scaleFactor);
              comboBox.insertItemAt(newItem, i + 1);
              comboBox.setSelectedItem(newItem);
            }
            else if (scaleFactor > comboBox.getItemAt(0).getScaleFactor()) {
              // Insert new item for scale factor larger than the largest predefined factor
              ZoomItem newItem = new ZoomItem(scaleFactor);
              comboBox.insertItemAt(newItem, 0);
              comboBox.setSelectedItem(newItem);
            }
            else if (scaleFactor < comboBox.getItemAt(comboBox.getItemCount() - 1).getScaleFactor()) {
              // Insert new item for scale factor larger than the largest predefined factor
              ZoomItem newItem = new ZoomItem(scaleFactor);
              comboBox.insertItemAt(newItem, comboBox.getItemCount());
              comboBox.setSelectedItem(newItem);
            }
          }
        }
      }
    });

    return comboBox;
  }

  /**
   * Creates a button that zooms the drawing to a scale factor so that
   * it fits the window size.
   *
   * @return The created button.
   */
  private JButton zoomViewToWindowButton(final OpenTCSDrawingView drawingView) {
    final JButton button = new JButton();
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    labels.configureToolBarButton(button, "view.zoomViewToWindow");
    button.setMargin(new Insets(0, 0, 0, 0));
    button.setFocusable(false);

    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        drawingView.zoomViewToWindow();
      }
    });

    return button;
  }

  /**
   * Creates a button to toggle the grid in the drawing.
   *
   * @param view The DrawingView the button will belong to.
   * @return The created button.
   */
  private JToggleButton toggleConstrainerButton(final OpenTCSDrawingView drawingView) {
    final JToggleButton toggleButton = new JToggleButton();
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    labels.configureToolBarButton(toggleButton, toggleGridActionID);
    toggleButton.setMargin(new Insets(0, 0, 0, 0));
    toggleButton.setFocusable(false);

    toggleButton.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent event) {
        drawingView.setConstrainerVisible(toggleButton.isSelected());
      }
    });

    return toggleButton;
  }

  /**
   * Creates a button to toggle the rulers in the drawing.
   *
   * @return The created button.
   */
  private JToggleButton toggleRulersButton() {
    final JToggleButton toggleButton = new JToggleButton();
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    labels.configureToolBarButton(toggleButton, toggleRulersActionID);
    toggleButton.setMargin(new Insets(0, 0, 0, 0));
    toggleButton.setFocusable(false);

    return toggleButton;
  }

  /**
   * Creates a button to toglle the labels.
   *
   * @param view The DrawingView the button will belong to.
   * @return The created button.
   */
  private JToggleButton toggleLabelsButton(final OpenTCSDrawingView drawingView) {
    final JToggleButton toggleButton = new JToggleButton();
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    labels.configureToolBarButton(toggleButton, toggleLabelsActionID);
    toggleButton.setMargin(new Insets(0, 0, 0, 0));
    toggleButton.setFocusable(false);

    toggleButton.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent event) {
        drawingView.setLabelsVisible(toggleButton.isSelected());
      }
    });

    return toggleButton;
  }

  /**
   * Creates a button to toggle the blocks in the drawing.
   *
   * @param view The DrawingView the button will belong to.
   * @return The created button.
   */
  private JToggleButton toggleBlocksButton(final OpenTCSDrawingView drawingView) {
    final JToggleButton toggleButton = new JToggleButton();
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    labels.configureToolBarButton(toggleButton, toggleBlocksActionID);
    toggleButton.setMargin(new Insets(0, 0, 0, 0));
    toggleButton.setFocusable(false);

    toggleButton.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent event) {
        drawingView.setBlocksVisible(toggleButton.isSelected());
      }
    });

    return toggleButton;
  }

  /**
   * Creates a button to toggle the static routes in the drawing.
   *
   * @param view The DrawingView the button will belong to.
   * @return The created button.
   */
  private JToggleButton toggleStaticRoutesButton(final OpenTCSDrawingView drawingView) {
    final JToggleButton toggleButton = new JToggleButton();
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    labels.configureToolBarButton(toggleButton, toggleStaticRoutesActionID);
    toggleButton.setMargin(new Insets(0, 0, 0, 0));
    toggleButton.setFocusable(false);

    toggleButton.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent event) {
        drawingView.setStaticRoutesVisible(toggleButton.isSelected());
      }
    });

    return toggleButton;
  }

}
