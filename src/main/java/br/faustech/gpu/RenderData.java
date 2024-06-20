package br.faustech.gpu;

import java.nio.FloatBuffer;
import java.util.Objects;
import org.lwjgl.opengl.GL46;

public class RenderData {

  private final int width, height;  // Texture width and height

  private final int pboCount = 2; // Number of Pixel Buffer Objects

  private int vao, vbo, textureId; // Vertex Array Object, Vertex Buffer Object, Texture ID

  private int numVertices; // Number of vertices to draw

  private int[] pboIds; // Array to store PBO IDs

  private int nextPboIndex = 0; // Index of the next PBO to use

  public RenderData(final int width, final int height) {

    this.width = width;
    this.height = height;
  }

  public void setup() {

    GL46.glActiveTexture(GL46.GL_TEXTURE0); // Activate texture unit 0

    // Vertex Array Object (VAO)
    vao = GL46.glGenVertexArrays();
    GL46.glBindVertexArray(vao);

    // Vertex Buffer Object (VBO)
    vbo = GL46.glGenBuffers();
    GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo);
    GL46.glBufferData(GL46.GL_ARRAY_BUFFER, 0, GL46.GL_DYNAMIC_DRAW); // Allocate empty buffer

    // Vertex attribute configuration (Position, Color, Texture Coordinates)
    int stride = 8 * Float.BYTES;
    GL46.glVertexAttribPointer(0, 2, GL46.GL_FLOAT, false, stride, 0);
    GL46.glEnableVertexAttribArray(0);
    GL46.glVertexAttribPointer(1, 4, GL46.GL_FLOAT, false, stride, 2 * Float.BYTES);
    GL46.glEnableVertexAttribArray(1);
    GL46.glVertexAttribPointer(2, 2, GL46.GL_FLOAT, false, stride, 6 * Float.BYTES);
    GL46.glEnableVertexAttribArray(2);

    GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, 0);
    GL46.glBindVertexArray(0);

    // Texture (Texture)
    textureId = GL46.glGenTextures();
    GL46.glBindTexture(GL46.GL_TEXTURE_2D, textureId);
    GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MIN_FILTER,
        GL46.GL_LINEAR_MIPMAP_LINEAR);
    GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_LINEAR);
    GL46.glTexImage2D(GL46.GL_TEXTURE_2D, 0, GL46.GL_RGBA32F, width, height, 0, GL46.GL_RGBA,
        GL46.GL_FLOAT, (FloatBuffer) null);

    // Pixel Buffer Objects (PBOs) - Texture Streaming
    pboIds = new int[pboCount];
    GL46.glGenBuffers(pboIds);
    for (int i = 0; i < pboCount; i++) {
      GL46.glBindBuffer(GL46.GL_PIXEL_UNPACK_BUFFER, pboIds[i]);
      GL46.glBufferData(GL46.GL_PIXEL_UNPACK_BUFFER, (long) width * height * 4 * Float.BYTES,
          GL46.GL_STREAM_DRAW);
    }
    GL46.glBindBuffer(GL46.GL_PIXEL_UNPACK_BUFFER, 0);
  }

  public void update(float[] vertices) {

    numVertices = vertices.length / 8;

    GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo);
    GL46.glBufferData(GL46.GL_ARRAY_BUFFER, vertices,
        GL46.GL_DYNAMIC_DRAW); // Updates the VBO with the new vertices
    GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, 0);

    // PBO - Mapping
    int pboId = pboIds[nextPboIndex];
    nextPboIndex = (nextPboIndex + 1) % pboCount; // Alternates between PBOs with each update

    GL46.glBindBuffer(GL46.GL_PIXEL_UNPACK_BUFFER, pboId);
    GL46.glBufferData(GL46.GL_PIXEL_UNPACK_BUFFER, (long) width * height * 4 * Float.BYTES,
        GL46.GL_STREAM_DRAW); // Resets the buffer
    FloatBuffer pboBuffer = Objects.requireNonNull(
            GL46.glMapBuffer(GL46.GL_PIXEL_UNPACK_BUFFER, GL46.GL_WRITE_ONLY, null))
        .asFloatBuffer();
    for (int i = 0; i < vertices.length; i += 8) {
      pboBuffer.put(vertices, i + 2, 4); // Extract RGBA values and put in the pboBuffer
    }
    GL46.glUnmapBuffer(GL46.GL_PIXEL_UNPACK_BUFFER);

    // Updates the texture from PBO
    GL46.glBindTexture(GL46.GL_TEXTURE_2D, textureId);
    GL46.glTexSubImage2D(GL46.GL_TEXTURE_2D, 0, 0, 0, width, height, GL46.GL_RGBA, GL46.GL_FLOAT,
        0); // Offset 0 no PBO
    GL46.glBindBuffer(GL46.GL_PIXEL_UNPACK_BUFFER, 0);
    GL46.glGenerateMipmap(GL46.GL_TEXTURE_2D);
  }

  public void draw() {

    GL46.glBindTexture(GL46.GL_TEXTURE_2D, textureId);
    GL46.glBindVertexArray(vao);
    GL46.glDrawArrays(GL46.GL_POINTS, 0, numVertices);
    GL46.glBindVertexArray(0);
  }

  public void cleanup() {

    GL46.glDeleteBuffers(vbo);
    GL46.glDeleteVertexArrays(vao);
    GL46.glDeleteTextures(textureId);
    GL46.glDeleteBuffers(pboIds);
  }

}
