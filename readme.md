MultimediaLib
=============

Framework for building multimedia applications that support desktop, mobile, and web. 
MultimediaLib is mainly targeted at 2D graphics and animation, though 3D graphics are also 
supported.

![MultimediaLib example screenshot](_development/example.jpg)

MultimediaLib supports several different platforms:

- **Desktop:** Windows, Mac OS, Linux
- **Mobile:** iOS, Android
- **Web:** All modern browsers, PWA

MultimediaLib acts as an abstraction layer between the application layer and the underlying 
platform. This is a similar approach to other frameworks, but MultimediaLib differs in that it 
targets not only different mobile platforms, but also allows the same application to be used on 
desktop platforms and from the browser.
    
Usage
-----

The library is available from the Maven Central repository. To use it in a Maven project, add it 
to the dependencies section in `pom.xml`:

    <dependency>
        <groupId>nl.colorize</groupId>
        <artifactId>multimedialib</artifactId>
        <version>2022.1</version>
    </dependency>  
    
The library can also be used in Gradle projects:

    dependencies {
        implementation "nl.colorize:multimedialib:2022.1"
    }
    
Supported platforms
-------------------

The *renderer* is the central access point for all platform-specific functionality, as depicted
in the picture above. Applications can access the renderer to display graphics, load media, check
for user input, or internet access.

![MultimediaLib platform architecture](_development/platform-architecture.svg)

MultimediaLib contains a number of renderer implementations, for different platforms and for
different types of applications. Some renderers are implemented using the platforms' native 
graphics API, other renderers are implemented on top of other libraries or frameworks. 
The following table shows an overview of the available renderer implementations:

