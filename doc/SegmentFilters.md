# Frankenstein VR - Segment Filters

## Available Filters

* Black and White - Converts to grayscale.
* Stereo Distance - Manipulates the stereo effect distance of side by side videos
** Distance slider: frame can appear farer or closer

## Howto create a new filter

## Overview
A filter is made of the following parts:
* controller class for the filter configuration editor - a fxml contoller
* property files with localized text
* fxml layout file for the filter configuration editor - sub layout for configuration of the filter instances
* filter class - contains the image manipulation.

The easiest way is to create the filter class is to extend de.serviceflow.frankenstein.vf.segment.DefaultSegmentFilter.
DefaultSegmentFilter expects all the classes and resources to be placed in the same package.
Look at de.serviceflow.frankenstein.vf.segment.BWFilter for an example. 

## Step 1: Create a controller class for the filter configuration editor

Start with an empty class. You can place fxml handling code there later:

__samplefilters/SampleConfigController.java:__
```java
package samplefilters;

public class SampleConfigController {

}
```


## Step 2: Create property files

The property files are required to store localized text of you Controller Configuration Editor.
The default property file is mandatory and should contain english text. It *must* contain a property 
__name__ mapped to the display name of the filter.

The file name is based on an unique identifier for you filter resources.
In this example i choose __sample__
Place it the proper resource package with the suffix .properties


__samplefilters/sample.properties:__
```java
name=Sample
message=No configuration options.
```

You may add localized versions of the file like this:

__samplefilters/sample_de.properties:__
```
name=Beispiel
message=Keine Einstellungenoptionen.
```


## Step 3: Create a new fxml layout file.

Notice: Eclipse JavaFX-Edition has a SceneBuilder.

Start by copying the following example and change the controller class reference behind fx:controller= to your class from step 1 (use full-qualified java class name).


__samplefilters/sample.fxml:__
```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.geometry.*?>
<?import javafx.scene.text.*?>
<?import javafx.scene.control.*?>
<?import java.lang.*?>
<?import javafx.scene.image.*?>
<?import javafx.scene.layout.*?>
<?import javafx.scene.layout.BorderPane?>

<BorderPane xmlns="http://javafx.com/javafx/8" xmlns:fx="http://javafx.com/fxml/1" fx:controller="samplefilters.SampleConfigController">
   <center>
      <Label text="%message" BorderPane.alignment="CENTER">
         <font>
            <Font name="System Italic" size="14.0" />
         </font></Label>
   </center>
</BorderPane>
```

In the example above the property __message__ from step 2 is used, by using the __%__ operator.


## Step 4: Create Filter Class

Now create the filter class:

__samplefilters/SampleFilter.java__:
```java
package samplefilters;

import org.opencv.core.Mat;
import de.serviceflow.frankenstein.vf.segment.DefaultSegmentFilter;

public class SampleFilter extends DefaultSegmentFilter<SampleConfigController> {

	public SampleFilter() {
		super("sample");
	}
	
	@Override
	public Mat process(Mat sourceFrame, int frameId) {
		// TODO: Your open OpenCV code here ...
		return sourceFrame;
	}

	@Override
	protected void initializeController() {
		// optional TODO ...
		
	}
}
```


## Step 5: Add filter to the list and use it

finally append list in FxMain.createSegmentFilters()

Now you can test the filter, by start Frankenstein VR and configure something, then on the progress scene
move slider to starting frame, click __M__, move slider to the end frame, add press __add__, select the freshly added segment, click __edit__,
select you filter from drop down, optionally configure it and press __ok__. Now you can move the slider inside the segment and the frame preview will show it.

## Advanced: Native Development

**Provide a gcc compiler (choose one):**
* windows 64-bit
 1. Download [mingw](https://sourceforge.net/projects/mingw-w64/) or [archive](https://sourceforge.net/p/mingw-w64/mailman/message/32967954/).
 2. Install with 'Architecture' option 'x86_64' when prompted at settings.
 3. Add ~YourMinGWPath/bin to PATH
* windows 32-bit
 1. [Instructions & Download](http://www.mingw.org/wiki/Getting_Started) or [archive](https://sourceforge.net/p/mingw-w64/mailman/message/32967954/)
 2. install gcc package with: mingw-get install gcc
 3. Add ~YourMinGWPath/bin to PATH

Build with:
```
cd jnilibrary
mvn clean package
mvn package
```
Comment: The package phase (for jniplugin-java) needs to be executed twice, because the build process is circular.

Test JNI loading within project directory:
```
java -Djava.library.path=jniplugin/native/win64/target -cp jniplugin/java/target/classes  cc0.JniTest
```

Some messages should appear: **Hello from C++!** ...

### Plattform Issues
*  java.lang.UnsatisfiedLinkError: "Can't load AMD 64-bit .dll on a IA 32-bit platform": In win32/pom.xml set the property gcc.customflags to -m32

