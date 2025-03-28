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
import java.util.logging.Logger;

@Log
public class Main {
    private static final int WIDTH = 320;

    private static final int HEIGHT = 240;

    private static final int FRAME_BUFFER_SIZE = WIDTH * HEIGHT * 4; // 4 bytes per pixel

    private static final int MEMORY_SIZE = 4194304; // 4MB

    @Getter
    private static int clockSpeed = 1000; // 1Hz

    private static boolean LOG = true;

    private static GPU gpu;

    private static CPU cpu;

    private static GUI gui;

    private static final Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException {
        LOG = args.length > 1 && args[1].equals("--log");

        gui = new GUI(new ArgsListener() {
            @Override
            public void onArgsSelected(String path) throws IOException {
//                WIDTH = Integer.parseInt(args[1]);
//                HEIGHT = Integer.parseInt(args[2]);
//                MEMORY_SIZE = Integer.parseInt(args[3]);
//                FRAME_BUFFER_SIZE = WIDTH * HEIGHT * 4;

                final FrameBuffer frameBuffer = new FrameBuffer(FRAME_BUFFER_SIZE);
                final Bus bus = new Bus(frameBuffer, new Memory(MEMORY_SIZE));

                ProgramUtils programUtils = new ProgramUtils(bus);
                programUtils.writeProgramInMemory(programUtils.readFile(new File(path)));

                gpu = new GPU(WIDTH, HEIGHT, frameBuffer);
                cpu = new CPU(bus, gui);
                cpu.start();
                gpu.start();

                while (gpu.isAlive()) {
                    if (gpu.getState() == Thread.State.TERMINATED) {
                        gpu.interrupt();
                        cpu.interrupt();
                    }
                }
            }
        });
    }

    public static void info(String message) {
        if (LOG) {
            log.info(message);
        }
    }
}
