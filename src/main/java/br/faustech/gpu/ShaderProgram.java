package br.faustech.gpu;

import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL46;

public class ShaderProgram {

  private int programId;

  public void loadShaders() {

    int vertexShader = compileShader(GL46.GL_VERTEX_SHADER, """
            #version 460
            layout (location = 0) in vec2 vertexPosition;
            layout (location = 1) in vec4 vertexColor;
            layout (location = 2) in vec2 texCoord;
            out vec4 fragmentColor;
            out vec2 TexCoord;
            void main() {
                gl_Position = vec4(vertexPosition, 0.0, 1.0);
                fragmentColor = vertexColor;
                TexCoord = texCoord;
            }
        """);

    int fragmentShader = compileShader(GL46.GL_FRAGMENT_SHADER, """
            #version 460
            in vec4 fragmentColor;
            in vec2 TexCoord;
            out vec4 color;
            uniform sampler2D ourTexture;
            void main() {
                color = texture(ourTexture, TexCoord) * fragmentColor;
            }
        """);

    programId = GL46.glCreateProgram();
    GL46.glAttachShader(programId, vertexShader);
    GL46.glAttachShader(programId, fragmentShader);
    GL46.glLinkProgram(programId);
    checkLinkStatus(programId);

    GL46.glDeleteShader(vertexShader);
    GL46.glDeleteShader(fragmentShader);
  }

  public void use() {

    GL46.glUseProgram(programId);
  }

  public void cleanup() {

    GL46.glDeleteProgram(programId);
  }

  private int compileShader(int type, String source) {

    int shader = GL46.glCreateShader(type);
    GL46.glShaderSource(shader, source);
    GL46.glCompileShader(shader);
    checkCompileStatus(shader);
    return shader;
  }

  private void checkCompileStatus(int shader) {

    IntBuffer status = BufferUtils.createIntBuffer(1);
    GL46.glGetShaderiv(shader, GL46.GL_COMPILE_STATUS, status);
    if (status.get(0) == GL46.GL_FALSE) {
      throw new RuntimeException(
          String.format("Shader compile error: %s", GL46.glGetShaderInfoLog(shader)));
    }
  }

  private void checkLinkStatus(int program) {

    IntBuffer status = BufferUtils.createIntBuffer(1);
    GL46.glGetProgramiv(program, GL46.GL_LINK_STATUS, status);
    if (status.get(0) == GL46.GL_FALSE) {
      throw new RuntimeException(
          String.format("Program link error: %s", GL46.glGetProgramInfoLog(program)));
    }
  }

}
