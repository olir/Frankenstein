/*
 * Copyright 2017 Oliver Rode, https://github.com/olir/Frankenstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.screenflow.frankenstein.vf.input;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import de.screenflow.frankenstein.ProcessingListener;
import de.screenflow.frankenstein.vf.VideoFilter;
import de.screenflow.frankenstein.vf.VideoSource;

public class SlideShowInput implements VideoFilter, VideoSource {

	private final File slidesDir;

	private Mat initFrame;
	private Mat newFrame = null;
	private Mat tmpFrame;

	private int smallWidth = 960;
	private int smallHeight = 1080;

	final boolean mode3D;

	final List<Slide> slides = new ArrayList<Slide>();

	int fps = 10;
	int fpSlide = 6;

	public SlideShowInput(String slidesDir) {
		this.slidesDir = new File(slidesDir);
		mode3D = true;
	}

	@Override
	public Mat configure(Mat sourceFrame) {
		tmpFrame = sourceFrame.clone();
		newFrame = sourceFrame.clone();
		if (mode3D)
			Imgproc.resize(sourceFrame, newFrame, new Size((double) (smallWidth << 1), (double) smallHeight));
		else
			Imgproc.resize(sourceFrame, newFrame, new Size((double) smallWidth, (double) smallHeight));
		return newFrame;
	}

	@Override
	public Mat process(Mat sourceFrame, int frameId) {
		int sid = (frameId - 1) / (fps * fpSlide);

		if (sid < slides.size()) {
			Slide s = slides.get(sid);
			File[] f = s.getFiles();

			Mat img = Imgcodecs.imread(f[0].getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_COLOR);
			img.convertTo(img, CvType.CV_8UC3);
			Imgproc.resize(img, tmpFrame, new Size((double) smallWidth, (double) smallHeight));
			Rect roi = new Rect(0, 0, smallWidth, smallHeight);
			tmpFrame.copyTo(new Mat(newFrame, roi));

			if (mode3D && f.length > 1) {
				img = Imgcodecs.imread(f[1].getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_COLOR);
				img.convertTo(img, CvType.CV_8UC3);
				Imgproc.resize(img, tmpFrame, new Size((double) smallWidth, (double) smallHeight));
			}
			roi = new Rect(smallWidth, 0, smallWidth, smallHeight);
			tmpFrame.copyTo(new Mat(newFrame, roi));
		}
		else
			newFrame.setTo(new Scalar(0,0,0,0));
		
		return newFrame;
	}

	public void setWidth(int smallWidth) {
		this.smallWidth = smallWidth;
	}

	public void setHeight(int smallHeight) {
		this.smallHeight = smallHeight;
	}

	@Override
	public int getFrames() {
		return slides.size() * fps * fpSlide;
	}

	@Override
	public double getFps() {
		return fps;
	}

	@Override
	public int getWidth() {
		return mode3D ? smallWidth << 1 : smallWidth;
	}

	@Override
	public int getHeight() {
		return smallHeight;
	}

	@Override
	public void open(ProcessingListener l) {
		initFrame = Mat.zeros(getHeight(), getWidth(), CvType.CV_8UC3);

		File[] files = slidesDir.listFiles();
		Arrays.sort(files);

		slides.clear();
		File prevFile = null;
		Slide prevSlide = null;
		for (File file : files) {
			if (acceptFile(file)) {
				if (prevSlide != null && prevFile != null && match(file, prevSlide.getFiles()[0], 0)) {
					File[] prevFiles = prevSlide.getFiles();
					if (prevFiles.length > 1)
						throw new Error("Ambigious File Filter");
					slides.remove(prevSlide);
					File[] newFiles = { prevFiles[0], file };
					Slide s = new Slide(newFiles);
					slides.add(s);
					prevFile = null;
					prevSlide = s;
				} else {
					File[] f = { file };
					Slide s = new Slide(f);
					slides.add(s);
					prevFile = file;
					prevSlide = s;
				}
			}
		}

		for (Slide s : slides) {
			System.out.println(s.toString());
		}

	}

	private boolean acceptFile(File file) {
		if (file.isDirectory())
			return false;

		String n = file.getName();
		int i = n.lastIndexOf('.');
		if (i < 1)
			return false;

		String s = n.substring(i + 1);
		if (s.equalsIgnoreCase("jpg"))
			return true;
		if (s.equalsIgnoreCase("png"))
			return true;
		if (s.equalsIgnoreCase("gif"))
			return true;

		return false;
	}

	private boolean match(File f1, File f2, int indexChars) {
		return match(nameWithoutSuffix(f1), nameWithoutSuffix(f2), indexChars);
	}

	private String nameWithoutSuffix(File f) {
		String n = f.getName();
		int p = n.lastIndexOf('.');
		if (p < 0)
			return n;
		else
			return n.substring(0, p);
	}

	private boolean match(String n1, String n2, int indexChars) {
		if (indexChars <= 0) {
			int i = Math.max(n1.lastIndexOf('-'), n1.lastIndexOf('_'));
			if (i > 0)
				indexChars = n1.length() - i - 1;
		}
		if (n1.length() != n2.length() && n1.length() > indexChars)
			return false;
		return (n1.substring(0, n1.length() - indexChars).equals(n2.substring(0, n2.length() - indexChars)));
	}

	@Override
	public void close() {
		initFrame = null;
	}

	@Override
	public void reopen(ProcessingListener l) {
		open(l);
		close();
	}

	@Override
	public Mat getFrame() {
		// newFrame.setTo(new Scalar(0, 0, 0, 0));
		return initFrame;
	}

	@Override
	public int seek(int pos, ProcessingListener l) {
		return pos;
	}

	class Slide {
		private final File[] files;

		public Slide(File[] files) {
			this.files = files;
		}

		public File[] getFiles() {
			return files;
		}

		@Override
		public String toString() {
			StringBuilder b = null;
			for (File f : files) {
				if (b == null) {
					b = new StringBuilder("Slide{");
				} else {
					b.append(',');
				}
				b.append(f.getName());
			}
			return b.append('}').toString();
		}
	}

}
