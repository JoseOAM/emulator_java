package br.faustech.gpu;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallbackI;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

@Getter
@RequiredArgsConstructor
public class Window {

  private long window;

  private final int width;

  private final int height;

  private final String title;

  public void init() {

    window = GLFW.glfwCreateWindow(width, height, title, 0, 0);
    if (window == 0) {
      throw new IllegalStateException("Failed to create window");
    }

    GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
    assert vidMode != null;
    GLFW.glfwSetWindowPos(window, (vidMode.width() - width) / 2, (vidMode.height() - height) / 2);

    GLFW.glfwMakeContextCurrent(window);
    GLFW.glfwSwapInterval(1);
    GLFW.glfwShowWindow(window);
    GL.createCapabilities();
  }

  public void setResizeCallback(GLFWFramebufferSizeCallbackI callback) {

    GLFW.glfwSetFramebufferSizeCallback(window, callback);
  }

  protected void setIcon() {

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

  public boolean shouldClose() {

    return GLFW.glfwWindowShouldClose(window);
  }

  public void swapBuffers() {

    GLFW.glfwSwapBuffers(window);
  }

  public void pollEvents() {

    GLFW.glfwPollEvents();
  }

  public void cleanup() {

    GLFW.glfwDestroyWindow(window);
  }

}