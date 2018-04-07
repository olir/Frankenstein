package de.serviceflow.frankenstein.plugin.jogamp;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import com.jogamp.opengl.DefaultGLCapabilitiesChooser;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;
import com.jogamp.opengl.util.gl2.GLUT;

import de.serviceflow.frankenstein.plugin.api.DefaultSegmentFilter;
import de.serviceflow.frankenstein.plugin.api.FilterContext;
import de.serviceflow.frankenstein.plugin.api.DefaultSegmentConfigController;
import de.serviceflow.frankenstein.plugin.jogamp.jni.MatBlender;

public class GLExampleFilter extends DefaultSegmentFilter {

	private final MatBlender proxy;

	Mat glFrame;
	GLProfile glp;
	GLCapabilities caps;

	public GLExampleFilter() {
		super("glexample");

		proxy = new MatBlender();
		proxy.init();

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

	}

	@Override
	protected DefaultSegmentConfigController instantiateController() {
		return new GLExampleConfigController();
	}

	@Override
	public Mat process(Mat sourceFrame, int frameId, FilterContext context) {
		// System.out.println("c = "+sourceFrame.cols()+" r =
		// "+sourceFrame.rows());

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

		proxy.process(sourceFrame, frameId, context, glFrame);

		return sourceFrame;
	}

	@Override
	protected void initializeController() {
		// getConfigController(). ...
	}

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
		String text = "" + frameId;
		gl.glRasterPos3d(111, 111, 0);
		glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, text);

		BufferedImage im = new AWTGLReadBufferUtil(drawable.getGLProfile(), true)
				.readPixelsToBufferedImage(drawable.getGL(), 0, 0, width, height, true);

		return im;
	}

}