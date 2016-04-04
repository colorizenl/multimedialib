//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.renderer.android;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Contains coordinate, color, and texture data for a number of vertices. The
 * data is stored in a NIO buffer so that it is accessible to native code.
 */
public class VertexData {
	
	private int vertexCount;
	private FloatBuffer buffer;

	public static final int POSITION_COMPONENTS = 2;
	public static final int COLOR_COMPONENTS = 4;
	public static final int TEXTURE_COMPONENTS = 2;
	public static final int COMPONENTS_COUNT = POSITION_COMPONENTS + COLOR_COMPONENTS + TEXTURE_COMPONENTS;
	public static final int BYTES_PER_FLOAT = 4;
	public static final int STRIDE = COMPONENTS_COUNT * BYTES_PER_FLOAT;
	
	public VertexData(int vertexCount) {
		this.vertexCount = vertexCount;
		
		buffer = ByteBuffer.allocateDirect(vertexCount * COMPONENTS_COUNT * BYTES_PER_FLOAT)
				.order(ByteOrder.nativeOrder())
				.asFloatBuffer();
	}
	
	public void put(float[] vertices) {
		if (vertices.length != vertexCount * COMPONENTS_COUNT) {
			throw new IllegalArgumentException("Invalid number of vertices");
		}
		
		buffer.clear();
		buffer.put(vertices, 0, vertices.length);
		buffer.flip();
	}
	
	public int getVertexCount() {
		return vertexCount;
	}
	
	public FloatBuffer getBuffer() {
		return buffer;
	}
}
