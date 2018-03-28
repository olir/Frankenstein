package de.serviceflow.frankenstein.vf.segment;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import de.serviceflow.frankenstein.plugin.api.FilterContext;
import de.serviceflow.frankenstein.plugin.api.NativeSegmentFilter;

public class GLExampleFilter extends NativeSegmentFilter<GLExampleConfigController> {

	private final static String JNI_FILTER_CLASS = "de.serviceflow.frankenstein.vf.jni.MatBlender";

	private final Method jniProxyProcessMethod;

  /*
	Mat glFrame;
	GLProfile glp;
	GLCapabilities caps;
*/

	@SuppressWarnings("unchecked")
	public GLExampleFilter() {
		super("glexample", JNI_FILTER_CLASS);
		try {
			jniProxyProcessMethod = getJniProxyClass().getMethod("process", Object.class, int.class, Object.class,
					Object.class);
		} catch (NoSuchMethodException | SecurityException | IllegalArgumentException e) {
			throw new RuntimeException("jni wrapper creation failed", e);
		}
/*
		glp = GLProfile.getDefault();
		caps = new GLCapabilities(glp);
		caps.setHardwareAccelerated(true);
		caps.setDoubleBuffered(false);
		caps.setAlphaBits(8);
		caps.setRedBits(8);
		caps.setBlueBits(8);
		caps.setGreenBits(8);
		caps.setDepthBits(24);
		caps.setOnscreen(false);
    */
	}

	@Override
	public Mat process(Mat sourceFrame, int frameId, FilterContext context) {
		// System.out.println("c = "+sourceFrame.cols()+" r =
		// "+sourceFrame.rows());
    
    /*
		if (glFrame == null || glFrame.cols() != sourceFrame.cols() || glFrame.rows() != sourceFrame.rows()) {
			glFrame = sourceFrame.clone();
		}

		GLAutoDrawable drawable = init(glFrame.cols(), glFrame.rows());
		BufferedImage bufImg = render(drawable, glFrame.cols(), glFrame.rows(), frameId);

		BufferedImage image = new BufferedImage(bufImg.getWidth(), bufImg.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		image.getGraphics().drawImage(bufImg, 0, 0, null);
		glFrame = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC4);
		byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		glFrame.put(0, 0, data);

		try {
			jniProxyProcessMethod.invoke(getJniProxy(), sourceFrame, frameId, context, glFrame);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
  */
		return sourceFrame;
	}

	@Override
	protected void initializeController() {
		// getConfigController(). ...
	}

  /*
	public GLAutoDrawable init(int width, int height) {
		GLDrawableFactory factory = GLDrawableFactory.getFactory(glp);

		GLAutoDrawable drawable = factory.createOffscreenAutoDrawable(factory.getDefaultDevice(), caps,
				new DefaultGLCapabilitiesChooser(), width, height);
		drawable.display();
		drawable.getContext().makeCurrent();
		return drawable;
	}

	private BufferedImage render(GLAutoDrawable drawable, int width, int height, int frameId) {

		GL2 gl = drawable.getGL().getGL2();

		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glViewport(0, 0, width, height);

		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
		gl.glLoadIdentity();

		gl.glOrtho(0d, width, height, 0d, -1d, 1d);
		gl.glPointSize(4f);
		gl.glColor3f(0.8f, 0.8f, 0.8f);

		GLUT glut = new GLUT();
		String text = ""+frameId;
		gl.glRasterPos3d(111, 111, 0);
		glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, text);

		BufferedImage im = new AWTGLReadBufferUtil(drawable.getGLProfile(), true)
				.readPixelsToBufferedImage(drawable.getGL(), 0, 0, width, height, true);

		return im;
	}
  */
}