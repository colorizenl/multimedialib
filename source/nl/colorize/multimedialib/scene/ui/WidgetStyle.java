//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.ui;

import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.stage.Spatial2D;

import java.util.List;

/**
 * Defines and creates the graphics for widgets within a {@link Form}. There
 * are two ways to create styles: Either create a custom implementation by
 * implementing this interface, or use {@link DeclarativeStyle} to create a
 * style by defining its properties.
 */
public interface WidgetStyle {

    public Spatial2D createTitle(String title, Rect bounds);

    public Spatial2D createLabel(String label, Rect bounds);

    public Spatial2D createButton(String label, InputModel<Void> model, Rect bounds);

    public Spatial2D createCheckbox(String label, InputModel<Boolean> model, Rect bounds);

    public Spatial2D createInputField(InputModel<String> model, Rect bounds);

    public Spatial2D createSelectField(InputModel<String> model, List<String> choices, Rect bounds);
}
