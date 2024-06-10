package faustech.gpu;

import br.faustech.gpu.FrameBuffer;
import br.faustech.gpu.GPU;
import br.faustech.gpu.VideoFrameToVertexArray;
import java.lang.Thread.State;
import org.junit.Test;


public class GPUTest {

  private static final String VIDEO_PATH = "";

  @Test
  public void gpuTest() {

    int WIDTH = 800;
    int HEIGHT = 600;

    FrameBuffer frameBuffer = new FrameBuffer(WIDTH * HEIGHT * 8);

    GPU gpu = new GPU(WIDTH, HEIGHT, frameBuffer);
    gpu.start();

    VideoFrameToVertexArray videoProcessor = new VideoFrameToVertexArray(frameBuffer,
        VIDEO_PATH, WIDTH, HEIGHT);
    videoProcessor.start();

    while (gpu.isAlive()) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    // Stop the video processor thread
    if (videoProcessor.getState() != State.TERMINATED) {
      videoProcessor.interrupt();
    }

    // Stop the GPU thread
    if (gpu.getState() != State.TERMINATED) {
      gpu.interrupt();
    }
  }

}
