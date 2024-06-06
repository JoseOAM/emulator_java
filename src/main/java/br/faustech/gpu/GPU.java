package br.faustech.gpu;

import br.faustech.comum.ComponentType;
import br.faustech.comum.ComponentThread;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import lombok.Getter;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

public class GPU extends ComponentThread {

  private final VideoBuffer videoBuffer;

  private long window;

  @Getter private int width;

  @Getter private int height;

  private byte[] pixelData;

  public GPU(final int width, final int height, final VideoBuffer videoBuffer) {

    byte[] address = new byte[10];
    for (int i = 0; i < address.length; i++) {
      address[i] = (byte) (Math.random() * 256);
    }

    super(address, ComponentType.GPU);

    this.width = width;
    this.height = height;
    this.videoBuffer = videoBuffer;
  }

  @Override
  public void run() {

    // Initialize the window
    this.init();

    // Render the window
    while (this.isRunning()) {
      this.render();
    }

    // Clean up
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

    // Set window icon
    setWindowIcon();

    // Create window in the center of the screen
    GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
    assert vidMode != null;
    GLFW.glfwSetWindowPos(this.window, (vidMode.width() - this.width) / 2,
        (vidMode.height() - this.height) / 2);

    // Make the OpenGL context current
    GLFW.glfwMakeContextCurrent(this.window);
    GLFW.glfwSwapInterval(1);
    GLFW.glfwShowWindow(this.window);

    // Load OpenGL functions
    GL.createCapabilities();

    // Set OpenGL settings
    GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    GL11.glOrtho(0, this.width, this.height, 0, -1, 1);

    // Set resize callback
    GLFW.glfwSetWindowSizeCallback(this.window, (_, width, height) -> {
      this.width = width;
      this.height = height;
      GL11.glViewport(0, 0, width, height);
      GL11.glLoadIdentity();
      GL11.glOrtho(0, width, height, 0, -1, 1);
    });

    // Set close callback
    GLFW.glfwSetWindowCloseCallback(this.window, window -> {
      GLFW.glfwSetWindowShouldClose(window, true);
    });
  }

  private synchronized boolean isRunning() {

    // Check if the window should close
    return !GLFW.glfwWindowShouldClose(this.window);
  }

  private void render() {

    // Update pixel data
    this.getPixelData();

    // Clear the screen
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
    GL11.glBegin(GL11.GL_POINTS);

    // Render pixel data
    for (int y = 0; y < this.height; y++) {
      for (int x = 0; x < this.width; x++) {
        int index = (y * this.width + x) * 4;
        if (index + 3 < this.pixelData.length) {  // Ensure we are not out of bounds
          float a = (this.pixelData[index] & 0xFF) / 255.0f;
          float r = (this.pixelData[index + 1] & 0xFF) / 255.0f;
          float g = (this.pixelData[index + 2] & 0xFF) / 255.0f;
          float b = (this.pixelData[index + 3] & 0xFF) / 255.0f;
          GL11.glColor4f(r, g, b, a);
          GL11.glVertex2i(x, y);
        }
      }
    }
    GL11.glEnd();

    // Swap buffers and poll events
    GLFW.glfwSwapBuffers(this.window);
    GLFW.glfwPollEvents();
  }

  private synchronized void cleanUp() {

    // Destroy window and terminate GLFW
    GLFW.glfwDestroyWindow(this.window);
    GLFW.glfwTerminate();
  }

  private void setWindowIcon() {

    try (MemoryStack stack = MemoryStack.stackPush()) {
      IntBuffer w = stack.mallocInt(1);
      IntBuffer h = stack.mallocInt(1);
      IntBuffer comp = stack.mallocInt(1);

      // Load icon
      ByteBuffer icon = STBImage.stbi_load("src/main/resources/icon.png", w, h, comp, 4);
      if (icon == null) {
        throw new RuntimeException(STR."Failed to load icon: \{STBImage.stbi_failure_reason()}");
      }

      // Set icon
      GLFWImage.Buffer icons = GLFWImage.malloc(1);
      icons.position(0).width(w.get(0)).height(h.get(0)).pixels(icon);

      GLFW.glfwSetWindowIcon(this.window, icons);

      STBImage.stbi_image_free(icon);
    }
  }

  private void getPixelData() {

    // Get pixel data from video memory
    synchronized (this.videoBuffer) {
      this.pixelData = this.videoBuffer.readFromFrontBuffer();
      this.videoBuffer.swap();
    }
  }

}
