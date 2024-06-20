package br.faustech;

import br.faustech.bus.Bus;
import br.faustech.gpu.FrameBuffer;
import br.faustech.gpu.GPU;
import br.faustech.gpu.VideoFrameToVertexArray;
import br.faustech.memory.Memory;
import java.lang.Thread.State;

public class Main {

  private static final String VIDEO_PATH = "";

  private final static int WIDTH = 512;

  private final static int HEIGHT = 512;

  private final static int MEMORY_SIZE = 1024;

  public static void main() {

    final FrameBuffer frameBuffer = new FrameBuffer(WIDTH * HEIGHT * 8);
    final GPU gpu = new GPU(WIDTH, HEIGHT, frameBuffer);
    final Memory memory = new Memory(MEMORY_SIZE);
    final Bus bus = new Bus(frameBuffer, memory);
    final VideoFrameToVertexArray videoProcessor = new VideoFrameToVertexArray(frameBuffer,
        VIDEO_PATH, WIDTH, HEIGHT, bus);

    videoProcessor.start();
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
