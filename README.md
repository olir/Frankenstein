# Frankenstein VR
Free Video Converter for Virtual Reality and 3D based on ffmpeg and OpenCV for Java.
An open source platform for OpenCV-based video filters.

Frankenstein VR can e.g. be used to convert anaglyph or side-by-side 3D videos for VR display. 
The output can be perfectly viewed in 3D with video players like [LittlStar](http://littlstar.info). 
It appears there on a virtual 160-inch curved 3D display.

[![Build Status](https://travis-ci.org/olir/Frankenstein.png)](https://travis-ci.org/olir/Frankenstein/builds) [![Dependency Status](https://www.versioneye.com/user/projects/594a6802368b0800421af505/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/594a6802368b0800421af505)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.screenflow/frankenstein/badge.png)](https://maven-badges.herokuapp.com/maven-central/de.screenflow/frankenstein)

## Screenshots
<img src="doc/config.png" width="45%"> <img src="doc/processing.png" width="45%" />


## Samples
I have uploaded some samples to vimeo: <a href="https://vimeo.com/user68089135"><img src="doc/vimeo.png"/></a>

## Status
Release 0.1: Windows only (Developed/Tested on a Windows 10 64bit.)

Install Pre-Requisites first (see below), then you have 3 options to start it:
A) Jar execution: Download and execute the jar file from the release (see section below) 
B) Java Webstart: You can execute releases with Java Webstart (see section below) 
C) From Maven: You can also use maven to run it with "mvn clean package exec:exec"

## Issues:
- Video editing / local filters is under construction

# Pre-Requisites
- [FFmpeg 3.1.1+](https://ffmpeg.org) installed. Select path at first startup (is stored in frankenstein.ini at user-home)
- [Java JRE 1.8+](https://java.com) installed.
# Provided (do not install)
- [OpenCV 3.2.0+](http://www.opencv.org/releases.html)


## Summary for Users
Frankenstein VR is an experimental video converter with some video filters/features:
- Virtual Reality side-by-side converter (projection, padding, shrinking)
- Anaglyph (e.g. red/blue) to grayscale side-by-side converter
- 3D Slideshow (SBS Video from 3D image pairs; see [samples](doc/slides) )
- Over/Under to Left/Right (side-by-side) converter
- Left/Right side swapper
- Test Image (good for calibrating configurations on a VR display)
Some filters can be chained together.

## Summary for Programmers
If you like to experiment with video filters based on OpenCV just fork it.
It is a very small and easy to extend Java Tool with JavaFX frontend. 
Uncompressed temporary video makes it a little bit slow, but the rest of it is pretty fast because it is mostly build on existing libraries: OpenCV for graphic processing and ffmpeg for video/audio processing. The Graphical Interface (Java FX) is optional. It was a little bit tricky to get the toolchain working with acceptable quality. 


But now... It's alive!


### FFMPEG OpenH264 support ###
FFMPEG build contains H264 encoder based on the OpenH264 library, that should be installed separatelly.
  OpenH264 Video Codec provided by Cisco Systems, Inc.
  See https://github.com/cisco/openh264/releases for details and OpenH264 license.
  Downloaded binary file can be placed into global system path (System32 or SysWOW64) or near application binaries (bin/).
  You can also specify location of binary file via OPENH264_LIBRARY_PATH environment variable.

### Start via Java Webstart (Windows only)
(Under construction... need to fix some provileges to write files)
1. Install the pre-requistes above
- Go to Security tab within the Java Control Panel (Help: https://java.com/en/download/help/jcp_security.xml). 
  You have to add github as site exception: https://github.com/olir/Frankenstein/releases/download/0.1/
2. Download & Open jnlp file from release:
  - e.g.: [Pre-Release 0.1](https://github.com/olir/Frankenstein/releases/download/0.1/launch.jnlp)
3. Accept warnings and execute.



