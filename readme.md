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
        <version>2021.3</version>
    </dependency>  
    
The library can also be used in Gradle projects:

    dependencies {
        compile "nl.colorize:multimedialib:2021.3"
    }
    
Supported platforms
-------------------

The *renderer* is the central access point for all platform-specific functionality, as depicted
in the picture above. Applications can access the renderer to display graphics, load media, check
for user input, or internet access.

![MultimediaLib architecture](_development/architecture.svg)

MultimediaLib contains a number of renderer implementations, for different platforms and for
different types of applications. Some renderers are implemented using the platforms' native 
graphics API, other renderers are implemented on top of other libraries or frameworks. 
The following table shows an overview of the available renderer implementations:

| Renderer                                                                             | Desktop | iOS | Android | Web | Graphics |
|--------------------------------------------------------------------------------------|---------|-----|---------|-----|----------|
| Java2D renderer                                                                      | ✓       | ×   | ×       | ×   | 2D       |
| [libGDX](https://libgdx.badlogicgames.com) / [LWJGL](https://www.lwjgl.org) renderer | ✓       | ×   | ×       | ×   | 2D + 3D  |
| libGDX / [RoboVM](http://robovm.mobidevelop.com) renderer                            | ×       | ✓   | ×       | ×   | 2D + 3D  |
| HTML5 canvas renderer                                                                | ✓       | ✓   | ✓       | ✓   | 2D       |
| WebGL 2D renderer                                                                    | ✓       | ✓   | ✓       | ✓   | 2D       |
| [three.js](https://threejs.org) renderer                                             | ✓       | ✓   | ✓       | ✓   | 2D + 3D  |

When using the TeaVM renderer, the application needs to be transpiled to JavaScript in order for
it to run in the browser. MultimediaLib includes a command line tool for integrating this step
into the build, refer to the section *Transpiling applications to HTML/JavaScript* below.

### Additional instructions for building native iOS apps using RoboVM

When using the libGDX renderer in combination with RoboVM, applications will need to add the
following additional Maven or Gradle dependencies:

  - `com.badlogicgames.gdx:gdx-backend-robovm:$gdxVersion`
  - `com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-ios`
  - `com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-ios`

It is not possible use both "regular" Java and RoboVM in the same project, which is why 
MultimediaLib does not include these dependencies by default.

Concepts
--------

For application structure, MultimediaLib uses the same terminology from the theater world that
is used by animation software. The application consists of a *stage* and a number of *scenes*.

The stage contains everything that should be displayed. The stage can contain 2D graphics,
3D graphics, or a combination of the two.

Scenes are used to structure the application logic. Only one scene can be active at the same
time, but the application can return to the same scene multiple times, for example when accessing
a menu. When a scene ends, the stage is cleared. When the new scene is started, it can fill the
stage with everything that should be displayed during the scene. The scene will then update every
frame for as long as it is active. More complex scenes can be split into sub-scenes, with each
sub-scene responsible for one functional area. 
    
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

| Name             | Required | Description                                   |
|------------------|----------|-----------------------------------------------|
| -renderer        | yes      | Renderer to use for the demo (java2d, gdx).   |
| -graphics        | yes      | Either '2d' or '3d'.                          |
| -framerate       | no       | Demo framerate, default is 60 fps.            |
| -canvas          | no       | Uses a fixed canvas size to display graphics. |
| -orientationlock | no       | Restricts the demo to landscape orientation.  |
| -verification    | no       | Prints instructions for verification.         |

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
using [TeaVM](http://teavm.org) and therefore only supports a subset of the Java language. 
Transpilation is started using the `TeaVMTranspiler` that is included as part of the library.
This command line tool takes the following arguments:

| Name         | Required | Description                                            |
|--------------|----------|--------------------------------------------------------|
| -project     | yes      | Project name for the application.                      |
| -renderer    | yes      | One of 'canvas', 'webgl', 'three'.                     |
| -resources   | yes      | Directory containing the application's resource files. |
| -out         | yes      | Output directory for the generated files.              |
| -main        | yes      | Main class that acts as application entry point.       |
| -minify      | no       | Minifies the generated JavaScript, off by default.     |

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

| Name      | Required | Description                                   |
|-----------|----------|-----------------------------------------------|
| -input    | yes      | Directory containing source images.           |
| -outimage | yes      | Generated image file location.                |
| -outdata  | yes      | Generated metadata file location.             |
| -metadata | yes      | Metadata file format, either 'yaml' or 'csv'. |
| -size     | yes      | Width/height of the sprite sheet.             |
| -exclude  | no       | Excludes all images beyond a certain size.    |

This will generate the PNG file containing the sprite sheet graphics and the YAML file with
metadata in the specified locations. Sprite sheets can then be loaded back as media assets.

Creating an application icon
----------------------------

MultimediaLib includes a command line tool for creating ICNS icons that can be used for Mac and/or
iOS applications. The entry point for this tool is `nl.colorize.multimedialib.tool.AppleIconTool`.

Documentation
-------------

- [JavaDoc](http://api.clrz.nl/multimedialib/)

Build instructions
------------------

Building the library can be done on any platform. The following is mandatory for building the
library itself:

- [Java JDK](http://java.oracle.com) 11+
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
  
License
-------

Copyright 2009-2021 Colorize

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
