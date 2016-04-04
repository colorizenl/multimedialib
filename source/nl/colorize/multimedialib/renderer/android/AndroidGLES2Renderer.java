//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.android;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.view.MotionEvent;
import android.view.View;

import com.google.common.base.Charsets;

import nl.colorize.multimedialib.graphics.ColorRGB;
import nl.colorize.multimedialib.graphics.ImageData;
import nl.colorize.multimedialib.graphics.ImageRegion;
import nl.colorize.multimedialib.graphics.Shape2D;
import nl.colorize.multimedialib.graphics.Transform;
import nl.colorize.multimedialib.math.Rect;
import nl.colorize.multimedialib.math.Shape;
import nl.colorize.multimedialib.renderer.RendererException;
import nl.colorize.multimedialib.renderer.ScaleStrategy;
import nl.colorize.multimedialib.renderer.TimingUtils;
import nl.colorize.util.ResourceFile;
import nl.colorize.util.Stopwatch;

/**
 * Renders graphics using the OpenGL ES 2.0 API provided by Android. Apps can
 * only use this renderer if they request permission to use OpenGL ES 2.0 or
 * higher.
 */
public class AndroidGLES2Renderer extends AndroidRenderer implements GLSurfaceView.Renderer {
	
	private GLSurfaceView glSurfaceView;
	private int programId;
	
	private Stopwatch timer;
	private Map<Object, VertexData> vertexCache;
	
	private static final int OPENGL_ES_VERSION = 2;
	private static final ResourceFile VERTEX_SHADER_FILE = new ResourceFile("vertex_shader.glsl");
	private static final ResourceFile FRAGMENT_SHADER_FILE = new ResourceFile("fragment_shader.glsl");
	private static final int SQUARE_POLYGON_VERTEX_COUNT = 6;
	private static final ColorRGB TEXTURED_POLYGON_COLOR = ColorRGB.WHITE;
	private static final float TEXTURE_COORDINATES_NOT_SET = -1f;
	private static final Rect NO_TEXTURE_COORDS = new Rect(0, 0, 1, 1);

	public AndroidGLES2Renderer(Context context, ScaleStrategy scaleStrategy, int targetFramerate) {
		super(context, scaleStrategy, targetFramerate);
		
		timer = new Stopwatch();
		vertexCache = new HashMap<Object, VertexData>();
	}
	
	@Override
	protected void startRenderer() {
		super.startRenderer();
		
		glSurfaceView = new GLSurfaceView(context) {
			@Override
			public boolean onTouchEvent(MotionEvent e) {
				touchInput.registerTouchEvent(e);
				return true;
			}
		};
		glSurfaceView.setEGLContextClientVersion(OPENGL_ES_VERSION);
		glSurfaceView.setRenderer(this);
		glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
	}
	
	@Override
	protected void stopRenderer() {
		// GLSurfaceView and it's renderer don't have a callback that is
		// fired when the surface is destroyed, so onStopped() has to be
		// called from the activity lifecycle methods.
		onStopped();
		
		glSurfaceView = null;
		super.stopRenderer();
	}
	
	public void onSurfaceCreated(GL10 nullContext, EGLConfig config) {
		GLES20.glClearColor(backgroundColor.getR() / 255f, backgroundColor.getG() / 255f,
				backgroundColor.getB() / 255f, 1f);

		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		
		String vertexShaderCode = loadShaderCode(VERTEX_SHADER_FILE);
		int vertexShaderId = compileVertexShader(vertexShaderCode);
		
		String fragmentShaderCode = loadShaderCode(FRAGMENT_SHADER_FILE);
		int fragmentShaderId = compileFragmentShader(fragmentShaderCode);
		
		programId = createShaderProgram(vertexShaderId, fragmentShaderId);
		GLES20.glUseProgram(programId);
	}
	
	private String loadShaderCode(ResourceFile source) {
		try {
			return source.read(Charsets.UTF_8);
		} catch (IOException e) {
			throw new RendererException("Cannot load shader code", e);
		}
	}

	private int compileVertexShader(String glsl) {
		return compileShader(glsl, GLES20.GL_VERTEX_SHADER);
	}
	
	private int compileFragmentShader(String glsl) {
		return compileShader(glsl, GLES20.GL_FRAGMENT_SHADER);
	}
	
