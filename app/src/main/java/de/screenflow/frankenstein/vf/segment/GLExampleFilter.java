package de.screenflow.frankenstein.vf.segment;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.jogamp.opengl.DefaultGLCapabilitiesChooser;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;

import de.screenflow.frankenstein.vf.FilterContext;

public class GLExampleFilter  extends DefaultSegmentFilter {

	Mat glFrame;
	GLProfile glp;
	GLCapabilities caps;
	
	public GLExampleFilter() {
		super("glexample");
		
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
	public Mat process(Mat sourceFrame, int frameId, FilterContext context) {
		System.out.println("c = "+sourceFrame.cols()+"  r = "+sourceFrame.rows());
		if (glFrame == null || glFrame.cols() != sourceFrame.cols() || glFrame.rows() != sourceFrame.rows()) {
			glFrame = sourceFrame.clone();
		}

		GLAutoDrawable drawable = init(glFrame.cols(), glFrame.rows());
		BufferedImage bufImg = render(drawable, glFrame.cols(), glFrame.rows());

		BufferedImage image = new BufferedImage(bufImg.getWidth(), bufImg.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		image.getGraphics().drawImage(bufImg, 0, 0, null);
		glFrame = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC4);
		byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		glFrame.put(0, 0, data);
		
		return sourceFrame;

		/*
		try {
			return BufferedImage2Mat(image);
		}
		catch (IOException e) {
			e.printStackTrace();
			return sourceFrame;
		}
		*/		
	}

	public static Mat BufferedImage2Mat(BufferedImage image) throws IOException {
	    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	    ImageIO.write(image, "png", byteArrayOutputStream);
	    byteArrayOutputStream.flush();
	    return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
	}
	
	public static BufferedImage Mat2BufferedImage(Mat matrix)throws IOException {
	    MatOfByte mob=new MatOfByte();
	    Imgcodecs.imencode(".png", matrix, mob);
	    return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
	}	
	@Override
	protected void initializeController() {
		// getConfigController(). ...
	}
	
	static int numPoints = 100;
	static Random r = new Random();

	public GLAutoDrawable init(int width, int height)  {
		GLDrawableFactory factory = GLDrawableFactory.getFactory(glp);

		GLAutoDrawable drawable = factory.createOffscreenAutoDrawable(factory.getDefaultDevice(), caps,
				new DefaultGLCapabilitiesChooser(), width, height);
		drawable.display();
		drawable.getContext().makeCurrent();
		return drawable;
	}

	private BufferedImage render(GLAutoDrawable drawable, int width, int height)  {

		List<Float> data = new ArrayList<Float>(numPoints * 2);

		// simulate some data here
		for (int i = 0; i < numPoints; i++) {
			float x = r.nextInt(width);
			float y = r.nextInt(height);
			data.add(x);
			data.add(y);
		}

		// x and y for each point, 4 bytes for each
		FloatBuffer buffer = ByteBuffer.allocateDirect(numPoints * 2 * 4).order(ByteOrder.nativeOrder())
				.asFloatBuffer();
		for (Float d : data) {
			buffer.put(d);
		}
		buffer.rewind();

		GL2 gl = drawable.getGL().getGL2();

		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glViewport(0, 0, width, height);

		gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
		gl.glLoadIdentity();

		gl.glOrtho(0d, width, height, 0d, -1d, 1d);
		gl.glPointSize(4f);
		gl.glColor3f(1f, 0f, 0f);

		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glVertexPointer(2, GL2.GL_FLOAT, 0, buffer);
		gl.glDrawArrays(GL2.GL_POINTS, 0, numPoints);
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);

		BufferedImage im = new AWTGLReadBufferUtil(drawable.getGLProfile(), true)
				.readPixelsToBufferedImage(drawable.getGL(), 0, 0, width, height, true);
		
		return im;
	}
}