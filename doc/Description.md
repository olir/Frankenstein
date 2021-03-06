# Frankenstein VR
A Video Stream Analysis and Manipulation Framework for Java™ and C++, 
where custom filters can be simply added into the processing pipeline.

Plattforms: Windows 64.

The Tool supports 
* [OpenCV](http://www.opencv.org/releases.html) - computer vision and machine learning
* [JogAmp (OpenGL®, OpenCL™, OpenAL)](http://jogamp.org) - 3D Graphics, Multimedia and Processing
* [FFmpeg](https://ffmpeg.org) - record, convert and stream audio and video.
* [VLC](https://www.videolan.org/vlc/) - video stream recording

<img src="pipeline.png" width="100%">

## Screenshots
<img src="config.png" width="45%"> <img src="processing.png" width="45%" />


## Samples
I have uploaded some samples to vimeo: <a href="https://vimeo.com/user68089135"><img src="vimeo.png"/></a>

## Features and video filters in the main pipeline
- Virtual Reality side-by-side converter (projection, padding, shrinking)
- Anaglyph (e.g. red/blue) to grayscale side-by-side converter
- 3D Slideshow (SBS Video from 3D image pairs; see [samples](https://github.com/olir/Frankenstein/tree/master/doc/slides) )
- Over/Under to Left/Right (side-by-side) converter
- Left/Right side swapper
- Test Image (good for calibrating configurations on a VR display)

### 3D / VR Features
The pipeline allows input as video file, camera, network stream or pictures stored as left/right 3D slides (e.g. from nikon camera). Frames can be converted tp anaglyph or side-by-side 3D videos for VR display, and the output video can be perfectly viewed in 3D with VR Hardware and a video players like [LittlStar](http://littlstar.info). VR videos appear in this viewer like displayed on a virtual 160-inch curved 3D display in front of you.


# HOWTO run it
Install Pre-Requisites first (see below), then you have 3 options to start it:
* _Jar execution:_ 
 * Download and execute the jar file from the release (see section below) 
 * or build jar (maven and Java JDK required, setup JAVA_HOME) with 'mvn clean package' and execute app with 'java -jar app/target/frankenstein.jar'

To build the plugin:
* [install mingw64](https://superuser.com/questions/1294343/install-gcc-in-git-for-windows-bash-environment).
* go into plugin-opencv module and compile with 'mvn clean package'.

# HOWTO change it

I support eclipse. 

- First build it with maven as decribed above.
- In Eclipse go to File->Import / Maven / Existing Maven Projects and import it.
- Then run it as application  app -> src/main/java -> de.serviceflow.frankenstein.Main

## Pre-Requisites
- [FFmpeg 3.1.1+](https://ffmpeg.org) installed. Select path at first startup (is stored in frankenstein.ini at user-home)
- [Java JRE 1.8+](https://java.com) installed.
- [VLC 2.2.8](https://www.vlc.de/vlc_archiv.php) installed for network stream recording support.
- Codecs. See section below.

### FFMPEG OpenH264 support ###
FFMPEG build contains H264 encoder based on the OpenH264 library, that should be installed separatelly.
  OpenH264 Video Codec provided by Cisco Systems, Inc.
  See https://github.com/cisco/openh264/releases for details and OpenH264 license.
  Downloaded binary file can be placed into global system path (System32 or SysWOW64) or near application binaries (bin/).
  You can also specify location of binary file via OPENH264_LIBRARY_PATH environment variable.

## Your custom Video Filters ##
Frankstein supports loading external filters plugins either from network or local.

After first run .frankenstein-plugin.set contains the list of sample filters. 
Filter plugin jars are either specified by the directory location or an URL to the jar file.

When you work on custom filters, you can concentrate on manipulating images with the OpenCV or JogAmp libraries. 
For more details read [SegmentFilters](https://olir.github.io/Frankenstein/doc/SegmentFilters.html).
To run on [Debian Linux click here](https://olir.github.io/Frankenstein/doc/DebianLinux.html).

