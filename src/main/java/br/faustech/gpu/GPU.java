package br.faustech.gpu;

import br.faustech.comum.ComponentThread;
import br.faustech.memory.MemoryException;
import lombok.Getter;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL46;

/**
 * Represents a GPU component that handles rendering operations.
 */
public class GPU extends ComponentThread {

  @Getter private static int width;

  @Getter private static int height;

  private final FrameBuffer frameBuffer;

  private ShaderProgram shaderProgram;

  private RenderData renderData;

  private Window window;

  /**
   * Constructs a new GPU instance with specified dimensions and framebuffer.
   *
   * @param addresses   the memory addresses.
   * @param width       the width of the render window.
   * @param height      the height of the render window.
   * @param frameBuffer the framebuffer to use for rendering.
   */
  public GPU(final int[] addresses, final int width, final int height,
      final FrameBuffer frameBuffer) {

    super(addresses);
    GPU.width = width;
    GPU.height = height;
    this.frameBuffer = frameBuffer;
  }

  /**
   * The main run loop of the GPU component, handling initialization and rendering.
   */
  @Override
  public void run() {

    init();
    while (isRunning()) {
      try {
        render();
      } catch (MemoryException e) {
        throw new RuntimeException(e);
      }
    }
    cleanUp();
  }

  /**
   * Initializes the necessary components including window, shader program, and other render data.
   */
  private void init() {

    if (!GLFW.glfwInit()) {
      throw new IllegalStateException("Failed to initialize GLFW");
    }

    window = new Window(width, height, "Emulator");
    window.init();
    window.setResizeCallback((ignore, newWidth, newHeight) -> {
      GL46.glViewport(0, 0, newWidth, newHeight);
    });

    shaderProgram = new ShaderProgram();
    shaderProgram.loadShaders();

    renderData = new RenderData(width, height);
    renderData.setup();

    window.setIcon();
  }

  /**
   * Checks if the window is still open and the rendering should continue.
   *
   * @return true if the window is not marked to close, false otherwise
   */
  private boolean isRunning() {

    return !window.shouldClose();
  }

  /**
   * Handles the rendering of each frame to the window.
   *
   * @throws MemoryException if there's an issue accessing frame data
   */
  private void render() throws MemoryException {

    GL46.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    GL46.glClear(GL46.GL_COLOR_BUFFER_BIT | GL46.GL_DEPTH_BUFFER_BIT);

    shaderProgram.use();
    float[] frame = frameBuffer.readFromFrontBufferAsFloats(0, FrameBuffer.getBufferSize() / 4);
    renderData.update(frame);
    renderData.draw();

    window.swapBuffers();
    window.pollEvents();
  }

  /**
   * Cleans up resources upon shutdown, ensuring graceful termination of GLFW and other components.
   */
  private void cleanUp() {

    renderData.cleanup();
    shaderProgram.cleanup();
    window.cleanup();
    GLFW.glfwTerminate();
  }

}
