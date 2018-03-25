package de.screenflow.frankenstein.vf.segment;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.jogamp.opengl.DefaultGLCapabilitiesChooser;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;

public class GLExample {
	static int width = 500;
	static int height = 500;
	static int numPoints = 100;
	static Random r = new Random();

	public static void main(String[] args) throws Exception {
		GLProfile glp = GLProfile.getDefault();
		GLCapabilities caps = new GLCapabilities(glp);
		caps.setHardwareAccelerated(true);
		caps.setDoubleBuffered(false);
		caps.setAlphaBits(8);
		caps.setRedBits(8);
		caps.setBlueBits(8);
		caps.setGreenBits(8);
		caps.setOnscreen(false);
		GLDrawableFactory factory = GLDrawableFactory.getFactory(glp);

		GLAutoDrawable drawable = factory.createOffscreenAutoDrawable(factory.getDefaultDevice(), caps,
				new DefaultGLCapabilitiesChooser(), width, height);
		drawable.display();
		drawable.getContext().makeCurrent();
		new GLExample().render(drawable);

	}

	private void render(GLAutoDrawable drawable) throws Exception {

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

	}
}