/**
 * (c): IML, JHotDraw.
 *
 */
package org.opentcs.guing.application.action;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.text.StyledEditorKit;
import org.jhotdraw.app.Disposable;
import org.jhotdraw.app.action.ActionUtil;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.action.AttributeToggler;
import org.jhotdraw.draw.action.EditorColorIcon;
import org.jhotdraw.draw.action.FontChooserHandler;
import org.jhotdraw.draw.action.LineDecorationIcon;
import org.jhotdraw.draw.action.StrokeIcon;
import org.jhotdraw.draw.decoration.ArrowTip;
import org.jhotdraw.draw.decoration.LineDecoration;
import org.jhotdraw.geom.DoubleStroke;
import org.jhotdraw.gui.JComponentPopup;
import org.jhotdraw.gui.JFontChooser;
import org.jhotdraw.gui.JPopupButton;
import org.jhotdraw.util.Images;
import org.opentcs.guing.application.action.draw.AlignAction;
import org.opentcs.guing.application.action.draw.ApplyAttributesAction;
import org.opentcs.guing.application.action.draw.AttributeAction;
import org.opentcs.guing.application.action.draw.BringToFrontAction;
import org.opentcs.guing.application.action.draw.ColorIcon;
import org.opentcs.guing.application.action.draw.DefaultAttributeAction;
import org.opentcs.guing.application.action.draw.EditorColorChooserAction;
import org.opentcs.guing.application.action.draw.MoveAction;
import org.opentcs.guing.application.action.draw.PickAttributesAction;
import org.opentcs.guing.application.action.draw.SendToBackAction;
import org.opentcs.guing.util.I18nPlantOverview;
import org.opentcs.guing.util.ImageDirectory;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * ButtonFactory.
 * <p>
 * Design pattern:<br> Name: Abstract Factory.<br> Role:
 * Abstract Factory.<br> Partners: {@link org.jhotdraw.samples.draw.DrawApplicationModel}
 * as Client,
 * {@link org.jhotdraw.samples.draw.DrawView} as Client,
 * {@link org.jhotdraw.samples.draw.DrawingPanel} as Client.
 *
 * FIXME - All buttons created using the ButtonFactory must automatically become
 * disabled/enabled, when the DrawingEditor is disabled/enabled.
 *
 * @author Werner Randelshofer
 */
public class ButtonFactory {

  /**
   * Mac OS X 'Apple Color Palette'. This palette has 8 columns.
   */
  private final static java.util.List<ColorIcon> DEFAULT_COLOR_ICONS;

