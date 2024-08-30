package br.faustech;

import br.faustech.bus.Bus;
import br.faustech.cpu.CPU;
import br.faustech.gpu.FrameBuffer;
import br.faustech.gpu.GPU;
import br.faustech.memory.Memory;
import br.faustech.reader.ProgramUtils;

import java.io.File;
import java.io.IOException;

public class Main {

    private static final int WIDTH = 320;

    private static final int HEIGHT = 240;

    private static final int FRAME_BUFFER_SIZE = WIDTH * HEIGHT * 4;

    private static final int MEMORY_SIZE = 4096;

    public static boolean LOG = false;

    private static GPU gpu;

    private static CPU cpu;

    public static void main(String[] args) throws IOException {

        if (args.length < 1) {
            throw new IllegalArgumentException("Program file name not provided.");
        }

        LOG = args.length > 1 && args[1].equals("--log");

        setup(args);

        cpu.start();
        gpu.start();

        while (gpu.isAlive()) {
            if (gpu.getState() == Thread.State.TERMINATED) {
                gpu.interrupt();
                cpu.interrupt();
            }
        }
    }

    private static void setup(String[] args) throws IOException {
        final FrameBuffer frameBuffer = new FrameBuffer(FRAME_BUFFER_SIZE);
        final Bus bus = new Bus(frameBuffer, new Memory(MEMORY_SIZE));

        ProgramUtils programUtils = new ProgramUtils(bus);
        programUtils.writeProgramInMemory(programUtils.readFile(new File(args[0])));

        gpu = new GPU(WIDTH, HEIGHT, frameBuffer);
        cpu = new CPU(bus);
    }
}
