package br.faustech.gpu;

import br.faustech.bus.Bus;
import br.faustech.memory.Memory;
import java.lang.Thread.State;
import org.junit.Test;


public class GPUTest {

  private static final String VIDEO_PATH = "";

  @Test
  public void gpuTest() {

    int WIDTH = 1280;
    int HEIGHT = 720;

    FrameBuffer frameBuffer = new FrameBuffer(WIDTH * HEIGHT * 8);

    Memory memory = new Memory(1024);
    Bus bus = new Bus(frameBuffer, memory);

    GPU gpu = new GPU(WIDTH, HEIGHT, frameBuffer);
    gpu.start();

    VideoFrameToVertexArray videoProcessor = new VideoFrameToVertexArray(VIDEO_PATH,
        WIDTH, HEIGHT, bus);
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