  static {
    LinkedList<ColorIcon> m = new LinkedList<>();
    m.add(new ColorIcon(0x800000, "Cayenne"));
    m.add(new ColorIcon(0x808000, "Asparagus"));
    m.add(new ColorIcon(0x008000, "Clover"));
    m.add(new ColorIcon(0x008080, "Teal"));
    m.add(new ColorIcon(0x000080, "Midnight"));
    m.add(new ColorIcon(0x800080, "Plum"));
    m.add(new ColorIcon(0x7f7f7f, "Tin"));
    m.add(new ColorIcon(0x808080, "Nickel"));
    m.add(new ColorIcon(0xff0000, "Maraschino"));
    m.add(new ColorIcon(0xffff00, "Lemon"));
    m.add(new ColorIcon(0x00ff00, "Spring"));
    m.add(new ColorIcon(0x00ffff, "Turquoise"));
    m.add(new ColorIcon(0x0000ff, "Blueberry"));
    m.add(new ColorIcon(0xff00ff, "Magenta"));
    m.add(new ColorIcon(0x666666, "Steel"));
    m.add(new ColorIcon(0x999999, "Aluminium"));
    m.add(new ColorIcon(0xff6666, "Salmon"));
    m.add(new ColorIcon(0xffff66, "Banana"));
    m.add(new ColorIcon(0x66ff66, "Flora"));
    m.add(new ColorIcon(0x66ffff, "Ice"));
    m.add(new ColorIcon(0x6666ff, "Orchid"));
    m.add(new ColorIcon(0xff66ff, "Bubblegum"));
    m.add(new ColorIcon(0x4c4c4c, "Iron"));
    m.add(new ColorIcon(0xb3b3b3, "Magnesium"));
    m.add(new ColorIcon(0x804000, "Mocha"));
    m.add(new ColorIcon(0x408000, "Fern"));
    m.add(new ColorIcon(0x008040, "Moss"));
    m.add(new ColorIcon(0x004080, "Ocean"));
    m.add(new ColorIcon(0x400080, "Eggplant"));
    m.add(new ColorIcon(0x800040, "Maroon"));
    m.add(new ColorIcon(0x333333, "Tungsten"));
    m.add(new ColorIcon(0xcccccc, "Silver"));
    m.add(new ColorIcon(0xff8000, "Tangerine"));
    m.add(new ColorIcon(0x80ff00, "Lime"));
    m.add(new ColorIcon(0x00ff80, "Sea Foam"));
    m.add(new ColorIcon(0x0080ff, "Aqua"));
    m.add(new ColorIcon(0x8000ff, "Grape"));
    m.add(new ColorIcon(0xff0080, "Strawberry"));
    m.add(new ColorIcon(0x191919, "Lead"));
    m.add(new ColorIcon(0xe6e6e6, "Mercury"));
    m.add(new ColorIcon(0xffcc66, "Cantaloupe"));
    m.add(new ColorIcon(0xccff66, "Honeydew"));
    m.add(new ColorIcon(0x66ffcc, "Spindrift"));
    m.add(new ColorIcon(0x66ccff, "Sky"));
    m.add(new ColorIcon(0xcc66ff, "Lavender"));
    m.add(new ColorIcon(0xff6fcf, "Carnation"));
    m.add(new ColorIcon(0x000000, "Licorice"));
    m.add(new ColorIcon(0xffffff, "Snow"));
    DEFAULT_COLOR_ICONS = Collections.unmodifiableList(m);
  }
  public final static int DEFAULT_COLORS_COLUMN_COUNT = 8;

  private static final ResourceBundleUtil BUNDLE
      = ResourceBundleUtil.getBundle(I18nPlantOverview.TOOLBAR_PATH);

  /**
   * Prevent instance creation.
   */
  private ButtonFactory() {
  }

  /**
   * Creates toolbar buttons and adds them to the specified JToolBar
   *
   * @param toolBar
   * @param editor
   */
  public static void addAttributesButtonsTo(
      JToolBar toolBar,
      DrawingEditor editor) {

    JButton button;

    button = toolBar.add(new PickAttributesAction(editor));
    button.setFocusable(false);

    button = toolBar.add(new ApplyAttributesAction(editor));
    button.setFocusable(false);

    toolBar.addSeparator();

    addColorButtonsTo(toolBar, editor);

    toolBar.addSeparator();

    addStrokeButtonsTo(toolBar, editor);

    toolBar.addSeparator();

    addFontButtonsTo(toolBar, editor);
  }

  private static void addColorButtonsTo(JToolBar bar, DrawingEditor editor) {
    bar.add(createEditorColorButton(editor,
                                    AttributeKeys.STROKE_COLOR,
                                    BUNDLE.getString("buttonFactory.button_lineColor.tooltipText"),
                                    ImageDirectory.getImageIcon("/toolbar/attributeStrokeColor.png"),
                                    new HashMap<AttributeKey, Object>()));
    bar.add(createEditorColorButton(editor,
                                    AttributeKeys.FILL_COLOR,
                                    BUNDLE.getString("buttonFactory.button_fillColor.tooltipText"),
                                    ImageDirectory.getImageIcon("/toolbar/attributeFillColor.png"),
                                    new HashMap<AttributeKey, Object>()));
    bar.add(createEditorColorButton(editor,
                                    AttributeKeys.TEXT_COLOR,
                                    BUNDLE.getString("buttonFactory.button_textlColor.tooltipText"),
                                    ImageDirectory.getImageIcon("/toolbar/attributeTextColor.png"),
                                    new HashMap<AttributeKey, Object>()));
  }

