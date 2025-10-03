//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.stage;

import lombok.Getter;
import lombok.Setter;
import nl.colorize.multimedialib.math.Angle;
import nl.colorize.util.stats.Aggregate;

/**
 * Extension of {@link Transform} that adds additional properties for
 * displaying images and sprites. The following properties are available:
 * <p>
 * <pre>
 * | Property        | Description                                                                |
 * |-----------------|----------------------------------------------------------------------------|
 * | Visible         | When set to false, the graphic will not be displayed at all.               |
 * | Position        | X/Y coordinates of where the graphic's center is displayed on the canvas.  |
 * | Alpha           | Percentage, where 0% is fully transparent and 100% is fully opaque.        |
 * | Rotation        | Angle, clockwise.                                                          |
 * | Scale X         | Percentage, where 100% indicates original size.                            |
 * | Scale Y         | Percentage, where 100% indicates original size.                            |
 * | Flip horizontal | When true, flips the image horizontally across its center.                 |
 * | Flip vertical   | When true, flips the image vertically across its center.                   |
 * | Mask color      | When set, displays all non-transparent pixels using the mask color.        |
 * </pre>
 */
@Getter
@Setter
public class ImageTransform extends Transform {

    private Angle rotation;
    private float scaleX;
    private float scaleY;
    private boolean flipHorizontal;
    private boolean flipVertical;
    private ColorRGB maskColor;

    public ImageTransform() {
        super();
        this.rotation = Angle.ORIGIN;
        this.scaleX = 100f;
        this.scaleY = 100f;
        this.flipHorizontal = false;
        this.flipVertical = false;
        this.maskColor = null;
    }

    public void setRotation(Angle rotation) {
        this.rotation = rotation;
    }

    public void setRotation(float degrees) {
        setRotation(new Angle(degrees));
    }

    public void addRotation(float degrees) {
        setRotation(rotation.move(degrees));
    }

    public void setScale(float scale) {
        setScaleX(scale);
        setScaleY(scale);
    }

    public void setScaleX(float scaleX) {
        this.scaleX = Math.abs(scaleX);
    }

    public float getScaleX() {
        return flipHorizontal ? -scaleX : scaleX;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = Math.abs(scaleY);
    }

    public float getScaleY() {
        return flipVertical ? -scaleY : scaleY;
    }

    @Override
    public void set(Transform other) {
        super.set(other);

        if (other instanceof ImageTransform otherIT) {
            setRotation(otherIT.rotation);
            setScaleX(otherIT.scaleX);
            setScaleY(otherIT.scaleY);
            setFlipHorizontal(otherIT.flipHorizontal);
            setFlipVertical(otherIT.flipVertical);
            setMaskColor(otherIT.maskColor);
        }
    }

    /**
     * Returns a new {@link ImageTransform} instance that is the result of
     * combining this transform with the specified other transform.
     */
    @Override
    public ImageTransform combine(Transform other) {
        ImageTransform combined = new ImageTransform();
        combined.setVisible(isVisible() && other.isVisible());
        combined.setPosition(getPosition().add(other.getPosition()));
        combined.setAlpha(Aggregate.multiplyPercentage(getAlpha(), other.getAlpha()));

        if (other instanceof ImageTransform otherIT) {
            combined.setRotation(rotation.degrees() + otherIT.rotation.degrees());
            combined.setScaleX(Aggregate.multiplyPercentage(scaleX, otherIT.scaleX));
            combined.setScaleY(Aggregate.multiplyPercentage(scaleY, otherIT.scaleY));
            combined.setFlipHorizontal(flipHorizontal || otherIT.flipHorizontal);
            combined.setFlipVertical(flipVertical || otherIT.flipVertical);
            combined.setMaskColor(otherIT.maskColor != null ? otherIT.maskColor : maskColor);
        }

        return combined;
    }
}
