package br.faustech;

import br.faustech.bus.Bus;
import br.faustech.cpu.CPU;
import br.faustech.gpu.FrameBuffer;
import br.faustech.gpu.GPU;
import br.faustech.memory.Memory;
import br.faustech.reader.ProgramUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;

public class Main {

  private static ProgramUtils programUtils;

  private static Bus bus;

  private static FrameBuffer frameBuffer;


  private static GPU gpu;

  private static CPU cpu;

  public static void main(String[] args) throws FileNotFoundException {

//    if (args.length < 1) {
//      throw new IllegalArgumentException("Program file name not provided.");
//    }

    String filename = "C:\\Users\\ffsga\\IdeaProjects\\emulator\\output.bin";

    // Check if the file extension is .bin
    if (!filename.toLowerCase().endsWith(".bin")) {
      throw new IllegalArgumentException("Invalid file type. The file must be a .bin file.");
    }

    File file = new File(filename);

    if (!file.exists()) {
      throw new FileNotFoundException(String.format("File %s not found.", filename));
    }

    setup();

    String program = programUtils.readFile(file);
    programUtils.writeProgramInMemory(program);

    gpu.start();
    cpu.start();

    while (gpu.isAlive()) {
      if (gpu.getState() == Thread.State.TERMINATED) {
        gpu.interrupt();
        cpu.interrupt();
      }
    }

    System.out.println(Arrays.toString(bus.read(4096, 4130)));
  }

  private static void setup() {

    int WIDTH = 800;
    int HEIGHT = 600;
    int frameBufferSize = WIDTH * HEIGHT * 8 * 4;
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

    frameBuffer = new FrameBuffer(frameBufferAddresses, frameBufferSize);
    Memory memory = new Memory(memoryAddresses, memorySize);
    bus = new Bus(frameBuffer, memory);
    programUtils = new ProgramUtils(bus);
    gpu = new GPU(new int[1], WIDTH, HEIGHT, frameBuffer);
    cpu = new CPU(new int[1], bus);
  }

}
