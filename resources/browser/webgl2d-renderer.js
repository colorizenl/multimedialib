//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2020 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

/**
 * Uses WebGL to draw graphics on the canvas. WebGL is supported by all modern
 * browsers, but may not be supported by the platform itself.
 */
class WebGL2DRenderer {

    constructor(glContext) {
        if (glContext == null) {
            throw "WebGL not supported";
        }

        this.gl = glContext;
        this.textures = {};

        this.gl.enable(this.gl.BLEND);
        this.gl.blendFunc(this.gl.ONE, this.gl.ONE_MINUS_SRC_ALPHA);

        this.shaderProgram = this.initShaderProgram();
        this.shaderVariables = this.bindShaderVariables(this.shaderProgram);       
        this.vertexBuffer = this.gl.createBuffer();
        this.textureBuffer = this.gl.createBuffer();
    }

    initShaderProgram() {
        let vertexShader = this.loadShader(this.gl.VERTEX_SHADER, this.initVertexShader());
        let fragmentShader = this.loadShader(this.gl.FRAGMENT_SHADER, this.initFragmentShader());

        let shaderProgram = this.gl.createProgram();
        this.gl.attachShader(shaderProgram, vertexShader);
        this.gl.attachShader(shaderProgram, fragmentShader);
        this.gl.linkProgram(shaderProgram);

        if (!this.gl.getProgramParameter(shaderProgram, this.gl.LINK_STATUS)) {
            throw "Error while initializing WebGL shader: " + this.gl.getProgramInfoLog(shaderProgram);
        }
        
        return shaderProgram;
    }
    
    loadShader(type, glsl) {
        let shader = this.gl.createShader(type);
        this.gl.shaderSource(shader, glsl);
        this.gl.compileShader(shader);

        if (!this.gl.getShaderParameter(shader, this.gl.COMPILE_STATUS)) {
            throw "Error while loading WebGL shader: " + this.gl.getShaderInfoLog(shader);
        }

        return shader;
    }

    initVertexShader() {
        return `
            attribute vec2 aVertexPosition;
            attribute vec2 aTextureCoordinates;

            uniform vec2 uTranslation;
            uniform vec2 uRotation;
            uniform vec2 uScale;
            uniform vec2 uResolution;
            
            varying vec2 vTextureCoordinates;

            void main() {
                vec2 scaledPosition = aVertexPosition * uScale;
            
                vec2 rotatedPosition = vec2(
                    scaledPosition.x * uRotation.y + scaledPosition.y * uRotation.x,
                    scaledPosition.y * uRotation.y - scaledPosition.x * uRotation.x
                );
                
                vec2 position2D = rotatedPosition + uTranslation;
                vec2 normalizedPosition1 = position2D / uResolution;
                vec2 normalizedPosition2 = normalizedPosition1 * 2.0;
                vec2 normalizedPosition3 = normalizedPosition2 - 1.0;
                gl_Position = vec4(normalizedPosition3 * vec2(1, -1), 0, 1);

                vTextureCoordinates = aTextureCoordinates;
            }
        `;
    }

    initFragmentShader() {
        return `
            precision mediump float;
            
            uniform vec4 uColor;
            uniform sampler2D uTexture;
            
            varying vec2 vTextureCoordinates;
            
            void main() {
                if (vTextureCoordinates.x < 0.0 || vTextureCoordinates.y < 0.0 ||
                        vTextureCoordinates.x > 1.0 || vTextureCoordinates.y > 1.0) {
                    gl_FragColor = uColor;
                } else {
                    gl_FragColor = texture2D(uTexture, vTextureCoordinates);
                }
            }
        `;
    }
    
    bindShaderVariables(shaderProgram) {
        return {
            vertexPosition: this.gl.getAttribLocation(shaderProgram, "aVertexPosition"),
            textureCoordinates: this.gl.getAttribLocation(shaderProgram, "aTextureCoordinates"),
        
            translation: this.gl.getUniformLocation(shaderProgram, "uTranslation"),
            rotation: this.gl.getUniformLocation(shaderProgram, "uRotation"),
            scale: this.gl.getUniformLocation(shaderProgram, "uScale"),
            resolution: this.gl.getUniformLocation(shaderProgram, "uResolution"),
            color: this.gl.getUniformLocation(shaderProgram, "uColor"),
            texture: this.gl.getUniformLocation(shaderProgram, "uTexture"),
        };
    }

