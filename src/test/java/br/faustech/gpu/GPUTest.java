package br.faustech.gpu;

import java.lang.Thread.State;
import org.junit.jupiter.api.Test;


public class GPUTest {

  private static final String VIDEO_PATH = "C:\\Users\\ffsga\\Downloads\\video2.mp4";

  @Test
  public void gpuTest() {

    int WIDTH = 800;
    int HEIGHT = 600;
    int frameBufferSize = WIDTH * HEIGHT * 8 * 4;

    int[] frameBufferAddresses = new int[frameBufferSize];
    for (int i = 0; i < frameBufferSize; i++) {
      frameBufferAddresses[i] = i;
    }

    FrameBuffer frameBuffer = new FrameBuffer(frameBufferAddresses, frameBufferSize);

    GPU gpu = new GPU(new int[1], WIDTH, HEIGHT, frameBuffer);
    gpu.start();

    VideoFrameToVertexArray videoProcessor = new VideoFrameToVertexArray(VIDEO_PATH, WIDTH, HEIGHT,
        frameBuffer);
    videoProcessor.start();

    while (gpu.isAlive()) {
      if (gpu.getState() == State.TERMINATED) {
        // Stop all threads
        if (videoProcessor.getState() != State.TERMINATED) {
          videoProcessor.interrupt();
        }
        if (gpu.getState() != State.TERMINATED) {
          gpu.interrupt();
        }
      }
    }
  }

}
