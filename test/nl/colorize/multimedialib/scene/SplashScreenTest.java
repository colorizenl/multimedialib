//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.scene;

import nl.colorize.multimedialib.mock.MockScene;
import nl.colorize.multimedialib.renderer.MediaLoader;
import nl.colorize.multimedialib.renderer.Network;
import nl.colorize.multimedialib.renderer.headless.HeadlessRenderer;
import nl.colorize.multimedialib.stage.LoadStatus;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.Subject;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SplashScreenTest {

    private static final Subject<String> PROMISE = new Subject<>();

    @Test
    void showProgressBar() {
        LoadStatus first = LoadStatus.track(new ResourceFile("1.txt"), PROMISE);
        LoadStatus second = LoadStatus.track(new ResourceFile("2.txt"), PROMISE);
        MediaAssetStore assets = mockMediaAssetStore(first, second);

        SplashScreen splashScreen = new SplashScreen(assets, MockScene::new);
        splashScreen.setDelay(Timer.none());

        HeadlessRenderer renderer = new HeadlessRenderer(false);
        renderer.changeScene(splashScreen);
        renderer.update(1f);

        String expected = """
            Stage
                $$root [1]
                    Container [3]
                        Rect [(-400, -300, 800, 600)]
                        Rect [(-150, -5, 300, 10)]
                        Rect [(-150, -5, 150, 10)]
            """;

        assertEquals(expected, renderer.getStage().toString());
    }

    @Test
    void fillProgressBar() {
        LoadStatus first = LoadStatus.track(new ResourceFile("1.txt"), PROMISE);
        LoadStatus second = LoadStatus.track(new ResourceFile("2.txt"), PROMISE);
        MediaAssetStore assets = mockMediaAssetStore(first, second);

        SplashScreen splashScreen = new SplashScreen(assets, MockScene::new);
        splashScreen.setDelay(Timer.none());

        HeadlessRenderer renderer = new HeadlessRenderer(false);
        renderer.changeScene(splashScreen);
        renderer.update(1f);
        first.setLoaded(true);
        renderer.update(1f);

        String expected = """
            Stage
                $$root [1]
                    Container [3]
                        Rect [(-400, -300, 800, 600)]
                        Rect [(-150, -5, 300, 10)]
                        Rect [(-150, -5, 225, 10)]
            """;

        assertEquals(expected, renderer.getStage().toString());
    }

    @Test
    void transitionToNextScene() {
        LoadStatus first = LoadStatus.track(new ResourceFile("1.txt"), PROMISE);
        LoadStatus second = LoadStatus.track(new ResourceFile("2.txt"), PROMISE);
        MediaAssetStore assets = mockMediaAssetStore(first, second);

        SplashScreen splashScreen = new SplashScreen(assets, MockScene::new);
        splashScreen.setDelay(Timer.none());

        HeadlessRenderer renderer = new HeadlessRenderer(false);
        renderer.changeScene(splashScreen);
        renderer.update(1f);
        first.setLoaded(true);
        second.setLoaded(true);
        renderer.update(1f);
        renderer.update(1f);

        assertFalse(renderer.getSceneManager().isActiveScene(splashScreen));
    }

    @Test
    void applyTimeDelay() {
        LoadStatus first = LoadStatus.track(new ResourceFile("1.txt"), PROMISE);
        LoadStatus second = LoadStatus.track(new ResourceFile("2.txt"), PROMISE);
        MediaAssetStore assets = mockMediaAssetStore(first, second);

        SplashScreen splashScreen = new SplashScreen(assets, MockScene::new);
        splashScreen.setDelay(new Timer(3f));

        HeadlessRenderer renderer = new HeadlessRenderer(false);
        renderer.changeScene(splashScreen);
        renderer.update(1f);

        assertTrue(renderer.getSceneManager().isActiveScene(splashScreen));

        first.setLoaded(true);
        second.setLoaded(true);
        renderer.update(1f);
        assertTrue(renderer.getSceneManager().isActiveScene(splashScreen));

        renderer.update(1f);
        renderer.update(1f);
        assertFalse(renderer.getSceneManager().isActiveScene(splashScreen));
    }

    private MediaAssetStore mockMediaAssetStore(LoadStatus... assets) {
        AtomicBoolean loaded = new AtomicBoolean(false);

        return new MediaAssetStore() {
            @Override
            public void loadMedia(MediaLoader mediaLoader, Network network) {
                for (LoadStatus asset : assets) {
                    mediaLoader.getLoadStatus().add(asset);
                }
                loaded.set(true);
            }

            @Override
            public boolean isLoaded() {
                return loaded.get();
            }
        };
    }
}
