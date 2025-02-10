package code;

import javax.swing.*;
import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.common.nio.Buffers;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;
import com.jogamp.opengl.util.*;

//Imports for KeyListener to capture user's key inputs 
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.event.*;

public class Code extends JFrame implements GLEventListener, KeyListener
{	private GLCanvas myCanvas;
	private int renderingProgram;
	private int vao[] = new int[1];
	private float x = 0.0f;
	private float inc = 0.01f;

	public Code()
	{	setTitle("Chapter 2 - program 2");
		setSize(600, 400);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		this.add(myCanvas);
		this.setVisible(true);

		//Allows for Keylistener to capture user key input
		myCanvas.addGLEventListener(this);
		myCanvas.addKeyListener(this);

		Animator animtr = new Animator(myCanvas);
		animtr.start();
	}

	@Override
	public voicd keyPressed (KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_1:
			//add something when the 1 key is pressed
			break;
			case KeyEvent.VK_2:
			//add something when the 2 key is pressed
			break;
		}
	}

	public void display(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glUseProgram(renderingProgram);

		x += inc;
		if (x > 1.0f) inc = -0.01f;
		if (x < -1.0f) inc = 0.01f;
		int offsetLoc = gl.glGetUniformLocation(renderingProgram, "offset");
		gl.glProgramUniform1f(renderingProgram,offsetLoc,x);
		
				
		gl.glDrawArrays(GL_TRIANGLES,0,3);
	}

	public void init(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		renderingProgram = Utils.createShaderProgram("code/vertShader.glsl", "code/fragShader.glsl");
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
	}

	private int createShaderProgram()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();

		String vshaderSource[] = readShaderSource("code/vertShader.glsl");
		String fshaderSource[] = readShaderSource("code/fragShader.glsl");

		int vShader = gl.glCreateShader(GL_VERTEX_SHADER);
		int fShader = gl.glCreateShader(GL_FRAGMENT_SHADER);

		gl.glShaderSource(vShader, vshaderSource.length, vshaderSource, null, 0);
		gl.glCompileShader(vShader);

		gl.glShaderSource(fShader, fshaderSource.length, fshaderSource, null, 0);
		gl.glCompileShader(fShader);

		int vfprogram = gl.glCreateProgram();
		gl.glAttachShader(vfprogram, vShader);
		gl.glAttachShader(vfprogram, fShader);
		gl.glLinkProgram(vfprogram);

		gl.glDeleteShader(vShader);
		gl.glDeleteShader(fShader);
		return vfprogram;
	}

	public static void main(String[] args) { new Code(); }
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
	public void dispose(GLAutoDrawable drawable) {}

	private String[] readShaderSource(String filename)
	{	Vector<String> lines = new Vector<String>();
		String[] program;
		Scanner sc;
		try
		{	sc = new Scanner(new File(filename));
			while (sc.hasNext())
			{	lines.addElement(sc.nextLine());
			}
			program = new String[lines.size()];
			for (int i = 0; i < lines.size(); i++)
			{	program[i] = (String) lines.elementAt(i) + "\n";
			}
		}
		catch (IOException e)
		{	System.err.println("IOException reading file: " + e);
			return null;
		}
		return program;
	}

	private void printShaderLog(int shader)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] len = new int[1];
		int[] chWrittn = new int[1];
		byte[] log = null;

		// determine the length of the shader compilation log
		gl.glGetShaderiv(shader, GL_INFO_LOG_LENGTH, len, 0);
		if (len[0] > 0)
		{	log = new byte[len[0]];
			gl.glGetShaderInfoLog(shader, len[0], chWrittn, 0, log, 0);
			System.out.println("Shader Info Log: ");
			for (int i = 0; i < log.length; i++)
			{	System.out.print((char) log[i]);
			}
		}
	}

	void printProgramLog(int prog)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		int[] len = new int[1];
		int[] chWrittn = new int[1];
		byte[] log = null;

		// determine length of the program compilation log
		gl.glGetProgramiv(prog, GL_INFO_LOG_LENGTH, len, 0);
		if (len[0] > 0)
		{	log = new byte[len[0]];
			gl.glGetProgramInfoLog(prog, len[0], chWrittn, 0, log, 0);
			System.out.println("Program Info Log: ");
			for (int i = 0; i < log.length; i++)
			{	System.out.print((char) log[i]);
			}
		}
	}

	boolean checkOpenGLError()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		boolean foundError = false;
		GLU glu = new GLU();
		int glErr = gl.glGetError();
		while (glErr != GL_NO_ERROR)
		{	System.err.println("glError: " + glu.gluErrorString(glErr));
			foundError = true;
			glErr = gl.glGetError();
		}
		return foundError;
	}
}
