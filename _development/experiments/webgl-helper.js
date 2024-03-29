function createShaderProgram(gl, vertexShader, fragmentShader) {
    const shaderProgram = gl.createProgram();
    compileShader(gl, shaderProgram, gl.VERTEX_SHADER, vertexShader);
    compileShader(gl, shaderProgram, gl.FRAGMENT_SHADER, fragmentShader);
    gl.linkProgram(shaderProgram);

    if (!gl.getProgramParameter(shaderProgram, gl.LINK_STATUS)) {
        throw new Error("Shader link error: " + gl.getProgramInfoLog(shaderProgram));
    }

    return shaderProgram;
}

function compileShader(gl, shaderProgram, type, glsl) {
    const shader = gl.createShader(type);
    gl.shaderSource(shader, glsl);
    gl.compileShader(shader);

    if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {
        gl.deleteShader(shader);
        throw new Error("Shader compile error: " + gl.getShaderInfoLog(shader));
    }

    gl.attachShader(shaderProgram, shader);
}

function loadTexture(gl, imageURL) {
    const texture = gl.createTexture();
    gl.bindTexture(gl.TEXTURE_2D, texture);
    const placeholder = new Uint8Array([255, 255, 255, 255]);
    gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, 1, 1, 0, gl.RGBA, gl.UNSIGNED_BYTE, placeholder);

    const imageElement = document.createElement("img");
    imageElement.addEventListener("load", event => {
        gl.bindTexture(gl.TEXTURE_2D, texture);
        gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, imageElement);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
        gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.LINEAR);
    });
    imageElement.src = imageURL;

    gl.pixelStorei(gl.UNPACK_FLIP_Y_WEBGL, true);

    return texture;
}

function fillBuffer(gl, x0, y0, x1, y1) {
    const data = [
        x0, y0,
        x1, y0,
        x0, y1,
        x0, y1,
        x1, y0,
        x1, y1
    ];

    gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(data), gl.STATIC_DRAW);
}

function createBuffer(gl, x0, y0, x1, y1) {
    const buffer = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, buffer);
    fillBuffer(gl, x0, y0, x1, y1);
    gl.bindBuffer(gl.ARRAY_BUFFER, null);
    return buffer;
}

function renderQuad(gl, shaderProgram, vertexBuffer, textureBuffer, color, rotation, scale) {
    const aVertexPosition = gl.getAttribLocation(shaderProgram, "aVertexPosition");
    const aTextureCoordinates = gl.getAttribLocation(shaderProgram, "aTextureCoordinates");
    const uColor = gl.getUniformLocation(shaderProgram, "uColor");
    const uScalingFactor = gl.getUniformLocation(shaderProgram, "uScalingFactor");
    const uRotationVector = gl.getUniformLocation(shaderProgram, "uRotationVector");

    gl.uniform4fv(uColor, color);
    gl.uniform2fv(uScalingFactor, scale);
    gl.uniform2fv(uRotationVector, rotation);

    gl.bindBuffer(gl.ARRAY_BUFFER, vertexBuffer);
    gl.enableVertexAttribArray(aVertexPosition);
    gl.vertexAttribPointer(aVertexPosition, 2, gl.FLOAT, false, 0, 0);

    gl.bindBuffer(gl.ARRAY_BUFFER, textureBuffer);
    gl.enableVertexAttribArray(aTextureCoordinates);
    gl.vertexAttribPointer(aTextureCoordinates, 2, gl.FLOAT, false, 0, 0);

    gl.drawArrays(gl.TRIANGLES, 0, 12 / 2);
}
