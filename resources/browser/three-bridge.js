//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2023 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

/**
 * Bridge interface for the parts of the Three.js interface implemented in
 * JavaScript. This interface is called from the renderer via TeaVM.
 */
class ThreeBridge {

    init() {
        this.scene3D = new THREE.Scene();
        this.scene2D = new THREE.Scene();
        this.textureLoader = new THREE.TextureLoader();

        const width = window.innerWidth;
        const height = window.innerHeight;

        this.camera3D = new THREE.PerspectiveCamera(50, width / height, 0.1, 2000);
        this.camera3D.position.z = 5;

        this.camera2D = new THREE.OrthographicCamera(-width / 2, width / 2,
            height / 2, -height / 2, 1, 10);
        this.camera2D.position.z = 10;

        this.renderer = new THREE.WebGLRenderer();
        this.renderer.setPixelRatio(window.devicePixelRatio);
        this.renderer.setSize(width. height);
        this.renderer.autoClear = false;

        const canvasContainer = document.getElementById("multimediaLibContainer");
        canvasContainer.appendChild(this.renderer.domElement);
        window.addEventListener("resize", () => this.resize());
    }

    resize() {
        const width = window.innerWidth;
        const height = window.innerHeight;

        this.camera3D.aspect = width / height;
        this.camera3D.updateProjectionMatrix();

        this.camera2D.left = -width / 2;
        this.camera2D.right = width / 2;
        this.camera2D.top = height / 2;
        this.camera2D.bottom = -height / 2;
        this.camera2D.updateProjectionMatrix();

        this.renderer.setSize(width, height);
    }

    render() {
        this.renderer.clear();
        this.renderer.render(this.scene3D, this.camera3D);
        this.renderer.clearDepth();
        this.renderer.render(this.scene2D, this.camera2D);
    }

    loadTexture(path, callback) {
        this.textureLoader.load(path, texture => {
            const material = new THREE.SpriteMaterial({ map: texture });
            const sprite = new THREE.Sprite(material);
            sprite.position.set(100, 0, 1);
            sprite.scale.set(100, 100, 1);
            this.scene2D.add(sprite);
        });
    }
}
