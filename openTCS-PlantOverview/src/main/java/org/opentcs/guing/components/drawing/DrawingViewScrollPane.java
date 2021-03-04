/*
 * openTCS copyright information:
 * Copyright (c) 2014 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */
package org.opentcs.guing.components.drawing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.EventObject;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import org.jhotdraw.gui.PlacardScrollPaneLayout;
import org.opentcs.guing.components.drawing.course.Origin;
import org.opentcs.guing.components.drawing.course.OriginChangeListener;

/**
 * A custom scroll pane to wrap an <code>OpenTCSDrawingView</code>.
 *
 * @author Stefan Walter (Fraunhofer IML)
 */
public class DrawingViewScrollPane
    extends JScrollPane
    implements OriginChangeListener {

  /**
   * The drawing view.
   */
  private final OpenTCSDrawingView drawingView;
  /**
   * The view's placard panel.
   */
  private final DrawingViewPlacardPanel placardPanel;
  /**
   * Whether the rulers are currently visible or not.
   */
  private boolean rulersVisible = true;
  private Origin origin = new Origin();

  /**
   * Creates a new instance.
   *
   * @param drawingView The drawing view.
   * @param placardPanel The view's placard panel.
   */
  public DrawingViewScrollPane(OpenTCSDrawingView drawingView,
                               DrawingViewPlacardPanel placardPanel) {
    this.drawingView = requireNonNull(drawingView, "drawingView");
    this.placardPanel = requireNonNull(placardPanel, "placardPanel");

    setViewport(new JViewport());
    getViewport().setView(drawingView);
    setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    setViewportBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
    setHorizontalScrollBar(new PlacardScrollbar());
    setLayout(new PlacardScrollPaneLayout());
    setBorder(new EmptyBorder(0, 0, 0, 0));

    // Horizontal and vertical rulers
    Ruler.Horizontal newHorizontalRuler = new Ruler.Horizontal(drawingView);
    drawingView.addPropertyChangeListener(newHorizontalRuler);
    newHorizontalRuler.setPreferredWidth(drawingView.getWidth());
    Ruler.Vertical newVerticalRuler = new Ruler.Vertical(drawingView);
    drawingView.addPropertyChangeListener(newVerticalRuler);
    newVerticalRuler.setPreferredHeight(drawingView.getHeight());
    setColumnHeaderView(newHorizontalRuler);
    setRowHeaderView(newVerticalRuler);

    this.add(placardPanel, JScrollPane.LOWER_LEFT_CORNER);

    // Register handler for rulers toggle button.
    placardPanel.getToggleRulersButton().addItemListener(
        new RulersToggleListener(placardPanel.getToggleRulersButton()));
    placardPanel.getToggleRulersButton().setSelected(rulersVisible);
  }

  public OpenTCSDrawingView getDrawingView() {
    return drawingView;
  }

  public DrawingViewPlacardPanel getPlacardPanel() {
    return placardPanel;
  }

  public Ruler.Horizontal getHorizontalRuler() {
    return (Ruler.Horizontal) getColumnHeader().getView();
  }

  public Ruler.Vertical getVerticalRuler() {
    return (Ruler.Vertical) getRowHeader().getView();
  }

  public boolean isRulersVisible() {
    return rulersVisible;
  }

  public void setRulersVisible(boolean visible) {
    this.rulersVisible = visible;
    if (visible) {
      getHorizontalRuler().setVisible(true);
      getHorizontalRuler().setPreferredWidth(getWidth());
      getVerticalRuler().setVisible(true);
      getVerticalRuler().setPreferredHeight(getHeight());
      getPlacardPanel().getToggleRulersButton().setSelected(true);
    }
    else {
      getHorizontalRuler().setVisible(false);
      getHorizontalRuler().setPreferredSize(new Dimension(0, 0));
      getVerticalRuler().setVisible(false);
      getVerticalRuler().setPreferredSize(new Dimension(0, 0));
      getPlacardPanel().getToggleRulersButton().setSelected(false);
    }
  }

  public void originChanged(@Nonnull Origin origin) {
    requireNonNull(origin, "origin");
    if (origin == this.origin) {
      return;
    }

    this.origin.removeListener(getHorizontalRuler());
    this.origin.removeListener(getVerticalRuler());
    this.origin.removeListener(this);
    this.origin = origin;

    origin.addListener(getHorizontalRuler());
    origin.addListener(getVerticalRuler());
    origin.addListener(this);

    // Notify the rulers directly. This is necessary to initialize/update the rulers scale when a 
    // model is created or loaded.
    // Calling origin.notifyScaleChanged() would lead to all model elements being notified (loading
    // times for bigger models would suffer).
    getHorizontalRuler().originScaleChanged(new EventObject(origin));
    getVerticalRuler().originScaleChanged(new EventObject(origin));
  }

  @Override
  public void originLocationChanged(EventObject evt) {

  }

  @Override
  public void originScaleChanged(EventObject evt) {
    drawingView.revalidate();
  }

  private class PlacardScrollbar
      extends JScrollBar {

    public PlacardScrollbar() {
      super(JScrollBar.HORIZONTAL);
      setPreferredSize(new Dimension(100, 18));
    }
  }

  private class RulersToggleListener
      implements ItemListener {

    private final JToggleButton rulersButton;

    public RulersToggleListener(JToggleButton rulersButton) {
      this.rulersButton = requireNonNull(rulersButton, "rulersButton");
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
      setRulersVisible(rulersButton.isSelected());
    }
  }
}
