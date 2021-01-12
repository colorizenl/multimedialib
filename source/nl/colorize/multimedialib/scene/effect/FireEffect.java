//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2021 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene.effect;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.math.MathUtils;
import nl.colorize.multimedialib.math.RandomGenerator;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.GraphicsContext2D;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Graphical effect that shows a fire within a rectangular area, starting from
 * the bottom. The fire will use the provided color palette, or a default one
 * when none is specified, with the first color in the list being the least
 * intense color and the last color being the most intense.
 * <p>
 * The implementation of this effect was taken from the article
 * https://fabiensanglard.net/doom_fire_psx/index.html
 */
public class FireEffect extends Effect {

    private Rect area;
    private int pixelSize;
    private List<ColorRGB> palette;
    private float duration;

    private int[] firePixels;
    private float timer;
    private float totalTime;

    private static final List<String> DEFAULT_PALETTE = ImmutableList.of(
        "#070707", "#1F0707", "#2F0F07", "#470F07", "#571707", "#671F07", "#771F07", "#8F2707",
        "#9F2F07", "#AF3F07", "#BF4707", "#C74707", "#DF4F07", "#DF5707", "#DF5707", "#D75F07",
        "#D7670F", "#CF6F0F", "#CF770F", "#CF7F0F", "#CF8717", "#C78717", "#C78F17", "#C7971F",
        "#BF9F1F", "#BF9F1F", "#BFA727", "#BFA727", "#BFAF2F", "#B7AF2F", "#B7B72F", "#B7B737",
        "#CFCF6F", "#DFDF9F", "#EFEFC7", "#FFFFFF");

    private static final float FRAME_TIME = 1f / 30f;

    public FireEffect(float duration, Rect area, int pixelSize, List<ColorRGB> palette) {
        super(duration);

        Preconditions.checkArgument(area.getWidth() > 0 && area.getHeight() > 0, "Invalid area");
        Preconditions.checkArgument(pixelSize >= 1, "Invalid pixel size");
        Preconditions.checkArgument(palette.size() >= 10, "Palette too small");
        Preconditions.checkArgument(duration >= 1f, "Duration too short");

        this.area = area.copy();
        this.pixelSize = pixelSize;
        this.palette = palette;
        this.duration = duration;

        setup();
        modifyFrameUpdate(this::updateFire);
    }

    public FireEffect(float duration, int pixelSize, Rect area) {
        this(duration, area, pixelSize,
            DEFAULT_PALETTE.stream().map(ColorRGB::parseHex).collect(Collectors.toList()));
    }

    private void setup() {
        firePixels = new int[getWidth() * getHeight()];
        Arrays.fill(firePixels, 0);

        for (int x = 0; x < getWidth(); x++) {
            setFirePixel(x, getHeight() - 1, palette.size() - 1);
        }

        timer = 0f;
        totalTime = 0f;
    }

    private void updateFire(float deltaTime) {
        timer += deltaTime;
        totalTime += deltaTime;

        if (timer >= FRAME_TIME) {
            spreadFire();
            if (totalTime >= 0.7f * duration) {
                fadeFire();
            }
            timer = 0f;
        }
    }

    private void spreadFire() {
        for (int x = 0; x < getWidth(); x++) {
            for (int y = getHeight() - 2; y >= 0; y--) {
                int targetX = MathUtils.clamp(x + RandomGenerator.getInt(-1, 2), 0, getWidth() - 1);
                int paletteIndex = getFirePixel(targetX, y + 1) - RandomGenerator.getInt(0, 2);
                setFirePixel(x, y, paletteIndex);
            }
        }
    }

    private void fadeFire() {
        for (int x = 0; x < getWidth(); x++) {
            int fade = RandomGenerator.getInt(2, 6);
            setFirePixel(x, getHeight() - 1, getFirePixel(x, getHeight() - 1) - fade);
        }
    }

    @Override
    public void render(GraphicsContext2D graphics) {
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                int paletteIndex = getFirePixel(x, y);
                if (paletteIndex >= 0) {
                    ColorRGB color = palette.get(paletteIndex);
                    Rect pixel = new Rect(area.getX() + x * pixelSize, area.getY() + y * pixelSize,
                        pixelSize, pixelSize);
                    graphics.drawRect(pixel, color);
                }
            }
        }
    }

    private int getWidth() {
        return Math.round(area.getWidth() / pixelSize);
    }

    private int getHeight() {
        return Math.round(area.getHeight() / pixelSize);
    }

    private void setFirePixel(int x, int y, int paletteIndex) {
        firePixels[y * getWidth() + x] = paletteIndex;
    }

    private int getFirePixel(int x, int y) {
        return firePixels[y * getWidth() + x];
    }
}
