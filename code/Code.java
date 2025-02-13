package code;

import javax.swing.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

public class Code extends JFrame implements GLEventListener, KeyListener
{	
	private GLCanvas myCanvas;
	private int renderingProgram;
	private int vao[] = new int[1];
	private int vbo[] = new int[1];
	private float triangleSize = 0.5f; // Initial size of the triangle
	private float sizeIncrement = 0.05f; // Amount to increase/decrease size
	private int colorMode = 0; // 0 = Yellow, 1 = Purple, 2 = Gradient
	private float offsetX = 0.0f; // Horizontal offset for animation
	private float offsetY = 0.0f; // Vertical offset for circular motion
	private float angle = 0.0f; // Angle for circular motion
	private boolean circularMotion = false; // Toggle for circular motion
	private int rotationState = 0; // 0 = Up, 1 = Down, 2 = Left, 3 = Right
	private long previousTime = System.nanoTime(); // Time tracking for smooth animation
	private float direction = 0.5f;

	public Code()
	{	
		setTitle("Assignment 1");
		setSize(600, 400);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		myCanvas.addKeyListener(this); // Add key listener to the canvas
		this.add(myCanvas);
		this.setVisible(true);
		Animator animtr = new Animator(myCanvas);
		animtr.start();
	}

	public void display(GLAutoDrawable drawable)
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glUseProgram(renderingProgram);

		// Calculate elapsed time
		long currentTime = System.nanoTime();
		float elapsedTime = (currentTime - previousTime) / 1_000_000_000.0f; // Convert to seconds
		previousTime = currentTime;

		// Update movement based on elapsed time
		if (circularMotion) {
			// Circular motion using unit circle equations
			angle += elapsedTime; // Increment angle based on time
			offsetX = (float) Math.cos(angle);
			offsetY = (float) Math.sin(angle);
		} else {
			// Horizontal motion
			offsetX += elapsedTime * direction; // Adjust speed as needed
			if (offsetX > 1.0f || offsetX < -1.0f) {
				direction = -direction; // Reverse direction at edges
			}
		}

		// Pass the offsets to the shader
		int offsetXLoc = gl.glGetUniformLocation(renderingProgram, "offsetX");
		int offsetYLoc = gl.glGetUniformLocation(renderingProgram, "offsetY");
		gl.glProgramUniform1f(renderingProgram, offsetXLoc, offsetX);
		gl.glProgramUniform1f(renderingProgram, offsetYLoc, offsetY);

		// Pass the size to the shader
		int sizeLoc = gl.glGetUniformLocation(renderingProgram, "size");
		gl.glProgramUniform1f(renderingProgram, sizeLoc, triangleSize);

		// Pass the color mode to the shader
		int colorModeLoc = gl.glGetUniformLocation(renderingProgram, "colorMode");
		gl.glProgramUniform1i(renderingProgram, colorModeLoc, colorMode);

		// Pass the rotation state to the shader
		int rotationLoc = gl.glGetUniformLocation(renderingProgram, "rotationState");
		gl.glProgramUniform1i(renderingProgram, rotationLoc, rotationState);

		gl.glDrawArrays(GL_TRIANGLES, 0, 3);
	}

	public void init(GLAutoDrawable drawable)
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
		renderingProgram = Utils.createShaderProgram("code/vertShader.glsl", "code/fragShader.glsl");
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);

		// Set up vertex data for the triangle (positions and colors)
		float[] vertices = {
			// Positions         // Colors (for gradient mode)
			-0.5f, -0.5f, 0.0f,  1.0f, 0.0f, 0.0f, // Red
			0.5f, -0.5f, 0.0f,   0.0f, 1.0f, 0.0f, // Green
			0.0f, 0.5f, 0.0f,    0.0f, 0.0f, 1.0f  // Blue
		};

		gl.glGenBuffers(vbo.length, vbo, 0);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glBufferData(GL_ARRAY_BUFFER, vertices.length * Float.BYTES, Buffers.newDirectFloatBuffer(vertices), GL_STATIC_DRAW);

		// Set up vertex attribute pointers
		int positionLoc = gl.glGetAttribLocation(renderingProgram, "vPosition");
		gl.glVertexAttribPointer(positionLoc, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
		gl.glEnableVertexAttribArray(positionLoc);

		int colorLoc = gl.glGetAttribLocation(renderingProgram, "vColor");
		gl.glVertexAttribPointer(colorLoc, 3, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
		gl.glEnableVertexAttribArray(colorLoc);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_1) {
			circularMotion = !circularMotion; // Toggle circular motion
		} else if (keyCode == KeyEvent.VK_2) {
			colorMode = (colorMode + 1) % 3; // Toggle color mode
		} else if (keyCode == KeyEvent.VK_3) {
			triangleSize += sizeIncrement; // Increase size
		} else if (keyCode == KeyEvent.VK_4) {
			triangleSize -= sizeIncrement; // Decrease size
		} else if (keyCode == KeyEvent.VK_5) {
			rotationState = (rotationState + 1) % 4; // Cycle rotation states
		}
		myCanvas.display(); // Trigger redraw
	}

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void keyTyped(KeyEvent e) {}

	public static void main(String[] args) { new Code(); }
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
	public void dispose(GLAutoDrawable drawable) {}

	private String[] readShaderSource(String filename)
	{	
		Vector<String> lines = new Vector<String>();
		String[] program;
		Scanner sc;
		try
		{	
			sc = new Scanner(new File(filename));
			while (sc.hasNext())
			{	
				lines.addElement(sc.nextLine());
			}
			program = new String[lines.size()];
			for (int i = 0; i < lines.size(); i++)
			{	
				program[i] = (String) lines.elementAt(i) + "\n";
			}
		}
		catch (IOException e)
		{	
			System.err.println("IOException reading file: " + e);
			return null;
		}
		return program;
	}

	private void printShaderLog(int shader)
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] len = new int[1];
		int[] chWrittn = new int[1];
		byte[] log = null;

		// determine the length of the shader compilation log
		gl.glGetShaderiv(shader, GL_INFO_LOG_LENGTH, len, 0);
		if (len[0] > 0)
		{	
			log = new byte[len[0]];
			gl.glGetShaderInfoLog(shader, len[0], chWrittn, 0, log, 0);
			System.out.println("Shader Info Log: ");
			for (int i = 0; i < log.length; i++)
			{	
				System.out.print((char) log[i]);
			}
		}
	}

	void printProgramLog(int prog)
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] len = new int[1];
		int[] chWrittn = new int[1];
		byte[] log = null;

		// determine length of the program compilation log
		gl.glGetProgramiv(prog, GL_INFO_LOG_LENGTH, len, 0);
		if (len[0] > 0)
		{	
			log = new byte[len[0]];
			gl.glGetProgramInfoLog(prog, len[0], chWrittn, 0, log, 0);
			System.out.println("Program Info Log: ");
			for (int i = 0; i < log.length; i++)
			{	
				System.out.print((char) log[i]);
			}
		}
	}

	boolean checkOpenGLError()
	{	
		GL4 gl = (GL4) GLContext.getCurrentGL();
		boolean foundError = false;
		GLU glu = new GLU();
		int glErr = gl.glGetError();
		while (glErr != GL_NO_ERROR)
		{	
			System.err.println("glError: " + glu.gluErrorString(glErr));
			foundError = true;
			glErr = gl.glGetError();
		}
		return foundError;
	}
}
