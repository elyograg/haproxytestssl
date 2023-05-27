# Blank Grade Java project

This is a blank gradle java project with a pre-created source code structure
that can be changed.  When eclipse creates a new gradle project, the
structure is odd.  Everything expected at the root is in a lib directory.
Created this as a quick starting point for new projects so no surgery is
required.

You should run `git grep -i changeme` and `find . | grep -i changeme` after
cloning this repository so you can see all the places that will most likely
need an adjustment for you to correctly create your own project.  You should
also delete the .git directory after looking over the output of the above
commands in anticipation of using this as the starting point for your own
project and source control system.  Importing the project works correctly
with eclipse and intellij.  Other IDEs were not attempted.

The project comes with the gradle wrapper, version 8.1.1.  All the things
that say `changeme` are very easy to change in a good IDE.

## Demo on a bash environment

- Clone the project.
- cd to the new directory.
- `./gradlew clean dist`
- `./changeme`

## Structure

- A complete project structure ready to be populated.
  - src/main/java/org/elyograg/changeme
    - Main.java
      - A mostly empty template class.
      - Sets up a picocli framework for handling commandline options.
    - StaticStuff.java
    -  A class to put static methods, constants, etc.
  - src/main/resources
    - A place to put resources.
      - Includes a logback.xml config file as a starting point.
      - One common thing found here is application.properties.
  - src/test/java/org/elyograg/changeme
    - MainTests.java
      - A mostly empty template class.
  - src/test/resources
    - A place to put resources for test code.
  - build.gradle
    - Uses mavenCentral for dependency downloads.
    - Configured to build for Java 11.
    - Includes picocli.
    - Uses slf4j as the logging framework.
    - Uses logback as the final logging destination.
    - Uses JUnit4 for a test framework.
    - Automatically gets new minor and point releases of dependencies.
    - Has a "dist" target that builds a jar with dependencies.  It specifies the class with a `main` method so it can be started with `java -jar filename.jar`.
  - settings.gradle
    - Just defines the project name.
  - `changeme`
    - A bash script that can handle `JAVA_HOME`, finds the jar, and starts it with all commandline options sent to script.