    render(deltaTime) {
        this.gl.viewport(0, 0, this.gl.canvas.width, this.gl.canvas.height);
        this.gl.clearColor(0.0, 0.0, 0.0, 1.0);
        this.gl.clear(this.gl.COLOR_BUFFER_BIT | this.gl.DEPTH_BUFFER_BIT);
        this.gl.useProgram(this.shaderProgram);

        if (overlayCanvas != null) {
            this.initOverlayContext();
            this.overlayContext.clearRect(0, 0, overlayCanvas.width, overlayCanvas.height);
        }
    }

    getCanvas() {
        return canvas;
    }

    hasOverlayCanvas() {
        return true;
    }

    onLoadImage(id, imageElement) {
        let texture = this.loadTexture(id);
        this.gl.bindTexture(this.gl.TEXTURE_2D, texture);
        this.gl.texImage2D(this.gl.TEXTURE_2D, 0, this.gl.RGBA, this.gl.RGBA,
            this.gl.UNSIGNED_BYTE, imageElement);
    }

    drawRect(x, y, width, height, color, alpha) {
        let vertices = this.toVertices(width, height);
        let textureCoordinates = vertices.map(p => -1);
        let colorVector = this.toColorVector(color, alpha);
        let translation = [x + width / 2, y + height / 2];
        let rotation = this.toRotationVector(0);
    
        this.draw(vertices, null, textureCoordinates, colorVector, translation, rotation, [1, 1]);
    }

    drawCircle(x, y, radius, color, alpha) {
        let points = [];
        let numVertices = 32;
        
        for (let i = 0; i < numVertices; i++) {
            let angle = i * ((2 * Math.PI) / numVertices);
            points.push(x + radius * Math.cos(angle));
			points.push(y + radius * Math.sin(angle));
        }
        
        this.drawPolygon(points, color, alpha);
    }

    drawPolygon(points, color, alpha) {
        let vertices = points.length <= 6 ? points : this.subdividePolygon(points);
        let textureCoordinates = vertices.map(p => -1);
        let colorVector = this.toColorVector(color, alpha);
        let rotation = this.toRotationVector(0);

        this.draw(vertices, null, textureCoordinates, colorVector, [0, 0], rotation, [1, 1]);
    }

    drawImage(id, x, y, width, height, alpha, mask) {
        if (images[id]) {
            this.drawImageRegion(id, 0, 0, images[id].width, images[id].height,
                x, y, width, height, 0, 100, 100, alpha, mask);
        }
    }

    drawImageRegion(id, regionX, regionY, regionWidth, regionHeight, x, y, width, height,
                    rotation, scaleX, scaleY, alpha, mask) {
        if (this.isImageAvailable(id)) {
            let vertices = this.toVertices(width, height);
            let textureImage = images[id];
            let texture = this.loadTexture(id);
            let textureCoordinates = this.toTextureCoordinates(textureImage, regionX, regionY,
                regionWidth, regionHeight);
            
            let colorVector = this.toColorVector("#FFFFFF", alpha);
            let translationVector = [x, y];
            let rotationVector = this.toRotationVector(rotation);
            let scaleVector = [scaleX, scaleY];
            
            if (mask != null) {
                textureCoordinates = vertices.map(v => -1);
                colorVector = this.toColorVector(mask, alpha);
            }

            this.draw(vertices, texture, textureCoordinates, 
                colorVector, translationVector, rotationVector, scaleVector);
        }
    }
    
    draw(vertices, texture, textureCoordinates, color, translation, rotation, scale) {
        if (texture != null) {
            this.gl.activeTexture(this.gl.TEXTURE0);
            this.gl.bindTexture(this.gl.TEXTURE_2D, texture);
        } else {
            this.gl.bindTexture(this.gl.TEXTURE_2D, null);
        }
        
        this.gl.bindBuffer(this.gl.ARRAY_BUFFER, this.vertexBuffer);
        this.gl.enableVertexAttribArray(this.shaderVariables.vertexPosition);
        this.gl.bufferData(this.gl.ARRAY_BUFFER, new Float32Array(vertices), this.gl.STATIC_DRAW);
        this.gl.vertexAttribPointer(this.shaderVariables.vertexPosition, 2, this.gl.FLOAT, false, 0, 0);
        
        this.gl.bindBuffer(this.gl.ARRAY_BUFFER, this.textureBuffer);
        this.gl.bufferData(this.gl.ARRAY_BUFFER, new Float32Array(textureCoordinates), this.gl.STATIC_DRAW);
        this.gl.vertexAttribPointer(this.shaderVariables.textureCoordinates, 2, this.gl.FLOAT, false, 0, 0);
        this.gl.enableVertexAttribArray(this.shaderVariables.textureCoordinates);
            
        this.gl.uniform2f(this.shaderVariables.resolution, this.gl.canvas.width, this.gl.canvas.height);
        this.gl.uniform2fv(this.shaderVariables.translation, translation);
        this.gl.uniform2fv(this.shaderVariables.rotation, rotation);
        this.gl.uniform2fv(this.shaderVariables.scale, scale);
        this.gl.uniform4fv(this.shaderVariables.color, color);
        this.gl.uniform1i(this.shaderVariables.texture, 0);
        
        this.gl.drawArrays(this.gl.TRIANGLES, 0, vertices.length / 2);
    }
    
