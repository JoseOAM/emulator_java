package faustech.gpu;

import br.faustech.gpu.GPU;
import br.faustech.gpu.FrameBuffer;
import br.faustech.gpu.VideoFrameToVertexArray;
import java.lang.Thread.State;
import org.junit.Test;


public class GPUTest {

  @Test
  public void gpuTest() {

    FrameBuffer frameBuffer = new FrameBuffer(800 * 600 * 6);

    GPU gpu = new GPU(800, 600, frameBuffer);
    gpu.start();

    VideoFrameToVertexArray videoProcessor = new VideoFrameToVertexArray(frameBuffer,
        "C:/Users/ffsga/Downloads/video2.mp4", 800, 600);
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
