//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import lombok.Getter;
import lombok.Setter;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.multimedialib.stage.ColorRGB;
import nl.colorize.multimedialib.stage.Container;
import nl.colorize.multimedialib.stage.LoadStatus;
import nl.colorize.multimedialib.stage.Primitive;
import nl.colorize.util.Platform;
import nl.colorize.util.stats.Aggregate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Displays a loading screen with a progress bar while the renderer is loading
 * media files asynchronously. The loading screen is configurable, but is
 * limited to shapes and colors. Images and fonts cannot be used, because they
 * require media files themselves. Once all media assets have been loaded,
 * the splash screen will automatically transition to the next scene.
 */
@Getter
@Setter
public class SplashScreen implements Scene {

    private final MediaAssetStore mediaAssets;
    private final Supplier<Scene> nextScene;
    private final List<LoadStatus> loading;

    private Timer delay;
    private int barWidth;
    private int barHeight;
    private ColorRGB screenBackgroundColor;
    private ColorRGB barBorderColor;
    private int barBorderStroke;
    private ColorRGB barBackgroundColor;
    private int barBackgroundAlpha;
    private ColorRGB barForegroundColor;

    public SplashScreen(MediaAssetStore mediaAssets, Supplier<Scene> nextScene) {
        this.mediaAssets = mediaAssets;
        this.nextScene = nextScene;
        this.loading = new ArrayList<>();

        this.delay = new Timer(Platform.isDesktopPlatform() ? 0f : 0.4f);
        this.barWidth = 300;
        this.barHeight = 10;
        this.screenBackgroundColor = ColorRGB.parseHex("#EBEBEB");;
        this.barBorderColor = null;
        this.barBorderStroke = 1;
        this.barBackgroundColor = ColorRGB.parseHex("#adadad");
        this.barBackgroundAlpha = 100;
        this.barForegroundColor = ColorRGB.parseHex("#e45d61");
    }

    @Override
    public void start(SceneContext context) {
        attachProgressBar(context);
        loadMediaAssets(context.getMediaLoader(), context.getNetwork());
    }

    private void loadMediaAssets(MediaLoader mediaLoader, Network network) {
        if (!mediaAssets.isLoaded()) {
            mediaAssets.loadMedia(mediaLoader, network);
        }

        loading.addAll(mediaLoader.getLoadStatus().flush().toList());
    }

    protected void attachProgressBar(SceneContext context) {
        int canvasWidth = context.getStage().getCanvas().getWidth();
        int canvasHeight = context.getStage().getCanvas().getHeight();

        Container progressBar = new Container();
        progressBar.getTransform().setPosition(context.getStage().getCanvas().getCenter());
        context.getStage().getRoot().addChild(progressBar);

        if (screenBackgroundColor != null) {
            Rect screen = Rect.aroundOrigin(canvasWidth, canvasHeight);
            progressBar.addChild(new Primitive(screen, screenBackgroundColor));
        }

        if (barBorderStroke >= 1 && barBorderColor != null) {
            Rect border = Rect.aroundOrigin(canvasWidth + barBorderStroke * 2,
                canvasHeight + barBorderStroke * 2);
            progressBar.addChild(new Primitive(border, barBorderColor));
        }

        Rect barBackground = Rect.aroundOrigin(barWidth, barHeight);
        progressBar.addChild(new Primitive(barBackground, barBackgroundColor, barBackgroundAlpha));

        Primitive foreground = new Primitive(Rect.aroundOrigin(1, barHeight), barForegroundColor);
        progressBar.addChild(foreground);

        context.attach(deltaTime -> animateProgressBar(foreground));
    }

    private void animateProgressBar(Primitive foreground) {
        int completed = (int) loading.stream()
            .filter(LoadStatus::isLoaded)
            .count();

        float loadDelta = Aggregate.fraction(completed, loading.size());
        float delayDelta = delay.isCompleted() ? 1f : delay.getRatio();
        float delta = 0.5f * loadDelta + 0.5f * delayDelta;
        foreground.setShape(new Rect(-barWidth / 2f, -barHeight / 2f, delta * barWidth, barHeight));
    }

    @Override
    public void update(SceneContext context, float deltaTime) {
        delay.update(deltaTime);

        if (isLoadingCompleted()) {
            context.changeScene(nextScene.get());
        }
    }

    private boolean isLoadingCompleted() {
        return delay.isCompleted() && loading.stream().allMatch(LoadStatus::isLoaded);
    }
}
