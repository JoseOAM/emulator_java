package faustech.gpu;

import br.faustech.gpu.GPU;
import br.faustech.gpu.VideoBuffer;
import br.faustech.gpu.VideoFrameToByteArray;
import java.lang.Thread.State;
import org.junit.Test;


public class GPUTest {

  @Test
  public void gpuTest() {

    VideoBuffer videoBuffer = new VideoBuffer(1920 * 1080 * 4, 1920 * 1080 * 4);

    GPU gpu = new GPU(800, 600, videoBuffer);
    gpu.start();

    VideoFrameToByteArray videoProcessor = new VideoFrameToByteArray(videoBuffer,
        "C:/Users/ffsga/Downloads/video2.mp4");
    videoProcessor.start();

    do {
      videoProcessor.setResolution(gpu.getWidth(), gpu.getHeight());
    } while (gpu.isAlive());

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
