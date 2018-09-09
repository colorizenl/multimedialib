MultimediaLib
=============

Framework for building multimedia applications that support both desktop (Windows, macOS, Linux)
and mobile (iOS, Android). 

Usage
-----

The library is available from the Maven Central repository. To use it in a Maven project, add it 
to the dependencies section in `pom.xml`:

    <dependency>
        <groupId>nl.colorize</groupId>
        <artifactId>multimedialib</artifactId>
        <version>2018.2.1</version>
    </dependency>  
    
The library can also be used in Gradle projects:

    dependencies {
        compile 'nl.colorize:multimedialib:2018.2.1'
    }

Build instructions
------------------

The build is cross-platform and supports Windows, macOS, and Linux, but requires the following 
software to be available:

  - [Java JDK](http://java.oracle.com)
  - [Gradle](http://gradle.org)
  - [Android SDK](https://developer.android.com/sdk/index.html)

Building the Android renderer also required the Android SDK to be
installed, with the environment variable `ANDROID_PLATFORM_HOME` pointing to the targeted Android
platform version (e.g. `~/Developer/android/platforms/android-15`). 

The following Gradle build tasks are available:

  - `gradle clean` cleans the build directory
  - `gradle assemble` creates the JAR file for distribution
  - `gradle test` runs all unit tests, then reports on test results and test coverage
  - `gradle javadoc` generates the JavaDoc API documentation

License
-------

Copyright 2011-2018 Colorize

The source code is licensed under the Apache License 2.0, meaning you can use it free of charge 
in commercial and non-commercial projects as long as you mention the original copyright.
The full license text can be found at 
[http://www.colorize.nl/code_license.txt](http://www.colorize.nl/code_license.txt).

By using the source code you agree to the Colorize terms and conditions, which are available 
from the Colorize website at [http://www.colorize.nl/en/](http://www.colorize.nl/en/).
