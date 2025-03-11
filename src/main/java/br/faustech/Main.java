package br.faustech;

import br.faustech.bus.Bus;
import br.faustech.cpu.CPU;
import br.faustech.gpu.GPU;
import br.faustech.gui.GUI;
import br.faustech.comum.ArgsListener;
import br.faustech.memory.FrameBuffer;
import br.faustech.memory.Memory;
import br.faustech.reader.ProgramUtils;
import lombok.Getter;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;

@Log
public class Main {

    private static int WIDTH = 320;

    private static int HEIGHT = 240;

    private static int FRAME_BUFFER_SIZE = WIDTH * HEIGHT * 4; // 4 bytes per pixel

    private static int MEMORY_SIZE = 4194304; // 4MB

    @Getter
    private static int clockSpeed = 1000; // 1Hz

    private static boolean LOG = true;

    private static GPU gpu;

    private static CPU cpu;

    private static GUI gui;

    public static void main(String[] args) throws IOException {
        LOG = args.length > 1 && args[1].equals("--log");

        gui = new GUI(new ArgsListener() {
            @Override
            public void onArgsSelected(String[] args) throws IOException {
                WIDTH = Integer.parseInt(args[1]);
                HEIGHT = Integer.parseInt(args[2]);
                MEMORY_SIZE = Integer.parseInt(args[3]);
                FRAME_BUFFER_SIZE = WIDTH * HEIGHT * 4;
                setup(args);
                cpu.start();
                gpu.start();

                while (gpu.isAlive()) {
                    if (gpu.getState() == Thread.State.TERMINATED) {
                        gpu.interrupt();
                        cpu.interrupt();
                    }
                }
            }});

    }

    private static void setup(String[] args) throws IOException {
        final FrameBuffer frameBuffer = new FrameBuffer(FRAME_BUFFER_SIZE);
        final Bus bus = new Bus(frameBuffer, new Memory(MEMORY_SIZE));

        ProgramUtils programUtils = new ProgramUtils(bus);
        programUtils.writeProgramInMemory(programUtils.readFile(new File(args[0])));

        gpu = new GPU(WIDTH, HEIGHT, frameBuffer);
        cpu = new CPU(bus);
    }

    public static void info(String message) {
        if (LOG) {
            log.info(message);
        }
    }

}