  /**
   * Creates a color button, with an action region and a popup menu. The button
   * works like the color button in Microsoft Office: <ul> <li>When the user
   * clicks on the action region, the default color of the DrawingEditor is
   * applied to the selected figures.</li> <li>When the user opens the popup
   * menu, a color palette is displayed. Choosing a color from the palette
   * changes the default color of the editor and also changes the color of the
   * selected figures.</li> <li>A shape on the color button displays the current
   * default color of the DrawingEditor.</li> </ul>
   *
   * @param editor The DrawingEditor.
   * @param attributeKey The AttributeKey of the color.
   * @param toolTipText The tooltip text.
   * @param defaultAttributes A set of attributes which are also applied to the
   * selected figures, when a color is selected. This can be used, to set
   * attributes that otherwise prevent the color from being shown. For example,
   * when the color attribute is set, we wan't the gradient attribute of the
   * Figure to be cleared.
   * @return
   */
  private static JPopupButton createEditorColorButton(
      DrawingEditor editor,
      AttributeKey<Color> attributeKey,
      String toolTipText,
      ImageIcon baseIcon,
      Map<AttributeKey, Object> defaultAttributes) {
    final JPopupButton popupButton = new JPopupButton();
    popupButton.setPopupAlpha(1f);

    popupButton.setAction(
        new DefaultAttributeAction(editor, attributeKey, defaultAttributes),
        new Rectangle(0, 0, 22, 22));
    popupButton.setColumnCount(DEFAULT_COLORS_COLUMN_COUNT, false);
    boolean hasNullColor = false;

    for (ColorIcon swatch : DEFAULT_COLOR_ICONS) {
      HashMap<AttributeKey, Object> attributes = new HashMap<>(defaultAttributes);
      Color swatchColor = swatch.getColor();
      attributes.put(attributeKey, swatchColor);

      if (swatchColor == null || swatchColor.getAlpha() == 0) {
        hasNullColor = true;
      }

      AttributeAction action = new AttributeAction(editor,
                                                   attributes,
                                                   toolTipText,
                                                   swatch);
      popupButton.add(action);
      action.putValue(Action.SHORT_DESCRIPTION, swatch.getName());
      action.setUpdateEnabledState(false);
    }

    // No color
    if (!hasNullColor) {
      HashMap<AttributeKey, Object> attributes = new HashMap<>(defaultAttributes);
      attributes.put(attributeKey, null);
      AttributeAction action
          = new AttributeAction(editor,
                                attributes,
                                BUNDLE.getString("buttonFactory.action_noColor.name"),
                                new ColorIcon(null,
                                              "",
                                              DEFAULT_COLOR_ICONS.get(0).getIconWidth(),
                                              DEFAULT_COLOR_ICONS.get(0).getIconHeight()));
      popupButton.add(action);
      action.putValue(Action.SHORT_DESCRIPTION,
                      BUNDLE.getString("buttonFactory.action_noColor.shortDescription"));
      action.setUpdateEnabledState(false);
    }

    // Color chooser
    ImageIcon chooserIcon = new ImageIcon(
        Images.createImage(ButtonFactory.class,
                           "/org/jhotdraw/draw/action/images/attribute.color.colorChooser.png"));
    Action action = new EditorColorChooserAction(editor,
                                                 attributeKey,
                                                 "color",
                                                 chooserIcon,
                                                 defaultAttributes);
    popupButton.add(action);
    popupButton.setText(null);
    popupButton.setToolTipText(toolTipText);

    Icon icon = new EditorColorIcon(editor,
                                    attributeKey,
                                    baseIcon.getImage(),
                                    new Rectangle(1, 17, 20, 4));

    popupButton.setIcon(icon);
    popupButton.setDisabledIcon(icon);
    popupButton.setFocusable(false);

    editor.addPropertyChangeListener(new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        popupButton.repaint();
      }
    });

    return popupButton;
  }

  private static void addStrokeButtonsTo(JToolBar bar, DrawingEditor editor) {
    bar.add(createStrokeDecorationButton(editor));
    bar.add(createStrokeWidthButton(editor));
    bar.add(createStrokeDashesButton(editor));
    bar.add(createStrokeTypeButton(editor));
    bar.add(createStrokePlacementButton(editor));
    bar.add(createStrokeCapButton(editor));
    bar.add(createStrokeJoinButton(editor));
  }

  private static JPopupButton createStrokeDecorationButton(DrawingEditor editor) {
    JPopupButton strokeDecorationPopupButton = new JPopupButton();

    strokeDecorationPopupButton.setIcon(
        ImageDirectory.getImageIcon("/toolbar/attributeStrokeDecoration.png"));

    strokeDecorationPopupButton.setText(null);
    strokeDecorationPopupButton.setToolTipText(BUNDLE.getString("buttonFactory.button_strokeDecoration.tooltipText"));

    strokeDecorationPopupButton.setFocusable(false);
    strokeDecorationPopupButton.setColumnCount(2, false);
    LineDecoration[] decorations = {
      // Arrow
      new ArrowTip(0.35, 12, 11.3),
      // Arrow
      new ArrowTip(0.35, 13, 7),
      // Generalization triangle
      new ArrowTip(Math.PI / 5, 12, 9.8, true, true, false),
      // Dependency arrow
      new ArrowTip(Math.PI / 6, 12, 0, false, true, false),
      // Link arrow
      new ArrowTip(Math.PI / 11, 13, 0, false, true, true),
      // Aggregation diamond
      new ArrowTip(Math.PI / 6, 10, 18, false, true, false),
      // Composition diamond
      new ArrowTip(Math.PI / 6, 10, 18, true, true, true),
      null
    };

    for (LineDecoration decoration : decorations) {
      strokeDecorationPopupButton.add(
          new AttributeAction(editor,
                              AttributeKeys.START_DECORATION,
                              decoration,
                              null,
                              new LineDecorationIcon(decoration, true)));
      strokeDecorationPopupButton.add(
          new AttributeAction(editor,
                              AttributeKeys.END_DECORATION,
                              decoration,
                              null,
                              new LineDecorationIcon(decoration, false)));
    }

    return strokeDecorationPopupButton;
  }

  private static JPopupButton createStrokeWidthButton(DrawingEditor editor) {
    JPopupButton strokeWidthPopupButton = new JPopupButton();
    strokeWidthPopupButton.setIcon(ImageDirectory.getImageIcon("/toolbar/attributeStrokeWidth.png"));
    strokeWidthPopupButton.setText(null);
    strokeWidthPopupButton.setToolTipText(BUNDLE.getString("buttonFactory.button_strokeWidth.tooltipText"));
    strokeWidthPopupButton.setFocusable(false);

    NumberFormat formatter = NumberFormat.getInstance();

    if (formatter instanceof DecimalFormat) {
      ((DecimalFormat) formatter).setMaximumFractionDigits(1);
      ((DecimalFormat) formatter).setMinimumFractionDigits(0);
    }

    double[] widths = new double[] {0.5d, 1d, 2d, 3d, 5d, 9d, 13d};

    for (int i = 0; i < widths.length; i++) {
      String label = Double.toString(widths[i]);
      Icon icon = new StrokeIcon(new BasicStroke((float) widths[i],
                                                 BasicStroke.CAP_BUTT,
                                                 BasicStroke.JOIN_BEVEL));
      AttributeAction action = new AttributeAction(
          editor, AttributeKeys.STROKE_WIDTH, new Double(widths[i]), label, icon);
      action.putValue(ActionUtil.UNDO_PRESENTATION_NAME_KEY,
                      BUNDLE.getString("buttonFactory.action_strokeWidth.undo.presentationName"));
      AbstractButton btn = strokeWidthPopupButton.add(action);
      btn.setDisabledIcon(icon);
    }

    return strokeWidthPopupButton;
  }

  private static JPopupButton createStrokeDashesButton(DrawingEditor editor) {
    JPopupButton strokeDashesPopupButton = new JPopupButton();
    strokeDashesPopupButton.setIcon(ImageDirectory.getImageIcon("/toolbar/attributeStrokeDashes.png"));
    strokeDashesPopupButton.setText(null);
    strokeDashesPopupButton.setToolTipText(BUNDLE.getString("buttonFactory.button_strokeDashes.tooltipText"));
    strokeDashesPopupButton.setFocusable(false);

    double[][] dashes = new double[][] {
      null,
      {4d, 4d},
      {2d, 2d},
      {4d, 2d},
      {2d, 4d},
      {8d, 2d},
      {6d, 2d, 2d, 2d},};

    for (double[] dash : dashes) {
      float[] fdashes;

      if (dash == null) {
        fdashes = null;
      }
      else {
        fdashes = new float[dash.length];

        for (int j = 0; j < dash.length; j++) {
          fdashes[j] = (float) dash[j];
        }
      }

      Icon icon = new StrokeIcon(new BasicStroke(2f,
                                                 BasicStroke.CAP_BUTT,
                                                 BasicStroke.JOIN_BEVEL,
                                                 10f,
                                                 fdashes,
                                                 0));
      AbstractButton button = strokeDashesPopupButton.add(
          new AttributeAction(editor, AttributeKeys.STROKE_DASHES, dash, null, icon));
      button.setDisabledIcon(icon);
    }

    return strokeDashesPopupButton;
  }

  private static JPopupButton createStrokeTypeButton(DrawingEditor editor) {
    JPopupButton strokeTypePopupButton = new JPopupButton();
    strokeTypePopupButton.setIcon(ImageDirectory.getImageIcon("/toolbar/attributeStrokeType.png"));
    strokeTypePopupButton.setText(null);
    strokeTypePopupButton.setToolTipText(BUNDLE.getString("buttonFactory.button_strokeType.tooltipText"));
    strokeTypePopupButton.setFocusable(false);

    strokeTypePopupButton.add(
        new AttributeAction(editor,
                            AttributeKeys.STROKE_TYPE,
                            AttributeKeys.StrokeType.BASIC,
                            BUNDLE.getString("buttonFactory.action_strokeTypeBasic.name"),
                            new StrokeIcon(new BasicStroke(1,
                                                           BasicStroke.CAP_BUTT,
                                                           BasicStroke.JOIN_BEVEL))));
    HashMap<AttributeKey, Object> attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_TYPE, AttributeKeys.StrokeType.DOUBLE);
    attributes.put(AttributeKeys.STROKE_INNER_WIDTH_FACTOR, 2d);
    strokeTypePopupButton.add(
        new AttributeAction(editor,
                            attributes,
                            BUNDLE.getString("buttonFactory.action_strokeTypeDouble.name"),
                            new StrokeIcon(new DoubleStroke(2, 1))));
    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_TYPE, AttributeKeys.StrokeType.DOUBLE);
    attributes.put(AttributeKeys.STROKE_INNER_WIDTH_FACTOR, 3d);
    strokeTypePopupButton.add(
        new AttributeAction(editor,
                            attributes,
                            BUNDLE.getString("buttonFactory.action_strokeTypeDouble.name"),
                            new StrokeIcon(new DoubleStroke(3, 1))));
    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_TYPE, AttributeKeys.StrokeType.DOUBLE);
    attributes.put(AttributeKeys.STROKE_INNER_WIDTH_FACTOR, 4d);
    strokeTypePopupButton.add(
        new AttributeAction(editor,
                            attributes,
                            BUNDLE.getString("buttonFactory.action_strokeTypeDouble.name"),
                            new StrokeIcon(new DoubleStroke(4, 1))));

    return strokeTypePopupButton;
  }

  private static JPopupButton createStrokePlacementButton(DrawingEditor editor) {
    JPopupButton strokePlacementPopupButton = new JPopupButton();
    strokePlacementPopupButton.setIcon(ImageDirectory.getImageIcon("/toolbar/attributeStrokePlacement.png"));
    strokePlacementPopupButton.setText(null);
    strokePlacementPopupButton.setToolTipText(BUNDLE.getString("buttonFactory.button_strokePlacement.tooltipText"));
    strokePlacementPopupButton.setFocusable(false);

    HashMap<AttributeKey, Object> attributes;
    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_PLACEMENT, AttributeKeys.StrokePlacement.CENTER);
    attributes.put(AttributeKeys.FILL_UNDER_STROKE, AttributeKeys.Underfill.CENTER);
    strokePlacementPopupButton.add(
        new AttributeAction(editor,
                            attributes,
                            BUNDLE.getString("buttonFactory.action_strokePlacementCenter.name"),
                            null));

    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_PLACEMENT, AttributeKeys.StrokePlacement.INSIDE);
    attributes.put(AttributeKeys.FILL_UNDER_STROKE, AttributeKeys.Underfill.CENTER);
    strokePlacementPopupButton.add(
        new AttributeAction(editor,
                            attributes,
                            BUNDLE.getString("buttonFactory.action_strokePlacementInside.name"),
                            null));

    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_PLACEMENT, AttributeKeys.StrokePlacement.OUTSIDE);
    attributes.put(AttributeKeys.FILL_UNDER_STROKE, AttributeKeys.Underfill.CENTER);
    strokePlacementPopupButton.add(
        new AttributeAction(editor,
                            attributes,
                            BUNDLE.getString("buttonFactory.action_strokePlacementOutside.name"),
                            null));

    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_PLACEMENT, AttributeKeys.StrokePlacement.CENTER);
    attributes.put(AttributeKeys.FILL_UNDER_STROKE, AttributeKeys.Underfill.FULL);
    strokePlacementPopupButton.add(
        new AttributeAction(editor,
                            attributes,
                            BUNDLE.getString("buttonFactory.action_strokePlacementCenterFilled.name"),
                            null));

    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_PLACEMENT, AttributeKeys.StrokePlacement.INSIDE);
    attributes.put(AttributeKeys.FILL_UNDER_STROKE, AttributeKeys.Underfill.FULL);
    strokePlacementPopupButton.add(
        new AttributeAction(editor,
                            attributes,
                            BUNDLE.getString("buttonFactory.action_strokePlacementInsideFilled.name"),
                            null));

    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_PLACEMENT, AttributeKeys.StrokePlacement.OUTSIDE);
    attributes.put(AttributeKeys.FILL_UNDER_STROKE, AttributeKeys.Underfill.FULL);
    strokePlacementPopupButton.add(
        new AttributeAction(editor,
                            attributes,
                            BUNDLE.getString("buttonFactory.action_strokePlacementOutsideFilled.name"),
                            null));

    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_PLACEMENT, AttributeKeys.StrokePlacement.CENTER);
    attributes.put(AttributeKeys.FILL_UNDER_STROKE, AttributeKeys.Underfill.NONE);
    strokePlacementPopupButton.add(
        new AttributeAction(editor,
                            attributes,
                            BUNDLE.getString("buttonFactory.action_strokePlacementCenterUnfilled.name"),
                            null));

    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_PLACEMENT, AttributeKeys.StrokePlacement.INSIDE);
    attributes.put(AttributeKeys.FILL_UNDER_STROKE, AttributeKeys.Underfill.NONE);
    strokePlacementPopupButton.add(
        new AttributeAction(editor,
                            attributes,
                            BUNDLE.getString("buttonFactory.action_strokePlacementInsideUnfilled.name"),
                            null));

    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_PLACEMENT, AttributeKeys.StrokePlacement.OUTSIDE);
    attributes.put(AttributeKeys.FILL_UNDER_STROKE, AttributeKeys.Underfill.NONE);
    strokePlacementPopupButton.add(
        new AttributeAction(editor,
                            attributes,
                            BUNDLE.getString("buttonFactory.action_strokePlacementOutsideUnfilled.name"),
                            null));

    return strokePlacementPopupButton;
  }

  private static JPopupButton createStrokeCapButton(DrawingEditor editor) {
    JPopupButton popupButton = new JPopupButton();
    popupButton.setIcon(ImageDirectory.getImageIcon("/toolbar/attributeStrokeCap.png"));
    popupButton.setText(null);
    popupButton.setToolTipText(BUNDLE.getString("buttonFactory.button_strokeCap.tooltipText"));
    popupButton.setFocusable(false);

    HashMap<AttributeKey, Object> attributes;
    java.util.List<Disposable> dsp = new LinkedList<>();

    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_CAP, BasicStroke.CAP_BUTT);
    AttributeAction action;
    action = new AttributeAction(editor,
                                 attributes,
                                 BUNDLE.getString("buttonFactory.action_strokeCapButt.name"),
                                 null);
    popupButton.add(action);
    dsp.add(action);

    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_CAP, BasicStroke.CAP_ROUND);
    action = new AttributeAction(editor,
                                 attributes,
                                 BUNDLE.getString("buttonFactory.action_strokeCapRound.name"),
                                 null);
    popupButton.add(action);
    dsp.add(action);

    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_CAP, BasicStroke.CAP_SQUARE);
    action = new AttributeAction(editor,
                                 attributes,
                                 BUNDLE.getString("buttonFactory.action_strokeCapSquare.name"),
                                 null);
    popupButton.add(action);
    dsp.add(action);

    return popupButton;
  }

  private static JPopupButton createStrokeJoinButton(DrawingEditor editor) {
    JPopupButton popupButton = new JPopupButton();
    popupButton.setIcon(ImageDirectory.getImageIcon("/toolbar/attributeStrokeJoin.png"));
    popupButton.setText(null);
    popupButton.setToolTipText(BUNDLE.getString("buttonFactory.button_strokeJoin.tooltipText"));
    popupButton.setFocusable(false);

    HashMap<AttributeKey, Object> attributes;
    java.util.List<Disposable> dsp = new LinkedList<>();

    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_JOIN, BasicStroke.JOIN_BEVEL);
    AttributeAction action;
    action = new AttributeAction(editor,
                                 attributes,
                                 BUNDLE.getString("buttonFactory.action_strokeJoinBevel.name"),
                                 null);
    popupButton.add(action);
    dsp.add(action);

    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_JOIN, BasicStroke.JOIN_ROUND);
    action = new AttributeAction(editor,
                                 attributes,
                                 BUNDLE.getString("buttonFactory.action_strokeJoinRound.name"),
                                 null);
    popupButton.add(action);
    dsp.add(action);

    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_JOIN, BasicStroke.JOIN_MITER);
    action = new AttributeAction(editor,
                                 attributes,
                                 BUNDLE.getString("buttonFactory.action_strokeJoinMiter.name"),
                                 null);
    popupButton.add(action);
    dsp.add(action);

    return popupButton;
  }

  private static void addFontButtonsTo(JToolBar bar, DrawingEditor editor) {
    bar.add(createFontButton(editor));
    bar.add(createFontStyleBoldButton(editor));
    bar.add(createFontStyleItalicButton(editor));
    bar.add(createFontStyleUnderlineButton(editor));
  }

  private static JPopupButton createFontButton(DrawingEditor editor) {
    JPopupButton fontPopupButton = new JPopupButton();
    fontPopupButton.setIcon(ImageDirectory.getImageIcon("/toolbar/attributeFont.png"));
    fontPopupButton.setText(null);
    fontPopupButton.setToolTipText(BUNDLE.getString("buttonFactory.button_font.tooltipText"));
    fontPopupButton.setFocusable(false);

    JComponentPopup popupMenu = new JComponentPopup();
    JFontChooser fontChooser = new JFontChooser();
    new LinkedList<Disposable>().add(
        new FontChooserHandler(editor, AttributeKeys.FONT_FACE, fontChooser, popupMenu));

    popupMenu.add(fontChooser);
    fontPopupButton.setPopupMenu(popupMenu);
    fontPopupButton.setFocusable(false);

    return fontPopupButton;
  }

  private static JButton createFontStyleBoldButton(DrawingEditor editor) {
    JButton button = new JButton();
    button.setIcon(ImageDirectory.getImageIcon("/toolbar/attributeFontBold.png"));
    button.setText(null);
    button.setToolTipText(BUNDLE.getString("buttonFactory.button_fontStyleBold.tooltipText"));
    button.setFocusable(false);

    AbstractAction action
        = new AttributeToggler<>(editor,
                                 AttributeKeys.FONT_BOLD,
                                 Boolean.TRUE,
                                 Boolean.FALSE,
                                 new StyledEditorKit.BoldAction());
    action.putValue(ActionUtil.UNDO_PRESENTATION_NAME_KEY,
                    BUNDLE.getString("buttonFactory.action_fontStyleBold.undo.presentationName"));
    button.addActionListener(action);

    return button;
  }

  private static JButton createFontStyleItalicButton(DrawingEditor editor) {
    JButton button = new JButton();
    button.setIcon(ImageDirectory.getImageIcon("/toolbar/attributeFontItalic.png"));
    button.setText(null);
    button.setToolTipText(BUNDLE.getString("buttonFactory.button_fontStyleItalic.tooltipText"));
    button.setFocusable(false);

    AbstractAction action
        = new AttributeToggler<>(editor,
                                 AttributeKeys.FONT_ITALIC,
                                 Boolean.TRUE,
                                 Boolean.FALSE,
                                 new StyledEditorKit.BoldAction());
    action.putValue(ActionUtil.UNDO_PRESENTATION_NAME_KEY,
                    BUNDLE.getString("buttonFactory.action_fontStyleItalic.undo.presentationName"));
    button.addActionListener(action);

    return button;
  }

  private static JButton createFontStyleUnderlineButton(DrawingEditor editor) {
    JButton button = new JButton();
    button.setIcon(ImageDirectory.getImageIcon("/toolbar/attributeFontUnderline.png"));
    button.setText(null);
    button.setToolTipText(BUNDLE.getString("buttonFactory.button_fontStyleUnderline.tooltipText"));
    button.setFocusable(false);

    AbstractAction action
        = new AttributeToggler<>(editor,
                                 AttributeKeys.FONT_UNDERLINE,
                                 Boolean.TRUE,
                                 Boolean.FALSE,
                                 new StyledEditorKit.BoldAction());
    action.putValue(ActionUtil.UNDO_PRESENTATION_NAME_KEY,
                    BUNDLE.getString("buttonFactory.action_fontStyleUnderline.undo.presentationName"));
    button.addActionListener(action);

    return button;
  }

  /**
   * Creates toolbar buttons and adds them to the specified JToolBar
   *
   * @param bar
   * @param editor
   */
  public static void addAlignmentButtonsTo(JToolBar bar, final DrawingEditor editor) {
    bar.add(new AlignAction.West(editor)).setFocusable(false);
    bar.add(new AlignAction.East(editor)).setFocusable(false);
    bar.add(new AlignAction.Horizontal(editor)).setFocusable(false);
    bar.add(new AlignAction.North(editor)).setFocusable(false);
    bar.add(new AlignAction.South(editor)).setFocusable(false);
    bar.add(new AlignAction.Vertical(editor)).setFocusable(false);

    bar.addSeparator();

    bar.add(new MoveAction.West(editor)).setFocusable(false);
    bar.add(new MoveAction.East(editor)).setFocusable(false);
    bar.add(new MoveAction.North(editor)).setFocusable(false);
    bar.add(new MoveAction.South(editor)).setFocusable(false);

    bar.addSeparator();

    bar.add(new BringToFrontAction(editor)).setFocusable(false);
    bar.add(new SendToBackAction(editor)).setFocusable(false);
  }
}
