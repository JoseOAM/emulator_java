package br.faustech.gpu;

import br.faustech.bus.Bus;
import br.faustech.memory.Memory;
import java.lang.Thread.State;
import org.junit.jupiter.api.Test;


public class GPUTest {

  private static final String VIDEO_PATH = "./teste.mp4";

  private static final int WIDTH = 512;

  private static final int HEIGHT = 512;

  private static final int FRAME_BUFFER_SIZE = WIDTH * HEIGHT * 4;

  private static final int MEMORY_SIZE = 4096;

  @Test
  public void gpuTest() {

    int WIDTH = 512;
    int HEIGHT = 512;
    int frameBufferSize = WIDTH * HEIGHT * 4;

    int[] memoryAddresses = new int[MEMORY_SIZE];
    int[] frameBufferAddresses = new int[FRAME_BUFFER_SIZE + 1];

    for (int i = 0; i <= MEMORY_SIZE + FRAME_BUFFER_SIZE; i++) {
      if (i < MEMORY_SIZE) {
        memoryAddresses[i] = i;
      } else {
        frameBufferAddresses[i - MEMORY_SIZE] = i;
      }
    }
    FrameBuffer frameBuffer = new FrameBuffer(frameBufferAddresses, frameBufferSize);

    Bus bus = new Bus(frameBuffer, new Memory(memoryAddresses, MEMORY_SIZE));

    VideoFrameToVertexArray videoProcessor = new VideoFrameToVertexArray(VIDEO_PATH, WIDTH, HEIGHT,
        bus, frameBuffer);
    videoProcessor.start();

    GPU gpu = new GPU(new int[1], WIDTH, HEIGHT, frameBuffer);
    gpu.start();

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
