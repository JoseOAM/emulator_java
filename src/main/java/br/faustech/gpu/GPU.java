package br.faustech.gpu;

import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;

import br.faustech.comum.ComponentThread;
import br.faustech.comum.ComponentType;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL46;

public class GPU extends ComponentThread {

  private final FrameBuffer frameBuffer;

  private int width;

  private int height;

  private ShaderProgram shaderProgram;

  private RenderData renderData;

  private Window window;

  public GPU(int width, int height, FrameBuffer frameBuffer) {

    super(ComponentType.GPU.toString().getBytes(), ComponentType.GPU);
    this.width = width;
    this.height = height;
    this.frameBuffer = frameBuffer;
  }

  @Override
  public void run() {

    init();

    while (isRunning()) {
      render();
    }

    cleanUp();
  }

  private void init() {

    if (!GLFW.glfwInit()) {
      throw new IllegalStateException("Failed to initialize GLFW");
    }

    window = new Window(width, height, "Emulador");
    window.init();
    window.setResizeCallback((_, newWidth, newHeight) -> {
      width = newWidth;
      height = newHeight;
      GL46.glViewport(0, 0, newWidth, newHeight);
      GL46.glMatrixMode(GL_PROJECTION);
      GL46.glLoadIdentity();
      GL46.glOrtho(0, newWidth, newHeight, 0, -1, 1);
      GL46.glMatrixMode(GL_MODELVIEW);
      GL46.glLoadIdentity();
    });

    shaderProgram = new ShaderProgram();
    shaderProgram.loadShaders();

    renderData = new RenderData(width, height);
    renderData.setup();

    window.setIcon();
  }

  private boolean isRunning() {

    return !window.shouldClose();
  }

  private void render() {

    GL46.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    GL46.glClear(GL46.GL_COLOR_BUFFER_BIT | GL46.GL_DEPTH_BUFFER_BIT);

    shaderProgram.use();
    float[] frame = frameBuffer.readFromFrontBufferAsFloats();

    renderData.update(frame);
    renderData.draw();

    window.swapBuffers();
    window.pollEvents();
  }

  private void cleanUp() {

    renderData.cleanup();
    shaderProgram.cleanup();
    window.cleanup();

    GLFW.glfwTerminate();
  }

}
