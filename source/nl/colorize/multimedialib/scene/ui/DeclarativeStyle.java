//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.ui;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.With;
import nl.colorize.multimedialib.math.Point2D;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.SegmentedLine;
import nl.colorize.multimedialib.stage.Align;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Container;
import nl.colorize.multimedialib.stage.FontFace;
import nl.colorize.multimedialib.stage.Image;
import nl.colorize.multimedialib.stage.ImageTransform;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.multimedialib.stage.Spatial2D;
import nl.colorize.multimedialib.stage.Sprite;
import nl.colorize.multimedialib.stage.Text;

import java.util.List;

/**
 * Implementation of {@link WidgetStyle} that is created by defining properties
 * such as fonts and colors. This is suitable for simple applications that do
 * not require lots of styling. For larger and more graphically impressive
 * applications, it is more suitable to create a custom {@link WidgetStyle}
 * implementation.
 */
@AllArgsConstructor
@With
public class DeclarativeStyle implements WidgetStyle {

    private FontFace font;
    private Align align;
    private ColorRGB backgroundColor;
    private Image backgroundImage;
    private double backgroundImageScale;
    private ColorRGB borderColor;
    private int borderSize;
    private int padding;

    public DeclarativeStyle(FontFace font) {
        Preconditions.checkArgument(font != null, "No font defined");
        this(font, Align.LEFT, null, null, 100.0, null, 1, 5);
    }

    public ColorRGB getFontColor() {
        return font.color();
    }

    public DeclarativeStyle withFontColor(ColorRGB fontColor) {
        return withFont(font.derive(fontColor));
    }

    public DeclarativeStyle withFontSize(int fontSize) {
        return withFont(font.derive(fontSize));
    }

    public DeclarativeStyle withBackgroundImageScaleToFit() {
        return withBackgroundImageScale(-1);
    }

    public DeclarativeStyle withBorder(ColorRGB borderColor, int borderSize) {
        return withBorderColor(borderColor).withBorderSize(borderSize);
    }

    private Spatial2D createBackground(Rect bounds) {
        Container background = new Container();
        if (backgroundColor != null) {
            background.addChild(new Primitive(bounds, backgroundColor));
        }
        if (backgroundImage != null) {
            Sprite backgroundSprite = new Sprite(backgroundImage);
            backgroundSprite.getTransform().setPosition(bounds.getCenter());
            scaleBackgroundImage(backgroundSprite, bounds);
            background.addChild(backgroundSprite);
        }
        if (borderSize > 0 && borderColor != null) {
            Primitive border = new Primitive(SegmentedLine.fromOutline(bounds), borderColor);
            border.setStroke(borderSize);
            background.addChild(border);
        }
        return background;
    }

    private void scaleBackgroundImage(Sprite backgroundSprite, Rect bounds) {
        ImageTransform transform = backgroundSprite.getTransform();
        if (backgroundImageScale >= 0) {
            transform.setScale(backgroundImageScale);
        } else {
            transform.setScaleX((bounds.width() / backgroundImage.getWidth()) * 100.0);
            transform.setScaleY((bounds.height() / backgroundImage.getHeight()) * 100.0);
        }
    }

    @Override
    public Spatial2D createTitle(String title, Rect bounds) {
        return createLabel(title, bounds);
    }

    @Override
    public Text createLabel(String label, Rect bounds) {
        Rect textBounds = bounds.expand(-2 * padding);
        Text labelText = new Text(label, font, align, (int) textBounds.width());
        labelText.getTransform().setX(textBounds.x());
        labelText.getTransform().setY(textBounds.y() + textBounds.height() * 0.8);
        return labelText;
    }

    @Override
    public Spatial2D createButton(String label, InputModel<Void> value, Rect bounds) {
        Container button = new Container();
        button.addChild(createBackground(bounds));
        button.addChild(createLabel(label, bounds));
        return button;
    }

    @Override
    public Spatial2D createCheckbox(String label, InputModel<Boolean> model, Rect bounds) {
        Point2D center = getCheckboxCenter(bounds);
        Rect outerSize = Rect.around(center, bounds.height(), bounds.height());
        Rect innerSize = Rect.around(center, bounds.height() * 0.7, bounds.height() * 0.7);

        Primitive inner = new Primitive(innerSize, getFontColor());
        inner.getTransform().setVisible(model.getValue().get());
        model.getValue().getChanges().subscribe(inner.getTransform()::setVisible);

        Rect labelBounds = new Rect(bounds.x() + outerSize.width() * 1.2, bounds.y(),
            bounds.width() - outerSize.width() * 1.2, bounds.height());

        Container checkbox = new Container();
        checkbox.addChild(createBackground(outerSize));
        checkbox.addChild(inner);
        checkbox.addChild(createLabel(label, labelBounds));
        return checkbox;
    }

    private Point2D getCheckboxCenter(Rect bounds) {
        return switch (align) {
            case LEFT -> new Point2D(bounds.x() + bounds.height() / 2.0, bounds.getCenterY());
            case CENTER -> bounds.getCenter();
            case RIGHT -> new Point2D(bounds.getEndX() + bounds.height() / 2.0, bounds.getCenterY());
        };
    }

    @Override
    public Spatial2D createInputField(InputModel<String> model, Rect bounds) {
        Text text = createLabel(model.getValue().get(), bounds);
        model.getValue().getChanges().subscribe(text::setText);

        Container input = new Container();
        input.addChild(createBackground(bounds));
        input.addChild(text);
        return input;
    }

    @Override
    public Spatial2D createSelectField(InputModel<String> model, List<String> choices, Rect bounds) {
        Text text = createLabel(model.getValue().get(), bounds);
        model.getValue().getChanges().subscribe(text::setText);

        Container select = new Container();
        select.addChild(createBackground(bounds));
        select.addChild(text);
        return select;
    }
}
