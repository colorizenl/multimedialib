Renderer compatibility table
============================

MultimediaLib supports several renderers, in order to support different types of applications
on different platform. Unfortunately, it is not possible to fully support every single
MultimediaLib feature on every single renderer across every single platform.

This compatibility table therefore provides a more detailed overview of supported features.
The compatibility table uses the following notation:

- Green (🟢) means the feature is fully supported by the renderer.
- Yellow (🟡) means the feature is not natively supported by the renderer, but is emulated.
- Orange (🟠) means the feature is *partially* supported, see footnotes for details.
- Red (🔴) means the feature is *not* supported by the renderer.
- Purple (🟣) means the feature is not yet supported, but *will* be supported in a future version. 

| Feature / Renderer       | Java2D | libGDX | HTML Canvas | WebGL  | Pixi   | Three  | 
|--------------------------|--------|--------|-------------|--------|--------|--------|
| **Animation loop**       |
| Canvas size              | 🟢     | 🟢     | 🟢          | 🟢     | 🟢     | 🟣     |
| Resize events            | 🟢     | 🟢     | 🟢          | 🟢     | 🟢     | 🟣     |
| Custom framerate         | 🟢     | 🟢     | 🟢          | 🟢     | 🟢     | 🟢     |
| **Sprites**              | 
| Sprites                  | 🟢     | 🟢     | 🟢          | 🟢     | 🟢     | 🟣     |
| Image translucency       | 🟢     | 🟢     | 🟢          | 🟢     | 🟢     | 🟣     |
| Image region             | 🟢     | 🟢     | 🟢          | 🟢     | 🟢     | 🟣     |
| Rotation                 | 🟢     | 🟢     | 🟢          | 🟠     | 🟢     | 🟣     |
| Scale                    | 🟢     | 🟢     | 🟢          | 🟢     | 🟢     | 🟣     |
| Independent X/Y scale    | 🟢     | 🟢     | 🟢          | 🟢     | 🟢     | 🟣     |
| Alpha                    | 🟢     | 🟢     | 🟢          | 🟢     | 🟢     | 🟣     |
| Flip horizontal/vertical | 🟢     | 🟢     | 🟢          | 🟢     | 🟢     | 🟣     |
| Retrieve pixel data      | 🟢     | 🟢     | 🟢          | 🟢     | 🟢     | 🟢     |
| **2D Graphics**          |
| Lines                    | 🟢     | 🟠 (3) | 🟢          | 🟡     | 🟢     | 🟣     |
| Segmented lines          | 🟢     | 🟢     | 🟢          | 🟡     | 🟢     | 🟣     |
| Rectangles               | 🟢     | 🟢     | 🟢          | 🟢     | 🟢     | 🟣     |
| Circles                  | 🟡 (2) | 🟢     | 🟢          | 🟡     | 🟢     | 🟣     |
| Polygons                 | 🟢     | 🟢     | 🟢          | 🟡     | 🟢     | 🟣     |
| Alpha                    | 🟢     | 🟢     | 🟢          | 🟡     | 🟢     | 🟣     |
| **Text**                 |
| TrueType fonts           | 🟢     | 🟢     | 🟢          | 🟡     | 🟢     | 🟣     |
| Text alignment           | 🟢     | 🟢     | 🟢          | 🟡     | 🟢     | 🟣     |
| Text alpha               | 🟢     | 🟢     | 🟢          | 🟡     | 🟢     | 🟣     |
| **Media**                |
| PNG images               | 🟢     | 🟢     | 🟢          | 🟢     | 🟢     | 🟢     |
| MP3 audio                | 🟠 (1) | 🟢     | 🟢          | 🟢     | 🟢     | 🟢     |
| **Input**                |
| Keyboard controls        | 🟢     | 🟢     | 🟢          | 🟢     | 🟢     | 🟢     |
| Mouse/touch controls     | 🟢     | 🟢     | 🟠 (4)      | 🟠 (4) | 🟠 (4) | 🟠 (4) |
| **Network**              |
| HTTP requests            | 🟢     | 🟢     | 🟢          | 🟢     | 🟢     | 🟢     |

Footnotes:

1. Not supported on Windows.
2. Emulated due to poor native performance.
3. Custom line thickness not supported.
4. Multi-touch detection not supported.
