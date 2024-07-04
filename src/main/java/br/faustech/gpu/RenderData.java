package br.faustech.gpu;

import java.nio.FloatBuffer;
import java.util.Objects;
import lombok.extern.java.Log;
import org.lwjgl.opengl.GL46;

/**
 * Handles the setup, updating, and drawing of render data for OpenGL.
 */
@Log
public class RenderData {

  private final int width, height; // Dimensions for the texture

  private final int pboCount = 2; // Number of Pixel Buffer Objects for efficient texture streaming

  private int vao, vbo, textureId; // OpenGL identifiers for Vertex Array, Vertex Buffer, and Texture

  private int numVertices; // Number of vertices to be drawn

  private int[] pboIds; // Stores identifiers for Pixel Buffer Objects

  private int nextPboIndex = 0; // Index to track the next PBO to use for rendering

  private ShaderProgram shaderProgram; // Shader program for rendering

  /**
   * Constructor for RenderData specifying dimensions for the rendering texture.
   *
   * @param width  Width of the texture.
   * @param height Height of the texture.
   */
  public RenderData(final int width, final int height) {

    this.width = width;
    this.height = height;
  }

  /**
   * Initializes OpenGL objects including textures, buffers, array objects, and shaders.
   */
  public void setup() {

    GL46.glEnable(GL46.GL_TEXTURE_2D);
    GL46.glPixelStorei(GL46.GL_UNPACK_ALIGNMENT, 1);

    // Initialize the shader program
    shaderProgram = new ShaderProgram();
    shaderProgram.loadShaders();

    // Setup and bind the Vertex Array Object (VAO)
    vao = GL46.glGenVertexArrays();
    GL46.glBindVertexArray(vao);

    // Create and bind the Vertex Buffer Object (VBO)
    vbo = GL46.glGenBuffers();
    GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo);
    GL46.glBufferData(GL46.GL_ARRAY_BUFFER, 0, GL46.GL_STREAM_DRAW); // Allocate an empty buffer

    // Configure vertex attributes for position, color, and texture coordinates
    int stride = 8 * Float.BYTES;
    GL46.glVertexAttribPointer(0, 2, GL46.GL_FLOAT, false, stride, 0);
    GL46.glEnableVertexAttribArray(0);
    GL46.glVertexAttribPointer(1, 4, GL46.GL_FLOAT, false, stride, 2 * Float.BYTES);
    GL46.glEnableVertexAttribArray(1);
    GL46.glVertexAttribPointer(2, 2, GL46.GL_FLOAT, false, stride, 6 * Float.BYTES);
    GL46.glEnableVertexAttribArray(2);

    // Unbind the buffer and array
    GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, 0);
    GL46.glBindVertexArray(0);

    // Create and configure the texture
    textureId = GL46.glGenTextures();
    GL46.glBindTexture(GL46.GL_TEXTURE_2D, textureId);

    // Set texture parameters for wrapping
    GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_S, GL46.GL_REPEAT);
    GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_WRAP_T, GL46.GL_REPEAT);

    // Set texture parameters for filtering
    GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MIN_FILTER,
        GL46.GL_NEAREST_MIPMAP_NEAREST);
    GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_NEAREST);

    // Initialize the texture with empty data
    GL46.glTexImage2D(GL46.GL_TEXTURE_2D, 0, GL46.GL_RGBA, width, height, 0, GL46.GL_RGBA,
        GL46.GL_FLOAT, 0);

    // Generate mipmaps
    GL46.glGenerateMipmap(GL46.GL_TEXTURE_2D);

    // Setup Pixel Buffer Objects (PBOs) for asynchronous data transfer
    pboIds = new int[pboCount];
    GL46.glGenBuffers(pboIds);
    for (int i = 0; i < pboCount; i++) {
      GL46.glBindBuffer(GL46.GL_PIXEL_UNPACK_BUFFER, pboIds[i]);
      GL46.glBufferData(GL46.GL_PIXEL_UNPACK_BUFFER, (long) width * height * 4 * Float.BYTES,
          GL46.GL_STREAM_DRAW);
    }
  }

  /**
   * Updates the vertex data and texture.
   *
   * @param vertices Array of vertices including position, color, and texture coordinates.
   */
  public void update(float[] vertices) {

    numVertices = vertices.length / 8; // Calculate the number of vertices

    // Bind and update the Vertex Buffer Object with new data
    GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo);
    GL46.glBufferData(GL46.GL_ARRAY_BUFFER, vertices, GL46.GL_STREAM_DRAW);

    // Use Pixel Buffer Objects for updating the texture
    int pboId = pboIds[nextPboIndex];
    nextPboIndex = (nextPboIndex + 1) % pboCount; // Cycle through PBOs

    GL46.glBindBuffer(GL46.GL_PIXEL_UNPACK_BUFFER, pboId);
    GL46.glBufferData(GL46.GL_PIXEL_UNPACK_BUFFER, (long) width * height * 4 * Float.BYTES,
        GL46.GL_STREAM_DRAW);
    FloatBuffer pboBuffer = Objects.requireNonNull(
        GL46.glMapBuffer(GL46.GL_PIXEL_UNPACK_BUFFER, GL46.GL_WRITE_ONLY)).asFloatBuffer();
    for (int i = 0; i < vertices.length; i += 8) {
      pboBuffer.put(vertices, i + 2, 4); // Extract RGBA values and put in the PBO buffer
    }
    GL46.glUnmapBuffer(GL46.GL_PIXEL_UNPACK_BUFFER);

    GL46.glBindTexture(GL46.GL_TEXTURE_2D, textureId);

    // Update texture from the PBO
    GL46.glTexSubImage2D(GL46.GL_TEXTURE_2D, 0, 0, 0, width, height, GL46.GL_RGBA, GL46.GL_FLOAT,
        0); // Use offset 0 in the PBO

    GL46.glGenerateMipmap(GL46.GL_TEXTURE_2D);

    int error = GL46.glGetError();
    if (error != GL46.GL_NO_ERROR) {
      log.severe("OpenGL error: " + error);
    }
  }

  /**
   * Draws the vertices as polygons.
   */
  public void draw() {

    shaderProgram.use(); // Use the shader program
    GL46.glBindVertexArray(vao);
    GL46.glPointSize(4.0f);
    GL46.glDrawArrays(GL46.GL_PATCHES, 0, numVertices);

  }

  /**
   * Cleans up OpenGL resources.
   */
  public void cleanup() {

    GL46.glDeleteBuffers(vbo);
    GL46.glDeleteVertexArrays(vao);
    GL46.glDeleteTextures(textureId);
    GL46.glDeleteBuffers(pboIds);
    shaderProgram.cleanup(); // Clean up the shader program
  }

}
