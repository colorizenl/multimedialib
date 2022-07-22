Platform/renderer compatibility table
=====================================

MultimediaLib aims to support as many platforms as possible, across desktop, mobile, and web.
To achieve this, it provides multiple renderers that can be used depending on the targeted
platform(s). Unfortunately, it is not possible to support every single feature across every single
renderer, due to lack of platform support. The table below provides an overview of which features
are supported by which renderer.

| Area        | Feature                   | Java2D | libGDX | HTML Canvas | PixiJS | three.js |
|-------------|---------------------------|--------|--------|-------------|--------|----------|
| Application | Configurable resolution   | ✓      | ✓      | ✓           | ✓      |
|             | Configurable framerate    | ✓      | ✓      | ✓           | ✓      |
|             | Screenshots               | ✓      |    
|             | Preferences               | ✓      | ✓      | ✓ (3)       | ✓ (3)  | ✓ (3)    |
|             | Framerate statistics      | ✓      | ✓      | ✓           | ✓      | ✓        |
| Input       | Mouse                     | ✓      | ✓      | ✓           | ✓      | ✓        |
|             | Keyboard                  | ✓      | ✓      | ✓           | ✓      | ✓        |
|             | Touch                     |        |        | ✓           | ✓      | ✓        |
| 2D Graphics | Sprites                   | ✓      | ✓      | ✓           | ✓      |
|             | Sprite rotation           | ✓      |
|             | Sprite scale              | ✓      |
|             | Sprite flip               | ✓      |
|             | Sprite alpha              | ✓      |
|             | Sprite mask/tint          | ✓      | ✓      | ✓           | ✓      |
|             | Lines                     | ✓      | ✓ (1)  | ✓           | ✓      |
|             | Rectangles                | ✓      | ✓      | ✓           |
|             | Circles                   | ✓      | ✓      | ✓           |
|             | Polygons                  | ✓      | ✓      | ✓           |
|             | Text                      | ✓      | ✓      | ✓           |
|             | Multi-line text           | ✓      | ✓      | ✓           |
|             | TrueType fonts            |        | ✓      | ✓           |
|             | Particle effects          | ✓      | ✓      | (2)         |
| 3D Graphics | Load GLTF models          |        |
|             | Load/play GLTF animations |        |
| Audio       | Load MP3                  | ✓      | ✓      | ✓           | ✓      | ✓        |
|             | Load OGG                  | ✓      | ✓      | ✓           | ✓      | ✓        |
| Network     | HTTP requests             | ✓      | ✓      | ✓           | ✓      | ✓        |
|             | Web sockets               | ✓      | ✓      | ✓           | ✓      | ✓        |
|             | WebRTC                    |        |        |             |        |          |

1. Line thickness is not supported.  
2. Not supported due to performance issues.
3. Stored in browser LocalStorage, might be removed by the browser when history is removed.

Known issues
------------

- PixiJS: Layers and z-index are not properly supported.
- PixiJS: Text is misaligned vertically.
- PixiJS: Fonts are not properly supported.
- PixiJS: Sprite scale/alignment is off.
- PixiJS: Tint/mask is not supported.
