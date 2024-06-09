package br.faustech.gpu;

import br.faustech.comum.ComponentThread;
import br.faustech.comum.ComponentType;
import br.faustech.gpu.FrameBuffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import lombok.Getter;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL46;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

public class GPU extends ComponentThread {

  private final FrameBuffer frameBuffer;

  private long window;

  @Getter private int width;

  @Getter private int height;

  private int vbo, vao, shaderProgram;

  public GPU(final int width, final int height, FrameBuffer frameBuffer) {

    super(new byte[10], ComponentType.GPU);
    this.width = width;
    this.height = height;
    this.frameBuffer = frameBuffer;
  }

  @Override
  public void run() {
    // Initialize the window and rendering program
    this.init();

    // Render loop
    while (this.isRunning()) {
      this.render();
    }

    // Clean up resources
    this.cleanUp();
  }

  private void init() {

    if (!GLFW.glfwInit()) {
      throw new IllegalStateException("Failed to initialize GLFW");
    }

    this.window = GLFW.glfwCreateWindow(this.width, this.height, "Emulador", 0, 0);
    if (this.window == 0) {
      throw new IllegalStateException("Failed to create window");
    }

    GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
    assert vidMode != null;
    GLFW.glfwSetWindowPos(this.window, (vidMode.width() - this.width) / 2,
        (vidMode.height() - this.height) / 2);

    GLFW.glfwMakeContextCurrent(this.window);
    GLFW.glfwSwapInterval(1);
    GLFW.glfwShowWindow(this.window);
    GL.createCapabilities();

    // Set up resize callback
    GLFW.glfwSetFramebufferSizeCallback(this.window, (window, newWidth, newHeight) -> {
      this.width = newWidth;
      this.height = newHeight;
      GL46.glViewport(0, 0, newWidth, newHeight);
    });

    // Load shaders and set up rendering pipeline
    setupShaders();
    setupRenderData();
    setWindowIcon();
  }

  private void setupShaders() {

    String vertexShaderSource = """
        #version 460
        layout (location = 0) in vec2 vertexPosition;
        layout (location = 1) in vec4 vertexColor;
        out vec4 fragmentColor;
        void main() {
            gl_Position = vec4(vertexPosition, 0.0, 1.0);
            fragmentColor = vertexColor;
        }
        """;

    String fragmentShaderSource = """
        #version 460
        in vec4 fragmentColor;
        out vec4 color;
        void main() {
            color = fragmentColor;
        }
        """;

    int vertexShader = compileShader(GL46.GL_VERTEX_SHADER, vertexShaderSource);
    int fragmentShader = compileShader(GL46.GL_FRAGMENT_SHADER, fragmentShaderSource);

    shaderProgram = GL46.glCreateProgram();
    GL46.glAttachShader(shaderProgram, vertexShader);
    GL46.glAttachShader(shaderProgram, fragmentShader);
    GL46.glLinkProgram(shaderProgram);
    checkLinkStatus(shaderProgram);

    GL46.glDeleteShader(vertexShader);
    GL46.glDeleteShader(fragmentShader);
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
      String error = GL46.glGetShaderInfoLog(shader);
      throw new RuntimeException(String.format("Error compiling shader: %s", error));
    }
  }

  private void checkLinkStatus(int program) {

    IntBuffer status = BufferUtils.createIntBuffer(1);
    GL46.glGetProgramiv(program, GL46.GL_LINK_STATUS, status);
    if (status.get(0) == GL46.GL_FALSE) {
      String error = GL46.glGetProgramInfoLog(program);
      throw new RuntimeException(String.format("Error linking program: %s", error));
    }
  }

  private void setupRenderData() {

    vao = GL46.glGenVertexArrays();
    vbo = GL46.glGenBuffers();

    GL46.glBindVertexArray(vao);
    GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo);

    GL46.glVertexAttribPointer(0, 2, GL46.GL_FLOAT, false, 6 * Float.BYTES, 0);
    GL46.glEnableVertexAttribArray(0);
    GL46.glVertexAttribPointer(1, 4, GL46.GL_FLOAT, false, 6 * Float.BYTES, 2 * Float.BYTES);
    GL46.glEnableVertexAttribArray(1);

    GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, 0);
    GL46.glBindVertexArray(0);
  }

  private synchronized boolean isRunning() {

    return !GLFW.glfwWindowShouldClose(this.window);
  }

  private void render() {

    GL46.glClear(GL46.GL_COLOR_BUFFER_BIT);
    updateVertexData();  // Update vertex data on each frame
    GL46.glUseProgram(shaderProgram);
    GL46.glBindVertexArray(vao);
    float[] vertices = frameBuffer.readFromFrontBuffer();
    int numVertices =
        vertices.length / 6; // Each vertex has 6 components (2 for position and 4 for color)
    GL46.glDrawArrays(GL46.GL_POINTS, 0, numVertices);
    GL46.glBindVertexArray(0);
    GLFW.glfwSwapBuffers(this.window);
    GLFW.glfwPollEvents();
  }

  private void updateVertexData() {

    float[] vertices = frameBuffer.readFromFrontBuffer();
    FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
    vertexBuffer.put(vertices).flip();

    GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vbo);
    GL46.glBufferData(GL46.GL_ARRAY_BUFFER, vertexBuffer, GL46.GL_DYNAMIC_DRAW);
    GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, 0);
  }

  private synchronized void cleanUp() {

    GL46.glDeleteBuffers(vbo);
    GL46.glDeleteVertexArrays(vao);
    GL46.glDeleteProgram(shaderProgram);
    GLFW.glfwDestroyWindow(this.window);
    GLFW.glfwTerminate();
  }

  private void setWindowIcon() {

    try (MemoryStack stack = MemoryStack.stackPush()) {
      IntBuffer w = stack.mallocInt(1);
      IntBuffer h = stack.mallocInt(1);
      IntBuffer comp = stack.mallocInt(1);
      ByteBuffer icon = STBImage.stbi_load("src/main/resources/icon.png", w, h, comp, 4);
      if (icon == null) {
        throw new RuntimeException(
            String.format("Failed to load icon: %s", STBImage.stbi_failure_reason()));
      }
      GLFWImage.Buffer icons = GLFWImage.malloc(1);
      icons.position(0).width(w.get(0)).height(h.get(0)).pixels(icon);
      GLFW.glfwSetWindowIcon(this.window, icons);
      STBImage.stbi_image_free(icon);
    }
  }

}