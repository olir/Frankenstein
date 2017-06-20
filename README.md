# Frankenstein VR
Free Video Converter for Virtual Reality and 3D based on ffmpeg and OpenCV for Java.

## Summary for Users
Frankenstein is an experimental video converter with some video filters/features:
- Virtual Reality side-by-side converter (projection, padding, shrinking)
- Anaglyph (e.g. red/blue) to grayscale side-by-side converter
- Over/Under to Left/Right (side-by-side) converter
- Left/Right side swapper
- Test Image (good for calibrating configurations on a VR display)
Some filters can be chained together.
The output of Frankenstein VR can be perfectly viewed with video players like [LittlStar](http://littlstar.info).

Issues: Temporary Video Output is uncompressed and can get very hugh. You need to have at least 100GB disk space.

## Summary for Programmers
Frankenstein is a very small and easy to extend Java Tool with JavaFX frontend.
If you like to experiment with video filters based on OpenCV just fork it.
It is very fast because it is mostly build on existing libraries: OpenCV for graphic processing and ffmpeg for video/audio processing. The Graphical Interface (Java FX) is optional. It's a little bit tricky to get the toolchain working with acceptable quality. But now...


It's alive!
