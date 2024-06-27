package br.faustech.gpu;

import br.faustech.bus.Bus;
import br.faustech.memory.Memory;
import java.lang.Thread.State;
import org.junit.Test;


public class GPUTest {

  private static final String VIDEO_PATH = "C:\\Users\\ffsga\\Downloads\\video2.mp4";

  @Test
  public void gpuTest() {

    int WIDTH = 800;
    int HEIGHT = 600;
    int bufferSize = WIDTH * HEIGHT * 8 * 4;
    int memorySize = 1024;

    int[] frameBufferAddresses = new int[bufferSize];
    int[] memoryAddresses = new int[memorySize];
    for (int i = 0; i < (bufferSize + memorySize); i++) {
      if (i < bufferSize) {
        frameBufferAddresses[i] = i;
      } else {
        memoryAddresses[i - bufferSize] = i;
      }
    }

    FrameBuffer frameBuffer = new FrameBuffer(frameBufferAddresses, bufferSize);

    Memory memory = new Memory(memoryAddresses, memorySize);

    GPU gpu = new GPU(new int[1], WIDTH, HEIGHT, frameBuffer);
    gpu.start();

    VideoFrameToVertexArray videoProcessor = new VideoFrameToVertexArray(VIDEO_PATH, WIDTH, HEIGHT, frameBuffer);
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
