//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2025 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

import * as THREE from "three";
import {GLTFLoader} from "three/loaders/GLTFLoader";
import {OBJLoader} from "three/loaders/OBJLoader";
import {MTLLoader} from "three/loaders/MTLLoader";

const FIELD_OF_VIEW = 75;
const NEAR_PLANE = 1;
const FAR_PLANE = 300;

/**
 * Bridge interface for the parts of the Three.js interface implemented in
 * JavaScript. This interface is called from the renderer via TeaVM.
 */
export class ThreeBridge {

    init() {
        this.textureCache = {};

        const width = window.innerWidth;
        const height = window.innerHeight;

        this.scene = new THREE.Scene();
        this.camera = new THREE.PerspectiveCamera(FIELD_OF_VIEW, width / height, NEAR_PLANE, FAR_PLANE);

        this.ambientLight = new THREE.AmbientLight("#DCDCDC", 3);
        this.scene.add(this.ambientLight);

        this.renderer = new THREE.WebGLRenderer();
        this.renderer.setPixelRatio(window.devicePixelRatio);
        this.renderer.setSize(width, height);

        const container = document.getElementById("multimediaLibContainer");
        container.appendChild(this.renderer.domElement);
        window.addEventListener("resize", () => this.resize());
    }

    resize() {
        const width = window.innerWidth;
        const height = window.innerHeight;

        this.camera.aspect = width / height;
        this.camera.updateProjectionMatrix();

        this.renderer.setSize(width, height);
    }

    render() {
        this.renderer.render(this.scene, this.camera);
    }

    changeBackgroundColor(color) {
        this.scene.background = new THREE.Color(color);
    }

    moveCamera(x, y, z, focusX, focusY, focusZ) {
        this.camera.position.set(x, y, z);
        this.camera.lookAt(focusX, focusY, focusZ);
    }

    createBox(width, height, depth, color) {
        const geometry = new THREE.BoxGeometry(width, height, depth);
        const material = this.createMaterial(color, null);
        return new THREE.Mesh(geometry, material);
    }

    createSphere(radius, color) {
        const geometry = new THREE.SphereGeometry(radius);
        const material = this.createMaterial(color, null);
        return new THREE.Mesh(geometry, material);
    }

    createLight(color, intensity) {
        return new THREE.PointLight(color, intensity);
    }

    createMaterial(color, texture) {
        const options = {};
        if (color) {
            options.color = color;
        }
        if (texture) {
            options.map = texture;
            options.transparent = true;
        }
        return new THREE.MeshStandardMaterial(options);
    }

    applyColor(model, color) {
        const material = this.createMaterial(color, null);
        model.traverse(child => {
            if (child.isMesh) {
                child.material = material;
            }
        });
    }

    applyTexture(model, image, x, y, width, height) {
        this.prepareTexture(image, x, y, width, height, texture => {
            model.material = this.createMaterial(null, texture);
            model.needsUpdate = true;
        });
    }

    prepareTexture(image, x, y, width, height, callback) {
        const cacheKey = image.src + "/" + x + "/" + y + "/" + width + "/" + height;

        if (this.textureCache[cacheKey]) {
            callback(this.textureCache[cacheKey]);
            return;
        }

        const canvas = document.createElement("canvas");
        canvas.width = width;
        canvas.height = height;

        const context = canvas.getContext("2d");
        context.drawImage(image, x, y, width, height, 0, 0, width, height);

        const subImage = document.createElement("img");
        subImage.onload = () => {
            const texture = new THREE.Texture(
                subImage,
                THREE.UVMapping,
                THREE.ClampToEdgeWrapping,
                THREE.ClampToEdgeWrapping
            );
            texture.colorSpace = THREE.SRGBColorSpace;
            texture.needsUpdate = true;
            this.textureCache[cacheKey] = texture;
            callback(texture);
        };
        subImage.src = canvas.toDataURL("image/png");
    }

    cloneObject(original) {
        return original.clone();
    }

    loadGLTF(url, callback) {
        const gltfLoader = new GLTFLoader();
        gltfLoader.load(url, gltf => callback(gltf.scene));
    }

    loadOBJ(url, callback) {
        const fileName = url.substring(url.lastIndexOf("/") + 1);
        const dir = url.substring(0, url.lastIndexOf("/") + 1);

        const mtlLoader = new MTLLoader();
        mtlLoader.setPath(dir);
        mtlLoader.load(fileName.replace(".obj", ".mtl"), mtl => {
            mtl.preload();

            const objLoader = new OBJLoader();
            objLoader.setPath(dir);
            objLoader.setMaterials(mtl);
            objLoader.load(fileName, obj => callback(obj));
        });
    }

    project(x, y, z) {
        const position = new THREE.Vector3(x, y, z);
        position.project(this.camera);
        return position;
    }

    castPickRay(pointX, pointY, x0, y0, z0, x1, y1, z1) {
        const raycaster = new THREE.Raycaster();
        raycaster.setFromCamera(new THREE.Vector2(pointX, pointY), this.camera);

        const boundingBox = new THREE.Box3(
            new THREE.Vector3(x0, y0, z0),
            new THREE.Vector3(x1, y1, z1)
        );

        const target = new THREE.Vector3();
        return raycaster.ray.intersectBox(boundingBox, target) != null;
    }
}
