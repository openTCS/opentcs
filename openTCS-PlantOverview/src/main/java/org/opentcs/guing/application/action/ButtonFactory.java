/**
 * (c): IML, JHotDraw.
 *
 */
package org.opentcs.guing.application.action;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.color.ColorSpace;
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
import org.jhotdraw.color.HSBColorSpace;
import org.jhotdraw.draw.AttributeKey;
import org.jhotdraw.draw.AttributeKeys;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.action.AbstractSelectedAction;
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
  /**
   * Websave color palette as used by Macromedia Fireworks. This palette has 19
   * columns. The leftmost column contains a redundant set of color icons to
   * make selection of gray scales and of the primary colors easier.
   */
  public final static java.util.List<ColorIcon> WEBSAVE_COLORS;

  static {
    LinkedList<ColorIcon> m = new LinkedList<>();

    for (int b = 0; b <= 0xff; b += 0x33) {
      int rgb = (b << 16) | (b << 8) | b;
      m.add(new ColorIcon(rgb));

      for (int r = 0; r <= 0x66; r += 0x33) {
        for (int g = 0; g <= 0xff; g += 0x33) {
          rgb = (r << 16) | (g << 8) | b;
          m.add(new ColorIcon(rgb));
        }
      }
    }

    int[] firstColumn = {
      0xff0000,
      0x00ff00,
      0x0000ff,
      0xff00ff,
      0x00ffff,
      0xffff00,};

    for (int b = 0x0, i = 0; b <= 0xff; b += 0x33, i++) {
      int rgb = (b << 16) | (b << 8) | b;
      m.add(new ColorIcon(firstColumn[i]));

      for (int r = 0x99; r <= 0xff; r += 0x33) {
        for (int g = 0; g <= 0xff; g += 0x33) {
          rgb = 0xff000000 | (r << 16) | (g << 8) | b;
          m.add(new ColorIcon(rgb, "#" + Integer.toHexString(rgb).substring(2)));
        }
      }
    }

    WEBSAVE_COLORS = Collections.unmodifiableList(m);
  }
  public final static int WEBSAVE_COLORS_COLUMN_COUNT = 19;
  /**
   * HSB color palette with a set of colors chosen based on a physical criteria.
   * <p>
   * This is a 'human friendly' color palette which arranges the color in a
   * way that makes it easy for humans to select the desired color. The colors
   * are ordered in a way which minimizes the color contrast effect in the human
   * visual system.
   * <p>
   * This palette has 12 columns and 10 rows.
   * <p>
   * The topmost
   * row contains a null-color and a gray scale from white to black in 10
   * percent steps.
   * <p>
   * The remaining rows contain colors taken from the outer
   * hull of the HSB color model:
   * <p>
   * The columns are ordered by hue starting
   * with red - the lowest wavelength - and ending with purple - the highest
   * wavelength. There are 12 different hues, so that all primary colors with
   * their additive complements can be selected.
   * <p>
   * The rows are orderd by
   * brightness with the brightest color at the top (sky) and the darkest color
   * at the bottom (earth). The first 5 rows contain colors with maximal
   * brightness and a saturation ranging form 20% up to 100%. The remaining 4
   * rows contain colors with maximal saturation and a brightness ranging from
   * 90% to 20% (this also makes for a range from 100% to 20% if the 5th row is
   * taken into account).
   */
  public final static java.util.List<ColorIcon> HSB_COLORS;
  public final static int HSB_COLORS_COLUMN_COUNT = 12;
  /**
   * This is the same palette as HSB_COLORS, but all color values are specified
   * in the sRGB color space.
   */
  public final static java.util.List<ColorIcon> HSB_COLORS_AS_RGB;
  public final static int HSB_COLORS_AS_RGB_COLUMN_COUNT = 12;

  static {
    ColorSpace grayCS = ColorSpace.getInstance(ColorSpace.CS_GRAY);
    HSBColorSpace hsbCS = HSBColorSpace.getInstance();
    LinkedList<ColorIcon> m = new LinkedList<>();
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    String text = bundle.getToolTipTextProperty("attribute.color.noColor");
    m.add(new ColorIcon(new Color(0, true), text));

    for (int b = 10; b >= 0; b--) {
      Color c = new Color(grayCS, new float[] {b / 10f}, 1f);
      text = bundle.getFormatted("attribute.color.grayComponents.toolTipText",
                                 b * 10);
      m.add(new ColorIcon(c, text));
    }

    for (int s = 2; s <= 8; s += 2) {
      for (int h = 0; h < 12; h++) {
        Color c = new Color(hsbCS, new float[] {(h) / 12f, s * 0.1f, 1f}, 1f);
        text = bundle.getFormatted("attribute.color.hsbComponents.toolTipText",
                                   h * 360 / 12, s * 10, 100);
        m.add(new ColorIcon(c, text));
      }
    }

    for (int b = 10; b >= 2; b -= 2) {
      for (int h = 0; h < 12; h++) {
        Color c = new Color(hsbCS, new float[] {(h) / 12f, 1f, b * 0.1f}, 1f);
        text = bundle.getFormatted("attribute.color.hsbComponents.toolTipText",
                                   h * 360 / 12, 100, b * 10);
        m.add(new ColorIcon(c, text));
      }
    }

    HSB_COLORS = Collections.unmodifiableList(m);

    m = new LinkedList<>();

    for (ColorIcon ci : HSB_COLORS) {
      if (ci.getColor() == null) {
        text = bundle.getToolTipTextProperty("attribute.color.noColor");
        m.add(new ColorIcon(new Color(0, true), text));
      }
      else {
        Color c = ci.getColor();
        c = c.getColorSpace() == grayCS //
            ? new Color(c.getGreen(), c.getGreen(), c.getGreen(), c.getAlpha())//workaround for rounding error
            : new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
        text = bundle.getFormatted("attribute.color.rgbComponents.toolTipText",
                                   c.getRed(), c.getGreen(), c.getBlue());
        m.add(new ColorIcon(c, text));
      }
    }

    HSB_COLORS_AS_RGB = Collections.unmodifiableList(m);
  }

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
                                    "attribute.strokeColor",
                                    new HashMap<AttributeKey, Object>()));
    bar.add(createEditorColorButton(editor,
                                    AttributeKeys.FILL_COLOR,
                                    "attribute.fillColor",
                                    new HashMap<AttributeKey, Object>()));
    bar.add(createEditorColorButton(editor,
                                    AttributeKeys.TEXT_COLOR,
                                    "attribute.textColor",
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
   * @param labelKey The resource bundle key used for retrieving the icon and
   * the tooltip of the button.
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
      String labelKey,
      Map<AttributeKey, Object> defaultAttributes) {

    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    final JPopupButton popupButton = new JPopupButton();
    popupButton.setPopupAlpha(1f);

    if (defaultAttributes == null) {
      defaultAttributes = new HashMap<>();
    }

    popupButton.setAction(
        new DefaultAttributeAction(editor, attributeKey, defaultAttributes),
        new Rectangle(0, 0, 22, 22));
    popupButton.setColumnCount(DEFAULT_COLORS_COLUMN_COUNT, false);
    boolean hasNullColor = false;

    for (ColorIcon swatch : DEFAULT_COLOR_ICONS) {
      AttributeAction action;
      HashMap<AttributeKey, Object> attributes = new HashMap<>(defaultAttributes);
      Color swatchColor = swatch.getColor();
      attributes.put(attributeKey, swatchColor);

      if (swatchColor == null || swatchColor.getAlpha() == 0) {
        hasNullColor = true;
      }

      popupButton.add(action
          = new AttributeAction(editor,
                                attributes,
                                labels.getToolTipTextProperty(labelKey),
                                swatch));
      action.putValue(Action.SHORT_DESCRIPTION, swatch.getName());
      action.setUpdateEnabledState(false);
    }

    // No color
    if (!hasNullColor) {
      AttributeAction action;
      HashMap<AttributeKey, Object> attributes = new HashMap<>(defaultAttributes);
      attributes.put(attributeKey, null);
      popupButton.add(action
          = new AttributeAction(editor,
                                attributes,
                                labels.getToolTipTextProperty("attribute.color.noColor"),
                                new ColorIcon(null,
                                              labels.getToolTipTextProperty("attribute.color.noColor"),
                                              DEFAULT_COLOR_ICONS.get(0).getIconWidth(),
                                              DEFAULT_COLOR_ICONS.get(0).getIconHeight())));
      action.putValue(Action.SHORT_DESCRIPTION,
                      labels.getToolTipTextProperty("attribute.color.noColor"));
      action.setUpdateEnabledState(false);
    }

    // Color chooser
    ImageIcon chooserIcon = new ImageIcon(
        Images.createImage(ButtonFactory.class,
                           "/org/jhotdraw/draw/action/images/attribute.color.colorChooser.png"));
    Action action;
    popupButton.add(action
        = new EditorColorChooserAction(editor,
                                       attributeKey,
                                       "color",
                                       chooserIcon,
                                       defaultAttributes));
    labels.configureToolBarButton(popupButton, labelKey);
    action.putValue(Action.SHORT_DESCRIPTION,
                    labels.getToolTipTextProperty("attribute.color.colorChooser"));
    Icon icon = new EditorColorIcon(editor,
                                    attributeKey,
                                    labels.getIconProperty(labelKey, ButtonFactory.class).getImage(),
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
    ResourceBundleUtil.getBundle().configureToolBarButton(
        strokeDecorationPopupButton, "attribute.strokeDecoration");
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
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    JPopupButton strokeWidthPopupButton = new JPopupButton();
    bundle.configureToolBarButton(strokeWidthPopupButton, "attribute.strokeWidth");
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
                      bundle.getString("attribute.strokeWidth.text"));
      AbstractButton btn = strokeWidthPopupButton.add(action);
      btn.setDisabledIcon(icon);
    }

    return strokeWidthPopupButton;
  }

  private static JPopupButton createStrokeDashesButton(DrawingEditor editor) {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    JPopupButton strokeDashesPopupButton = new JPopupButton();
    bundle.configureToolBarButton(strokeDashesPopupButton, "attribute.strokeDashes");
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
      AttributeAction action;
      AbstractButton button = strokeDashesPopupButton.add(action
          = new AttributeAction(editor, AttributeKeys.STROKE_DASHES, dash, null, icon));
      button.setDisabledIcon(icon);
    }

    return strokeDashesPopupButton;
  }

  private static JPopupButton createStrokeTypeButton(DrawingEditor editor) {
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    JPopupButton strokeTypePopupButton = new JPopupButton();
    labels.configureToolBarButton(strokeTypePopupButton, "attribute.strokeType");
    strokeTypePopupButton.setFocusable(false);

    strokeTypePopupButton.add(
        new AttributeAction(editor,
                            AttributeKeys.STROKE_TYPE,
                            AttributeKeys.StrokeType.BASIC,
                            labels.getString("attribute.strokeType.basic"),
                            new StrokeIcon(new BasicStroke(1,
                                                           BasicStroke.CAP_BUTT,
                                                           BasicStroke.JOIN_BEVEL))));
    HashMap<AttributeKey, Object> attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_TYPE, AttributeKeys.StrokeType.DOUBLE);
    attributes.put(AttributeKeys.STROKE_INNER_WIDTH_FACTOR, 2d);
    strokeTypePopupButton.add(
        new AttributeAction(editor,
                            attributes,
                            labels.getString("attribute.strokeType.double"),
                            new StrokeIcon(new DoubleStroke(2, 1))));
    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_TYPE, AttributeKeys.StrokeType.DOUBLE);
    attributes.put(AttributeKeys.STROKE_INNER_WIDTH_FACTOR, 3d);
    strokeTypePopupButton.add(
        new AttributeAction(editor,
                            attributes,
                            labels.getString("attribute.strokeType.double"),
                            new StrokeIcon(new DoubleStroke(3, 1))));
    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_TYPE, AttributeKeys.StrokeType.DOUBLE);
    attributes.put(AttributeKeys.STROKE_INNER_WIDTH_FACTOR, 4d);
    strokeTypePopupButton.add(
        new AttributeAction(editor,
                            attributes,
                            labels.getString("attribute.strokeType.double"),
                            new StrokeIcon(new DoubleStroke(4, 1))));

    return strokeTypePopupButton;
  }

  private static JPopupButton createStrokePlacementButton(DrawingEditor editor) {
    ResourceBundleUtil labels = ResourceBundleUtil.getBundle();
    JPopupButton strokePlacementPopupButton = new JPopupButton();
    labels.configureToolBarButton(strokePlacementPopupButton, "attribute.strokePlacement");
    strokePlacementPopupButton.setFocusable(false);

    HashMap<AttributeKey, Object> attributes;
    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_PLACEMENT, AttributeKeys.StrokePlacement.CENTER);
    attributes.put(AttributeKeys.FILL_UNDER_STROKE, AttributeKeys.Underfill.CENTER);
    strokePlacementPopupButton.add(
        new AttributeAction(editor,
                            attributes,
                            labels.getString("attribute.strokePlacement.center"),
                            null));

    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_PLACEMENT, AttributeKeys.StrokePlacement.INSIDE);
    attributes.put(AttributeKeys.FILL_UNDER_STROKE, AttributeKeys.Underfill.CENTER);
    strokePlacementPopupButton.add(
        new AttributeAction(editor,
                            attributes,
                            labels.getString("attribute.strokePlacement.inside"),
                            null));

    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_PLACEMENT, AttributeKeys.StrokePlacement.OUTSIDE);
    attributes.put(AttributeKeys.FILL_UNDER_STROKE, AttributeKeys.Underfill.CENTER);
    strokePlacementPopupButton.add(
        new AttributeAction(editor,
                            attributes,
                            labels.getString("attribute.strokePlacement.outside"),
                            null));

    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_PLACEMENT, AttributeKeys.StrokePlacement.CENTER);
    attributes.put(AttributeKeys.FILL_UNDER_STROKE, AttributeKeys.Underfill.FULL);
    strokePlacementPopupButton.add(
        new AttributeAction(editor,
                            attributes,
                            labels.getString("attribute.strokePlacement.centerFilled"),
                            null));

    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_PLACEMENT, AttributeKeys.StrokePlacement.INSIDE);
    attributes.put(AttributeKeys.FILL_UNDER_STROKE, AttributeKeys.Underfill.FULL);
    strokePlacementPopupButton.add(
        new AttributeAction(editor,
                            attributes,
                            labels.getString("attribute.strokePlacement.insideFilled"),
                            null));

    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_PLACEMENT, AttributeKeys.StrokePlacement.OUTSIDE);
    attributes.put(AttributeKeys.FILL_UNDER_STROKE, AttributeKeys.Underfill.FULL);
    strokePlacementPopupButton.add(
        new AttributeAction(editor,
                            attributes,
                            labels.getString("attribute.strokePlacement.outsideFilled"),
                            null));

    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_PLACEMENT, AttributeKeys.StrokePlacement.CENTER);
    attributes.put(AttributeKeys.FILL_UNDER_STROKE, AttributeKeys.Underfill.NONE);
    strokePlacementPopupButton.add(
        new AttributeAction(editor,
                            attributes,
                            labels.getString("attribute.strokePlacement.centerUnfilled"),
                            null));

    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_PLACEMENT, AttributeKeys.StrokePlacement.INSIDE);
    attributes.put(AttributeKeys.FILL_UNDER_STROKE, AttributeKeys.Underfill.NONE);
    strokePlacementPopupButton.add(
        new AttributeAction(editor,
                            attributes,
                            labels.getString("attribute.strokePlacement.insideUnfilled"),
                            null));

    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_PLACEMENT, AttributeKeys.StrokePlacement.OUTSIDE);
    attributes.put(AttributeKeys.FILL_UNDER_STROKE, AttributeKeys.Underfill.NONE);
    strokePlacementPopupButton.add(
        new AttributeAction(editor,
                            attributes,
                            labels.getString("attribute.strokePlacement.outsideUnfilled"),
                            null));

    return strokePlacementPopupButton;
  }

  private static JPopupButton createStrokeCapButton(DrawingEditor editor) {

    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    JPopupButton popupButton = new JPopupButton();
    bundle.configureToolBarButton(popupButton, "attribute.strokeCap");
    popupButton.setFocusable(false);

    HashMap<AttributeKey, Object> attributes;
    java.util.List<Disposable> dsp = new LinkedList<>();

    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_CAP, BasicStroke.CAP_BUTT);
    AttributeAction a;
    popupButton.add(a = new AttributeAction(editor, attributes, bundle.getString("attribute.strokeCap.butt"), null));
    dsp.add(a);

    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_CAP, BasicStroke.CAP_ROUND);
    popupButton.add(a = new AttributeAction(editor, attributes, bundle.getString("attribute.strokeCap.round"), null));
    dsp.add(a);

    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_CAP, BasicStroke.CAP_SQUARE);
    popupButton.add(a = new AttributeAction(editor, attributes, bundle.getString("attribute.strokeCap.square"), null));
    dsp.add(a);

    return popupButton;
  }

  private static JPopupButton createStrokeJoinButton(DrawingEditor editor) {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    JPopupButton popupButton = new JPopupButton();
    bundle.configureToolBarButton(popupButton, "attribute.strokeJoin");
    popupButton.setFocusable(false);

    HashMap<AttributeKey, Object> attributes;
    java.util.List<Disposable> dsp = new LinkedList<>();

    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_JOIN, BasicStroke.JOIN_BEVEL);
    AttributeAction a;
    popupButton.add(a = new AttributeAction(editor, attributes, bundle.getString("attribute.strokeJoin.bevel"), null));
    dsp.add(a);

    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_JOIN, BasicStroke.JOIN_ROUND);
    popupButton.add(a = new AttributeAction(editor, attributes, bundle.getString("attribute.strokeJoin.round"), null));
    dsp.add(a);

    attributes = new HashMap<>();
    attributes.put(AttributeKeys.STROKE_JOIN, BasicStroke.JOIN_MITER);
    popupButton.add(a = new AttributeAction(editor, attributes, bundle.getString("attribute.strokeJoin.miter"), null));
    dsp.add(a);

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
    ResourceBundleUtil.getBundle().configureToolBarButton(fontPopupButton, "attribute.font");
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
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    JButton button = new JButton();
    bundle.configureToolBarButton(button, "attribute.fontStyle.bold");
    button.setFocusable(false);

    AbstractAction action
        = new AttributeToggler<>(editor,
                                 AttributeKeys.FONT_BOLD,
                                 Boolean.TRUE,
                                 Boolean.FALSE,
                                 new StyledEditorKit.BoldAction());
    action.putValue(ActionUtil.UNDO_PRESENTATION_NAME_KEY,
                    bundle.getString("attribute.fontStyle.bold.text"));
    button.addActionListener(action);

    return button;
  }

  private static JButton createFontStyleItalicButton(DrawingEditor editor) {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    JButton button = new JButton();
    bundle.configureToolBarButton(button, "attribute.fontStyle.italic");
    button.setFocusable(false);

    AbstractAction action
        = new AttributeToggler<>(editor,
                                 AttributeKeys.FONT_ITALIC,
                                 Boolean.TRUE,
                                 Boolean.FALSE,
                                 new StyledEditorKit.BoldAction());
    action.putValue(ActionUtil.UNDO_PRESENTATION_NAME_KEY,
                    bundle.getString("attribute.fontStyle.italic.text"));
    button.addActionListener(action);

    return button;
  }

  private static JButton createFontStyleUnderlineButton(DrawingEditor editor) {
    ResourceBundleUtil bundle = ResourceBundleUtil.getBundle();
    JButton button = new JButton();
    bundle.configureToolBarButton(button, "attribute.fontStyle.underline");
    button.setFocusable(false);

    AbstractAction action
        = new AttributeToggler<>(editor,
                                 AttributeKeys.FONT_UNDERLINE,
                                 Boolean.TRUE,
                                 Boolean.FALSE,
                                 new StyledEditorKit.BoldAction());
    action.putValue(ActionUtil.UNDO_PRESENTATION_NAME_KEY,
                    bundle.getString("attribute.fontStyle.underline.text"));
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
    java.util.List<Disposable> dsp = new LinkedList<>();
    AbstractSelectedAction action;

    bar.add(action = new AlignAction.West(editor)).setFocusable(false);
    dsp.add(action);
    bar.add(action = new AlignAction.East(editor)).setFocusable(false);
    dsp.add(action);
    bar.add(action = new AlignAction.Horizontal(editor)).setFocusable(false);
    dsp.add(action);
    bar.add(action = new AlignAction.North(editor)).setFocusable(false);
    dsp.add(action);
    bar.add(action = new AlignAction.South(editor)).setFocusable(false);
    dsp.add(action);
    bar.add(action = new AlignAction.Vertical(editor)).setFocusable(false);
    dsp.add(action);

    bar.addSeparator();

    bar.add(action = new MoveAction.West(editor)).setFocusable(false);
    dsp.add(action);
    bar.add(action = new MoveAction.East(editor)).setFocusable(false);
    dsp.add(action);
    bar.add(action = new MoveAction.North(editor)).setFocusable(false);
    dsp.add(action);
    bar.add(action = new MoveAction.South(editor)).setFocusable(false);
    dsp.add(action);

    bar.addSeparator();

    bar.add(new BringToFrontAction(editor)).setFocusable(false);
    dsp.add(action);
    bar.add(new SendToBackAction(editor)).setFocusable(false);
    dsp.add(action);
  }
}
