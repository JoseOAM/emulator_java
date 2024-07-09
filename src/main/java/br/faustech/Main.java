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

  private static ProgramUtils programUtils;

  private static Bus bus;

  private static GPU gpu;

  private static CPU cpu;

  public static void main(String[] args) throws IOException, MemoryException {

//    if (args.length < 1) {
//      throw new IllegalArgumentException("Program file name not provided.");
//    }

    String filename = "C:\\Users\\ffsga\\IdeaProjects\\emulator\\output.txt";

    setup();

    programUtils.writeProgramInMemory(programUtils.readFile(new File(filename)));

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

    int WIDTH = 512;
    int HEIGHT = 512;
    int frameBufferSize = WIDTH * HEIGHT;
    int memorySize = 4096;

    int[] memoryAddresses = new int[memorySize];
    int[] frameBufferAddresses = new int[frameBufferSize];

    for (int i = 0; i < memorySize + frameBufferSize; i++) {
      if (i < memorySize) {
        memoryAddresses[i] = i;
      } else {
        frameBufferAddresses[i - memorySize] = i;
      }
    }

    FrameBuffer frameBuffer = new FrameBuffer(frameBufferAddresses, frameBufferSize);
    Memory memory = new Memory(memoryAddresses, memorySize);
    bus = new Bus(frameBuffer, memory);
    programUtils = new ProgramUtils(bus);
    gpu = new GPU(new int[1], WIDTH, HEIGHT, frameBuffer);
    cpu = new CPU(new int[1], bus);
  }

}