    loadTexture(id) {
        let texture = this.textures[id];
    
        if (texture == null) {
            texture = this.gl.createTexture();  
            this.gl.bindTexture(this.gl.TEXTURE_2D, texture);
            this.gl.texImage2D(this.gl.TEXTURE_2D, 0, this.gl.RGBA, 1, 1, 0, this.gl.RGBA,
                this.gl.UNSIGNED_BYTE, new Uint8Array([255, 255, 255, 255]));
            this.gl.texParameteri(this.gl.TEXTURE_2D, this.gl.TEXTURE_WRAP_S, this.gl.CLAMP_TO_EDGE);
            this.gl.texParameteri(this.gl.TEXTURE_2D, this.gl.TEXTURE_WRAP_T, this.gl.CLAMP_TO_EDGE);
            this.gl.texParameteri(this.gl.TEXTURE_2D, this.gl.TEXTURE_MIN_FILTER, this.gl.LINEAR);
            this.gl.texParameteri(this.gl.TEXTURE_2D, this.gl.TEXTURE_MAG_FILTER, this.gl.LINEAR);
            this.textures[id] = texture;
        }
        
        return texture;
    }
    
    isImageAvailable(id) {
        return images[id] && images[id].width > 0 && images[id].height > 0;
    }

    drawText(text, font, size, color, bold, x, y, align, alpha) {
        this.initOverlayContext();

        this.overlayContext.globalAlpha = alpha;
        this.overlayContext.fillStyle = color;
        this.overlayContext.font = (bold ? "bold " : "") + size + "px " + font;
        this.overlayContext.textAlign = align;
        this.overlayContext.fillText(text, x, y);
        this.overlayContext.globalAlpha = 1.0;
    }

    initOverlayContext() {
        if (this.overlayContext == null) {
            this.overlayContext = overlayCanvas.getContext("2d");
        }
    }
    
    toVertices(width, height) {
        return [
            -width / 2, -height / 2,
            width / 2, -height / 2,
            -width / 2, height / 2,
            -width / 2, height / 2,
            width / 2, -height / 2,
            width / 2, height / 2
        ];
    }
    
    toTextureCoordinates(image, x, y, width, height) {
        x = Math.max(x, 1);
        y = Math.max(y, 1);
        width = Math.min(width, image.width - 2);
        height = Math.min(height, image.height - 2);
        
        return [
            x / image.width, y / image.height,
            (x + width) / image.width, y / image.height,
            x / image.width, (y + height) / image.height,
            x / image.width, (y + height) / image.height,
            (x + width) / image.width, y / image.height,
            (x + width) / image.width, (y + height) / image.height
        ];
    }
    
    toRotationVector(radians) {
        return [Math.sin(-radians), Math.cos(-radians)];
    }
    
    toColorVector(hexColor, alpha) {
        let rgb = toRGB(hexColor);
        return [rgb[0] / 255.0, rgb[1] / 255.0, rgb[2] / 255.0, alpha];
    }
    
    getPolygonCenter(vertices) {
        let minX = vertices[0];
        let minY = vertices[1];
        let maxX = vertices[0];
        let maxY = vertices[1];
        
        for (let i = 2; i < vertices.length; i += 2) {
            minX = Math.min(minX, vertices[i]);
            maxX = Math.max(maxX, vertices[i]);
            minY = Math.min(minY, vertices[i + 1]);
            maxY = Math.max(maxY, vertices[i + 1]);
        }
        
        return [minX + (maxX - minX) / 2.0, minY + (maxY - minY) / 2.0];
    }
    
    subdividePolygon(points) {
        let vertices = [];
        let center = this.getPolygonCenter(points);
        
        for (let i = 0; i < points.length; i += 2) {
            vertices.push(points[i]);
            vertices.push(points[i + 1]);
            
            if (i >= 2) {
                vertices.push(center[0]);
                vertices.push(center[1]);
                vertices.push(points[i]);
                vertices.push(points[i + 1]);
            }
            
            if (i == points.length - 2) {
                vertices.push(points[0]);
                vertices.push(points[1]);
                vertices.push(center[0]);
                vertices.push(center[1]);
            }
        }
        
        return vertices;
    }
    
    getName() {
        return "WebGL 2D renderer";
    }
}
