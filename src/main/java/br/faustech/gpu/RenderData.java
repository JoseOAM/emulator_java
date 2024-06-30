package br.faustech.gpu;

import java.nio.FloatBuffer;
import java.util.Objects;
import org.lwjgl.opengl.GL46;

/**
 * Handles the setup, updating, and drawing of render data for OpenGL.
 */
public class RenderData {

  private final int width, height; // Dimensions for the texture

  private final int pboCount = 2; // Number of Pixel Buffer Objects for efficient texture streaming

  private int vao, vbo, textureId; // OpenGL identifiers for Vertex Array, Vertex Buffer, and Texture

  private int numVertices; // Number of vertices to be drawn

  private int[] pboIds; // Stores identifiers for Pixel Buffer Objects

  private int nextPboIndex = 0; // Index to track the next PBO to use for rendering

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
   * Initializes OpenGL objects including textures, buffers, and array objects.
   */
  public void setup() {
    // Activate texture unit 0
    GL46.glActiveTexture(GL46.GL_TEXTURE0);

    // Setup and bind the Vertex Array Object (VAO)
    vao = GL46.glGenVertexArrays();
    GL46.glBindVertexArray(vao);

    // Create and bind the Vertex Buffer Object (VBO)
    vbo = GL46.glGenBuffers();
    GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo);
    GL46.glBufferData(GL46.GL_ARRAY_BUFFER, 0, GL46.GL_DYNAMIC_DRAW); // Allocate an empty buffer

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
    GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MIN_FILTER,
        GL46.GL_LINEAR_MIPMAP_LINEAR);
    GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_LINEAR);
    GL46.glTexImage2D(GL46.GL_TEXTURE_2D, 0, GL46.GL_RGBA32F, width, height, 0, GL46.GL_RGBA,
        GL46.GL_FLOAT, (FloatBuffer) null);

    // Setup Pixel Buffer Objects (PBOs) for asynchronous data transfer
    pboIds = new int[pboCount];
    GL46.glGenBuffers(pboIds);
    for (int i = 0; i < pboCount; i++) {
      GL46.glBindBuffer(GL46.GL_PIXEL_UNPACK_BUFFER, pboIds[i]);
      GL46.glBufferData(GL46.GL_PIXEL_UNPACK_BUFFER, (long) width * height * 4 * Float.BYTES,
          GL46.GL_STREAM_DRAW);
    }
    GL46.glBindBuffer(GL46.GL_PIXEL_UNPACK_BUFFER, 0);
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
    GL46.glBufferData(GL46.GL_ARRAY_BUFFER, vertices, GL46.GL_DYNAMIC_DRAW);
    GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, 0);

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

    // Update texture from the PBO
    GL46.glBindTexture(GL46.GL_TEXTURE_2D, textureId);
    GL46.glTexSubImage2D(GL46.GL_TEXTURE_2D, 0, 0, 0, width, height, GL46.GL_RGBA, GL46.GL_FLOAT,
        0); // Use offset 0 in the PBO
    GL46.glGenerateMipmap(GL46.GL_TEXTURE_2D);
    GL46.glBindBuffer(GL46.GL_PIXEL_UNPACK_BUFFER, 0);
  }

  /**
   * Draws the vertices as points.
   */
  public void draw() {

    GL46.glBindTexture(GL46.GL_TEXTURE_2D, textureId);
    GL46.glBindVertexArray(vao);
    GL46.glDrawArrays(GL46.GL_POINTS, 0, numVertices);
    GL46.glBindVertexArray(0);
  }

  /**
   * Cleans up OpenGL resources.
   */
  public void cleanup() {

    GL46.glDeleteBuffers(vbo);
    GL46.glDeleteVertexArrays(vao);
    GL46.glDeleteTextures(textureId);
    GL46.glDeleteBuffers(pboIds);
  }

}
