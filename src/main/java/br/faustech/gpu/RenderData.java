package br.faustech.gpu;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL46;

public class RenderData {

  private int vao, vbo, textureId;

  private int numVertices;

  FloatBuffer vertexBuffer;

  FloatBuffer textureBuffer;

  public void setup() {

    GL46.glActiveTexture(GL46.GL_TEXTURE0);

    vao = GL46.glGenVertexArrays();
    GL46.glBindVertexArray(vao);

    vbo = GL46.glGenBuffers();
    GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo);
    GL46.glBufferData(GL46.GL_ARRAY_BUFFER, 0, GL46.GL_DYNAMIC_DRAW);

    // Configure vertex attribute pointers
    int stride = 8 * Float.BYTES;
    GL46.glVertexAttribPointer(0, 2, GL46.GL_FLOAT, false, stride, 0);
    GL46.glEnableVertexAttribArray(0);
    GL46.glVertexAttribPointer(1, 4, GL46.GL_FLOAT, false, stride, 2 * Float.BYTES);
    GL46.glEnableVertexAttribArray(1);
    GL46.glVertexAttribPointer(2, 2, GL46.GL_FLOAT, false, stride, 6 * Float.BYTES);
    GL46.glEnableVertexAttribArray(2);

    GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo);
    GL46.glBufferData(GL46.GL_ARRAY_BUFFER, 0, GL46.GL_DYNAMIC_DRAW);

    GL46.glBindVertexArray(0);

    // Texture setup
    textureId = GL46.glGenTextures();
    GL46.glBindTexture(GL46.GL_TEXTURE_2D, textureId);
    GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MIN_FILTER,
        GL46.GL_LINEAR_MIPMAP_LINEAR);
    GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_LINEAR);
  }

  public void update(float[] vertices, int width, int height) {

    numVertices = vertices.length / 8;

    if (vertexBuffer == null || vertexBuffer.capacity() < vertices.length) {
      vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
    } else {
      vertexBuffer.clear();
    }
    vertexBuffer.put(vertices).flip();

    GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo);
    GL46.glBufferData(GL46.GL_ARRAY_BUFFER, vertexBuffer, GL46.GL_STATIC_DRAW);
    GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, 0);

    if (textureBuffer == null || textureBuffer.capacity() < width * height * 4) {
      textureBuffer = BufferUtils.createFloatBuffer(width * height * 4);
    } else {
      textureBuffer.clear();
    }

    for (int i = 0; i < vertices.length; i += 8) {
      textureBuffer.put(vertices[i + 2]).put(vertices[i + 3]).put(vertices[i + 4])
          .put(vertices[i + 5]);
    }
    textureBuffer.flip();
    GL46.glBindTexture(GL46.GL_TEXTURE_2D, textureId);
    GL46.glTexImage2D(GL46.GL_TEXTURE_2D, 0, GL46.GL_RGBA, width, height, 0, GL46.GL_RGBA,
        GL46.GL_FLOAT, textureBuffer);
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
  }

}