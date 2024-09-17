package br.faustech;

import br.faustech.bus.Bus;
import br.faustech.cpu.CPU;
import br.faustech.gpu.FrameBuffer;
import br.faustech.gpu.GPU;
import br.faustech.memory.Memory;
import br.faustech.memory.MemoryException;
import br.faustech.reader.ProgramUtils;
import java.io.File;
import java.io.IOException;

public class Main {

  private static final int WIDTH = 512;

  private static final int HEIGHT = 512;

  private static final int FRAME_BUFFER_SIZE = WIDTH * HEIGHT * 4;

  private static final int MEMORY_SIZE = 4096;

  public static boolean LOG = false;

  private static ProgramUtils programUtils;

  private static GPU gpu;

  private static CPU cpu;

  public static void main(String[] args) throws IOException, MemoryException {

//    if (args.length < 1) {
//      throw new IllegalArgumentException("Program file name not provided.");
//    }
    args = new String[1];
    args[0] = "C:\\Users\\Mate Amargo\\IdeaProjects\\emulator\\boot.txt";

    LOG = false;

    setup();

    programUtils.writeProgramInMemory(programUtils.readFile(new File(args[0])));

    gpu.start();
    cpu.start();

    while (gpu.isAlive()) {
      if (gpu.getState() == Thread.State.TERMINATED) {
        gpu.interrupt();
        cpu.interrupt();
      }
    }
  }

  private static void setup() {

    int[] memoryAddresses = new int[MEMORY_SIZE];
    int[] frameBufferAddresses = new int[FRAME_BUFFER_SIZE + 1];

    for (int i = 0; i <= MEMORY_SIZE + FRAME_BUFFER_SIZE; i++) {
      if (i < MEMORY_SIZE) {
        memoryAddresses[i] = i;
      } else {
        frameBufferAddresses[i - MEMORY_SIZE] = i;
      }
    }

    final FrameBuffer frameBuffer = new FrameBuffer(frameBufferAddresses, FRAME_BUFFER_SIZE);
    final Memory memory = new Memory(memoryAddresses, MEMORY_SIZE);
    final Bus bus = new Bus(frameBuffer, memory);
    programUtils = new ProgramUtils(bus);
    gpu = new GPU(new int[1], WIDTH, HEIGHT, frameBuffer);
    cpu = new CPU(new int[1], bus);
  }

}
