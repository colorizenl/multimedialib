//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

const FIELD_OF_VIEW = 75;
const NEAR_PLANE = 1;
const FAR_PLANE = 300;
const AMBIENT_LIGHT_COLOR = 0x646464;
const LIGHT_COLOR = 0xc8c8c8;

/**
 * Renderer that uses three.js to render 3D graphics. This requires WebGL
 * support, and this renderer is therefore only supported on browsers that
 * support WebGL.
 */
class ThreejsRenderer {

    constructor(container) {
        this.meshData = {};
        this.models = {};
        this.requestedModels = {};
        this.textures = {};
        this.animations = {};

        this.initRenderer(container);
    }
    
    initRenderer(container) {
        this.scene = new THREE.Scene();
        this.camera = new THREE.PerspectiveCamera(FIELD_OF_VIEW,
            window.innerWidth / window.innerHeight, NEAR_PLANE, FAR_PLANE);

        this.ambientLight = new THREE.AmbientLight(AMBIENT_LIGHT_COLOR);
        this.scene.add(this.ambientLight);

        this.light = new THREE.DirectionalLight(LIGHT_COLOR, 0.5);
        this.scene.add(this.light);

        this.renderer = new THREE.WebGLRenderer();
        this.renderer.setSize(window.innerWidth, window.innerHeight);
        container.appendChild(this.renderer.domElement);
    }

    render(deltaTime) {
        // Initialize the renderer for 2D graphics. This can only be done
        // after the main 3D renderer is already running.
        if (this.delegate2D == null) {
            let context2D = overlayCanvas.getContext("2d");
            this.delegate2D = new Html5CanvasRenderer(context2D);
        }

        this.updateModels(deltaTime);
        this.renderer.render(this.scene, this.camera);
        this.delegate2D.render();
    }

    updateModels(deltaTime) {
        for (let modelId in this.requestedModels) {
            let meshId = this.requestedModels[modelId];
            let mesh = this.meshData[meshId];

            if (mesh != null) {
                let model = new THREE.Mesh(mesh.geometry, mesh.material);
                this.scene.add(model);
                this.models[modelId] = model;
                delete this.requestedModels[modelId];
            }
        }

        for (let modelId in this.animations) {
            this.animations[modelId].update(deltaTime);
        }
    }

    syncModel(modelId, x, y, z, rotX, rotY, rotZ, scaleX, scaleY, scaleZ) {
        let model = this.models[modelId];

        if (model != null) {
            model.position.set(x, y, z);
            model.rotation.set(rotX, rotY - 0.5 * Math.PI, rotZ);
            model.scale.set(scaleX, scaleY, scaleZ);
        }
    }

    getCanvas() {
        return this.renderer.domElement;
    }

    hasOverlayCanvas() {
        return true;
    }

    onLoadImage(id, imageElement) {
    }

    changeAmbientLight(color) {
        this.ambientLight.color.setHex(this.parseColor(color));
    }

    changeLight(color) {
        this.light.color.setHex(this.parseColor(color));
    }

    moveCamera(x, y, z, targetX, targetY, targetZ) {
        this.camera.position.x = x;
        this.camera.position.y = y;
        this.camera.position.z = z;
        this.camera.lookAt(targetX, targetY, targetZ);
    }

    loadModel(meshId, path, callback) {
        let loader = new THREE.FBXLoader();
        loader.load(path, result => this.onModelLoad(meshId, result, callback));
    }

    onModelLoad(meshId, result, callback) {
        let meshes = result.children.filter(child => child.type == "Mesh");

        if (meshes.length != 1) {
            throw "Model file contains multiple meshes";
        }

        meshes.forEach(mesh => {
            this.registerMeshData(meshId, mesh.geometry, mesh.material, result.animations);
        });

        this.registerAnimations(result, callback);
    }

    registerAnimations(result, callback) {
        let animNames = [];
        let animDurations = [];

        for (let i = 0; i < result.animations.length; i++) {
            animNames.push(result.animations[i].name);
            animDurations.push(result.animations[i].duration);
        }

        callback(animNames, animDurations);
    }

    addModel(modelId, meshId) {
        this.requestedModels[modelId] = meshId;
    }

    removeModel(modelId) {
        this.scene.remove(this.models[modelId]);
        delete this.models[modelId];
        delete this.requestedModels[modelId];
    }

    clearModels() {
        for (let id in this.models) {
            this.scene.remove(this.models[id]);
        }
        this.models = {};
        this.requestedModels = {};
    }

    playAnimation(modelId, meshId, name, loop) {
        let mixer = new THREE.AnimationMixer(this.models[modelId]);
        this.animations[modelId] = mixer;

        let mesh = this.meshData[meshId];
        let anim = THREE.AnimationClip.findByName(mesh.animations, name);
        let action = mixer.clipAction(anim);
        action.loop = loop ? THREE.LoopRepeat : THREE.LoopOnce;
        action.play();
    }

    createBox(meshId, sizeX, sizeY, sizeZ, color, texturePath) {
        let geometry = new THREE.BoxGeometry(sizeX, sizeY, sizeZ);
        let material = this.createMaterial(color, texturePath);
        this.registerMeshData(meshId, geometry, material);
    }

    createSphere(meshId, diameter, color, texturePath) {
        let geometry = new THREE.SphereGeometry(diameter / 2);
        let material = this.createMaterial(color, texturePath);
        this.registerMeshData(meshId, geometry, material);
    }

    registerMeshData(id, geometry, material, animations) {
        this.meshData[id] = {
            id: id,
            geometry: geometry,
            material: material,
            animations: animations
        };
    }

    createMaterial(color, texturePath) {
        let properties = {};
        if (texturePath != null) {
            properties.map = this.getTexture(texturePath);
        } else if (color != null) {
            properties.color = this.parseColor(color);
        }

        return new THREE.MeshBasicMaterial(properties);
    }

    getTexture(path) {
        if (this.textures[path]) {
            return this.textures[path];
        } else {
            let textureLoader = new THREE.TextureLoader();
            let texture = textureLoader.load(path);
            this.textures[path] = texture;
            return texture;
        }
    }

    parseColor(hexColor) {
        if (hexColor.length != 7) {
            throw "Invalid hex color string: " + hexColor;
        }
        return parseInt("0x" + hexColor.substring(1));
    }

    // 2D graphics are delegated to another renderer that will use the HTML5
    // canvas drawing API, which is then drawn on top of the WebGL canvas.

    drawRect(x, y, width, height, color, alpha) {
        this.delegate2D.drawRect(x, y, width, height, color, alpha);
    }

    drawCircle(x, y, radius, color, alpha) {
        this.delegate2D.drawCircle(x, y, radius, color, alpha);
    }

    drawPolygon(points, color, alpha) {
        this.delegate2D.drawPolygon(points, color, alpha);
    }

    drawImage(id, x, y, width, height, alpha, mask) {
        this.delegate2D.drawImage(id, x, y, width, height, alpha, mask);
    }

    drawImageRegion(id, regionX, regionY, regionWidth, regionHeight, x, y, width, height,
                    rotation, scaleX, scaleY, alpha, mask) {
        this.delegate2D.drawImageRegion(id, regionX, regionY, regionWidth, regionHeight,
            x, y, width, height, rotation, scaleX, scaleY, alpha, mask);
    }

    drawText(text, font, size, color, bold, x, y, align, alpha) {
        this.delegate2D.drawText(text, font, size, color, bold, x, y, align, alpha);
    }
    
    getName() {
        return "Three.js renderer";
    }
}