	private int compileShader(String glsl, int type) {
		int shaderId = GLES20.glCreateShader(type);
		if (shaderId == 0) {
			throw new OpenGLESException("Creating vertex shader failed");
		}
		
		GLES20.glShaderSource(shaderId, glsl);
		GLES20.glCompileShader(shaderId);
		
		int[] compileStatus = new int[1];
		GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
		if (compileStatus[0] == 0) {
			throw new OpenGLESException("Compiling shader failed: " + GLES20.glGetShaderInfoLog(shaderId));
		}
		
		return shaderId;
	}
	
	private int createShaderProgram(int vertexShaderId, int fragmentShaderId) {
		int programId = GLES20.glCreateProgram();
		if (programId == 0) {
			throw new OpenGLESException("Creating program failed");
		}
		
		GLES20.glAttachShader(programId, vertexShaderId);
		GLES20.glAttachShader(programId, fragmentShaderId);
		GLES20.glLinkProgram(programId);
		
		int[] linkStatus = new int[1];
		GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, linkStatus, 0);
		if (linkStatus[0] == 0) {
			throw new OpenGLESException("Linking program failed: " + GLES20.glGetProgramInfoLog(programId));
		}
		
		GLES20.glValidateProgram(programId);
		
		int[] validateStatus = new int[1];
		GLES20.glGetProgramiv(programId, GLES20.GL_VALIDATE_STATUS, validateStatus, 0);
		if (validateStatus[0] == 0) {
			throw new OpenGLESException("Validation failed: " + GLES20.glGetProgramInfoLog(programId));
		}
		
