MultimediaLib
=============

Library that provides a framework for multimedia applications. Applications built using this
framework can run on multiple platforms, both as desktop applications on Windows/OS X/Linux
and as mobile applications on Android. To achieve this, the framework includes a number of
renderer implementations that enable graphics/audio/input on different platforms.

Build
-----

Building the library requires the [Java JDK](http://java.oracle.com) and
[Gradle](http://gradle.org). Building the Android renderer also required the Android SDK to be
installed, with the environment variable `ANDROID_PLATFORM_HOME` pointing to the targeted Android
platform version (e.g. `~/Developer/android/platforms/android-15`). 

The source code has dependencies on the Colorize project *Colorize Java Commons*, which needs to 
be available in the location specified in `settings.gradle`. 

The following Gradle build tasks are available:

- `gradle clean` cleans the build directory
- `gradle assemble` creates the JAR file for distribution
- `gradle test` runs all the unit tests
- `gradle cobertura` runs all unit tests, calculates the test coverage, and generates a report
- `gradle javadoc` generates the JavaDoc API documentation

License
-------

Copyright 2011-2016 Colorize

The source code is licensed under the Apache License 2.0, meaning you can use it free of charge 
in commercial and non-commercial projects as long as you mention the original copyright.
The full license text can be found at 
[http://www.colorize.nl/code_license.txt](http://www.colorize.nl/code_license.txt).

By using the source code you agree to the Colorize terms and conditions, which are available 
from the Colorize website at [http://www.colorize.nl/en/](http://www.colorize.nl/en/).
