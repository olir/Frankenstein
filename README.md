# Frankenstein VR
Free Video Converter for Virtual Reality and 3D based on ffmpeg and OpenCV for Java.

[![Build Status](https://travis-ci.org/olir/Frankenstein.png)](https://travis-ci.org/olir/Frankenstein/builds) [![Dependency Status](https://www.versioneye.com/user/projects/594a6802368b0800421af505/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/594a6802368b0800421af505)

# Status
No release yet. 

You can use maven to run it with "mvn clean package exec:exec"

Issues:
- Currently video input is buggy. I'm working on it.
- The path to ffmpeg (parent of bin) is still hardcoded in de.screenflow.frankenstein.Configuration . Good luck ;-)
- Temporary Video Output is uncompressed and can get very hugh. You need to have at least 100GB disk space. Developed on a Windows 10 64bit.

# Pre-Requisites
- [FFmpeg 3.1.1+](https://java.com) installed.
- [Java JRE 1.8+](https://ffmpeg.org) installed.
# Provided (do not install)
- [OpenCV 3.2.0+](http://www.opencv.org/releases.html)


## Summary for Users
Frankenstein VR is an experimental video converter with some video filters/features:
- Virtual Reality side-by-side converter (projection, padding, shrinking)
- Anaglyph (e.g. red/blue) to grayscale side-by-side converter
- Over/Under to Left/Right (side-by-side) converter
- Left/Right side swapper
- Test Image (good for calibrating configurations on a VR display)
Some filters can be chained together.
The output of Frankenstein VR can be perfectly viewed with video players like [LittlStar](http://littlstar.info).


## Summary for Programmers
If you like to experiment with video filters based on OpenCV just fork it.
It is a very small and easy to extend Java Tool with JavaFX frontend. 
Uncompressed temporary video makes it a little bit slow, but the rest of it is pretty fast because it is mostly build on existing libraries: OpenCV for graphic processing and ffmpeg for video/audio processing. The Graphical Interface (Java FX) is optional. It was a little bit tricky to get the toolchain working with acceptable quality. 


But now... It's alive!
