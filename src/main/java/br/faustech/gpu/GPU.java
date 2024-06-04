package br.faustech.gpu;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

public class GPU {

  private final VideoMemory videoMemory;

  private long window;

  private int width;

  private int height;

  private byte[] pixelData;

  public GPU(int width, int height, VideoMemory videoMemory) {

    this.width = width;
    this.height = height;
    this.videoMemory = videoMemory;
    init();
  }

  private void init() {

    if (!GLFW.glfwInit()) {
      throw new IllegalStateException("Failed to initialize GLFW");
    }

    window = GLFW.glfwCreateWindow(width, height, "Pixel Renderer", 0, 0);
    if (window == 0) {
      throw new IllegalStateException("Failed to create window");
    }

    // Set window icon
    setWindowIcon();

    GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
    assert vidMode != null;
    GLFW.glfwSetWindowPos(window, (vidMode.width() - width) / 2, (vidMode.height() - height) / 2);

    GLFW.glfwMakeContextCurrent(window);
    GLFW.glfwSwapInterval(1);
    GLFW.glfwShowWindow(window);

    GL.createCapabilities();

    GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    GL11.glOrtho(0, width, height, 0, -1, 1);
    GL11.glViewport(0, 0, width, height);

    // Set resize callback
    GLFW.glfwSetFramebufferSizeCallback(window, (window, newWidth, newHeight) -> {
      this.width = newWidth;
      this.height = newHeight;
      GL11.glViewport(0, 0, newWidth, newHeight);
    });
  }

  private void setWindowIcon() {

    try (MemoryStack stack = MemoryStack.stackPush()) {
      IntBuffer w = stack.mallocInt(1);
      IntBuffer h = stack.mallocInt(1);
      IntBuffer comp = stack.mallocInt(1);

      ByteBuffer icon = STBImage.stbi_load("src/main/resources/icon.png", w, h, comp, 4);
      if (icon == null) {
        throw new RuntimeException("Failed to load icon: " + STBImage.stbi_failure_reason());
      }

      GLFWImage.Buffer icons = GLFWImage.malloc(1);
      icons.position(0).width(w.get(0)).height(h.get(0)).pixels(icon);

      GLFW.glfwSetWindowIcon(window, icons);

      STBImage.stbi_image_free(icon);
    }
  }

  public void render() {

    updatePixelData();

    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
    GL11.glBegin(GL11.GL_POINTS);

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int index = (y * width + x) * 4;
        if (index + 3 < pixelData.length) {  // Ensure we are not out of bounds
          float a = (pixelData[index] & 0xFF) / 255.0f;
          float r = (pixelData[index + 1] & 0xFF) / 255.0f;
          float g = (pixelData[index + 2] & 0xFF) / 255.0f;
          float b = (pixelData[index + 3] & 0xFF) / 255.0f;
          GL11.glColor4f(r, g, b, a);
          GL11.glVertex2i(x, y);
        }
      }
    }
    GL11.glEnd();

    GLFW.glfwSwapBuffers(window);
    GLFW.glfwPollEvents();

  }

  public void updatePixelData() {

    synchronized (videoMemory) {
      this.pixelData = this.videoMemory.readFromFrontBuffer();
      this.videoMemory.swap();
    }
  }

  public boolean isRunning() {

    return !GLFW.glfwWindowShouldClose(window);
  }

  public void cleanUp() {

    GLFW.glfwDestroyWindow(window);
    GLFW.glfwTerminate();
  }

}
