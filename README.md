# Frankenstein
3D Video Processor and Converter for Virtual Reality.

## Summary for Users
Frankenstein is a video converter with the following video filters/features:
- Virtual Reality side-by-side converter (padding and shrinking)
- Anaglyph (e.g. red/blue) to grayscale side-by-side converter
- Over/Under to Left/Right (side-by-side) converter
- Left/Right side swapper
- Test Image (good for calibrating videos on a VR display)
Some filters can be chained together.

It's is not designed for movies on dvd/blueray. There is no need: Your VR plattform should have a movie player capable of playing 3D Movies over VR.

The output of Frankenstein can be perfectly viewed with video players like [LittlStar](http://littlstar.info).


## Summary for Programmers
Frankenstein is a very small and easy to extend Java Tool.
It is very fast because it is mostly build on existing libraries: OpenCV for graphic processing and ffmpeg for video/audio processing. The Graphical Interface (Java FX) is optional. It's a little bit tricky to get the toolchain working with acceptable quality. But now...

It's alive!