		return programId;
	}
	
	public void onSurfaceChanged(GL10 nullContext, int width, int height) {
		GLES20.glViewport(0, 0, width, height);
		getScreenBounds().set(0, 0, width, height);
		onInitialized();
	}

	public void onDrawFrame(GL10 nullContext) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		touchInput.processTouchEventBuffer();
		TimingUtils.syncFrame(getTargetFramerate(), timer, 0L, this, getStats());
	}

	public void drawImage(ImageData image, int x, int y, Transform transform) {
		VertexData vertexData = obtainVertexData(image);
		Rect area = new Rect(x - image.getWidth() / 2, y - image.getHeight() / 2, 
				image.getWidth(), image.getHeight());
		BitmapTexture texture = (BitmapTexture) image;
		Rect textureCoords = new Rect(0, 0, texture.getWidth(), texture.getHeight());
		updateVertexData(vertexData, area, TEXTURED_POLYGON_COLOR, transform.getAlpha(),
				texture, textureCoords);
		drawPolygon(vertexData, texture);
	}
	
	public void drawImageRegion(ImageRegion imageRegion, int x, int y, Transform transform) {
		VertexData vertexData = obtainVertexData(imageRegion);
		Rect region = imageRegion.getRegion();
		Rect area = new Rect(x - region.getWidth() / 2, y - region.getHeight() / 2,
				region.getWidth(), region.getHeight());
		BitmapTexture texture = (BitmapTexture) imageRegion.getImage();
		updateVertexData(vertexData, area, TEXTURED_POLYGON_COLOR, transform.getAlpha(),
				texture, region);
		drawPolygon(vertexData, texture);
	}
	
	public void drawShape(Shape2D shape) {
		VertexData vertexData = obtainVertexData(shape);
		updateVertexData(vertexData, shape);
		drawPolygon(vertexData, null);
	}
	
	private void drawPolygon(VertexData vertexData, BitmapTexture texture) {
		int positionLocation = GLES20.glGetAttribLocation(programId, "a_Position");
		int colorLocation = GLES20.glGetAttribLocation(programId, "a_Color");
		int textureCoordsLocation = GLES20.glGetAttribLocation(programId, "a_TextureCoordinates");
		FloatBuffer buffer = vertexData.getBuffer();
		
		if (texture != null) {
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture.getTextureId());
		}
		
		buffer.position(0);
		GLES20.glVertexAttribPointer(positionLocation, VertexData.POSITION_COMPONENTS, 
				GLES20.GL_FLOAT, false, VertexData.STRIDE, buffer);
		GLES20.glEnableVertexAttribArray(positionLocation);
		
		buffer.position(VertexData.POSITION_COMPONENTS);
		GLES20.glVertexAttribPointer(colorLocation, VertexData.COLOR_COMPONENTS, GLES20.GL_FLOAT,
				false, VertexData.STRIDE, buffer);
		GLES20.glEnableVertexAttribArray(colorLocation);
		
		buffer.position(VertexData.POSITION_COMPONENTS + VertexData.COLOR_COMPONENTS);
		GLES20.glVertexAttribPointer(textureCoordsLocation, VertexData.TEXTURE_COMPONENTS, 
				GLES20.GL_FLOAT, false, VertexData.STRIDE, buffer);
		GLES20.glEnableVertexAttribArray(textureCoordsLocation);
		
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexData.getVertexCount());
		
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		GLES20.glDisableVertexAttribArray(positionLocation);
		GLES20.glDisableVertexAttribArray(colorLocation);
		GLES20.glDisableVertexAttribArray(textureCoordsLocation);
	}
	
	private VertexData obtainVertexData(Object key) {
		VertexData vertexData = vertexCache.get(key);
		if (vertexData == null) {
			vertexData = new VertexData(SQUARE_POLYGON_VERTEX_COUNT);
			vertexCache.put(key, vertexData);
		}
		return vertexData;
	}
	
	private void updateVertexData(VertexData vertexData, Shape2D shape) {
		Shape currentShape = shape.getShapeCurrent();
		ColorRGB shapeColor = shape.getColor();
		Transform transform = shape.getTransform();
		if (currentShape instanceof Rect) {
			updateVertexData(vertexData, (Rect) currentShape, shapeColor, transform.getAlpha(), 
					null, NO_TEXTURE_COORDS);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	private void updateVertexData(VertexData vertexData, Rect area, ColorRGB color, int alpha,
			BitmapTexture texture, Rect textureCoords) {
		float r = color.getR() / 255f;
		float g = color.getG() / 255f;
		float b = color.getB() / 255f;
		float a = alpha / 100f;
		
		float leftS = getTextureS(texture, textureCoords.getX());
		float rightS = getTextureS(texture, textureCoords.getEndX());
		float centerS = leftS + (rightS - leftS) / 2f;
		float topT = getTextureT(texture, textureCoords.getY());
		float bottomT = getTextureT(texture, textureCoords.getEndY());
		float centerT = bottomT + (topT - bottomT) / 2f;
		
		// Vertex attributes: (position) x, y, (color) r, g, b, a, (texture coordinates) s, t
		float[] vertices = {
			area.getCenterX(), area.getCenterY(), r, g, b, a, centerS, centerT,
			area.getX(), area.getEndY(), r, g, b, a, leftS, bottomT,
			area.getEndX(), area.getEndY(), r, g, b, a, rightS, bottomT,
			area.getEndX(), area.getY(), r, g, b, a, rightS, topT,
			area.getX(), area.getY(), r, g, b, a, leftS, topT,
			area.getX(), area.getEndY(), r, g, b, a, leftS, bottomT
		};
		
		convertVertexCoordinates(vertices);
		vertexData.put(vertices);
	}
	
	/**
	 * Converts X and Y coordinates in an array of vertices from the canvas
	 * coordinate space (0, 0, width, height) to the OpenGL ES coordinate space
	 * (-1, 1, 1, -1).
	 */
	private void convertVertexCoordinates(float[] vertices) {
		for (int v = 0; v < vertices.length; v += VertexData.COMPONENTS_COUNT) {
			vertices[v] = (vertices[v] / getCanvasWidth() * 2f) - 1f;
			vertices[v + 1] = ((vertices[v + 1] / getCanvasHeight() * 2f) - 1f) * -1f;
		}
	}
	
	private float getTextureS(BitmapTexture texture, int x) {
		if (texture == null) {
			return TEXTURE_COORDINATES_NOT_SET;
		}
		return texture.getS(x);
	}
	
	private float getTextureT(BitmapTexture texture, int x) {
		if (texture == null) {
			return TEXTURE_COORDINATES_NOT_SET;
		}
		return texture.getT(x);
	}

	public ImageData loadImage(ResourceFile source) {
		Bitmap image = loadBitmap(source);
		BitmapTexture texture = new BitmapTexture(image);
		bindTexture(texture);
		//TODO image.recycle();
		return texture;
	}
	
	private void bindTexture(BitmapTexture texture) {
		int[] textureIds = new int[1];
		GLES20.glGenTextures(1, textureIds, 0);
		if (textureIds[0] == 0) {
			throw new OpenGLESException("Creating texture failed");
		}
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0]);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, texture.getImage(), 0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		texture.bind(textureIds[0]);
	}

	public View getView() {
		return glSurfaceView;
	}
}