| Renderer                                                                             | Desktop | iOS | Android | Web | Graphics |
|--------------------------------------------------------------------------------------|---------|-----|---------|-----|----------|
| Java2D renderer                                                                      | ✓       | ×   | ×       | ×   | 2D       |
| [libGDX](https://libgdx.badlogicgames.com) / [LWJGL](https://www.lwjgl.org) renderer | ✓       | ×   | ×       | ×   | 2D + 3D  |
| HTML5 canvas renderer                                                                | ✓       | ✓   | ✓       | ✓   | 2D       |
| [PixiJS](https://www.pixijs.com) renderer                                            | ✓       | ✓   | ✓       | ✓   | 2D       |
| [three.js](https://threejs.org) renderer                                             | ✓       | ✓   | ✓       | ✓   | 2D + 3D  |

When using a browser-based renderer, the application needs to be transpiled to JavaScript in order
for it to run in the browser. MultimediaLib includes a command line tool for integrating this step
into the build, refer to the section *Transpiling applications to HTML/JavaScript* below.

Not all platforms and renderers will support all features. Refer to the
[platform/renderer compatibility table](compatibility.md) for a full overview of supported features.

Application structure
---------------------

MultimediaLib uses a number of concepts similar to
[Adobe Flash](https://en.wikipedia.org/wiki/Adobe_Flash), both in terms of its theatre-inspired
terminology and in terms of how applications are split into multiple scenes.

![MultimediaLib application architecture](_development/application-architecture.svg)

Each scene represents a discrete part or phase of an application that is active for some period 
of time. Only one scene can be active at any point in time. Simple applications may consist of a 
single scene, while larger applications will typically have many. The currently active scene will 
receive frame updates for as long as it is active.

Scenes update the *stage*, which displays the graphics and sound for the current scene. The stage
consists of multiple layers of 2D graphics, 3D graphics, or a combination thereof. The stage is 
linked to the current scene, once the scene ends the stage is cleared and all contents are removed.
During frame updates, scenes use the *scene context* to access both the stage and the underlying 
platform.

Scene logic is based on the
[Entity Component System](https://en.wikipedia.org/wiki/Entity_component_system) architecture
pattern. The scene is split into a number of *actors* (also known as "entities"). Actors do not
contain data themselves. Instead, all data associated with an actor is located in *components*.
The actual logic is implemented in a number of *systems*. These systems update both the scene's
data (located in actors and their components) and the scene's stage. Simple scenes may consist of
a single system, while more complex scenes will consist of many. Most systems will be active for
the entire duration of the scene, but some systems may only be active for a limited period of time
or until a certain condition is reached.

Starting the demo applications
------------------------------

MultimediaLib includes simple demo applications that showcase some of its features, and can be 
used as an example when using the framework to create applications. The demo applications can also
be used for verification purposes when testing the framework on new platforms. Two demo
applications are included: one for 2D graphics and one for 3D graphics. 

To run the demo for desktop platforms, create a normal build of the library using
`gradle assemble`, which builds both the desktop and browser versions.

To start the desktop version of the demo application, run the class
`nl.colorize.multimedialib.tool.DemoLauncher`. This class supports the following command line 
parameters:

| Name                | Required | Description                                   |
|---------------------|----------|-----------------------------------------------|
| `--renderer`        | yes      | Renderer to use for the demo (java2d, gdx).   |
| `--graphics`        | yes      | Either '2d' or '3d'.                          |
| `--framerate`       | no       | Demo framerate, default is 60 fps.            |
| `--canvas`          | no       | Uses a fixed canvas size to display graphics. |

Note that when using 3D graphics on Mac OS the command line argument `-XstartOnFirstThread` must
be present.

The browser version of the demo applications can be created by running 
`gradle transpileDemoApplication2D` and `gradle transpileDemoApplication3D` respectively.
The build output is then saved to the directories `build/browserdemo2d` and `browserdemo3d`, and 
can be started by opening the corresponding `index.html` in a browser.

Transpiling applications to HTML/JavaScript
-------------------------------------------

Applications using MultimediaLib are written in Java. However, these applications can be transpiled
to a combination of HTML and JavaScript so that they can be distributed via the web. This is done
using [TeaVM](http://teavm.org). Transpilation is started using the `TeaVMTranspilerTool` that is 
included as part of the library. The tool provides a command line interface, and supports the 
following arguments:

| Name          | Required | Description                                            |
|---------------|----------|--------------------------------------------------------|
| `--project`   | yes      | Project name for the application.                      |
| `--resources` | yes      | Directory containing the application's resource files. |
| `--out`       | yes      | Output directory for the generated files.              |
| `--main`      | yes      | Main class that acts as application entry point.       |
| `--minify`    | no       | Minifies the generated JavaScript, off by default.     |

Loading image contents in JavaScript is not allowed unless when running on a remote host. This is
not a problem for "true" web applications, but can be problematic if the JavaScript version of the
application is embedded in a mobile app. For this reason, all image are converted to data URLs
during transpilation, so that they can be used without these restrictions.

Distributing applications
-------------------------

MultimediaLib does not include a distribution mechanism for applications, but it integrates with
other tools for each supported platform.

- **Windows:** Use [Launch4j](http://launch4j.sourceforge.net) to create a .exe file. 
  Alternatively, the browser version can be submitted to the Windows Store as a PWA.
- **Mac OS:** Create an application bundle and installer, and distribute those via the Mac App
  Store. A [Gradle plugin](https://plugins.gradle.org/plugin/nl.colorize.gradle.macapplicationbundle)
  is provided to generate the application bundle as part of the build.
- **iOS:** Use [Cordova](https://cordova.apache.org) to wrap the transpiled version of the
  application in a native app, and distribute that via the App Store. A
  [Gradle Cordova plugin](https://plugins.gradle.org/plugin/nl.colorize.gradle.cordova) is provided
  to generate the app as part of the build.
- **Android:** Use [Cordova](https://cordova.apache.org) to wrap the transpiled version of the
  application in a native app, and distribute that via the Play Store. The same
  [Gradle Cordova plugin](https://plugins.gradle.org/plugin/nl.colorize.gradle.cordova) can be
  used to generate this app as part of the build.
- **Web:** Upload the transpiled version of the application can be uploaded to a web server
  and distribute the corresponding URL.

Packing images into a sprite sheet
----------------------------------

A "sprite sheet" is a large image that consists of a large image that contains multiple sprites,
with each sprite is identified by a name and a set of coordinates. A sprite sheet consists of the
image plus a metadata file describing those coordinates. On most platforms sprite sheets have 
better performance characteristics than loading the images individually. 

MultimediaLib includes a tool to create a sprite sheet from all images within a directory. This
tool is started using `SpriteSheetPacker` that is part of the library. The tool takes the 
following arguments.

| Name               | Required | Description                                   |
|--------------------|----------|-----------------------------------------------|
| `--input`          | yes      | Directory containing source images.           |
| `--outimage`       | yes      | Generated image file location.                |
| `--outdata`        | yes      | Generated metadata file location.             |
| `--metadata`       | yes      | Metadata file format, either 'yaml' or 'csv'. |
| `--size <size>`    | yes      | Width/height of the sprite sheet.             |
| `--exclude <size>` | no       | Excludes all images beyond a certain size.    |

This will generate the PNG file containing the sprite sheet graphics and the YAML file with
metadata in the specified locations. Sprite sheets can then be loaded back as media assets.

Documentation
-------------

- [JavaDoc](http://api.clrz.nl/multimedialib/)

Build instructions
------------------

Building the library can be done on any platform. The following is mandatory for building the
library itself:

- [Java JDK](http://java.oracle.com) 17+
- [Gradle](http://gradle.org)

Note that creating application that *use* MultimediaLib will usually have additional dependencies,
depending on which platforms are targeted.

- [NodeJS](https://nodejs.org/en/) 14+
- [Cordova](https://cordova.apache.org) 
- [Xcode](https://developer.apple.com/xcode/) (for iOS apps)
- [Android SDK](https://developer.android.com/sdk/index.html) (for Android apps)

The following Gradle build tasks are available:

- `gradle clean` cleans the build directory
- `gradle assemble` creates the JAR file for distribution
- `gradle test` runs all unit tests
- `gradle coverage` runs all unit tests and reports on test coverage
- `gradle javadoc` generates the JavaDoc API documentation
- `gradle dependencyUpdates` checks for and reports on library updates.
- `gradle -b build-mavencentral.gradle publish` publishes to Maven central (requires account)
  
License
-------

Copyright 2009-2022 Colorize

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
